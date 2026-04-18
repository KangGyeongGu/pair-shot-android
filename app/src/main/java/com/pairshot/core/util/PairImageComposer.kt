package com.pairshot.core.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PairImageComposer
    @Inject
    constructor(
        private val exifBitmapLoader: ExifBitmapLoader,
    ) {
        fun combineSideBySide(
            beforeUri: Uri,
            afterUri: Uri,
        ): Bitmap {
            val beforeBitmap = exifBitmapLoader.loadBitmapWithExifCorrection(beforeUri)
            val afterBitmap = exifBitmapLoader.loadBitmapWithExifCorrection(afterUri)

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
