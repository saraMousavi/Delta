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
            parentColumns = ["buildingTypeId"],
            childColumns = ["buildingTypeId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = BuildingUsages::class,
            parentColumns = ["buildingUsageId"],
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
    val phone: String,
    val email: String,
    val postCode: String,
    val street: String,
    val province: String = "Tehran",  // Default province
    val state: String = "Central",     // Default state
    val buildingTypeId: Long? = null,
    val buildingUsageId: Long? = null
) : Parcelable
