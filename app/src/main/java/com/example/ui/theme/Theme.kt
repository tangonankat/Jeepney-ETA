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
    primary = TransitBlueDark,
    onPrimary = Color(0xFF003554),
    primaryContainer = TransitBlueDarkContainer,
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = SunGoldDark,
    onSecondary = Color(0xFF451A03),
    secondaryContainer = Color(0xFF78350F),
    onSecondaryContainer = Color(0xFFFEF3C7),
    tertiary = FiestaCoralDark,
    onTertiary = Color(0xFF4C0519),
    background = SlateBackgroundDark,
    onBackground = SlateTextPrimaryDark,
    surface = SlateSurfaceDark,
    onSurface = SlateTextPrimaryDark,
    surfaceVariant = SlateSurfaceVariantDark,
    onSurfaceVariant = SlateTextSecondaryDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = TransitBluePrimary,
    onPrimary = TransitBlueOnPrimary,
    primaryContainer = TransitBluePrimaryContainer,
    onPrimaryContainer = TransitBlueOnPrimaryContainer,
    secondary = SunGold,
    onSecondary = Color.White,
    secondaryContainer = SunGoldContainer,
    onSecondaryContainer = SunGoldOnContainer,
    tertiary = FiestaCoral,
    onTertiary = Color.White,
    tertiaryContainer = FiestaCoralContainer,
    background = SlateBackgroundLight,
    onBackground = SlateTextPrimaryLight,
    surface = SlateSurfaceLight,
    onSurface = SlateTextPrimaryLight,
    surfaceVariant = SlateSurfaceVariantLight,
    onSurfaceVariant = SlateTextSecondaryLight
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
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

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

