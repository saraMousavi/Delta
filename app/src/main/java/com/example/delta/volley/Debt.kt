package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Debt {
    private val baseUrl = "http://217.144.107.231:3000/debts"

    fun fetchDebts(context: Context, onSuccess: (String) -> Unit, onError: (Exception) -> Unit) {
        val queue = Volley.newRequestQueue(context)
        val request = JsonArrayRequest(
            Request.Method.GET, baseUrl, null,
            { response -> onSuccess(response.toString()) },
            { error -> onError(Exception(error.message)) }
        )
        queue.add(request)
    }

    fun insertDebt(context: Context, debtJson: JSONObject, onSuccess: (String) -> Unit, onError: (Exception) -> Unit) {
        val queue = Volley.newRequestQueue(context)
        Log.d("DebtVolley", "Insert debt JSON: $debtJson")
        val request = JsonObjectRequest(
            Request.Method.POST, baseUrl, debtJson,
            { response -> onSuccess(response.toString()) },
            { error -> onError(Exception(error.message)) }
        )
        queue.add(request)
    }
}