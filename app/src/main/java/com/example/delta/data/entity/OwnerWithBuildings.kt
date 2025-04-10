package com.example.delta.data.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class OwnerWithBuildings(
    @Embedded val owner: Owners,
    @Relation(
        parentColumn = "ownerId",
        entityColumn = "buildingId",
        associateBy = Junction(BuildingOwnerCrossRef::class)
    )
    val buildings: List<Buildings>
)

