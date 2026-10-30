package com.example.ui.theme

import android.content.Context
import androidx.compose.runtime.compositionLocalOf

enum class AppThemeMode(val titleEn: String, val titleAr: String) {
    SYSTEM("System Default", "تلقائي حسب النظام"),
    LIGHT("Light Mode", "الوضع الفاتح"),
    DARK("Dark Mode", "الوضع الداكن")
}

val LocalAppThemeMode = compositionLocalOf { AppThemeMode.SYSTEM }

object ThemeManager {
    private const val PREFS_NAME = "app_theme_preferences"
    private const val KEY_THEME_MODE = "key_selected_theme_mode"

    fun getSavedThemeMode(context: Context): AppThemeMode {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name)
        return try {
            AppThemeMode.valueOf(name ?: AppThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            AppThemeMode.SYSTEM
        }
    }

    fun saveThemeMode(context: Context, mode: AppThemeMode) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME_MODE, mode.name)
            .apply()
    }
}
