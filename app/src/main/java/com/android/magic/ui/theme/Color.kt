package com.android.magic.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Purple200 = Color(0xFFBB86FC)
val Purple500 = Color(0xFF6200EE)
val Purple700 = Color(0xFF3700B3)
val Teal200 = Color(0xFF03DAC5)
val ExtraLightBlue = Color(0xFFF8F9FF)
val colorWhite = Color(0xFFFFF9F9)
val colorBlue = Color(0xFF102D86)
val colorGray = Color(0xFF6D6D6D)

@Composable
fun getColor(light: Color, dark: Color): Color {
    return if (isSystemInDarkTheme()) dark else light
}

val Colors.primaryTextColor: Color
    @Composable
    get() = getColor(light = Color.Black, dark = Color.White)

val Colors.secondaryTextColor: Color
    @Composable
    get() = getColor(light = Color.Gray, dark = Color.LightGray)

val Colors.dividerColor: Color
    @Composable
    get() = getColor(light = Color.Black, dark = Color.White)
