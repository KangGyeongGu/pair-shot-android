package com.pairshot.core.ads.initializer

import android.content.Context
import com.google.android.gms.ads.MobileAds
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdsInitializer
    @Inject
    constructor() {
        private val initialized = AtomicBoolean(false)

        fun initialize(context: Context) {
            if (!initialized.compareAndSet(false, true)) return
            runCatching {
                MobileAds.initialize(context.applicationContext) { status ->
                    Timber.tag(TAG).d("MobileAds initialized: %s", status.adapterStatusMap)
                }
            }.onFailure { error ->
                Timber.tag(TAG).e(error, "MobileAds initialize failed")
                initialized.set(false)
            }
        }

        private companion object {
            const val TAG = "AdsInitializer"
        }
    }
