package com.pairshot.benchmark

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import org.junit.Rule
import org.junit.Test

class StartupBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun startupColdNoCompilation() =
        benchmarkRule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(StartupTimingMetric()),
            iterations = 5,
            startupMode = StartupMode.COLD,
            compilationMode = CompilationMode.None(),
        ) {
            pressHome()
            startActivityAndWait()
        }

    @Test
    fun startupColdBaselineProfile() =
        benchmarkRule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(StartupTimingMetric()),
            iterations = 5,
            startupMode = StartupMode.COLD,
            compilationMode =
                CompilationMode.Partial(
                    baselineProfileMode = BaselineProfileMode.Require,
                ),
        ) {
            pressHome()
            startActivityAndWait()
        }

    @Test
    fun startupWarm() =
        benchmarkRule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(StartupTimingMetric()),
            iterations = 5,
            startupMode = StartupMode.WARM,
        ) {
            pressHome()
            startActivityAndWait()
        }

    @Test
    fun homeFrameTiming() =
        benchmarkRule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(FrameTimingMetric()),
            iterations = 5,
            startupMode = StartupMode.WARM,
        ) {
            pressHome()
            startActivityAndWait()
            device.waitForIdle()
        }

    companion object {
        const val PACKAGE_NAME = "com.pairshot"
    }
}
