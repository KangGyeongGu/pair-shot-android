package com.pairshot.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageUtils
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        /**
         * URI에서 비트맵을 로드하고 EXIF 회전을 보정.
         */
        fun loadBitmapWithExifCorrection(uri: Uri): Bitmap {
            val inputStream =
                context.contentResolver.openInputStream(uri)
                    ?: throw IllegalStateException("Cannot open input stream for $uri")

            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            val rotation = getExifRotation(uri)
            if (rotation == 0) return bitmap

            val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotated != bitmap) bitmap.recycle()
            return rotated
        }

        private fun getExifRotation(uri: Uri): Int {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return 0
            val exif = ExifInterface(inputStream)
            inputStream.close()

            return when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        }

        /**
         * Before/After 이미지를 좌우로 합성 (이중 JPEG 압축 방지를 위해 메모리 내 처리).
         */
        fun combineSideBySide(
            beforeUri: Uri,
            afterUri: Uri,
        ): Bitmap {
            val beforeBitmap = loadBitmapWithExifCorrection(beforeUri)
            val afterBitmap = loadBitmapWithExifCorrection(afterUri)

            val width = beforeBitmap.width + afterBitmap.width
            val height = maxOf(beforeBitmap.height, afterBitmap.height)

            val combined = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(combined)
            canvas.drawBitmap(beforeBitmap, 0f, 0f, null)
            canvas.drawBitmap(afterBitmap, beforeBitmap.width.toFloat(), 0f, null)

            beforeBitmap.recycle()
            afterBitmap.recycle()

            return combined
        }
    }
