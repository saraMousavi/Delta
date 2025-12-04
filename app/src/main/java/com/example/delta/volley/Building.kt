package com.example.delta.volley

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkError
import com.android.volley.NoConnectionError
import com.android.volley.Request
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.entity.*
import com.example.delta.enums.CalculateMethod
import com.example.delta.enums.FundType
import com.example.delta.enums.PaymentLevel
import com.example.delta.enums.Period
import com.example.delta.enums.Responsible
import com.example.delta.enums.Roles
import com.example.delta.init.AppRequestQueue
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.UUID

class Building {
    private val baseUrl = "http://217.144.107.231:3000/building"

    data class BuildingWithResidents(
        val building: Buildings,
        val units: List<com.example.delta.data.entity.Units>,
        val owners: List<User>,
        val tenants: List<User>,
        val ownerUnits: List<OwnersUnitsCrossRef>,
        val tenantUnits: List<TenantsUnitsCrossRef>
    )


    data class BuildingFullDto(
        val building: Buildings,
        val buildingType: BuildingTypes?,
        val buildingUsage: BuildingUsages?,
        val cityComplex: CityComplexes?,
        val files: List<UploadedFileEntity>,
        val units: List<com.example.delta.data.entity.Units>,
        val users: List<User>,
        val role: List<UserRole>,
        val ownerUnits: List<OwnersUnitsCrossRef>,
        val tenantUnits: List<TenantsUnitsCrossRef>,
        val costs: List<Costs>,
        val defaultChargeCosts: List<Costs>,
        val chargeCostsForYear: List<Costs>
    )

    data class UserRole(
        val user: User,
        val roles: Roles
    )

    data class BulkBuildingItem(
        val building: Buildings,
        val buildingType: BuildingTypes?,
        val buildingUsage: BuildingUsages?,
        val units: List<com.example.delta.data.entity.Units> = emptyList(),
        val user: List<User> = emptyList(),
        val role: List<UserRole> = emptyList(),
        val ownerUnits: List<OwnersUnitsCrossRef> = emptyList(),
        val tenantUnits: List<TenantsUnitsCrossRef> = emptyList()
    )

    fun fetchBuildingsWithResidents(
        context: Context,
        mobileNumber: String,
        onSuccess: (List<BuildingWithResidents>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)
        val url = "$baseUrl/with-residents?mobileNumber=${mobileNumber.trim()}"

        val request = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { arr ->
                try {
                    val out = mutableListOf<BuildingWithResidents>()

                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)

                        val bObj = obj.getJSONObject("building")
                        val building = Buildings(
                            buildingId = bObj.optLong("buildingId"),
                            complexId = if (bObj.isNull("complexId")) null else bObj.optLong("complexId"),
                            serialNumber = bObj.optString("serialNumber", ""),
                            name = bObj.optString("name", ""),
                            postCode = bObj.optString("postCode", ""),
                            street = bObj.optString("street", ""),
                            province = bObj.optString("province", ""),
                            state = bObj.optString("state", ""),
                            buildingTypeId = if (bObj.isNull("buildingTypeId")) null else bObj.optLong("buildingTypeId"),
                            buildingUsageId = if (bObj.isNull("buildingUsageId")) null else bObj.optLong("buildingUsageId"),
                            fund = bObj.optDouble("fund").let { if (it.isNaN()) 0.0 else it },
                            userId = bObj.optLong("userId"),
                            floorCount = bObj.optInt("floorCount")
                        )

                        val units = parseUnits(obj.optJSONArray("units") ?: JSONArray())
                        val owners = parseUsers(obj.optJSONArray("owners") ?: JSONArray())
                        val tenants = parseUsers(obj.optJSONArray("tenants") ?: JSONArray())
                        val ownerUnits = parseOwnerUnits(obj.optJSONArray("ownerUnits") ?: JSONArray())
                        val tenantUnits = parseTenantUnits(obj.optJSONArray("tenantUnits") ?: JSONArray())

                        out += BuildingWithResidents(
                            building = building,
                            units = units,
                            owners = owners,
                            tenants = tenants,
                            ownerUnits = ownerUnits,
                            tenantUnits = tenantUnits
                        )
                    }

                    onSuccess(out)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err -> onError(formatVolleyError("fetchBuildingsWithResidents", err)) }
        )

        queue.add(request)
    }

    private fun parseUnits(arr: JSONArray): List<com.example.delta.data.entity.Units> {
        val list = mutableListOf<com.example.delta.data.entity.Units>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list += com.example.delta.data.entity.Units(
                unitId = o.optLong("unitId"),
                buildingId = o.optLong("buildingId"),
                unitNumber = o.optString("unitNumber"),
                area = o.optDouble("area").toString(),
                numberOfRooms = o.optInt("numberOfRooms", 0).toString(),
                numberOfParking = o.optInt("numberOfParking", 0).toString(),
                numberOfWarehouse = o.optInt("numberOfWarehouse", 0).toString(),
                postCode = o.optString("postCode")
            )
        }
        return list
    }

    private fun parseUsers(arr: JSONArray): List<User> {
        val list = mutableListOf<User>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list += User(
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
        }
        return list
    }

    private fun parseOwnerUnits(arr: JSONArray): List<OwnersUnitsCrossRef> {
        val list = mutableListOf<OwnersUnitsCrossRef>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list += OwnersUnitsCrossRef(
                ownerId = o.optLong("ownerId"),
                unitId = o.optLong("unitId"),
                dang = o.optDouble("dang")
            )
        }
        return list
    }

    private fun parseTenantUnits(arr: JSONArray): List<TenantsUnitsCrossRef> {
        val list = mutableListOf<TenantsUnitsCrossRef>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list += TenantsUnitsCrossRef(
                tenantId = o.optLong("tenantId"),
                unitId = o.optLong("unitId"),
                startDate = o.optString("startDate"),
                endDate = o.optString("endDate"),
                numberOfTenants = o.optString("numberOfTenants"),
                status = o.optString("status")
            )
        }
        return list
    }

    fun insertBuildingsBulk(
        mobileNumber: String,
        context: Context,
        items: List<BulkBuildingItem>,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)
        val payload = JSONArray().apply {
            items.forEach { put(makeBulkItemJson(mobileNumber, it)) }
        }
        Log.d("BuildingVolley", "Bulk payload: $payload")

        val req = jsonObjectRequestWithArrayBody(
            Request.Method.POST,
            baseUrl,
            payload,
            onSuccess = { resp -> onSuccess(resp.toString()) },
            onError = onError
        )
        queue.add(req)
    }

    fun insertBuilding(
        mobileNumber: String,
        context: Context,
        buildingJson: JSONObject,
        unitsJsonArray: JSONArray,
        costsJsonArray: JSONArray,
        filesJsonArray: JSONArray,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)
        val item = JSONObject().apply {
            put("building", buildingJson)
            put("mobileNumber", mobileNumber)
            put("units", unitsJsonArray)
            put("costs", costsJsonArray)
            put("files", filesJsonArray)
            put("idempotencyKey", "imp-${UUID.randomUUID()}")
        }
        val payload = JSONArray().put(item)
        val req = jsonObjectRequestWithArrayBody(
            Request.Method.POST,
            baseUrl,
            payload,
            onSuccess = { resp -> onSuccess(resp.toString()) },
            onError = onError
        )
        queue.add(req)
    }

    fun makeBulkItemJson(mobileNumber: String, item: BulkBuildingItem): JSONObject {
        val buildingJson =
            buildingToJson(mobileNumber, item.building, item.buildingType, item.buildingUsage)

        val unitsJson = JSONArray().apply {
            item.units.forEach { u ->
                val unitNumber = u.unitNumber.toString().trim()
                if (unitNumber.isEmpty()) return@forEach
                val area = u.area.toDouble()
                val rooms = u.numberOfRooms.toInt()
                val park = u.numberOfParking.toInt()
                val postCode = u.postCode.toInt()
                val floorNumber = u.floorNumber.toInt()
                put(
                    JSONObject().apply {
                        put("unitNumber", unitNumber)
                        put("area", area)
                        put("numberOfRooms", rooms)
                        put("numberOfParking", park)
                        put("postCode", postCode)
                        put("floorNumber", floorNumber)
                    }
                )
            }
        }

        val unitNumberById = mutableMapOf<Long, String>().apply {
            item.units.forEach { u ->
                val num = u.unitNumber.toString().trim()
                if (num.isNotEmpty()) {
                    put(u.unitId, num)
                }
            }
        }

        val ownersJson = JSONArray()
        val tenantsJson = JSONArray()

        val ownersSeen = mutableSetOf<String>()
        val tenantsSeen = mutableSetOf<String>()

        item.role.forEach { r ->
            val u = r.user
            val mobile = (u.mobileNumber ?: "").trim()
            if (mobile.isEmpty()) return@forEach

            when (r.roles) {
                Roles.PROPERTY_OWNER, Roles.COMPLEX_MANAGER, Roles.BUILDING_MANAGER -> {
                    if (!ownersSeen.contains(mobile)) {
                        ownersSeen.add(mobile)
                        ownersJson.put(
                            JSONObject().apply {
                                put("tempId", u.userId)
                                put("ownerId", u.userId)
                                put("firstName", u.firstName)
                                put("lastName", u.lastName)
                                put("phoneNumber", u.phoneNumber)
                                put("mobileNumber", mobile)
                                put("birthday", u.birthday)
                                put("address", u.address)
                                put("email", u.email)
                                put("nationalCode", u.nationalCode)
                                put("isManager", r.roles == Roles.BUILDING_MANAGER)
                            }
                        )
                    }
                }
                Roles.PROPERTY_TENANT -> {
                    if (!tenantsSeen.contains(mobile)) {
                        tenantsSeen.add(mobile)
                        tenantsJson.put(
                            JSONObject().apply {
                                put("tempId", u.userId)
                                put("tenantId", u.userId)
                                put("firstName", u.firstName)
                                put("lastName", u.lastName)
                                put("phoneNumber", u.phoneNumber)
                                put("mobileNumber", mobile)
                                put("birthday", u.birthday)
                                put("address", u.address)
                                put("email", u.email)
                                put("numberOfTenants", 1)
                            }
                        )
                    }
                }
                else -> {}
            }
        }

        val ownerMobileByOwnerId = mutableMapOf<Long, String>().apply {
            item.role.forEach { r ->
                val u = r.user
                val mobile = (u.mobileNumber ?: "").trim()
                if (mobile.isEmpty()) return@forEach
                when (r.roles) {
                    Roles.PROPERTY_OWNER, Roles.BUILDING_MANAGER, Roles.COMPLEX_MANAGER -> {
                        put(u.userId, mobile)
                    }
                    else -> {}
                }
            }
        }

        val tenantMobileByTenantId = mutableMapOf<Long, String>().apply {
            item.role.forEach { r ->
                val u = r.user
                val mobile = (u.mobileNumber ?: "").trim()
                if (mobile.isEmpty()) return@forEach
                when (r.roles) {
                    Roles.PROPERTY_TENANT -> {
                        put(u.userId, mobile)
                    }
                    else -> {}
                }
            }
        }

        val ownerUnitsJson = JSONArray().apply {
            item.ownerUnits.forEach { ou ->
                val mobile = ownerMobileByOwnerId[ou.ownerId] ?: ""
                if (mobile.isBlank()) return@forEach
                val unitNumber = unitNumberById[ou.unitId]?.trim().orEmpty()
                if (unitNumber.isBlank()) return@forEach
                put(
                    JSONObject().apply {
                        put("ownerId", ou.ownerId)
                        put("ownerMobile", mobile)
                        put("unitId", ou.unitId)
                        put("unitNumber", unitNumber)
                        put("dang", ou.dang)
                    }
                )
            }
        }

        val tenantUnitsJson = JSONArray().apply {
            item.tenantUnits.forEach { tu ->
                val mobile = tenantMobileByTenantId[tu.tenantId] ?: ""
                if (mobile.isBlank()) return@forEach
                val unitNumber = unitNumberById[tu.unitId]?.trim().orEmpty()
                if (unitNumber.isBlank()) return@forEach
                put(
                    JSONObject().apply {
                        put("tenantId", tu.tenantId)
                        put("tenantMobile", mobile)
                        put("unitId", tu.unitId)
                        put("unitNumber", unitNumber)
                        put("startDate", tu.startDate)
                        put("endDate", tu.endDate)
                        put("status", tu.status)
                    }
                )
            }
        }

        return JSONObject().apply {
            put("building", buildingJson)
            put("mobileNumber", mobileNumber)
            put("units", unitsJson)
            put("owners", ownersJson)
            put("tenants", tenantsJson)
            put("ownerUnits", ownerUnitsJson)
            put("tenantUnits", tenantUnitsJson)
            put("idempotencyKey", "imp-${UUID.randomUUID()}")
        }
    }

    fun buildingToJson(
        mobileNumber: String,
        b: Buildings,
        t: BuildingTypes?,
        u: BuildingUsages?
    ): JSONObject =
        JSONObject().apply {
            put("name", b.name)
            put("postCode", b.postCode)
            put("street", b.street)
            put("province", b.province)
            put("state", b.state)
            put("floorCount", b.floorCount)
            put("serialNumber", b.serialNumber)

            if (b.complexId != null) {
                put("complexId", b.complexId)
            }
            if (b.buildingTypeId != null) {
                put("buildingTypeId", b.buildingTypeId)
            }
            if (b.buildingUsageId != null) {
                put("buildingUsageId", b.buildingUsageId)
            }

            put("buildingTypeName", t?.buildingTypeName ?: "")
            put("buildingUsageName", u?.buildingUsageName ?: "")

            put("fund", b.fund)
            put("userId", b.userId)
            put("mobileNumber", mobileNumber)
        }

    fun unitToJson(unit: com.example.delta.data.entity.Units): JSONObject =
        JSONObject().apply {
            put("unitNumber", unit.unitNumber)
            put("area", unit.area)
            put("numberOfRooms", unit.numberOfRooms)
            put("numberOfParking", unit.numberOfParking)
            put("floorNumber", unit.floorNumber)
            put("postCode", unit.postCode)
        }

    fun costToJson(cost: Costs): JSONObject =
        JSONObject().apply {
            put("costName", cost.costName)
            put("tempAmount", cost.tempAmount)
            put("period", cost.period)
            put("calculateMethod", cost.calculateMethod)
            put("paymentLevel", cost.paymentLevel)
            put("responsible", cost.responsible)
            put("fundType", cost.fundType)
        }

    fun <T> listToJsonArray(list: List<T>, toJsonFunc: (T) -> JSONObject): JSONArray =
        JSONArray().apply { list.forEach { put(toJsonFunc(it)) } }

    private fun formatVolleyError(tag: String, error: VolleyError): Exception {
        val resp = error.networkResponse
        return if (resp != null) {
            val body = try {
                String(resp.data ?: ByteArray(0), Charsets.UTF_8)
            } catch (_: Exception) {
                String(resp.data ?: ByteArray(0))
            }
            Log.e(tag, "HTTP ${resp.statusCode}: $body")
            Exception("HTTP ${resp.statusCode}: $body")
        } else {
            val msg = when (error) {
                is TimeoutError -> "مهلت اتصال به سرور به پایان رسید (Timeout)"
                is NoConnectionError -> "اتصال به اینترنت یا سرور برقرار نشد"
                is NetworkError -> "خطای شبکه (لطفاً اینترنت را بررسی کنید)"
                else -> "خطای نامشخص شبکه: ${error.message}"
            }
            Exception(msg, error)
        }
    }

    private fun jsonObjectRequestWithArrayBody(
        method: Int,
        url: String,
        arrayBody: JSONArray,
        onSuccess: (JSONObject) -> Unit,
        onError: (Exception) -> Unit
    ): Request<JSONObject> {
        return object : JsonObjectRequest(
            method, url, null,
            { resp -> onSuccess(resp) },
            { err -> onError(formatVolleyError("InsertBuildingsBulk", err)) }
        ) {
            override fun getBody(): ByteArray = arrayBody.toString().toByteArray(Charsets.UTF_8)
            override fun getBodyContentType(): String = "application/json; charset=utf-8"
            override fun getHeaders(): MutableMap<String, String> = hashMapOf(
                "Accept" to "application/json"
            )
        }
    }


    suspend fun fetchBuildingByIdSuspend(
        context: Context,
        buildingId: Long
    ): BulkBuildingItem = kotlinx.coroutines.suspendCancellableCoroutine { cont ->
        fetchBuildingById(
            context = context,
            buildingId = buildingId,
            onSuccess = { bulk -> if (cont.isActive) cont.resume(bulk, onCancellation = null) },
            onError = { e -> if (cont.isActive) cont.resumeWith(Result.failure(e)) }
        )
    }

    fun fetchBuildingById(
        context: Context,
        buildingId: Long,
        onSuccess: (BulkBuildingItem) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/$buildingId/full"
        Log.d("BuildingVolley", "GET $url")

        val queue = Volley.newRequestQueue(context)
        val req = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { root ->
                try {
                    Log.d("root", root.toString())
                    val bObj = root.getJSONObject("building")
                    val building = Buildings(
                        buildingId = bObj.optLong("buildingId"),
                        serialNumber = bObj.optString("serialNumber", ""),
                        complexId = if (bObj.isNull("complexId")) null else bObj.optLong("complexId"),
                        name = bObj.optString("name", ""),
                        postCode = bObj.optString("postCode", ""),
                        street = bObj.optString("street", ""),
                        province = bObj.optString("province", "Tehran"),
                        state = bObj.optString("state", "Central"),
                        buildingTypeId = if (bObj.isNull("buildingTypeId")) null else bObj.optLong("buildingTypeId"),
                        buildingUsageId = if (bObj.isNull("buildingUsageId")) null else bObj.optLong(
                            "buildingUsageId"
                        ),
                        fund = bObj.optDouble("fund").let { if (it.isNaN()) 0.0 else it },
                        userId = bObj.optLong("userId"),
                        floorCount = bObj.optInt("floorCount"),
                    )

                    fun parseUnits(arr: JSONArray): List<com.example.delta.data.entity.Units> {
                        val out = ArrayList<com.example.delta.data.entity.Units>(arr.length())
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

                    fun parseOwnerUnits(arr: JSONArray): List<OwnersUnitsCrossRef> {
                        val out = ArrayList<OwnersUnitsCrossRef>(arr.length())
                        for (i in 0 until arr.length()) {
                            val o = arr.getJSONObject(i)
                            out.add(
                                OwnersUnitsCrossRef(
                                    ownerId = o.optLong("ownerId"),
                                    unitId = o.optLong("unitId"),
                                    dang = o.optDouble("dang")
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

                    val units = parseUnits(root.optJSONArray("units") ?: JSONArray())
                    val ownerUnits = parseOwnerUnits(root.optJSONArray("ownerUnits") ?: JSONArray())
                    val tenantUnits =
                        parseTenantUnits(root.optJSONArray("tenantUnits") ?: JSONArray())

                    onSuccess(
                        BulkBuildingItem(
                            building = building,
                            buildingType = null,
                            buildingUsage = null,
                            units = units,
                            user = emptyList(),
                            role = emptyList(),
                            ownerUnits = ownerUnits,
                            tenantUnits = tenantUnits
                        )
                    )
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err -> onError(formatVolleyError("BuildingVolley(fetchBuildingById)", err)) }
        )
        queue.add(req)
    }

    fun fetchBuildingsForUser(
        context: Context,
        mobileNumber: String,
        roleId: Long? = null,
        page: Int = 1,
        limit: Int = 50,
        onSuccess: (List<BuildingWithCounts>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uriBuilder = "$baseUrl/by-user".toUri().buildUpon()
            .appendQueryParameter("mobileNumber", mobileNumber.trim())
            .appendQueryParameter("page", page.toString())
            .appendQueryParameter("limit", limit.toString())

        if (roleId != null) {
            uriBuilder.appendQueryParameter("roleId", roleId.toString())
        }

        val url = uriBuilder.build().toString()
        Log.d("BuildingVolley", "GET $url")

        val queue = Volley.newRequestQueue(context)
        val req = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { arr ->
                try {
                    Log.d("out", arr.toString())
                    val out = ArrayList<BuildingWithCounts>(arr.length())
                    for (i in 0 until arr.length()) {
                        val o: JSONObject = arr.getJSONObject(i)

                        val fundSafe = o.optDouble("fund").let { if (it.isNaN()) 0.0 else it }

                        out.add(
                            BuildingWithCounts(
                                buildingId = o.optLong("buildingId"),
                                complexId = if (o.isNull("complexId")) null else o.optLong("complexId"),
                                name = o.optString("name", ""),
                                phone = o.optString("phone", ""),
                                email = o.optString("email", ""),
                                postCode = o.optString("postCode", ""),
                                street = o.optString("street", ""),
                                province = o.optString("province", ""),
                                state = o.optString("state", ""),
                                buildingTypeId = if (o.isNull("buildingTypeId")) null else o.optLong(
                                    "buildingTypeId"
                                ),
                                buildingUsageId = if (o.isNull("buildingUsageId")) null else o.optLong(
                                    "buildingUsageId"
                                ),
                                fund = fundSafe,
                                userId = o.optLong("userId"),
                                buildingTypeName = o.optString("buildingTypeName", null),
                                buildingUsageName = o.optString("buildingUsageName", null),
                                unitsCount = o.optInt("unitsCount", 0),
                                ownersCount = o.optInt("ownersCount", 0)
                            )
                        )
                    }
                    onSuccess(out)
                } catch (e: Exception) {
                    Log.e("BuildingVolley", "Parse error", e)
                    onError(e)
                }
            },
            { err -> onError(formatVolleyError("BuildingUsage(fetch)", err)) }
        ).apply {
            retryPolicy = DefaultRetryPolicy(
                10_000,
                1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT

            )
        }
        queue.add(req)
    }

    suspend fun fetchBuildingFullSuspend(
        context: Context,
        buildingId: Long,
        fiscalYear: String?
    ): BuildingFullDto = kotlinx.coroutines.suspendCancellableCoroutine { cont ->
        fetchBuildingFull(
            context = context,
            buildingId = buildingId,
            fiscalYear = fiscalYear,
            onSuccess = { dto -> if (cont.isActive) cont.resume(dto, onCancellation = null) },
            onError = { e -> if (cont.isActive) cont.resumeWith(Result.failure(e)) }
        )
    }

    fun fetchBuildingFull(
        context: Context,
        buildingId: Long,
        fiscalYear: String?,
        onSuccess: (BuildingFullDto) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val base = "$baseUrl/$buildingId/full"
        val url = if (!fiscalYear.isNullOrBlank()) {
            "$base?fiscalYear=$fiscalYear"
        } else {
            base
        }

        Log.d("BuildingVolley", "GET $url")

        val queue = Volley.newRequestQueue(context)
        val req = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { root ->
                try {
                    val bObj = root.getJSONObject("building")
                    val building = Buildings(
                        buildingId = bObj.optLong("buildingId"),
                        complexId = if (bObj.isNull("complexId")) null else bObj.optLong("complexId"),
                        serialNumber = bObj.optString("serialNumber", ""),
                        name = bObj.optString("name", ""),
                        postCode = bObj.optString("postCode", ""),
                        street = bObj.optString("street", ""),
                        province = bObj.optString("province", "Tehran"),
                        state = bObj.optString("state", "Central"),
                        buildingTypeId = if (bObj.isNull("buildingTypeId")) null else bObj.optLong("buildingTypeId"),
                        buildingUsageId = if (bObj.isNull("buildingUsageId")) null else bObj.optLong(
                            "buildingUsageId"
                        ),
                        fund = bObj.optDouble("fund").let { if (it.isNaN()) 0.0 else it },
                        userId = bObj.optLong("userId"),
                        floorCount = bObj.optInt("floorCount"),
                    )

                    fun parseBuildingType(obj: JSONObject?): BuildingTypes? {
                        if (obj == null) return null
                        val id = when {
                            obj.has("buildingTypeId") -> obj.optLong("buildingTypeId")
                            obj.has("id") -> obj.optLong("id")
                            else -> 0L
                        }
                        val name = obj.optString("name", obj.optString("buildingTypeName", ""))
                        return BuildingTypes(
                            buildingTypeId = id,
                            buildingTypeName = name
                        )
                    }

                    fun parseBuildingUsage(obj: JSONObject?): BuildingUsages? {
                        if (obj == null) return null
                        val id = when {
                            obj.has("buildingUsageId") -> obj.optLong("buildingUsageId")
                            obj.has("id") -> obj.optLong("id")
                            else -> 0L
                        }
                        val name = obj.optString("name", obj.optString("buildingUsageName", ""))
                        return BuildingUsages(
                            buildingUsageId = id,
                            buildingUsageName = name
                        )
                    }

                    fun parseCityComplex(obj: JSONObject?): CityComplexes? {
                        if (obj == null) return null
                        val id = when {
                            obj.has("complexId") -> obj.optLong("complexId")
                            obj.has("id") -> obj.optLong("id")
                            else -> 0L
                        }
                        val name = obj.optString("name", "")
                        val address =
                            if (obj.isNull("address")) null else obj.optString("address", null)
                        return CityComplexes(
                            complexId = id,
                            name = name,
                            address = address
                        )
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
                            val urlField = o.optString(
                                "fileUrl",
                                o.optString("url", "")
                            )
                            out.add(
                                UploadedFileEntity(
                                    fileId = id,
                                    fileUrl = urlField
                                )
                            )
                        }
                        return out
                    }

                    fun parseUnits(arr: JSONArray): List<com.example.delta.data.entity.Units> {
                        val out = ArrayList<com.example.delta.data.entity.Units>(arr.length())
                        for (i in 0 until arr.length()) {
                            val o = arr.getJSONObject(i)
                            out.add(
                                com.example.delta.data.entity.Units(
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

                    fun parseOwnerUnits(arr: JSONArray): List<OwnersUnitsCrossRef> {
                        val out = ArrayList<OwnersUnitsCrossRef>(arr.length())
                        for (i in 0 until arr.length()) {
                            val o = arr.getJSONObject(i)
                            out.add(
                                OwnersUnitsCrossRef(
                                    ownerId = o.optLong("ownerId"),
                                    unitId = o.optLong("unitId"),
                                    dang = o.optDouble("dang")
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

                    fun parseCosts(arr: JSONArray): List<Costs> {
                        val out = ArrayList<Costs>(arr.length())
                        for (i in 0 until arr.length()) {
                            val o = arr.getJSONObject(i)
                            out.add(
                                Costs(
                                    costId = o.optLong("costId"),
                                    buildingId = if (o.isNull("buildingId")) null else o.optLong("buildingId"),
                                    costName = o.optString("costName"),
                                    tempAmount = o.optDouble("tempAmount", 0.0),
                                    period = runCatching {
                                        val raw = o.optString("period", "").trim()
                                        if (raw.isEmpty()) {
                                            Period.NONE
                                        } else {
                                            Period.valueOf(raw.uppercase(Locale.US))
                                        }
                                    }.getOrElse { Period.NONE },
                                    calculateMethod = runCatching {
                                        val raw = o.optString("calculateMethod", "").trim()
                                        if (raw.isEmpty()) {
                                            CalculateMethod.EQUAL
                                        } else {
                                            CalculateMethod.valueOf(raw.uppercase(Locale.US))
                                        }
                                    }.getOrElse { CalculateMethod.EQUAL },
                                    paymentLevel = runCatching {
                                        val raw = o.optString("paymentLevel", "").trim()
                                        if (raw.isEmpty()) {
                                            PaymentLevel.BUILDING
                                        } else {
                                            PaymentLevel.valueOf(raw.uppercase(Locale.US))
                                        }
                                    }.getOrElse { PaymentLevel.BUILDING },
                                    responsible = Responsible.valueOf(o.optString("responsible")),
                                    fundType = FundType.valueOf(o.optString("fundType")),
                                    chargeFlag = o.optBoolean("chargeFlag", false),
                                    dueDate = o.optString("dueDate")
                                )
                            )
                        }
                        return out
                    }

                    val buildingType = parseBuildingType(root.optJSONObject("buildingType"))
                    val buildingUsage = parseBuildingUsage(root.optJSONObject("buildingUsage"))
                    val cityComplex = parseCityComplex(root.optJSONObject("cityComplex"))
                    val files = parseFiles(root.optJSONArray("files") ?: JSONArray())

                    val units = parseUnits(root.optJSONArray("units") ?: JSONArray())
                    val ownerUnits = parseOwnerUnits(root.optJSONArray("ownerUnits") ?: JSONArray())
                    val tenantUnits =
                        parseTenantUnits(root.optJSONArray("tenantUnits") ?: JSONArray())
                    val costs = parseCosts(root.optJSONArray("costs") ?: JSONArray())
                    val defaultChargeCosts =
                        parseCosts(root.optJSONArray("defaultChargeCosts") ?: JSONArray())
                    val chargeCostsForYear =
                        parseCosts(root.optJSONArray("chargeCostsForYear") ?: JSONArray())

                    val users: List<User> = emptyList()
                    val roles: List<UserRole> = emptyList()

                    onSuccess(
                        BuildingFullDto(
                            building = building,
                            buildingType = buildingType,
                            buildingUsage = buildingUsage,
                            cityComplex = cityComplex,
                            files = files,
                            units = units,
                            users = users,
                            role = roles,
                            ownerUnits = ownerUnits,
                            tenantUnits = tenantUnits,
                            costs = costs,
                            defaultChargeCosts = defaultChargeCosts,
                            chargeCostsForYear = chargeCostsForYear
                        )
                    )
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err -> onError(formatVolleyError("BuildingVolley(fetchBuildingFull)", err)) }
        )
        queue.add(req)
    }

    fun updateBuilding(
        context: Context,
        building: Buildings,
        selectedCostNames: List<String>,
        replaceCosts: Boolean,
        fileIds: List<Long>,
        onSuccess: (Buildings) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/${building.buildingId}"

        val body = JSONObject().apply {
            put("name", building.name)
            put("street", building.street)
            put("postCode", building.postCode)
            put("province", building.province)
            put("state", building.state)
            put("buildingTypeId", building.buildingTypeId)
            put("buildingUsageId", building.buildingUsageId)
            put("complexId", building.complexId)
            put("serialNumber", building.serialNumber)
            put("floorCount", building.floorCount)
            put("costNames", JSONArray(selectedCostNames))
            put("replaceCosts", replaceCosts)
            put("fileIds", JSONArray(fileIds))
        }

        Log.d("BuildingApi", "PUT $url payload=$body")

        val queue = Volley.newRequestQueue(context)
        val request = JsonObjectRequest(
            Request.Method.PUT,
            url,
            body,
            { obj ->
                try {
                    val fundSafe = obj.optDouble("fund").let { if (it.isNaN()) 0.0 else it }

                    val updated = Buildings(
                        buildingId = obj.optLong("buildingId"),
                        complexId = if (obj.isNull("complexId")) null else obj.optLong("complexId"),
                        name = obj.optString("name", ""),
                        postCode = obj.optString("postCode", ""),
                        street = obj.optString("street", ""),
                        province = obj.optString("province", "Tehran"),
                        state = obj.optString("state", "Central"),
                        buildingTypeId = if (obj.isNull("buildingTypeId")) null else obj.optLong("buildingTypeId"),
                        buildingUsageId = if (obj.isNull("buildingUsageId")) null else obj.optLong("buildingUsageId"),
                        fund = fundSafe,
                        userId = obj.optLong("userId"),
                        serialNumber = obj.optString("serialNumber", ""),
                        floorCount = obj.optInt("floorCount"),
                    )

                    onSuccess(updated)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err ->
                onError(formatVolleyError("BuildingApi(updateBuilding)", err))
            }
        )

        queue.add(request)
    }

    fun deleteBuilding(
        context: Context,
        buildingId: Long,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/$buildingId"
        Log.d("BuildingApi", "DELETE $url")

        val queue = Volley.newRequestQueue(context)
        val req = JsonObjectRequest(
            Request.Method.DELETE,
            url,
            null,
            { _ ->
                onSuccess()
            },
            { err ->
                onError(formatVolleyError("BuildingApi(deleteBuilding)", err))
            }
        )
        queue.add(req)
    }

    fun sendBulk(
        context: Context,
        data: List<Map<String, Any>>,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val body = JSONArray(data)

        val req = object : JsonArrayRequest(
            Method.POST,
            baseUrl,
            body,
            { onSuccess() },
            { onError(it) }
        ) {}

        AppRequestQueue.getInstance(context).addToRequestQueue(req)
    }

}
