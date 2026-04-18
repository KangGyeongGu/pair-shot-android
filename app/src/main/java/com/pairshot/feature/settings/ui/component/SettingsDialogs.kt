package com.pairshot.feature.settings.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
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

@Composable
internal fun OverlayAlphaDialog(
    currentAlpha: Float,
    onAlphaChange: (Float) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
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
                onValueChange = onAlphaChange,
                labelWidth = 0.dp,
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("확인")
            }
        },
    )
}

@Composable
internal fun ClearCacheDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        containerColor = MaterialTheme.colorScheme.surface,
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
    AlertDialog(
        onDismissRequest = onDismiss,
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
                                    onQualityChange(option.value)
                                    onDismiss()
                                }.padding(vertical = PairShotSpacing.itemGap),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = currentQuality == option.value,
                            onClick = {
                                onQualityChange(option.value)
                                onDismiss()
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
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        },
    )
}

@Composable
internal fun FileNamePrefixDialog(
    currentPrefix: String,
    onPrefixChange: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var prefixInput by rememberSaveable { mutableStateOf(currentPrefix) }

    AlertDialog(
        onDismissRequest = onDismiss,
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
                    onPrefixChange(prefixInput)
                    onDismiss()
                },
            ) {
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
