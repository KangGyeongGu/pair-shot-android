package com.pairshot.feature.settings.ui.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.pairshot.core.designsystem.PairShotSpacing
import com.pairshot.core.ui.component.PairShotBottomSheet
import com.pairshot.core.ui.component.PairShotDialog
import com.pairshot.core.ui.component.SettingsSliderItem
import kotlin.math.roundToInt

private val InputFieldMinHeight = 40.dp
private val InputErrorHeight = 20.dp

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

@Composable
internal fun OverlayAlphaDialog(
    currentAlpha: Float,
    onAlphaChange: (Float) -> Unit,
    onDismiss: () -> Unit,
) {
    PairShotBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = "오버레이 기본 투명도",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(16.dp))
        SettingsSliderItem(
            label = "",
            value = currentAlpha,
            valueRange = 0f..1.0f,
            steps = 9,
            valueLabel = { "${(it * 100).roundToInt()}%" },
            onValueChange = onAlphaChange,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onDismiss) {
                Text("확인")
            }
        }
    }
}

@Composable
internal fun ClearCacheDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    PairShotDialog(
        onDismissRequest = onDismiss,
        title = { Text("캐시 초기화") },
        text = { Text("캐시를 초기화하시겠습니까?\n썸네일이 삭제되며, 다시 생성됩니다.") },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    onConfirm()
                },
            ) {
                Text("초기화")
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
internal fun ImageQualityDialog(
    currentQuality: Int,
    onQualityChange: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedQuality by rememberSaveable { mutableIntStateOf(currentQuality) }

    PairShotBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = "이미지 품질",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(12.dp))
        qualityOptions.forEach { option ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { selectedQuality = option.value }
                        .padding(vertical = PairShotSpacing.iconTextGap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = selectedQuality == option.value,
                    onClick = { selectedQuality = option.value },
                    colors =
                        RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary,
                        ),
                )
                Column(modifier = Modifier.padding(start = PairShotSpacing.iconTextGap)) {
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
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onDismiss) { Text("취소") }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(
                onClick = {
                    onQualityChange(selectedQuality)
                    onDismiss()
                },
            ) {
                Text("적용", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
internal fun FileNamePrefixDialog(
    currentPrefix: String,
    onPrefixChange: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var prefixInput by rememberSaveable { mutableStateOf(currentPrefix) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    val isError = prefixInput.isBlank()
    val outlineColor = MaterialTheme.colorScheme.outline
    val errorColor = MaterialTheme.colorScheme.error
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    val underscoreSuffix =
        remember {
            VisualTransformation { original ->
                TransformedText(
                    text = AnnotatedString(original.text + "_"),
                    offsetMapping =
                        object : OffsetMapping {
                            override fun originalToTransformed(offset: Int) = offset

                            override fun transformedToOriginal(offset: Int) = offset.coerceAtMost(original.text.length)
                        },
                )
            }
        }

    PairShotBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = "파일명 접두어",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            BasicTextField(
                value = prefixInput,
                onValueChange = { raw ->
                    prefixInput = raw.replace(fileNameSafePattern, "")
                },
                singleLine = true,
                textStyle =
                    MaterialTheme.typography.bodyLarge.copy(
                        color = onSurfaceColor,
                    ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                visualTransformation = underscoreSuffix,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .padding(bottom = PairShotSpacing.xs)
                        .drawBehind {
                            val lineColor = if (isError) errorColor else outlineColor
                            drawLine(
                                color = lineColor,
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = 1.dp.toPx(),
                            )
                        },
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.heightIn(min = InputFieldMinHeight),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (prefixInput.isEmpty()) {
                            Text(
                                text = "접두어 입력 (예: 현장A)_",
                                style = MaterialTheme.typography.bodyLarge,
                                color = onSurfaceVariantColor,
                            )
                        }
                        innerTextField()
                    }
                },
            )
        }
        Box(modifier = Modifier.height(InputErrorHeight)) {
            if (isError) {
                Text(
                    text = "접두어를 입력해주세요",
                    style = MaterialTheme.typography.bodySmall,
                    color = errorColor,
                )
            }
        }
        if (prefixInput != "PAIRSHOT") {
            TextButton(
                onClick = { prefixInput = "PAIRSHOT" },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text("기본값으로 초기화")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onDismiss) { Text("취소") }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(
                onClick = {
                    onPrefixChange(prefixInput)
                    onDismiss()
                },
                enabled = prefixInput.isNotBlank(),
            ) {
                Text("저장", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
