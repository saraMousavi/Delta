package com.example.delta.init

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector


data class NavItem(
    @StringRes val title: Int,
    val icon: ImageVector,
    val onClick: () -> Unit
)
