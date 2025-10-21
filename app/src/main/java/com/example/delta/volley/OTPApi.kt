package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.example.delta.R
import com.example.delta.init.VolleySingleton
import org.json.JSONObject

private const val BASE_URL = "http://217.144.107.231:3000"
class OTPApi {

    fun sendOtp(
        context: Context,
        phone: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "$BASE_URL/auth/otp/send"
        val body = JSONObject().apply { put("phone", phone) }
        val req = JsonObjectRequest(
            Request.Method.POST, url, body,
            { resp ->
                val ok = resp.optBoolean("ok", false)
                if (ok) onSuccess() else onError(resp.optString("error", context.getString(R.string.failed)))
            },
            { err -> onError(err.message ?: context.getString(R.string.network_error)) }
        ).apply {
            retryPolicy = DefaultRetryPolicy(
                10_000, // 10s
                0,
                1f
            )
        }

        VolleySingleton.getInstance(context).addToRequestQueue(req)
    }

    fun verifyOtp(
        context: Context,
        phone: String,
        code: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "$BASE_URL/auth/otp/verify"
        val body = JSONObject().apply {
            put("phone", phone)
            put("code", code)
        }

        val req = JsonObjectRequest(
            Request.Method.POST, url, body,
            { resp ->
                val ok = resp.optBoolean("ok", false)
                if (ok) onSuccess() else onError(resp.optString("error", context.getString(R.string.wrong_expired_code)))
            },
            { err -> onError(err.message ?: context.getString(R.string.network_error)) }
        ).apply {
            retryPolicy = DefaultRetryPolicy(10_000, 0, 1f)
        }

        VolleySingleton.getInstance(context).addToRequestQueue(req)
    }
}