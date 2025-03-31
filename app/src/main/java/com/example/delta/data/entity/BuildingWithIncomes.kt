package com.example.delta.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class BuildingWithIncomes(
    @Embedded val building: Buildings, // The building entity
    @Relation(
        parentColumn = "buildingId", // Foreign key in the Incomes table refers to this column in Buildings table.
        entityColumn = "buildingId"   // This column in Incomes table references the parent.
    )
    val incomes: List<Income> // List of incomes related to this building.
)
