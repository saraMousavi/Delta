package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.NetworkError
import com.android.volley.NoConnectionError
import com.android.volley.Request
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.entity.Credits
import com.example.delta.data.entity.Earnings
import com.example.delta.enums.Period
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale

class Earning {

    private val baseUrl = "http://217.144.107.231:3000/earnings"

    data class EarningWithCredits(
        val earning: Earnings,
        val credits: List<Credits>
    )


    private fun parsePeriod(raw: String?): Period {
        val v = raw?.trim()?.uppercase(Locale.US) ?: ""
        return when (v) {
            "MONTHLY" -> Period.MONTHLY
            "YEARLY"  -> Period.YEARLY
            "WEEKLY"  -> {
                Period.MONTHLY
            }
            "NONE", "" -> Period.NONE
            else -> Period.NONE
        }
    }

    private fun parseInvoiceFlag(obj: JSONObject): Boolean {
        return when {
            obj.has("invoiceFlag") && obj.get("invoiceFlag") is Boolean ->
                obj.optBoolean("invoiceFlag", false)
            obj.has("invoice_flag") && obj.get("invoice_flag") is Boolean ->
                obj.optBoolean("invoice_flag", false)
            obj.has("invoiceFlag") && obj.get("invoiceFlag") is Int ->
                obj.optInt("invoiceFlag", 0) != 0
            obj.has("invoice_flag") && obj.get("invoice_flag") is Int ->
                obj.optInt("invoice_flag", 0) != 0
            else -> false
        }
    }

    private fun parseEarningsArray(arr: JSONArray): List<Earnings> {
        val out = mutableListOf<Earnings>()
        for (i in 0 until arr.length()) {
            val o: JSONObject = arr.getJSONObject(i)

            val earnings = Earnings(
                earningsId   = o.optLong("earningsId", 0L),
                earningsName = o.optString("earningsName",
                    o.optString("earnings_name", "")),
                buildingId   = if (o.isNull("buildingId")) null else o.optLong("buildingId"),
                amount       = o.optDouble("amount", 0.0),
                period       = parsePeriod(
                    o.optString("period", null)
                ),
                invoiceFlag  = parseInvoiceFlag(o),
                startDate    = o.optString("startDate",
                    o.optString("start_date", "")),
                endDate      = o.optString("endDate",
                    o.optString("end_date", ""))
            )
            out += earnings
        }
        return out
    }

    fun getNotInvoicedEarnings(
        context: Context,
        buildingId: Long,
        onSuccess: (List<Earnings>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/not-invoiced?buildingId=$buildingId"
        Log.d("EarningsApi", "GET $url")

        val queue = Volley.newRequestQueue(context)
        val req = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { arr ->
                try {
                    onSuccess(parseEarningsArray(arr))
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err ->
                onError(formatVolleyError("EarningsApi(getNotInvoicedEarnings)", err))
            }
        )
        queue.add(req)
    }

    suspend fun getNotInvoicedEarningsSuspend(
        context: Context,
        buildingId: Long
    ): List<Earnings> = suspendCancellableCoroutine { cont ->
        getNotInvoicedEarnings(
            context = context,
            buildingId = buildingId,
            onSuccess = { list ->
                if (cont.isActive) cont.resume(list, onCancellation = null)
            },
            onError = { e ->
                if (cont.isActive) cont.resumeWith(Result.failure(e))
            }
        )
    }


    fun createEarningsWithCredits(
        context: Context,
        earnings: Earnings,
        onSuccess: (Long) -> Unit,             // earningsId جدید
        onConflict: () -> Unit,                // کانفلیکت مشابه IllegalStateException
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/with-credits"
        val queue = Volley.newRequestQueue(context)

        val body = JSONObject().apply {
            put("buildingId", earnings.buildingId)
            put("earningsName", earnings.earningsName)
            put("amount", earnings.amount)
            put("period", earnings.period.name)     // MONTHLY / YEARLY / NONE
            put("startDate", earnings.startDate)    // "YYYY/MM/DD"
            put("endDate", earnings.endDate)
        }

        val req = object : JsonObjectRequest(
            Method.POST,
            url,
            body,
            { resp ->
                val ok = resp.optBoolean("ok", false)
                if (ok) {
                    val earningsId = resp.optLong("earningsId", 0L)
                    onSuccess(earningsId)
                } else {
                    onError(Exception("Unknown server response"))
                }
            },
            { err ->
                val resp = err.networkResponse
                if (resp != null && resp.statusCode == 409) {
                    // earnings-conflict-with-existing-credits
                    onConflict()
                } else {
                    onError(formatVolleyError("EarningsApi(createEarningsWithCredits)", err))
                }
            }
        ) {
            override fun getBodyContentType(): String =
                "application/json; charset=utf-8"
        }

        queue.add(req)
    }

    suspend fun createEarningsWithCreditsSuspend(
        context: Context,
        earnings: Earnings
    ): Long = suspendCancellableCoroutine { cont ->
        createEarningsWithCredits(
            context = context,
            earnings = earnings,
            onSuccess = { id ->
                if (cont.isActive) cont.resume(id, onCancellation = null)
            },
            onConflict = {
                if (cont.isActive) {
                    cont.resumeWith(
                        Result.failure(IllegalStateException("Earnings conflict with existing credits"))
                    )
                }
            },
            onError = { e ->
                if (cont.isActive) cont.resumeWith(Result.failure(e))
            }
        )
    }


    private fun parseEarningObject(o: JSONObject): Earnings {
        return Earnings(
            earningsId   = o.optLong("earningsId", 0L),
            earningsName = o.optString("earningsName",
                o.optString("earnings_name", "")),
            buildingId   = if (o.isNull("buildingId")) null else o.optLong("buildingId"),
            amount       = o.optDouble("amount", 0.0),
            period       = parsePeriod(o.optString("period", null)),
            invoiceFlag  = parseInvoiceFlag(o),
            startDate    = o.optString("startDate",
                o.optString("start_date", "")),
            endDate      = o.optString("endDate",
                o.optString("end_date", ""))
        )
    }

    fun getEarningById(
        context: Context,
        earningId: Long,
        onSuccess: (EarningWithCredits) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/$earningId"
        Log.d("EarningsApi", "GET $url")

        val queue = Volley.newRequestQueue(context)
        val req = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { obj ->
                try {
                    val result = parseEarningWithCredits(obj)
                    onSuccess(result)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err ->
                onError(formatVolleyError("EarningsApi(getEarningById)", err))
            }
        )
        queue.add(req)
    }


    suspend fun getEarningByIdSuspend(
        context: Context,
        earningId: Long
    ): EarningWithCredits = suspendCancellableCoroutine { cont ->
        getEarningById(
            context = context,
            earningId = earningId,
            onSuccess = { result ->
                if (cont.isActive) cont.resume(result, onCancellation = null)
            },
            onError = { e ->
                if (cont.isActive) cont.resumeWith(Result.failure(e))
            }
        )
    }



    private fun parseCreditsArray(arr: JSONArray): List<Credits> {
        val out = mutableListOf<Credits>()
        for (i in 0 until arr.length()) {
            val o: JSONObject = arr.getJSONObject(i)
            val c = Credits(
                creditsId   = o.optLong("creditsId", 0L),
                earningsId  = o.optLong("earningsId", 0L),
                buildingId  = o.optLong("buildingId", 0L),
                description = o.optString("description", ""),
                dueDate     = o.optString("dueDate", ""),
                amount      = o.optDouble("amount", 0.0),
                receiptFlag = o.optBoolean("receiptFlag")
            )
            out += c
        }
        return out
    }

    private fun parseEarningWithCredits(obj: JSONObject): EarningWithCredits {
        val earningObj = obj.getJSONObject("earning")
        val creditsArr = obj.optJSONArray("credits") ?: JSONArray()

        val earning = parseEarningObject(earningObj)
        val credits = parseCreditsArray(creditsArr)

        return EarningWithCredits(earning = earning, credits = credits)
    }

    fun getAllMenuEarnings(
        context: Context,
        onSuccess: (List<Earnings>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/menu"
        Log.d("EarningsApi", "GET $url")

        val queue = Volley.newRequestQueue(context)
        val req = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { arr ->
                try {
                    val list = parseEarningsArray(arr)
                    onSuccess(list)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err ->
                onError(formatVolleyError("EarningsApi(getAllMenuEarnings)", err))
            }
        )
        queue.add(req)
    }

    suspend fun getAllMenuEarningsSuspend(
        context: Context
    ): List<Earnings> = suspendCancellableCoroutine { cont ->
        getAllMenuEarnings(
            context = context,
            onSuccess = { list ->
                if (cont.isActive) cont.resume(list, onCancellation = null)
            },
            onError = { e ->
                if (cont.isActive) cont.resumeWith(Result.failure(e))
            }
        )
    }

    fun insertNewEarning(
        context: Context,
        earning: Earnings,
        onSuccess: (Long) -> Unit,
        onConflict: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = baseUrl
        val queue = Volley.newRequestQueue(context)

        val body = JSONObject().apply {
            put("buildingId", earning.buildingId)
            put("earningsName", earning.earningsName)
            put("amount", earning.amount)
            put("period", earning.period.name)
            put("startDate", earning.startDate)
            put("endDate", earning.endDate)
            put("invoiceFlag", earning.invoiceFlag)
        }

        val req = object : JsonObjectRequest(
            Method.POST,
            url,
            body,
            { resp ->
                val ok = resp.optBoolean("ok", false)
                if (!ok) {
                    onError(Exception("Unknown server response"))
                } else {
                    val earningsId = resp.optLong("earningsId", 0L)
                    onSuccess(earningsId)
                }
            },
            { err ->
                val resp = err.networkResponse
                if (resp != null && resp.statusCode == 409) {
                    onConflict()
                } else {
                    onError(formatVolleyError("EarningsApi(insertNewEarning)", err))
                }
            }
        ) {
            override fun getBodyContentType(): String =
                "application/json; charset=utf-8"
        }

        queue.add(req)
    }

    suspend fun insertNewEarningSuspend(
        context: Context,
        earning: Earnings
    ): Long = suspendCancellableCoroutine { cont ->
        insertNewEarning(
            context = context,
            earning = earning,
            onSuccess = { id ->
                if (cont.isActive) cont.resume(id, onCancellation = null)
            },
            onConflict = {
                if (cont.isActive) {
                    cont.resumeWith(
                        Result.failure(IllegalStateException("earnings-name-exists"))
                    )
                }
            },
            onError = { e ->
                if (cont.isActive) cont.resumeWith(Result.failure(e))
            }
        )
    }

    fun earningNameExists(
        context: Context,
        buildingId: Long?,
        earningName: String,
        onSuccess: (Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val base = "$baseUrl/name-exists"
        val builder = android.net.Uri.parse(base).buildUpon()
            .appendQueryParameter("earningsName", earningName)
        buildingId?.let { builder.appendQueryParameter("buildingId", it.toString()) }
        val url = builder.build().toString()

        val queue = Volley.newRequestQueue(context)
        val req = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { obj ->
                val exists = obj.optBoolean("exists", false)
                onSuccess(exists)
            },
            { err ->
                onError(formatVolleyError("EarningsApi(earningNameExists)", err))
            }
        )
        queue.add(req)
    }

    suspend fun earningNameExistsSuspend(
        context: Context,
        buildingId: Long?,
        earningName: String
    ): Boolean = suspendCancellableCoroutine { cont ->
        earningNameExists(
            context = context,
            buildingId = buildingId,
            earningName = earningName,
            onSuccess = { exists ->
                if (cont.isActive) cont.resume(exists, onCancellation = null)
            },
            onError = { e ->
                if (cont.isActive) cont.resumeWith(Result.failure(e))
            }
        )
    }
    private fun formatVolleyError(tag: String, error: VolleyError): Exception {
        val resp = error.networkResponse
        return if (resp != null) {
            val body = try {
                String(resp.data ?: ByteArray(0), Charsets.UTF_8)
            } catch (_: Exception) {
                String(resp.data ?: ByteArray(0))
            }
            Log.e(tag, "HTTP ${resp.statusCode}: $body")
            Exception("HTTP ${resp.statusCode}: $body")
        } else {
            val msg = when (error) {
                is TimeoutError -> "timeout"
                is NoConnectionError -> "no-connection"
                is NetworkError -> "network-error"
                else -> "unknown-network-error"
            }
            Exception(msg, error)
        }
    }

    fun insertEarningsWithCredits(
        context: Context,
        earning: com.example.delta.data.entity.Earnings,
        onSuccess: (Long, Int) -> Unit,
        onConflict: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/with-credits"
        val queue = Volley.newRequestQueue(context)

        val body = JSONObject().apply {
            put("buildingId", earning.buildingId)
            put("earningsName", earning.earningsName)
            put("amount", earning.amount)
            put("period", earning.period.name)
            put("startDate", earning.startDate)
            put("endDate", earning.endDate)
        }

        val req = object : JsonObjectRequest(
            Method.POST,
            url,
            body,
            { resp ->
                val ok = resp.optBoolean("ok", false)
                if (!ok) {
                    onError(Exception("unknown-server-response"))
                } else {
                    val earningsId = resp.optLong("earningsId", 0L)
                    val creditsInserted = resp.optInt("creditsInserted", 0)
                    onSuccess(earningsId, creditsInserted)
                }
            },
            { err ->
                val resp = err.networkResponse
                val rawBody = if (resp?.data != null) {
                    try {
                        String(resp.data, Charsets.UTF_8)
                    } catch (_: Exception) {
                        ""
                    }
                } else {
                    ""
                }

                val messageCode = try {
                    if (rawBody.isNotBlank()) {
                        org.json.JSONObject(rawBody).optString("message", "")
                    } else {
                        ""
                    }
                } catch (_: Exception) {
                    ""
                }

                when {
                    resp?.statusCode == 409 || messageCode == "earnings-conflict-with-existing-credits" -> {
                        onConflict()
                    }

                    messageCode == "period-range-too-short" -> {
                        onError(IllegalStateException("period-range-too-short"))
                    }

                    else -> {
                        onError(formatVolleyError("EarningsApi(insertEarningsWithCredits)", err))
                    }
                }
            }
        ) {
            override fun getBodyContentType(): String =
                "application/json; charset=utf-8"
        }

        queue.add(req)
    }


    suspend fun insertEarningsWithCreditsSuspend(
        context: Context,
        earning: com.example.delta.data.entity.Earnings
    ): Pair<Long, Int> = suspendCancellableCoroutine { cont ->
        insertEarningsWithCredits(
            context = context,
            earning = earning,
            onSuccess = { eId, count ->
                if (cont.isActive) cont.resume(eId to count, onCancellation = null)
            },
            onConflict = {
                if (cont.isActive) {
                    cont.resumeWith(
                        Result.failure(IllegalStateException("earnings-conflict-with-existing-credits"))
                    )
                }
            },
            onError = { e ->
                if (cont.isActive) {
                    cont.resumeWith(Result.failure(e))
                }
            }
        )
    }

    fun markSelectedCreditsAsReceived(
        context: Context,
        earningId: Long,
        creditIds: List<Long>,
        onSuccess: (List<Credits>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/mark-received"
        val queue = Volley.newRequestQueue(context)

        val idsArray = JSONArray().apply {
            creditIds.forEach { id -> put(id) }
        }

        val body = JSONObject().apply {
            put("earningId", earningId)
            put("creditIds", idsArray)
        }

        val req = object : JsonObjectRequest(
            Method.POST,
            url,
            body,
            { obj ->
                try {
                    val creditsJson = obj.optJSONArray("credits") ?: JSONArray()
                    val list = mutableListOf<Credits>()
                    for (i in 0 until creditsJson.length()) {
                        val c = creditsJson.getJSONObject(i)
                        list += Credits(
                            creditsId = c.optLong("creditsId"),
                            earningsId = c.optLong("earningsId"),
                            buildingId = c.optLong("buildingId"),
                            description = c.optString("description", ""),
                            dueDate = c.optString("dueDate", ""),
                            amount = c.optDouble("amount", 0.0),
                            receiptFlag = c.optBoolean("receiptFlag", false)
                        )
                    }
                    onSuccess(list)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err ->
                onError(formatVolleyError("EarningsApi(markSelectedCreditsAsReceived)", err))
            }
        ) {
            override fun getBodyContentType(): String =
                "application/json; charset=utf-8"
        }

        queue.add(req)
    }

    suspend fun markSelectedCreditsAsReceivedSuspend(
        context: Context,
        earningId: Long,
        creditIds: List<Long>
    ): List<Credits> = suspendCancellableCoroutine { cont ->
        markSelectedCreditsAsReceived(
            context = context,
            earningId = earningId,
            creditIds = creditIds,
            onSuccess = { list ->
                if (cont.isActive) cont.resume(list, onCancellation = null)
            },
            onError = { e ->
                if (cont.isActive) cont.resumeWith(Result.failure(e))
            }
        )
    }




}
