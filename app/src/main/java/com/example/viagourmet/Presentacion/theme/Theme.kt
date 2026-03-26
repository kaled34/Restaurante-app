package com.example.viagourmet.Presentacion.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.viagourmet.Presentacion.theme.Brown40
import com.example.viagourmet.Presentacion.theme.Brown60
import com.example.viagourmet.Presentacion.theme.Brown80
import com.example.viagourmet.Presentacion.theme.Cream
import com.example.viagourmet.Presentacion.theme.RedError
import com.example.viagourmet.Presentacion.theme.TextPrimary

private val LightColorScheme = lightColorScheme(
    primary = Brown80,
    onPrimary = Color.White,
    primaryContainer = Brown40,
    onPrimaryContainer = TextPrimary,
    secondary = Brown60,
    onSecondary = Color.White,
    background = Cream,
    onBackground = TextPrimary,
    surface = Color.White,
    onSurface = TextPrimary,
    error = RedError,
    onError = Color.White
)

@Composable
fun ViaGourmetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography(),
        content = content
    )
}

// Tipografía personalizada
@Composable
fun Typography() = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 14.sp
    )
)