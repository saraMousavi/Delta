package com.example.delta.server


import android.util.Log
import com.example.delta.data.entity.BuildingWithCounts
import com.example.delta.data.entity.BuildingTypes
import com.example.delta.data.entity.BuildingUsages
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.CityComplexes
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Credits
import com.example.delta.data.entity.Debts
import com.example.delta.data.entity.Earnings
import com.example.delta.data.entity.OwnersUnitsCrossRef
import com.example.delta.data.entity.TenantsUnitsCrossRef
import com.example.delta.data.entity.Units
import com.example.delta.data.entity.UploadedFileEntity
import com.example.delta.data.entity.User
import com.example.delta.data.entity.UserRoleBuildingUnitCrossRef
import com.example.delta.enums.CalculateMethod
import com.example.delta.enums.FundType
import com.example.delta.enums.Gender
import com.example.delta.enums.PaymentLevel
import com.example.delta.enums.Period
import com.example.delta.enums.Responsible
import com.example.delta.enums.Roles
import com.example.delta.volley.Building
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

class JsonParser {

    fun parseBuilding(obj: JSONObject): Buildings {
        val fundSafe = obj.optDouble("fund").let { if (it.isNaN()) 0.0 else it }

        return Buildings(
            buildingId = obj.optLong("buildingId"),
            complexId = if (obj.isNull("complexId")) null else obj.optLong("complexId"),
            serialNumber = obj.optString("serialNumber", ""),
            name = obj.optString("name", ""),
            postCode = obj.optString("postCode", ""),
            street = obj.optString("address", ""),
            province = obj.optString("province", ""),
            state = obj.optString("state", ""),
            buildingTypeId = if (obj.isNull("buildingTypeId")) null else obj.optLong("buildingTypeId"),
            buildingUsageId = if (obj.isNull("buildingUsageId")) null else obj.optLong("buildingUsageId"),
            fund = fundSafe,
            userId = obj.optLong("userId"),
            floorCount = obj.optString("floorCount"),
            unitCount = obj.optString("unitCount"),
            parkingCount = obj.optString("parkingCount"),
            phone = obj.optString("phone"),
            mobileNumber = obj.optString("mobileNumber")
        )
    }

    fun parseBuildingType(obj: JSONObject?): BuildingTypes? {
        if (obj == null) return null
        val id = when {
            obj.has("buildingTypeId") -> obj.optLong("buildingTypeId")
            obj.has("id") -> obj.optLong("id")
            else -> 0L
        }
        val name = obj.optString("name", obj.optString("buildingTypeName", ""))
        return BuildingTypes(buildingTypeId = id, buildingTypeName = name)
    }

    fun parseBuildingUsage(obj: JSONObject?): BuildingUsages? {
        if (obj == null) return null
        val id = when {
            obj.has("buildingUsageId") -> obj.optLong("buildingUsageId")
            obj.has("id") -> obj.optLong("id")
            else -> 0L
        }
        val name = obj.optString("name", obj.optString("buildingUsageName", ""))
        return BuildingUsages(buildingUsageId = id, buildingUsageName = name)
    }

    fun parseCityComplex(obj: JSONObject?): CityComplexes? {
        if (obj == null) return null
        val id = when {
            obj.has("complexId") -> obj.optLong("complexId")
            obj.has("id") -> obj.optLong("id")
            else -> 0L
        }
        val name = obj.optString("name", "")
        val address = if (obj.isNull("address")) null else obj.optString("address", null)
        return CityComplexes(complexId = id, name = name, address = address)
    }

    fun parseFiles(arr: JSONArray): List<UploadedFileEntity> {
        val out = ArrayList<UploadedFileEntity>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val id = when {
                o.has("fileId") -> o.optLong("fileId")
                o.has("id") -> o.optLong("id")
                else -> 0L
            }
            val url = o.optString("fileUrl", o.optString("url", ""))
            out.add(UploadedFileEntity(fileId = id, fileUrl = url))
        }
        return out
    }

    fun parseUnits(arr: JSONArray): List<Units> {
        val out = ArrayList<Units>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                Units(
                    unitId = o.optLong("unitId"),
                    buildingId = o.optLong("buildingId"),
                    unitNumber = o.optString("unitNumber"),
                    area = o.optDouble("area").toString(),
                    numberOfRooms = o.optInt("numberOfRooms", 0).toString(),
                    numberOfParking = o.optInt("numberOfParking", 0).toString(),
                    numberOfWarehouse = o.optInt("numberOfWarehouse", 0).toString(),
                    postCode = o.optString("postCode")
                )
            )
        }
        return out
    }

    fun parseUsers(arr: JSONArray): List<User> {
        val out = ArrayList<User>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                User(
                    userId = o.optLong("userId"),
                    firstName = o.optString("firstName", ""),
                    lastName = o.optString("lastName", ""),
                    phoneNumber = o.optString("phoneNumber", ""),
                    mobileNumber = o.optString("mobileNumber", ""),
                    birthday = o.optString("birthday", ""),
                    address = o.optString("address", ""),
                    email = o.optString("email", ""),
                    password = o.optString("password", "")
                )
            )
        }
        return out
    }

    fun parseOwnerUnits(arr: JSONArray): List<OwnersUnitsCrossRef> {
        val out = ArrayList<OwnersUnitsCrossRef>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                OwnersUnitsCrossRef(
                    ownerId = o.optLong("ownerId"),
                    unitId = o.optLong("unitId"),
                    dang = o.optDouble("dang"),
                    isResident = o.optBoolean("isResident")
                )
            )
        }
        return out
    }

    fun parseTenantUnits(arr: JSONArray): List<TenantsUnitsCrossRef> {
        val out = ArrayList<TenantsUnitsCrossRef>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                TenantsUnitsCrossRef(
                    tenantId = o.optLong("tenantId"),
                    unitId = o.optLong("unitId"),
                    startDate = o.optString("startDate"),
                    endDate = o.optString("endDate"),
                    numberOfTenants = o.optString("numberOfTenants"),
                    status = o.optString("status")
                )
            )
        }
        return out
    }

    fun parseCosts(array: JSONArray): List<Costs> {
        val out = ArrayList<Costs>(array.length())
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)

            out.add(
                Costs(
                    costId = obj.optLong("costId", 0L),
                    buildingId = if (obj.isNull("buildingId")) 0L else obj.optLong("buildingId"),
                    costName = obj.optString("costName", ""),
                    tempAmount = obj.optDouble("tempAmount", 0.0),
                    period = parseEnum(obj.optString("period"), Period.NONE),
                    calculateMethod = parseEnum(obj.optString("calculateMethod"), CalculateMethod.EQUAL),
                    paymentLevel = parseEnum(obj.optString("paymentLevel"), PaymentLevel.BUILDING),
                    responsible = parseEnum(obj.optString("responsible"), Responsible.OWNER),
                    fundType = parseEnum(obj.optString("fundType"), FundType.NONE),
                    chargeFlag = obj.optBoolean("chargeFlag", false),
                    capitalFlag = obj.optBoolean("capitalFlag", false),
                    invoiceFlag = obj.optBoolean("invoiceFlag", false),
                    dueDate = obj.optString("dueDate", ""),
                    costFor = obj.optString("costFor"),
                    documentNumber = obj.optString("documentNumber"),
                    addedBeforeCreateBuilding = obj.optBoolean("addedBeforeCreateBuilding", false),
                    forBuildingId = obj.optLong("forBuildingId", 0L)
                )
            )
        }
        return out
    }

    fun parseDebts(array: JSONArray): List<Debts> {
        val out = ArrayList<Debts>(array.length())
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            out.add(
                Debts(
                    debtId = obj.optLong("debtId", 0L),
                    unitId = if (obj.isNull("unitId")) null else obj.optLong("unitId"),
                    costId = obj.optLong("costId", 0L),
                    ownerId = obj.optLong("ownerId", 0L),
                    buildingId = obj.optLong("buildingId", 0L),
                    description = obj.optString("description", ""),
                    dueDate = obj.optString("dueDate", ""),
                    amount = obj.optDouble("amount", 0.0),
                    paymentFlag = obj.optBoolean("paymentFlag", false)
                )
            )
        }
        return out
    }

    fun parseBuildingWithCounts(obj: JSONObject): BuildingWithCounts {
        val fundSafe = obj.optDouble("fund").let { if (it.isNaN()) 0.0 else it }
        return BuildingWithCounts(
            buildingId = obj.optLong("buildingId"),
            complexId = if (obj.isNull("complexId")) null else obj.optLong("complexId"),
            name = obj.optString("name", ""),
            phone = obj.optString("phone", ""),
            email = obj.optString("email", ""),
            postCode = obj.optString("postCode", ""),
            street = obj.optString("address", ""),
            province = obj.optString("province", ""),
            state = obj.optString("state", ""),
            buildingTypeId = if (obj.isNull("buildingTypeId")) null else obj.optLong("buildingTypeId"),
            buildingUsageId = if (obj.isNull("buildingUsageId")) null else obj.optLong("buildingUsageId"),
            fund = fundSafe,
            userId = obj.optLong("userId"),
            buildingTypeName = obj.optString("buildingTypeName", null),
            buildingUsageName = obj.optString("buildingUsageName", null),
            unitsCount = obj.optInt("unitsCount", 0),
            ownersCount = obj.optInt("ownersCount", 0)
        )
    }

    data class EarningWithCredits(
        val earning: Earnings,
        val credits: List<Credits>
    )

    fun parseEarnings(arr: JSONArray): List<Earnings> {
        val out = ArrayList<Earnings>(arr.length())
        for (i in 0 until arr.length()) {
            out.add(parseEarning(arr.getJSONObject(i)))
        }
        return out
    }

    fun parseEarning(o: JSONObject): Earnings {
        return Earnings(
            earningsId = o.optLong("earningsId", 0L),
            earningsName = o.optString("earningsName", o.optString("earnings_name", "")),
            buildingId = if (o.isNull("buildingId")) null else o.optLong("buildingId"),
            amount = o.optDouble("amount", 0.0),
            period = parsePeriod(o.optString("period", null)),
            invoiceFlag = parseInvoiceFlag(o),
            startDate = o.optString("startDate", o.optString("start_date", "")),
            endDate = o.optString("endDate", o.optString("end_date", "")),
            addedBeforeCreateBuilding = o.optBoolean("addedBeforeCreateBuilding", false),
            forBuildingId = o.optLong("forBuildingId", 0L)
        )
    }

    fun parseCredits(arr: JSONArray): List<Credits> {
        val out = ArrayList<Credits>(arr.length())
        for (i in 0 until arr.length()) {
            out.add(parseCredit(arr.getJSONObject(i)))
        }
        return out
    }

    fun parseCredit(o: JSONObject): Credits {
        return Credits(
            creditsId = o.optLong("creditsId", 0L),
            earningsId = o.optLong("earningsId", 0L),
            buildingId = o.optLong("buildingId", 0L),
            description = o.optString("description", ""),
            dueDate = o.optString("dueDate", ""),
            amount = o.optDouble("amount", 0.0),
            receiptFlag = o.optBoolean("receiptFlag", false)
        )
    }

    fun parseEarningWithCredits(obj: JSONObject): EarningWithCredits {
        val earningObj = obj.optJSONObject("earning") ?: JSONObject()
        val creditsArr = obj.optJSONArray("credits") ?: JSONArray()
        return EarningWithCredits(
            earning = parseEarning(earningObj),
            credits = parseCredits(creditsArr)
        )
    }

    fun parseOk(resp: JSONObject): Boolean = resp.optBoolean("ok", false)

    fun parseHasReceived(resp: JSONObject, key: String, fallback: Boolean = false): Boolean =
        resp.optBoolean(key, fallback)

    fun parseEarningsId(resp: JSONObject): Long = resp.optLong("earningsId", 0L)

    fun parseCreditsInserted(resp: JSONObject): Int = resp.optInt("creditsInserted", 0)

    fun parseMessage(respBody: String): String =
        runCatching { JSONObject(respBody).optString("message", "") }.getOrDefault("")

    private fun parsePeriod(raw: String?): Period {
        val v = raw?.trim()?.uppercase(Locale.US).orEmpty()
        return when (v) {
            "DAILY" -> Period.DAILY
            "WEEKLY" -> Period.MONTHLY
            "MONTHLY" -> Period.MONTHLY
            "YEARLY" -> Period.YEARLY
            "NONE", "" -> Period.NONE
            else -> Period.NONE
        }
    }

    private fun parseInvoiceFlag(obj: JSONObject): Boolean {
        return when {
            obj.has("invoiceFlag") && obj.opt("invoiceFlag") is Boolean -> obj.optBoolean("invoiceFlag", false)
            obj.has("invoice_flag") && obj.opt("invoice_flag") is Boolean -> obj.optBoolean("invoice_flag", false)
            obj.has("invoiceFlag") && obj.opt("invoiceFlag") is Int -> obj.optInt("invoiceFlag", 0) != 0
            obj.has("invoice_flag") && obj.opt("invoice_flag") is Int -> obj.optInt("invoice_flag", 0) != 0
            else -> obj.optBoolean("invoiceFlag", false)
        }
    }

    data class OwnerWithUnitsDto(
        val user: User?,
        val tenants: List<TenantUnitInfo>,
        val userRole: Building.UserRole,
        val ownerUnits: List<OwnersUnitsCrossRef>,
        val units: List<Units>,
        val userRoleCrossRefs: List<UserRoleBuildingUnitCrossRef>,
        val isManager: Boolean,
        val isResident: Boolean
    )

    data class TenantUnitInfo(
        val tenant: User,
        val unitId: Long
    )

    fun parseOwnerWithUnits(obj: JSONObject): OwnerWithUnitsDto {
        val userObj = obj.optJSONObject("user")
        val user = userObj?.let { parseUser(it) }

        val tenantsUnits = parseTenantUnitInfo(obj.optJSONArray("tenantForUnit") ?: JSONArray())

        val roleEnum = parseRole(obj.optString("roleName", "PROPERTY_OWNER"))
        val roleUser = user ?: User(
            userId = 0L,
            mobileNumber = "",
            password = "",
            firstName = "",
            lastName = "",
            email = "",
            gender = Gender.MALE,
            profilePhoto = "",
            nationalCode = "",
            address = "",
            phoneNumber = "",
            birthday = ""
        )

        val userRole = Building.UserRole(
            user = roleUser,
            roles = roleEnum
        )

        val ownerUnits = parseOwnerUnits(obj.optJSONArray("ownerUnits") ?: JSONArray())
        val units = parseUnits(obj.optJSONArray("units") ?: JSONArray())
        val userRoleCrossRefs = parseUserRoleCrossRefs(obj.optJSONArray("userRoleCrossRefs") ?: JSONArray())

        val isManager = obj.optBoolean("isManager", roleEnum == Roles.BUILDING_MANAGER)
        val isResident = obj.optBoolean("isResident", false)

        return OwnerWithUnitsDto(
            user = user,
            tenants = tenantsUnits,
            userRole = userRole,
            ownerUnits = ownerUnits,
            units = units,
            userRoleCrossRefs = userRoleCrossRefs,
            isManager = isManager,
            isResident = isResident
        )
    }

    fun parseOwnerWithUnitsList(arr: JSONArray): List<OwnerWithUnitsDto> {
        val out = ArrayList<OwnerWithUnitsDto>(arr.length())
        for (i in 0 until arr.length()) {
            out.add(parseOwnerWithUnits(arr.getJSONObject(i)))
        }
        return out
    }

    fun parseUser(userObj: JSONObject): User =
        User(
            userId = userObj.optLong("userId", 0L),
            mobileNumber = userObj.optString("mobileNumber", ""),
            password = "",
            firstName = userObj.optString("firstName", ""),
            lastName = userObj.optString("lastName", ""),
            email = userObj.optString("email", ""),
            gender = parseGender(userObj.optString("gender", "MALE")),
            profilePhoto = userObj.optString("profilePhoto", ""),
            nationalCode = userObj.optString("nationalCode", ""),
            address = userObj.optString("address", ""),
            phoneNumber = userObj.optString("phoneNumber", ""),
            birthday = userObj.optString("birthday", "")
        )

    private fun parseTenantUnitInfo(arr: JSONArray): List<TenantUnitInfo> {
        val out = ArrayList<TenantUnitInfo>(arr.length())
        for (i in 0 until arr.length()) {
            val u = arr.getJSONObject(i)
            val tenantObj = u.optJSONObject("tenant") ?: JSONObject()
            val tenantUser = parseUser(tenantObj)
            out.add(
                TenantUnitInfo(
                    tenant = tenantUser,
                    unitId = u.optLong("unitId", 0L)
                )
            )
        }
        return out
    }


    private fun parseUserRoleCrossRefs(arr: JSONArray): List<UserRoleBuildingUnitCrossRef> {
        val out = ArrayList<UserRoleBuildingUnitCrossRef>(arr.length())
        for (i in 0 until arr.length()) {
            val c = arr.getJSONObject(i)
            out.add(
                UserRoleBuildingUnitCrossRef(
                    roleId = c.optLong("roleId", 0L),
                    userId = c.optLong("userId", 0L),
                    buildingId = c.optLong("buildingId", 0L),
                    unitId = c.optLong("unitId", 0L)
                )
            )
        }
        return out
    }

    private fun parseGender(raw: String): Gender =
        runCatching { Gender.valueOf(raw.trim().uppercase(Locale.US)) }
            .getOrElse { Gender.MALE }

    private fun parseRole(raw: String): Roles =
        runCatching { Roles.valueOf(raw.trim().uppercase(Locale.US)) }
            .getOrElse { Roles.PROPERTY_OWNER }


    private inline fun <reified T : Enum<T>> parseEnum(raw: String?, fallback: T): T {
        val v = raw?.trim().orEmpty()
        if (v.isEmpty()) return fallback
        return runCatching { enumValueOf<T>(v.uppercase(Locale.US)) }.getOrElse { fallback }
    }
}
