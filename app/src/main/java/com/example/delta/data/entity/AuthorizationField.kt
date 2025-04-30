package com.example.delta.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// AuthorizationField.kt
@Entity(
    tableName = "authorization_fields",
    foreignKeys = [ForeignKey(
        entity = AuthorizationObject::class,
        parentColumns = ["objectId"],
        childColumns = ["objectId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class AuthorizationField(
    @PrimaryKey(autoGenerate = true)
    val fieldId: Long = 0,
    val objectId: Long,  // Parent object reference
    val name: Int,    // e.g., "fundTab", "phoneNumberField"
    val fieldType: String // "text", "checkbox", "button", etc.
)
