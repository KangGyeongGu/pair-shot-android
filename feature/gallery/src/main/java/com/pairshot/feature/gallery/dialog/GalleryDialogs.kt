package com.pairshot.feature.gallery.dialog

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.pairshot.core.designsystem.PairShotSpacing
import com.pairshot.core.model.CombineConfig
import com.pairshot.core.model.CombineLayout
import com.pairshot.core.model.RenderProfile
import com.pairshot.core.model.WatermarkConfig
import com.pairshot.core.rendering.PairImageComposer
import com.pairshot.core.ui.component.PairShotBottomSheet
import com.pairshot.core.ui.component.PairShotDialog

@Composable
internal fun DeletePairsDialog(
    selectedCount: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    PairShotDialog(
        onDismissRequest = onDismiss,
        title = { Text("${selectedCount}개 페어 삭제") },
        text = { Text("삭제된 사진은 복구할 수 없습니다.", style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("삭제", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        },
    )
}

@Composable
internal fun RenameProjectDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var newName by remember { mutableStateOf(currentName) }
    PairShotDialog(
        onDismissRequest = onDismiss,
        title = { Text("프로젝트명 수정", style = MaterialTheme.typography.titleMedium) },
        text = {
            TextField(
                value = newName,
                onValueChange = { newName = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors =
                    TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(newName) },
                enabled = newName.isNotBlank(),
            ) {
                Text("저장", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        },
    )
}

@Composable
internal fun DeleteProjectDialog(
    projectName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    PairShotDialog(
        onDismissRequest = onDismiss,
        title = { Text("프로젝트 삭제") },
        text = { Text("'$projectName' 프로젝트와 모든 사진이 삭제됩니다.", style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("삭제", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        },
    )
}

@Composable
internal fun CombinePreviewBottomSheet(
    selectedCount: Int,
    beforeUri: String?,
    afterUri: String?,
    combineConfig: CombineConfig,
    watermarkConfig: WatermarkConfig,
    pairImageComposer: PairImageComposer,
    onDismiss: () -> Unit,
    onCombineStart: (applyWatermark: Boolean, combineConfigOverride: CombineConfig?) -> Unit,
    onNavigateToCombineSettings: () -> Unit,
    onNavigateToWatermarkSettings: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    var applyOverlays by remember { mutableStateOf(combineConfig.borderEnabled || combineConfig.labelEnabled) }
    var applyWatermark by remember { mutableStateOf(watermarkConfig.enabled) }

    val effectiveCombineConfig =
        remember(combineConfig, applyOverlays) {
            if (applyOverlays) combineConfig else combineConfig.copy(borderEnabled = false, labelEnabled = false)
        }
    val effectiveWatermark =
        remember(watermarkConfig, applyWatermark) {
            watermarkConfig.copy(enabled = applyWatermark)
        }

    PairShotBottomSheet(onDismissRequest = onDismiss) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "합성 미리보기",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${selectedCount}개 페어",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (beforeUri != null && afterUri != null) {
            GalleryCompositePreview(
                beforeUri = beforeUri,
                afterUri = afterUri,
                combineConfig = effectiveCombineConfig,
                watermarkConfig = effectiveWatermark,
                pairImageComposer = pairImageComposer,
            )
        } else {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "현재 합성 설정이 적용됩니다",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "합성 옵션",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Switch(
                checked = applyOverlays,
                onCheckedChange = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    applyOverlays = it
                },
                colors =
                    SwitchDefaults.colors(
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                modifier = Modifier.wrapContentHeight(unbounded = true).scale(0.67f),
            )
            IconButton(onClick = {
                onDismiss()
                onNavigateToCombineSettings()
            }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "합성 설정",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        HorizontalDivider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "워터마크 포함",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Switch(
                checked = applyWatermark,
                onCheckedChange = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    applyWatermark = it
                },
                colors =
                    SwitchDefaults.colors(
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                modifier = Modifier.wrapContentHeight(unbounded = true).scale(0.67f),
            )
            IconButton(onClick = {
                onDismiss()
                onNavigateToWatermarkSettings()
            }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "워터마크 설정",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onDismiss()
                val configOverride =
                    if (!applyOverlays) {
                        combineConfig.copy(borderEnabled = false, labelEnabled = false)
                    } else {
                        null
                    }
                onCombineStart(applyWatermark, configOverride)
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(PairShotSpacing.touchTarget),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text("합성 시작")
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun GalleryCompositePreview(
    beforeUri: String,
    afterUri: String,
    combineConfig: CombineConfig,
    watermarkConfig: WatermarkConfig,
    pairImageComposer: PairImageComposer,
) {
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(beforeUri, afterUri, combineConfig, watermarkConfig) {
        val result =
            runCatching {
                pairImageComposer.compose(
                    beforeUri = Uri.parse(beforeUri),
                    afterUri = Uri.parse(afterUri),
                    combineConfig = combineConfig,
                    watermarkConfig = watermarkConfig,
                    profile = RenderProfile.PREVIEW,
                )
            }.getOrNull()
        previewBitmap?.let { old ->
            if (old !== result && !old.isRecycled) old.recycle()
        }
        previewBitmap = result
    }

    val isVertical = combineConfig.layout == CombineLayout.VERTICAL
    val bmp = previewBitmap
    if (bmp != null) {
        val bitmapRatio = bmp.width.toFloat().coerceAtLeast(1f) / bmp.height.toFloat().coerceAtLeast(1f)
        Image(
            bitmap = bmp.asImageBitmap(),
            contentDescription = "합성 미리보기",
            contentScale = ContentScale.Fit,
            modifier =
                if (isVertical) {
                    Modifier.fillMaxWidth().heightIn(max = 220.dp)
                } else {
                    Modifier.fillMaxWidth().aspectRatio(bitmapRatio)
                },
        )
    } else {
        Box(
            modifier =
                if (isVertical) {
                    Modifier.fillMaxWidth().height(160.dp)
                } else {
                    Modifier.fillMaxWidth().aspectRatio(2f)
                },
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
internal fun DeleteWithCombinedDialog(
    pairCount: Int,
    combinedCount: Int,
    onDismiss: () -> Unit,
    onDeleteAll: () -> Unit,
    onDeleteCombinedOnly: () -> Unit,
) {
    PairShotBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = "삭제 방식 선택",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${pairCount}개 선택됨, ${combinedCount}개 합성본",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(20.dp))
        TextButton(
            onClick = onDeleteAll,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 14.dp),
        ) {
            Text(
                text = "일괄 삭제",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
            )
        }
        TextButton(
            onClick = onDeleteCombinedOnly,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 14.dp),
        ) {
            Text(
                text = "합성본만 삭제",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 14.dp),
        ) {
            Text(
                text = "취소",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
