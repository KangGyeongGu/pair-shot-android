package com.pairshot.core.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Glass-style surface tokens shared across dialogs, panels, and modals.
 * All surfaces use the same dark translucent base color for visual coherence.
 */
object PairShotGlassTokens {
    /** 60% opaque dark — the unified opacity level for all glass surfaces */
    val surfaceColor = Color(0x991C1C1E)

    val contentColor = Color.White
    val border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
    val shape = RoundedCornerShape(20.dp)
    val shadowElevation = 20.dp

    /**
     * Vivid red for destructive action buttons on dark glass surfaces.
     * MaterialTheme.colorScheme.error in dark theme resolves to a pastel pink (0xFFB4AB),
     * which is unreadable as a button fill. This is the correct solid red for glass contexts.
     */
    val destructiveColor = Color(0xFFFF3B30)
    val destructiveContentColor = Color.White
}
