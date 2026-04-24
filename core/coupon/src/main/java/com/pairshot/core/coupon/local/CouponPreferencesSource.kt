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
            val COUPON_ID = stringPreferencesKey("coupon_id")
            val DURATION_DAYS = longPreferencesKey("duration_days")
            val ACTIVATED_AT_MILLIS = longPreferencesKey("activated_at_millis")
            val HAS_DURATION = longPreferencesKey("has_duration_flag")
        }

        val state: Flow<StoredCouponState?> =
            context.couponDataStore.data.map { prefs ->
                val id = prefs[Keys.COUPON_ID] ?: return@map null
                val activatedAt = prefs[Keys.ACTIVATED_AT_MILLIS] ?: return@map null
                val hasDuration = (prefs[Keys.HAS_DURATION] ?: 0L) == 1L
                val durationDays = if (hasDuration) prefs[Keys.DURATION_DAYS] else null
                StoredCouponState(
                    couponId = id,
                    durationDays = durationDays,
                    activatedAtEpochMillis = activatedAt,
                )
            }

        suspend fun save(state: StoredCouponState) {
            context.couponDataStore.edit { prefs ->
                prefs[Keys.COUPON_ID] = state.couponId
                prefs[Keys.ACTIVATED_AT_MILLIS] = state.activatedAtEpochMillis
                if (state.durationDays != null) {
                    prefs[Keys.DURATION_DAYS] = state.durationDays
                    prefs[Keys.HAS_DURATION] = 1L
                } else {
                    prefs.remove(Keys.DURATION_DAYS)
                    prefs[Keys.HAS_DURATION] = 0L
                }
            }
        }

        suspend fun clear() {
            context.couponDataStore.edit { prefs -> prefs.clear() }
        }
    }
