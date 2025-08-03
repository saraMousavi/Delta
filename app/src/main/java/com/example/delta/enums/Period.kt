package com.example.delta.enums

import androidx.annotation.StringRes
import com.example.delta.R

enum class Period(@StringRes val resId: Int) {
    MONTHLY(R.string.monthly),
    YEARLY(R.string.yearly),
    NONE(R.string.doesnt_have);

    fun getDisplayName(context: android.content.Context): String = context.getString(resId)
}