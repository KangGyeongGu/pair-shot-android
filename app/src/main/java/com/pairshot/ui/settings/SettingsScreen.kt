package com.pairshot.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.pairshot.domain.model.WatermarkConfig
import com.pairshot.ui.theme.PairShotSpacing

private data class QualityOption(
    val label: String,
    val description: String,
    val value: Int,
)

private val qualityOptions =
    listOf(
        QualityOption("낮음 (75%)", "파일 크기 작음", 75),
        QualityOption("높음 (85%)", "기본값, 균형", 85),
        QualityOption("최상 (95%)", "최대 품질", 95),
    )

private val fileNameSafePattern = Regex("[^a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ_-]")

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

    // ── 오버레이 투명도 다이얼로그 ──
    if (showOverlayDialog) {
        AlertDialog(
            onDismissRequest = { showOverlayDialog = false },
            shape = MaterialTheme.shapes.large,
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("오버레이 기본 투명도") },
            text = {
                SettingsSliderItem(
                    label = "",
                    value = currentAlpha,
                    valueRange = 0f..0.5f,
                    steps = 9,
                    displayText = "${(currentAlpha * 100).toInt()}%",
                    onValueChange = onOverlayAlphaChange,
                    labelWidth = 0.dp,
                )
            },
            confirmButton = {
                TextButton(onClick = { showOverlayDialog = false }) {
                    Text("확인")
                }
            },
        )
    }

    // ── 캐시 초기화 다이얼로그 ──
    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            shape = MaterialTheme.shapes.large,
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("캐시 초기화") },
            text = { Text("캐시를 초기화하시겠습니까?\n썸네일이 삭제되며, 다시 생성됩니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearCacheDialog = false
                        onClearCache()
                    },
                ) {
                    Text("초기화")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("취소")
                }
            },
        )
    }

    // ── 이미지 품질 다이얼로그 ──
    if (showQualityDialog) {
        AlertDialog(
            onDismissRequest = { showQualityDialog = false },
            shape = MaterialTheme.shapes.large,
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("이미지 품질") },
            text = {
                Column {
                    qualityOptions.forEach { option ->
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onJpegQualityChange(option.value)
                                        showQualityDialog = false
                                    }.padding(vertical = PairShotSpacing.itemGap),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = currentQuality == option.value,
                                onClick = {
                                    onJpegQualityChange(option.value)
                                    showQualityDialog = false
                                },
                                colors =
                                    RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary,
                                    ),
                            )
                            Column(modifier = Modifier.padding(start = PairShotSpacing.itemGap)) {
                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = option.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showQualityDialog = false }) {
                    Text("취소")
                }
            },
        )
    }

    // ── 파일명 접두어 다이얼로그 ──
    if (showPrefixDialog) {
        var prefixInput by rememberSaveable { mutableStateOf(currentPrefix) }

        AlertDialog(
            onDismissRequest = { showPrefixDialog = false },
            shape = MaterialTheme.shapes.large,
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("파일명 접두어") },
            text = {
                Column {
                    OutlinedTextField(
                        value = prefixInput,
                        onValueChange = { raw ->
                            prefixInput = raw.replace(fileNameSafePattern, "")
                        },
                        placeholder = {
                            Text(
                                text = "접두어 입력 (예: 현장A)",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth(),
                        suffix = {
                            Text(
                                text = "_",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                    )
                    if (prefixInput != "PAIRSHOT") {
                        TextButton(
                            onClick = { prefixInput = "PAIRSHOT" },
                            modifier = Modifier.align(Alignment.End),
                        ) {
                            Text("기본값으로 초기화")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onFileNamePrefixChange(prefixInput)
                        showPrefixDialog = false
                    },
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPrefixDialog = false }) {
                    Text("취소")
                }
            },
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
                val qualityLabel =
                    qualityOptions
                        .firstOrNull { it.value == uiState.jpegQuality }
                        ?.label
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
                    // ── 카드 1: 촬영 ──
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

                    // ── 카드 2: 워터마크 ──
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

                    // ── 카드 3: 저장 ──
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

                    // ── 카드 4: 정보 ──
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
