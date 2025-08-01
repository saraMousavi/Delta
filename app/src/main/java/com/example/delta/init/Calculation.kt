package com.example.delta.init

import android.util.Log
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Debts
import com.example.delta.data.entity.Owners
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


    fun calculateOwnerPaymentsPerCost(
        debts: List<Debts>,
        areaByOwner: Map<Long, Double>,
        ownersUnitsCrossRefs: List<OwnersUnitsCrossRef>,
        costs: Map<Long, Costs?>
    ): Map<Long, Double> {  // Map<ownerId, amount>
        if (debts.isEmpty()) return emptyMap()

        val costPayments = mutableMapOf<Long, Double>()

        // Group debts by costId
        val debtsByCost = debts.groupBy { it.costId }

        debtsByCost.forEach { (costId, debtsForCost) ->
            var totalAmountForCost = 0.0

            val cost = costs[costId]
            if (cost == null) return@forEach

            // We'll accumulate per-owner shares for this costId
            val ownerShares = mutableMapOf<Long, Double>()

            debtsForCost.forEach { debt ->
                val unitId = debt.unitId
                val debtAmount = debt.amount
                when (cost.calculateMethod.name) {
                    CalculateMethod.DANG.name -> {
// Sum total dang for all owners across all units
                        val totalDang = ownersUnitsCrossRefs.sumOf { it.dang }
                        // Calculate each owner's total dang
                        val dangByOwner = ownersUnitsCrossRefs.groupBy { it.ownerId }
                            .mapValues { entry -> entry.value.sumOf { it.dang } }

                        // Calculate proportional share
                        dangByOwner.forEach { (ownerId, ownerDang) ->
                            val share =
                                if (totalDang > 0) (ownerDang / totalDang) * debt.amount else 0.0
                            costPayments[ownerId] = share
                        }
                    }

                    CalculateMethod.AREA.name -> {
                        val totalArea: Double = areaByOwner.values.sumByDouble { it }


                        // Calculate proportional share by area
                        areaByOwner.forEach { (ownerId, ownerArea) ->
                            val share =
                                if (totalArea > 0) (ownerArea / totalArea) * debt.amount else 0.0
                            costPayments[ownerId] = share
                        }
                    }

                    CalculateMethod.EQUAL.name -> {
// Get distinct owners
                        val distinctOwners = ownersUnitsCrossRefs.map { it.ownerId }.distinct()
                        val sharePerOwner =
                            if (distinctOwners.isNotEmpty()) debt.amount / distinctOwners.size else 0.0
                        distinctOwners.forEach { ownerId ->
                            costPayments[ownerId] = sharePerOwner
                        }
                    }

                }
                // Find owner's dang for this unit
                val ownerUnit = ownersUnitsCrossRefs.find { it.unitId == unitId }
                val ownerDang = ownerUnit?.dang ?: 0.0
                val share = (ownerDang / 6) * debtAmount
                totalAmountForCost += share

            }
            costPayments[costId] = totalAmountForCost
        }

        return costPayments
    }

    fun calculateOwnerPaymentsPerCost(
        cost: Costs,
        totalAmount: Double,
        areaByOwner: Map<Long, Double>, // ownerId to area
        ownersUnitsCrossRefs: List<OwnersUnitsCrossRef>
    ): Map<Long, Double> {
        Log.d("cost.calculateMethod.name", cost.calculateMethod.name)
        return when (cost.calculateMethod.name) {
            CalculateMethod.DANG.name -> {
                Log.d("CalculateMethod.DANG.name", CalculateMethod.DANG.name)
                // Sum total dang for all owners
                val totalDang = ownersUnitsCrossRefs.sumOf { it.dang }
                // Group by owner and sum dang per owner
                Log.d("totalDang", totalDang.toString())
                val dangByOwner = ownersUnitsCrossRefs.groupBy { it.ownerId }
                    .mapValues { entry -> entry.value.sumOf { it.dang } }
                // Calculate each owner's share
                Log.d("dangByOwner", dangByOwner.toString())
                dangByOwner.mapValues { (_, ownerDang) ->
                    if (totalDang > 0) (ownerDang / totalDang) * totalAmount else 0.0
                }
            }

            CalculateMethod.AREA.name -> {
                val totalArea = areaByOwner.values.sum()
                areaByOwner.mapValues { (_, ownerArea) ->
                    if (totalArea > 0) (ownerArea / totalArea) * totalAmount else 0.0
                }
            }

            CalculateMethod.EQUAL.name -> {
                val distinctOwners = ownersUnitsCrossRefs.map { it.ownerId }.distinct()
                val sharePerOwner = if (distinctOwners.isNotEmpty()) totalAmount / distinctOwners.size else 0.0
                distinctOwners.associateWith { sharePerOwner }
            }

            CalculateMethod.PEOPLE.name -> {
                emptyMap()
            }

            CalculateMethod.AUTOMATIC.name -> {
                emptyMap()
            }
            else -> emptyMap()
        }
    }



    /**
     * Calculate total owned area per owner.
     *
     * @param owners List of owners to calculate area for.
     * @param units List of all units in the building.
     * @param ownersUnitsCrossRefs List of ownership shares (dang) linking owners to units.
     * @return Map where key = ownerId, value = total area owned by that owner.
     */
    fun calculateAreaByOwners(
        owners: List<Owners>,
        units: List<Units>,
        ownersUnitsCrossRefs: List<OwnersUnitsCrossRef>
    ): Map<Long, Double> {
        // Group ownership shares by ownerId for quick lookup
        val crossRefsByOwner = ownersUnitsCrossRefs.groupBy { it.ownerId }

        // Map each owner to their total owned area
        return owners.associate { owner ->
            val ownerCrossRefs = crossRefsByOwner[owner.ownerId] ?: emptyList()

            // Sum area of units weighted by owner's dang share
            val totalArea = ownerCrossRefs.sumOf { crossRef ->
                val unitArea =
                    units.find { it.unitId == crossRef.unitId }?.area?.persianToEnglishDigits()
                        ?.toDouble() ?: 0.0
                unitArea * crossRef.dang
            }

            owner.ownerId to totalArea
        }
    }



}