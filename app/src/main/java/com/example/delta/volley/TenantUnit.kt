package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class TenantUnit {
    private val baseUrl = "http://89.42.211.69:3000/tenantunits"

    fun fetchTenantUnitsByTenant(context: Context, tenantId: String, onSuccess: (String) -> Units, onError: (Exception) -> Units) {
        val queue = Volley.newRequestQueue(context)
        val url = "$baseUrl/tenant/$tenantId/units"
        val request = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response -> onSuccess(response.toString()) },
            { error -> onError(Exception(error.message)) }
        )
        queue.add(request)
    }

    fun insertTenantUnit(context: Context, tenantUnitJson: JSONObject, onSuccess: (String) -> Units, onError: (Exception) -> Units) {
        val queue = Volley.newRequestQueue(context)
        Log.d("TenantUnitVolley", "Insert tenant-unit JSON: $tenantUnitJson")
        val request = JsonObjectRequest(
            Request.Method.POST, baseUrl, tenantUnitJson,
            { response -> onSuccess(response.toString()) },
            { error -> onError(Exception(error.message)) }
        )
        queue.add(request)
    }
}
