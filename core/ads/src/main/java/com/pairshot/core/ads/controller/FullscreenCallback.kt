package com.pairshot.core.ads.controller

import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import timber.log.Timber

internal fun fullscreenCallback(
    tag: String,
    fullscreenAdState: FullscreenAdState,
    onDismissed: () -> Unit,
    onShowFailed: (AdError) -> Unit,
    onShown: () -> Unit = {},
): FullScreenContentCallback =
    object : FullScreenContentCallback() {
        override fun onAdDismissedFullScreenContent() {
            fullscreenAdState.markDismissed()
            onDismissed()
        }

        override fun onAdFailedToShowFullScreenContent(error: AdError) {
            Timber.tag(tag).w("show failed: %s", error.message)
            fullscreenAdState.markDismissed()
            onShowFailed(error)
        }

        override fun onAdShowedFullScreenContent() {
            onShown()
        }
    }
