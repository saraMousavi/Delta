package com.example.delta.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "phonebook_entries")
data class PhonebookEntry(
    @PrimaryKey(autoGenerate = true) val entryId: Long = 0,
    val buildingId: Long,
    val name: String,
    val phoneNumber: String,
    val type: String, // "resident" or "emergency"
    val unitId: Long? = null, // Link to units if resident
    val isEmergency: Boolean = false
)


