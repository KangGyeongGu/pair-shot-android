package com.pairshot.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.runtime.CompositionLocalProvider
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.pairshot.core.designsystem.PairShotTheme
import com.pairshot.core.model.PairStatus
import com.pairshot.core.model.PhotoPair
import com.pairshot.core.model.SortOrder
import com.pairshot.feature.home.screen.HomeScreen
import com.pairshot.feature.home.viewmodel.HomeMode
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [SDK_LEVEL])
class HomeScreenshotTest {
    @Test fun home_pairs_loaded_pixel7_light() = capture("pairs_loaded_pixel7_light") {
        HomeShell(pairs = samplePairs(8))
    }

    @Test fun home_pairs_empty_pixel7_light() = capture("pairs_empty_pixel7_light") {
        HomeShell(pairs = emptyList())
    }

    @Test fun home_pairs_loaded_pixel7_dark() =
        capture("pairs_loaded_pixel7_dark", darkTheme = true) {
            HomeShell(pairs = samplePairs(8))
        }

    @Test
    @Config(sdk = [SDK_LEVEL], qualifiers = RobolectricDeviceQualifiers.MediumTablet)
    fun home_pairs_loaded_tablet_light() = capture("pairs_loaded_tablet_light") {
        HomeShell(pairs = samplePairs(8))
    }

    @Test
    @Config(sdk = [SDK_LEVEL], qualifiers = RobolectricDeviceQualifiers.Pixel4)
    fun home_pairs_loaded_pixel4_light() = capture("pairs_loaded_pixel4_light") {
        HomeShell(pairs = samplePairs(8))
    }

    @Test
    @Config(sdk = [SDK_LEVEL], qualifiers = PairShotDeviceQualifiers.GalaxyZFlip6Main)
    fun home_pairs_loaded_galaxy_s22() = capture("pairs_loaded_galaxy_s22") {
        HomeShell(pairs = samplePairs(8))
    }

    @Test
    @Config(sdk = [SDK_LEVEL], qualifiers = PairShotDeviceQualifiers.GalaxyZFlip6Main)
    fun home_pairs_loaded_galaxy_flip6_open() = capture("pairs_loaded_galaxy_flip6_open") {
        HomeShell(pairs = samplePairs(8))
    }

    @Test
    @Config(sdk = [SDK_LEVEL], qualifiers = PairShotDeviceQualifiers.GalaxyZFlip6Cover)
    fun home_pairs_loaded_galaxy_flip6_closed() = capture("pairs_loaded_galaxy_flip6_closed") {
        HomeShell(pairs = samplePairs(4))
    }

    @Test
    @Config(sdk = [SDK_LEVEL], qualifiers = PairShotDeviceQualifiers.GalaxyZFold5Main)
    fun home_pairs_loaded_galaxy_fold5_open() = capture("pairs_loaded_galaxy_fold5_open") {
        HomeShell(pairs = samplePairs(8))
    }

    @Test
    @Config(sdk = [SDK_LEVEL], qualifiers = PairShotDeviceQualifiers.GalaxyZFlip6Cover)
    fun home_pairs_loaded_small_phone() = capture("pairs_loaded_small_phone") {
        HomeShell(pairs = samplePairs(4))
    }

    @Test fun home_selection_mode_pixel7_light() = capture("selection_mode_pixel7_light") {
        HomeShell(
            pairs = samplePairs(6),
            selectionMode = true,
            selectedIds = setOf(1L, 3L),
        )
    }

    private fun capture(
        name: String,
        darkTheme: Boolean = false,
        content: @Composable () -> Unit,
    ) {
        captureRoboImage(filePath = "$SNAPSHOT_DIR/Home_$name.png") {
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
private fun HomeShell(
    pairs: List<PhotoPair>,
    selectionMode: Boolean = false,
    selectedIds: Set<Long> = emptySet(),
) {
    HomeScreen(
        mode = HomeMode.PAIRS,
        pairs = pairs,
        albums = emptyList(),
        selectionMode = selectionMode,
        selectedIds = selectedIds,
        albumSelectionMode = false,
        selectedAlbumIds = emptySet(),
        currentLocation = null,
        showCreateAlbumDialog = false,
        sortOrder = SortOrder.DESC,
        onModeSelected = {},
        onToggleSortOrder = {},
        onPairClick = {},
        onPairLongClick = {},
        onAlbumClick = {},
        onAlbumLongPress = {},
        onEnterSelectionMode = {},
        onExitAlbumSelectionMode = {},
        onRenameAlbum = {},
        onDeleteAlbums = {},
        onExitSelectionMode = {},
        onToggleSelectAll = {},
        onShare = {},
        onSaveToDevice = {},
        onDeleteSelected = {},
        onDeleteCombinedOnly = {},
        onExportSettings = {},
        onCreateAlbumClick = {},
        onDismissCreateAlbumDialog = {},
        onConfirmCreateAlbum = { _, _, _, _ -> },
        onFetchLocation = {},
        onNavigateToSettings = {},
        onNavigateToCamera = {},
        isRefreshing = false,
        onRefresh = {},
    )
}

private fun samplePairs(count: Int): List<PhotoPair> =
    List(count) { idx ->
        PhotoPair(
            id = idx.toLong(),
            beforePhotoUri = null,
            afterPhotoUri = null,
            beforeTimestamp = 1700000000000L - idx * 86400000L,
            afterTimestamp = 1700000000000L - idx * 86400000L + 60_000L,
            status = if (idx % 3 == 0) PairStatus.BEFORE_ONLY else PairStatus.PAIRED,
            hasCombined = idx % 2 == 0,
        )
    }
