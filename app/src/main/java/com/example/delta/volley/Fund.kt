package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Funds
import org.json.JSONArray
import org.json.JSONObject

class Fund {
    private val baseUrl = "http://217.144.107.231:3000/funds"

    fun fetchDebts(context: Context, onSuccess: (String) -> Unit, onError: (Exception) -> Unit) {
        val queue = Volley.newRequestQueue(context)
        val request = JsonArrayRequest(
            Request.Method.GET, baseUrl, null,
            { response -> onSuccess(response.toString()) },
            { error -> onError(Exception(error.message)) }
        )
        queue.add(request)
    }

    fun insertFund(context: Context, fundJsonArray: JSONArray, onSuccess: (String) -> Unit, onError: (Exception) -> Unit) {
        val queue = Volley.newRequestQueue(context)
        val payload = JSONObject().apply {
            put("fund", fundJsonArray)
        }
//
        Log.d("FundVolley", payload.toString())
        val request = object : JsonObjectRequest(
            Request.Method.POST, baseUrl, payload,
            { response ->
                Log.d("InsertFundServer", "Fund inserted: $response")
                onSuccess(response.toString())
            },
            { error ->
                onError(formatVolleyError("InsertFundServer", error))
            }
        ) {
            override fun getBodyContentType() = "application/json; charset=utf-8"
        }
        queue.add(request)
    }

    fun updateFund(
        context: Context,
        fundId: Long,                    // or Int, depending on your API
        fundJson: JSONObject,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)
        val url = "$baseUrl/$fundId"
        Log.d("FundVolley", "Update fund ($fundId) JSON: $fundJson")

        val request = object : JsonObjectRequest(
            Request.Method.PUT,            // Use PATCH here if your backend expects it
            url,
            fundJson,
            { response -> onSuccess(response.toString()) },
            { error ->
                onError(formatVolleyError("InsertFundServer", error)) }
        ) {
            override fun getBodyContentType() = "application/json; charset=utf-8"
        }

        queue.add(request)
    }

    private fun formatVolleyError(tag: String, error: com.android.volley.VolleyError): Exception {
        val resp = error.networkResponse
        if (resp != null) {
            val charsetName = resp.headers?.get("Content-Type")
                ?.substringAfter("charset=", "UTF-8") ?: "UTF-8"
            val body = try { String(resp.data ?: ByteArray(0), charset(charsetName)) }
            catch (_: Exception) { String(resp.data ?: ByteArray(0)) }
            Log.e(tag, "HTTP ${resp.statusCode}")
            Log.e(tag, "Headers: ${resp.headers}")
            Log.e(tag, "Body: $body")
            return Exception("HTTP ${resp.statusCode}: $body")
        } else {
            Log.e(tag, "No networkResponse: ${error.message}", error)
            return Exception(error.toString())
        }
    }

    fun fundToJson(fund: Funds): JSONObject {
        return JSONObject().apply {
            put("buildingId", fund.buildingId)
            put("fundType", fund.fundType)
            put("balance", fund.balance)
        }
    }

    fun <T> listToJsonArray(list: List<T>, toJsonFunc: (T) -> JSONObject): JSONArray {
        val jsonArray = JSONArray()
        list.forEach { item ->
            jsonArray.put(toJsonFunc(item))
        }
        return jsonArray
    }

}