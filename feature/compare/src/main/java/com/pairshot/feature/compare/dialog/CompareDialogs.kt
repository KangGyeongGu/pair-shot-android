package com.pairshot.feature.compare.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pairshot.core.designsystem.PairShotSpacing
import com.pairshot.core.ui.component.PairShotDialog

@Composable
internal fun CombiningDialog() {
    PairShotDialog(
        onDismissRequest = {},
        title = { Text("합성 중...") },
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PairShotSpacing.cardPadding),
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Text("이미지를 합성하고 있습니다", style = MaterialTheme.typography.bodyMedium)
            }
        },
        confirmButton = {},
    )
}

@Composable
internal fun DeleteConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    PairShotDialog(
        onDismissRequest = onDismiss,
        title = { Text("페어 삭제") },
        text = { Text("Before/After 사진이 모두 삭제됩니다.", style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("삭제", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        },
    )
}
