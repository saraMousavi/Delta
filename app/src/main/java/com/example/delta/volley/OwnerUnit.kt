package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class OwnerUnit {
    private val baseUrl = "http://89.42.211.69:3000/ownerunits"

    fun fetchOwnerUnitsByOwner(context: Context, ownerId: String, onSuccess: (String) -> Units, onError: (Exception) -> Units) {
        val queue = Volley.newRequestQueue(context)
        val url = "$baseUrl/owner/$ownerId/units"
        val request = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response -> onSuccess(response.toString()) },
            { error -> onError(Exception(error.message)) }
        )
        queue.add(request)
    }

    fun insertOwnerUnit(context: Context, ownerUnitJson: JSONObject, onSuccess: (String) -> Units, onError: (Exception) -> Units) {
        val queue = Volley.newRequestQueue(context)
        Log.d("OwnerUnitVolley", "Insert owner-unit JSON: $ownerUnitJson")
        val request = JsonObjectRequest(
            Request.Method.POST, baseUrl, ownerUnitJson,
            { response -> onSuccess(response.toString()) },
            { error -> onError(Exception(error.message)) }
        )
        queue.add(request)
    }
}
