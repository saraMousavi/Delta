package com.example.delta.data.entity

import androidx.room.Embedded
import androidx.room.Relation


data class BuildingWithUsage(
    @Embedded val building: Buildings,
    @Relation(
        parentColumn = "buildingUsageId",
        entityColumn = "id",
        entity = BuildingUsages::class
    )
    val buildingUsages: BuildingUsages?
)