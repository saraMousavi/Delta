package com.example.delta.init

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.delta.HomePageActivity
import com.example.delta.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AppFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val userId = Preference().getUserId(applicationContext)
        if (userId != 0L) {
            // upload token if needed
        } else {
            // store pending token if needed
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["title"]
            ?: remoteMessage.notification?.title
            ?: getString(R.string.app_name)

        val body = remoteMessage.data["body"]
            ?: remoteMessage.notification?.body
            ?: ""

        val openDrawer = remoteMessage.data["type"] != "chat"

        showNotification(
            title = title,
            body = body,
            openNotificationsDrawer = openDrawer,
            threadId = remoteMessage.data["threadId"]?.toLongOrNull()
        )
    }

    private fun showNotification(
        title: String,
        body: String,
        openNotificationsDrawer: Boolean,
        threadId: Long? = null
    ) {
        try {
            val channelId = "delta_default_channel"
            val notificationId = System.currentTimeMillis().toInt()

            val manager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Delta Notification",
                    NotificationManager.IMPORTANCE_HIGH
                )
                manager.createNotificationChannel(channel)
            }

            val intent = Intent(this, HomePageActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("from_push", true)
                putExtra("open_notifications_drawer", openNotificationsDrawer)
                if (threadId != null) {
                    putExtra("chat_thread_id", threadId)
                }
            }

            val pendingFlags =
                PendingIntent.FLAG_UPDATE_CURRENT or
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            PendingIntent.FLAG_IMMUTABLE
                        else
                            0

            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                pendingFlags
            )

            val appTitle = "برنامه مدیریت ساختمان"

            val builder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.delta_logo)
                .setContentTitle("$appTitle • $title")
                .setContentText(body)
                .setStyle(
                    NotificationCompat.BigTextStyle().bigText(body)
                )
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)

            manager.notify(notificationId, builder.build())
        } catch (e: Exception) {
            Log.e("Notif.Error", e.message.toString(), e)
        }
    }
}
