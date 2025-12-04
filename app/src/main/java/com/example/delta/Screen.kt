package com.example.delta

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector,
                    val isCenter: Boolean = false) {
    data object Home : Screen("home", "خانه", Icons.Default.Home)
    data object Dashboard : Screen("dashboard", "داشبورد", Icons.Default.BarChart)
    data object Add : Screen("add_placeholder", "ثبت ساختمان", Icons.Default.Add, true) // Fake route
    data object Chat : Screen("chat", "ارتباط با مدیریت", Icons.Default.Chat)
    data object Settings : Screen("settings", "تنظیمات", Icons.Default.Settings)

    companion object {
        val items = listOf(Home,Dashboard, Add, Chat, Settings)
    }
}

