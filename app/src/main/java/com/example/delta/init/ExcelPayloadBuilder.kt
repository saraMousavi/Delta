package com.example.delta.init

import android.util.Log
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.Units

data class ExcelOwner(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val mobileNumber: String,
    val address: String,
    val email: String,
    val excelUnitsNumber: String,
    val excelBuildingName: String,
    val excelIsManager: Boolean,
//    val excelDang: Double,
    val excelIsResident: Boolean
)

data class ExcelTenant(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val mobileNumber: String,
    val email: String,
    val numberOfTenants: String,
    val startDate: String,
    val endDate: String,
    val status: String,
    val excelUnitsNumber: String,
    val excelBuildingName: String,
    val excelPostCode: String
)

data class ExcelBuilding(
    val name: String,
    val serialNumber: String,
    val phone: String,
    val mobileNumber: String,
    val floorCount: String,
    val unitCount: String,
    val parkingCount: String,
    val postCode: String,
    val street: String,
    val province: String = "Tehran",
    val state: String = "Central",
    val buildingTypeId: Long? = null,
    val buildingUsageId: Long? = null,
    val fund: Double,
    val userId: Long,
    val buildingTypeName: String,
    val buildingUsageName: String,
    val creatorMobile: String,
)

object ExcelPayloadBuilder {

    fun build(
        buildings: List<ExcelBuilding>,
        units: List<Units>,
        owners: List<ExcelOwner>,
        tenants: List<ExcelTenant>
    ): List<Map<String, Any>> {

        return buildings.map { b ->
            val buildingName = b.name

            val buildingUnits = units.filter { it.excelBuildingName == buildingName }
            val buildingOwners = owners.filter { it.excelBuildingName == buildingName }
            val buildingTenants = tenants.filter { it.excelBuildingName == buildingName }

            val managerOwner = buildingOwners.firstOrNull { it.excelIsManager }
//            val creatorMobile = when {
//                managerOwner != null && managerOwner.mobileNumber.isNotBlank() ->
//                    managerOwner.mobileNumber
//                buildingOwners.firstOrNull()?.mobileNumber?.isNotBlank() == true ->
//                    buildingOwners.first().mobileNumber
//                else ->
//                    b.userId.toString()
//            }

            val ownerUnits = buildingOwners.map {
                mapOf(
                    "ownerMobile" to it.mobileNumber,
                    "unitNumber" to it.excelUnitsNumber,
                    "dang" to 6,
                    "isResident" to it.excelIsResident,
                    "isManager" to it.excelIsManager
                )
            }

            val tenantUnits = buildingTenants.map {
                mapOf(
                    "tenantMobile" to it.mobileNumber,
                    "unitNumber" to it.excelUnitsNumber,
                    "startDate" to it.startDate,
                    "endDate" to it.endDate,
                    "numberOfTenants" to it.numberOfTenants,
                    "status" to it.status
                )
            }

            mapOf(
                "mobileNumber" to b.creatorMobile,
                "building" to mapOf(
                    "name" to b.name,
                    "postCode" to b.postCode,
                    "mobileNumber" to b.mobileNumber,
                    "phone" to b.phone,
                    "address" to b.street,
                    "province" to b.province,
                    "state" to b.state,
                    "buildingTypeId" to b.buildingTypeId,
                    "buildingUsageId" to b.buildingUsageId,
                    "fund" to b.fund,
                    "userId" to b.userId,
                    "floorCount" to b.floorCount,
                    "unitCount" to b.unitCount,
                    "serialNumber" to b.serialNumber,
                    "buildingTypeName" to b.buildingTypeName,
                    "buildingUsageName" to b.buildingUsageName
                ),
                "units" to buildingUnits.map {
                    mapOf(
                        "unitNumber" to it.unitNumber,
                        "area" to it.area,
                        "numberOfRooms" to it.numberOfRooms,
                        "numberOfParking" to it.numberOfParking,
                        "numberOfWarehouse" to it.numberOfWarehouse,
                        "postCode" to it.postCode,
                        "floorNumber" to it.floorNumber
                    )
                },
                "owners" to buildingOwners.map {
                    mapOf(
                        "firstName" to it.firstName,
                        "lastName" to it.lastName,
                        "mobileNumber" to it.mobileNumber,
                        "phoneNumber" to it.phoneNumber,
                        "address" to it.address,
                        "email" to it.email,
                        "isManager" to it.excelIsManager,
                        "excelIsManager" to it.excelIsManager,
                        "excelIsResident" to it.excelIsResident
                    )
                },
                "tenants" to buildingTenants.map {
                    mapOf(
                        "firstName" to it.firstName,
                        "lastName" to it.lastName,
                        "mobileNumber" to it.mobileNumber,
                        "phoneNumber" to it.phoneNumber,
                        "email" to it.email,
                        "startDate" to it.startDate,
                        "endDate" to it.endDate,
                        "status" to it.status,
                        "numberOfTenants" to it.numberOfTenants,
                        "postCode" to it.excelPostCode
                    )
                },
                "ownerUnits" to ownerUnits,
                "tenantUnits" to tenantUnits
            )
        }
    }
}
