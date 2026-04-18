package com.pairshot.feature.settings.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pairshot.core.designsystem.PairShotSpacing
import com.pairshot.core.domain.model.WatermarkConfig
import com.pairshot.feature.settings.ui.component.ClearCacheDialog
import com.pairshot.feature.settings.ui.component.FileNamePrefixDialog
import com.pairshot.feature.settings.ui.component.ImageQualityDialog
import com.pairshot.feature.settings.ui.component.OverlayAlphaDialog
import com.pairshot.feature.settings.ui.component.SettingsCard
import com.pairshot.feature.settings.ui.component.SettingsDivider
import com.pairshot.feature.settings.ui.component.SettingsItem
import com.pairshot.feature.settings.ui.component.SettingsSectionLabel
import com.pairshot.feature.settings.ui.component.SettingsSwitchItem
import com.pairshot.feature.settings.ui.viewmodel.SettingsUiState
import com.pairshot.feature.settings.ui.viewmodel.formatBytes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    watermarkConfig: WatermarkConfig,
    onClearCache: () -> Unit,
    onLicenseClick: () -> Unit,
    onNavigateBack: () -> Unit,
    onWatermarkConfigChange: (WatermarkConfig) -> Unit,
    onWatermarkSettingsClick: () -> Unit,
    onJpegQualityChange: (Int) -> Unit,
    onFileNamePrefixChange: (String) -> Unit,
    onOverlayEnabledChange: (Boolean) -> Unit,
    onOverlayAlphaChange: (Float) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var showPrefixDialog by remember { mutableStateOf(false) }
    var showOverlayDialog by remember { mutableStateOf(false) }

    val currentQuality = (uiState as? SettingsUiState.Success)?.jpegQuality ?: 85
    val currentPrefix = (uiState as? SettingsUiState.Success)?.fileNamePrefix ?: "PAIRSHOT"
    val currentAlpha = (uiState as? SettingsUiState.Success)?.overlayAlpha ?: 0.3f

    if (showOverlayDialog) {
        OverlayAlphaDialog(
            currentAlpha = currentAlpha,
            onAlphaChange = onOverlayAlphaChange,
            onDismiss = { showOverlayDialog = false },
        )
    }

    if (showClearCacheDialog) {
        ClearCacheDialog(
            onConfirm = onClearCache,
            onDismiss = { showClearCacheDialog = false },
        )
    }

    if (showQualityDialog) {
        ImageQualityDialog(
            currentQuality = currentQuality,
            onQualityChange = onJpegQualityChange,
            onDismiss = { showQualityDialog = false },
        )
    }

    if (showPrefixDialog) {
        FileNamePrefixDialog(
            currentPrefix = currentPrefix,
            onPrefixChange = onFileNamePrefixChange,
            onDismiss = { showPrefixDialog = false },
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "설정",
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
        when (uiState) {
            is SettingsUiState.Loading -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is SettingsUiState.Error -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "설정을 불러올 수 없습니다",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            is SettingsUiState.Success -> {
                val qualityOptions =
                    listOf(
                        Triple("낮음 (75%)", "파일 크기 작음", 75),
                        Triple("높음 (85%)", "기본값, 균형", 85),
                        Triple("최상 (95%)", "최대 품질", 95),
                    )
                val qualityLabel =
                    qualityOptions
                        .firstOrNull { it.third == uiState.jpegQuality }
                        ?.first
                        ?: "${uiState.jpegQuality}%"
                val prefixDisplay =
                    if (uiState.fileNamePrefix.isEmpty()) "없음" else "${uiState.fileNamePrefix}_"

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
                    item(key = "label_capture") {
                        SettingsSectionLabel(label = "촬영 및 파일")
                        Spacer(modifier = Modifier.height(PairShotSpacing.iconTextGap))
                    }

                    item(key = "card_capture") {
                        SettingsCard {
                            SettingsItem(
                                label = "이미지 품질",
                                trailing = qualityLabel,
                                onClick = { showQualityDialog = true },
                            )
                            SettingsDivider()
                            SettingsSwitchItem(
                                label = "오버레이 투명도",
                                checked = uiState.overlayEnabled,
                                onCheckedChange = onOverlayEnabledChange,
                                onClick = { showOverlayDialog = true },
                            )
                            SettingsDivider()
                            SettingsItem(
                                label = "파일명 접두어",
                                trailing = prefixDisplay,
                                onClick = { showPrefixDialog = true },
                            )
                        }
                    }

                    item(key = "gap_1") {
                        Spacer(modifier = Modifier.height(PairShotSpacing.cardPadding))
                    }

                    item(key = "label_watermark") {
                        SettingsSectionLabel(label = "워터마크")
                        Spacer(modifier = Modifier.height(PairShotSpacing.iconTextGap))
                    }

                    item(key = "card_watermark") {
                        SettingsCard {
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable(onClick = onWatermarkSettingsClick)
                                        .padding(
                                            horizontal = PairShotSpacing.cardPadding,
                                            vertical = PairShotSpacing.cardPadding,
                                        ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "워터마크 사용",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                )
                                Switch(
                                    checked = watermarkConfig.enabled,
                                    onCheckedChange = { checked ->
                                        onWatermarkConfigChange(watermarkConfig.copy(enabled = checked))
                                    },
                                )
                            }
                        }
                    }

                    item(key = "gap_2") {
                        Spacer(modifier = Modifier.height(PairShotSpacing.cardPadding))
                    }

                    item(key = "label_storage") {
                        SettingsSectionLabel(label = "저장공간")
                        Spacer(modifier = Modifier.height(PairShotSpacing.iconTextGap))
                    }

                    item(key = "card_storage") {
                        SettingsCard {
                            SettingsItem(
                                label = "사진 용량",
                                trailing = formatBytes(uiState.usedStorageBytes),
                            )
                            SettingsDivider()
                            SettingsItem(
                                label = "캐시",
                                trailing = formatBytes(uiState.cacheBytes),
                                onClick = { showClearCacheDialog = true },
                            )
                        }
                    }

                    item(key = "gap_3") {
                        Spacer(modifier = Modifier.height(PairShotSpacing.cardPadding))
                    }

                    item(key = "label_info") {
                        SettingsSectionLabel(label = "앱 정보")
                        Spacer(modifier = Modifier.height(PairShotSpacing.iconTextGap))
                    }

                    item(key = "card_info") {
                        SettingsCard {
                            SettingsItem(
                                label = "앱 버전",
                                trailing = uiState.appVersion,
                            )
                            SettingsDivider()
                            SettingsItem(
                                label = "라이선스",
                                onClick = onLicenseClick,
                            )
                        }
                    }
                }
            }
        }
    }
}
