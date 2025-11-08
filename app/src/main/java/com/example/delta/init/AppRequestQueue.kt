package com.example.delta.init

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class AppRequestQueue private constructor(context: Context) {
    val requestQueue: RequestQueue = Volley.newRequestQueue(context.applicationContext)

    companion object {
        @Volatile private var INSTANCE: AppRequestQueue? = null
        fun getInstance(context: Context): AppRequestQueue =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppRequestQueue(context).also { INSTANCE = it }
            }
    }

    fun <T> addToRequestQueue(req: Request<T>) = requestQueue.add(req)
}
