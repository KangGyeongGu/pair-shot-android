package com.pairshot.feature.settings.theme

import androidx.appcompat.app.AppCompatDelegate

enum class AppTheme(
    val nightMode: Int,
) {
    SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
    LIGHT(AppCompatDelegate.MODE_NIGHT_NO),
    DARK(AppCompatDelegate.MODE_NIGHT_YES),
    ;

    fun apply() {
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    companion object {
        fun fromName(name: String?): AppTheme = entries.firstOrNull { it.name == name } ?: SYSTEM
    }
}
