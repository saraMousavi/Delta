package com.example.delta.init

import com.github.mikephil.charting.formatter.ValueFormatter

class MoneyValueFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        val divided = (value).toInt()
        return "%,dت".format(divided)
    }
}
