package com.pairshot.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FlashAuto
import androidx.compose.material.icons.outlined.FlashOff
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.FlashlightOn
import androidx.compose.material.icons.outlined.Grid3x3
import androidx.compose.material.icons.outlined.HdrOn
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.pairshot.ui.camera.CameraCapabilities
import com.pairshot.ui.camera.CameraSettingsState
import com.pairshot.ui.camera.FlashMode
import kotlin.math.roundToInt

@Composable
fun CameraSettingsPanel(
    visible: Boolean,
    settingsState: CameraSettingsState,
    capabilities: CameraCapabilities,
    onToggleGrid: () -> Unit,
    onCycleFlash: () -> Unit,
    onToggleNightMode: () -> Unit,
    onToggleHdr: () -> Unit,
    onToggleLevel: () -> Unit,
    onDismiss: () -> Unit,
    overlayAlpha: Float? = null,
    onOverlayAlphaChange: ((Float) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (visible) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) { onDismiss() },
            )
        }

        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) { /* consume clicks so they don't reach the dismiss layer */ },
            ) {
                val settingItems =
                    buildSettingItems(
                        state = settingsState,
                        capabilities = capabilities,
                        onToggleGrid = onToggleGrid,
                        onCycleFlash = onCycleFlash,
                        onToggleNightMode = onToggleNightMode,
                        onToggleHdr = onToggleHdr,
                        onToggleLevel = onToggleLevel,
                    )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    settingItems.forEach { item ->
                        SettingIconItem(
                            icon = item.icon,
                            label = item.label,
                            statusText = item.statusText,
                            isActive = item.isActive,
                            onClick = item.onClick,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                if (overlayAlpha != null && onOverlayAlphaChange != null) {
                    Spacer(modifier = Modifier.height(20.dp))
                    OverlayAlphaSlider(
                        alpha = overlayAlpha,
                        onAlphaChange = onOverlayAlphaChange,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

private data class SettingItem(
    val icon: ImageVector,
    val label: String,
    val statusText: String,
    val isActive: Boolean,
    val onClick: () -> Unit,
)

private fun buildSettingItems(
    state: CameraSettingsState,
    capabilities: CameraCapabilities,
    onToggleGrid: () -> Unit,
    onCycleFlash: () -> Unit,
    onToggleNightMode: () -> Unit,
    onToggleHdr: () -> Unit,
    onToggleLevel: () -> Unit,
): List<SettingItem> {
    val items = mutableListOf<SettingItem>()

    items.add(
        SettingItem(
            icon = Icons.Outlined.Grid3x3,
            label = "격자",
            statusText = if (state.gridEnabled) "ON" else "OFF",
            isActive = state.gridEnabled,
            onClick = onToggleGrid,
        ),
    )

    if (capabilities.hasFlash) {
        val (flashIcon, flashStatus) =
            when (state.flashMode) {
                FlashMode.OFF -> Icons.Outlined.FlashOff to "OFF"
                FlashMode.AUTO -> Icons.Outlined.FlashAuto to "AUTO"
                FlashMode.ON -> Icons.Outlined.FlashOn to "ON"
                FlashMode.TORCH -> Icons.Outlined.FlashlightOn to "TORCH"
            }
        items.add(
            SettingItem(
                icon = flashIcon,
                label = "플래시",
                statusText = flashStatus,
                isActive = state.flashMode != FlashMode.OFF,
                onClick = onCycleFlash,
            ),
        )
    }

    if (capabilities.nightModeAvailable) {
        items.add(
            SettingItem(
                icon = Icons.Outlined.NightsStay,
                label = "야간모드",
                statusText = if (state.nightModeEnabled) "ON" else "OFF",
                isActive = state.nightModeEnabled,
                onClick = onToggleNightMode,
            ),
        )
    }

    if (capabilities.hdrAvailable) {
        items.add(
            SettingItem(
                icon = Icons.Outlined.HdrOn,
                label = "HDR",
                statusText = if (state.hdrEnabled) "ON" else "OFF",
                isActive = state.hdrEnabled,
                onClick = onToggleHdr,
            ),
        )
    }

    items.add(
        SettingItem(
            icon = Icons.Outlined.Straighten,
            label = "수평계",
            statusText = if (state.levelEnabled) "ON" else "OFF",
            isActive = state.levelEnabled,
            onClick = onToggleLevel,
        ),
    )

    return items
}

@Composable
private fun SettingIconItem(
    icon: ImageVector,
    label: String,
    statusText: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onClick,
                ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        } else {
                            Color.Transparent
                        },
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp),
                tint = if (isActive) MaterialTheme.colorScheme.primary else Color.White,
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
        )
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun OverlayAlphaSlider(
    alpha: Float,
    onAlphaChange: (Float) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "오버레이 투명도",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
            )
            Text(
                text = "${(alpha * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Slider(
            value = alpha,
            onValueChange = onAlphaChange,
            valueRange = 0f..0.5f,
            colors =
                SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
        )
    }
}
