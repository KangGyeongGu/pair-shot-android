package com.pairshot.core.ui.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.pairshot.core.designsystem.ModalShape

private val DialogHorizontalMargin = 32.dp
private val DialogConfirmMaxWidth = 360.dp
private val DialogOptionMaxWidth = 480.dp

@Composable
fun confirmDialogWidth(): Dp {
    val sw = LocalConfiguration.current.screenWidthDp.dp
    return (sw - DialogHorizontalMargin).coerceAtMost(DialogConfirmMaxWidth)
}

@Composable
fun optionDialogWidth(): Dp {
    val sw = LocalConfiguration.current.screenWidthDp.dp
    return (sw - DialogHorizontalMargin).coerceAtMost(DialogOptionMaxWidth)
}

@Composable
fun inputDialogWidth(): Dp {
    val sw = LocalConfiguration.current.screenWidthDp.dp
    return (sw - DialogHorizontalMargin).coerceAtMost(DialogOptionMaxWidth)
}

@Composable
fun PairShotDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    properties: DialogProperties = DialogProperties(),
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        icon = icon,
        title = title,
        text = text,
        modifier = modifier,
        shape = ModalShape,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = if (isSystemInDarkTheme()) 14.dp else 8.dp,
        properties = properties,
    )
}
