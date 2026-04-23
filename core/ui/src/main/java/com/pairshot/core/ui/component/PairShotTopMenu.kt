package com.pairshot.core.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.pairshot.core.designsystem.ModalShape
import com.pairshot.core.designsystem.PairShotSpacing

private const val MENU_WIDTH_FRACTION = 0.48f

@Composable
fun PairShotTopMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val menuWidth =
        (screenWidthDp * MENU_WIDTH_FRACTION)
            .coerceAtLeast(PairShotSpacing.menuMinWidth)
            .coerceAtMost(PairShotSpacing.menuMaxWidth)

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier.width(menuWidth),
        shape = ModalShape,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
        shadowElevation = PairShotSpacing.menuShadow,
        border =
            BorderStroke(
                PairShotSpacing.menuBorderWidth,
                MaterialTheme.colorScheme.outlineVariant,
            ),
        content = content,
    )
}

@Composable
fun PairShotTopMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
) {
    DropdownMenuItem(
        text = text,
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        leadingIcon = leadingIcon,
        enabled = enabled,
    )
}

@Composable
fun PairShotTopMenuItemText(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = titleColor,
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = subtitleColor,
            )
        }
    }
}

@Composable
fun PairShotTopMenuDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(horizontal = PairShotSpacing.itemGap),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}
