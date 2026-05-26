package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MonoPrimary,
    onPrimary = Color.Black,
    secondary = MonoSecondary,
    onSecondary = Color.White,
    tertiary = MonoTertiary,
    onTertiary = Color.White,
    background = AmoledBg,
    onBackground = TextPrincipal,
    surface = AmoledSurface,
    onSurface = TextPrincipal,
    surfaceVariant = AmoledBg,
    onSurfaceVariant = TextSecundario,
    outline = BorderColor,
    outlineVariant = BorderColorSubtle
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force amoled dark
    dynamicColor: Boolean = false, // Force amoled dark, ignore dynamic color
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
