package com.pairshot.core.data.export

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import com.pairshot.core.model.CombineConfig
import com.pairshot.core.model.CombineLayout
import com.pairshot.core.model.LabelPosition
import com.pairshot.core.model.WatermarkConfig
import com.pairshot.core.rendering.WatermarkRenderer
import com.pairshot.core.rendering.ExifBitmapLoader
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatermarkedBitmapWriter
    @Inject
    constructor(
        private val exifBitmapLoader: ExifBitmapLoader,
        private val watermarkRenderer: WatermarkRenderer,
    ) {
        suspend fun applyWatermarkToFile(
            sourceUri: String,
            destFile: File,
            config: WatermarkConfig,
            jpegQuality: Int,
        ) {
            val bitmap = exifBitmapLoader.loadBitmapWithExifCorrection(Uri.parse(sourceUri))
            val watermarked = watermarkRenderer.apply(bitmap, config.copy(enabled = true))
            FileOutputStream(destFile).use { out ->
                watermarked.compress(Bitmap.CompressFormat.JPEG, jpegQuality, out)
            }
            if (watermarked !== bitmap) bitmap.recycle()
            watermarked.recycle()
        }

        suspend fun combineWithWatermark(
            beforeUri: String,
            afterUri: String,
            destFile: File,
            config: WatermarkConfig,
            jpegQuality: Int,
            combineConfig: CombineConfig = CombineConfig(),
        ) {
            val enabledConfig = config.copy(enabled = true)
            val beforeBitmap = exifBitmapLoader.loadBitmapWithExifCorrection(Uri.parse(beforeUri))
            val wmBefore = watermarkRenderer.apply(beforeBitmap, enabledConfig)

            val afterBitmap = exifBitmapLoader.loadBitmapWithExifCorrection(Uri.parse(afterUri))
            val wmAfter = watermarkRenderer.apply(afterBitmap, enabledConfig)

            val combined = buildCombinedBitmap(wmBefore, wmAfter, combineConfig)

            FileOutputStream(destFile).use { out ->
                combined.compress(Bitmap.CompressFormat.JPEG, jpegQuality, out)
            }

            if (wmBefore !== beforeBitmap) beforeBitmap.recycle()
            wmBefore.recycle()
            if (wmAfter !== afterBitmap) afterBitmap.recycle()
            wmAfter.recycle()
            combined.recycle()
        }

        private fun buildCombinedBitmap(
            beforeBitmap: Bitmap,
            afterBitmap: Bitmap,
            config: CombineConfig,
        ): Bitmap {
            // 3px/dp 근사 — WatermarkedBitmapWriter는 Context를 주입받지 않으므로 고정 근사값 사용
            val border = if (config.borderEnabled) config.borderThicknessDp * 3 else 0

            val (width, height) =
                when (config.layout) {
                    CombineLayout.HORIZONTAL -> {
                        val w = beforeBitmap.width + afterBitmap.width + border * 3
                        val h = maxOf(beforeBitmap.height, afterBitmap.height) + border * 2
                        w to h
                    }

                    CombineLayout.VERTICAL -> {
                        val w = maxOf(beforeBitmap.width, afterBitmap.width) + border * 2
                        val h = beforeBitmap.height + afterBitmap.height + border * 3
                        w to h
                    }
                }

            val combined = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(combined)

            if (config.borderEnabled) {
                canvas.drawColor(config.borderColorArgb)
            }

            val (beforeLeft, beforeTop, afterLeft, afterTop) =
                when (config.layout) {
                    CombineLayout.HORIZONTAL -> listOf(border, border, border + beforeBitmap.width + border, border)
                    CombineLayout.VERTICAL -> listOf(border, border, border, border + beforeBitmap.height + border)
                }

            canvas.drawBitmap(beforeBitmap, beforeLeft.toFloat(), beforeTop.toFloat(), null)
            canvas.drawBitmap(afterBitmap, afterLeft.toFloat(), afterTop.toFloat(), null)

            if (config.labelEnabled) {
                drawLabel(canvas, config.beforeLabel, beforeLeft, beforeTop, beforeBitmap.width, beforeBitmap.height, config)
                drawLabel(canvas, config.afterLabel, afterLeft, afterTop, afterBitmap.width, afterBitmap.height, config)
            }

            return combined
        }

        private fun drawLabel(
            canvas: Canvas,
            text: String,
            imageLeft: Int,
            imageTop: Int,
            imageWidth: Int,
            imageHeight: Int,
            config: CombineConfig,
        ) {
            val fontSize = imageHeight * config.labelSizeRatio
            val rectHeight = (fontSize * 1.6f).toInt()
            val rectTop =
                when (config.labelPosition) {
                    LabelPosition.TOP -> imageTop
                    LabelPosition.BOTTOM -> imageTop + imageHeight - rectHeight
                }

            val bgPaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = config.labelBgColorArgb
                    alpha = (config.labelBgAlpha * 255).toInt().coerceIn(0, 255)
                    style = Paint.Style.FILL
                }
            canvas.drawRect(
                imageLeft.toFloat(),
                rectTop.toFloat(),
                (imageLeft + imageWidth).toFloat(),
                (rectTop + rectHeight).toFloat(),
                bgPaint,
            )

            val textPaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = config.labelTextColorArgb
                    alpha = 255
                    textSize = fontSize
                    textAlign = Paint.Align.CENTER
                }
            val textX = imageLeft + imageWidth / 2f
            val textY = rectTop + rectHeight / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(text, textX, textY, textPaint)
        }
    }
