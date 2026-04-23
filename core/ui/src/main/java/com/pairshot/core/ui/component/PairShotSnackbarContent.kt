package com.pairshot.core.ui.component

import androidx.compose.material3.SnackbarData
import androidx.compose.runtime.Composable

@Composable
fun PairShotSnackbarContent(
    data: SnackbarData,
    variant: SnackbarVariant,
) {
    val visuals = data.visuals
    val message: String
    val actionLabel: String?
    if (visuals is PairShotSnackbarVisuals) {
        message = visuals.event.message.asString()
        actionLabel = visuals.event.actionLabel?.asString()
    } else {
        message = visuals.message
        actionLabel = visuals.actionLabel
    }
    PairShotSnackbar(
        message = message,
        variant = variant,
        actionLabel = actionLabel,
        onAction = { data.performAction() },
    )
}
