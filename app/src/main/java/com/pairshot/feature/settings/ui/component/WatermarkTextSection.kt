package com.pairshot.feature.settings.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pairshot.domain.model.WatermarkConfig
import com.pairshot.domain.model.WatermarkType
import com.pairshot.ui.theme.PairShotSpacing

@Composable
internal fun WatermarkTextSection(
    watermarkConfig: WatermarkConfig,
    onWatermarkConfigChange: (WatermarkConfig) -> Unit,
) {
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

@Composable
internal fun WatermarkTypeItem(
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
            textAlign = TextAlign.End,
        )
    val cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)

    val focusRequester = remember { FocusRequester() }
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
