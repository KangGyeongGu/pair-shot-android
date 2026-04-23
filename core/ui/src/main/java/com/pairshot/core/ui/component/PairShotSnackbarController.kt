package com.pairshot.core.ui.component

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult

class PairShotSnackbarController {
    val hostState = SnackbarHostState()
    var currentVariant: SnackbarVariant = SnackbarVariant.INFO
        private set

    suspend fun show(event: SnackbarEvent): SnackbarResult {
        currentVariant = event.variant
        return hostState.showSnackbar(PairShotSnackbarVisuals(event))
    }
}
