package com.pairshot.feature.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.pairshot.core.designsystem.PairShotSpacing
import com.pairshot.feature.home.R
import com.pairshot.feature.home.viewmodel.HomeMode

@Composable
fun HomeFilterRow(
    selectedMode: HomeMode,
    inSelectionMode: Boolean,
    onModeSelected: (HomeMode) -> Unit,
    onEnterSelectionMode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = PairShotSpacing.screenPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(PairShotSpacing.itemGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HomeMode.entries.forEach { mode ->
                val label =
                    when (mode) {
                        HomeMode.PAIRS -> stringResource(R.string.home_filter_all)
                        HomeMode.ALBUMS -> stringResource(R.string.home_filter_album)
                    }
                FilterChip(
                    selected = selectedMode == mode,
                    onClick = { onModeSelected(mode) },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    },
                    colors =
                        FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    border =
                        FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedMode == mode,
                            borderColor = MaterialTheme.colorScheme.outlineVariant,
                            selectedBorderColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                )
            }
        }

        if (!inSelectionMode) {
            IconButton(onClick = onEnterSelectionMode) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = stringResource(R.string.home_desc_selection_mode),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
