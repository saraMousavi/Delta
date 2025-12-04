package com.example.delta.init

import android.util.Log
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Debts
import com.example.delta.data.entity.OwnersUnitsCrossRef
import com.example.delta.data.entity.Units
import com.example.delta.enums.CalculateMethod
import com.example.delta.persianToEnglishDigits
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.sumOf

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
        return when (calculationMethod) {
            CalculateMethod.EQUAL -> {
                val totalUnits = allUnits.size
                if (totalUnits > 0) {
                    costAmount / totalUnits
                } else {
                    0.0
                }
            }
            CalculateMethod.PEOPLE -> {
                val totalTenants = allUnits.sumOf { getTenantCountForUnit(it.unitId) }
                val unitTenants = getTenantCountForUnit(unit.unitId)
                if (totalTenants > 0) {
                    // Allocate proportional to number of tenants in this unit
                    (unitTenants.toDouble() / totalTenants) * costAmount
                } else {
                    0.0
                }
            }
            CalculateMethod.AREA -> {
                val totalArea = allUnits.sumOf { it.area.persianToEnglishDigits().toDoubleOrNull() ?: 0.0 }
                val unitArea = unit.area.persianToEnglishDigits().toDoubleOrNull() ?: 0.0
                if (totalArea > 0) {
                    (unitArea / totalArea) * costAmount
                } else {
                    0.0
                }
            }
            // Extend for other CalculateMethod cases if needed
            else -> {
                // Default fallback: equal division
                val totalUnits = allUnits.size
                if (totalUnits > 0) costAmount / totalUnits else 0.0
            }
        }
    }


}