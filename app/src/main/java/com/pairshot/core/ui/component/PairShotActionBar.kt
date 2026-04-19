package com.pairshot.core.ui.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pairshot.core.designsystem.PairShotSpacing
import com.pairshot.core.designsystem.PairShotTypographyTokens

private const val DisabledAlpha = 0.38f

@Composable
fun PairShotActionBar(content: @Composable RowScope.() -> Unit) {
    val barColor =
        if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainer
    Surface(color = barColor, tonalElevation = 0.dp) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .height(PairShotSpacing.actionBar)
                    .padding(horizontal = PairShotSpacing.screenPadding),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PairShotSpacing.sectionGap, Alignment.CenterHorizontally),
                content = content,
            )
        }
    }
}

@Composable
fun PairShotActionBarItem(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    labelColor: Color = Color.Unspecified,
    icon: @Composable () -> Unit,
) {
    // label 색상: 명시된 경우 비활성 시 alpha 적용, 미지정 시 onSurface로 fallback 후 dimming
    val resolvedLabelColor =
        when {
            !enabled && labelColor != Color.Unspecified -> labelColor.copy(alpha = DisabledAlpha)
            !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = DisabledAlpha)
            else -> labelColor
        }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(-6.dp),
    ) {
        IconButton(onClick = onClick, enabled = enabled) {
            icon()
        }
        Text(
            text = label,
            style = PairShotTypographyTokens.labelExtraSmall,
            color = resolvedLabelColor,
        )
    }
}
