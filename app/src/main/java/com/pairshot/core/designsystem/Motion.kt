package com.pairshot.core.designsystem

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween

/**
 * 앱 전역 모션 토큰.
 * 모든 duration/easing 값은 여기서 참조한다.
 */
object PairShotMotionTokens {
    // ── Easing ──────────────────────────────────────────
    /** 진입 시: 빠르게 시작 → 천천히 안착 */
    val EasingEnter: Easing = LinearOutSlowInEasing

    /** 퇴장 시: 천천히 시작 → 빠르게 사라짐 */
    val EasingExit: Easing = FastOutLinearInEasing

    // ── Screen Transition Durations ──────────────────────

    /** 화면 진입 페이드인 지속 시간 */
    const val DurationEnter = 220

    /** 화면 진입 시 딜레이 (이전 화면 퇴장 후 진입 시작) */
    const val DelayEnter = 130

    /** 화면 퇴장 페이드아웃 지속 시간 */
    const val DurationExit = 180

    /** 뒤로가기 진입 페이드인 지속 시간 */
    const val DurationPopEnter = 210

    /** 뒤로가기 진입 딜레이 */
    const val DelayPopEnter = 120

    /** 뒤로가기 퇴장 페이드아웃 지속 시간 */
    const val DurationPopExit = 170

    // ── Panel / Modal Durations ───────────────────────────

    /** 패널·모달 진입 지속 시간 */
    const val DurationPanelEnter = 200

    /** 패널·모달 퇴장 지속 시간 */
    const val DurationPanelExit = 160

    // ── AnimationSpec factories ───────────────────────────
    fun <T> enterTween() = tween<T>(durationMillis = DurationEnter, delayMillis = DelayEnter, easing = EasingEnter)

    fun <T> exitTween() = tween<T>(durationMillis = DurationExit, easing = EasingExit)

    fun <T> popEnterTween() = tween<T>(durationMillis = DurationPopEnter, delayMillis = DelayPopEnter, easing = EasingEnter)

    fun <T> popExitTween() = tween<T>(durationMillis = DurationPopExit, easing = EasingExit)

    fun <T> panelEnterTween() = tween<T>(durationMillis = DurationPanelEnter, easing = EasingEnter)

    fun <T> panelExitTween() = tween<T>(durationMillis = DurationPanelExit, easing = EasingExit)
}
