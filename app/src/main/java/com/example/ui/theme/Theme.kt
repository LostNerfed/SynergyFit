package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

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

fun Modifier.liquidGlassModifier(
    shape: Shape = RoundedCornerShape(20.dp),
    borderAlpha: Float = 0.06f
): Modifier = this
    .clip(shape)
    .background(Color.White.copy(alpha = 0.03f))
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = borderAlpha),
                Color.Transparent,
                Color.White.copy(alpha = 0.02f)
            )
        ),
        shape = shape
    )

@Composable
fun SynergyBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF040A18), // Dark cosmic blue
                        Color(0xFF000000)  // Pitch black
                    )
                )
            )
    ) {
        content()
    }
}

fun Modifier.bounceClick(
    scaleDown: Float = 0.95f,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) scaleDown else 1f, label = "bounce")

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = androidx.compose.foundation.LocalIndication.current,
            onClick = onClick
        )
}
