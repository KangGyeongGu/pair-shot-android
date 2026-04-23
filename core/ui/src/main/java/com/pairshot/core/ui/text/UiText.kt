package com.pairshot.core.ui.text

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource

sealed class UiText {
    data class Resource(
        @param:StringRes val resId: Int,
        val args: List<Any> = emptyList(),
    ) : UiText()

    data class Plural(
        @param:PluralsRes val resId: Int,
        val count: Int,
        val args: List<Any> = emptyList(),
    ) : UiText()

    data class Dynamic(
        val value: String,
    ) : UiText()

    @Composable
    fun asString(): String =
        when (this) {
            is Resource -> stringResource(resId, *args.toTypedArray())
            is Plural -> pluralStringResource(resId, count, *args.toTypedArray())
            is Dynamic -> value
        }

    fun asString(context: Context): String =
        when (this) {
            is Resource -> context.getString(resId, *args.toTypedArray())
            is Plural -> context.resources.getQuantityString(resId, count, *args.toTypedArray())
            is Dynamic -> value
        }
}
