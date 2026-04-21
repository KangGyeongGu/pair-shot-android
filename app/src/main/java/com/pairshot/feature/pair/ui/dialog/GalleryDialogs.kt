package com.pairshot.feature.pair.ui.dialog

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.exifinterface.media.ExifInterface
import com.pairshot.core.designsystem.PairShotSpacing
import com.pairshot.core.model.CombineConfig
import com.pairshot.core.model.CombineLayout
import com.pairshot.core.model.LabelAnchor
import com.pairshot.core.model.LabelPosition
import com.pairshot.core.model.LabelPositionMode
import com.pairshot.core.model.WatermarkConfig
import com.pairshot.core.infra.image.WatermarkRenderer
import com.pairshot.core.ui.component.PairShotBottomSheet
import com.pairshot.core.ui.component.PairShotDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    watermarkRenderer: WatermarkRenderer,
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
                watermarkRenderer = watermarkRenderer,
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

        // 합성 옵션 행
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

        // 워터마크 행
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
    watermarkRenderer: WatermarkRenderer,
) {
    val context = LocalContext.current
    val density = LocalDensity.current.density
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(beforeUri, afterUri, combineConfig, watermarkConfig) {
        val result =
            withContext(Dispatchers.IO) {
                val before = loadBitmapWithExif(context, Uri.parse(beforeUri), inSampleSize = 4)
                val after = loadBitmapWithExif(context, Uri.parse(afterUri), inSampleSize = 4)
                if (before != null && after != null) {
                    val wmBefore = watermarkRenderer.apply(before, watermarkConfig)
                    if (wmBefore !== before) before.recycle()
                    val wmAfter = watermarkRenderer.apply(after, watermarkConfig)
                    if (wmAfter !== after) after.recycle()
                    buildGalleryPreviewBitmap(wmBefore, wmAfter, combineConfig, density)
                } else {
                    null
                }
            }
        previewBitmap?.recycle()
        previewBitmap = result
    }

    // 수직 모드는 높이를 제한해 버튼이 항상 보이도록 함, 수평 모드는 2:1 비율 유지
    val isVertical = combineConfig.layout == CombineLayout.VERTICAL
    val bmp = previewBitmap
    if (bmp != null) {
        val bitmapRatio = bmp.width.toFloat().coerceAtLeast(1f) / bmp.height.toFloat().coerceAtLeast(1f)
        // Box 없이 Image를 직접 배치 → ContentScale.Fit이 투명하게 처리되어 검정 여백 없음
        Image(
            bitmap = bmp.asImageBitmap(),
            contentDescription = "합성 미리보기",
            contentScale = ContentScale.Fit,
            modifier =
                if (isVertical) {
                    // 수직: 높이 상한 220dp, 폭은 비트맵 비율에 따라 자동
                    Modifier.fillMaxWidth().heightIn(max = 220.dp)
                } else {
                    // 수평: 폭 100%, 높이는 실제 비트맵 비율로
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

private fun buildGalleryPreviewBitmap(
    before: Bitmap,
    after: Bitmap,
    config: CombineConfig,
    density: Float,
): Bitmap {
    // inSampleSize=4로 로드한 ~1000px 이미지, 실제 카메라 4000px 대비 0.25 비율 적용
    val border = if (config.borderEnabled) (config.borderThicknessDp * density * 0.25f).toInt() else 0

    val (width, height) =
        when (config.layout) {
            CombineLayout.HORIZONTAL -> {
                (before.width + after.width + border * 3) to
                    (maxOf(before.height, after.height) + border * 2)
            }

            CombineLayout.VERTICAL -> {
                (maxOf(before.width, after.width) + border * 2) to
                    (before.height + after.height + border * 3)
            }
        }

    val combined = Bitmap.createBitmap(width.coerceAtLeast(1), height.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(combined)

    if (config.borderEnabled) {
        canvas.drawColor(config.borderColorArgb)
    } else {
        canvas.drawColor(android.graphics.Color.BLACK)
    }

    val (bLeft, bTop, aLeft, aTop) =
        when (config.layout) {
            CombineLayout.HORIZONTAL -> listOf(border, border, border + before.width + border, border)
            CombineLayout.VERTICAL -> listOf(border, border, border, border + before.height + border)
        }

    canvas.drawBitmap(before, bLeft.toFloat(), bTop.toFloat(), null)
    canvas.drawBitmap(after, aLeft.toFloat(), aTop.toFloat(), null)

    if (config.labelEnabled) {
        val isFree = config.labelPositionMode == LabelPositionMode.FREE
        val cornerPx = if (isFree) config.labelBgCornerDp * density * 0.25f else 0f
        drawGalleryLabel(
            canvas,
            before,
            bLeft,
            bTop,
            config.beforeLabel,
            config,
            if (isFree) config.beforeLabelAnchor else null,
            cornerPx,
        )
        drawGalleryLabel(
            canvas,
            after,
            aLeft,
            aTop,
            config.afterLabel,
            config,
            if (isFree) config.afterLabelAnchor else null,
            cornerPx,
        )
    }

    return combined
}

private fun drawGalleryLabel(
    canvas: android.graphics.Canvas,
    image: Bitmap,
    imgLeft: Int,
    imgTop: Int,
    text: String,
    config: CombineConfig,
    anchor: LabelAnchor? = null,
    cornerPx: Float = 0f,
) {
    val fontSize = (image.height * config.labelSizeRatio).coerceAtLeast(10f)
    val labelHeight = (fontSize * 1.8f).toInt()

    val bgAlpha = (config.labelBgAlpha * 255).toInt().coerceIn(0, 255)
    val bgRed = android.graphics.Color.red(config.labelBgColorArgb)
    val bgGreen = android.graphics.Color.green(config.labelBgColorArgb)
    val bgBlue = android.graphics.Color.blue(config.labelBgColorArgb)
    val bgColor = android.graphics.Color.argb(bgAlpha, bgRed, bgGreen, bgBlue)

    val bgPaint =
        Paint().apply {
            color = bgColor
            style = Paint.Style.FILL
        }
    val textPaint =
        Paint().apply {
            color = config.labelTextColorArgb
            textSize = fontSize
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

    if (anchor == null) {
        // 전체 너비 모드
        val labelTop =
            when (config.labelPosition) {
                LabelPosition.TOP -> imgTop
                LabelPosition.BOTTOM -> imgTop + image.height - labelHeight
            }
        if (config.labelBgEnabled) {
            canvas.drawRect(
                RectF(
                    imgLeft.toFloat(),
                    labelTop.toFloat(),
                    (imgLeft + image.width).toFloat(),
                    (labelTop + labelHeight).toFloat(),
                ),
                bgPaint,
            )
        }
        val textX = imgLeft + image.width / 2f
        val textY = labelTop + labelHeight / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(text, textX, textY, textPaint)
    } else {
        // 자유 위치 모드
        val textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        val hPad = (fontSize * 0.75f).toInt()
        val labelWidth = (textBounds.width() + hPad * 2).coerceAtLeast(labelHeight)
        val margin = (fontSize * 0.4f).toInt()

        val labelLeft =
            when (anchor) {
                LabelAnchor.TOP_LEFT, LabelAnchor.MIDDLE_LEFT, LabelAnchor.BOTTOM_LEFT -> {
                    imgLeft + margin
                }

                LabelAnchor.TOP_CENTER, LabelAnchor.MIDDLE_CENTER, LabelAnchor.BOTTOM_CENTER -> {
                    imgLeft + (image.width - labelWidth) / 2
                }

                LabelAnchor.TOP_RIGHT, LabelAnchor.MIDDLE_RIGHT, LabelAnchor.BOTTOM_RIGHT -> {
                    imgLeft + image.width - labelWidth - margin
                }
            }
        val labelTop =
            when (anchor) {
                LabelAnchor.TOP_LEFT, LabelAnchor.TOP_CENTER, LabelAnchor.TOP_RIGHT -> {
                    imgTop + margin
                }

                LabelAnchor.MIDDLE_LEFT, LabelAnchor.MIDDLE_CENTER, LabelAnchor.MIDDLE_RIGHT -> {
                    imgTop + (image.height - labelHeight) / 2
                }

                LabelAnchor.BOTTOM_LEFT, LabelAnchor.BOTTOM_CENTER, LabelAnchor.BOTTOM_RIGHT -> {
                    imgTop + image.height - labelHeight - margin
                }
            }

        val rf =
            RectF(
                labelLeft.toFloat(),
                labelTop.toFloat(),
                (labelLeft + labelWidth).toFloat(),
                (labelTop + labelHeight).toFloat(),
            )
        if (config.labelBgEnabled) {
            if (cornerPx > 0f) {
                canvas.drawRoundRect(rf, cornerPx, cornerPx, bgPaint)
            } else {
                canvas.drawRect(rf, bgPaint)
            }
        }
        val textX = labelLeft + labelWidth / 2f
        val textY = labelTop + labelHeight / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(text, textX, textY, textPaint)
    }
}

private fun loadBitmapWithExif(
    context: Context,
    uri: Uri,
    inSampleSize: Int,
): Bitmap? {
    val options = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
    val bitmap =
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: return null

    val rotation =
        context.contentResolver.openInputStream(uri)?.use { stream ->
            val exif = ExifInterface(stream)
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f

    if (rotation == 0f) return bitmap

    val matrix = Matrix().apply { postRotate(rotation) }
    val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    if (rotated !== bitmap) bitmap.recycle()
    return rotated
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
