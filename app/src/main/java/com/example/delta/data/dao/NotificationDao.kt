package com.example.delta.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.delta.data.entity.Notification
import com.example.delta.data.entity.UsersNotificationCrossRef

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification): Long

    @Insert
    suspend fun insertUsersNotificationCrossRef(crossRef: UsersNotificationCrossRef)


    @Update
    suspend fun updateNotification(notification: Notification)

    @Update
    suspend fun updateUserNotificationCrossRef(usersNotificationCrossRef: UsersNotificationCrossRef)

    @Delete
    suspend fun deleteNotification(notification: Notification)

    @Query("SELECT * FROM notification")
    suspend fun getNotifications(): List<Notification>


    @Query("SELECT * FROM users_notification_cross_ref where userId = :userId")
    suspend fun getUsersNotificationsByUser(userId: Long): List<UsersNotificationCrossRef>

//@todo and userId = :userId
    @Query("SELECT * FROM users_notification_cross_ref where notificationId = :notificationId" +
            " ")
    suspend fun getUsersNotificationsByNotification(notificationId: Long): UsersNotificationCrossRef?

    @Delete
    suspend fun deleteUserNotificationCrossRef(crossRef: UsersNotificationCrossRef)

    @Query("SELECT * FROM users_notification_cross_ref where notificationId = :notificationId")
    suspend fun getUsersNotificationsById(notificationId: Long): UsersNotificationCrossRef

    @Query("SELECT * FROM notification WHERE notificationId IN (:notificationIds)")
    suspend fun getNotificationsByIds(notificationIds: List<Long>): List<Notification>


    @Query("SELECT * FROM notification WHERE notificationId = :notificationIds")
    suspend fun getNotificationsById(notificationIds: Long): Notification

    // Helper function that accepts the list of UsersNotificationCrossRef and extracts the IDs internally:
    suspend fun getNotificationsForCrossRefs(crossRefs: List<UsersNotificationCrossRef>): List<Notification> {
        val notificationIds = crossRefs.map { it.notificationId }
        return getNotificationsByIds(notificationIds)
    }

    //@todo WHERE uncr.userId = :userId userId: Long
    @Query("""
        SELECT n.*, uncr.isRead AS isRead
        FROM notification n
        INNER JOIN users_notification_cross_ref uncr ON n.notificationId = uncr.notificationId
        ORDER BY n.timestamp DESC
    """)
    fun getNotificationsWithReadStatusByUser(): kotlinx.coroutines.flow.Flow<List<NotificationWithRead>>

    data class NotificationWithRead(
        @Embedded val notification: Notification,
        val isRead: Boolean
    )
}