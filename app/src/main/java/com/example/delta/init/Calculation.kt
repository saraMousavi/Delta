package com.example.delta.init

import com.example.delta.data.entity.Units
import com.example.delta.enums.CalculateMethod
import com.example.delta.persianToEnglishDigits
import kotlin.collections.sumOf
import kotlin.math.round

class Calculation {

    /**
     * Calculate amount for a single unit based on the calculation method.
     *
     * @param costAmount Total amount to be divided among units.
     * @param unit The unit for which to calculate the amount.
     * @param allUnits All units involved in the calculation (required for PEOPLE method).
     * @param calculationMethod Calculation method to apply (EQUAL or PEOPLE).
     * @param getTenantCountForUnit Function to get the number of tenants in a given unit.
     * @return Calculated amount for the given unit.
     */


    fun calculateAmountByMethod(
        costAmount: Double,
        unit: Units,
        allUnits: List<Units>,
        calculationMethod: CalculateMethod,
        getTenantCountForUnit: (unitId: Long) -> Int
    ): Double {

        fun rounded(value: Double): Double {
            return round(value)
        }

        return when (calculationMethod) {
            CalculateMethod.EQUAL -> {
                val totalUnits = allUnits.size
                if (totalUnits > 0) {
                    rounded(costAmount / totalUnits)
                } else 0.0
            }

            CalculateMethod.PEOPLE -> {
                val totalTenants = allUnits.sumOf { getTenantCountForUnit(it.unitId) }
                val unitTenants = getTenantCountForUnit(unit.unitId)
                if (totalTenants > 0) {
                    rounded((unitTenants.toDouble() / totalTenants) * costAmount)
                } else 0.0
            }

            CalculateMethod.AREA -> {
                val totalArea = allUnits.sumOf {
                    it.area.persianToEnglishDigits().toDoubleOrNull() ?: 0.0
                }
                val unitArea = unit.area.persianToEnglishDigits().toDoubleOrNull() ?: 0.0
                if (totalArea > 0) {
                    rounded((unitArea / totalArea) * costAmount)
                } else 0.0
            }

            else -> {
                val totalUnits = allUnits.size
                if (totalUnits > 0) rounded(costAmount / totalUnits) else 0.0
            }
        }
    }

}