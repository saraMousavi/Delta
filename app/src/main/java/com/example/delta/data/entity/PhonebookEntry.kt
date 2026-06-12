package com.example.delta.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "phonebook_entries")
data class PhonebookEntry(
    @PrimaryKey val entryId: Long = 0L,
    val buildingId: Long,
    val userId: Long? = null,
    val name: String,
    val phoneNumber: String,
    val type: String,
    val isEmergency: Boolean = false,
    val unitId: Long? = null,
    val roleLabel: String? = null,
    val roles: List<PhonebookRole> = emptyList()
)

data class PhonebookRole(
    val unitId: Long,
    val role: String,
    val roleLabel: String,
    val unitNumber: String? = null
)



