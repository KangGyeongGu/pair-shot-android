package com.pairshot.core.ads.component

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.pairshot.core.ads.config.AdsConfig
import com.pairshot.core.ads.di.AdsEntryPoint
import com.pairshot.core.domain.coupon.AdFreeStatusProvider
import dagger.hilt.android.EntryPointAccessors

@Composable
fun PairShotBannerAd(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val entryPoint =
        remember(context) {
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                AdsEntryPoint::class.java,
            )
        }
    val adsConfig = remember(entryPoint) { entryPoint.adsConfig() }
    val adFreeStatusProvider = remember(entryPoint) { entryPoint.adFreeStatusProvider() }

    val isAdFree by adFreeStatusProvider
        .observeIsAdFree()
        .collectAsStateWithLifecycle(initialValue = false)

    if (isAdFree) return

    val configuration = LocalConfiguration.current
    val adWidth = configuration.screenWidthDp

    val adView =
        remember(context, adsConfig, adWidth) {
            buildAdView(context = context, adsConfig = adsConfig, widthDp = adWidth)
        }

    DisposableEffect(adView) {
        onDispose { adView.destroy() }
    }

    AndroidView(
        factory = { adView },
        modifier = modifier.fillMaxWidth(),
    )
}

private fun buildAdView(
    context: Context,
    adsConfig: AdsConfig,
    widthDp: Int,
): AdView {
    val activity = context.asActivityOrNull()
    val adSize =
        if (activity != null) {
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, widthDp)
        } else {
            AdSize.BANNER
        }
    return AdView(context).apply {
        layoutParams =
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
        setAdSize(adSize)
        adUnitId = adsConfig.bannerAdUnitId
        loadAd(AdRequest.Builder().build())
    }
}

private tailrec fun Context.asActivityOrNull(): Activity? =
    when (this) {
        is Activity -> this
        is android.content.ContextWrapper -> baseContext.asActivityOrNull()
        else -> null
    }
