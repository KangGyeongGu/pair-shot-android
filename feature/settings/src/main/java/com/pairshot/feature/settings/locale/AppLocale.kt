package com.pairshot.feature.settings.locale

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

enum class AppLocale(
    val tag: String?,
) {
    SYSTEM(null),
    KOREAN("ko"),
    ENGLISH("en"),
}

fun AppLocale.apply() {
    val locales =
        when (tag) {
            null -> LocaleListCompat.getEmptyLocaleList()
            else -> LocaleListCompat.forLanguageTags(tag)
        }
    AppCompatDelegate.setApplicationLocales(locales)
}

fun currentAppLocale(): AppLocale {
    val applied = AppCompatDelegate.getApplicationLocales()
    if (applied.isEmpty) return AppLocale.SYSTEM
    val primaryTag = applied.toLanguageTags().substringBefore('-').lowercase()
    return AppLocale.entries.firstOrNull { it.tag == primaryTag } ?: AppLocale.SYSTEM
}
