package com.example.delta.enums

import android.content.Context
import androidx.annotation.StringRes
import com.example.delta.R

// For paymentLevel
enum class PaymentLevel(@StringRes val resId: Int) {
    UNIT(R.string.unit),
    BUILDING(R.string.building);

    companion object {
        fun fromValue(resId: Int) = entries.find { it.resId == resId } ?: UNIT
    }

    fun getDisplayName(context: Context): String = context.getString(resId)
}