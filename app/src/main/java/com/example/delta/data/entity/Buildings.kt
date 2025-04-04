package com.example.delta.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "buildings",
    foreignKeys = [
        ForeignKey(
            entity = BuildingTypes::class,
            parentColumns = ["id"],
            childColumns = ["buildingTypeId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = BuildingUsages::class,
            parentColumns = ["id"],
            childColumns = ["buildingUsageId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("buildingTypeId"),
        Index("buildingUsageId")
    ]
)
@Parcelize
data class Buildings(
    @PrimaryKey(autoGenerate = true) val buildingId: Long = 0,
    val name: String,
    val ownerName: String,
    val phone: String,
    val email: String,
    val nationalCode: String,
    val postCode: String,
    val address: String,
    val fundNumber: Long,
    val currentBalance: Long,
    val buildingTypeId: Int? = null,
    val buildingUsageId: Int? = null
) : Parcelable
