package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.example.delta.data.entity.User
import com.example.delta.init.AppRequestQueue
import org.json.JSONArray
import org.json.JSONObject

object TokenUploader {
    private const val TAG = "TokenUploader"
//    private const val TOKEN_URL = "http://217.144.107.231:3000/push/token"
// TokenUploader
private const val TOKEN_URL =
    "http://217.144.107.231:3000/push/device-token/upsert"


    fun uploadFcmToken(
        context: Context,
        user: User,
        fcmToken: String,
        platform: String = "android",
        deviceId: String? = null,
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        val body = JSONObject().apply {
            put("userId", user.userId)
            put("token", fcmToken)
            put("platform", platform)
            deviceId?.let { put("deviceId", it) }
        }

        val req = object : JsonObjectRequest(
            Request.Method.POST,
            TOKEN_URL,
            body,
            { onSuccess() },
            { onError(it) }
        ) {
            override fun getHeaders(): MutableMap<String, String> =
                mutableMapOf("Content-Type" to "application/json")
        }

        AppRequestQueue.getInstance(context).addToRequestQueue(req)
    }

}
