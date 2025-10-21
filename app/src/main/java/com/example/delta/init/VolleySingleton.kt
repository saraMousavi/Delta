package com.example.delta.init


import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class VolleySingleton private constructor(context: Context) {
    companion object {
        @Volatile private var instance: VolleySingleton? = null
        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: VolleySingleton(context.applicationContext).also { instance = it }
            }
    }

    val requestQueue: RequestQueue by lazy { Volley.newRequestQueue(context.applicationContext) }

    fun <T> addToRequestQueue(req: Request<T>): Request<T?>? = requestQueue.add(req)
}
