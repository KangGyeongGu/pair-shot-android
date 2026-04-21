package com.pairshot.core.rendering

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreviewSampleProvider
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        @Volatile
        private var cached: Bitmap? = null

        suspend fun get(): Bitmap =
            withContext(Dispatchers.IO) {
                cached?.takeIf { !it.isRecycled } ?: loadSample().also { cached = it }
            }

        private fun loadSample(): Bitmap {
            val resources = context.resources
            val resId =
                resources.getIdentifier(
                    "watermark_preview_sample",
                    "drawable",
                    context.packageName,
                )
            if (resId == 0) {
                throw IllegalStateException("watermark_preview_sample drawable not found")
            }
            return BitmapFactory.decodeResource(resources, resId)
                ?: throw IllegalStateException("Failed to decode watermark_preview_sample")
        }
    }
