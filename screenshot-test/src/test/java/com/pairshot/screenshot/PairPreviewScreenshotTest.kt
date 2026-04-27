package com.pairshot.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.pairshot.core.designsystem.PairShotTheme
import com.pairshot.core.model.PairStatus
import com.pairshot.feature.pairpreview.screen.PairPreviewScreen
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [SDK_LEVEL])
class PairPreviewScreenshotTest {
    @Test fun preview_paired_loading() = capture("paired_loading") {
        Shell(status = PairStatus.PAIRED, hasCombined = false, failed = false)
    }

    @Test fun preview_paired_failed() = capture("paired_failed") {
        Shell(status = PairStatus.PAIRED, hasCombined = false, failed = true)
    }

    @Test fun preview_after_only_pixel7() = capture("after_only_pixel7") {
        Shell(status = PairStatus.AFTER_ONLY)
    }

    @Test fun preview_before_only_pixel7() = capture("before_only_pixel7") {
        Shell(status = PairStatus.BEFORE_ONLY)
    }

    @Test fun preview_paired_dark() =
        capture("paired_dark", darkTheme = true) {
            Shell(status = PairStatus.PAIRED, hasCombined = true, failed = false)
        }

    @Test
    @Config(sdk = [SDK_LEVEL], qualifiers = RobolectricDeviceQualifiers.MediumTablet)
    fun preview_paired_tablet() = capture("paired_tablet") {
        Shell(status = PairStatus.PAIRED, hasCombined = true, failed = false)
    }

    @Test
    @Config(sdk = [SDK_LEVEL], qualifiers = RobolectricDeviceQualifiers.Pixel4)
    fun preview_after_only_pixel4() = capture("after_only_pixel4") {
        Shell(status = PairStatus.AFTER_ONLY)
    }

    @Test
    @Config(sdk = [SDK_LEVEL], qualifiers = PairShotDeviceQualifiers.GalaxyZFlip6Main)
    fun preview_paired_galaxy_flip6_open() = capture("paired_galaxy_flip6_open") {
        Shell(status = PairStatus.PAIRED, hasCombined = true, failed = false)
    }

    @Test
    @Config(sdk = [SDK_LEVEL], qualifiers = PairShotDeviceQualifiers.GalaxyZFold5Main)
    fun preview_after_only_galaxy_fold5_open() =
        capture("after_only_galaxy_fold5_open") {
            Shell(status = PairStatus.AFTER_ONLY)
        }

    @Test
    @Config(sdk = [SDK_LEVEL], qualifiers = PairShotDeviceQualifiers.GalaxyZFlip6Cover)
    fun preview_paired_small_phone() = capture("paired_small_phone") {
        Shell(status = PairStatus.PAIRED, hasCombined = false, failed = false)
    }

    private fun capture(
        name: String,
        darkTheme: Boolean = false,
        content: @Composable () -> Unit,
    ) {
        captureRoboImage(filePath = "$SNAPSHOT_DIR/PairPreview_$name.png") {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                PairShotTheme(darkTheme = darkTheme) {
                    content()
                }
            }
        }
    }
}

private const val SDK_LEVEL = 34
private const val SNAPSHOT_DIR = "src/test/snapshots"

@Composable
private fun Shell(
    status: PairStatus,
    hasCombined: Boolean = false,
    failed: Boolean = false,
) {
    PairPreviewScreen(
        hasCombined = hasCombined,
        pairStatus = status,
        livePreviewBitmap = null,
        livePreviewFailed = failed,
        onLivePreviewRetry = {},
        showDeleteDialog = false,
        onClose = {},
        onShareSelected = {},
        onNavigateToAfterCamera = {},
        onNavigateToBeforeRetake = {},
        onDeleteRequested = {},
        onDeleteAll = {},
        onDeleteCombinedOnly = {},
        onDeleteDismissed = {},
    )
}
