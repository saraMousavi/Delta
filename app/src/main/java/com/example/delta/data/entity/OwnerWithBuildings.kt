package com.example.delta.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.Relation

@Entity(
    primaryKeys = ["ownerId", "buildingId"],
    tableName = "owners_with_building",
    foreignKeys = [
        ForeignKey(
            entity = Owners::class,
            parentColumns = ["ownerId"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Buildings::class,
            parentColumns = ["buildingId"],
            childColumns = ["buildingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("ownerId"),
        Index("buildingId")
    ]
)
data class OwnerWithBuildings(
    val ownerId: Long,
    val buildingId: Long
)

