package com.example.delta.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    primaryKeys = ["userId", "notificationId"],
    tableName = "users_notification_cross_ref",
    foreignKeys = [
        ForeignKey(
            entity = Notification::class,
            parentColumns = ["notificationId"],
            childColumns = ["notificationId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("userId"),
        Index("notificationId")
    ]
)
data class UsersNotificationCrossRef(
    val userId:Long,
    val notificationId:Long,
    var isRead:Boolean
)
