package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Units {
    private val baseUrl = "http://89.42.211.69:3000/units"

    fun fetchUnits(context: Context, onSuccess: (String) -> Units, onError: (Exception) -> Units) {
        val queue = Volley.newRequestQueue(context)
        val request = JsonArrayRequest(
            Request.Method.GET, baseUrl, null,
            { response -> onSuccess(response.toString()) },
            { error -> onError(Exception(error.message)) }
        )
        queue.add(request)
    }

    fun insertUnit(context: Context, unitJson: JSONObject, onSuccess: (String) -> Units, onError: (Exception) -> Units) {
        val queue = Volley.newRequestQueue(context)
        Log.d("UnitVolley", "Insert unit JSON: $unitJson")
        val request = JsonObjectRequest(
            Request.Method.POST, baseUrl, unitJson,
            { response -> onSuccess(response.toString()) },
            { error -> onError(Exception(error.message)) }
        )
        queue.add(request)
    }
}
