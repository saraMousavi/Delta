package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.entity.Owners
import com.example.delta.data.entity.OwnersUnitsCrossRef
import com.example.delta.data.entity.UnitWithDang
import com.example.delta.data.entity.Units
import org.json.JSONArray
import org.json.JSONObject

class Owner {
    private val baseUrl = "http://217.144.107.231:3000/owners"

    data class OwnerWithUnitsDto(
        val owner: Owners,
        val units: List<UnitWithDang>
    )

    fun fetchOwnersForBuilding(
        context: Context,
        buildingId: Long,
        onSuccess: (List<Owners>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)
        val url = "$baseUrl?buildingId=$buildingId"

        val request = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                try {
                    val list = mutableListOf<Owners>()
                    for (i in 0 until response.length()) {
                        val obj = response.getJSONObject(i)
                        val item = Owners(
                            ownerId = obj.optLong("ownerId", 0L),
                            firstName = obj.optString("firstName", ""),
                            lastName = obj.optString("lastName", ""),
                            email = obj.optString("email", ""),
                            phoneNumber = obj.optString("phoneNumber", ""),
                            mobileNumber = obj.optString("mobileNumber", ""),
                            address = obj.optString("address", ""),
                            birthday = obj.optString("birthday", "")
                        )
                        list += item
                    }
                    onSuccess(list)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { error ->
                onError(formatVolleyError("OwnerApi(fetchOwnersForBuilding)", error))
            }
        )
        queue.add(request)
    }

    fun insertOwnerWithUnits(
        context: Context,
        owner: Owners,
        units: List<OwnersUnitsCrossRef>,
        isManager: Boolean,
        buildingId: Long,
        onSuccess: (OwnerWithUnitsDto) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)

        val ownerJson = JSONObject().apply {
            put("ownerId", if (owner.ownerId == 0L) JSONObject.NULL else owner.ownerId)
            put("firstName", owner.firstName)
            put("lastName", owner.lastName)
            put("email", owner.email)
            put("phoneNumber", owner.phoneNumber)
            put("mobileNumber", owner.mobileNumber)
            put("address", owner.address)
            put("birthday", owner.birthday)
            put("buildingId", buildingId)
            put("isManager", isManager)
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
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)
        val url = "$baseUrl/$ownerId"

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
        val url = "$baseUrl/building/$buildingId/with-units"
        Log.d("OwnerApi", "GET $url")

        val queue = Volley.newRequestQueue(context)
        val request = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { arr ->
                try {
                    val list = mutableListOf<OwnerWithUnitsDto>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        list += parseOwnerWithUnits(obj)
                    }
                    onSuccess(list)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err ->
                val ex = formatVolleyError("OwnerApi(getOwnersWithUnitsByBuilding)", err)
                onError(ex)
            }
        )
        queue.add(request)
    }

    private fun parseOwnerWithUnits(obj: JSONObject): OwnerWithUnitsDto {
        val ownerObj = obj.getJSONObject("owner")
        val unitsArr = obj.optJSONArray("units") ?: JSONArray()

        val owner = Owners(
            ownerId = ownerObj.optLong("ownerId", 0L),
            firstName = ownerObj.optString("firstName", ""),
            lastName = ownerObj.optString("lastName", ""),
            email = ownerObj.optString("email", ""),
            phoneNumber = ownerObj.optString("phoneNumber", ""),
            mobileNumber = ownerObj.optString("mobileNumber", ""),
            address = ownerObj.optString("address", ""),
            birthday = ownerObj.optString("birthday", "")
        )

        val unitsWithDang = mutableListOf<UnitWithDang>()

        for (i in 0 until unitsArr.length()) {
            val u = unitsArr.getJSONObject(i)
            val unit = Units(
                unitId = u.optLong("unitId", 0L),
                unitNumber = u.optString("unitNumber", ""),
                area = u.optString("area", "0"),
                numberOfRooms = u.optString("numberOfRooms", "0"),
                numberOfWarehouse = u.optString("numberOfWarehouse", "0"),
                numberOfParking = u.optString("numberOfParking", "0"),
                postCode = u.optString("postCode")
            )

            val unitWithDang = UnitWithDang(
                unit = unit,
                dang = u.optDouble("dang", 0.0)
            )
            unitsWithDang += unitWithDang
        }

        return OwnerWithUnitsDto(owner = owner, units = unitsWithDang)
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
