package com.studentmanagement.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDarkTheme,
    onPrimary = Color(0xFF000000),
    primaryContainer = PrimaryDark,
    onPrimaryContainer = PrimaryLight,
    secondary = SecondaryDarkTheme,
    onSecondary = Color(0xFF000000),
    secondaryContainer = SecondaryDark,
    onSecondaryContainer = SecondaryLight,
    tertiary = TertiaryDarkTheme,
    onTertiary = Color(0xFF000000),
    tertiaryContainer = TertiaryDark,
    onTertiaryContainer = TertiaryLight,
    error = ErrorColor,
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFF8B0000),
    onErrorContainer = Color(0xFFFFDADA),
    background = BackgroundDark,
    onBackground = Color(0xFFFFFFFF),
    surface = SurfaceDark,
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFFCACACB),
    outline = Color(0xFF949494)
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = PrimaryLight,
    onPrimaryContainer = PrimaryDark,
    secondary = Secondary,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = SecondaryLight,
    onSecondaryContainer = SecondaryDark,
    tertiary = Tertiary,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = TertiaryLight,
    onTertiaryContainer = TertiaryDark,
    error = ErrorColor,
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDADA),
    onErrorContainer = Color(0xFF8B0000),
    background = Background,
    onBackground = Color(0xFF000000),
    surface = Surface,
    onSurface = Color(0xFF000000),
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = Color(0xFF6B7280),
    outline = Outline
)

@Composable
fun StudentManagementTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view)?.isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
