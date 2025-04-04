package com.example.delta.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "debts",
    foreignKeys = [
        ForeignKey(
            entity = Costs::class,
            parentColumns = ["id"],
            childColumns = ["costId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Units::class,
            parentColumns = ["unitId"],
            childColumns = ["unitId"],
            onDelete = ForeignKey.CASCADE
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

    val unitId: Long,

    val costId: Long, // Foreign key referencing Costs

    val description: String, // Description of the debt

    val dueDate: String, // Due date of the debt

    var paymentFlag: Boolean = false // Indicates whether the debt has been paid
)
