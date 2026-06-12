package com.example.delta.server


import android.util.Log
import com.android.volley.NetworkError
import com.android.volley.NoConnectionError
import com.android.volley.TimeoutError
import com.android.volley.VolleyError

object VolleyErrorMapper {

    fun toException(tag: String, error: VolleyError): Exception {
        val resp = error.networkResponse
        return if (resp != null) {
            val body = runCatching {
                String(resp.data ?: ByteArray(0), Charsets.UTF_8)
            }.getOrElse {
                runCatching { String(resp.data ?: ByteArray(0)) }.getOrDefault("")
            }
            Log.e(tag, "HTTP ${resp.statusCode}")
            Exception("HTTP ${resp.statusCode}: $body")
        } else {
            val msg = when (error) {
                is TimeoutError -> "مهلت اتصال به سرور به پایان رسید"
                is NoConnectionError -> "اتصال به اینترنت یا سرور برقرار نشد"
                is NetworkError -> "خطای شبکه (لطفاً اینترنت را بررسی کنید)"
                else -> "خطای نامشخص شبکه"
            }
            Exception(msg, error)
        }
    }
}
