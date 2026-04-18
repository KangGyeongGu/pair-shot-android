package com.pairshot.feature.export.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.pairshot.core.designsystem.PairShotSpacing

@Composable
internal fun ExportActionSection(
    onSaveToDevice: () -> Unit,
    onShare: () -> Unit,
) {
    Text(
        text = "내보내기 방식",
        style = MaterialTheme.typography.titleMedium,
    )
    Spacer(modifier = Modifier.size(PairShotSpacing.itemGap))
    Column(
        verticalArrangement = Arrangement.spacedBy(PairShotSpacing.itemGap),
    ) {
        ExportActionCard(
            icon = Icons.Default.Save,
            label = "기기에 저장",
            onClick = onSaveToDevice,
        )
        ExportActionCard(
            icon = Icons.Default.Share,
            label = "공유",
            onClick = onShare,
        )
    }
}

@Composable
private fun ExportActionCard(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = onClick,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(PairShotSpacing.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(PairShotSpacing.iconSize),
                tint = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.width(PairShotSpacing.iconTextGap))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
