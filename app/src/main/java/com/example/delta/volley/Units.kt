package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.entity.Units
import org.json.JSONObject

class Units {
    private val baseUrl = "http://217.144.107.231:3000/units"

    fun fetchUnitsForBuilding(
        context: Context,
        buildingId: Long,
        onSuccess: (List<Units>) -> Unit,
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
                    val list = mutableListOf<Units>()
                    for (i in 0 until response.length()) {
                        val obj = response.getJSONObject(i)
                        val item = Units(
                            unitId = obj.optLong("unitId", 0L),
                            unitNumber = obj.optString("unitNumber", ""),
                            area = obj.optString("area", "0"),
                            numberOfRooms = obj.optString("numberOfRooms", "0"),
                            numberOfWarehouse = obj.optString("numberOfWarehouse", "0"),
                            numberOfParking = obj.optString("numberOfParking", "0"),
                            postCode = obj.optString("postCode", "")
                        )
                        list += item
                    }
                    onSuccess(list)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { error ->
                onError(formatVolleyError("UnitsApi(fetchUnitsForBuilding)", error))
            }
        )
        queue.add(request)
    }

    fun fetchUnitsWithOwnerForBuilding(
        context: Context,
        buildingId: Long,
        onSuccess: (List<Units>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)
        val url = "$baseUrl/with-owner?buildingId=$buildingId"
        Log.d("buildingId", buildingId.toString())
        val request = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                try {
                    val list = mutableListOf<Units>()
                    for (i in 0 until response.length()) {
                        val obj = response.getJSONObject(i)
                        val item = Units(
                            unitId = obj.optLong("unitId", 0L),
                            unitNumber = obj.optString("unitNumber", ""),
                            area = obj.optString("area", "0"),
                            numberOfRooms = obj.optString("numberOfRooms", "0"),
                            numberOfWarehouse = obj.optString("numberOfWarehouse", "0"),
                            numberOfParking = obj.optString("numberOfParking", "0"),
                            postCode = obj.optString("postCode", "")
                        )
                        Log.d("item", item.toString())
                        list += item
                    }
                    onSuccess(list)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { error ->
                onError(formatVolleyError("UnitsApi(fetchUnitsWithOwnerForBuilding)", error))
            }
        )
        queue.add(request)
    }

    fun insertUnitForBuilding(
        context: Context,
        buildingId: Long,
        unit: Units,
        onSuccess: (Units) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)

        val body = JSONObject().apply {
            put("buildingId", buildingId)
            put("unitNumber", unit.unitNumber)
            put("area", unit.area)
            put("numberOfRooms", unit.numberOfRooms)
            put("numberOfWarehouse", unit.numberOfWarehouse)
            put("numberOfParking", unit.numberOfParking)
            put("postCode", unit.postCode)
            put("floorNumber", unit.floorNumber)
        }

        Log.d("UnitVolley", "Insert unit JSON: $body")

        val request = JsonObjectRequest(
            Request.Method.POST,
            baseUrl,
            body,
            { response ->
                try {
                    val obj = response.optJSONObject("unit") ?: response
                    val created = Units(
                        unitId = obj.optLong("unitId", 0L),
                        unitNumber = obj.optString("unitNumber", ""),
                        area = obj.optString("area", "0"),
                        numberOfRooms = obj.optString("numberOfRooms", "0"),
                        numberOfWarehouse = obj.optString("numberOfWarehouse", "0"),
                        numberOfParking = obj.optString("numberOfParking", "0"),
                        postCode = obj.optString("postCode"),
                        floorNumber = obj.optInt("floorNumber")
                    )
                    onSuccess(created)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { error ->
                onError(formatVolleyError("UnitsApi(insertUnitForBuilding)", error))
            }
        )
        queue.add(request)
    }

    fun updateUnit(
        context: Context,
        buildingId: Long,
        unit: Units,
        onSuccess: (Units) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)
        val url = "$baseUrl/${unit.unitId}"

        val body = JSONObject().apply {
            put("buildingId", buildingId)
            put("unitNumber", unit.unitNumber)
            put("area", unit.area)
            put("numberOfRooms", unit.numberOfRooms)
            put("numberOfWarehouse", unit.numberOfWarehouse)
            put("numberOfParking", unit.numberOfParking)
            put("postCode", unit.postCode)
            put("floorNumber", unit.floorNumber)
        }

        Log.d("UnitVolley", "Update unit JSON: $body")

        val request = JsonObjectRequest(
            Request.Method.PUT,
            url,
            body,
            { response ->
                try {
                    val obj = response.optJSONObject("unit") ?: response
                    val updated = Units(
                        unitId = obj.optLong("unitId", 0L),
                        unitNumber = obj.optString("unitNumber", ""),
                        area = obj.optString("area", "0"),
                        numberOfRooms = obj.optString("numberOfRooms", "0"),
                        numberOfWarehouse = obj.optString("numberOfWarehouse", "0"),
                        numberOfParking = obj.optString("numberOfParking", "0"),
                        postCode = obj.optString("postCode"),
                        floorNumber = obj.optInt("floorNumber")
                    )
                    onSuccess(updated)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { error ->
                onError(formatVolleyError("UnitsApi(updateUnit)", error))
            }
        )
        queue.add(request)
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
                ""
            }

            Log.e(tag, "HTTP ${resp.statusCode}: $body")
            Exception(body)
        } else {
            Exception(error.toString())
        }
    }
}
