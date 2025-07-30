package com.example.delta.data.entity

import androidx.room.Entity

@Entity(
    tableName = "on_boarding_page"
)
data class OnboardingPage(
    val imageRes: Int,
    val title: String,
    val description: String
)
