package com.pairshot.feature.export.ui.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.pairshot.feature.export.ui.component.ExportActionSection
import com.pairshot.feature.export.ui.component.ExportFormatSection
import com.pairshot.feature.export.ui.component.ExportIncludeSection
import com.pairshot.feature.export.ui.component.ExportProgressSection
import com.pairshot.feature.export.ui.component.ExportWatermarkSection
import com.pairshot.feature.export.ui.viewmodel.ExportFormat
import com.pairshot.ui.theme.PairShotSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    pairCount: Int,
    beforeCount: Int,
    afterCount: Int,
    combinedCount: Int,
    incompleteCount: Int = 0,
    includeBefore: Boolean,
    includeAfter: Boolean,
    includeCombined: Boolean,
    onIncludeBeforeChange: (Boolean) -> Unit,
    onIncludeAfterChange: (Boolean) -> Unit,
    onIncludeCombinedChange: (Boolean) -> Unit,
    exportFormat: ExportFormat,
    onExportFormatChange: (ExportFormat) -> Unit,
    applyWatermark: Boolean = false,
    onApplyWatermarkChange: (Boolean) -> Unit = {},
    onWatermarkSettingsClick: () -> Unit = {},
    onSaveToDevice: () -> Unit,
    onShare: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    isExporting: Boolean = false,
    exportProgress: Float = 0f,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "내보내기") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "뒤로가기",
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            contentPadding =
                PaddingValues(
                    start = PairShotSpacing.screenPadding,
                    end = PairShotSpacing.screenPadding,
                    top = innerPadding.calculateTopPadding() + PairShotSpacing.screenPadding,
                    bottom = innerPadding.calculateBottomPadding() + PairShotSpacing.screenPadding,
                ),
        ) {
            item(key = "selection_summary") {
                Text(
                    text = "${pairCount}개 페어 선택됨",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Spacer(modifier = Modifier.height(PairShotSpacing.iconTextGap))
                Text(
                    text = "${beforeCount}장 Before · ${afterCount}장 After · ${combinedCount}장 합성",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (incompleteCount > 0) {
                    Spacer(modifier = Modifier.height(PairShotSpacing.iconTextGap))
                    Text(
                        text = "⚠ ${incompleteCount}개 페어가 미완성 (After 미촬영)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            item(key = "divider_after_summary") {
                Spacer(modifier = Modifier.height(PairShotSpacing.sectionGap))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(PairShotSpacing.sectionGap))
            }

            item(key = "include_section") {
                ExportIncludeSection(
                    beforeCount = beforeCount,
                    afterCount = afterCount,
                    combinedCount = combinedCount,
                    includeBefore = includeBefore,
                    includeAfter = includeAfter,
                    includeCombined = includeCombined,
                    onIncludeBeforeChange = onIncludeBeforeChange,
                    onIncludeAfterChange = onIncludeAfterChange,
                    onIncludeCombinedChange = onIncludeCombinedChange,
                )
            }

            item(key = "divider_after_include") {
                Spacer(modifier = Modifier.height(PairShotSpacing.sectionGap))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(PairShotSpacing.sectionGap))
            }

            item(key = "format_section") {
                ExportFormatSection(
                    exportFormat = exportFormat,
                    onExportFormatChange = onExportFormatChange,
                )
            }

            item(key = "divider_after_format") {
                Spacer(modifier = Modifier.height(PairShotSpacing.sectionGap))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(PairShotSpacing.sectionGap))
            }

            item(key = "watermark_section") {
                ExportWatermarkSection(
                    applyWatermark = applyWatermark,
                    onApplyWatermarkChange = onApplyWatermarkChange,
                    onWatermarkSettingsClick = onWatermarkSettingsClick,
                )
            }

            item(key = "divider_after_watermark") {
                Spacer(modifier = Modifier.height(PairShotSpacing.sectionGap))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(PairShotSpacing.sectionGap))
            }

            item(key = "destination_section") {
                if (isExporting) {
                    ExportProgressSection(progress = exportProgress)
                } else {
                    ExportActionSection(
                        onSaveToDevice = onSaveToDevice,
                        onShare = onShare,
                    )
                }
            }
        }
    }
}
