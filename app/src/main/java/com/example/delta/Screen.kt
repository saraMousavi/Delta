package com.example.delta

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector,
                    val isCenter: Boolean = false) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Add : Screen("add_placeholder", "Add", Icons.Default.Add, true) // Fake route
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    companion object {
        val items = listOf(Home, Add, Settings)
    }
}

