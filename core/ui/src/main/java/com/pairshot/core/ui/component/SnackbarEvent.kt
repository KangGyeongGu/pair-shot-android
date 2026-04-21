package com.pairshot.core.ui.component

data class SnackbarEvent(
    val message: String,
    val variant: SnackbarVariant = SnackbarVariant.INFO,
    val actionLabel: String? = null,
)
