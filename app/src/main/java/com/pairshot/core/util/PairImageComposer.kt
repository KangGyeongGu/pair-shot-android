package com.pairshot.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import com.pairshot.core.domain.combine.CombineConfig
import com.pairshot.core.domain.combine.CombineLayout
import com.pairshot.core.domain.combine.LabelAnchor
import com.pairshot.core.domain.combine.LabelPosition
import com.pairshot.core.domain.combine.LabelPositionMode
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PairImageComposer
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val exifBitmapLoader: ExifBitmapLoader,
    ) {
        fun combineSideBySide(
            beforeUri: Uri,
            afterUri: Uri,
        ): Bitmap = combineSideBySide(beforeUri, afterUri, CombineConfig())

        fun combineSideBySide(
            beforeUri: Uri,
            afterUri: Uri,
            config: CombineConfig,
        ): Bitmap {
            val beforeBitmap = exifBitmapLoader.loadBitmapWithExifCorrection(beforeUri)
            val afterBitmap = exifBitmapLoader.loadBitmapWithExifCorrection(afterUri)

            val density = context.resources.displayMetrics.density
            val border = if (config.borderEnabled) (config.borderThicknessDp * density).toInt() else 0

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

            // 1. 테두리 배경 채우기
            if (config.borderEnabled) {
                canvas.drawColor(config.borderColorArgb)
            }

            // 2. 비트맵 배치
            val (beforeLeft, beforeTop, afterLeft, afterTop) =
                when (config.layout) {
                    CombineLayout.HORIZONTAL -> {
                        val bL = border
                        val bT = border
                        val aL = border + beforeBitmap.width + border
                        val aT = border
                        listOf(bL, bT, aL, aT)
                    }

                    CombineLayout.VERTICAL -> {
                        val bL = border
                        val bT = border
                        val aL = border
                        val aT = border + beforeBitmap.height + border
                        listOf(bL, bT, aL, aT)
                    }
                }

            canvas.drawBitmap(beforeBitmap, beforeLeft.toFloat(), beforeTop.toFloat(), null)
            canvas.drawBitmap(afterBitmap, afterLeft.toFloat(), afterTop.toFloat(), null)

            // 3. 레이블 오버레이
            if (config.labelEnabled) {
                val isFree = config.labelPositionMode == LabelPositionMode.FREE
                val cornerPx = if (isFree) config.labelBgCornerDp * density else 0f
                drawLabel(
                    canvas = canvas,
                    text = config.beforeLabel,
                    imageLeft = beforeLeft,
                    imageTop = beforeTop,
                    imageWidth = beforeBitmap.width,
                    imageHeight = beforeBitmap.height,
                    config = config,
                    anchor = if (isFree) config.beforeLabelAnchor else null,
                    cornerPx = cornerPx,
                )
                drawLabel(
                    canvas = canvas,
                    text = config.afterLabel,
                    imageLeft = afterLeft,
                    imageTop = afterTop,
                    imageWidth = afterBitmap.width,
                    imageHeight = afterBitmap.height,
                    config = config,
                    anchor = if (isFree) config.afterLabelAnchor else null,
                    cornerPx = cornerPx,
                )
            }

            beforeBitmap.recycle()
            afterBitmap.recycle()

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
            anchor: LabelAnchor? = null,
            cornerPx: Float = 0f,
        ) {
            val fontSize = imageHeight * config.labelSizeRatio
            val rectHeight = (fontSize * 1.6f).toInt()
            val bgAlpha = (config.labelBgAlpha * 255).toInt().coerceIn(0, 255)

            val bgPaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = config.labelBgColorArgb
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
                // 전체 너비 모드
                val rectTop =
                    when (config.labelPosition) {
                        LabelPosition.TOP -> imageTop
                        LabelPosition.BOTTOM -> imageTop + imageHeight - rectHeight
                    }
                canvas.drawRect(
                    imageLeft.toFloat(),
                    rectTop.toFloat(),
                    (imageLeft + imageWidth).toFloat(),
                    (rectTop + rectHeight).toFloat(),
                    bgPaint,
                )
                val textX = imageLeft + imageWidth / 2f
                val textY = rectTop + rectHeight / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
                canvas.drawText(text, textX, textY, textPaint)
            } else {
                // 자유 위치 모드
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

                if (cornerPx > 0f) {
                    canvas.drawRoundRect(
                        rectLeft.toFloat(),
                        rectTop.toFloat(),
                        (rectLeft + rectWidth).toFloat(),
                        (rectTop + rectHeight).toFloat(),
                        cornerPx,
                        cornerPx,
                        bgPaint,
                    )
                } else {
                    canvas.drawRect(
                        rectLeft.toFloat(),
                        rectTop.toFloat(),
                        (rectLeft + rectWidth).toFloat(),
                        (rectTop + rectHeight).toFloat(),
                        bgPaint,
                    )
                }
                val textX = rectLeft + rectWidth / 2f
                val textY = rectTop + rectHeight / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
                canvas.drawText(text, textX, textY, textPaint)
            }
        }
    }
