package com.pairshot.core.ui.component

import com.pairshot.core.ui.text.UiText

data class SnackbarEvent(
    val message: UiText,
    val variant: SnackbarVariant = SnackbarVariant.INFO,
    val actionLabel: UiText? = null,
) {
    constructor(
        message: String,
        variant: SnackbarVariant = SnackbarVariant.INFO,
        actionLabel: String? = null,
    ) : this(
        message = UiText.Dynamic(message),
        variant = variant,
        actionLabel = actionLabel?.let { UiText.Dynamic(it) },
    )
}
