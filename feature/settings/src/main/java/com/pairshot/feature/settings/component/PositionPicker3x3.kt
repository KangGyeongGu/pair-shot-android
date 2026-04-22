package com.pairshot.feature.settings.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.pairshot.core.designsystem.PairShotSpacing

/**
 * 3x3 격자로 9개 포지션 중 하나를 선택하는 공용 피커.
 * Watermark 로고 위치 / Combine 레이블 앵커 등에서 공통 사용.
 *
 * @param positions 9개 포지션 (행 우선 순서: TL, TC, TR, CL, CC, CR, BL, BC, BR)
 */
@Composable
internal fun <T> PositionPicker3x3Row(
    label: String,
    positions: List<T>,
    selectedPosition: T,
    onPositionChange: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    require(positions.size == 9) { "positions must contain exactly 9 entries" }
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    horizontal = PairShotSpacing.cardPadding,
                    vertical = PairShotSpacing.cardPadding,
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Column(verticalArrangement = Arrangement.spacedBy(PairShotSpacing.iconTextGap)) {
            positions.chunked(3).forEach { rowPositions ->
                Row(horizontalArrangement = Arrangement.spacedBy(PairShotSpacing.iconTextGap)) {
                    rowPositions.forEach { position ->
                        val isSelected = position == selectedPosition
                        Box(
                            modifier =
                                Modifier
                                    .size(PairShotSpacing.iconSize)
                                    .clip(MaterialTheme.shapes.extraSmall)
                                    .background(
                                        if (isSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.surfaceContainerHigh
                                        },
                                    ).semantics { selected = isSelected }
                                    .clickable { onPositionChange(position) },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
