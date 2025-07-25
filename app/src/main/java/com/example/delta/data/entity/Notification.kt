package com.example.delta.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.delta.enums.NotificationType


@Entity(
    tableName = "notification",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["userId"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["userId"])]
)
data class Notification(
    @PrimaryKey(autoGenerate = true) var notificationId: Long = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "message") val message: String,
    @ColumnInfo(name = "type") val type: NotificationType,
    @ColumnInfo(name = "userId") val userId: Long?,
    @ColumnInfo(name = "timestamp") val timestamp: Long
)

