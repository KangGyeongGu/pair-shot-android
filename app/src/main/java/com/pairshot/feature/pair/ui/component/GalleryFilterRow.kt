package com.pairshot.feature.pair.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pairshot.ui.theme.PairShotSpacing

@Composable
internal fun GalleryFilterRow(
    totalCount: Int,
    combinedCount: Int,
    showCombinedOnly: Boolean,
    onToggleFilter: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = PairShotSpacing.screenPadding),
        horizontalArrangement = Arrangement.spacedBy(PairShotSpacing.iconTextGap),
    ) {
        FilterChip(
            selected = !showCombinedOnly,
            onClick = { if (showCombinedOnly) onToggleFilter() },
            label = { Text(text = "전체 ($totalCount)") },
        )
        FilterChip(
            selected = showCombinedOnly,
            onClick = { if (!showCombinedOnly) onToggleFilter() },
            label = { Text(text = "합성 ($combinedCount)") },
        )
    }
}
