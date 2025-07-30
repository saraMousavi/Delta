package com.example.delta.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import com.example.delta.enums.FundType
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "funds",
    foreignKeys = [
        ForeignKey(
            entity = Buildings::class,
            parentColumns = ["buildingId"],
            childColumns = ["buildingId"],
            onDelete = CASCADE
        )
])
data class Funds(
    @PrimaryKey(autoGenerate = true) val fundId: Long = 0,
    @ColumnInfo(name = "buildingId") val buildingId: Long,
    @ColumnInfo(name = "fund_type") val fundType: FundType,
    @ColumnInfo(name = "balance") val balance: Double = 0.0
) : Parcelable

