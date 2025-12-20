package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.entity.Debts
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject

class Debt {
    private val baseUrl = "http://217.144.107.231:3000/debts"

    fun getDebtsForCost(
        context: Context,
        costId: Long,
        onSuccess: (List<Debts>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/by-cost?costId=$costId"
        Log.d("DebtApi", "GET $url")

        val queue = Volley.newRequestQueue(context)
        val req = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { arr ->
                try {
                    onSuccess(parseDebts(arr))
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err -> onError(formatVolleyError("DebtApi(getDebtsForCost)", err)) }
        )
        queue.add(req)
    }

    suspend fun getDebtsForCostSuspend(
        context: Context,
        costId: Long
    ): List<Debts> = suspendCancellableCoroutine { cont ->
        getDebtsForCost(
            context = context,
            costId = costId,
            onSuccess = { list ->
                Log.d("debtList", list.toString())
                if (cont.isActive) cont.resume(list, onCancellation = null)
            },
            onError = { e ->
                Log.d("debtError", e.toString())
                if (cont.isActive) cont.resumeWith(Result.failure(e))
            }
        )
    }

    private fun formatVolleyError(
        tag: String,
        error: com.android.volley.VolleyError
    ): Exception {
        val resp = error.networkResponse
        return if (resp != null) {
            val body = try { String(resp.data ?: ByteArray(0), Charsets.UTF_8) }
            catch (_: Exception) { String(resp.data ?: ByteArray(0)) }
            Log.e(tag, "HTTP ${resp.statusCode}: $body")
            Exception("HTTP ${resp.statusCode}: $body")
        } else {
            Log.e(tag, "No networkResponse: ${error.message}", error)
            Exception(error.toString())
        }
    }

    private fun parseDebts(arr: JSONArray): List<Debts> {
        val list = mutableListOf<Debts>()
        for (i in 0 until arr.length()) {
            val o: JSONObject = arr.getJSONObject(i)
            val d = Debts(
                debtId      = o.optLong("debtId", 0L),
                costId      = o.optLong("costId", 0L),
                unitId      = o.optLong("unitId", 0L),
                ownerId     = if (o.isNull("ownerId")) null else o.optLong("ownerId"),
                amount      = o.optDouble("amount", 0.0),
                description = o.optString("description", ""),
                paymentFlag = o.optBoolean("paymentFlag", false),
                dueDate = o.optString("dueDate", ""),
                buildingId = o.optLong("buildingId", 0L)
            )
            list += d
        }
        return list
    }


    suspend fun updateDebtSuspend(
        context: Context,
        debt: Debts
    ): Debts = suspendCancellableCoroutine { cont ->
        updateDebt(
            context,
            debt,
            onSuccess = { updated ->
                if (cont.isActive) cont.resume(updated, onCancellation = null)
            },
            onError = { e ->
                if (cont.isActive) cont.resumeWith(Result.failure(e))
            }
        )
    }


    fun updateDebt(
        context: Context,
        debt: Debts,
        onSuccess: (Debts) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/${debt.debtId}"
        val body = JSONObject().apply {
            put("paymentFlag", debt.paymentFlag)
            put("amount", debt.amount)
            put("description", debt.description)
            put("dueDate", debt.dueDate)
            put("ownerId", debt.ownerId)
            put("unitId", debt.unitId)
            put("buildingId", debt.buildingId)
            put("costId", debt.costId)
        }

        val queue = Volley.newRequestQueue(context)
        val req = JsonObjectRequest(
            Request.Method.PUT,
            url,
            body,
            { obj ->
                try {
                    val updated = Debts(
                        debtId = obj.getJSONObject("debt").optLong("debtId"),
                        costId = obj.getJSONObject("debt").optLong("costId"),
                        unitId = obj.getJSONObject("debt").optLong("unitId"),
                        ownerId = if (obj.getJSONObject("debt").isNull("ownerId")) null else obj.getJSONObject("debt").optLong("ownerId"),
                        amount = obj.getJSONObject("debt").optDouble("amount"),
                        description = obj.getJSONObject("debt").optString("description"),
                        paymentFlag = obj.getJSONObject("debt").optBoolean("paymentFlag"),
                        dueDate = obj.getJSONObject("debt").optString("dueDate"),
                        buildingId = obj.getJSONObject("debt").optLong("buildingId")
                    )
                    onSuccess(updated)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err -> onError(formatVolleyError("DebtApi(updateDebt)", err)) }
        )
        queue.add(req)
    }

}
