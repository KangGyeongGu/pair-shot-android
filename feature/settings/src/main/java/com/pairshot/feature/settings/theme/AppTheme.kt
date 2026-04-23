package com.pairshot.feature.settings.theme

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

enum class AppTheme(
    val nightMode: Int,
) {
    SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
    LIGHT(AppCompatDelegate.MODE_NIGHT_NO),
    DARK(AppCompatDelegate.MODE_NIGHT_YES),
}

private const val PREF_NAME = "app_theme"
private const val KEY_THEME = "theme"

fun Context.loadAppTheme(): AppTheme {
    val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    val stored = prefs.getString(KEY_THEME, null) ?: return AppTheme.SYSTEM
    return AppTheme.entries.firstOrNull { it.name == stored } ?: AppTheme.SYSTEM
}

fun Context.saveAppTheme(theme: AppTheme) {
    getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_THEME, theme.name)
        .apply()
}

fun AppTheme.applyNightMode() {
    AppCompatDelegate.setDefaultNightMode(nightMode)
}

fun Context.applyAppThemeFromPreference() {
    loadAppTheme().applyNightMode()
}
