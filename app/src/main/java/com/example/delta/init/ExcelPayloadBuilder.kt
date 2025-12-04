package com.example.delta.init

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
    val excelDang: Double
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
    val excelBuildingName: String
)

object ExcelPayloadBuilder {

    fun build(
        buildings: List<Buildings>,
        units: List<Units>,
        owners: List<ExcelOwner>,
        tenants: List<ExcelTenant>
    ): List<Map<String, Any>> {

        return buildings.map { b ->
            val buildingName = b.name

            val buildingUnits = units.filter { it.excelBuildingName == buildingName }
            val buildingOwners = owners.filter { it.excelBuildingName == buildingName }
            val buildingTenants = tenants.filter { it.excelBuildingName == buildingName }

            val ownerUnits = buildingOwners.map {
                mapOf(
                    "ownerMobile" to it.mobileNumber,
                    "unitNumber" to it.excelUnitsNumber,
                    "dang" to it.excelDang
                )
            }

            val tenantUnits = buildingTenants.map {
                mapOf(
                    "tenantMobile" to it.mobileNumber,
                    "unitNumber" to it.excelUnitsNumber,
                    "startDate" to it.startDate,
                    "endDate" to it.endDate,
                    "status" to it.status
                )
            }

            mapOf(
                "mobileNumber" to b.userId.toString(),
                "building" to mapOf(
                    "name" to b.name,
                    "postCode" to b.postCode,
                    "street" to b.street,
                    "province" to b.province,
                    "state" to b.state,
                    "buildingTypeId" to b.buildingTypeId,
                    "buildingUsageId" to b.buildingUsageId,
                    "fund" to b.fund
                ),
                "units" to buildingUnits.map {
                    mapOf(
                        "unitNumber" to it.unitNumber,
                        "area" to it.area,
                        "numberOfRooms" to it.numberOfRooms,
                        "numberOfParking" to it.numberOfParking,
                        "numberOfWarehouse" to it.numberOfWarehouse,
                        "postCode" to it.postCode
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
                        "isManager" to it.excelIsManager
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
                        "numberOfTenants" to it.numberOfTenants
                    )
                },
                "ownerUnits" to ownerUnits,
                "tenantUnits" to tenantUnits
            )
        }
    }
}
