package com.pairshot.feature.album.dialog

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.pairshot.core.ui.component.PairShotDialog
import com.pairshot.feature.album.R
import com.pairshot.core.ui.R as CoreR

@Composable
fun DeletePairsDialog(
    count: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PairShotDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Text(
                text = stringResource(R.string.album_dialog_delete_pairs_title),
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Text(
                text =
                    pluralStringResource(
                        R.plurals.album_dialog_delete_pairs_confirm,
                        count,
                        count,
                    ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(R.string.album_dialog_delete_pairs_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(CoreR.string.common_button_cancel))
            }
        },
    )
}
