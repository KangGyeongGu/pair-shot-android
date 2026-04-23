package com.pairshot.core.designsystem

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween

object PairShotMotionTokens {
    val EasingEnter: Easing = LinearOutSlowInEasing

    val EasingExit: Easing = FastOutLinearInEasing

    const val DurationEnter = 220

    const val DelayEnter = 130

    const val DurationExit = 180

    const val DurationPopEnter = 210

    const val DelayPopEnter = 120

    const val DurationPopExit = 170

    const val DurationPanelEnter = 200

    const val DurationPanelExit = 160

    fun <T> enterTween() = tween<T>(durationMillis = DurationEnter, delayMillis = DelayEnter, easing = EasingEnter)

    fun <T> exitTween() = tween<T>(durationMillis = DurationExit, easing = EasingExit)

    fun <T> popEnterTween() = tween<T>(durationMillis = DurationPopEnter, delayMillis = DelayPopEnter, easing = EasingEnter)

    fun <T> popExitTween() = tween<T>(durationMillis = DurationPopExit, easing = EasingExit)

    fun <T> panelEnterTween() = tween<T>(durationMillis = DurationPanelEnter, easing = EasingEnter)

    fun <T> panelExitTween() = tween<T>(durationMillis = DurationPanelExit, easing = EasingExit)
}
