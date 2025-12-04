package com.example.delta.enums

import androidx.annotation.StringRes
import com.example.delta.R

enum class FundType(@StringRes val resId: Int) {
    OPERATIONAL(R.string.operational),
    CAPITAL(R.string.capital),
    NONE(R.string.rental);

    fun getDisplayName(context: android.content.Context): String = context.getString(resId)

    companion object {
        fun fromDisplayName(context: android.content.Context, displayName: String): FundType? {
            return entries.firstOrNull { it.getDisplayName(context) == displayName }
        }
    }
}


