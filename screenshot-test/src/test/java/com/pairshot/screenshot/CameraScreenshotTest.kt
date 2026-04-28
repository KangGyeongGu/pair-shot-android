package com.pairshot.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.pairshot.core.designsystem.PairShotTheme
import com.pairshot.core.model.CameraCapabilities
import com.pairshot.feature.camera.component.ZoomUiState
import com.pairshot.feature.camera.screen.CameraScreenCallbacks
import com.pairshot.feature.camera.screen.CameraScreenContent
import com.pairshot.feature.camera.state.CameraSettingsState
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [SDK_LEVEL])
class CameraScreenshotTest {
    @Test
    @Config(sdk = [SDK_LEVEL], qualifiers = RobolectricDeviceQualifiers.Pixel4)
    fun camera_pixel4_light() = capture("pixel4_light") { CameraShell() }

    @Test
    @Config(sdk = [SDK_LEVEL], qualifiers = PairShotDeviceQualifiers.GalaxyZFlip6Main)
    fun camera_galaxy_flip6_open() =
        capture("galaxy_flip6_open") { CameraShell() }

    private fun capture(
        name: String,
        darkTheme: Boolean = false,
        content: @Composable () -> Unit,
    ) {
        captureRoboImage(filePath = "$SNAPSHOT_DIR/Camera_$name.png") {
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
private fun CameraShell(
    settingsOpen: Boolean = false,
    beforePreviewUris: List<String> = emptyList(),
) {
    CameraScreenContent(
        surfaceRequest = null,
        zoomUiState = ZoomUiState(presetRatios = listOf(0.5f, 1f, 2f, 5f)),
        isSaving = false,
        settingsState = CameraSettingsState(showPanel = settingsOpen),
        capabilities = CameraCapabilities(),
        roll = 0f,
        blackoutAlpha = 0f,
        beforePreviewUris = beforePreviewUris,
        lastPairThumbnailUri = null,
        callbacks = NoopCallbacks,
    )
}

private val NoopCallbacks =
    CameraScreenCallbacks(
        onZoomRatioChanged = {},
        onPresetTapped = {},
        onDragEnd = {},
        onExposureReset = {},
        onExposureAdjust = {},
        onTapToFocus = { _, _, _, _ -> },
        onToggleLens = {},
        onToggleSettings = {},
        onShutter = {},
        onThumbnailClick = {},
        onToggleGrid = {},
        onCycleFlash = {},
        onToggleNightMode = {},
        onToggleHdr = {},
        onToggleLevel = {},
        onDismissSettings = {},
    )
