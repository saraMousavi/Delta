package com.example.delta.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    primaryKeys = ["userId", "buildingId"],
    tableName = "users_buildings_cross_ref",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
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
        Index("buildingId"),
        Index("userId")
    ]
)
data class UsersBuildingsCrossRef(
    val userId: Long,
    val buildingId: Long,
    val roleName: String
)

