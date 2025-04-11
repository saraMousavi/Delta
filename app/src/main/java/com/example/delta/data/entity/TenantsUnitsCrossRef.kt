package com.example.delta.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["tenantId", "unitId"],
    foreignKeys = [
        ForeignKey(
            entity = Tenants::class,
            parentColumns = ["tenantId"],
            childColumns = ["tenantId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Units::class,
            parentColumns = ["unitId"],
            childColumns = ["unitId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TenantsUnitsCrossRef(
    val tenantId: Long,
    val unitId: Long,
    val startDate: String,  // Add relationship-specific fields
    val endDate: String
)

