package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.entity.OwnersUnitsCrossRef
import com.example.delta.data.entity.Units
import com.example.delta.data.entity.User
import com.example.delta.data.entity.UserRoleBuildingUnitCrossRef
import com.example.delta.enums.Gender
import com.example.delta.enums.Roles
import org.json.JSONArray
import org.json.JSONObject

class Owner {
    private val baseUrl = "http://217.144.107.231:3000/owners"

    data class OwnerWithUnitsDto(
        val user: User?,
        val userRole: Building.UserRole,
        val ownerUnits: List<OwnersUnitsCrossRef>,
        val units: List<Units>,
        val userRoleCrossRefs: List<UserRoleBuildingUnitCrossRef>,
        val isManager: Boolean,
        val isResident: Boolean
    )

    fun updateOwnerUnitsAndRoleVolley(
        context: Context,
        buildingId: Long,
        userId: Long,
        units: List<OwnersUnitsCrossRef>,
        isManager: Boolean,
        isResident: Boolean,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/building/$buildingId/user/$userId/units-and-role"

        val body = JSONObject().apply {
            put("isManager", isManager)
            put("isResident", isResident)

            val arr = JSONArray()
            units.forEach { u ->
                arr.put(
                    JSONObject().apply {
                        put("unitId", u.unitId)
                        put("dang", u.dang)
                    }
                )
            }
            put("units", arr)
        }

        val queue = Volley.newRequestQueue(context)
        val req = JsonObjectRequest(
            Request.Method.PUT,
            url,
            body,
            { _ -> onSuccess() },
            { err ->
                val ex = formatVolleyError("OwnerApi(updateOwnerUnitsAndRole)", err)
                onError(ex)
            }
        )

        queue.add(req)
    }


    fun insertOwnerWithUnits(
        context: Context,
        units: List<OwnersUnitsCrossRef>,
        user: User,
        isManager: Boolean,
        isResident: Boolean,
        buildingId: Long,
        onSuccess: (OwnerWithUnitsDto) -> Unit,
        onError: (Exception) -> Unit
    ){

    val queue = Volley.newRequestQueue(context)

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
            put("isResident", isResident)
        }

        val unitsArray = JSONArray().apply {
            units.forEach { uwd ->
                val u = JSONObject().apply {
                    put("unitId", uwd.unitId)
                    put("dang", uwd.dang)
                }
                put(u)
            }
        }

        val body = JSONObject().apply {
            put("owner", ownerJson)
            put("units", unitsArray)
        }

        Log.d("OwnerVolley", "Insert owner payload: $body")

        val request = JsonObjectRequest(
            Request.Method.POST,
            baseUrl,
            body,
            { response ->
                try {
                    val dto = parseOwnerWithUnits(response)
                    onSuccess(dto)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { error ->
                onError(formatVolleyError("OwnerApi(insertOwnerWithUnits)", error))
            }
        )
        queue.add(request)
    }


    fun deleteOwner(
        context: Context,
        ownerId: Long,
        buildingId: Long,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)
        val url = "$baseUrl/$ownerId?buildingId=$buildingId"

        val request = JsonObjectRequest(
            Request.Method.DELETE,
            url,
            null,
            { _ -> onSuccess() },
            { error -> onError(formatVolleyError("OwnerApi(deleteOwner)", error)) }
        )
        queue.add(request)
    }


    fun getOwnerWithUnits(
        context: Context,
        ownerId: Long,
        onSuccess: (OwnerWithUnitsDto) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/$ownerId/with-units"
        Log.d("OwnerApi", "GET $url")

        val queue = Volley.newRequestQueue(context)
        val request = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { obj ->
                try {
                    val result = parseOwnerWithUnits(obj)
                    onSuccess(result)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err ->
                val ex = formatVolleyError("OwnerApi(getOwnerWithUnits)", err)
                onError(ex)
            }
        )
        queue.add(request)
    }

    fun getOwnersWithUnitsByBuilding(
        context: Context,
        buildingId: Long,
        onSuccess: (List<OwnerWithUnitsDto>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "http://217.144.107.231:3000/owners/building/$buildingId/list"
        Log.d("url", url.toString())
        val queue = Volley.newRequestQueue(context)
        val req = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { arr ->
                try {
                    val list = mutableListOf<OwnerWithUnitsDto>()
                    for (i in 0 until arr.length()) {
                        list += parseOwnerWithUnits(arr.getJSONObject(i))
                    }
                    onSuccess(list)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err -> onError(formatVolleyError("OwnerApi(getOwnersWithUnitsByBuilding)", err)) }
        )
        queue.add(req)
    }

    private fun parseOwnerWithUnits(obj: JSONObject): OwnerWithUnitsDto {

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

        val roleNameStr = obj.optString("roleName", "PROPERTY_OWNER")
        val roleEnum = try {
            Roles.valueOf(roleNameStr)
        } catch (_: Exception) {
            Roles.PROPERTY_OWNER
        }

        val userRole = Building.UserRole(
            user = user!!,
            roles = roleEnum
        )

        val ownerUnitsArr = obj.optJSONArray("ownerUnits") ?: JSONArray()
        val ownerUnits = mutableListOf<OwnersUnitsCrossRef>()
        for (i in 0 until ownerUnitsArr.length()) {
            val u = ownerUnitsArr.getJSONObject(i)
            ownerUnits += OwnersUnitsCrossRef(
                ownerId = u.optLong("ownerId", 0L),
                unitId = u.optLong("unitId", 0L),
                dang = u.optDouble("dang", 0.0)
            )
        }

        val unitsArr = obj.optJSONArray("units") ?: JSONArray()
        val units = mutableListOf<Units>()
        for (i in 0 until unitsArr.length()) {
            val u = unitsArr.getJSONObject(i)
            units += Units(
                unitId = u.optLong("unitId", 0L),
                buildingId = u.optLong("buildingId", 0L),
                unitNumber = u.optString("unitNumber", ""),
                area = u.optString("area", "0"),
                numberOfRooms = u.optString("numberOfRooms", "0"),
                numberOfParking = u.optString("numberOfParking", "0"),
                numberOfWarehouse = u.optString("numberOfWarehouse", "0"),
                postCode = u.optString("postCode", "")
            )
        }

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

        val isManager = obj.optBoolean("isManager", roleEnum == Roles.BUILDING_MANAGER)
        val isResident = obj.optBoolean("isResident", false)

        return OwnerWithUnitsDto(
            user = user,
            userRole = userRole,
            ownerUnits = ownerUnits,
            units = units,
            userRoleCrossRefs = userRoleCrossRefs,
            isManager = isManager,
            isResident = isResident
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

    fun updateOwnerRole(
        context: Context,
        userId: Long,
        buildingId: Long,
        isManager: Boolean,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "http://217.144.107.231:3000/userRole/update-role"

        val body = JSONObject().apply {
            put("userId", userId)
            put("buildingId", buildingId)
            put("roleName", if (isManager) "BUILDING_MANAGER" else "PROPERTY_OWNER")
        }

        val queue = Volley.newRequestQueue(context)
        val req = JsonObjectRequest(
            Request.Method.PUT,
            url,
            body,
            { onSuccess() },
            { err -> onError(formatVolleyError("OwnerApi(updateOwnerRole)", err)) }
        )
        queue.add(req)
    }


    fun updateOwnerUnitsAndRole(
        context: Context,
        userId: Long,
        buildingId: Long,
        units: List<OwnersUnitsCrossRef>,
        isManager: Boolean,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/building/$buildingId/user/$userId/units-and-role"

        val unitsArray = JSONArray().apply {
            units.forEach { uwd ->
                put(
                    JSONObject().apply {
                        put("unitId", uwd.unitId)
                        put("dang", uwd.dang)
                    }
                )
            }
        }

        val body = JSONObject().apply {
            put("units", unitsArray)
            put("isManager", isManager)
        }

        val queue = Volley.newRequestQueue(context)
        val req = JsonObjectRequest(
            Request.Method.PUT,
            url,
            body,
            { _ -> onSuccess() },
            { err -> onError(formatVolleyError("OwnerApi(updateOwnerUnitsAndRole)", err)) }
        )

        queue.add(req)
    }
}