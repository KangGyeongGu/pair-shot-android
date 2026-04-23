package com.pairshot.feature.camera.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.pairshot.core.designsystem.PairShotCameraTokens
import androidx.compose.ui.unit.dp

@Composable
fun GridOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val strokeWidth = 1.dp.toPx()
        val color = PairShotCameraTokens.Foreground.copy(alpha = 0.3f)
        val thirdW = size.width / 3f
        val thirdH = size.height / 3f

        drawLine(color, Offset(thirdW, 0f), Offset(thirdW, size.height), strokeWidth)
        drawLine(color, Offset(thirdW * 2, 0f), Offset(thirdW * 2, size.height), strokeWidth)

        drawLine(color, Offset(0f, thirdH), Offset(size.width, thirdH), strokeWidth)
        drawLine(color, Offset(0f, thirdH * 2), Offset(size.width, thirdH * 2), strokeWidth)
    }
}
