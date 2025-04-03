package com.example.delta.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class BuildingWithCosts(
    @Embedded val building: Buildings, // The building entity
    @Relation(
        parentColumn = "buildingId", // Foreign key in the Costs table refers to this column in Buildings table.
        entityColumn = "buildingId",   // This column in Costs table references the parent.
        entity = Costs::class
    )
    val costs: List<Costs> // List of costs related to this building.
)
