package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = StudyBluePrimaryDark,
    onPrimary = Color(0xFF003258),
    primaryContainer = StudyBlueContainerDark,
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = StudyTeal,
    tertiary = StudyAmber,
    background = StudyBackgroundDark,
    surface = StudySurfaceDark,
    surfaceVariant = StudySurfaceVariantDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = StudyBluePrimary,
    onPrimary = StudyBlueOnPrimary,
    primaryContainer = StudyBlueContainer,
    onPrimaryContainer = StudyBlueOnContainer,
    secondary = StudyTeal,
    secondaryContainer = StudyTealContainer,
    onSecondaryContainer = StudyTealOnContainer,
    tertiary = StudyAmber,
    tertiaryContainer = StudyAmberContainer,
    onTertiaryContainer = StudyAmberOnContainer,
    background = StudyBackgroundLight,
    surface = StudySurfaceLight,
    surfaceVariant = StudySurfaceVariantLight
  )

@Composable
fun MyApplicationTheme(
  themeMode: AppThemeMode = AppThemeMode.SYSTEM,
  darkTheme: Boolean = when (themeMode) {
      AppThemeMode.SYSTEM -> isSystemInDarkTheme()
      AppThemeMode.LIGHT -> false
      AppThemeMode.DARK -> true
  },
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  androidx.compose.runtime.CompositionLocalProvider(
    LocalAppThemeMode provides themeMode
  ) {
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
  }
}
