package com.example.delta

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// 1. Define your custom colors
val BluePrimary = Color(0xFF1976D2)
val BluePrimaryDark = Color(0xFF004BA0)
val BluePrimaryLight = Color(0xFF63A4FF)
val OrangeAccent = Color(0xFFFF9800)
val GreenSecondary = Color(0xFF43A047)
val SurfaceGray = Color(0xFFF5F5F5)
val TextPrimary = Color(0xFF222B45)
val TextSecondary = Color(0xFF8F9BB3)
val ErrorRed = Color(0xFFD32F2F)

// 2. Create a Material3 ColorScheme
val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = BluePrimaryLight,
    secondary = GreenSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC8E6C9),
    background = SurfaceGray,
    surface = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = ErrorRed,
    onError = Color.White
)

val DarkColorScheme = darkColorScheme(
    primary = BluePrimaryLight,
    onPrimary = BluePrimaryDark,
    secondary = GreenSecondary,
    onSecondary = Color.Black,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onBackground = Color.White,
    onSurface = Color.White,
    error = Color(0xFFFF6659),
    onError = Color.Black
)

// 3. Custom font family
val customFontFamily = FontFamily(
    Font(R.font.yekan, FontWeight.Normal)
)

// 4. Custom typography
val AppTypography = Typography(
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        fontFamily = customFontFamily
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        fontFamily = customFontFamily
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        fontFamily = customFontFamily
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        fontFamily = customFontFamily
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 30.sp,
        fontFamily = customFontFamily
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        fontFamily = customFontFamily
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        fontFamily = customFontFamily
    ),

    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        fontFamily = customFontFamily
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        fontFamily = customFontFamily
    ),
    // Add more styles if needed
)

// 5. Theme composable with colorScheme and typography
@Composable
fun AppTheme(
    useDarkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (useDarkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}
