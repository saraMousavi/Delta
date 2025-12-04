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
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = CityComplexes::class,
            parentColumns = ["complexId"],
            childColumns = ["complexId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("buildingTypeId"),
        Index("buildingUsageId"),
        Index("userId"),
        Index("complexId"),
        Index(value = ["serialNumber"], unique = true)
    ]
)
@Parcelize
data class Buildings(
    @PrimaryKey(autoGenerate = false) val buildingId: Long = 0,
    val complexId: Long? = null,
    val name: String,
    val serialNumber: String,
    val floorCount: Int,
    val postCode: String,
    val street: String,
    val province: String = "Tehran",
    val state: String = "Central",
    val buildingTypeId: Long? = null,
    val buildingUsageId: Long? = null,
    val fund: Double,
    val userId: Long
) : Parcelable

