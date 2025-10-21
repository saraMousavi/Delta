package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Debts
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.forEach

class Cost {
    private val baseUrl = "http://217.144.107.231:3000/costs"

    fun insertCost(
        context: Context,
        costsJsonArray: JSONArray,
        debtsJsonArray: JSONArray,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)

        // Build full JSON payload
        val payload = JSONObject().apply {
            put("cost", costsJsonArray)
            Log.d("debtsJsonArray", debtsJsonArray.toString())
            put("debts", debtsJsonArray)
        }

        Log.d("CostVolley", "Payload: $payload")

        val request = object : JsonObjectRequest(
            Request.Method.POST, baseUrl, payload,
            { response ->
                Log.d("InsertCostServer", "Costs inserted: $response")
                onSuccess(response.toString())
            },
            { error ->
                val ex = formatVolleyError("InsertCostServer", error)
                onError(ex)
            }
        ) {
            override fun getBodyContentType(): String =
                "application/json; charset=utf-8"
        }

        queue.add(request)
    }

    fun costToJson(cost: Costs): JSONObject {
        return JSONObject().apply {
//            put("costId", cost.costId)
            put("buildingId", cost.buildingId)
            put("costName", cost.costName)
            put("tempAmount", cost.tempAmount)
            put("period", cost.period)
            put("calculateMethod", cost.calculateMethod)
            put("paymentLevel", cost.paymentLevel)
            put("responsible", cost.responsible)
            put("fundType", cost.fundType)
            put("chargeFlag", cost.chargeFlag)
            put("capitalFlag", cost.capitalFlag)
            put("invoiceFlag", cost.invoiceFlag)
            put("dueDate", cost.dueDate)
        }
    }

    fun debtToJson(debts: Debts, costTempId: String? = null): JSONObject {
        return JSONObject().apply {
            if (costTempId != null) put("costTempId", costTempId) else put("costId", debts.costId)

            put("debtId", debts.debtId)
            put("unitId", debts.unitId)
            put("costId", debts.costId)
            put("ownerId", debts.ownerId)
            put("buildingId", debts.buildingId)
            put("description", debts.description)
            put("dueDate", debts.dueDate)
            put("amount", debts.amount)
            put("paymentFlag", debts.paymentFlag)
        }
    }


    fun <T> listToJsonArray(list: List<T>, toJsonFunc: (T) -> JSONObject): JSONArray {
        val jsonArray = JSONArray()
        list.forEach { item ->
            jsonArray.put(toJsonFunc(item))
        }
        return jsonArray
    }

    private fun formatVolleyError(tag: String, error: com.android.volley.VolleyError): Exception {
        val resp = error.networkResponse
        if (resp != null) {
            val status = resp.statusCode
            // سعی کن charset رو از هدر بخونی، وگرنه UTF-8
            val charset = resp.headers?.get("Content-Type")
                ?.substringAfter("charset=", "UTF-8")
                ?: "UTF-8"
            val body = try {
                String(resp.data ?: ByteArray(0), charset(charset))
            } catch (_: Exception) {
                String(resp.data ?: ByteArray(0))
            }

            Log.e(tag, "HTTP $status")
            Log.e(tag, "Headers: ${resp.headers}")
            Log.e(tag, "Body: $body")

            // یک Exception معنادار برگردون
            return Exception("HTTP $status: $body")
        } else {
            // خطاهایی مثل Timeout / NoConnection / DNS
            val klass = error::class.java.simpleName
            Log.e(tag, "No networkResponse ($klass): ${error.message}", error)
            return Exception("$klass: ${error.message ?: error.toString()}")
        }
    }


}