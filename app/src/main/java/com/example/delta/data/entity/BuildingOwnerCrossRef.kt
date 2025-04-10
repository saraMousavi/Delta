package com.example.delta.data.entity

import androidx.room.Entity

@Entity(primaryKeys = ["buildingId", "ownerId"])
data class BuildingOwnerCrossRef(
    val buildingId: Long,
    val ownerId: Long
)
