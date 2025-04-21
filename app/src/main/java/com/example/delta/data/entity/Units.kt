package com.example.delta.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
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
        ),
        ForeignKey(
            entity = Owners::class,
            parentColumns = ["ownerId"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("buildingId"),
        Index("ownerId")
    ]
)
data class Units(
    @PrimaryKey(autoGenerate = true) val unitId: Long = 0,
    @ColumnInfo(name = "buildingId") var buildingId: Long? = null,
    @ColumnInfo(name = "ownerId") val ownerId: Long? = null,
    @ColumnInfo(name = "unit_number") val unitNumber: String,
    @ColumnInfo(name = "area") val area: String,
    @ColumnInfo(name = "number_of_room") val numberOfRooms: String,
    @ColumnInfo(name = "number_of_parking") val numberOfParking: String

) : Parcelable
