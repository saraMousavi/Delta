package com.example.delta.init

import android.content.Context

class Preference {
    fun getUserId(context: Context): Long {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getLong("user_id", -1L) // -1L or any invalid ID as default
    }
    fun getUserMobile(context: Context): String? {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getString("user_mobile", "") // -1L or any invalid ID as default
    }
}