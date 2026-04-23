package com.pairshot.core.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object PairShotGlassTokens {
    val surfaceColor = Color(0x991C1C1E)

    val contentColor = Color.White
    val border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
    val shape = RoundedCornerShape(20.dp)
    val shadowElevation = 20.dp

    val destructiveColor = Color(0xFFFF3B30)
    val destructiveContentColor = Color.White
}
