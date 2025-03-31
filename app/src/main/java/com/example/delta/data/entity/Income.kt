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
    tableName = "incomes",
    foreignKeys = [
        ForeignKey(
            entity = Buildings::class,
            parentColumns = ["buildingId"],
            childColumns = ["buildingId"],
            onDelete = ForeignKey.CASCADE //VERY IMPORTANT to prevent orphaned data
        )
    ],
    indices = [
        Index("buildingId")
    ]
)
data class Income(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "income_name") val incomeName: String,
    @ColumnInfo(name = "buildingId") val buildingId: Long, // Foreign key reference
    @ColumnInfo(name = "amount") val amount: Double,
    @ColumnInfo(name = "currency") val currency: String
) : Parcelable
