package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.entity.Notification
import com.example.delta.data.entity.UsersNotificationCrossRef
import com.example.delta.enums.NotificationType
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class CreateNotificationResult(
    val notificationId: Long,
    val targets: Int
)

data class NotificationWithCrossRef(
    val notification: Notification,
    val crossRef: UsersNotificationCrossRef
)

class Notification {

    private val baseUrl = "http://217.144.107.231:3000/notification"

    suspend fun createNotificationSuspend(
        context: Context,
        title: String,
        message: String,
        type: String? = null,
        createdByUserId: Long? = null,
        buildingId: Long? = null,
        targetUserIds: List<Long>? = null,
        targetMobiles: List<String>? = null
    ): CreateNotificationResult =
        suspendCancellableCoroutine { cont ->
            val queue = Volley.newRequestQueue(context.applicationContext)

            val body = JSONObject().apply {
                put("title", title)
                put("message", message)
                if (!type.isNullOrBlank()) put("type", type)
                if (createdByUserId != null) put("createdByUserId", createdByUserId)
                if (buildingId != null) put("buildingId", buildingId)
                targetUserIds?.takeIf { it.isNotEmpty() }?.let {
                    put("targetUserIds", JSONArray(it))
                }
                targetMobiles?.takeIf { it.isNotEmpty() }?.let {
                    put("targetMobiles", JSONArray(it))
                }
            }

            val request = object : JsonObjectRequest(
                Request.Method.POST,
                baseUrl,
                body,
                { resp ->
                    try {
                        val id = resp.optLong("notificationId", 0L)
                        val targets = resp.optInt("targets", 0)
                        cont.resume(CreateNotificationResult(id, targets))
                    } catch (e: Exception) {
                        cont.resumeWithException(e)
                    }
                },
                { error ->
                    cont.resumeWithException(error)
                }
            ) {}

            queue.add(request)
            cont.invokeOnCancellation { request.cancel() }
        }

    suspend fun sendNotificationPushSuspend(
        context: Context,
        notificationId: Long
    ): Unit =
        suspendCancellableCoroutine { cont ->
            val queue = Volley.newRequestQueue(context.applicationContext)
            val url = "$baseUrl/send-push"

            val body = JSONObject().apply {
                put("notificationId", notificationId)
            }

            val request = object : JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                {
                    cont.resume(Unit)
                },
                { error ->
                    cont.resumeWithException(error)
                }
            ) {}

            queue.add(request)
            cont.invokeOnCancellation { request.cancel() }
        }

    fun fetchNotificationsForUser(
        context: Context,
        userId: Long,
        onSuccess: (List<NotificationWithCrossRef>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/by-user?userId=$userId"
        Log.d("NotificationApi", "GET $url")

        val queue = Volley.newRequestQueue(context)
        val req = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { arr ->
                try {
                    val result = mutableListOf<NotificationWithCrossRef>()
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)

                        val typeRaw = o.optString("type", NotificationType.MANAGER.name)
                        val typeEnum = runCatching {
                            NotificationType.valueOf(typeRaw)
                        }.getOrElse { NotificationType.MANAGER }

                        val notificationId = o.optLong("notificationId")
                        val createdByUserId =
                            if (o.isNull("createdByUserId")) null else o.optLong("createdByUserId")
                        val ts = o.optLong("timestamp")
                        val isRead = o.optBoolean("isRead", false)

                        val notification = Notification(
                            notificationId = notificationId,
                            title = o.optString("title"),
                            message = o.optString("message"),
                            type = typeEnum,
                            userId = createdByUserId,
                            timestamp = ts
                        )

                        val crossRef = UsersNotificationCrossRef(
                            userId = userId,
                            notificationId = notificationId,
                            isRead = isRead
                        )

                        result += NotificationWithCrossRef(
                            notification = notification,
                            crossRef = crossRef
                        )
                    }
                    onSuccess(result)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err ->
                onError(formatVolleyError("NotificationApi(fetchNotificationsForUser)", err))
            }
        )
        queue.add(req)
    }

    fun markNotificationRead(
        context: Context,
        userId: Long,
        notificationId: Long,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/$notificationId/read"
        val queue = Volley.newRequestQueue(context)

        val body = JSONObject().apply {
            put("userId", userId)
        }

        val req = object : JsonObjectRequest(
            Request.Method.POST,
            url,
            body,
            { _ -> onSuccess() },
            { err -> onError(formatVolleyError("NotificationApi(markNotificationRead)", err)) }
        ) {}

        queue.add(req)
    }

    fun deleteNotificationForUser(
        context: Context,
        userId: Long,
        notificationId: Long,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/$notificationId/user/$userId"
        val queue = Volley.newRequestQueue(context)

        val req = object : JsonObjectRequest(
            Request.Method.DELETE,
            url,
            null,
            { _ -> onSuccess() },
            { err -> onError(formatVolleyError("NotificationApi(deleteNotificationForUser)", err)) }
        ) {}

        queue.add(req)
    }

    private fun formatVolleyError(tag: String, error: com.android.volley.VolleyError): Exception {
        val resp = error.networkResponse
        return if (resp != null) {
            val body = try {
                String(resp.data ?: ByteArray(0), Charsets.UTF_8)
            } catch (_: Exception) {
                String(resp.data ?: ByteArray(0))
            }
            Log.e(tag, "HTTP ${resp.statusCode}: $body")
            Exception("HTTP ${resp.statusCode}: $body")
        } else {
            Log.e(tag, "No networkResponse: ${error.message}", error)
            Exception(error.toString())
        }
    }

    suspend fun fetchNotificationsForUserSuspend(
        context: Context,
        userId: Long
    ): List<NotificationWithCrossRef> =
        suspendCancellableCoroutine { cont ->
            fetchNotificationsForUser(
                context = context,
                userId = userId,
                onSuccess = { list ->
                    if (cont.isActive) cont.resume(list)
                },
                onError = { e ->
                    if (cont.isActive) cont.resumeWithException(e)
                }
            )
        }

    suspend fun markNotificationReadSuspend(
        context: Context,
        userId: Long,
        notificationId: Long
    ) = suspendCancellableCoroutine<Unit> { cont ->
        markNotificationRead(
            context = context,
            userId = userId,
            notificationId = notificationId,
            onSuccess = {
                if (cont.isActive) cont.resume(Unit)
            },
            onError = { e ->
                if (cont.isActive) cont.resumeWithException(e)
            }
        )
    }
}
