package com.pairshot.core.data.export

import android.graphics.Bitmap
import android.net.Uri
import com.pairshot.core.model.CombineConfig
import com.pairshot.core.model.RenderProfile
import com.pairshot.core.model.WatermarkConfig
import com.pairshot.core.rendering.ExifBitmapLoader
import com.pairshot.core.rendering.PairImageComposer
import com.pairshot.core.rendering.WatermarkRenderer
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
        private val pairImageComposer: PairImageComposer,
    ) {
        suspend fun applyWatermarkToFile(
            sourceUri: String,
            destFile: File,
            config: WatermarkConfig,
            jpegQuality: Int,
        ) {
            val bitmap = exifBitmapLoader.loadBitmapWithExifCorrection(Uri.parse(sourceUri))
            val watermarked =
                try {
                    watermarkRenderer.apply(bitmap, config.copy(enabled = true))
                } catch (e: IllegalArgumentException) {
                    if (!bitmap.isRecycled) bitmap.recycle()
                    throw e
                } catch (e: IllegalStateException) {
                    if (!bitmap.isRecycled) bitmap.recycle()
                    throw e
                }
            try {
                FileOutputStream(destFile).use { out ->
                    watermarked.compress(Bitmap.CompressFormat.JPEG, jpegQuality, out)
                }
            } finally {
                if (watermarked !== bitmap && !bitmap.isRecycled) bitmap.recycle()
                if (!watermarked.isRecycled) watermarked.recycle()
            }
        }

        suspend fun combineWithWatermark(
            beforeUri: String,
            afterUri: String,
            destFile: File,
            config: WatermarkConfig,
            jpegQuality: Int,
            combineConfig: CombineConfig = CombineConfig(),
        ) {
            pairImageComposer.composeToFile(
                beforeUri = Uri.parse(beforeUri),
                afterUri = Uri.parse(afterUri),
                destFile = destFile,
                combineConfig = combineConfig,
                watermarkConfig = config.copy(enabled = true),
                jpegQuality = jpegQuality,
                profile = RenderProfile.FULL,
            )
        }
    }
