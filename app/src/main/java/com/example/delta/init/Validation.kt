package com.example.delta.init

import com.example.delta.viewmodel.SharedViewModel

class Validation {
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Simple phone validation: digits only, length 7-15 (adjust as needed)
    fun isValidPhone(phone: String): Boolean {
        val phoneRegex = Regex("^\\d{7,15}\$")
        return phoneRegex.matches(phone)
    }

    // Iran mobile number validation
    fun isValidIranMobile(mobile: String): Boolean {
        // Iran mobile numbers start with 09 and have 11 digits total
        val iranMobileRegex = Regex("^09\\d{9}\$")
        return iranMobileRegex.matches(mobile)
    }

    fun validateDang(dang: Double): Boolean {
        return dang in 0.0..6.0
    }

    fun isBuildingInfoValid(sharedViewModel: SharedViewModel): Boolean {
        return sharedViewModel.name.isNotBlank() &&
                sharedViewModel.selectedBuildingTypes != null &&
                sharedViewModel.selectedBuildingTypes?.buildingTypeId != 0L &&
                sharedViewModel.selectedBuildingUsages != null &&
                sharedViewModel.selectedBuildingUsages?.buildingUsageId != 0L &&
                sharedViewModel.street.isNotBlank() &&
                sharedViewModel.postCode.isNotBlank() &&
                (!sharedViewModel.sameArea ||
                        (sharedViewModel.numberOfUnits.isNotBlank() && sharedViewModel.unitArea.isNotBlank()))
    }


    fun isTenantPeriodConflicted(
        unitId: Long,
        startDate: String,
        endDate: String,
        sharedViewModel: SharedViewModel
    ): Boolean {
        // Get all tenants for this unit
        val tenantsForUnit = sharedViewModel.tenantUnitMap.filter {
            it.value.unitId == unitId && it.key.status == "فعال" // "فعال" = "active"
        }
        // Parse dates
        val newStart = sharedViewModel.parsePersianDate(startDate)
        val newEnd = sharedViewModel.parsePersianDate(endDate)
        for (tenant in tenantsForUnit) {
            val existingStart = sharedViewModel.parsePersianDate(tenant.key.startDate)
            val existingEnd = sharedViewModel.parsePersianDate(tenant.key.endDate)
            // If periods overlap
            if (existingStart != null && existingEnd != null && newStart != null && newEnd != null) {
                val overlap =
                    !(newEnd < existingStart || newStart > existingEnd)
                if (overlap) return true
            }
        }
        return false
    }


}