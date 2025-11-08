package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.dao.OwnersDao
import com.example.delta.data.entity.Units
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.BuildingUsages
import com.example.delta.data.entity.BuildingTypes
import com.example.delta.data.entity.BuildingWithCounts
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Owners
import com.example.delta.data.entity.OwnersUnitsCrossRef
import com.example.delta.data.entity.Tenants
import com.example.delta.data.entity.TenantsUnitsCrossRef
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import androidx.core.net.toUri

class Building {
    private val baseUrl = "http://217.144.107.231:3000/building"

    // ---------- Bulk payload model ----------
    data class BulkBuildingItem(
        val building: Buildings,
        val buildingType: BuildingTypes?,
        val buildingUsage: BuildingUsages?,
        val units: List<Units> = emptyList(),
        val owners: List<Owners> = emptyList(),
        val tenants: List<Tenants> = emptyList(),
        val ownerUnits: List<OwnersUnitsCrossRef> = emptyList(),
        val tenantUnits: List<TenantsUnitsCrossRef> = emptyList()
    )

    // ---------- Public API ----------
    fun insertBuildingsBulk(
        mobileNumber: String,
        context: Context,
        items: List<BulkBuildingItem>,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)
        val payload = JSONArray().apply { items.forEach { put(makeBulkItemJson(mobileNumber, it)) } }
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
        ownersJsonArray: JSONArray,
        tenantsJsonArray: JSONArray,
        ownerUnitsJsonArray: JSONArray,
        tenantUnitsJsonArray: JSONArray,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)
        val item = JSONObject().apply {
            put("building", buildingJson)
            put("mobileNumber", mobileNumber)
            put("units", unitsJsonArray)
            put("owners", ownersJsonArray)
            put("tenants", tenantsJsonArray)
            put("ownerUnits", ownerUnitsJsonArray)
            put("tenantUnits", tenantUnitsJsonArray)
            put("idempotencyKey", "imp-${java.util.UUID.randomUUID()}")
        }
        val payload = JSONArray().put(item)
        Log.d("BuildingVolley", "Single->bulk payload: $payload")

        val req = jsonObjectRequestWithArrayBody(
            Request.Method.POST,
            baseUrl,
            payload,
            onSuccess = { resp -> onSuccess(resp.toString()) },
            onError = onError
        )
        queue.add(req)
    }


    // ---------- Builders to match server bulk-import shape (with tempIds) ----------
    fun makeBulkItemJson(mobileNumber: String, item: BulkBuildingItem): JSONObject {
        val unitTemp = mutableMapOf<Long, String>()
        val ownerTemp = mutableMapOf<Long, String>()
        val tenantTemp = mutableMapOf<Long, String>()

        val building = buildingToJson(mobileNumber, item.building, item.buildingType, item.buildingUsage)

        val units = JSONArray().apply {
            item.units.forEach { u ->
                val unitNumber = (u.unitNumber).toString().trim()
                val area = (u.area).toDouble()
                val rooms = (u.numberOfRooms).toInt()
                val park = (u.numberOfParking).toInt()
                if (unitNumber.isEmpty()) return@forEach
                val tid = unitTemp.getOrPut(u.unitId) { "u-${UUID.randomUUID()}" }
                put(JSONObject().apply {
                    put("tempId", tid)
                    put("unitNumber", unitNumber)
                    put("area", area)
                    put("numberOfRooms", rooms)
                    put("numberOfParking", park)
                })
            }
        }

        val owners = JSONArray().apply {
            item.owners.forEach { o ->
                val tid = ownerTemp.getOrPut(o.ownerId) { "o-${UUID.randomUUID()}" }
                put(JSONObject().apply {
                    put("tempId", tid)
                    put("firstName", o.firstName)
                    put("lastName", o.lastName)
                    put("phoneNumber", o.phoneNumber)
                    put("mobileNumber", o.mobileNumber)
                    put("birthday", o.birthday)
                    put("address", o.address)
                    put("email", o.email)
                })
            }
        }

        val tenants = JSONArray().apply {
            item.tenants.forEach { t ->
                val tid = tenantTemp.getOrPut(t.tenantId) { "t-${UUID.randomUUID()}" }
                put(JSONObject().apply {
                    put("tempId", tid)
                    put("firstName", t.firstName)
                    put("lastName", t.lastName)
                    put("phoneNumber", t.phoneNumber)
                    put("mobileNumber", t.mobileNumber)
                    put("email", t.email)
                    put("birthday", t.birthday)
                    put("numberOfTenants", t.numberOfTenants.toInt())
                    put("startDate", t.startDate)
                    put("endDate", t.endDate)
                    put("status", t.status)
                })
            }
        }

        val ownerUnits = JSONArray().apply {
            item.ownerUnits.forEach { ou ->
                val oTid = ownerTemp.getOrPut(ou.ownerId) { "o-${UUID.randomUUID()}" }
                val uTid = unitTemp.getOrPut(ou.unitId) { "u-${UUID.randomUUID()}" }
                put(JSONObject().apply {
                    put("ownerTempId", oTid)
                    put("unitTempId", uTid)
                    put("dang", ou.dang.toInt())
                })
            }
        }

        val tenantUnits = JSONArray().apply {
            item.tenantUnits.forEach { tu ->
                val tTid = tenantTemp.getOrPut(tu.tenantId) { "t-${UUID.randomUUID()}" }
                val uTid = unitTemp.getOrPut(tu.unitId) { "u-${UUID.randomUUID()}" }
                put(JSONObject().apply {
                    put("tenantTempId", tTid)
                    put("unitTempId", uTid)
                    put("startDate", tu.startDate)
                    put("endDate", tu.endDate)
                    put("status", tu.status)
                })
            }
        }

        return JSONObject().apply {
            put("building", building)
            put("units", units)
            put("owners", owners)
            put("tenants", tenants)
            put("ownerUnits", ownerUnits)
            put("tenantUnits", tenantUnits)
            put("idempotencyKey", "imp-${UUID.randomUUID()}")
        }
    }

    // ---------- Primitive JSON mappers ----------
    fun buildingToJson(mobileNumber: String, b: Buildings, t: BuildingTypes?, u: BuildingUsages?): JSONObject =

        JSONObject().apply {
            put("name", b.name)
            put("phone", b.phone)
            put("email", b.email)
            put("postCode", b.postCode)
            put("street", b.street)
            put("province", b.province)
            put("state", b.state)
            put("buildingTypeName", t?.buildingTypeName ?: "")
            put("buildingUsageName", u?.buildingUsageName ?: "")
            put("fund", b.fund)
            put("userId", b.userId)
            put("mobileNumber", mobileNumber)
        }

    fun unitToJson(unit: Units): JSONObject =
        JSONObject().apply {
            put("unitNumber", unit.unitNumber)
            put("area", unit.area)
            put("numberOfRooms", unit.numberOfRooms)
            put("numberOfParking", unit.numberOfParking)
        }

    fun ownerToJson(owner: Owners): JSONObject =
        JSONObject().apply {
            put("firstName", owner.firstName)
            put("lastName", owner.lastName)
            put("phoneNumber", owner.phoneNumber)
            put("mobileNumber", owner.mobileNumber)
            put("birthday", owner.birthday)
            put("address", owner.address)
            put("email", owner.email)
        }

    fun tenantToJson(tenant: Tenants): JSONObject =
        JSONObject().apply {
            put("firstName", tenant.firstName)
            put("lastName", tenant.lastName)
            put("phoneNumber", tenant.phoneNumber)
            put("mobileNumber", tenant.mobileNumber)
            put("email", tenant.email)
            put("birthday", tenant.birthday)
            put("numberOfTenants", tenant.numberOfTenants)
            put("startDate", tenant.startDate)
            put("endDate", tenant.endDate)
            put("status", tenant.status)
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

    fun tenantUnitListToJsonArray(tenantUnitMap: Map<Tenants, Units>): JSONArray {
        val jsonArray = JSONArray()
        tenantUnitMap.forEach { tu ->
            val jsonObject = JSONObject().apply {
                put("tenantId", tu.key.tenantId)
                put("unitId", tu.value.unitId)
                put("startDate", tu.key.startDate)
                put("endDate", tu.key.endDate)
                put("status", tu.key.status)
            }
            jsonArray.put(jsonObject)
        }
        Log.d("jsonArray", jsonArray.toString())
        return jsonArray
    }

    private fun formatVolleyError(tag: String, error: com.android.volley.VolleyError): Exception {
        val resp = error.networkResponse
        if (resp != null) {
            val charsetName = resp.headers?.get("Content-Type")
                ?.substringAfter("charset=", "UTF-8") ?: "UTF-8"
            val body = try { String(resp.data ?: ByteArray(0), charset(charsetName)) }
            catch (_: Exception) { String(resp.data ?: ByteArray(0)) }
            Log.e(tag, "HTTP ${resp.statusCode}")
            Log.e(tag, "Headers: ${resp.headers}")
            Log.e(tag, "Body: $body")
            return Exception("HTTP ${resp.statusCode}: $body")
        } else {
            Log.e(tag, "No networkResponse: ${error.message}", error)
            return Exception(error.toString())
        }
    }

    // Optional helper that builds ownerUnits JSON with real ids (legacy path)
    suspend fun buildOwnerUnitsJsonArray(
        ownersList: List<Owners>,
        ownerUnitMap: Map<Owners, List<OwnersUnitsCrossRef>>,
        ownersDao: OwnersDao
    ): JSONArray {
        val arr = JSONArray()
        for (owner in ownersList) {
            val ownerId = ownersDao.insertOwners(owner)
            val ownerUnits = ownerUnitMap[owner] ?: emptyList()
            ownerUnits.forEach { ou ->
                arr.put(
                    JSONObject().apply {
                        put("ownerId", ownerId)
                        put("unitId", ou.unitId)
                        put("dang", ou.dang.toInt())
                    }
                )
            }
        }
        return arr
    }
    // sends JSONArray body, expects JSONObject response
    private fun jsonObjectRequestWithArrayBody(
        method: Int,
        url: String,
        arrayBody: JSONArray,
        onSuccess: (JSONObject) -> Unit,
        onError: (Exception) -> Unit
    ): com.android.volley.Request<JSONObject> {
        return object : com.android.volley.toolbox.JsonObjectRequest(method, url, null,
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
    // com/example/delta/volley/Building.kt

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
                    val bObj = root.getJSONObject("building")
                    val building = Buildings(
                        buildingId      = bObj.optLong("buildingId"),
                        complexId       = if (bObj.isNull("complexId")) null else bObj.optLong("complexId"),
                        name            = bObj.optString("name", ""),
                        phone           = bObj.optString("phone", ""),
                        email           = bObj.optString("email", ""),
                        postCode        = bObj.optString("postCode", ""),
                        street          = bObj.optString("street", ""),
                        province        = bObj.optString("province", ""),
                        state           = bObj.optString("state", ""),
                        buildingTypeId  = if (bObj.isNull("buildingTypeId")) null else bObj.optLong("buildingTypeId"),
                        buildingUsageId = if (bObj.isNull("buildingUsageId")) null else bObj.optLong("buildingUsageId"),
                        fund            = bObj.optDouble("fund").let { if (it.isNaN()) 0.0 else it },
                        userId          = bObj.optLong("userId")
                    )

                    fun parseUnits(arr: JSONArray): List<Units> {
                        val out = ArrayList<Units>(arr.length())
                        for (i in 0 until arr.length()) {
                            val o = arr.getJSONObject(i)
                            out.add(
                                Units(
                                    unitId           = o.optLong("unitId"),
                                    buildingId       = o.optLong("buildingId"),
                                    unitNumber       = o.optString("unitNumber"),
                                    area             = o.optDouble("area").toString(),
                                    numberOfRooms    = o.optInt("numberOfRooms", 0).toString(),
                                    numberOfParking  = o.optInt("numberOfParking", 0).toString(),
                                    numberOfWarehouse = o.optInt("numberOfWarehouse", 0).toString()
                                )
                            )
                        }
                        return out
                    }

                    fun parseOwners(arr: JSONArray): List<Owners> {
                        val out = ArrayList<Owners>(arr.length())
                        for (i in 0 until arr.length()) {
                            val o = arr.getJSONObject(i)
                            out.add(
                                Owners(
                                    ownerId      = o.optLong("ownerId"),
                                    firstName    = o.optString("firstName"),
                                    lastName     = o.optString("lastName"),
                                    phoneNumber  = o.optString("phoneNumber"),
                                    mobileNumber = o.optString("mobileNumber"),
                                    birthday     = o.optString("birthday"),
                                    address      = o.optString("address"),
                                    email        = o.optString("email")
                                )
                            )
                        }
                        return out
                    }

                    fun parseTenants(arr: JSONArray): List<Tenants> {
                        val out = ArrayList<Tenants>(arr.length())
                        for (i in 0 until arr.length()) {
                            val o = arr.getJSONObject(i)
                            out.add(
                                Tenants(
                                    tenantId       = o.optLong("tenantId"),
                                    firstName      = o.optString("firstName"),
                                    lastName       = o.optString("lastName"),
                                    phoneNumber    = o.optString("phoneNumber"),
                                    mobileNumber   = o.optString("mobileNumber"),
                                    email          = o.optString("email"),
                                    birthday       = o.optString("birthday"),
                                    numberOfTenants= o.optInt("numberOfTenants", 1).toString(),
                                    startDate      = o.optString("startDate"),
                                    endDate        = o.optString("endDate"),
                                    status         = o.optString("status")
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
                                    unitId  = o.optLong("unitId"),
                                    dang    = o.optDouble("dang")
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
                                    unitId   = o.optLong("unitId"),
                                    startDate= o.optString("startDate"),
                                    endDate  = o.optString("endDate"),
                                    status   = o.optString("status")
                                )
                            )
                        }
                        return out
                    }

                    val units       = parseUnits(root.optJSONArray("units") ?: JSONArray())
                    val owners      = parseOwners(root.optJSONArray("owners") ?: JSONArray())
                    val tenants     = parseTenants(root.optJSONArray("tenants") ?: JSONArray())
                    val ownerUnits  = parseOwnerUnits(root.optJSONArray("ownerUnits") ?: JSONArray())
                    val tenantUnits = parseTenantUnits(root.optJSONArray("tenantUnits") ?: JSONArray())

                    onSuccess(
                        BulkBuildingItem(
                            building      = building,
                            buildingType  = null,
                            buildingUsage = null,
                            units         = units,
                            owners        = owners,
                            tenants       = tenants,
                            ownerUnits    = ownerUnits,
                            tenantUnits   = tenantUnits
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
                                buildingTypeId = if (o.isNull("buildingTypeId")) null else o.optLong("buildingTypeId"),
                                buildingUsageId = if (o.isNull("buildingUsageId")) null else o.optLong("buildingUsageId"),
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
            { err ->
                onError(formatVolleyError("BuildingVolley(fetchBuildingsForUser)", err))
            }
        )
        queue.add(req)
    }



}
