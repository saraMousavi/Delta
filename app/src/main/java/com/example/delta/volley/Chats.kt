// ChatApi.kt
package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.entity.ChatManagerDto
import com.example.delta.data.entity.ChatMessageDto
import com.example.delta.data.entity.ChatThreadDto
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class Chats {

    private val baseUrl = "http://217.144.107.231:3000/chat"

    fun fetchManagersForUser(
        context: Context,
        userId: Long,
        onSuccess: (List<ChatManagerDto>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/managers?userId=$userId"
        val queue = Volley.newRequestQueue(context.applicationContext)

        val req = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { arr ->
                try {
                    val list = mutableListOf<ChatManagerDto>()
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        list += ChatManagerDto(
                            userId = o.optLong("userId"),
                            firstName = o.optString("firstName", ""),
                            lastName = o.optString("lastName", ""),
                            mobileNumber = o.optString("mobileNumber", ""),
                            buildingId = if (o.isNull("buildingId")) null else o.optLong("buildingId"),
                            buildingName = o.optString("buildingName", "")
                        )
                    }
                    onSuccess(list)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err ->
                onError(Exception(err.toString()))
            }
        )

        queue.add(req)
    }

    suspend fun fetchManagersForUserSuspend(
        context: Context,
        userId: Long
    ): List<ChatManagerDto> = suspendCancellableCoroutine { cont ->
        fetchManagersForUser(
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

    fun createOrGetThread(
        context: Context,
        userId: Long,
        managerId: Long,
        buildingId: Long?,
        onSuccess: (ChatThreadDto) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/threads"
        val queue = Volley.newRequestQueue(context.applicationContext)

        val body = JSONObject().apply {
            put("userId", userId)
            put("managerId", managerId)
            if (buildingId != null) put("buildingId", buildingId)
        }

        val req = object : JsonObjectRequest(
            Method.POST,
            url,
            body,
            { o ->
                try {
                    val u = o.optLong("userId")
                    val m = o.optLong("managerId")
                    val dto = ChatThreadDto(
                        threadId = o.optLong("threadId"),
                        buildingId = if (o.isNull("buildingId")) null else o.optLong("buildingId"),
                        buildingName = o.optString("buildingName", ""),
                        participants = listOf(u, m),
                        lastMessageAt = o.optLong("lastMessageAt", 0L).takeIf { it != 0L },
                        lastMessageText = o.optString("lastMessageText", ""),
                        partnerId = managerId,
                        partnerFirstName = o.optString("partnerFirstName", ""),
                        partnerLastName = o.optString("partnerLastName", ""),
                        partnerMobileNumber = o.optString("partnerMobileNumber", ""),
                        unreadCount = 0
                    )
                    onSuccess(dto)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err -> onError(Exception(err.toString())) }
        ) {}

        queue.add(req)
    }

    suspend fun createOrGetThreadSuspend(
        context: Context,
        userId: Long,
        managerId: Long,
        buildingId: Long?
    ): ChatThreadDto = suspendCancellableCoroutine { cont ->
        createOrGetThread(
            context = context,
            userId = userId,
            managerId = managerId,
            buildingId = buildingId,
            onSuccess = { t -> if (cont.isActive) cont.resume(t) },
            onError = { e -> if (cont.isActive) cont.resumeWithException(e) }
        )
    }

    fun fetchThreadsForUser(
        context: Context,
        userId: Long,
        asManager: Boolean,
        onSuccess: (List<ChatThreadDto>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/threads?userId=$userId&asManager=${if (asManager) 1 else 0}"
        val queue = Volley.newRequestQueue(context.applicationContext)

        val req = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { arr ->
                try {
                    val list = mutableListOf<ChatThreadDto>()
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        val partner = o.optJSONObject("partner")
                        list += ChatThreadDto(
                            threadId = o.optLong("threadId"),
                            buildingId = if (o.isNull("buildingId")) null else o.optLong("buildingId"),
                            participants = emptyList(),
                            buildingName = o.optString("buildingName", ""),
                            lastMessageAt = o.optLong("lastMessageAt", 0L).takeIf { it != 0L },
                            lastMessageText = o.optString("lastMessageText", ""),
                            partnerId = partner?.optLong("userId"),
                            partnerFirstName = partner?.optString("firstName", ""),
                            partnerLastName = partner?.optString("lastName", ""),
                            partnerMobileNumber = partner?.optString("mobileNumber", ""),
                            unreadCount = o.optInt("unreadCount", 0)
                        )
                    }
                    Log.d("messageList", list.toString())
                    onSuccess(list)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err -> onError(Exception(err.toString())) }
        )

        queue.add(req)
    }


    suspend fun fetchThreadsForUserSuspend(
        context: Context,
        userId: Long,
        asManager: Boolean
    ): List<ChatThreadDto> = suspendCancellableCoroutine { cont ->
        fetchThreadsForUser(
            context = context,
            userId = userId,
            asManager = asManager,
            onSuccess = { list -> if (cont.isActive) cont.resume(list) },
            onError = { e -> if (cont.isActive) cont.resumeWithException(e) }
        )
    }

    data class OpenThreadResult(
        val thread: ChatThreadDto,
        val messages: List<ChatMessageDto>
    )

    suspend fun openThreadSuspend(
        context: Context,
        userId: Long,
        peerId: Long,
        buildingId: Long?
    ): OpenThreadResult =
        suspendCancellableCoroutine { cont ->
            val queue = Volley.newRequestQueue(context.applicationContext)
            val url = "$baseUrl/open-thread"

            val body = JSONObject().apply {
                put("userId", userId)
                put("peerId", peerId)
                if (buildingId != null) put("buildingId", buildingId)
            }

            val req = object : JsonObjectRequest(
                Method.POST,
                url,
                body,
                { resp ->
                    try {
                        val t = resp.getJSONObject("thread")
                        val thread = ChatThreadDto(
                            threadId = t.optLong("threadId"),
                            buildingId = if (t.isNull("buildingId")) null else t.optLong("buildingId"),
                            buildingName = t.optString("buildingName", null),
                            participants = (t.optJSONArray("participants") ?: JSONArray()).let { arr ->
                                List(arr.length()) { i -> arr.getLong(i) }
                            },
                            lastMessageAt = null,
                            lastMessageText = null,
                            partnerId = null,
                            partnerFirstName = null,
                            partnerLastName = null,
                            partnerMobileNumber = null,
                            unreadCount = 0
                        )


                        val msgsJson = resp.optJSONArray("messages") ?: JSONArray()
                        val messages = mutableListOf<ChatMessageDto>()
                        for (i in 0 until msgsJson.length()) {
                            val m = msgsJson.getJSONObject(i)
                            messages += ChatMessageDto(
                                messageId = m.optLong("messageId"),
                                threadId = m.optLong("threadId"),
                                senderId = m.optLong("senderId"),
                                text = m.optString("text"),
                                createdAt = m.optLong("createdAt", 0L)
                            )
                        }

                        if (cont.isActive) {
                            cont.resume(
                                OpenThreadResult(
                                    thread = thread,
                                    messages = messages
                                ),
                                onCancellation = null
                            )
                        }
                    } catch (e: Exception) {
                        if (cont.isActive) cont.resumeWithException(e)
                    }
                },
                { err ->
                    if (cont.isActive) cont.resumeWithException(err)
                }
            ) {}

            queue.add(req)
            cont.invokeOnCancellation { req.cancel() }
        }

    suspend fun fetchMessagesSuspend(
        context: Context,
        threadId: Long,
        sinceTs: Long?
    ): List<ChatMessageDto> = suspendCancellableCoroutine { cont ->
        val queue = Volley.newRequestQueue(context.applicationContext)

        val url = buildString {
            append("$baseUrl/threads/$threadId/messages")
            if (sinceTs != null && sinceTs > 0) {
                append("?sinceTs=$sinceTs")
            }
        }

        val req = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { arr ->
                try {
                    val list = mutableListOf<ChatMessageDto>()
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        list += ChatMessageDto(
                            messageId = o.optLong("messageId"),
                            threadId = o.optLong("threadId"),
                            senderId = o.optLong("senderId"),
                            text = o.optString("text"),
                            createdAt = o.optLong("createdAt", 0L)
                        )
                    }
                    cont.resume(list)
                } catch (e: Exception) {
                    cont.resumeWithException(e)
                }
            },
            { err ->
                cont.resumeWithException(err)
            }
        )

        queue.add(req)
        cont.invokeOnCancellation { req.cancel() }
    }

    suspend fun sendMessageSuspend(
        context: Context,
        threadId: Long,
        senderId: Long,
        text: String
    ): ChatMessageDto = suspendCancellableCoroutine { cont ->
        val queue = Volley.newRequestQueue(context.applicationContext)

        val body = JSONObject().apply {
            put("threadId", threadId)
            put("senderId", senderId)
            put("text", text)
        }

        val req = object : JsonObjectRequest(
            Method.POST,
            "$baseUrl/messages",
            body,
            { o ->
                try {
                    val msg = ChatMessageDto(
                        messageId = o.optLong("messageId"),
                        threadId = o.optLong("threadId"),
                        senderId = o.optLong("senderId"),
                        text = o.optString("text"),
                        createdAt = o.optLong("createdAt", 0L)
                    )
                    cont.resume(msg)
                } catch (e: Exception) {
                    cont.resumeWithException(e)
                }
            },
            { err ->
                cont.resumeWithException(err)
            }
        ) {}

        queue.add(req)
        cont.invokeOnCancellation { req.cancel() }
    }

    fun markThreadRead(
        context: Context,
        threadId: Long,
        userId: Long,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/threads/read"
        val queue = Volley.newRequestQueue(context.applicationContext)

        val body = JSONObject().apply {
            put("threadId", threadId)
            put("userId", userId)
        }

        val req = object : JsonObjectRequest(
            Method.POST,
            url,
            body,
            { _ -> onSuccess()  },
            { err -> onError(Exception(err.toString())) }
        ) {}

        queue.add(req)
    }

    fun getUnreadCount(
        context: Context,
        userId: Long,
        asManager: Boolean,
        onSuccess: (Int) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/unread-count?userId=$userId&asManager=${if (asManager) 1 else 0}"
        val queue = Volley.newRequestQueue(context.applicationContext)

        val req = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            { o ->
                val unread = o.optInt("unread", 0)
                onSuccess(unread)
            },
            { err -> onError(Exception(err.toString())) }
        ) {}

        queue.add(req)
    }
}
