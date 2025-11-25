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
        Log.d("body", body.toString())
        val req = JsonObjectRequest(
            Request.Method.POST, url, body,
            { resp ->
                val ok = resp.optBoolean("ok", false)
                if (ok) onSuccess() else onError(resp.optString("error", context.getString(R.string.failed)))
            },
            { err ->
                Log.d("err.message1", err.message.toString())
                onError(err.message ?: context.getString(R.string.network_error)) }
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
        val request = object : JsonObjectRequest(
            Method.POST,
            url,
            body,
            { response ->
                val ok = response.optBoolean("ok", false)
                if (ok) {
                    onSuccess()
                } else {
                    val errorCode = response.optString("errorCode", "UNKNOWN")
                    onError(mapOtpErrorMessage(context, errorCode))
                }
            },
            { error ->
                val errorCode = try {
                    val data = String(error.networkResponse?.data ?: ByteArray(0))
                    val json = JSONObject(data)
                    json.optString("errorCode", "UNKNOWN")
                } catch (e: Exception) {
                    "UNKNOWN"
                }
                onError(mapOtpErrorMessage(context, errorCode))
            }
        ){}
        request.apply {
            retryPolicy = DefaultRetryPolicy(10_000, 0, 1f)
        }

        VolleySingleton.getInstance(context).addToRequestQueue(request)
    }
}


private fun mapOtpErrorMessage(context: Context, code: String): String {
    return when (code) {
        "NO_OTP_REQUEST"   -> context.getString(R.string.otp_no_request)
        "EXPIRED"          -> context.getString(R.string.otp_expired)
        "TOO_MANY_ATTEMPTS"-> context.getString(R.string.otp_too_many)
        "INVALID_CODE"     -> context.getString(R.string.otp_invalid)
        else               -> context.getString(R.string.error_unknown)
    }
}