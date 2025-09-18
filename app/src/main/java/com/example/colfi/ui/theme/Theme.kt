package com.example.colfi.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.colfi.R

private val DarkColorScheme = darkColorScheme(
    primary = LightCream3,
    secondary = LightBrown1,
    tertiary = DarkBrown1,
    background = LightCream2,
    surface = LightCream1,
    onPrimary = DarkBrown1,
    onSecondary = DarkBrown1,
    onTertiary = DarkBrown1,
    onBackground = DarkBrown1,
    onSurface = DarkBrown1
)

private val LightColorScheme = lightColorScheme(
    primary = LightCream3,
    secondary = LightBrown1,
    tertiary = DarkBrown1,
    background = LightCream2,
    surface = LightCream1,
    onPrimary = DarkBrown1,
    onSecondary = DarkBrown1,
    onTertiary = DarkBrown1,
    onBackground = DarkBrown1,
    onSurface = DarkBrown1
)

// Define custom font family
val colfiFont = FontFamily(Font(R.font.colfifont))

@Composable
fun ColfiTheme(
    darkTheme: Boolean = false, // Always use light theme
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disable dynamic colors to ensure consistent theming
    content: @Composable () -> Unit
) {
    // Always use LightColorScheme regardless of system theme or dynamic colors
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ColfiTypography,
        content = content
    )
}