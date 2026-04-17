package com.pairshot.ui.settings

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.pairshot.R
import com.pairshot.data.local.image.WatermarkManager
import com.pairshot.domain.model.LogoPosition
import com.pairshot.domain.model.WatermarkConfig
import com.pairshot.domain.model.WatermarkType
import com.pairshot.ui.theme.PairShotSpacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatermarkSettingsScreen(
    watermarkConfig: WatermarkConfig,
    onWatermarkConfigChange: (WatermarkConfig) -> Unit,
    onSelectLogo: () -> Unit,
    onNavigateBack: () -> Unit,
    watermarkManager: WatermarkManager,
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
            // ── 카드 1: 기본 ──
            item(key = "card_basic") {
                SettingsCard {
                    SettingsSwitchItem(
                        label = "워터마크 기본 사용",
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

            item(key = "gap_1") {
                Spacer(modifier = Modifier.height(PairShotSpacing.cardPadding))
            }

            // ── 카드 2: 텍스트 설정 (텍스트 모드일 때) ──
            if (watermarkConfig.type == WatermarkType.TEXT) {
                item(key = "card_text") {
                    SettingsCard {
                        WatermarkTextItem(
                            text = watermarkConfig.text,
                            onTextChange = { text ->
                                onWatermarkConfigChange(watermarkConfig.copy(text = text))
                            },
                        )
                        SettingsDivider()
                        SettingsSliderItem(
                            label = "투명도",
                            value = watermarkConfig.alpha,
                            valueRange = 0f..1f,
                            displayText = "${(watermarkConfig.alpha * 100).toInt()}%",
                            onValueChange = { v ->
                                onWatermarkConfigChange(watermarkConfig.copy(alpha = v))
                            },
                        )
                        SettingsDivider()
                        SettingsSliderItem(
                            label = "대각선 줄 수",
                            value = watermarkConfig.diagonalCount.toFloat(),
                            valueRange = 1f..20f,
                            steps = 18,
                            displayText = "${watermarkConfig.diagonalCount}",
                            onValueChange = { v ->
                                onWatermarkConfigChange(watermarkConfig.copy(diagonalCount = v.toInt()))
                            },
                        )
                        SettingsDivider()
                        SettingsSliderItem(
                            label = "반복 밀도",
                            value = watermarkConfig.repeatDensity,
                            valueRange = 0.5f..3.0f,
                            displayText = "%.1f".format(watermarkConfig.repeatDensity),
                            onValueChange = { v ->
                                onWatermarkConfigChange(watermarkConfig.copy(repeatDensity = v))
                            },
                        )
                    }
                }
            }

            // ── 카드 2: 로고 설정 (로고 모드일 때) ──
            if (watermarkConfig.type == WatermarkType.LOGO) {
                item(key = "card_logo") {
                    SettingsCard {
                        SettingsItem(
                            label = "로고 등록",
                            trailing =
                                if (watermarkConfig.logoPath.isNotEmpty()) {
                                    File(watermarkConfig.logoPath).name
                                } else {
                                    "선택"
                                },
                            onClick = onSelectLogo,
                        )
                        SettingsDivider()
                        LogoPositionItem(
                            selectedPosition = watermarkConfig.logoPosition,
                            onPositionChange = { position ->
                                onWatermarkConfigChange(watermarkConfig.copy(logoPosition = position))
                            },
                        )
                        SettingsDivider()
                        SettingsSliderItem(
                            label = "로고 크기",
                            value = watermarkConfig.logoSizeRatio,
                            valueRange = 0.05f..0.5f,
                            displayText = "${(watermarkConfig.logoSizeRatio * 100).toInt()}%",
                            onValueChange = { v ->
                                onWatermarkConfigChange(watermarkConfig.copy(logoSizeRatio = v))
                            },
                        )
                        SettingsDivider()
                        SettingsSliderItem(
                            label = "로고 투명도",
                            value = watermarkConfig.logoAlpha,
                            valueRange = 0f..1f,
                            displayText = "${(watermarkConfig.logoAlpha * 100).toInt()}%",
                            onValueChange = { v ->
                                onWatermarkConfigChange(watermarkConfig.copy(logoAlpha = v))
                            },
                        )
                    }
                }
            }

            // ── 미리보기 (카드 없이) ──
            item(key = "wm_preview") {
                Spacer(modifier = Modifier.height(PairShotSpacing.sectionGap))
                Text(
                    text = "미리보기",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(PairShotSpacing.iconTextGap))
                WatermarkPreview(
                    config = watermarkConfig,
                    watermarkManager = watermarkManager,
                )
                Spacer(modifier = Modifier.height(PairShotSpacing.sectionGap))
            }
        }
    }
}

// ── Private composables ──

@Composable
private fun WatermarkTypeItem(
    selectedType: WatermarkType,
    onTypeChange: (WatermarkType) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = PairShotSpacing.cardPadding,
                    vertical = PairShotSpacing.cardPadding,
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "유형",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(PairShotSpacing.iconTextGap)) {
            WatermarkType.entries.forEach { type ->
                val isSelected = type == selectedType
                Box(
                    modifier =
                        Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(
                                if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                            ).border(
                                width = 1.dp,
                                color =
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outline
                                    },
                                shape = MaterialTheme.shapes.small,
                            ).clickable { onTypeChange(type) }
                            .padding(
                                horizontal = PairShotSpacing.itemGap,
                                vertical = PairShotSpacing.iconTextGap,
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text =
                            when (type) {
                                WatermarkType.TEXT -> "텍스트"
                                WatermarkType.LOGO -> "로고"
                            },
                        style = MaterialTheme.typography.bodySmall,
                        color =
                            if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                    )
                }
            }
        }
    }
}

@Composable
private fun WatermarkTextItem(
    text: String,
    onTextChange: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = text, selection = TextRange(text.length)))
    }
    LaunchedEffect(text) {
        if (textFieldValue.text != text) {
            textFieldValue = TextFieldValue(text = text, selection = TextRange(text.length))
        }
    }

    val dividerColor = MaterialTheme.colorScheme.outline
    val textColor = MaterialTheme.colorScheme.onSurface
    val hintColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textStyle =
        MaterialTheme.typography.bodyMedium.copy(
            color = textColor,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
        )
    val cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)

    val focusRequester =
        remember {
            androidx.compose.ui.focus
                .FocusRequester()
        }
    var isFocused by remember { mutableStateOf(false) }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = PairShotSpacing.cardPadding,
                    vertical = PairShotSpacing.cardPadding,
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "반복 텍스트",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.35f),
        )
        BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                textFieldValue = newValue
                onTextChange(newValue.text)
            },
            singleLine = true,
            textStyle = textStyle,
            cursorBrush = cursorBrush,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier =
                Modifier
                    .weight(0.65f)
                    .focusRequester(focusRequester)
                    .onFocusChanged { isFocused = it.isFocused },
            decorationBox = { innerTextField ->
                Column {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        if (textFieldValue.text.isEmpty()) {
                            Text(
                                text = "텍스트 입력",
                                style = MaterialTheme.typography.bodyMedium,
                                color = hintColor,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                            )
                        }
                        innerTextField()
                    }
                    if (isFocused) {
                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                    }
                }
            },
        )
    }
}

private val logoPositionOrder =
    listOf(
        LogoPosition.TOP_LEFT,
        LogoPosition.TOP_CENTER,
        LogoPosition.TOP_RIGHT,
        LogoPosition.CENTER_LEFT,
        LogoPosition.CENTER,
        LogoPosition.CENTER_RIGHT,
        LogoPosition.BOTTOM_LEFT,
        LogoPosition.BOTTOM_CENTER,
        LogoPosition.BOTTOM_RIGHT,
    )

@Composable
private fun LogoPositionItem(
    selectedPosition: LogoPosition,
    onPositionChange: (LogoPosition) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = PairShotSpacing.cardPadding,
                    vertical = PairShotSpacing.cardPadding,
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "로고 위치",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(PairShotSpacing.iconTextGap),
        ) {
            logoPositionOrder.chunked(3).forEach { rowPositions ->
                Row(horizontalArrangement = Arrangement.spacedBy(PairShotSpacing.iconTextGap)) {
                    rowPositions.forEach { position ->
                        val isSelected = position == selectedPosition
                        Box(
                            modifier =
                                Modifier
                                    .size(PairShotSpacing.iconSize)
                                    .clip(MaterialTheme.shapes.extraSmall)
                                    .background(
                                        if (isSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        },
                                    ).clickable { onPositionChange(position) },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WatermarkPreview(
    config: WatermarkConfig,
    watermarkManager: WatermarkManager,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val sourceBitmap =
        remember {
            BitmapFactory.decodeResource(context.resources, R.drawable.watermark_preview_sample)
        }

    val previewConfig =
        remember(config) {
            config.copy(enabled = true)
        }

    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(previewConfig) {
        val result =
            withContext(Dispatchers.Default) {
                val scaled =
                    Bitmap.createScaledBitmap(
                        sourceBitmap,
                        (sourceBitmap.width * 0.5f).toInt().coerceAtLeast(200),
                        (sourceBitmap.height * 0.5f).toInt().coerceAtLeast(200),
                        true,
                    )
                val applied = watermarkManager.apply(scaled, previewConfig)
                if (applied !== scaled) scaled.recycle()
                applied
            }
        previewBitmap?.let { old ->
            if (old !== result) old.recycle()
        }
        previewBitmap = result
    }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        val bmp = previewBitmap
        if (bmp != null) {
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "워터마크 미리보기",
                contentScale = ContentScale.Fit,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(sourceBitmap.width.toFloat() / sourceBitmap.height.toFloat()),
            )
        } else {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(sourceBitmap.width.toFloat() / sourceBitmap.height.toFloat()),
            )
        }
    }
}
