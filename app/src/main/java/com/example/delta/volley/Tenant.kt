package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Tenant {
    private val baseUrl = "http://89.42.211.69:3000/tenants"

    fun fetchTenants(context: Context, onSuccess: (String) -> Units, onError: (Exception) -> Units) {
        val queue = Volley.newRequestQueue(context)
        val request = JsonArrayRequest(
            Request.Method.GET, baseUrl, null,
            { response -> onSuccess(response.toString()) },
            { error -> onError(Exception(error.message)) }
        )
        queue.add(request)
    }

    fun insertTenant(context: Context, tenantJson: JSONObject, onSuccess: (String) -> Units, onError: (Exception) -> Units) {
        val queue = Volley.newRequestQueue(context)
        Log.d("TenantVolley", "Insert tenant JSON: $tenantJson")
        val request = JsonObjectRequest(
            Request.Method.POST, baseUrl, tenantJson,
            { response -> onSuccess(response.toString()) },
            { error -> onError(Exception(error.message)) }
        )
        queue.add(request)
    }
}
