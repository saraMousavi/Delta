package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.entity.Tenants
import org.json.JSONArray
import org.json.JSONObject

class Tenant {
    private val baseUrl = "http://217.144.107.231:3000/tenants"

    data class TenantWithUnitDto(
        val tenant: Tenants,
        val unit: com.example.delta.data.entity.Units?
    )

    fun fetchTenantsForBuilding(
        context: Context,
        buildingId: Long,
        onSuccess: (List<Tenants>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)
        val url = "$baseUrl?buildingId=$buildingId"

        val req = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { arr ->
                try {
                    val list = mutableListOf<Tenants>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        list += parseTenant(obj)
                    }
                    onSuccess(list)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err ->
                val ex = formatVolleyError("TenantApi(fetchTenantsForBuilding)", err)
                onError(ex)
            }
        )
        queue.add(req)
    }

    private fun parseTenantWithUnit(obj: JSONObject): TenantWithUnitDto {
        val tenantObj = obj.optJSONObject("tenant") ?: obj

        val unitsArr: JSONArray? = obj.optJSONArray("units")
        val unit: com.example.delta.data.entity.Units? = if (unitsArr != null && unitsArr.length() > 0) {
            val uObj = unitsArr.getJSONObject(0)
            com.example.delta.data.entity.Units(
                unitId           = uObj.optLong("unitId", 0L),
                buildingId       = uObj.optLong("buildingId", 0L),
                unitNumber       = uObj.optString("unitNumber", ""),
                area             = uObj.optString("area", ""),
                numberOfRooms    = uObj.optString("numberOfRooms", ""),
                numberOfParking  = uObj.optString("numberOfParking", ""),
                numberOfWarehouse= uObj.optString("numberOfWarehouse", ""),
                postCode         = uObj.optString("postCode", "")
            )
        } else {
            null
        }

        return TenantWithUnitDto(
            tenant = parseTenant(tenantObj),
            unit = unit
        )
    }

    fun fetchTenantsWithUnitsByBuilding(
        context: Context,
        buildingId: Long,
        onSuccess: (List<TenantWithUnitDto>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)
        val url = "$baseUrl/with-units/$buildingId"

        val req = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { arr ->
                try {
                    val list = mutableListOf<TenantWithUnitDto>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        Log.d("obj", obj.toString())
                        list += parseTenantWithUnit(obj)
                    }
                    onSuccess(list)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err ->
                val ex = formatVolleyError("TenantApi(fetchTenantsWithUnitsByBuilding)", err)
                onError(ex)
            }
        )
        queue.add(req)
    }
    fun insertTenantWithUnit(
        context: Context,
        buildingId: Long,
        tenant: Tenants,
        unitId: Long,
        onSuccess: (TenantWithUnitDto) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)

        val tenantJson = JSONObject().apply {
            put("buildingId", buildingId)
            put("firstName", tenant.firstName)
            put("lastName", tenant.lastName)
            put("phoneNumber", tenant.phoneNumber)
            put("mobileNumber", tenant.mobileNumber)
            put("email", tenant.email)
            put("startDate", tenant.startDate)
            put("endDate", tenant.endDate)
            put("numberOfTenants", tenant.numberOfTenants)
            put("birthday", tenant.birthday)
            put("status", tenant.status)
        }

        val unitsArray = JSONArray().apply {
            put(
                JSONObject().apply {
                    put("unitId", unitId)
                    put("startDate", tenant.startDate)
                    put("endDate", tenant.endDate)
                    put("status", tenant.status)
                }
            )
        }

        val body = JSONObject().apply {
            put("tenant", tenantJson)
            put("units", unitsArray)
        }

        val req = JsonObjectRequest(
            Request.Method.POST,
            "$baseUrl/with-units",
            body,
            { obj ->
                try {
                    val dto = parseTenantWithUnit(obj)
                    onSuccess(dto)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err ->
                val ex = formatVolleyError("TenantApi(insertTenantWithUnit)", err)
                onError(ex)
            }
        )
        queue.add(req)
    }

    fun insertTenantForBuilding(
        context: Context,
        buildingId: Long,
        tenant: Tenants,
        onSuccess: (Tenants) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)

        val body = JSONObject().apply {
            put("buildingId", buildingId)
            put("firstName", tenant.firstName)
            put("lastName", tenant.lastName)
            put("phoneNumber", tenant.phoneNumber)
            put("mobileNumber", tenant.mobileNumber)
            put("email", tenant.email)
            put("startDate", tenant.startDate)
            put("endDate", tenant.endDate)
            put("numberOfTenants", tenant.numberOfTenants)
            put("birthday", tenant.birthday)
            put("status", tenant.status)
        }

        val req = JsonObjectRequest(
            Request.Method.POST,
            baseUrl,
            body,
            { obj ->
                try {
                    val t = parseTenant(obj.optJSONObject("tenant") ?: obj)
                    onSuccess(t)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err ->
                val ex = formatVolleyError("TenantApi(insertTenantForBuilding)", err)
                onError(ex)
            }
        )
        queue.add(req)
    }

    fun deleteTenant(
        context: Context,
        tenantId: Long,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)
        val url = "$baseUrl/$tenantId"

        val req = JsonObjectRequest(
            Request.Method.DELETE,
            url,
            null,
            {
                onSuccess()
            },
            { err ->
                val ex = formatVolleyError("TenantApi(deleteTenant)", err)
                onError(ex)
            }
        )
        queue.add(req)
    }

    fun getTenant(
        context: Context,
        tenantId: Long,
        onSuccess: (Tenants) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/$tenantId"
        Log.d("TenantApi", "GET $url")

        val queue = Volley.newRequestQueue(context)
        val req = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { obj ->
                try {
                    val tenant = parseTenant(obj)
                    onSuccess(tenant)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err ->
                val ex = formatVolleyError("TenantApi(getTenant)", err)
                onError(ex)
            }
        )
        queue.add(req)
    }

    private fun parseTenant(obj: JSONObject): Tenants {
        return Tenants(
            tenantId        = obj.optLong("tenantId", 0L),
            firstName       = obj.optString("firstName", ""),
            lastName        = obj.optString("lastName", ""),
            phoneNumber     = obj.optString("phoneNumber", ""),
            mobileNumber    = obj.optString("mobileNumber", ""),
            email           = obj.optString("email", ""),
            startDate       = obj.optString("startDate", ""),
            endDate         = obj.optString("endDate", ""),
            numberOfTenants = obj.optString("numberOfTenants", ""),
            birthday        = obj.optString("birthday", ""),
            status          = obj.optString("status", "")
        )
    }

    private fun formatVolleyError(
        tag: String,
        error: com.android.volley.VolleyError
    ): Exception {
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
            Log.e(tag, "No networkResponse: ${error.message}", error)
            Exception(error.toString())
        }
    }
}
