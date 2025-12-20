package com.example.delta.init

import android.content.Context
import com.example.delta.R

object FloorFormatter {

    fun toLabel(context: Context, floorNumber: Int): String {
        return when {
            floorNumber < 0 -> {
                val index = kotlin.math.abs(floorNumber)
                if (index == 1) {
                    context.getString(R.string.floor_basement_single)
                } else {
                    context.getString(R.string.floor_basement_n, index)
                }
            }
            floorNumber == 0 -> {
                context.getString(R.string.floor_ground)
            }
            else -> {
                val index = floorNumber
                context.getString(R.string.floor_n, index)
            }
        }
    }

    fun basement(index: Int): Int = -index
    fun ground(): Int = 0
    fun normalFloor(index: Int): Int = index
}
