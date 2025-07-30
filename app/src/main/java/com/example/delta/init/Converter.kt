package com.example.delta.init

import androidx.room.TypeConverter
import com.example.delta.enums.FundType
import com.example.delta.enums.PaymentLevel
import com.example.delta.enums.Period
import com.example.delta.enums.Responsible
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converter {

    companion object {

        private val gson = Gson()

        // --- List<String> conversion using Gson ---
        @JvmStatic
        @TypeConverter
        fun fromStringToList(value: String?): List<String> {
            if (value.isNullOrEmpty()) return emptyList()
            return gson.fromJson(value, object : TypeToken<List<String>>() {}.type)
        }

        @JvmStatic
        @TypeConverter
        fun fromListToString(list: List<String>?): String {
            return gson.toJson(list ?: emptyList<String>())
        }

        @JvmStatic
        @TypeConverter
        fun fromIntToFundType(value: Int?): FundType {
            return value?.let { v ->
                FundType.entries.firstOrNull { it.resId == v } ?: FundType.OPERATIONAL
            } ?: FundType.OPERATIONAL
        }

        @JvmStatic
        @TypeConverter
        fun fromFundTypeToInt(fundType: FundType?): Int {
            return fundType?.resId ?: FundType.OPERATIONAL.resId
        }

        // --- PaymentLevel enum conversion by name ---
        @JvmStatic
        @TypeConverter
        fun fromStringToPaymentLevel(name: String?): PaymentLevel {
            return name?.let { PaymentLevel.valueOf(it) } ?: PaymentLevel.UNIT
        }

        @JvmStatic
        @TypeConverter
        fun fromPaymentLevelToString(level: PaymentLevel?): String {
            return level?.name ?: PaymentLevel.UNIT.name
        }


        // --- Period enum conversion by name ---
        @JvmStatic
        @TypeConverter
        fun fromStringToPeriod(name: String?): Period {
            return name?.let { Period.valueOf(it) } ?: Period.MONTHLY
        }

        @JvmStatic
        @TypeConverter
        fun fromPeriodToString(period: Period?): String {
            return period?.name ?: Period.MONTHLY.name
        }

        // --- Responsible enum conversion by name ---
        @JvmStatic
        @TypeConverter
        fun fromStringToResponsible(name: String?): Responsible {
            return name?.let { Responsible.valueOf(it) } ?: Responsible.TENANT
        }

        @JvmStatic
        @TypeConverter
        fun fromResponsibleToString(responsible: Responsible?): String {
            return responsible?.name ?: Responsible.TENANT.name
        }

    }
}
