package com.pairshot.feature.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pairshot.core.model.PhotoPair
import com.pairshot.core.model.SortOrder
import com.pairshot.core.ui.component.PairCard
import com.pairshot.feature.home.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy. MM. dd", Locale.KOREAN)

private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

@Composable
private fun formatDateLabel(
    date: LocalDate,
    today: LocalDate,
): String {
    val base = date.format(DateFormatter)
    return when (date) {
        today -> stringResource(R.string.home_date_suffix_today, base)
        today.minusDays(1) -> stringResource(R.string.home_date_suffix_yesterday, base)
        else -> base
    }
}

@Composable
fun HomePairGridSection(
    pairs: List<PhotoPair>,
    selectedIds: Set<Long>,
    selectionMode: Boolean,
    sortOrder: SortOrder,
    onPairClick: (Long) -> Unit,
    onPairLongClick: (Long) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val today = remember { LocalDate.now(ZoneId.systemDefault()) }
    val grouped =
        remember(pairs, sortOrder) {
            pairs
                .groupBy { it.beforeTimestamp.toLocalDate() }
                .toSortedMap(compareByDescending { it })
                .mapValues { (_, items) ->
                    when (sortOrder) {
                        SortOrder.DESC -> items.sortedByDescending { it.beforeTimestamp }
                        SortOrder.ASC -> items.sortedBy { it.beforeTimestamp }
                    }
                }
        }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        grouped.forEach { (date, datePairs) ->
            item(
                key = "header_$date",
                span = { GridItemSpan(maxLineSpan) },
            ) {
                Text(
                    text = formatDateLabel(date, today),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp),
                )
            }
            items(
                items = datePairs,
                key = { it.id },
            ) { pair ->
                PairCard(
                    pair = pair,
                    isSelected = pair.id in selectedIds,
                    isSelectionMode = selectionMode,
                    onClick = { onPairClick(pair.id) },
                    onLongPress = { onPairLongClick(pair.id) },
                )
            }
        }
    }
}
