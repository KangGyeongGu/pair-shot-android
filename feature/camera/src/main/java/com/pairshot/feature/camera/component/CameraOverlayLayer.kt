package com.pairshot.feature.camera.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun CameraOverlayLayer(
    gridEnabled: Boolean,
    levelEnabled: Boolean,
    roll: Float,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = gridEnabled,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            GridOverlay()
        }

        AnimatedVisibility(
            visible = levelEnabled,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            LevelOverlay(roll = roll)
        }
    }
}
