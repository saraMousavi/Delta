package com.example.delta.enums

import android.content.Context
import androidx.annotation.StringRes
import com.example.delta.R

enum class CalculateMethod(@StringRes val resId: Int) {
    FIXED(R.string.fixed),
    AUTOMATIC(R.string.automatic),
    AREA(R.string.area),
    DANG(R.string.dang),
    PEOPLE(R.string.people);

    fun getDisplayName(context: Context): String = context.getString(resId)

    companion object {
        fun fromString(value: String, context: Context): CalculateMethod? =
            entries.find { it.getDisplayName(context) == value }
    }
}