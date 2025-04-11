package com.example.delta.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["ownerId", "unitId"],
    foreignKeys = [
        ForeignKey(
            entity = Owners::class,
            parentColumns = ["ownerId"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Units::class,
            parentColumns = ["unitId"],
            childColumns = ["unitId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class OwnersUnitsCrossRef(
    val ownerId: Long,
    val unitId: Long
)

