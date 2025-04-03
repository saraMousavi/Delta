package com.example.delta.data.entity

import androidx.room.Embedded
import androidx.room.Relation


data class BuildingWithType(
    @Embedded val building: Buildings,
    @Relation(
        parentColumn = "buildingTypeId",
        entityColumn = "id",
        entity = BuildingTypes::class
    )
    val buildingTypes: BuildingTypes?
)