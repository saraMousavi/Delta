package com.example.delta.init

import com.example.delta.viewmodel.SharedViewModel

class Validation {
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPhone(phone: String): Boolean {
        val phoneRegex = Regex("^\\d{7,15}\$")
        return phoneRegex.matches(phone)
    }

    fun isValidIranMobile(mobile: String): Boolean {
        val iranMobileRegex = Regex("^09\\d{9}\$")
        return iranMobileRegex.matches(mobile)
    }

    fun isValidPostalCode(postCode: String): Boolean {
        val postalRegex = Regex("^\\d{10}\$")
        return postalRegex.matches(postCode)
    }

    fun validateDang(dang: Double): Boolean {
        return dang in 0.0..6.0
    }

    // --- NEW: deed serial (18-digit unique id) ---
    fun normalizeDeedSerial(raw: String): String {
        return raw.filter { it.isDigit() }
    }

    fun isValidDeedSerial(raw: String): Boolean {
        val digits = normalizeDeedSerial(raw)
        return digits.length == 18
    }

    // Optional: floor number range
    fun isValidFloorNumber(floor: Int): Boolean {
        return floor in -5..50
    }

    fun isBuildingInfoValid(sharedViewModel: SharedViewModel): Boolean {
        return sharedViewModel.name.isNotBlank() &&
                sharedViewModel.selectedBuildingTypes != null &&
                sharedViewModel.selectedBuildingTypes?.buildingTypeId != 0L &&
                sharedViewModel.selectedBuildingUsages != null &&
                sharedViewModel.selectedBuildingUsages?.buildingUsageId != 0L &&
                sharedViewModel.street.isNotBlank() &&
                sharedViewModel.postCode.isNotBlank() &&
                isValidPostalCode(sharedViewModel.postCode) &&
                (!sharedViewModel.sameArea ||
                        (sharedViewModel.numberOfUnits.isNotBlank() && sharedViewModel.unitArea.isNotBlank()))
    }

    fun isTenantPeriodConflicted(
        unitId: Long,
        startDate: String,
        endDate: String,
        sharedViewModel: SharedViewModel
    ): Boolean {
//        val tenantsForUnit = sharedViewModel.tenantUnitMap.filter {
//            it.value.unitId == unitId && it.key.status == "فعال"
//        }
//
//        val newStart = sharedViewModel.parsePersianDate(startDate)
//        val newEnd = sharedViewModel.parsePersianDate(endDate)
//
//        for (tenant in tenantsForUnit) {
//            val existingStart = sharedViewModel.parsePersianDate(tenant.key.startDate)
//            val existingEnd = sharedViewModel.parsePersianDate(tenant.key.endDate)
//            if (existingStart != null && existingEnd != null && newStart != null && newEnd != null) {
//                val overlap = !(newEnd < existingStart || newStart > existingEnd)
//                if (overlap) return true
//            }
//        }
        return false
    }
}
