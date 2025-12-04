package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Debts
import com.example.delta.enums.CalculateMethod
import com.example.delta.enums.FundType
import com.example.delta.enums.PaymentLevel
import com.example.delta.enums.Period
import com.example.delta.enums.Responsible
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.forEach
import kotlin.coroutines.resumeWithException

class Cost{
    private val baseUrl = "http://217.144.107.231:3000/costs"
    private val baseBuildingUrl = "http://217.144.107.231:3000/building"

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

    fun costToJson(cost: Costs, tempId: String? = null): JSONObject {
        return JSONObject().apply {
            if (tempId != null) {
                put("tempId", tempId)
            }
            put("buildingId", cost.buildingId)
            put("costName", cost.costName)
            put("tempAmount", if (cost.tempAmount == 1.0) 0.0 else cost.tempAmount)
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
            if (costTempId != null) {
                put("costTempId", costTempId)
            } else {
                put("costId", debts.costId)
            }

            put("buildingId", debts.buildingId)

            debts.unitId?.let {
                if (it > 0L) {
                    put("unitId", debts.unitId)
                }
            }

            put("ownerId", debts.ownerId)
            put("description", debts.description)
            put("dueDate", debts.dueDate)
            put("amount", debts.amount)
            put("paymentFlag", debts.paymentFlag)
        }
    }



    fun jsonArrayToCosts(array: JSONArray): List<Costs> {
        val list = mutableListOf<Costs>()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)

            val cost = Costs(
                costId = obj.optLong("costId", 0L),
                buildingId = obj.optLong("buildingId", 0L),
                costName = obj.optString("costName", ""),
                tempAmount = obj.optDouble("tempAmount", 0.0),

                period = Period.valueOf(obj.optString("period", "NONE")),
                calculateMethod = CalculateMethod.valueOf(obj.optString("calculateMethod", "EQUAL")),
                paymentLevel = PaymentLevel.valueOf(obj.optString("paymentLevel", "GENERAL")),
                responsible = Responsible.valueOf(obj.optString("responsible", "MANAGER")),
                fundType = FundType.valueOf(obj.optString("fundType", "NONE")),

                chargeFlag = obj.optBoolean("chargeFlag", false),
                capitalFlag = obj.optBoolean("capitalFlag", false),
                invoiceFlag = obj.optBoolean("invoiceFlag", false),

                dueDate = obj.optString("dueDate", "")
            )

            list.add(cost)
        }

        return list
    }

    fun jsonArrayToDebts(array: JSONArray): List<Debts> {
        val list = mutableListOf<Debts>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val debt = Debts(
                debtId = obj.optLong("debtId", 0L),
                unitId = obj.optLong("unitId", 0L),
                costId = obj.optLong("costId", 0L),
                ownerId = obj.optLong("ownerId", 0L),
                buildingId = obj.optLong("buildingId", 0L),
                description = obj.optString("description", ""),
                dueDate = obj.optString("dueDate", ""),
                amount = obj.optDouble("amount", 0.0),
                paymentFlag = obj.optBoolean("paymentFlag", false)
            )
            list.add(debt)
        }
        return list
    }

    fun fetchBuildingsWithCosts(
        context: Context,
        mobileNumber: String,
        onSuccess: (List<BuildingWithCosts>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)
        val url = "$baseBuildingUrl/with-costs?mobileNumber=${mobileNumber.trim()}"

        val request = object : JsonArrayRequest(
            Method.GET,
            url,
            null,
            { response ->
                try {
                    val result = mutableListOf<BuildingWithCosts>()
                    for (i in 0 until response.length()) {
                        val bObj = response.getJSONObject(i)

                        val building = Buildings(
                            buildingId = bObj.optLong("buildingId"),
                            complexId = if (bObj.isNull("complexId")) null else bObj.optLong("complexId"),
                            name = bObj.optString("name", ""),
                            postCode = bObj.optString("postCode", ""),
                            street = bObj.optString("street", ""),
                            province = bObj.optString("province", ""),
                            state = bObj.optString("state", ""),
                            buildingTypeId = if (bObj.isNull("buildingTypeId")) null else bObj.optLong("buildingTypeId"),
                            buildingUsageId = if (bObj.isNull("buildingUsageId")) null else bObj.optLong("buildingUsageId"),
                            fund = bObj.optDouble("fund").let { if (it.isNaN()) 0.0 else it },
                            userId = bObj.optLong("userId"),
                            serialNumber = bObj.optString("serialNumber"),
                            floorCount = bObj.optInt("floorCount"),
                        )

                        val costsArray = bObj.optJSONArray("costs") ?: JSONArray()
                        val costs = jsonArrayToCosts(costsArray)

                        result += BuildingWithCosts(
                            building = building,
                            costs = costs
                        )
                    }

                    onSuccess(result)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { error ->
                onError(formatVolleyError("FetchBuildingsWithCosts", error))
            }
        ) {
            override fun getBodyContentType(): String =
                "application/json; charset=utf-8"
        }

        queue.add(request)
    }


    fun fetchCostsWithDebts(
        context: Context,
        ownerId: Long? = null,
        unitId: Long? = null,
        userId: Long? = null,
        onSuccess: (List<Costs>, List<Debts>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)

        val queryParams = mutableListOf<String>()
        if (ownerId != null) {
            queryParams += "ownerId=$ownerId"
        }
        if (unitId != null) {
            queryParams += "unitId=$unitId"
        }
        if (userId != null) {
            queryParams += "userId=$userId"
        }

        val query = if (queryParams.isNotEmpty()) {
            "?" + queryParams.joinToString("&")
        } else {
            ""
        }

        val url = "$baseUrl/full$query"   // GET /costs/full?ownerId=...&unitId=...

        val request = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            { response ->
                try {
                    val costsArray = response.optJSONArray("costs") ?: JSONArray()
                    val debtsArray = response.optJSONArray("debts") ?: JSONArray()

                    val costs = jsonArrayToCosts(costsArray)
                    val debts = jsonArrayToDebts(debtsArray)

                    onSuccess(costs, debts)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { error ->
                val ex = formatVolleyError("FetchCostWithDebts", error)
                onError(ex)
            }
        ) {
            override fun getBodyContentType(): String =
                "application/json; charset=utf-8"
        }

        queue.add(request)
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

            return Exception("HTTP $status: $body")
        } else {
            // خطاهایی مثل Timeout / NoConnection / DNS
            val klass = error::class.java.simpleName
            Log.e(tag, "No networkResponse ($klass): ${error.message}", error)
            return Exception("$klass: ${error.message ?: error.toString()}")
        }
    }

    fun markCostInvoicedOnServer(
        context: Context,
        costId: Long,
        onSuccess: (Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)
        val url = "$baseUrl/$costId/mark-invoiced"

        val payload = JSONObject().apply {
            put("costId", costId)
        }

        val req = object : JsonObjectRequest(
            Method.POST,
            url,
            payload,
            { resp ->
                val ok = resp.optBoolean("ok", false)
                onSuccess(ok)
            },
            { err ->
                val ex = formatVolleyError("CostInvoice", err)
                onError(ex)
            }
        ) {
            override fun getBodyContentType(): String =
                "application/json; charset=utf-8"
        }

        queue.add(req)
    }

    fun invoiceCostIfEnoughFund(
        context: Context,
        costId: Long,
        onSuccess: (Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/invoice"
        val queue = Volley.newRequestQueue(context)

        val body = JSONObject().apply {
            put("costId", costId)
        }

        val req = object : JsonObjectRequest(
            Request.Method.POST,
            url,
            body,
            { resp ->
                val ok = resp.optBoolean("ok", false)
                onSuccess(ok)
            },
            { err ->
                onError(formatVolleyError("CostApi(invoiceCost)", err))
            }
        ) {
            override fun getBodyContentType(): String = "application/json; charset=utf-8"
        }

        queue.add(req)
    }

    suspend fun invoiceCostIfEnoughFundSuspend(
        context: Context,
        costId: Long
    ): Boolean = suspendCancellableCoroutine { cont ->
        invoiceCostIfEnoughFund(
            context = context,
            costId = costId,
            onSuccess = { ok ->
                if (cont.isActive) cont.resume(ok, onCancellation = null)
            },
            onError = { e ->
                if (cont.isActive) cont.resumeWith(Result.failure(e))
            }
        )
    }

    private fun formatError(tag: String, e: com.android.volley.VolleyError): Exception {
        val r = e.networkResponse
        if (r != null) {
            val body = try {
                String(r.data ?: ByteArray(0), charset("UTF-8"))
            } catch (_: Exception) {
                String(r.data ?: ByteArray(0))
            }
            Log.e(tag, "HTTP ${r.statusCode}: $body")
            return Exception("HTTP ${r.statusCode}: $body")
        }
        Log.e(tag, "No networkResponse", e)
        return Exception(e.toString())
    }

    fun fetchGlobalCosts(
        context: Context,
        onSuccess: (List<Costs>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)
        val url = "$baseUrl?page=1&limit=100"

        val req = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { arr ->
                try {
                    val out = mutableListOf<Costs>()
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        out += Costs(
                            costId = o.optLong("costId", 0L),
                            costName = o.optString("costName", ""),
                            period = Period.valueOf(o.optString("period", "YEARLY")),
                            paymentLevel = PaymentLevel.valueOf(o.optString("paymentLevel", "UNIT")),
                            fundType = FundType.valueOf(o.optString("fundType", "OPERATIONAL")),
                            calculateMethod = CalculateMethod.valueOf(o.optString("calculateMethod", "EQUAL")),
                            responsible = Responsible.valueOf(o.optString("responsible", "TENANT")),
                            tempAmount = o.optDouble("tempAmount", 0.0),
                            chargeFlag = o.optBoolean("chargeFlag", true),
                            dueDate = o.optString("dueDate", "")
                        )
                    }
                    onSuccess(out)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err -> onError(formatError("CostApi(fetchGlobalCosts)", err)) }
        )

        queue.add(req)
    }

    fun createGlobalCost(
        context: Context,
        cost: Costs,
        onSuccess: (Costs) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)
        val url = baseUrl

        val costJson = JSONObject().apply {
            put("buildingId", 0)  // template/global
            put("costName", cost.costName)
            put("tempAmount", cost.tempAmount)
            put("period", cost.period.name)
            put("calculateMethod", cost.calculateMethod.name)
            put("paymentLevel", cost.paymentLevel.name)
            put("responsible", cost.responsible.name)
            put("fundType", cost.fundType.name)
            put("chargeFlag", cost.chargeFlag)
            put("capitalFlag", false)
            put("invoiceFlag", false)
            put("dueDate", cost.dueDate)
        }

        val body = JSONObject().apply {
            put("cost", costJson)
            put("debts", JSONArray())
        }

        val req = JsonObjectRequest(
            Request.Method.POST,
            url,
            body,
            { resp ->
                try {
                    val id = resp.optLong("costId", 0L)
                    onSuccess(cost.copy(costId = id))
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err -> onError(formatError("CostApi(createGlobalCost)", err)) }
        )

        queue.add(req)
    }

    suspend fun fetchGlobalCostsSuspend(
        context: Context
    ): List<Costs> = suspendCancellableCoroutine { cont ->
        fetchGlobalCosts(
            context = context,
            onSuccess = { list ->
                if (cont.isActive) cont.resume(list, onCancellation = null)
            },
            onError = { e ->
                if (cont.isActive) cont.resumeWithException(e)
            }
        )
    }

    fun costNameExists(
        context: Context,
        buildingId: Long?,
        costName: String,
        onSuccess: (Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val base = "$baseUrl/name-exists"
        val builder = android.net.Uri.parse(base).buildUpon()
            .appendQueryParameter("costName", costName)
        buildingId?.let { builder.appendQueryParameter("buildingId", it.toString()) }
        val url = builder.build().toString()

        val queue = Volley.newRequestQueue(context)
        val req = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { obj: JSONObject ->
                val exists = obj.optBoolean("exists", false)
                onSuccess(exists)
            },
            { err ->
                onError(formatVolleyError("CostApi(costNameExists)", err))
            }
        )
        queue.add(req)
    }

    fun insertDebtForCapitalCostOnServer(
        context: Context,
        buildingId: Long,
        cost: Costs,
        amountPerOwner: Double,
        dueDate: String,
        description: String,
        onSuccess: (List<Costs>, List<Debts>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)
        val url = "$baseUrl/capital-with-debts"

        val costJson = JSONObject().apply {
            put("costName", cost.costName)
            put("tempAmount", cost.tempAmount)
            put("period", cost.period.name)
            put("calculateMethod", cost.calculateMethod.name)
            put("paymentLevel", cost.paymentLevel.name)
            put("responsible", cost.responsible.name)
            put("fundType", cost.fundType.name)
            put("chargeFlag", cost.chargeFlag)
            put("capitalFlag", cost.capitalFlag)
            put("invoiceFlag", cost.invoiceFlag)
        }

        val payload = JSONObject().apply {
            put("buildingId", buildingId)
            put("amountPerOwner", amountPerOwner)
            put("dueDate", dueDate)
            put("description", description)
            put("cost", costJson)
        }

        val req = object : JsonObjectRequest(
            Method.POST,
            url,
            payload,
            { resp ->
                try {
                    val insertedCostsArr = resp.optJSONArray("insertedCosts") ?: JSONArray()
                    val insertedDebtsArr = resp.optJSONArray("insertedDebts") ?: JSONArray()

                    val costsList = jsonArrayToCosts(insertedCostsArr)
                    val debtsList = jsonArrayToDebts(insertedDebtsArr)

                    onSuccess(costsList, debtsList)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err ->
                val ex = formatVolleyError("CostApi(insertDebtForCapitalCostOnServer)", err)
                onError(ex)
            }
        ) {
            override fun getBodyContentType(): String =
                "application/json; charset=utf-8"
        }

        queue.add(req)
    }

    suspend fun insertDebtForCapitalCostSuspend(
        context: Context,
        buildingId: Long,
        cost: Costs,
        amountPerOwner: Double,
        dueDate: String,
        description: String
    ): Pair<List<Costs>, List<Debts>> = suspendCancellableCoroutine { cont ->
        insertDebtForCapitalCostOnServer(
            context = context,
            buildingId = buildingId,
            cost = cost,
            amountPerOwner = amountPerOwner,
            dueDate = dueDate,
            description = description,
            onSuccess = { c, d ->
                if (cont.isActive) cont.resume(c to d, onCancellation = null)
            },
            onError = { e ->
                if (cont.isActive) cont.resumeWithException(e)
            }
        )
    }


    suspend fun costNameExistsSuspend(
        context: Context,
        buildingId: Long?,
        costName: String
    ): Boolean = kotlinx.coroutines.suspendCancellableCoroutine { cont ->
        costNameExists(
            context = context,
            buildingId = buildingId,
            costName = costName,
            onSuccess = { exists ->
                if (cont.isActive) cont.resume(exists, onCancellation = null)
            },
            onError = { e ->
                if (cont.isActive) cont.resumeWith(Result.failure(e))
            }
        )
    }
    data class BuildingWithCosts(
        val building: Buildings,
        val costs: List<Costs>
    )

}


