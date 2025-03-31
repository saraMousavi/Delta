package com.example.delta.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class BuildingWithCosts(
    @Embedded val building: Buildings, // The building entity
    @Relation(
        parentColumn = "buildingId", // Foreign key in the Costs table refers to this column in Buildings table.
        entityColumn = "buildingId"   // This column in Costs table references the parent.
    )
    val costs: List<Cost> // List of costs related to this building.
)
