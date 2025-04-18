package com.example.delta.init

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class CurvedBottomNavShape(
    private val curveHeight: Float = 20f,
    private val curveWidth: Float = 150f
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Generic(Path().apply {
            moveTo(0f, size.height)
            lineTo(0f, curveHeight)
            quadraticBezierTo(
                size.width / 2 - curveWidth / 2,
                curveHeight,
                size.width / 2,
                0f
            )
            quadraticBezierTo(
                size.width / 2 + curveWidth / 2,
                curveHeight,
                size.width,
                curveHeight
            )
            lineTo(size.width, size.height)
            close()
        })
    }
}
