package com.pairshot.data.local.export

import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import com.pairshot.core.util.ExifBitmapLoader
import com.pairshot.data.local.image.WatermarkManager
import com.pairshot.domain.model.WatermarkConfig
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatermarkedBitmapWriter
    @Inject
    constructor(
        private val exifBitmapLoader: ExifBitmapLoader,
        private val watermarkManager: WatermarkManager,
    ) {
        suspend fun applyWatermarkToFile(
            sourceUri: String,
            destFile: File,
            config: WatermarkConfig,
            jpegQuality: Int,
        ) {
            val bitmap = exifBitmapLoader.loadBitmapWithExifCorrection(Uri.parse(sourceUri))
            val watermarked = watermarkManager.apply(bitmap, config.copy(enabled = true))
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
        ) {
            val enabledConfig = config.copy(enabled = true)
            val beforeBitmap = exifBitmapLoader.loadBitmapWithExifCorrection(Uri.parse(beforeUri))
            val wmBefore = watermarkManager.apply(beforeBitmap, enabledConfig)

            val afterBitmap = exifBitmapLoader.loadBitmapWithExifCorrection(Uri.parse(afterUri))
            val wmAfter = watermarkManager.apply(afterBitmap, enabledConfig)

            val width = wmBefore.width + wmAfter.width
            val height = maxOf(wmBefore.height, wmAfter.height)
            val combined = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(combined)
            canvas.drawBitmap(wmBefore, 0f, 0f, null)
            canvas.drawBitmap(wmAfter, wmBefore.width.toFloat(), 0f, null)

            FileOutputStream(destFile).use { out ->
                combined.compress(Bitmap.CompressFormat.JPEG, jpegQuality, out)
            }

            if (wmBefore !== beforeBitmap) beforeBitmap.recycle()
            wmBefore.recycle()
            if (wmAfter !== afterBitmap) afterBitmap.recycle()
            wmAfter.recycle()
            combined.recycle()
        }
    }
