package com.pairshot.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class SnackbarVariant { SUCCESS, INFO, WARNING, ERROR }

private val SnackbarBackground = Color(0xF01C1C1E)

private fun dotColor(variant: SnackbarVariant): Color =
    when (variant) {
        SnackbarVariant.SUCCESS -> Color(0xFF30D158)
        SnackbarVariant.INFO -> Color(0xFF0A84FF)
        SnackbarVariant.WARNING -> Color(0xFFFF9F0A)
        SnackbarVariant.ERROR -> Color(0xFFFF453A)
    }

@Composable
fun PairShotSnackbar(
    message: String,
    variant: SnackbarVariant = SnackbarVariant.INFO,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = SnackbarBackground,
        shadowElevation = 8.dp,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(8.dp)
                        .background(dotColor(variant), CircleShape),
            )
            Text(
                text = message,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
            )
            if (actionLabel != null && onAction != null) {
                TextButton(onClick = onAction) {
                    Text(
                        text = actionLabel,
                        color = dotColor(variant),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
