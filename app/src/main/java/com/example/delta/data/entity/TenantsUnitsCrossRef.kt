package com.example.delta.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    primaryKeys = ["tenantId", "unitId"],
    tableName = "tenants_units_cross_ref",
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
    ],
    indices = [
        Index("tenantId"),
        Index("unitId")
    ]
)
data class TenantsUnitsCrossRef(
    val tenantId: Long,
    val unitId: Long,
    val startDate: String,  // Add relationship-specific fields
    val endDate: String,
    val status: String
)

