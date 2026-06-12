package com.example.delta.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.delta.enums.NotificationType


@Entity(
    tableName = "notification"
)
data class Notification(
    @PrimaryKey(autoGenerate = true) var notificationId: Long = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "building_name") val buildingName: String? = "",
    @ColumnInfo(name = "userId") val userId: Long? = 0L,
    @ColumnInfo(name = "message") val message: String,
    @ColumnInfo(name = "type") val type: NotificationType,
    @ColumnInfo(name = "sender_name") val senderName: String? = "",
    @ColumnInfo(name = "buildingId") val buildingId: Long? = 0L,
    @ColumnInfo(name = "timestamp") val timestamp: Long
)

