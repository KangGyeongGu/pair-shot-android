package com.pairshot.feature.settings.ui.screen

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
import com.pairshot.core.model.WatermarkConfig
import com.pairshot.core.model.WatermarkType
import com.pairshot.core.rendering.WatermarkRenderer
import com.pairshot.core.ui.component.SettingsCard
import com.pairshot.core.ui.component.SettingsDivider
import com.pairshot.core.ui.component.SettingsSectionLabel
import com.pairshot.core.ui.component.SettingsSwitchItem
import com.pairshot.feature.settings.ui.component.WatermarkLogoSection
import com.pairshot.feature.settings.ui.component.WatermarkPreviewSection
import com.pairshot.feature.settings.ui.component.WatermarkTextSection
import com.pairshot.feature.settings.ui.component.WatermarkTypeItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatermarkSettingsScreen(
    watermarkConfig: WatermarkConfig,
    onWatermarkConfigChange: (WatermarkConfig) -> Unit,
    onSelectLogo: () -> Unit,
    onNavigateBack: () -> Unit,
    watermarkRenderer: WatermarkRenderer,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "워터마크 설정",
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
            item(key = "label_basic") {
                SettingsSectionLabel(label = "기본 설정")
                Spacer(modifier = Modifier.height(PairShotSpacing.iconTextGap))
            }

            item(key = "card_basic") {
                SettingsCard {
                    SettingsSwitchItem(
                        label = "워터마크",
                        checked = watermarkConfig.enabled,
                        onCheckedChange = { checked ->
                            onWatermarkConfigChange(watermarkConfig.copy(enabled = checked))
                        },
                    )
                    SettingsDivider()
                    WatermarkTypeItem(
                        selectedType = watermarkConfig.type,
                        onTypeChange = { type ->
                            onWatermarkConfigChange(watermarkConfig.copy(type = type))
                        },
                    )
                }
            }

            if (watermarkConfig.type == WatermarkType.TEXT) {
                item(key = "label_text") {
                    Spacer(modifier = Modifier.height(PairShotSpacing.sectionGap))
                    SettingsSectionLabel(label = "텍스트 설정")
                    Spacer(modifier = Modifier.height(PairShotSpacing.iconTextGap))
                }
                item(key = "card_text") {
                    WatermarkTextSection(
                        watermarkConfig = watermarkConfig,
                        onWatermarkConfigChange = onWatermarkConfigChange,
                    )
                }
            }

            if (watermarkConfig.type == WatermarkType.LOGO) {
                item(key = "label_logo") {
                    Spacer(modifier = Modifier.height(PairShotSpacing.sectionGap))
                    SettingsSectionLabel(label = "로고 설정")
                    Spacer(modifier = Modifier.height(PairShotSpacing.iconTextGap))
                }
                item(key = "card_logo") {
                    WatermarkLogoSection(
                        watermarkConfig = watermarkConfig,
                        onWatermarkConfigChange = onWatermarkConfigChange,
                        onSelectLogo = onSelectLogo,
                    )
                }
            }

            item(key = "wm_preview") {
                Spacer(modifier = Modifier.height(PairShotSpacing.sectionGap))
                SettingsSectionLabel(label = "미리보기")
                Spacer(modifier = Modifier.height(PairShotSpacing.iconTextGap))
                WatermarkPreviewSection(
                    config = watermarkConfig,
                    watermarkRenderer = watermarkRenderer,
                )
                Spacer(modifier = Modifier.height(PairShotSpacing.sectionGap))
            }
        }
    }
}
