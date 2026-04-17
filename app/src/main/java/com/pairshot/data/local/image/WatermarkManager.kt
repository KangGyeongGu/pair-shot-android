package com.pairshot.data.local.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.pairshot.domain.model.LogoPosition
import com.pairshot.domain.model.WatermarkConfig
import com.pairshot.domain.model.WatermarkType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

@Singleton
class WatermarkManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        /**
         * 텍스트 워터마크 적용.
         * - 원본 Bitmap을 copy() 후 Canvas에 그림
         * - 45도 대각선으로 텍스트를 반복 타일링
         * - alpha, diagonalCount, repeatDensity, textSizeRatio 파라미터 반영
         */
        suspend fun applyTextWatermark(
            source: Bitmap,
            config: WatermarkConfig,
        ): Bitmap =
            withContext(Dispatchers.Default) {
                val result = source.copy(Bitmap.Config.ARGB_8888, true)
                if (config.text.isBlank()) return@withContext result
                val canvas = Canvas(result)

                val paint =
                    Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.WHITE
                        alpha = (config.alpha * 255).toInt().coerceIn(0, 255)
                        textSize = source.width * config.textSizeRatio
                    }

                val width = result.width.toFloat()
                val height = result.height.toFloat()
                val centerX = width / 2f
                val centerY = height / 2f

                // 대각선 길이 — 회전 후 전체 이미지를 덮으려면 대각선 길이가 필요
                val diagonal = sqrt(width * width + height * height)

                // 줄 간격: 대각선 전체를 diagonalCount 줄로 나눔
                val lineSpacing = diagonal / config.diagonalCount

                // 같은 줄 내 텍스트 간격: 텍스트 폭 + 여백
                val textWidth = paint.measureText(config.text)
                val textSpacing = (textWidth * (2f / config.repeatDensity)).coerceAtLeast(textWidth + 16f)

                canvas.save()
                canvas.rotate(-45f, centerX, centerY)

                // 회전된 좌표계에서 이미지 전체를 덮도록 타일링
                // y 범위: -diagonal/2 ~ diagonal/2 (centerY 기준)
                val startY = centerY - diagonal
                val endY = centerY + diagonal
                val startX = centerX - diagonal
                val endX = centerX + diagonal

                var y = startY
                while (y <= endY) {
                    var x = startX
                    while (x <= endX) {
                        canvas.drawText(config.text, x, y, paint)
                        x += textSpacing
                    }
                    y += lineSpacing
                }

                canvas.restore()
                result
            }

        /**
         * 로고 워터마크 적용.
         * - 원본 Bitmap을 copy() 후 Canvas에 그림
         * - logoPath에서 PNG 로드 → logoSizeRatio로 리사이즈
         * - logoPosition에 따라 배치 (9-point grid)
         * - logoAlpha 적용
         */
        suspend fun applyLogoWatermark(
            source: Bitmap,
            config: WatermarkConfig,
        ): Bitmap =
            withContext(Dispatchers.Default) {
                val result = source.copy(Bitmap.Config.ARGB_8888, true)
                if (config.logoPath.isBlank()) return@withContext result

                val rawLogo =
                    runCatching { BitmapFactory.decodeFile(config.logoPath) }.getOrNull()
                        ?: return@withContext result
                val canvas = Canvas(result)

                val targetSize = (source.width * config.logoSizeRatio).toInt().coerceAtLeast(1)
                val logoAspect = rawLogo.height.toFloat() / rawLogo.width.toFloat()
                val logoWidth = targetSize
                val logoHeight = (targetSize * logoAspect).toInt().coerceAtLeast(1)

                val resizedLogo =
                    runCatching {
                        Bitmap.createScaledBitmap(rawLogo, logoWidth, logoHeight, true)
                    }.getOrNull()

                rawLogo.recycle()

                if (resizedLogo == null) {
                    result.recycle()
                    return@withContext source
                }

                val padding = (source.width * 0.02f).toInt()
                val imgWidth = result.width
                val imgHeight = result.height

                val (x, y) =
                    when (config.logoPosition) {
                        LogoPosition.TOP_LEFT -> {
                            padding.toFloat() to padding.toFloat()
                        }

                        LogoPosition.TOP_CENTER -> {
                            ((imgWidth - logoWidth) / 2f) to padding.toFloat()
                        }

                        LogoPosition.TOP_RIGHT -> {
                            (imgWidth - logoWidth - padding).toFloat() to padding.toFloat()
                        }

                        LogoPosition.CENTER_LEFT -> {
                            padding.toFloat() to ((imgHeight - logoHeight) / 2f)
                        }

                        LogoPosition.CENTER -> {
                            ((imgWidth - logoWidth) / 2f) to ((imgHeight - logoHeight) / 2f)
                        }

                        LogoPosition.CENTER_RIGHT -> {
                            (imgWidth - logoWidth - padding).toFloat() to ((imgHeight - logoHeight) / 2f)
                        }

                        LogoPosition.BOTTOM_LEFT -> {
                            padding.toFloat() to (imgHeight - logoHeight - padding).toFloat()
                        }

                        LogoPosition.BOTTOM_CENTER -> {
                            ((imgWidth - logoWidth) / 2f) to (imgHeight - logoHeight - padding).toFloat()
                        }

                        LogoPosition.BOTTOM_RIGHT -> {
                            (imgWidth - logoWidth - padding).toFloat() to (imgHeight - logoHeight - padding).toFloat()
                        }
                    }

                val paint =
                    Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        alpha = (config.logoAlpha * 255).toInt().coerceIn(0, 255)
                    }

                canvas.drawBitmap(resizedLogo, x, y, paint)
                resizedLogo.recycle()

                result
            }

        /**
         * config.type에 따라 적절한 워터마크 적용.
         */
        suspend fun apply(
            source: Bitmap,
            config: WatermarkConfig,
        ): Bitmap {
            if (!config.enabled) return source
            return when (config.type) {
                WatermarkType.TEXT -> applyTextWatermark(source, config)
                WatermarkType.LOGO -> applyLogoWatermark(source, config)
            }
        }
    }
