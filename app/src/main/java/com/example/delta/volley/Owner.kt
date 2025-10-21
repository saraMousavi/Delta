package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Owner {
    private val baseUrl = "http://217.144.107.231:3000/owner"

    fun fetchOwners(context: Context, onSuccess: (String) -> Units, onError: (Exception) -> Units) {
        val queue = Volley.newRequestQueue(context)
        val request = JsonArrayRequest(
            Request.Method.GET, baseUrl, null,
            { response -> onSuccess(response.toString()) },
            { error -> onError(Exception(error.message)) }
        )
        queue.add(request)
    }

    fun insertOwner(context: Context, ownerJson: JSONObject, onSuccess: (String) -> Units, onError: (Exception) -> Units) {
        val queue = Volley.newRequestQueue(context)
        Log.d("OwnerVolley", "Insert owner JSON: $ownerJson")
        val request = JsonObjectRequest(
            Request.Method.POST, baseUrl, ownerJson,
            { response -> onSuccess(response.toString()) },
            { error -> onError(Exception(error.message)) }
        )
        queue.add(request)
    }
}
