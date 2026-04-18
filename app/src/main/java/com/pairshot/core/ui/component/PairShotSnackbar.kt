package com.pairshot.core.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pairshot.core.designsystem.LocalPairShotExtendedColors
import com.pairshot.core.designsystem.ModalShape

enum class SnackbarVariant { SUCCESS, INFO, WARNING, ERROR }

@Composable
fun PairShotSnackbar(
    message: String,
    variant: SnackbarVariant = SnackbarVariant.INFO,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val extended = LocalPairShotExtendedColors.current
    val colorScheme = MaterialTheme.colorScheme

    val (containerColor, contentColor, icon) =
        when (variant) {
            SnackbarVariant.SUCCESS -> {
                Triple(
                    extended.success.copy(alpha = 0.15f),
                    extended.success,
                    Icons.Filled.CheckCircle,
                )
            }

            SnackbarVariant.INFO -> {
                Triple(
                    colorScheme.primaryContainer,
                    colorScheme.onPrimaryContainer,
                    Icons.Filled.Info,
                )
            }

            SnackbarVariant.WARNING -> {
                Triple(
                    extended.warning.copy(alpha = 0.15f),
                    extended.warning,
                    Icons.Filled.Warning,
                )
            }

            SnackbarVariant.ERROR -> {
                Triple(
                    colorScheme.errorContainer,
                    colorScheme.onErrorContainer,
                    Icons.Filled.Error,
                )
            }
        }

    Surface(
        modifier = modifier.heightIn(min = 52.dp),
        shape = ModalShape,
        color = containerColor,
        border = BorderStroke(1.dp, colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                maxLines = 2,
                modifier = Modifier.weight(1f),
            )
            if (actionLabel != null && onAction != null) {
                TextButton(onClick = onAction) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = contentColor,
                    )
                }
            }
        }
    }
}
