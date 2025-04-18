package com.example.delta

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val customFontFamily = FontFamily(
    androidx.compose.ui.text.font.Font(R.font.yekan, FontWeight.Normal)
)

val AppTypography = Typography( // use Material3 typography
    bodyLarge = TextStyle( // instead of body1
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        fontFamily = customFontFamily
    ),
    bodyMedium = TextStyle( // instead of body1
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        fontFamily = customFontFamily
    ),
    titleMedium = TextStyle( // instead of button
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        fontFamily = customFontFamily
    ),
    bodySmall = TextStyle( // instead of caption
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        fontFamily = customFontFamily
    )
    // Define styles for other text elements as needed
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        typography = AppTypography,
        content = content
    )
}
