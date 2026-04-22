package com.pairshot.feature.exportsettings.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pairshot.core.designsystem.PairShotSpacing
import com.pairshot.core.model.ExportFormat
import com.pairshot.core.ui.component.SettingsSectionLabel
import com.pairshot.feature.exportsettings.component.ExportCombineSection
import com.pairshot.feature.exportsettings.component.ExportFormatSection
import com.pairshot.feature.exportsettings.component.ExportIncludeSection
import com.pairshot.feature.exportsettings.component.ExportWatermarkSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportSettingsScreen(
    includeBefore: Boolean,
    includeAfter: Boolean,
    includeCombined: Boolean,
    format: ExportFormat,
    applyWatermark: Boolean,
    applyCombineConfig: Boolean,
    onIncludeBeforeChange: (Boolean) -> Unit,
    onIncludeAfterChange: (Boolean) -> Unit,
    onIncludeCombinedChange: (Boolean) -> Unit,
    onFormatChange: (ExportFormat) -> Unit,
    onApplyWatermarkChange: (Boolean) -> Unit,
    onApplyCombineConfigChange: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToWatermarkSettings: () -> Unit,
    onNavigateToCombineSettings: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "내보내기 설정",
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
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
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            contentPadding =
                PaddingValues(
                    horizontal = PairShotSpacing.screenPadding,
                    vertical = PairShotSpacing.cardPadding,
                ),
        ) {
            item(key = "label_include") {
                SettingsSectionLabel(label = "포함 항목")
                Spacer(modifier = Modifier.height(PairShotSpacing.iconTextGap))
            }
            item(key = "section_include") {
                ExportIncludeSection(
                    includeBefore = includeBefore,
                    includeAfter = includeAfter,
                    includeCombined = includeCombined,
                    onIncludeBeforeChange = onIncludeBeforeChange,
                    onIncludeAfterChange = onIncludeAfterChange,
                    onIncludeCombinedChange = onIncludeCombinedChange,
                )
            }

            item(key = "label_format") {
                Spacer(modifier = Modifier.height(PairShotSpacing.sectionGap))
                SettingsSectionLabel(label = "저장 형식")
                Spacer(modifier = Modifier.height(PairShotSpacing.iconTextGap))
            }
            item(key = "section_format") {
                ExportFormatSection(
                    format = format,
                    onFormatChange = onFormatChange,
                )
            }

            item(key = "label_watermark") {
                Spacer(modifier = Modifier.height(PairShotSpacing.sectionGap))
                SettingsSectionLabel(label = "워터마크")
                Spacer(modifier = Modifier.height(PairShotSpacing.iconTextGap))
            }
            item(key = "section_watermark") {
                ExportWatermarkSection(
                    applyWatermark = applyWatermark,
                    onApplyWatermarkChange = onApplyWatermarkChange,
                    onNavigateToWatermarkSettings = onNavigateToWatermarkSettings,
                )
            }

            item(key = "label_combine") {
                Spacer(modifier = Modifier.height(PairShotSpacing.sectionGap))
                SettingsSectionLabel(label = "합성")
                Spacer(modifier = Modifier.height(PairShotSpacing.iconTextGap))
            }
            item(key = "section_combine") {
                ExportCombineSection(
                    applyCombineConfig = applyCombineConfig,
                    onApplyCombineConfigChange = onApplyCombineConfigChange,
                    onNavigateToCombineSettings = onNavigateToCombineSettings,
                )
            }

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(PairShotSpacing.sectionGap))
            }
        }
    }
}
