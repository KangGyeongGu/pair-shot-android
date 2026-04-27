package com.pairshot.microbenchmark.rendering

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.pairshot.core.model.CombineConfig
import com.pairshot.core.model.RenderProfile
import com.pairshot.core.model.WatermarkConfig
import com.pairshot.core.rendering.ExifBitmapLoader
import com.pairshot.core.rendering.PairImageComposer
import com.pairshot.core.rendering.WatermarkRenderer
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PairImageComposerBenchmark {
    @get:Rule
    val rule = BenchmarkRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val composer =
        PairImageComposer(
            context = context,
            exifBitmapLoader = ExifBitmapLoader(context),
            watermarkRenderer = WatermarkRenderer(),
        )

    @Test
    fun composePreview_4MP() = bench(width = 2272, height = 1704, profile = RenderProfile.PREVIEW)

    @Test
    fun composePreview_8MP() = bench(width = 3264, height = 2448, profile = RenderProfile.PREVIEW)

    @Test
    fun composePreview_16MP() = bench(width = 4608, height = 3456, profile = RenderProfile.PREVIEW)

    @Test
    fun composeFull_4MP() = bench(width = 2272, height = 1704, profile = RenderProfile.FULL)

    private fun bench(
        width: Int,
        height: Int,
        profile: RenderProfile,
    ) {
        rule.measureRepeated {
            val before = runWithTimingDisabled { createSyntheticBitmap(width, height, Color.BLUE) }
            val after = runWithTimingDisabled { createSyntheticBitmap(width, height, Color.GREEN) }
            runBlocking {
                composer.composeFromBitmaps(
                    before = before,
                    after = after,
                    combineConfig = CombineConfig(),
                    watermarkConfig = WatermarkConfig(),
                    profile = profile,
                )
            }
            runWithTimingDisabled {
                if (!before.isRecycled) before.recycle()
                if (!after.isRecycled) after.recycle()
            }
        }
    }

    private fun createSyntheticBitmap(
        width: Int,
        height: Int,
        color: Int,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).drawColor(color)
        return bitmap
    }
}
