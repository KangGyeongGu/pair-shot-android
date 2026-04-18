package com.pairshot.feature.pair.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.pairshot.core.designsystem.PairShotSpacing
import com.pairshot.core.ui.component.PairShotDialog
import com.pairshot.core.ui.component.PairShotProgressBar
import com.pairshot.feature.pair.ui.viewmodel.CombineProgress

@Composable
internal fun DeletePairsDialog(
    selectedCount: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    PairShotDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "선택 항목 삭제") },
        text = { Text(text = "${selectedCount}개 페어를 삭제하시겠습니까?") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
            ) {
                Text(
                    text = "삭제",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "취소")
            }
        },
    )
}

@Composable
internal fun RenameProjectDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var newName by remember { mutableStateOf(currentName) }
    PairShotDialog(
        onDismissRequest = onDismiss,
        title = { Text("프로젝트명 수정", style = MaterialTheme.typography.titleMedium) },
        text = {
            TextField(
                value = newName,
                onValueChange = { newName = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors =
                    TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(newName)
                },
                enabled = newName.isNotBlank(),
            ) {
                Text("저장", color = MaterialTheme.colorScheme.primary)
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
internal fun DeleteProjectDialog(
    projectName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    PairShotDialog(
        onDismissRequest = onDismiss,
        title = { Text("프로젝트 삭제", style = MaterialTheme.typography.titleMedium) },
        text = {
            Text(
                "'$projectName' 프로젝트와 모든 사진이 삭제됩니다.",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
            ) {
                Text("삭제", color = MaterialTheme.colorScheme.error)
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
internal fun CombineProgressDialog(progress: CombineProgress) {
    PairShotDialog(
        onDismissRequest = { },
        title = { Text(text = "합성 중") },
        text = {
            Column {
                PairShotProgressBar(
                    progress = progress.current.toFloat() / progress.total,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(PairShotSpacing.iconTextGap))
                Text(
                    text = "${progress.current}/${progress.total} 처리 중...",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        confirmButton = { },
    )
}
