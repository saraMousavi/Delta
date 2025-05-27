package com.example.delta.data.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(primaryKeys = ["buildingId", "ownerId"],
    indices = [Index("ownerId")]
    , tableName = "building_owner_cross_ref")
data class BuildingOwnerCrossRef(
    val buildingId: Long,
    val ownerId: Long,
    val isManager: Boolean = false
)
