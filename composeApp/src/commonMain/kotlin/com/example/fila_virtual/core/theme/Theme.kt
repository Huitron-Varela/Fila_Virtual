package com.example.fila_virtual.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.fila_virtual.core.LocalWindowSize

private val LightColorScheme = lightColorScheme(
    primary = PrimaryOrange,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = Color.White,
    onBackground = DarkGray,
    onSurface = DarkGray,
    error = TrafficRed,
    onError = Color.White
)

@Composable
fun FilaVirtualTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val windowSize = LocalWindowSize.current

    // Tipografía adaptativa basada en el tamaño de la ventana
    val typography = Typography(
        displayLarge = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = windowSize.adaptiveSp(32)
        ),
        titleLarge = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = windowSize.adaptiveSp(24)
        ),
        bodyLarge = TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = windowSize.adaptiveSp(16)
        ),
        bodyMedium = TextStyle(
            fontSize = windowSize.adaptiveSp(14)
        ),
        labelSmall = TextStyle(
            fontSize = windowSize.adaptiveSp(12),
            color = MediumGray
        )
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
