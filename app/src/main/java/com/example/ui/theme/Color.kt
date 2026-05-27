package com.example.ui.theme

import androidx.compose.ui.graphics.Color

val AmoledBg = Color(0xFF0A0A0A) // 5% less amoled
val AmoledSurface = Color(0xFF141416) // Premium dark gray
val BorderColor = Color(0xFF333333)
val BorderColorSubtle = Color(0xFF222222)
val TextPrincipal = Color(0xFFFFFFFF)
val TextSecundario = Color(0xFF888888)

// Semantic Material 3 monochrome bindings
val MonoPrimary = Color(0xFFFFFFFF)
val MonoSecondary = Color(0xFF888888)
val MonoTertiary = Color(0xFF444444)

val PremiumGradientBorder = androidx.compose.ui.graphics.Brush.verticalGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.15f),
        Color.Transparent
    )
)
