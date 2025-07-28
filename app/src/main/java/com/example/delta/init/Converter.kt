package com.example.delta.init

import androidx.room.TypeConverter
import com.example.delta.enums.CalculateMethod
import com.example.delta.enums.FundFlag
import com.example.delta.enums.NotificationType
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

        // --- FundFlag enum conversion using ordinal or value ---
        @JvmStatic
        @TypeConverter
        fun fromIntToFundFlag(value: Int?): FundFlag {
            return value?.let { FundFlag.fromValue(it) } ?: FundFlag.NO_EFFECT
        }

        @JvmStatic
        @TypeConverter
        fun fromFundFlagToInt(flag: FundFlag?): Int {
            return flag?.value ?: FundFlag.NO_EFFECT.value
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
