package com.pairshot.ui.export

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "내보내기") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기",
                        )
                    }
                },
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
            // 선택 요약
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

            // 포함할 항목 섹션
            item(key = "include_section") {
                Text(
                    text = "포함할 항목",
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(PairShotSpacing.itemGap))
                Column {
                    CheckboxRow(
                        label = "Before 원본 (${beforeCount}장)",
                        checked = includeBefore,
                        onCheckedChange = onIncludeBeforeChange,
                        enabled = beforeCount > 0,
                    )
                    CheckboxRow(
                        label = "After 원본 (${afterCount}장)",
                        checked = includeAfter,
                        onCheckedChange = onIncludeAfterChange,
                        enabled = afterCount > 0,
                    )
                    CheckboxRow(
                        label = "합성 이미지 (${combinedCount}장)",
                        checked = includeCombined,
                        onCheckedChange = onIncludeCombinedChange,
                        enabled = combinedCount > 0,
                    )
                }
            }

            item(key = "divider_after_include") {
                Spacer(modifier = Modifier.height(PairShotSpacing.sectionGap))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(PairShotSpacing.sectionGap))
            }

            // 내보내기 형식 섹션
            item(key = "format_section") {
                Text(
                    text = "내보내기 형식",
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(PairShotSpacing.itemGap))
                Column {
                    RadioButtonRow(
                        label = "이미지 개별",
                        selected = exportFormat == ExportFormat.INDIVIDUAL,
                        onClick = { onExportFormatChange(ExportFormat.INDIVIDUAL) },
                    )
                    RadioButtonRow(
                        label = "ZIP 압축",
                        selected = exportFormat == ExportFormat.ZIP,
                        onClick = { onExportFormatChange(ExportFormat.ZIP) },
                    )
                }
            }

            item(key = "divider_after_format") {
                Spacer(modifier = Modifier.height(PairShotSpacing.sectionGap))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(PairShotSpacing.sectionGap))
            }

            // 목적지 섹션
            item(key = "destination_section") {
                Text(
                    text = "내보내기 방식",
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(PairShotSpacing.itemGap))
                if (isExporting) {
                    ExportingIndicator(progress = exportProgress)
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(PairShotSpacing.itemGap),
                    ) {
                        ExportActionCard(
                            icon = Icons.Default.Save,
                            label = "기기에 저장",
                            onClick = onSaveToDevice,
                        )
                        ExportActionCard(
                            icon = Icons.Default.Share,
                            label = "공유",
                            onClick = onShare,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckboxRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = PairShotSpacing.cardPadding / 2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
        Spacer(modifier = Modifier.width(PairShotSpacing.iconTextGap))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color =
                if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
        )
    }
}

@Composable
private fun RadioButtonRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = PairShotSpacing.cardPadding / 2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(PairShotSpacing.iconTextGap))
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ExportActionCard(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = onClick,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(PairShotSpacing.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(PairShotSpacing.iconSize),
                tint = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.width(PairShotSpacing.iconTextGap))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ExportingIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PairShotSpacing.itemGap),
    ) {
        if (progress <= 0f) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        } else {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Text(
            text =
                if (progress <= 0f) {
                    "내보내는 중..."
                } else {
                    "내보내는 중... ${(progress * 100).toInt()}%"
                },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
