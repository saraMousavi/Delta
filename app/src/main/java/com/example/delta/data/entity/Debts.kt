package com.example.delta.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "debts",
    foreignKeys = [
        ForeignKey(
            entity = Costs::class,
            parentColumns = ["id"],
            childColumns = ["costId"],
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = Units::class,
            parentColumns = ["unitId"],
            childColumns = ["unitId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Buildings::class,
            parentColumns = ["buildingId"],
            childColumns = ["buildingId"],
            onDelete = CASCADE
        )
    ],
    indices = [
        Index("costId"),
        Index("unitId")
    ]
)
data class Debts(
    @PrimaryKey(autoGenerate = true)
    val debtId: Long = 0,

    @ColumnInfo(name = "unitId") val unitId: Long,

    @ColumnInfo(name = "costId") val costId: Long, // Foreign key referencing Costs

    @ColumnInfo(name = "buildingId") val buildingId: Long, // Foreign Key referencing Buildings

    @ColumnInfo(name = "description") val description: String, // Description of the debt

    @ColumnInfo(name = "due_date") val dueDate: String, // Due date of the debt

    @ColumnInfo(name = "amount") val amount: Double,

    @ColumnInfo(name = "payment_flag") var paymentFlag: Boolean = false // Indicates whether the debt has been paid

)
