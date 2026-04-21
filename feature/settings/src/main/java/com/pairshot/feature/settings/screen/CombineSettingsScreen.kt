package com.pairshot.feature.settings.screen

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pairshot.core.designsystem.PairShotSpacing
import com.pairshot.core.model.CombineConfig
import com.pairshot.core.model.CombineLayout
import com.pairshot.core.model.LabelAnchor
import com.pairshot.core.model.LabelPosition
import com.pairshot.core.model.LabelPositionMode
import com.pairshot.core.model.RenderProfile
import com.pairshot.core.model.WatermarkConfig
import com.pairshot.core.rendering.PairImageComposer
import com.pairshot.core.rendering.PreviewSampleProvider
import com.pairshot.core.ui.component.PairShotDialog
import com.pairshot.core.ui.component.SettingsCard
import com.pairshot.core.ui.component.SettingsDivider
import com.pairshot.core.ui.component.SettingsSectionLabel
import com.pairshot.core.ui.component.SettingsSliderItem
import com.pairshot.core.ui.component.SettingsSwitchItem
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CombineSettingsScreen(
    combineConfig: CombineConfig,
    watermarkConfig: WatermarkConfig,
    pairImageComposer: PairImageComposer,
    previewSampleProvider: PreviewSampleProvider,
    onCombineConfigChange: (CombineConfig) -> Unit,
    onNavigateBack: () -> Unit,
) {
    var borderColorPickerVisible by remember { mutableStateOf(false) }
    var labelTextColorPickerVisible by remember { mutableStateOf(false) }
    var labelBgColorPickerVisible by remember { mutableStateOf(false) }

    if (borderColorPickerVisible) {
        ColorPickerDialog(
            initialColor = combineConfig.borderColorArgb,
            onDismiss = { borderColorPickerVisible = false },
            onConfirm = { color ->
                onCombineConfigChange(combineConfig.copy(borderColorArgb = color))
                borderColorPickerVisible = false
            },
        )
    }

    if (labelTextColorPickerVisible) {
        ColorPickerDialog(
            initialColor = combineConfig.labelTextColorArgb,
            onDismiss = { labelTextColorPickerVisible = false },
            onConfirm = { color ->
                onCombineConfigChange(combineConfig.copy(labelTextColorArgb = color))
                labelTextColorPickerVisible = false
            },
        )
    }

    if (labelBgColorPickerVisible) {
        LabelBgColorPickerDialog(
            initialColor = combineConfig.labelBgColorArgb,
            borderColorArgb = combineConfig.borderColorArgb,
            borderEnabled = combineConfig.borderEnabled,
            initialMatchesBorder = combineConfig.labelBgMatchesBorder,
            onDismiss = { labelBgColorPickerVisible = false },
            onConfirm = { color, matchesBorder ->
                onCombineConfigChange(
                    combineConfig.copy(
                        labelBgColorArgb = color,
                        labelBgMatchesBorder = matchesBorder,
                    ),
                )
                labelBgColorPickerVisible = false
            },
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "합성 설정",
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
            item(key = "label_layout") {
                SettingsSectionLabel(label = "레이아웃")
                Spacer(modifier = Modifier.height(PairShotSpacing.iconTextGap))
            }

            item(key = "card_layout") {
                SettingsCard {
                    CombineLayoutItem(
                        selectedLayout = combineConfig.layout,
                        onLayoutChange = { layout ->
                            onCombineConfigChange(combineConfig.copy(layout = layout))
                        },
                    )
                }
            }

            item(key = "gap_1") {
                Spacer(modifier = Modifier.height(PairShotSpacing.sectionGap))
            }

            item(key = "label_border") {
                SettingsSectionLabel(label = "테두리")
                Spacer(modifier = Modifier.height(PairShotSpacing.iconTextGap))
            }

            item(key = "card_border") {
                SettingsCard {
                    SettingsSwitchItem(
                        label = "테두리",
                        checked = combineConfig.borderEnabled,
                        onCheckedChange = { checked ->
                            onCombineConfigChange(combineConfig.copy(borderEnabled = checked))
                        },
                    )
                    AnimatedVisibility(
                        visible = combineConfig.borderEnabled,
                        enter = expandVertically(),
                        exit = shrinkVertically(),
                    ) {
                        Column {
                            SettingsDivider()
                            SettingsSliderItem(
                                label = "두께",
                                value = combineConfig.borderThicknessDp.toFloat(),
                                valueRange = 0f..32f,
                                steps = 31,
                                valueLabel = { "${it.toInt()}dp" },
                                onValueChange = { v ->
                                    onCombineConfigChange(combineConfig.copy(borderThicknessDp = v.toInt()))
                                },
                                onLiveUpdate = { v ->
                                    onCombineConfigChange(combineConfig.copy(borderThicknessDp = v.toInt()))
                                },
                            )
                            SettingsDivider()
                            ColorItem(
                                label = "테두리 색상",
                                colorArgb = combineConfig.borderColorArgb,
                                onClick = { borderColorPickerVisible = true },
                            )
                        }
                    }
                }
            }

            item(key = "gap_2") {
                Spacer(modifier = Modifier.height(PairShotSpacing.sectionGap))
            }

            item(key = "label_label") {
                SettingsSectionLabel(label = "레이블")
                Spacer(modifier = Modifier.height(PairShotSpacing.iconTextGap))
            }

            item(key = "card_label_text") {
                SettingsCard {
                    SettingsSwitchItem(
                        label = "레이블",
                        checked = combineConfig.labelEnabled,
                        onCheckedChange = { checked ->
                            onCombineConfigChange(combineConfig.copy(labelEnabled = checked))
                        },
                    )
                    AnimatedVisibility(
                        visible = combineConfig.labelEnabled,
                        enter = expandVertically(),
                        exit = shrinkVertically(),
                    ) {
                        Column {
                            SettingsDivider()
                            LabelTextItem(
                                labelName = "BEFORE 텍스트",
                                text = combineConfig.beforeLabel,
                                onTextChange = { text ->
                                    onCombineConfigChange(combineConfig.copy(beforeLabel = text))
                                },
                            )
                            SettingsDivider()
                            LabelTextItem(
                                labelName = "AFTER 텍스트",
                                text = combineConfig.afterLabel,
                                onTextChange = { text ->
                                    onCombineConfigChange(combineConfig.copy(afterLabel = text))
                                },
                            )
                            SettingsDivider()
                            SettingsSliderItem(
                                label = "폰트 크기",
                                value = combineConfig.labelSizeRatio,
                                valueRange = 0f..0.10f,
                                steps = 9,
                                valueLabel = { "${(it * 100).roundToInt()}%" },
                                onValueChange = { v ->
                                    onCombineConfigChange(combineConfig.copy(labelSizeRatio = v))
                                },
                                onLiveUpdate = { v ->
                                    onCombineConfigChange(combineConfig.copy(labelSizeRatio = v))
                                },
                            )
                            SettingsDivider()
                            ColorItem(
                                label = "텍스트 색상",
                                colorArgb = combineConfig.labelTextColorArgb,
                                onClick = { labelTextColorPickerVisible = true },
                            )
                        }
                    }
                }
            }

            item(key = "card_label_position") {
                AnimatedVisibility(
                    visible = combineConfig.labelEnabled,
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(PairShotSpacing.cardPadding))
                        SettingsCard {
                            LabelPositionModeItem(
                                selectedMode = combineConfig.labelPositionMode,
                                onModeChange = { mode ->
                                    onCombineConfigChange(combineConfig.copy(labelPositionMode = mode))
                                },
                            )
                            if (combineConfig.labelPositionMode == LabelPositionMode.FULL_WIDTH) {
                                SettingsDivider()
                                LabelPositionItem(
                                    selectedPosition = combineConfig.labelPosition,
                                    onPositionChange = { position ->
                                        onCombineConfigChange(combineConfig.copy(labelPosition = position))
                                    },
                                )
                            } else {
                                SettingsDivider()
                                LabelAnchorPickerRow(
                                    label = "BEFORE 위치",
                                    selectedAnchor = combineConfig.beforeLabelAnchor,
                                    onAnchorChange = { anchor ->
                                        onCombineConfigChange(combineConfig.copy(beforeLabelAnchor = anchor))
                                    },
                                )
                                SettingsDivider()
                                LabelAnchorPickerRow(
                                    label = "AFTER 위치",
                                    selectedAnchor = combineConfig.afterLabelAnchor,
                                    onAnchorChange = { anchor ->
                                        onCombineConfigChange(combineConfig.copy(afterLabelAnchor = anchor))
                                    },
                                )
                            }
                        }
                    }
                }
            }

            item(key = "card_label_bg") {
                AnimatedVisibility(
                    visible = combineConfig.labelEnabled,
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(PairShotSpacing.cardPadding))
                        SettingsCard {
                            SettingsSwitchItem(
                                label = "레이블 배경",
                                checked = combineConfig.labelBgEnabled,
                                onCheckedChange = { checked ->
                                    onCombineConfigChange(combineConfig.copy(labelBgEnabled = checked))
                                },
                            )
                            AnimatedVisibility(
                                visible = combineConfig.labelBgEnabled,
                                enter = expandVertically(),
                                exit = shrinkVertically(),
                            ) {
                                Column {
                                    SettingsDivider()
                                    ColorItem(
                                        label = "배경 색상",
                                        colorArgb = combineConfig.labelBgColorArgb,
                                        onClick = { labelBgColorPickerVisible = true },
                                    )
                                    SettingsDivider()
                                    SettingsSliderItem(
                                        label = "배경 투명도",
                                        value = combineConfig.labelBgAlpha,
                                        valueRange = 0f..1f,
                                        valueLabel = { "${(it * 100).roundToInt()}%" },
                                        onValueChange = { v ->
                                            onCombineConfigChange(combineConfig.copy(labelBgAlpha = v))
                                        },
                                        onLiveUpdate = { v ->
                                            onCombineConfigChange(combineConfig.copy(labelBgAlpha = v))
                                        },
                                    )
                                    if (combineConfig.labelPositionMode == LabelPositionMode.FREE) {
                                        SettingsDivider()
                                        SettingsSliderItem(
                                            label = "배경 곡률",
                                            value = combineConfig.labelBgCornerDp.toFloat(),
                                            valueRange = 0f..50f,
                                            steps = 24,
                                            valueLabel = { "${it.toInt()}dp" },
                                            onValueChange = { v ->
                                                onCombineConfigChange(combineConfig.copy(labelBgCornerDp = v.toInt()))
                                            },
                                            onLiveUpdate = { v ->
                                                onCombineConfigChange(combineConfig.copy(labelBgCornerDp = v.toInt()))
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item(key = "gap_3") {
                Spacer(modifier = Modifier.height(PairShotSpacing.sectionGap))
            }

            item(key = "label_preview") {
                SettingsSectionLabel(label = "미리보기")
                Spacer(modifier = Modifier.height(PairShotSpacing.iconTextGap))
            }

            item(key = "combine_preview") {
                CombinePreviewSection(
                    config = combineConfig,
                    watermarkConfig = watermarkConfig,
                    pairImageComposer = pairImageComposer,
                    previewSampleProvider = previewSampleProvider,
                )
                Spacer(modifier = Modifier.height(PairShotSpacing.sectionGap))
            }
        }
    }
}

@Composable
private fun CombineLayoutItem(
    selectedLayout: CombineLayout,
    onLayoutChange: (CombineLayout) -> Unit,
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
            text = "방향",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(PairShotSpacing.iconTextGap)) {
            CombineLayout.entries.forEach { layout ->
                val isSelected = layout == selectedLayout
                Box(
                    modifier =
                        Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(
                                if (isSelected) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                } else {
                                    Color.Transparent
                                },
                            ).clickable { onLayoutChange(layout) }
                            .padding(
                                horizontal = PairShotSpacing.itemGap,
                                vertical = PairShotSpacing.iconTextGap,
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text =
                            when (layout) {
                                CombineLayout.HORIZONTAL -> "좌우"
                                CombineLayout.VERTICAL -> "상하"
                            },
                        style =
                            MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            ),
                        color =
                            if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    )
                }
            }
        }
    }
}

@Composable
private fun LabelPositionItem(
    selectedPosition: LabelPosition,
    onPositionChange: (LabelPosition) -> Unit,
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
            text = "레이블 위치",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(PairShotSpacing.iconTextGap)) {
            LabelPosition.entries.forEach { position ->
                val isSelected = position == selectedPosition
                Box(
                    modifier =
                        Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(
                                if (isSelected) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                } else {
                                    Color.Transparent
                                },
                            ).clickable { onPositionChange(position) }
                            .padding(
                                horizontal = PairShotSpacing.itemGap,
                                vertical = PairShotSpacing.iconTextGap,
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text =
                            when (position) {
                                LabelPosition.TOP -> "위"
                                LabelPosition.BOTTOM -> "아래"
                            },
                        style =
                            MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            ),
                        color =
                            if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    )
                }
            }
        }
    }
}

@Composable
private fun LabelPositionModeItem(
    selectedMode: LabelPositionMode,
    onModeChange: (LabelPositionMode) -> Unit,
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
            text = "레이블 방식",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(PairShotSpacing.iconTextGap)) {
            LabelPositionMode.entries.forEach { mode ->
                val isSelected = mode == selectedMode
                Box(
                    modifier =
                        Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(
                                if (isSelected) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                } else {
                                    Color.Transparent
                                },
                            ).clickable { onModeChange(mode) }
                            .padding(
                                horizontal = PairShotSpacing.itemGap,
                                vertical = PairShotSpacing.iconTextGap,
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text =
                            when (mode) {
                                LabelPositionMode.FULL_WIDTH -> "전체 너비"
                                LabelPositionMode.FREE -> "자유 위치"
                            },
                        style =
                            MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            ),
                        color =
                            if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    )
                }
            }
        }
    }
}

private val labelAnchorOrder =
    listOf(
        LabelAnchor.TOP_LEFT,
        LabelAnchor.TOP_CENTER,
        LabelAnchor.TOP_RIGHT,
        LabelAnchor.MIDDLE_LEFT,
        LabelAnchor.MIDDLE_CENTER,
        LabelAnchor.MIDDLE_RIGHT,
        LabelAnchor.BOTTOM_LEFT,
        LabelAnchor.BOTTOM_CENTER,
        LabelAnchor.BOTTOM_RIGHT,
    )

@Composable
private fun LabelAnchorPickerRow(
    label: String,
    selectedAnchor: LabelAnchor,
    onAnchorChange: (LabelAnchor) -> Unit,
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
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Column(verticalArrangement = Arrangement.spacedBy(PairShotSpacing.iconTextGap)) {
            labelAnchorOrder.chunked(3).forEach { rowAnchors ->
                Row(horizontalArrangement = Arrangement.spacedBy(PairShotSpacing.iconTextGap)) {
                    rowAnchors.forEach { anchor ->
                        val isSelected = anchor == selectedAnchor
                        Box(
                            modifier =
                                Modifier
                                    .size(PairShotSpacing.iconSize)
                                    .clip(MaterialTheme.shapes.extraSmall)
                                    .background(
                                        if (isSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.surfaceContainerHigh
                                        },
                                    ).semantics { selected = isSelected }
                                    .clickable { onAnchorChange(anchor) },
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
private fun LabelTextItem(
    labelName: String,
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

    val textColor = MaterialTheme.colorScheme.onSurface
    val hintColor = MaterialTheme.colorScheme.onSurfaceVariant
    val dividerColor = MaterialTheme.colorScheme.outline
    val textStyle =
        MaterialTheme.typography.bodyMedium.copy(
            color = textColor,
            textAlign = TextAlign.End,
        )
    val cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
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
            text = labelName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.45f),
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
                    .weight(0.55f)
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
                                textAlign = TextAlign.End,
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

@Composable
private fun ColorItem(
    label: String,
    colorArgb: Int,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .height(PairShotSpacing.inputRow)
                .padding(horizontal = PairShotSpacing.cardPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier =
                Modifier
                    .size(24.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(Color(colorArgb)),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "#%06X".format(colorArgb and 0xFFFFFF),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private val HUE_PRESETS: List<Float?> = listOf(null, -1f, 0f, 30f, 60f, 120f, 210f, 240f, 270f)

private fun nearestPresetIdx(hsv: FloatArray): Int {
    val isBlack = hsv[2] < 0.15f && hsv[1] < 0.15f
    val isWhiteish = !isBlack && hsv[1] < 0.15f
    return when {
        isBlack -> {
            1
        }

        isWhiteish -> {
            0
        }

        else -> {
            HUE_PRESETS.indices.drop(2).minByOrNull { idx ->
                val hue = HUE_PRESETS[idx] ?: 0f
                val diff = kotlin.math.abs(hue - hsv[0])
                minOf(diff, 360f - diff)
            } ?: 2
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColorPickerContent(
    selectedIdx: Int,
    onSelectedIdxChange: (Int) -> Unit,
    brightness: Float,
    onBrightnessChange: (Float) -> Unit,
    currentColor: Color,
) {
    val selectedHue = HUE_PRESETS[selectedIdx]
    val isGrayscale = selectedHue == null || selectedHue == -1f
    val gradientStart = if (isGrayscale) Color.Black else Color.hsv(selectedHue!!, 1f, 0.15f)
    val gradientEnd = if (isGrayscale) Color.White else Color.hsv(selectedHue!!, 1f, 1f)
    val sliderRange = if (isGrayscale) 0f..1f else 0.15f..1f

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            HUE_PRESETS.forEachIndexed { idx, hue ->
                val swatchColor =
                    when {
                        hue == null -> Color.White
                        hue == -1f -> Color.Black
                        else -> Color.hsv(hue, 1f, 0.9f)
                    }
                val isSelected = idx == selectedIdx
                val needsOutline = hue == null || hue == -1f
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(MaterialTheme.shapes.small)
                            .background(swatchColor)
                            .then(
                                when {
                                    isSelected -> {
                                        Modifier.border(
                                            2.dp,
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.shapes.small,
                                        )
                                    }

                                    needsOutline -> {
                                        Modifier.border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outlineVariant,
                                            MaterialTheme.shapes.small,
                                        )
                                    }

                                    else -> {
                                        Modifier
                                    }
                                },
                            ).clickable { onSelectedIdxChange(idx) },
                )
            }
        }

        androidx.compose.material3.Slider(
            value = brightness,
            onValueChange = onBrightnessChange,
            valueRange = sliderRange,
            track = { _ ->
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(CircleShape)
                            .background(Brush.horizontalGradient(listOf(gradientStart, gradientEnd))),
                )
            },
            thumb = { _ ->
                Box(
                    modifier =
                        Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape),
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(24.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(currentColor)
                        .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small),
            )
            Text(
                text = "#%06X".format(currentColor.toArgb() and 0xFFFFFF),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ColorPickerDialog(
    initialColor: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    val initialHsv = remember { FloatArray(3).also { android.graphics.Color.colorToHSV(initialColor, it) } }
    var selectedIdx by remember { mutableStateOf(nearestPresetIdx(initialHsv)) }
    var brightness by remember { mutableStateOf(initialHsv[2].coerceIn(0f, 1f)) }

    val selectedHue = HUE_PRESETS[selectedIdx]
    val isGrayscale = selectedHue == null || selectedHue == -1f
    val currentColor =
        if (isGrayscale) Color.hsv(0f, 0f, brightness) else Color.hsv(selectedHue!!, 1f, brightness)

    PairShotDialog(
        onDismissRequest = onDismiss,
        title = { Text("색상 선택") },
        text = {
            ColorPickerContent(
                selectedIdx = selectedIdx,
                onSelectedIdxChange = { idx ->
                    when (HUE_PRESETS[idx]) {
                        null -> brightness = 1.0f
                        -1f -> brightness = 0.05f
                        else -> Unit
                    }
                    selectedIdx = idx
                },
                brightness = brightness,
                onBrightnessChange = { brightness = it },
                currentColor = currentColor,
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(currentColor.toArgb()) }) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        },
    )
}

@Composable
private fun LabelBgColorPickerDialog(
    initialColor: Int,
    borderColorArgb: Int,
    borderEnabled: Boolean,
    initialMatchesBorder: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (color: Int, matchesBorder: Boolean) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    var matchesBorder by remember { mutableStateOf(initialMatchesBorder) }
    val initialHsv = remember { FloatArray(3).also { android.graphics.Color.colorToHSV(initialColor, it) } }
    var selectedIdx by remember { mutableStateOf(nearestPresetIdx(initialHsv)) }
    var brightness by remember { mutableStateOf(initialHsv[2].coerceIn(0f, 1f)) }

    val selectedHue = HUE_PRESETS[selectedIdx]
    val isGrayscale = selectedHue == null || selectedHue == -1f
    val currentPickedColor =
        if (isGrayscale) Color.hsv(0f, 0f, brightness) else Color.hsv(selectedHue!!, 1f, brightness)

    PairShotDialog(
        onDismissRequest = onDismiss,
        title = { Text("배경 색상") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "테두리색상과 일치",
                        style = MaterialTheme.typography.bodyMedium,
                        color =
                            if (borderEnabled) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            },
                        modifier = Modifier.weight(1f),
                    )
                    androidx.compose.material3.Switch(
                        checked = matchesBorder,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            matchesBorder = it
                        },
                        enabled = borderEnabled,
                    )
                }

                if (matchesBorder) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(24.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(Color(borderColorArgb))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small),
                        )
                        Text(
                            text = "#%06X".format(borderColorArgb and 0xFFFFFF),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    ColorPickerContent(
                        selectedIdx = selectedIdx,
                        onSelectedIdxChange = { idx ->
                            when (HUE_PRESETS[idx]) {
                                null -> brightness = 1.0f
                                -1f -> brightness = 0.05f
                                else -> Unit
                            }
                            selectedIdx = idx
                        },
                        brightness = brightness,
                        onBrightnessChange = { brightness = it },
                        currentColor = currentPickedColor,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(currentPickedColor.toArgb(), matchesBorder) }) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        },
    )
}

@Composable
internal fun CombinePreviewSection(
    config: CombineConfig,
    watermarkConfig: WatermarkConfig,
    pairImageComposer: PairImageComposer,
    previewSampleProvider: PreviewSampleProvider,
    modifier: Modifier = Modifier,
) {
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(config, watermarkConfig) {
        val sample = previewSampleProvider.get()
        val result =
            runCatching {
                pairImageComposer.composeFromBitmaps(
                    before = sample,
                    after = sample,
                    combineConfig = config,
                    watermarkConfig = watermarkConfig,
                    profile = RenderProfile.PREVIEW,
                )
            }.getOrNull()
        previewBitmap?.let { old ->
            if (old !== result && !old.isRecycled) old.recycle()
        }
        previewBitmap = result
    }

    val aspectRatio =
        when (config.layout) {
            CombineLayout.HORIZONTAL -> 2f
            CombineLayout.VERTICAL -> 0.5f
        }

    val bmp = previewBitmap
    if (bmp != null) {
        Image(
            bitmap = bmp.asImageBitmap(),
            contentDescription = "합성 미리보기",
            contentScale = ContentScale.Fit,
            modifier =
                modifier
                    .fillMaxWidth()
                    .aspectRatio(
                        bmp.width.toFloat().coerceAtLeast(1f) /
                            bmp.height.toFloat().coerceAtLeast(1f),
                    ),
        )
    } else {
        Box(
            modifier =
                modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio),
        )
    }
}
