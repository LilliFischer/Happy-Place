package com.example.happyplaces.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
// import androidx.compose.material3.darkColorScheme // Keep if you plan a dark theme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color // Make sure Color is imported
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat

// Your custom colors from Color.kt are used here
private val AppLightColorScheme = lightColorScheme(
    primary = AccentRed,
    onPrimary = TextOnAccent,

    secondary = AccentRed,
    onSecondary = TextOnAccent,

    tertiary = AccentRed,
    onTertiary = TextOnAccent,

    error = Color(0xFFB00020),
    onError = Color.White,
    errorContainer = Color(0xFFFCD8DF),
    onErrorContainer = Color(0xFF141213),

    background = BackgroundLightPink, // <--- This will be used for Scaffold background
    onBackground = TextPrimaryDark,

    surface = SurfaceCream,
    onSurface = TextPrimaryDark,

    primaryContainer = SurfaceYellow, // Used for other things, not status bar in this option
    onPrimaryContainer = TextPrimaryDark,

    secondaryContainer = SurfaceYellow.copy(alpha = 0.8f),
    onSecondaryContainer = TextPrimaryDark,

    tertiaryContainer = SurfaceYellow.copy(alpha = 0.6f),
    onTertiaryContainer = TextPrimaryDark,

    surfaceVariant = BackgroundLightPink.copy(alpha = 0.8f),
    onSurfaceVariant = TextSecondaryDark,

    outline = AccentRed.copy(alpha = 0.5f)
)

/*
// --- OPTIONAL DARK THEME (Define if needed later) ---
private val AppDarkColorScheme = darkColorScheme(
    // ... your dark theme colors ...
    background = Color(0xFF121212), // Example
    // ...
)
*/

@Composable
fun HappyPlacesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to always use your custom scheme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> AppLightColorScheme // Default to your custom light scheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Set status bar color to match the theme's background color
            window.statusBarColor = colorScheme.background.toArgb()

            // Set status bar icons to be dark or light based on the background color's luminance
            // If colorScheme.background (BackgroundLightPink) is light, use dark icons.
            // A simple check (you might want a more robust luminance calculation if you have many theme backgrounds)
            // ColorUtils.calculateLuminance expects an @ColorInt (packed int)
            val isLightStatusBar = ColorUtils.calculateLuminance(colorScheme.background.toArgb()) > 0.5
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isLightStatusBar

            // Optional: Set navigation bar color similarly if you want it to be solid
            // window.navigationBarColor = colorScheme.background.toArgb()
            // WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = isLightStatusBar
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography, // Your AppTypography from Type.kt
        shapes = AppShapes, // Your AppShapes from Shapes.kt
        content = content
    )
}

