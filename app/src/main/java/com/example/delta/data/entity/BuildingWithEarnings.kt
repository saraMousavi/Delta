package com.example.delta.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class BuildingWithEarnings(
    @Embedded val building: Buildings, // The building entity
    @Relation(
        parentColumn = "buildingId", // Foreign key in the Incomes table refers to this column in Buildings table.
        entityColumn = "buildingId",   // This column in Incomes table references the parent.
        entity = Earnings::class
    )
    val earnings: List<Earnings> // List of earnings related to this building.
)
