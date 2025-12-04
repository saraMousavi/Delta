package com.example.delta.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    primaryKeys = ["ownerId", "unitId"],
    tableName = "owners_units_cross_ref",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Units::class,
            parentColumns = ["unitId"],
            childColumns = ["unitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
       indices = [
        Index("ownerId"),
        Index("unitId")
    ]
)
data class OwnersUnitsCrossRef(
    val ownerId: Long,
    val unitId: Long,
    val dang: Double
)

