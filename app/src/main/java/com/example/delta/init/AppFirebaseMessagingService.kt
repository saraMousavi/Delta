package com.example.delta.init


import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.android.volley.Request.Method
import com.android.volley.toolbox.JsonObjectRequest
import com.example.delta.HomePageActivity
import com.example.delta.R
import com.example.delta.volley.TokenUploader
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONArray
import org.json.JSONObject

class AppFirebaseMessagingService : FirebaseMessagingService() {
    private  val BASE_URL = "http://217.144.107.231:3000/push/send"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val userId = Preference().getUserId(applicationContext)
        if (userId != 0L) {
//            TokenUploader.uploadFcmToken(
//                context = applicationContext,
//                user = userId,
//                fcmToken = token
//            )
        } else {
//            Preference().savePendingFcmToken(applicationContext, token)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["title"] ?: getString(R.string.app_name)
        val body  = remoteMessage.data["body"] ?: ""
        val deepLink = remoteMessage.data["deepLink"]

        showLocalNotification(title, body, deepLink)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showLocalNotification(title: String, body: String, deepLink: String?) {
        val channelId = "general_channel"
        createChannelIfNeeded(channelId)

        val intent = Intent(this, HomePageActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            deepLink?.let { putExtra("deeplink", it) }
        }
        val pending = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_home)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pending)

        NotificationManagerCompat.from(this)
            .notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun createChannelIfNeeded(channelId: String) {
        val mgr = getSystemService(NotificationManager::class.java)
        mgr.createNotificationChannel(
            NotificationChannel(channelId, "General", NotificationManager.IMPORTANCE_HIGH)
        ) }

    fun sendPushNotification(
        context: Context,
        userIds: List<Long>,
        template: String? = null,
        params: Map<String, String> = emptyMap(),
        onSuccess: (() -> Unit)? = null,
        onError: ((Throwable) -> Unit)? = null
    ) {

        val payload = JSONObject().apply {
            put("mobileNumber", JSONArray(listOf("09103009458")))
//            put("userIds", JSONArray(userIds))
            template?.let { put("template", it) }
            if (params.isNotEmpty()) put("params", JSONObject(params))
        }
        val req = object : JsonObjectRequest(Method.POST, BASE_URL, payload,
            { _ -> onSuccess?.invoke() },
            { err -> onError?.invoke(err) }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(
                    "Content-Type" to "application/json"
                    // "Authorization" to "Bearer <token>"  // if needed
                )
            }
        }

        AppRequestQueue.getInstance(context).addToRequestQueue(req)
    }
}
