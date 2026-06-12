package com.example.delta.server

import com.example.delta.data.entity.BuildingTypes
import com.example.delta.data.entity.BuildingUsages
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Credits
import com.example.delta.data.entity.Debts
import com.example.delta.data.entity.Earnings
import com.example.delta.data.entity.OwnersUnitsCrossRef
import com.example.delta.data.entity.Units
import com.example.delta.data.entity.User
import org.json.JSONArray
import org.json.JSONObject

class JsonMapper {

    fun buildingToJson(b: Buildings, t: BuildingTypes?, u: BuildingUsages?): JSONObject =
        JSONObject().apply {
            put("name", b.name)
            put("postCode", b.postCode)
            put("address", b.street)
            put("province", b.province)
            put("state", b.state)
            put("floorCount", b.floorCount)
            put("parkingCount", b.parkingCount)
            put("unitCount", b.unitCount)
            put("serialNumber", b.serialNumber)

            b.complexId?.let { put("complexId", it) }
            b.buildingTypeId?.let { put("buildingTypeId", it) }
            b.buildingUsageId?.let { put("buildingUsageId", it) }

            put("buildingTypeName", t?.buildingTypeName.orEmpty())
            put("buildingUsageName", u?.buildingUsageName.orEmpty())

            put("fund", b.fund)
            put("userId", b.userId)
            put("mobileNumber", b.mobileNumber)
            put("phone", b.phone)
        }

    fun unitToJson(unit: Units): JSONObject =
        JSONObject().apply {
            put("unitNumber", unit.unitNumber)
            put("area", unit.area)
            put("numberOfRooms", unit.numberOfRooms)
            put("numberOfParking", unit.numberOfParking)
            put("floorNumber", unit.floorNumber)
            put("numberOfWarehouse", unit.numberOfWarehouse)
            put("postCode", unit.postCode)
        }

    fun costToJson(cost: Costs, tempId: String? = null): JSONObject =
        JSONObject().apply {
            tempId?.let { put("tempId", it) }

            if (cost.buildingId != null && cost.buildingId > 0L) put("buildingId", cost.buildingId) else put("buildingId", 0)
            put("costName", cost.costName)
            put("tempAmount", if (cost.tempAmount == 1.0) 0.0 else cost.tempAmount)
            put("period", cost.period)
            put("calculateMethod", cost.calculateMethod)
            put("paymentLevel", cost.paymentLevel)
            put("responsible", cost.responsible)
            put("fundType", cost.fundType)
            put("chargeFlag", cost.chargeFlag)
            put("capitalFlag", cost.capitalFlag)
            put("invoiceFlag", cost.invoiceFlag)
            put("dueDate", cost.dueDate)
            put("paymentDate", cost.paymentDate)
            put("documentNumber", cost.documentNumber)
            put("costFor", cost.costFor)
            put("forBuildingId", cost.forBuildingId)
            put("addedBeforeCreateBuilding", cost.addedBeforeCreateBuilding)
        }

    fun debtToJson(debt: Debts, costTempId: String? = null): JSONObject =
        JSONObject().apply {
            if (!costTempId.isNullOrBlank()) {
                put("costTempId", costTempId)
            } else {
                put("costId", debt.costId)
            }

            put("buildingId", debt.buildingId)

            val unitId = debt.unitId
            if (unitId != null && unitId > 0L) put("unitId", unitId)

            put("ownerId", debt.ownerId)
            put("description", debt.description)
            put("dueDate", debt.dueDate)
            put("amount", debt.amount)
            put("paymentFlag", debt.paymentFlag)
        }

    fun buildInsertCostPayload(costs: List<Costs>, debts: List<Debts>): JSONObject =
        JSONObject().apply {
            put("cost", listToJsonArray(costs) { costToJson(it) })
            put("debts", listToJsonArray(debts) { debtToJson(it) })
        }

    fun buildUpdatePayload(cost: Costs): JSONObject =
        JSONObject().apply {
            put("buildingId", cost.buildingId)
            put("costName", cost.costName)
            put("tempAmount", cost.tempAmount)
            put("period", cost.period.name)
            put("calculateMethod", cost.calculateMethod.name)
            put("paymentLevel", cost.paymentLevel.name)
            put("responsible", cost.responsible.name)
            put("fundType", cost.fundType.name)
            put("chargeFlag", cost.chargeFlag)
            put("capitalFlag", cost.capitalFlag)
            put("invoiceFlag", cost.invoiceFlag)
            put("dueDate", cost.dueDate)
            put("costFor", cost.costFor)
            put("documentNumber", cost.documentNumber)
        }

    fun earningToJson(e: Earnings): JSONObject =
        JSONObject().apply {
            put("earningsId", e.earningsId)
            put("buildingId", e.buildingId)
            put("earningsName", e.earningsName)
            put("amount", e.amount)
            put("period", e.period.name)
            put("startDate", e.startDate)
            put("endDate", e.endDate)
            put("invoiceFlag", e.invoiceFlag)
            put("addedBeforeCreateBuilding", e.addedBeforeCreateBuilding)
            put("forBuildingId", e.forBuildingId)
        }

    fun earningCreateBody(e: Earnings): JSONObject =
        JSONObject().apply {
            put("buildingId", e.buildingId)
            put("earningsName", e.earningsName)
            put("amount", e.amount)
            put("period", e.period.name)
            put("startDate", e.startDate)
            put("endDate", e.endDate)
            put("invoiceFlag", e.invoiceFlag)
            put("addedBeforeCreateBuilding", e.addedBeforeCreateBuilding)
            put("forBuildingId", e.forBuildingId)
        }

    fun earningWithCreditsCreateBody(e: Earnings): JSONObject =
        JSONObject().apply {
            put("buildingId", e.buildingId)
            put("earningsName", e.earningsName)
            put("amount", e.amount)
            put("period", e.period.name)
            put("startDate", e.startDate)
            put("endDate", e.endDate)
            put("addedBeforeCreateBuilding", e.addedBeforeCreateBuilding)
            put("forBuildingId", e.forBuildingId)
        }

    fun buildUpdateEarningWithCreditsBody(e: Earnings): JSONObject =
        JSONObject().apply {
            put("earning", earningToJson(e))
        }

    fun buildMarkReceivedBody(earningId: Long, creditIds: List<Long>): JSONObject =
        JSONObject().apply {
            put("earningId", earningId)
            put("creditIds", JSONArray(creditIds.filter { it > 0L }.distinct()))
        }

    fun creditToJson(c: Credits): JSONObject =
        JSONObject().apply {
            put("creditsId", c.creditsId)
            put("earningsId", c.earningsId)
            put("buildingId", c.buildingId)
            put("description", c.description)
            put("dueDate", c.dueDate)
            put("amount", c.amount)
            put("receiptFlag", c.receiptFlag)
        }

    fun ownerCreateBody(
        user: User,
        buildingId: Long,
        isManager: Boolean,
        units: List<OwnersUnitsCrossRef>
    ): JSONObject {
        val ownerJson = JSONObject().apply {
            put("firstName", user.firstName)
            put("lastName", user.lastName)
            put("email", user.email)
            put("phoneNumber", user.phoneNumber)
            put("mobileNumber", user.mobileNumber)
            put("address", user.address)
            put("birthday", user.birthday)
            put("buildingId", buildingId)
            put("isManager", isManager)
        }

        val unitsArray = JSONArray().apply {
            units.forEach { u ->
                put(
                    JSONObject().apply {
                        put("unitId", u.unitId)
                        put("dang", u.dang)
                        put("isResident", u.isResident)
                    }
                )
            }
        }

        return JSONObject().apply {
            put("owner", ownerJson)
            put("units", unitsArray)
        }
    }

    fun updateOwnerUnitsAndRoleBody(
        isManager: Boolean,
        units: List<OwnersUnitsCrossRef>
    ): JSONObject {
        val arr = JSONArray().apply {
            units.forEach { u ->
                put(
                    JSONObject().apply {
                        put("unitId", u.unitId)
                        put("dang", u.dang)
                        put("isResident", u.isResident)
                    }
                )
            }
        }
        return JSONObject().apply {
            put("isManager", isManager)
            put("units", arr)
        }
    }

    fun updateOwnerRoleBody(
        userId: Long,
        buildingId: Long,
        isManager: Boolean
    ): JSONObject = JSONObject().apply {
        put("userId", userId)
        put("buildingId", buildingId)
        put("roleName", if (isManager) "BUILDING_MANAGER" else "PROPERTY_OWNER")
    }


    fun <T> listToJsonArray(list: List<T>, toJsonFunc: (T) -> JSONObject): JSONArray =
        JSONArray().apply { list.forEach { put(toJsonFunc(it)) } }
}
