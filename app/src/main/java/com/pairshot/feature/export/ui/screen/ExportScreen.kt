package com.pairshot.feature.export.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pairshot.core.designsystem.PairShotSpacing
import com.pairshot.core.ui.component.PairShotSnackbar
import com.pairshot.core.ui.component.PairShotSnackbarController
import com.pairshot.core.ui.component.SettingsSectionLabel
import com.pairshot.core.ui.component.TopProgressPill
import com.pairshot.feature.export.ui.component.ExportActionSection
import com.pairshot.feature.export.ui.component.ExportCombineSection
import com.pairshot.feature.export.ui.component.ExportFormatSection
import com.pairshot.feature.export.ui.component.ExportIncludeSection
import com.pairshot.feature.export.ui.component.ExportWatermarkSection
import com.pairshot.feature.export.ui.viewmodel.ExportFormat
import kotlin.math.roundToInt

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
    applyCombineConfig: Boolean = false,
    onApplyCombineConfigChange: (Boolean) -> Unit = {},
    onCombineSettingsClick: () -> Unit = {},
    onSaveToDevice: () -> Unit,
    onShare: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    isExporting: Boolean = false,
    exportProgress: Float = 0f,
    snackbarController: PairShotSnackbarController = remember { PairShotSnackbarController() },
) {
    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                ExportActionSection(
                    onSaveToDevice = onSaveToDevice,
                    onShare = onShare,
                    enabled = !isExporting,
                )
            },
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(text = "내보내기") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "뒤로가기",
                            )
                        }
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
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

                item(key = "include_section") {
                    Spacer(modifier = Modifier.height(PairShotSpacing.sectionGap))
                    SettingsSectionLabel(label = "포함할 항목")
                    Spacer(modifier = Modifier.height(PairShotSpacing.iconTextGap))
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

                item(key = "format_section") {
                    Spacer(modifier = Modifier.height(PairShotSpacing.sectionGap))
                    SettingsSectionLabel(label = "내보내기 형식")
                    Spacer(modifier = Modifier.height(PairShotSpacing.iconTextGap))
                    ExportFormatSection(
                        exportFormat = exportFormat,
                        onExportFormatChange = onExportFormatChange,
                    )
                }

                item(key = "watermark_section") {
                    Spacer(modifier = Modifier.height(PairShotSpacing.sectionGap))
                    SettingsSectionLabel(label = "워터마크")
                    Spacer(modifier = Modifier.height(PairShotSpacing.iconTextGap))
                    ExportWatermarkSection(
                        applyWatermark = applyWatermark,
                        onApplyWatermarkChange = onApplyWatermarkChange,
                        onWatermarkSettingsClick = onWatermarkSettingsClick,
                    )
                }

                item(key = "combine_section") {
                    Spacer(modifier = Modifier.height(PairShotSpacing.sectionGap))
                    SettingsSectionLabel(label = "합성 옵션")
                    Spacer(modifier = Modifier.height(PairShotSpacing.iconTextGap))
                    ExportCombineSection(
                        applyCombineConfig = applyCombineConfig,
                        onApplyCombineConfigChange = onApplyCombineConfigChange,
                        onCombineSettingsClick = onCombineSettingsClick,
                    )
                }
            }
        }

        if (isExporting) {
            TopProgressPill(
                label = "내보내는 중...",
                progress = exportProgress,
                progressText = if (exportProgress > 0f) "${(exportProgress * 100).roundToInt()}%" else "",
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 8.dp),
            )
        }

        SnackbarHost(
            hostState = snackbarController.hostState,
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 8.dp),
            snackbar = { data ->
                PairShotSnackbar(
                    message = data.visuals.message,
                    variant = snackbarController.currentVariant,
                    actionLabel = data.visuals.actionLabel,
                    onAction = { data.performAction() },
                )
            },
        )
    }
}
