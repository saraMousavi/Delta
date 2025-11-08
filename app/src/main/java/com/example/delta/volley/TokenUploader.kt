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
    private const val TOKEN_URL = "http://217.144.107.231:3000/push/token"

    fun uploadFcmToken(
        context: Context,
        user: User,
        fcmToken: String,
        jwt: String? = null,
        platform: String = "android",
        deviceId: String? = null,
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        val body = JSONObject().apply {
//            put("userId", user.userId)
            put("mobileNumber", user.mobileNumber)
            put("token", fcmToken)
            put("platform", platform)
            deviceId?.let { put("deviceId", it) }
        }
        Log.d("tokenbody", body.toString())
        val req = object : JsonObjectRequest(
            Request.Method.POST, TOKEN_URL, body,
            { _ -> onSuccess() },
            { err -> Log.e(TAG, "upload failed: ${err.message}", err); onError(err) }
        ) {
            override fun getHeaders(): MutableMap<String, String> =
                mutableMapOf<String, String>().apply {
                    put("Content-Type", "application/json")
                    jwt?.let { put("Authorization", "Bearer $it") }
                }
        }.apply {
            retryPolicy = DefaultRetryPolicy(10_000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        }

        AppRequestQueue.getInstance(context).addToRequestQueue(req)
    }



}
