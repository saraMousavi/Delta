package com.example.delta.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "credits",
    foreignKeys = [
        ForeignKey(
            entity = Earnings::class,
            parentColumns = ["earningsId"],
            childColumns = ["earningsId"],
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = Buildings::class,
            parentColumns = ["buildingId"],
            childColumns = ["buildingId"],
            onDelete = CASCADE
        ),
    ],
    indices = [
        Index("earningsId"),
        Index("buildingId")
    ]
)

data class Credits(
    @PrimaryKey(autoGenerate = true)
    val creditsId: Long = 0,

    @ColumnInfo(name = "earningsId") val earningsId: Long,

    @ColumnInfo(name = "buildingId") val buildingId: Long, // Foreign Key referencing Buildings

    @ColumnInfo(name = "description") val description: String, // Description of the debt

    @ColumnInfo(name = "due_date") val dueDate: String, // Due date of the debt

    @ColumnInfo(name = "amount") val amount: Double,

    @ColumnInfo(name = "receipt_flag") var receiptFlag: Boolean = false // Indicates whether the debt has been paid

)
