package com.example.delta.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "units",
    foreignKeys = [
        ForeignKey(
            entity = Buildings::class,
            parentColumns = ["buildingId"],
            childColumns = ["buildingId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Units(
    @PrimaryKey(autoGenerate = true)
    val unitId: Long = 0,

    val buildingId: Long, // Foreign key referencing Buildings

    val unitNumber: Int, // Unit number within the building

    val metrage: Double, // Metrage of the unit

    val ownerName: String, // Name of the unit owner

    val tenantName: String, // Name of the unit tenant

    val numberOfTenants: Int = 0 // Number of tenants in the unit
) : Parcelable
