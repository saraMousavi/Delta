package com.example.delta.enums

import androidx.annotation.StringRes
import com.example.delta.R


enum class FilterType(@StringRes val resId: Int) {
    ALL(R.string.all),
    PAYMENT(R.string.payments),
    DEBT(R.string.debt);

    fun getDisplayName(context: android.content.Context): String = context.getString(resId)

}

