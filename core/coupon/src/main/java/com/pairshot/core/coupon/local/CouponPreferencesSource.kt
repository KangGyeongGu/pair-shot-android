package com.pairshot.core.coupon.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.couponDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "coupon_state",
)

@Singleton
class CouponPreferencesSource
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private object Keys {
            val LATEST_COUPON_ID = stringPreferencesKey("latest_coupon_id")
            val FIRST_ACTIVATED_AT_MILLIS = longPreferencesKey("first_activated_at_millis")
            val EXPIRES_AT_MILLIS = longPreferencesKey("expires_at_millis")
            val HAS_EXPIRY = longPreferencesKey("has_expiry")
            val PENDING_CODE = stringPreferencesKey("pending_code")
            val PENDING_SINCE = longPreferencesKey("pending_since")
        }

        val state: Flow<StoredCouponState?> =
            context.couponDataStore.data.map { prefs ->
                val id = prefs[Keys.LATEST_COUPON_ID] ?: return@map null
                val firstActivatedAt = prefs[Keys.FIRST_ACTIVATED_AT_MILLIS] ?: return@map null
                val hasExpiry = (prefs[Keys.HAS_EXPIRY] ?: 0L) == 1L
                val expiresAt = if (hasExpiry) prefs[Keys.EXPIRES_AT_MILLIS] else null
                StoredCouponState(
                    latestCouponId = id,
                    firstActivatedAtEpochMillis = firstActivatedAt,
                    expiresAtEpochMillis = expiresAt,
                )
            }

        val pending: Flow<PendingCouponActivation?> =
            context.couponDataStore.data.map { prefs ->
                val code = prefs[Keys.PENDING_CODE] ?: return@map null
                val since = prefs[Keys.PENDING_SINCE] ?: return@map null
                PendingCouponActivation(code = code, sinceEpochMillis = since)
            }

        suspend fun save(state: StoredCouponState) {
            context.couponDataStore.edit { prefs ->
                prefs[Keys.LATEST_COUPON_ID] = state.latestCouponId
                prefs[Keys.FIRST_ACTIVATED_AT_MILLIS] = state.firstActivatedAtEpochMillis
                if (state.expiresAtEpochMillis != null) {
                    prefs[Keys.EXPIRES_AT_MILLIS] = state.expiresAtEpochMillis
                    prefs[Keys.HAS_EXPIRY] = 1L
                } else {
                    prefs.remove(Keys.EXPIRES_AT_MILLIS)
                    prefs[Keys.HAS_EXPIRY] = 0L
                }
            }
        }

        suspend fun savePending(
            code: String,
            sinceEpochMillis: Long,
        ) {
            context.couponDataStore.edit { prefs ->
                prefs[Keys.PENDING_CODE] = code
                prefs[Keys.PENDING_SINCE] = sinceEpochMillis
            }
        }

        suspend fun clearPending() {
            context.couponDataStore.edit { prefs ->
                prefs.remove(Keys.PENDING_CODE)
                prefs.remove(Keys.PENDING_SINCE)
            }
        }

        suspend fun clear() {
            context.couponDataStore.edit { prefs -> prefs.clear() }
        }
    }
