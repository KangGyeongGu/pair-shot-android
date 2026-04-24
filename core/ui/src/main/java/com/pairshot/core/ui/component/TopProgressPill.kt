package com.pairshot.core.ui.component

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pairshot.core.designsystem.LocalPairShotExtendedColors
import com.pairshot.core.designsystem.PairShotSpacing

@Composable
fun TopProgressPill(
    label: String,
    progress: Float,
    modifier: Modifier = Modifier,
    progressText: String = "",
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing),
        label = "top_progress_pill",
    )

    val extendedColors = LocalPairShotExtendedColors.current
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = SnackbarBackground,
        shadowElevation = PairShotSpacing.snackbarElevation,
        modifier =
            modifier
                .widthIn(max = PairShotSpacing.progressPillMaxWidth)
                .heightIn(min = PairShotSpacing.snackbarMinHeight),
    ) {
        Column(
            modifier =
                Modifier.padding(
                    horizontal = PairShotSpacing.snackbarHorizontalPadding,
                    vertical = PairShotSpacing.snackbarVerticalPadding,
                ),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = label,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (progressText.isNotEmpty()) {
                    Text(
                        text = progressText,
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(999.dp)),
                color = extendedColors.success,
                trackColor = Color.White.copy(alpha = 0.2f),
            )
        }
    }
}
