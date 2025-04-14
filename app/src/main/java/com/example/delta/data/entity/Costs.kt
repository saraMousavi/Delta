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
    tableName = "costs"
)
data class Costs(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "cost_name") val costName: String,
    @ColumnInfo(name = "buildingId") val buildingId: Long, // Foreign key reference
    @ColumnInfo(name = "amount") val amount: Double,
    @ColumnInfo(name = "currency") val currency: String,
    @ColumnInfo(name = "period") val period: String,
    @ColumnInfo(name = "amount_unit") val amountUnit: String
) : Parcelable
