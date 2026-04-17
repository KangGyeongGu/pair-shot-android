package com.pairshot.ui.settings

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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pairshot.domain.model.WatermarkConfig
import com.pairshot.ui.theme.PairShotSpacing

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
    snackbarHostState: SnackbarHostState,
) {
    var showClearCacheDialog by remember { mutableStateOf(false) }

    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
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
                                trailing = "높음 (85%)",
                            )
                            SettingsDivider()
                            SettingsItem(
                                label = "오버레이 기본 투명도",
                                trailing = "30%",
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
