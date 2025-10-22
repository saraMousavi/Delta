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
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Owners
import com.example.delta.data.entity.OwnersUnitsCrossRef
import com.example.delta.data.entity.Tenants
import com.example.delta.data.entity.TenantsUnitsCrossRef
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class Building {
    private val baseUrl = "http://217.144.107.231:3000/building"

    // ---------- Bulk payload model ----------
    data class BulkItem(
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
        context: Context,
        items: List<BulkItem>,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)
        val payload = JSONArray().apply { items.forEach { put(makeBulkItemJson(it)) } }
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
    fun makeBulkItemJson(item: BulkItem): JSONObject {
        val unitTemp = mutableMapOf<Long, String>()
        val ownerTemp = mutableMapOf<Long, String>()
        val tenantTemp = mutableMapOf<Long, String>()

        val building = buildingToJson(item.building, item.buildingType, item.buildingUsage)

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
    fun buildingToJson(b: Buildings, t: BuildingTypes?, u: BuildingUsages?): JSONObject =
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

}
