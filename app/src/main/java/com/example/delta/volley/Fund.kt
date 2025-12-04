package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Funds
import com.example.delta.enums.CalculateMethod
import com.example.delta.enums.FundType
import com.example.delta.enums.PaymentLevel
import com.example.delta.enums.Period
import com.example.delta.enums.Responsible
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

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

    fun increaseBalanceFundOnServer(
        context: Context,
        buildingId: Long,
        amount: Double,
        fundType: FundType,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)
        val url = "$baseUrl//increase"

        val payload = JSONObject().apply {
            Log.d("buildingId", buildingId.toString())
            Log.d("amount", amount.toString())
            put("buildingId", buildingId)
            put("amount", amount)
            put("fundType", fundType.name) // or value to match server
        }

        val request = object : JsonObjectRequest(
            Method.POST,
            url,
            payload,
            { response ->
                onSuccess(response.toString())
            },
            { error ->
                val ex = formatVolleyError("IncreaseFund", error)
                onError(ex)
            }
        ) {
            override fun getBodyContentType(): String =
                "application/json; charset=utf-8"
        }

        queue.add(request)
    }

    fun decreaseOperationalFundOnServer(
        context: Context,
        buildingId: Long,
        amount: Double,
        fundType: FundType,
        onSuccess: (Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)
        val url = "$baseUrl//decrease"
        Log.d("url", url)
        val payload = JSONObject().apply {
            put("buildingId", buildingId)
            put("amount", amount)
            put("fundType", fundType.name)
        }

        val request = object : JsonObjectRequest(
            Method.POST,
            url,
            payload,
            { response ->
                val ok = response.optBoolean("ok", false)
                onSuccess(ok)
            },
            { error ->
                val ex = formatVolleyError("DecreaseFund", error)
                onError(ex)
            }
        ) {
            override fun getBodyContentType(): String =
                "application/json; charset=utf-8"
        }

        queue.add(request)
    }

    fun getFundsForBuilding(
        context: Context,
        buildingId: Long,
        onSuccess: (List<Funds>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)
        val url = "$baseUrl?buildingId=$buildingId"   // GET /funds?buildingId=...

        val request = object : JsonArrayRequest(
            Method.GET,
            url,
            null,
            { response ->
                try {
                    val list = mutableListOf<Funds>()
                    for (i in 0 until response.length()) {
                        val obj = response.getJSONObject(i)
                        val fund = Funds(
                            fundId = obj.optLong("fundId", 0L),
                            buildingId = obj.optLong("buildingId", 0L),
                            fundType = FundType.valueOf(obj.optString("fundType", "OPERATIONAL")),
                            balance = obj.optDouble("balance", 0.0)
                        )
                        list += fund
                    }
                    Log.d("fundList", list.toString())
                    onSuccess(list)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { error ->
                val ex = formatVolleyError("GetFundsForBuilding", error)
                onError(ex)
            }
        ) {
            override fun getBodyContentType(): String =
                "application/json; charset=utf-8"
        }

        queue.add(request)
    }

    fun getFundsAndCostsForBuilding(
        context: Context,
        buildingId: Long,
        onSuccess: (List<Funds>, List<Costs>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)
        val url = "$baseUrl/by-building-with-costs?buildingId=$buildingId"
        // baseUrl = "http://217.144.107.231:3000/funds"

        val request = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            { response ->
                try {
                    Log.d("response", response.toString())
                    val fundsArray = response.optJSONArray("funds") ?: JSONArray()
                    val pendingArray = response.optJSONArray("pendingCosts") ?: JSONArray()

                    val fundsList = mutableListOf<Funds>()
                    for (i in 0 until fundsArray.length()) {
                        val obj = fundsArray.getJSONObject(i)
                        val fund = Funds(
                            fundId = obj.optLong("fundId", 0L),
                            buildingId = obj.optLong("buildingId", 0L),
                            fundType = FundType.valueOf(obj.optString("fundType", "OPERATIONAL")),
                            balance = obj.optDouble("balance", 0.0)
                        )
                        fundsList += fund
                    }

                    val costsList = mutableListOf<Costs>()
                    for (i in 0 until pendingArray.length()) {
                        val obj = pendingArray.getJSONObject(i)
                        val cost = Costs(
                            costId        = obj.optLong("costId", 0L),
                            buildingId    = if (obj.isNull("buildingId")) null else obj.optLong("buildingId"),
                            costName      = obj.optString("costName", ""),
                            tempAmount    = obj.optDouble("tempAmount", 0.0),
                            period        = runCatching {
                                val raw = obj.optString("period", "").trim()
                                if (raw.isEmpty()) {
                                    Period.NONE
                                } else {
                                    Period.valueOf(raw.uppercase(Locale.US))
                                }
                            }.getOrElse { Period.NONE },
                            calculateMethod = runCatching {
                                val raw = obj.optString("calculateMethod", "").trim()
                                if (raw.isEmpty()) {
                                    CalculateMethod.EQUAL
                                } else {
                                    CalculateMethod.valueOf(raw.uppercase(Locale.US))
                                }
                            }.getOrElse { CalculateMethod.EQUAL},
                            paymentLevel  = runCatching {
                                val raw = obj.optString("paymentLevel", "").trim()
                                if (raw.isEmpty()) {
                                    PaymentLevel.BUILDING
                                } else {
                                    PaymentLevel.valueOf(raw.uppercase(Locale.US))
                                }
                            }.getOrElse { PaymentLevel.BUILDING},
                            responsible   = Responsible.valueOf(obj.optString("responsible")),
                            fundType      = FundType.valueOf(obj.optString("fundType")),
                            chargeFlag    = obj.optBoolean("chargeFlag", false),
                            dueDate       = obj.optString("dueDate", ""),
                            invoiceFlag   = obj.optBoolean("invoiceFlag", false)
                        )
                        costsList += cost
                    }

                    onSuccess(fundsList, costsList)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { error ->
                val ex = formatVolleyError("GetFundsAndCostsForBuilding", error)
                onError(ex)
            }
        ) {
            override fun getBodyContentType(): String =
                "application/json; charset=utf-8"
        }

        queue.add(request)
    }

    fun getFundByType(
        context: Context,
        buildingId: Long,
        fundType: FundType,
        onSuccess: (Funds?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        getFundsForBuilding(
            context = context,
            buildingId = buildingId,
            onSuccess = { list ->
                val fund = list.firstOrNull { it.fundType == fundType }
                onSuccess(fund)
            },
            onError = onError
        )
    }


}