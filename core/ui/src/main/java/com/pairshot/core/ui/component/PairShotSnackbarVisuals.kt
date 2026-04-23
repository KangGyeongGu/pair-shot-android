package com.pairshot.core.ui.component

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals

internal class PairShotSnackbarVisuals(
    val event: SnackbarEvent,
) : SnackbarVisuals {
    override val message: String = ""
    override val actionLabel: String? = if (event.actionLabel != null) "" else null
    override val withDismissAction: Boolean = false
    override val duration: SnackbarDuration =
        if (event.actionLabel != null) SnackbarDuration.Long else SnackbarDuration.Short
}
