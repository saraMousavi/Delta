package com.example.delta.init

import android.content.Context
import androidx.core.content.edit

class Preference {
    fun getUserId(context: Context): Long {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getLong("user_id", -1L) // -1L or any invalid ID as default
    }

    fun getDarkModeState(context: Context): Boolean {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("is_dark_mode", false) // -1L or any invalid ID as default
    }


    fun isFirstLogin(context: Context): Boolean {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("first_login", true)
    }

    fun setFirstLoginCompleted(context: Context) {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit() { putBoolean("first_login", false) }
    }

    fun getUserMobile(context: Context): String? {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getString("user_mobile", "") // -1L or any invalid ID as default
    }
}