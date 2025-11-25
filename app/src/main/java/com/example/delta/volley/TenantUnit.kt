package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.entity.Units
import org.json.JSONObject

class TenantUnit {
    private val baseUrl = "http://89.42.211.69:3000/tenantunits"

    fun fetchTenantUnitsByTenant(
        context: Context,
        tenantId: Long,
        onSuccess: (List<Units>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)
        val url = "$baseUrl/tenant/$tenantId/units"

        val request = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { arr ->
                try {
                    val list = mutableListOf<Units>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val unit = Units(
                            unitId = obj.optLong("unitId", 0L),
                            unitNumber = obj.optString("unitNumber", ""),
                            area = obj.optString("area", "0"),
                            numberOfRooms = obj.optString("numberOfRooms", "0"),
                            numberOfWarehouse = obj.optString("numberOfWarehouse", "0"),
                            numberOfParking = obj.optString("numberOfParking", "0"),
                            postCode = obj.optString("postCode")
                        )
                        list += unit
                    }
                    onSuccess(list)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { error ->
                onError(formatVolleyError("TenantUnitApi(fetchTenantUnitsByTenant)", error))
            }
        )
        queue.add(request)
    }

    fun insertTenantUnit(
        context: Context,
        tenantId: Long,
        unitId: Long,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)

        val body = JSONObject().apply {
            put("tenantId", tenantId)
            put("unitId", unitId)
        }

        Log.d("TenantUnitVolley", "Insert tenant-unit JSON: $body")

        val request = JsonObjectRequest(
            Request.Method.POST,
            baseUrl,
            body,
            {
                onSuccess()
            },
            { error ->
                onError(formatVolleyError("TenantUnitApi(insertTenantUnit)", error))
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
