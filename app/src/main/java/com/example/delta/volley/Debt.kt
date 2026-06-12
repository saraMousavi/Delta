package com.example.delta.volley

import android.content.Context
import android.net.Uri
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.example.delta.data.entity.Debts
import com.example.delta.init.AppRequestQueue
import com.example.delta.server.JsonMapper
import com.example.delta.server.JsonParser
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import androidx.core.net.toUri
import com.example.delta.server.VolleyErrorMapper
import com.example.delta.server.toException

class Debt(
    appContext: Context,
    private val baseUrl: String = "http://185.129.197.6:443/debts",
    private val queue: RequestQueue = AppRequestQueue.getInstance(appContext.applicationContext).requestQueue,
    private val mapper: JsonMapper = JsonMapper(),
    private val parser: JsonParser = JsonParser()
) {

    fun getDebtsForCost(
        costId: Long,
        onSuccess: (List<Debts>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (costId <= 0L) {
            onError(IllegalArgumentException("costId is invalid"))
            return
        }

        val url = "$baseUrl/by-cost".toUri()
            .buildUpon()
            .appendQueryParameter("costId", costId.toString())
            .build()
            .toString()

        val req = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { arr ->
                runCatching { parser.parseDebts(arr) }
                    .onSuccess(onSuccess)
                    .onFailure { onError(it.toException()) }
            },
            { err -> onError(VolleyErrorMapper.toException("DebtApi(getDebtsForCost)", err)) }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    suspend fun getDebtsForCostSuspend(costId: Long): List<Debts> =
        suspendCancellableCoroutine { cont ->
            getDebtsForCost(
                costId = costId,
                onSuccess = { if (cont.isActive) cont.resume(it) },
                onError = { if (cont.isActive) cont.resumeWithException(it) }
            )
        }

    fun updateDebt(
        debt: Debts,
        onSuccess: (Debts) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val id = debt.debtId
        if (id <= 0L) {
            onError(IllegalArgumentException("debtId is invalid"))
            return
        }

        val url = "$baseUrl/$id"

        val body = mapper.debtToJson(debt).apply {
            remove("debtId")
        }

        val req = JsonObjectRequest(
            Request.Method.PUT,
            url,
            body,
            { obj ->
                runCatching {
                    val dObj = obj.optJSONObject("debt") ?: obj
                    parser.parseDebts(JSONArray().put(dObj)).first()
                }.onSuccess(onSuccess).onFailure { onError(it.toException()) }
            },
            { err -> onError(VolleyErrorMapper.toException("DebtApi(updateDebt)", err)) }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    suspend fun updateDebtSuspend(debt: Debts): Debts =
        suspendCancellableCoroutine { cont ->
            updateDebt(
                debt = debt,
                onSuccess = { if (cont.isActive) cont.resume(it) },
                onError = { if (cont.isActive) cont.resumeWithException(it) }
            )
        }

    private fun Request<*>.applyDefaultPolicy() {
        retryPolicy = DefaultRetryPolicy(
            12_000,
            1,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        setShouldCache(false)
    }
}
