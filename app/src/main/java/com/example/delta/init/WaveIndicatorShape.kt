package com.example.delta.init

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.runtime.Immutable  // For @Immutable annotation

// Modified WaveIndicatorShape
@Immutable
class WaveIndicatorShape(
    private val waveHeight: Float = 12f  // Removed unused waveWidth
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ) = Outline.Generic(Path().apply {
        moveTo(0f, size.height)  // Start bottom-left
        lineTo(0f, size.height - waveHeight)  // Move up to wave base

        // Wave curve (simplified)
        quadraticBezierTo(
            size.width * 0.25f,
            size.height - waveHeight * 3,  // Wave peak
            size.width * 0.5f,
            size.height - waveHeight
        )
        quadraticBezierTo(
            size.width * 0.75f,
            size.height - waveHeight * 0.5f,
            size.width,
            size.height - waveHeight
        )

        lineTo(size.width, size.height)  // Bottom-right
        close()
    })
}

