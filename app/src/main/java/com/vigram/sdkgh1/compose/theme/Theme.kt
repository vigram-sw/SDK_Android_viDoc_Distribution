package com.vigram.sdkgh1.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat


private val LightColorScheme = lightColorScheme(
    background = Color(0xFFF5F5F5),
    surface = Color(0xFFFFFFFF),
    primary = Color(0xFF3B5998),
    secondary = Color(0xFFD32F2F),
    onBackground = Color(0xFF212121),
    onSurface = Color(0xFF212121),
//    surfaceContainerLow = Color(0xFFE7E0EC)

    onPrimary = Color.White,          // Текст на кнопках (белый)
    onSecondary = Color.Black,
    onTertiary = Color(0xFF0D47A1),   // Текст на третьестепенных элементах (тёмно-синий)
)



private val DarkColorScheme = darkColorScheme(
    background = Color(0xFF989898),
    surface = Color(0xFF1E1E1E),
    primary = Color(0xFF3B5998),
    secondary = Color(0xFFD32F2F),
    onBackground = Color(0xFFE0E0E0),
    onSurface = Color(0xFFFFFFFF),

    onPrimary = Color.White,          // Текст на кнопках (белый)
    onSecondary = Color.Black,
    onTertiary = Color(0xFF0D47A1),   // Текст на третьестепенных элементах (тёмно-синий)
)



@Composable
fun BlueTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography, // Можно настроить
        content = content
    )
}