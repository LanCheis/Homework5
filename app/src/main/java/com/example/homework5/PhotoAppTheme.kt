package com.homework.photoapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF9C27B0),
    secondary = Color(0xFF9C27B0),
    tertiary = Color(0xFF9C27B0),
    background = Color.White,
    surface = Color.White
)

@Composable
fun PhotoAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}