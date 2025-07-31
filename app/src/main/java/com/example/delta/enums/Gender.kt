package com.example.delta.enums

import android.content.Context
import androidx.annotation.StringRes
import com.example.delta.R

enum class Gender(@StringRes val resId: Int) {
    MALE(R.string.male),
    FEMALE(R.string.female);

    fun getDisplayName(context: Context): String = context.getString(resId)

    companion object {
        fun fromDisplayName(context: Context, displayName: String): Gender? {
            return entries.firstOrNull { it.getDisplayName(context) == displayName }
        }
    }
}


