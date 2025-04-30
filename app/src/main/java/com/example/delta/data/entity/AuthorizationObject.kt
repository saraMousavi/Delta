package com.example.delta.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// AuthorizationObject.kt
@Entity(tableName = "authorization_objects")
data class AuthorizationObject(
    @PrimaryKey(autoGenerate = true)
    val objectId: Long = 0,
    val name: Int, // e.g., "HomePageActivity", "BuildingFormActivity"
    val description: Int
)
