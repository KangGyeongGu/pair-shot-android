package com.pairshot.feature.camera.chrome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pairshot.feature.camera.component.ShutterButton

@Composable
internal fun CameraBottomBar(
    isSaving: Boolean,
    shutterEnabled: Boolean,
    height: Dp,
    onToggleLens: () -> Unit,
    onToggleSettings: () -> Unit,
    onShutterClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(height)
                .background(Color.Black)
                .padding(horizontal = 32.dp),
    ) {
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onToggleLens) {
                Icon(
                    imageVector = Icons.Default.FlipCameraAndroid,
                    contentDescription = "카메라 전환",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }
            IconButton(onClick = onToggleSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "설정",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }
        }

        ShutterButton(
            onClick = onShutterClick,
            enabled = !isSaving && shutterEnabled,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}
