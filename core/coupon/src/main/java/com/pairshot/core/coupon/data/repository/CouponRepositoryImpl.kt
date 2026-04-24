package com.pairshot.core.coupon.data.repository

import com.pairshot.core.coupon.data.CouponStatusCalculator
import com.pairshot.core.coupon.domain.ActivationResult
import com.pairshot.core.coupon.domain.Coupon
import com.pairshot.core.coupon.domain.CouponDuration
import com.pairshot.core.coupon.domain.CouponListItem
import com.pairshot.core.coupon.domain.CouponListItemStatus
import com.pairshot.core.coupon.domain.CouponRepository
import com.pairshot.core.coupon.domain.CouponStatus
import com.pairshot.core.coupon.local.CouponPreferencesSource
import com.pairshot.core.coupon.local.DeviceHashProvider
import com.pairshot.core.coupon.local.StoredCouponState
import com.pairshot.core.coupon.remote.ActivationApiResult
import com.pairshot.core.coupon.remote.CouponActivationApi
import com.pairshot.core.coupon.remote.ListApiResult
import com.pairshot.core.coupon.remote.StatusApiResult
import com.pairshot.core.coupon.remote.dto.ActivateRequestDto
import com.pairshot.core.coupon.remote.dto.ActivateResponseDto
import com.pairshot.core.coupon.remote.dto.CouponListItemDto
import com.pairshot.core.coupon.remote.dto.StatusRequestDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CouponRepositoryImpl
    @Inject
    constructor(
        private val api: CouponActivationApi,
        private val preferences: CouponPreferencesSource,
        private val deviceHashProvider: DeviceHashProvider,
    ) : CouponRepository {
        override fun observeStatus(): Flow<CouponStatus> =
            preferences.state.map { stored ->
                CouponStatusCalculator.toStatus(stored, System.currentTimeMillis())
            }

        override suspend fun activate(couponCode: String): ActivationResult {
            val trimmed = couponCode.trim()
            if (trimmed.isEmpty()) return ActivationResult.Failure.InvalidFormat

            preferences.savePending(trimmed, System.currentTimeMillis())

            val deviceHash = deviceHashProvider.deviceHash()
            val apiResult = api.activate(ActivateRequestDto(code = trimmed, deviceHash = deviceHash))

            return when (apiResult) {
                is ActivationApiResult.Success -> {
                    preferences.clearPending()
                    onSuccess(apiResult.response)
                }

                ActivationApiResult.InvalidCodeFormat -> {
                    preferences.clearPending()
                    ActivationResult.Failure.InvalidFormat
                }

                ActivationApiResult.InvalidSignature -> {
                    preferences.clearPending()
                    ActivationResult.Failure.InvalidSignature
                }

                ActivationApiResult.NotFound -> {
                    preferences.clearPending()
                    ActivationResult.Failure.NotFound
                }

                ActivationApiResult.AlreadyUsedOnAnotherDevice -> {
                    preferences.clearPending()
                    ActivationResult.Failure.AlreadyUsedOnAnotherDevice
                }

                ActivationApiResult.Revoked -> {
                    preferences.clearPending()
                    ActivationResult.Failure.Revoked
                }

                ActivationApiResult.NetworkError -> {
                    ActivationResult.Failure.NetworkError
                }

                ActivationApiResult.ServerError -> {
                    ActivationResult.Failure.UnknownError
                }
            }
        }

        override suspend fun retryPendingIfAny() {
            val pending = preferences.pending.first() ?: return
            if (System.currentTimeMillis() - pending.sinceEpochMillis > PENDING_EXPIRY_MS) {
                preferences.clearPending()
                return
            }
            activate(pending.code)
        }

        private suspend fun onSuccess(response: ActivateResponseDto): ActivationResult {
            val now = System.currentTimeMillis()
            val newDurationDays = response.durationDays
            val serverActivatedMillis = parseIsoMillisOrNow(response.activatedAt)

            val current = preferences.state.first()

            val firstActivatedAt = current?.firstActivatedAtEpochMillis ?: serverActivatedMillis

            val newExpiresAt =
                computeNewExpiry(
                    current = current?.expiresAtEpochMillis,
                    currentIsUnlimited = current != null && current.expiresAtEpochMillis == null,
                    newDurationDays = newDurationDays,
                    now = now,
                )

            val stored =
                StoredCouponState(
                    latestCouponId = response.couponId,
                    firstActivatedAtEpochMillis = firstActivatedAt,
                    expiresAtEpochMillis = newExpiresAt,
                )
            preferences.save(stored)

            val duration = CouponDuration.fromDays(newDurationDays)
            val coupon =
                Coupon(
                    id = response.couponId,
                    duration = duration,
                    activatedAtEpochMillis = serverActivatedMillis,
                )
            return ActivationResult.Success(coupon = coupon, expiresAtEpochMillis = newExpiresAt)
        }

        internal fun computeNewExpiry(
            current: Long?,
            currentIsUnlimited: Boolean,
            newDurationDays: Long?,
            now: Long,
        ): Long? {
            if (currentIsUnlimited) return null
            if (newDurationDays == null) return null
            val currentRemaining = current?.let { maxOf(0L, it - now) } ?: 0L
            val newDurationMs = newDurationDays * MILLIS_PER_DAY
            return now + currentRemaining + newDurationMs
        }

        override suspend fun syncStatus() {
            val stored = preferences.state.first() ?: return
            val deviceHash = deviceHashProvider.deviceHash()
            val result =
                api.fetchStatus(
                    StatusRequestDto(couponId = stored.latestCouponId, deviceHash = deviceHash),
                )
            when (result) {
                StatusApiResult.Activated -> {
                    Unit
                }

                StatusApiResult.Revoked,
                StatusApiResult.NotFoundOrForeign,
                -> {
                    Timber.i("Coupon revoked or no longer valid on server, clearing local state")
                    preferences.clear()
                }

                StatusApiResult.NetworkError,
                StatusApiResult.ServerError,
                -> {
                    Unit
                }
            }
        }

        override suspend fun fetchMyCoupons(): List<CouponListItem> {
            val deviceHash = deviceHashProvider.deviceHash()
            val result = api.fetchMyCoupons(deviceHash)
            return when (result) {
                is ListApiResult.Success -> result.coupons.mapNotNull { dto -> dto.toDomainOrNull() }
                ListApiResult.NetworkError, ListApiResult.ServerError -> emptyList()
            }
        }

        override suspend fun clear() {
            preferences.clear()
        }

        private fun parseIsoMillisOrNow(iso: String?): Long = parseIsoMillisOrNull(iso) ?: System.currentTimeMillis()

        private fun parseIsoMillisOrNull(iso: String?): Long? =
            iso?.let { value ->
                runCatching { Instant.parse(value).toEpochMilli() }.getOrNull()
            }

        private companion object {
            const val MILLIS_PER_DAY: Long = 24L * 60L * 60L * 1000L
            const val PENDING_EXPIRY_MS: Long = 7L * 24L * 60L * 60L * 1000L
        }
    }

private fun CouponListItemDto.toDomainOrNull(): CouponListItem? {
    val parsedStatus =
        when (status) {
            "activated" -> CouponListItemStatus.ACTIVATED
            "expired" -> CouponListItemStatus.EXPIRED
            "revoked" -> CouponListItemStatus.REVOKED
            else -> return null
        }
    val activatedAtMillis =
        runCatching { Instant.parse(activatedAt).toEpochMilli() }.getOrNull() ?: return null
    return CouponListItem(
        couponId = couponId,
        shortCode = shortCode,
        durationDays = durationDays,
        status = parsedStatus,
        activatedAtEpochMillis = activatedAtMillis,
        batchLabel = batchLabel,
    )
}
