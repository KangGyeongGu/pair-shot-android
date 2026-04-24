package com.pairshot.core.coupon.data.repository

import com.pairshot.core.coupon.crypto.CouponParseError
import com.pairshot.core.coupon.crypto.CouponParseException
import com.pairshot.core.coupon.crypto.CouponPayloadParser
import com.pairshot.core.coupon.crypto.CouponSignatureVerifier
import com.pairshot.core.coupon.crypto.ParsedCoupon
import com.pairshot.core.coupon.data.CouponStatusCalculator
import com.pairshot.core.coupon.domain.ActivationResult
import com.pairshot.core.coupon.domain.Coupon
import com.pairshot.core.coupon.domain.CouponDuration
import com.pairshot.core.coupon.domain.CouponRepository
import com.pairshot.core.coupon.domain.CouponStatus
import com.pairshot.core.coupon.local.CouponPreferencesSource
import com.pairshot.core.coupon.local.DeviceHashProvider
import com.pairshot.core.coupon.local.StoredCouponState
import com.pairshot.core.coupon.remote.ActivationApiResult
import com.pairshot.core.coupon.remote.CouponActivationApi
import com.pairshot.core.coupon.remote.dto.ActivateRequestDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CouponRepositoryImpl
    @Inject
    constructor(
        private val parser: CouponPayloadParser,
        private val verifier: CouponSignatureVerifier,
        private val api: CouponActivationApi,
        private val preferences: CouponPreferencesSource,
        private val deviceHashProvider: DeviceHashProvider,
    ) : CouponRepository {
        override fun observeStatus(): Flow<CouponStatus> =
            preferences.state.map { stored ->
                CouponStatusCalculator.toStatus(stored, System.currentTimeMillis())
            }

        override suspend fun activate(couponCode: String): ActivationResult {
            val parsed =
                parser.parse(couponCode).getOrElse { error ->
                    val reason = (error as? CouponParseException)?.reason
                    return when (reason) {
                        CouponParseError.TypeMismatch -> ActivationResult.Failure.InvalidSignature
                        else -> ActivationResult.Failure.InvalidFormat
                    }
                }

            val signatureValid =
                runCatching { verifier.verify(parsed.payloadBytes, parsed.signatureBytes) }
                    .getOrElse {
                        Timber.w(it, "Coupon signature verifier failed")
                        false
                    }
            if (!signatureValid) return ActivationResult.Failure.InvalidSignature

            val deviceHash = deviceHashProvider.deviceHash()
            val apiResult =
                api.activate(
                    ActivateRequestDto(
                        couponId = parsed.couponId,
                        signatureB58 = parsed.signatureBase58,
                        deviceHash = deviceHash,
                    ),
                )

            return when (apiResult) {
                is ActivationApiResult.Success -> onSuccess(parsed, apiResult)
                ActivationApiResult.NotFound -> ActivationResult.Failure.NotFound
                ActivationApiResult.AlreadyUsedOnAnotherDevice -> ActivationResult.Failure.AlreadyUsedOnAnotherDevice
                ActivationApiResult.Revoked -> ActivationResult.Failure.Revoked
                ActivationApiResult.InvalidRequest -> ActivationResult.Failure.InvalidFormat
                ActivationApiResult.NetworkError -> ActivationResult.Failure.NetworkError
                ActivationApiResult.ServerError -> ActivationResult.Failure.UnknownError
            }
        }

        private suspend fun onSuccess(
            parsed: ParsedCoupon,
            apiResult: ActivationApiResult.Success,
        ): ActivationResult {
            val durationDays = apiResult.response.durationDays ?: parsed.durationDays
            val activatedMillis = parseIsoMillisOrNow(apiResult.response.activatedAt)
            val stored =
                StoredCouponState(
                    couponId = parsed.couponId,
                    durationDays = durationDays,
                    activatedAtEpochMillis = activatedMillis,
                )
            preferences.save(stored)

            val duration = CouponDuration.fromDays(durationDays)
            val coupon =
                Coupon(
                    id = parsed.couponId,
                    duration = duration,
                    activatedAtEpochMillis = activatedMillis,
                )
            val expiresMillis =
                apiResult.response.expiresAt?.let { parseIsoMillisOrNull(it) }
                    ?: duration.days?.let { days -> activatedMillis + days * MILLIS_PER_DAY }
            return ActivationResult.Success(coupon = coupon, expiresAtEpochMillis = expiresMillis)
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
        }
    }
