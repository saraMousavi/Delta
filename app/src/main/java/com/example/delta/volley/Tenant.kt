package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.entity.TenantsUnitsCrossRef
import com.example.delta.data.entity.Units
import com.example.delta.data.entity.User
import com.example.delta.data.entity.UserRoleBuildingUnitCrossRef
import com.example.delta.enums.Gender
import com.example.delta.enums.Roles
import org.json.JSONArray
import org.json.JSONObject

class Tenant {
    private val baseUrl = "http://217.144.107.231:3000/tenants"

    data class TenantWithUnitDto(
        val user: User?,
        val unit: Units,
        val userRole: Building.UserRole,
        val tenantUnit: TenantsUnitsCrossRef,
        val userRoleCrossRefs: List<UserRoleBuildingUnitCrossRef>
    )

    fun getTenantWithUnit(
        context: Context,
        tenantId: Long,
        onSuccess: (TenantWithUnitDto) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/$tenantId/with-units"
        Log.d("TenantApi", "GET $url")

        val queue = Volley.newRequestQueue(context)
        val req = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { obj ->
                try {
                    val dto = parseTenantWithUnit(obj)
                    onSuccess(dto)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err ->
                val ex = formatVolleyError("TenantApi(getTenantWithUnit)", err)
                onError(ex)
            }
        )
        queue.add(req)
    }



    private fun parseTenantWithUnit(obj: JSONObject): TenantWithUnitDto {
        val userObj = obj.optJSONObject("user")

        val user = if (userObj != null) {
            User(
                userId = userObj.optLong("userId", 0L),
                mobileNumber = userObj.optString("mobileNumber", ""),
                password = "",
                firstName = userObj.optString("firstName", ""),
                lastName = userObj.optString("lastName", ""),
                email = userObj.optString("email", ""),
                gender = try {
                    Gender.valueOf(userObj.optString("gender", "MALE"))
                } catch (_: Exception) {
                    Gender.MALE
                },
                profilePhoto = userObj.optString("profilePhoto", ""),
                nationalCode = userObj.optString("nationalCode", ""),
                address = userObj.optString("address", ""),
                phoneNumber = userObj.optString("phoneNumber", ""),
                birthday = userObj.optString("birthday", "")
            )
        } else null

        val roleNameStr = obj.optString("roleName", "PROPERTY_TENANT")
        val roleEnum = try {
            Roles.valueOf(roleNameStr)
        } catch (_: Exception) {
            Roles.PROPERTY_TENANT
        }

        val userRole = Building.UserRole(
            user = user!!,
            roles = roleEnum
        )

        val unitsObj = obj.getJSONObject("unit")
        val unit = Units(
            unitId = unitsObj.optLong("unitId", 0L),
            buildingId = unitsObj.optLong("buildingId", 0L),
            unitNumber = unitsObj.optString("unitNumber", ""),
            area = unitsObj.optString("area", "0"),
            numberOfRooms = unitsObj.optString("numberOfRooms", "0"),
            numberOfParking = unitsObj.optString("numberOfParking", "0"),
            numberOfWarehouse = unitsObj.optString("numberOfWarehouse", "0"),
            postCode = unitsObj.optString("postCode", "")
        )

        val tenantUnitsObj = obj.getJSONObject("tenantUnits")
        val tenantUnits = TenantsUnitsCrossRef(
            tenantId = tenantUnitsObj.optLong("tenantId", 0L),
            unitId = tenantUnitsObj.optLong("unitId", 0L),
            numberOfTenants = tenantUnitsObj.optString("numberOfTenants", ""),
            startDate = tenantUnitsObj.optString("startDate", ""),
            endDate = tenantUnitsObj.optString("endDate", ""),
            status = tenantUnitsObj.optString("status", "")
        )

        val userRoleCrossArr = obj.optJSONArray("userRoleCrossRefs") ?: JSONArray()
        val userRoleCrossRefs = mutableListOf<UserRoleBuildingUnitCrossRef>()
        for (i in 0 until userRoleCrossArr.length()) {
            val c = userRoleCrossArr.getJSONObject(i)
            userRoleCrossRefs += UserRoleBuildingUnitCrossRef(
                roleId = c.optLong("roleId", 0L),
                userId = c.optLong("userId", 0L),
                buildingId = c.optLong("buildingId", 0L),
                unitId = c.optLong("unitId", 0L)
            )
        }

        return TenantWithUnitDto(
            user = user,
            userRole = userRole,
            tenantUnit = tenantUnits,
            unit = unit,
            userRoleCrossRefs = userRoleCrossRefs
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
        user: User,
        tenantUnit: TenantsUnitsCrossRef,
        onSuccess: (TenantWithUnitDto) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)

        val userJson = JSONObject().apply {
            put("userId", if (user.userId == 0L) JSONObject.NULL else user.userId)
            put("firstName", user.firstName)
            put("lastName", user.lastName)
            put("email", user.email)
            put("phoneNumber", user.phoneNumber)
            put("mobileNumber", user.mobileNumber)
            put("address", user.address)
            put("birthday", user.birthday)
        }

        val tenantJson = JSONObject().apply {
            put("buildingId", buildingId)
            put("numberOfTenants", tenantUnit.numberOfTenants)
            put("startDate", tenantUnit.startDate)
            put("endDate", tenantUnit.endDate)
            put("status", tenantUnit.status)
        }

        val unitsArray = JSONArray().apply {
            put(
                JSONObject().apply {
                    put("unitId", tenantUnit.unitId)
                    put("numberOfTenants", tenantUnit.numberOfTenants)
                    put("startDate", tenantUnit.startDate)
                    put("endDate", tenantUnit.endDate)
                    put("status", tenantUnit.status)
                }
            )
        }

        val body = JSONObject().apply {
            put("user", userJson)
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

    fun updateTenant(
        context: Context,
        tenantId: Long,
        buildingId: Long,
        tenantUnit: TenantsUnitsCrossRef?,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit,
        rentDebt: Double,
        mortgageDebt : Double
    ) {
        val queue = Volley.newRequestQueue(context)
        val url = "$baseUrl/$tenantId"

        val body = JSONObject().apply {
            put("unitId", tenantUnit!!.unitId)
            put("buildingId", buildingId)
            put("numberOfTenants", tenantUnit.numberOfTenants)
            put("startDate", tenantUnit.startDate)
            put("endDate", tenantUnit.endDate)
            put("status", tenantUnit.status)
            put("rentAmount", rentDebt)
            put("depositAmount", mortgageDebt)
        }

        val req = object : JsonObjectRequest(
            Request.Method.PUT,
            url,
            body,
            { _ -> onSuccess() },
            { err -> onError(formatVolleyError("TenantApi(updateTenant)", err)) }
        ) {
            override fun getBodyContentType(): String = "application/json; charset=utf-8"
        }

        queue.add(req)
    }


    fun getTenantForUnitInBuilding(
        context: Context,
        buildingId: Long,
        unitId: Long,
        onSuccess: (TenantWithUnitDto?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        fetchTenantsWithUnitsByBuilding(
            context = context,
            buildingId = buildingId,
            onSuccess = { list ->
                val match = list.firstOrNull { it.unit.unitId == unitId }
                onSuccess(match)
            },
            onError = { e -> onError(e) }
        )
    }




}
