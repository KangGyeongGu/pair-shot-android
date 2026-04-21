package com.pairshot.feature.gallery.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import com.pairshot.core.designsystem.PairShotSpacing
import com.pairshot.core.designsystem.PairShotTypographyTokens

@Composable
internal fun GalleryFilterRow(
    totalCount: Int,
    combinedCount: Int,
    showCombinedOnly: Boolean,
    onToggleFilter: () -> Unit,
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = PairShotSpacing.screenPadding),
        horizontalArrangement = Arrangement.spacedBy(PairShotSpacing.cardPadding, Alignment.End),
    ) {
        FilterTab(
            text = "전체 ($totalCount)",
            selected = !showCombinedOnly,
            selectedColor = onSurface,
            defaultColor = onSurfaceVariant,
            onClick = { if (showCombinedOnly) onToggleFilter() },
        )
        FilterTab(
            text = "합성 ($combinedCount)",
            selected = showCombinedOnly,
            selectedColor = onSurface,
            defaultColor = onSurfaceVariant,
            onClick = { if (!showCombinedOnly) onToggleFilter() },
        )
    }
}

@Composable
private fun FilterTab(
    text: String,
    selected: Boolean,
    selectedColor: androidx.compose.ui.graphics.Color,
    defaultColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        style =
            PairShotTypographyTokens.labelExtraSmall.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            ),
        color = if (selected) selectedColor else defaultColor,
        modifier = Modifier.clickable(onClick = onClick),
    )
}
