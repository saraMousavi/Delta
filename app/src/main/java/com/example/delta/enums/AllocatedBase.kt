package com.example.delta.enums

import android.content.Context
import androidx.annotation.StringRes
import com.example.delta.R

enum class AllocatedBase (@StringRes val resId: Int) {
    EQUAL(R.string.fixed),
    AREA(R.string.area),
    PEOPLE(R.string.people);

    fun getDisplayName(context: Context): String = context.getString(resId)

    companion object {
        fun fromString(value: String, context: Context): CalculateMethod? =
            CalculateMethod.entries.find { it.getDisplayName(context) == value }
    }
}