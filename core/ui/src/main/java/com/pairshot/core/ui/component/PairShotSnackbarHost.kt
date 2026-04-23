package com.pairshot.core.ui.component

import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PairShotSnackbarHost(
    controller: PairShotSnackbarController,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = controller.hostState,
        modifier = modifier,
        snackbar = { data ->
            PairShotSnackbarContent(data, controller.currentVariant)
        },
    )
}
