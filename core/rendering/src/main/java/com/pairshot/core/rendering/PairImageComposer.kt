package com.pairshot.core.rendering

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import com.pairshot.core.model.CombineConfig
import com.pairshot.core.model.CombineLayout
import com.pairshot.core.model.LabelAnchor
import com.pairshot.core.model.LabelPosition
import com.pairshot.core.model.LabelPositionMode
import com.pairshot.core.model.RenderProfile
import com.pairshot.core.model.WatermarkConfig
import com.pairshot.core.model.effectiveLabelBgColor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PairImageComposer
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val exifBitmapLoader: ExifBitmapLoader,
        private val watermarkRenderer: WatermarkRenderer,
    ) {
        suspend fun compose(
            beforeUri: Uri,
            afterUri: Uri,
            combineConfig: CombineConfig,
            watermarkConfig: WatermarkConfig = WatermarkConfig(),
            profile: RenderProfile = RenderProfile.FULL,
        ): Bitmap =
            withContext(Dispatchers.Default) {
                val before = withContext(Dispatchers.IO) { exifBitmapLoader.loadBitmapWithExifCorrection(beforeUri) }
                val after = withContext(Dispatchers.IO) { exifBitmapLoader.loadBitmapWithExifCorrection(afterUri) }
                try {
                    composeInternal(before, after, combineConfig, watermarkConfig, profile)
                } finally {
                    if (!before.isRecycled) before.recycle()
                    if (!after.isRecycled) after.recycle()
                }
            }

        suspend fun composeFromBitmaps(
            before: Bitmap,
            after: Bitmap,
            combineConfig: CombineConfig,
            watermarkConfig: WatermarkConfig = WatermarkConfig(),
            profile: RenderProfile = RenderProfile.FULL,
        ): Bitmap =
            withContext(Dispatchers.Default) {
                composeInternal(before, after, combineConfig, watermarkConfig, profile, recycleInputs = false)
            }

        suspend fun composeToFile(
            beforeUri: Uri,
            afterUri: Uri,
            destFile: File,
            combineConfig: CombineConfig,
            watermarkConfig: WatermarkConfig,
            jpegQuality: Int,
            profile: RenderProfile = RenderProfile.FULL,
        ) {
            val combined = compose(beforeUri, afterUri, combineConfig, watermarkConfig, profile)
            try {
                withContext(Dispatchers.IO) {
                    FileOutputStream(destFile).use { out ->
                        combined.compress(Bitmap.CompressFormat.JPEG, jpegQuality, out)
                    }
                }
            } finally {
                combined.recycle()
            }
        }

        suspend fun combineSideBySide(
            beforeUri: Uri,
            afterUri: Uri,
        ): Bitmap = combineSideBySide(beforeUri, afterUri, CombineConfig())

        suspend fun combineSideBySide(
            beforeUri: Uri,
            afterUri: Uri,
            config: CombineConfig,
        ): Bitmap = compose(beforeUri, afterUri, config, WatermarkConfig(), RenderProfile.FULL)

        suspend fun combineSideBySideWithWatermark(
            beforeUri: Uri,
            afterUri: Uri,
            config: CombineConfig,
            watermarkConfig: WatermarkConfig,
        ): Bitmap = compose(beforeUri, afterUri, config, watermarkConfig.copy(enabled = true), RenderProfile.FULL)

        private suspend fun composeInternal(
            before: Bitmap,
            after: Bitmap,
            combineConfig: CombineConfig,
            watermarkConfig: WatermarkConfig,
            profile: RenderProfile,
            recycleInputs: Boolean = true,
        ): Bitmap {
            val wmBefore =
                if (watermarkConfig.enabled) watermarkRenderer.apply(before, watermarkConfig) else before
            val wmAfter =
                if (watermarkConfig.enabled) watermarkRenderer.apply(after, watermarkConfig) else after

            if (recycleInputs) {
                if (wmBefore !== before && !before.isRecycled) before.recycle()
                if (wmAfter !== after && !after.isRecycled) after.recycle()
            }

            val density = context.resources.displayMetrics.density
            val border =
                if (combineConfig.borderEnabled) {
                    (combineConfig.borderThicknessDp * density * profile.borderScale).toInt()
                } else {
                    0
                }

            val (width, height) =
                when (combineConfig.layout) {
                    CombineLayout.HORIZONTAL -> {
                        val w = wmBefore.width + wmAfter.width + border * 3
                        val h = maxOf(wmBefore.height, wmAfter.height) + border * 2
                        w to h
                    }

                    CombineLayout.VERTICAL -> {
                        val w = maxOf(wmBefore.width, wmAfter.width) + border * 2
                        val h = wmBefore.height + wmAfter.height + border * 3
                        w to h
                    }
                }

            val combined =
                Bitmap.createBitmap(width.coerceAtLeast(1), height.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(combined)

            if (combineConfig.borderEnabled) {
                canvas.drawColor(combineConfig.borderColorArgb)
            } else {
                canvas.drawColor(android.graphics.Color.BLACK)
            }

            val (beforeLeft, beforeTop, afterLeft, afterTop) =
                when (combineConfig.layout) {
                    CombineLayout.HORIZONTAL -> {
                        listOf(border, border, border + wmBefore.width + border, border)
                    }

                    CombineLayout.VERTICAL -> {
                        listOf(border, border, border, border + wmBefore.height + border)
                    }
                }

            canvas.drawBitmap(wmBefore, beforeLeft.toFloat(), beforeTop.toFloat(), null)
            canvas.drawBitmap(wmAfter, afterLeft.toFloat(), afterTop.toFloat(), null)

            if (combineConfig.labelEnabled) {
                val isFree = combineConfig.labelPositionMode == LabelPositionMode.FREE
                val cornerPx =
                    if (isFree) combineConfig.labelBgCornerDp * density * profile.borderScale else 0f
                drawLabel(
                    canvas = canvas,
                    text = combineConfig.beforeLabel,
                    imageLeft = beforeLeft,
                    imageTop = beforeTop,
                    imageWidth = wmBefore.width,
                    imageHeight = wmBefore.height,
                    config = combineConfig,
                    anchor = if (isFree) combineConfig.beforeLabelAnchor else null,
                    cornerPx = cornerPx,
                )
                drawLabel(
                    canvas = canvas,
                    text = combineConfig.afterLabel,
                    imageLeft = afterLeft,
                    imageTop = afterTop,
                    imageWidth = wmAfter.width,
                    imageHeight = wmAfter.height,
                    config = combineConfig,
                    anchor = if (isFree) combineConfig.afterLabelAnchor else null,
                    cornerPx = cornerPx,
                )
            }

            if (recycleInputs) {
                if (!wmBefore.isRecycled) wmBefore.recycle()
                if (!wmAfter.isRecycled) wmAfter.recycle()
            } else {
                if (wmBefore !== before && !wmBefore.isRecycled) wmBefore.recycle()
                if (wmAfter !== after && !wmAfter.isRecycled) wmAfter.recycle()
            }

            return downscaleIfNeeded(combined, profile.maxOutputPx)
        }

        private fun downscaleIfNeeded(
            source: Bitmap,
            maxOutputPx: Int,
        ): Bitmap {
            if (maxOutputPx <= 0) return source
            val longestSide = maxOf(source.width, source.height)
            if (longestSide <= maxOutputPx) return source
            val ratio = maxOutputPx.toFloat() / longestSide
            val targetW = (source.width * ratio).toInt().coerceAtLeast(1)
            val targetH = (source.height * ratio).toInt().coerceAtLeast(1)
            val scaled = Bitmap.createScaledBitmap(source, targetW, targetH, true)
            if (scaled !== source && !source.isRecycled) source.recycle()
            return scaled
        }

        private fun drawLabel(
            canvas: Canvas,
            text: String,
            imageLeft: Int,
            imageTop: Int,
            imageWidth: Int,
            imageHeight: Int,
            config: CombineConfig,
            anchor: LabelAnchor? = null,
            cornerPx: Float = 0f,
        ) {
            val fontSize = (imageHeight * config.labelSizeRatio).coerceAtLeast(10f)
            val rectHeight = (fontSize * 1.6f).toInt()
            val bgAlpha = (config.labelBgAlpha * 255).toInt().coerceIn(0, 255)

            val bgPaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = config.effectiveLabelBgColor()
                    alpha = bgAlpha
                    style = Paint.Style.FILL
                }
            val textPaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = config.labelTextColorArgb
                    alpha = 255
                    textSize = fontSize
                    textAlign = Paint.Align.CENTER
                }

            if (anchor == null) {
                val rectTop =
                    when (config.labelPosition) {
                        LabelPosition.TOP -> imageTop
                        LabelPosition.BOTTOM -> imageTop + imageHeight - rectHeight
                    }
                if (config.labelBgEnabled) {
                    canvas.drawRect(
                        imageLeft.toFloat(),
                        rectTop.toFloat(),
                        (imageLeft + imageWidth).toFloat(),
                        (rectTop + rectHeight).toFloat(),
                        bgPaint,
                    )
                }
                val textX = imageLeft + imageWidth / 2f
                val textY = rectTop + rectHeight / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
                canvas.drawText(text, textX, textY, textPaint)
            } else {
                val textBounds = Rect()
                textPaint.getTextBounds(text, 0, text.length, textBounds)
                val hPad = (fontSize * 0.75f).toInt()
                val rectWidth = (textBounds.width() + hPad * 2).coerceAtLeast(rectHeight)
                val margin = (fontSize * 0.4f).toInt()

                val rectLeft =
                    when (anchor) {
                        LabelAnchor.TOP_LEFT, LabelAnchor.MIDDLE_LEFT, LabelAnchor.BOTTOM_LEFT -> {
                            imageLeft + margin
                        }

                        LabelAnchor.TOP_CENTER, LabelAnchor.MIDDLE_CENTER, LabelAnchor.BOTTOM_CENTER -> {
                            imageLeft + (imageWidth - rectWidth) / 2
                        }

                        LabelAnchor.TOP_RIGHT, LabelAnchor.MIDDLE_RIGHT, LabelAnchor.BOTTOM_RIGHT -> {
                            imageLeft + imageWidth - rectWidth - margin
                        }
                    }
                val rectTop =
                    when (anchor) {
                        LabelAnchor.TOP_LEFT, LabelAnchor.TOP_CENTER, LabelAnchor.TOP_RIGHT -> {
                            imageTop + margin
                        }

                        LabelAnchor.MIDDLE_LEFT, LabelAnchor.MIDDLE_CENTER, LabelAnchor.MIDDLE_RIGHT -> {
                            imageTop + (imageHeight - rectHeight) / 2
                        }

                        LabelAnchor.BOTTOM_LEFT, LabelAnchor.BOTTOM_CENTER, LabelAnchor.BOTTOM_RIGHT -> {
                            imageTop + imageHeight - rectHeight - margin
                        }
                    }

                if (config.labelBgEnabled) {
                    val rf =
                        android.graphics.RectF(
                            rectLeft.toFloat(),
                            rectTop.toFloat(),
                            (rectLeft + rectWidth).toFloat(),
                            (rectTop + rectHeight).toFloat(),
                        )
                    if (cornerPx > 0f) {
                        canvas.drawRoundRect(rf, cornerPx, cornerPx, bgPaint)
                    } else {
                        canvas.drawRect(rf, bgPaint)
                    }
                }
                val textX = rectLeft + rectWidth / 2f
                val textY = rectTop + rectHeight / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
                canvas.drawText(text, textX, textY, textPaint)
            }
        }
    }
