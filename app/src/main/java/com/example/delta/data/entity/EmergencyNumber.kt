package com.example.delta.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_numbers")
data class EmergencyNumber(
    @PrimaryKey(autoGenerate = true) val emergencyId: Long = 0,
    val buildingId: Long,
    val serviceName: String, // e.g., Fire Department
    val phoneNumber: String
)
