package com.pairshot.feature.compare.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun ComparePagerControls(
    pairNumber: Int,
    pairsSize: Int,
    canGoPrev: Boolean,
    canGoNext: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 1.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            onClick = onPrev,
            enabled = canGoPrev,
        ) {
            Text(
                text = "<",
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Text(
            text =
                if (pairNumber > 0) {
                    "$pairNumber/$pairsSize"
                } else {
                    "-"
                },
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(
            onClick = onNext,
            enabled = canGoNext,
        ) {
            Text(
                text = ">",
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}
