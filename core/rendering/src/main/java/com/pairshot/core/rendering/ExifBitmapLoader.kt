package com.pairshot.core.rendering

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExifBitmapLoader
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun loadBitmapWithExifCorrection(uri: Uri): Bitmap = loadBitmapWithExifCorrection(uri, inSampleSize = 1)

        fun loadBitmapWithExifCorrection(
            uri: Uri,
            inSampleSize: Int,
        ): Bitmap {
            val options =
                BitmapFactory.Options().apply {
                    this.inSampleSize = inSampleSize.coerceAtLeast(1)
                }
            val inputStream =
                context.contentResolver.openInputStream(uri)
                    ?: error("Cannot open input stream for $uri")

            val bitmap =
                inputStream.use { stream ->
                    BitmapFactory.decodeStream(stream, null, options)
                } ?: error("Cannot decode bitmap for $uri")

            val rotation = readExifDegrees(uri)
            if (rotation == 0) return bitmap

            val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotated !== bitmap) bitmap.recycle()
            return rotated
        }

        fun readExifDegrees(uri: Uri): Int {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return 0
            return inputStream.use { stream ->
                val exif = ExifInterface(stream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> ROTATE_QUARTER
                    ExifInterface.ORIENTATION_ROTATE_180 -> ROTATE_HALF
                    ExifInterface.ORIENTATION_ROTATE_270 -> ROTATE_THREE_QUARTERS
                    else -> 0
                }
            }
        }

        companion object {
            private const val ROTATE_QUARTER = 90
            private const val ROTATE_HALF = 180
            private const val ROTATE_THREE_QUARTERS = 270
        }
    }
