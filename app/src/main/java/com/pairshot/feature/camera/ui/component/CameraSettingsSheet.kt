package com.pairshot.feature.camera.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FlashAuto
import androidx.compose.material.icons.outlined.FlashOff
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.FlashlightOn
import androidx.compose.material.icons.outlined.Grid3x3
import androidx.compose.material.icons.outlined.HdrOn
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.pairshot.core.designsystem.PairShotMotionTokens
import com.pairshot.core.designsystem.PairShotSpacing
import com.pairshot.core.designsystem.PairShotTypographyTokens
import com.pairshot.feature.camera.ui.state.CameraCapabilities
import com.pairshot.feature.camera.ui.state.CameraSettingsState
import com.pairshot.feature.camera.ui.state.FlashMode
import kotlin.math.roundToInt

@Composable
fun CameraSettingsSheet(
    visible: Boolean,
    settingsState: CameraSettingsState,
    capabilities: CameraCapabilities,
    onToggleGrid: () -> Unit,
    onCycleFlash: () -> Unit,
    onToggleNightMode: () -> Unit,
    onToggleHdr: () -> Unit,
    onToggleLevel: () -> Unit,
    onDismiss: () -> Unit,
    overlayEnabled: Boolean? = null,
    onToggleOverlay: (() -> Unit)? = null,
    overlayAlpha: Float? = null,
    onOverlayAlphaChange: ((Float) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val isDark = isSystemInDarkTheme()
    val scrimAlpha = if (isDark) 0.62f else 0.45f

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = PairShotMotionTokens.panelEnterTween()),
            exit = fadeOut(animationSpec = PairShotMotionTokens.panelExitTween()),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = scrimAlpha))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) { onDismiss() },
            )
        }

        AnimatedVisibility(
            visible = visible,
            enter =
                fadeIn(animationSpec = PairShotMotionTokens.panelEnterTween()) +
                    scaleIn(initialScale = 0.96f, animationSpec = PairShotMotionTokens.panelEnterTween()),
            exit =
                fadeOut(animationSpec = PairShotMotionTokens.panelExitTween()) +
                    scaleOut(targetScale = 0.98f, animationSpec = PairShotMotionTokens.panelExitTween()),
            modifier = Modifier.align(Alignment.Center),
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(horizontal = PairShotSpacing.cardPadding)
                        .widthIn(max = 520.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = PairShotSpacing.screenPadding, vertical = PairShotSpacing.screenPadding)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) { },
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
                        overlayEnabled = overlayEnabled,
                        onToggleOverlay = onToggleOverlay,
                    )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    settingItems.forEach { item ->
                        SettingIconItem(
                            icon = item.icon,
                            label = item.label,
                            isActive = item.isActive,
                            onClick = item.onClick,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                if (overlayAlpha != null && onOverlayAlphaChange != null) {
                    Spacer(modifier = Modifier.height(PairShotSpacing.iconSize))
                    OverlayAlphaSlider(
                        alpha = overlayAlpha,
                        enabled = overlayEnabled == true,
                        onAlphaChange = onOverlayAlphaChange,
                    )
                }
            }
        }
    }
}

private data class SettingItem(
    val icon: ImageVector,
    val label: String,
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
    overlayEnabled: Boolean? = null,
    onToggleOverlay: (() -> Unit)? = null,
): List<SettingItem> {
    val items = mutableListOf<SettingItem>()

    items.add(
        SettingItem(
            icon = Icons.Outlined.Grid3x3,
            label = "격자",
            isActive = state.gridEnabled,
            onClick = onToggleGrid,
        ),
    )

    if (capabilities.hasFlash) {
        val flashIcon =
            when (state.flashMode) {
                FlashMode.OFF -> Icons.Outlined.FlashOff
                FlashMode.AUTO -> Icons.Outlined.FlashAuto
                FlashMode.ON -> Icons.Outlined.FlashOn
                FlashMode.TORCH -> Icons.Outlined.FlashlightOn
            }
        items.add(
            SettingItem(
                icon = flashIcon,
                label = "플래시",
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
                isActive = state.hdrEnabled,
                onClick = onToggleHdr,
            ),
        )
    }

    if (overlayEnabled != null && onToggleOverlay != null) {
        items.add(
            SettingItem(
                icon = Icons.Outlined.Layers,
                label = "오버레이",
                isActive = overlayEnabled,
                onClick = onToggleOverlay,
            ),
        )
    }

    items.add(
        SettingItem(
            icon = Icons.Outlined.Straighten,
            label = "수평계",
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
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier.clickable(
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
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
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
                tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = label,
            style = PairShotTypographyTokens.labelExtraSmall,
            color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OverlayAlphaSlider(
    alpha: Float,
    enabled: Boolean,
    onAlphaChange: (Float) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val contentColor =
        if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    val primaryColor =
        if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    val sliderColors =
        SliderDefaults.colors(
            thumbColor = primaryColor,
            activeTrackColor = primaryColor,
            inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant,
            disabledThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            disabledActiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "오버레이 투명도",
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
            )
            Text(
                text = "${(alpha * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = primaryColor,
            )
        }
        Slider(
            value = alpha,
            onValueChange = onAlphaChange,
            valueRange = 0f..0.5f,
            enabled = enabled,
            interactionSource = interactionSource,
            colors = sliderColors,
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = interactionSource,
                    thumbSize = DpSize(1.dp, 16.dp),
                    colors = sliderColors,
                )
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    modifier = Modifier.graphicsLayer(scaleY = 0.3f),
                    colors = sliderColors,
                    drawTick = { _, _ -> },
                    drawStopIndicator = null,
                )
            },
        )
    }
}
