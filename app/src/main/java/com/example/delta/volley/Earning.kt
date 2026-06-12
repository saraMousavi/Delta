// EarningApi.kt
package com.example.delta.volley

import android.content.Context
import android.net.Uri
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.example.delta.data.entity.Credits
import com.example.delta.data.entity.Earnings
import com.example.delta.init.AppRequestQueue
import com.example.delta.server.JsonMapper
import com.example.delta.server.JsonParser
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import androidx.core.net.toUri
import com.example.delta.server.VolleyErrorMapper
import com.example.delta.server.toException

class Earning(
    appContext: Context,
    private val baseUrl: String = "http://185.129.197.6:443/earnings",
    private val queue: RequestQueue = AppRequestQueue.getInstance(appContext.applicationContext).requestQueue,
    private val mapper: JsonMapper = JsonMapper(),
    private val parser: JsonParser = JsonParser()
) {

    data class EarningWithCredits(
        val earning: Earnings,
        val credits: List<Credits>
    )

    fun getNotInvoicedEarnings(
        buildingId: Long,
        onSuccess: (List<Earnings>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (buildingId <= 0L) {
            onError(IllegalArgumentException("buildingId is invalid"))
            return
        }

        val url = "$baseUrl/not-invoiced".toUri()
            .buildUpon()
            .appendQueryParameter("buildingId", buildingId.toString())
            .build()
            .toString()

        val req = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { arr ->
                runCatching { parser.parseEarnings(arr) }
                    .onSuccess(onSuccess)
                    .onFailure { onError(it.toException()) }
            },
            { err -> onError(VolleyErrorMapper.toException("EarningApi(getNotInvoicedEarnings)", err)) }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    suspend fun getNotInvoicedEarningsSuspend(buildingId: Long): List<Earnings> =
        suspendCancellableCoroutine { cont ->
            getNotInvoicedEarnings(
                buildingId = buildingId,
                onSuccess = { if (cont.isActive) cont.resume(it) },
                onError = { if (cont.isActive) cont.resumeWithException(it) }
            )
        }

    fun getFullyReceivedEarnings(
        buildingId: Long,
        onSuccess: (List<Earnings>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (buildingId <= 0L) {
            onError(IllegalArgumentException("buildingId is invalid"))
            return
        }

        val url = "$baseUrl/fully-received".toUri()
            .buildUpon()
            .appendQueryParameter("buildingId", buildingId.toString())
            .build()
            .toString()

        val req = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { arr ->
                runCatching { parser.parseEarnings(arr) }
                    .onSuccess(onSuccess)
                    .onFailure { onError(it.toException()) }
            },
            { err -> onError(VolleyErrorMapper.toException("EarningApi(getFullyReceivedEarnings)", err)) }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    fun getAllMenuEarnings(
        buildingId: Long? = null,
        onSuccess: (List<Earnings>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/menu".toUri().buildUpon().apply {
            buildingId?.takeIf { it > 0L }?.let { appendQueryParameter("buildingId", it.toString()) }
        }.build().toString()

        val req = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { arr ->
                runCatching { parser.parseEarnings(arr) }
                    .onSuccess(onSuccess)
                    .onFailure { onError(it.toException()) }
            },
            { err -> onError(VolleyErrorMapper.toException("EarningApi(getAllMenuEarnings)", err)) }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    suspend fun getAllMenuEarningsSuspend(buildingId: Long? = null): List<Earnings> =
        suspendCancellableCoroutine { cont ->
            getAllMenuEarnings(
                buildingId = buildingId,
                onSuccess = { if (cont.isActive) cont.resume(it) },
                onError = { if (cont.isActive) cont.resumeWithException(it) }
            )
        }

    fun getEarningById(
        earningId: Long,
        onSuccess: (EarningWithCredits) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (earningId <= 0L) {
            onError(IllegalArgumentException("earningId is invalid"))
            return
        }

        val url = "$baseUrl/$earningId"
        val req = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { obj ->
                runCatching {
                    val parsed = parser.parseEarningWithCredits(obj)
                    EarningWithCredits(parsed.earning, parsed.credits)
                }.onSuccess(onSuccess).onFailure { onError(it.toException()) }
            },
            { err -> onError(VolleyErrorMapper.toException("EarningApi(getEarningById)", err)) }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    suspend fun getEarningByIdSuspend(earningId: Long): EarningWithCredits =
        suspendCancellableCoroutine { cont ->
            getEarningById(
                earningId = earningId,
                onSuccess = { if (cont.isActive) cont.resume(it) },
                onError = { if (cont.isActive) cont.resumeWithException(it) }
            )
        }

    fun createEarningsWithCredits(
        earnings: Earnings,
        onSuccess: (Long) -> Unit,
        onConflict: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val bId = earnings.buildingId ?: 0L
        if (bId <= 0L) {
            onError(IllegalArgumentException("buildingId is invalid"))
            return
        }
        val name = earnings.earningsName.trim()
        if (name.isEmpty()) {
            onError(IllegalArgumentException("earningsName is empty"))
            return
        }

        val url = "$baseUrl/with-credits"
        val body = mapper.earningWithCreditsCreateBody(earnings.copy(earningsName = name))

        val req = JsonObjectRequest(
            Request.Method.POST,
            url,
            body,
            { resp ->
                if (parser.parseOk(resp)) {
                    onSuccess(parser.parseEarningsId(resp))
                } else {
                    onError(IllegalStateException("unknown-server-response"))
                }
            },
            { err ->
                val code = err.networkResponse?.statusCode
                if (code == 409) onConflict()
                else onError(VolleyErrorMapper.toException("EarningApi(createEarningsWithCredits)", err))
            }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    suspend fun createEarningsWithCreditsSuspend(earnings: Earnings): Long =
        suspendCancellableCoroutine { cont ->
            createEarningsWithCredits(
                earnings = earnings,
                onSuccess = { if (cont.isActive) cont.resume(it) },
                onConflict = {
                    if (cont.isActive) cont.resumeWithException(IllegalStateException("earnings-conflict-with-existing-credits"))
                },
                onError = { if (cont.isActive) cont.resumeWithException(it) }
            )
        }

    fun insertNewEarning(
        earning: Earnings,
        onSuccess: (Long) -> Unit,
        onConflict: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val bId = earning.buildingId ?: 0L
        if (bId <= 0L) {
            onError(IllegalArgumentException("buildingId is invalid"))
            return
        }
        val name = earning.earningsName.trim()
        if (name.isEmpty()) {
            onError(IllegalArgumentException("earningsName is empty"))
            return
        }

        val body = mapper.earningCreateBody(earning.copy(earningsName = name))

        val req = JsonObjectRequest(
            Request.Method.POST,
            baseUrl,
            body,
            { resp ->
                if (parser.parseOk(resp)) onSuccess(parser.parseEarningsId(resp))
                else onError(IllegalStateException("unknown-server-response"))
            },
            { err ->
                val code = err.networkResponse?.statusCode
                if (code == 409) onConflict()
                else onError(VolleyErrorMapper.toException("EarningApi(insertNewEarning)", err))
            }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    suspend fun insertNewEarningSuspend(earning: Earnings): Long =
        suspendCancellableCoroutine { cont ->
            insertNewEarning(
                earning = earning,
                onSuccess = { if (cont.isActive) cont.resume(it) },
                onConflict = { if (cont.isActive) cont.resumeWithException(IllegalStateException("earnings-name-exists")) },
                onError = { if (cont.isActive) cont.resumeWithException(it) }
            )
        }

    fun insertEarningsWithCredits(
        earning: Earnings,
        onSuccess: (Long, Int) -> Unit,
        onConflict: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val bId = earning.buildingId ?: 0L
        if (bId <= 0L) {
            onError(IllegalArgumentException("buildingId is invalid"))
            return
        }
        val name = earning.earningsName.trim()
        if (name.isEmpty()) {
            onError(IllegalArgumentException("earningsName is empty"))
            return
        }

        val url = "$baseUrl/with-credits"
        val body = mapper.earningWithCreditsCreateBody(earning.copy(earningsName = name))

        val req = JsonObjectRequest(
            Request.Method.POST,
            url,
            body,
            { resp ->
                if (!parser.parseOk(resp)) {
                    onError(IllegalStateException("unknown-server-response"))
                } else {
                    onSuccess(parser.parseEarningsId(resp), parser.parseCreditsInserted(resp))
                }
            },
            { err ->
                val resp = err.networkResponse
                val rawBody = runCatching { String(resp?.data ?: ByteArray(0), Charsets.UTF_8) }.getOrDefault("")
                val message = if (rawBody.isNotBlank()) parser.parseMessage(rawBody) else ""

                when {
                    resp?.statusCode == 409 || message == "earnings-conflict-with-existing-credits" -> onConflict()
                    message == "period-range-too-short" -> onError(IllegalStateException("period-range-too-short"))
                    message == "startDate-must-be-before-or-equal-endDate" -> onError(IllegalStateException("startDate-must-be-before-or-equal-endDate"))
                    message == "invalid-date-format-expected-YYYY/MM/DD" -> onError(IllegalStateException("invalid-date-format-expected-YYYY/MM/DD"))
                    message == "invalid-amount" -> onError(IllegalStateException("invalid-amount"))
                    message == "invalid-buildingId" -> onError(IllegalStateException("invalid-buildingId"))
                    else -> onError(VolleyErrorMapper.toException("EarningApi(insertEarningsWithCredits)", err))
                }
            }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    suspend fun insertEarningsWithCreditsSuspend(earning: Earnings): Pair<Long, Int> =
        suspendCancellableCoroutine { cont ->
            insertEarningsWithCredits(
                earning = earning,
                onSuccess = { eId, count -> if (cont.isActive) cont.resume(eId to count) },
                onConflict = { if (cont.isActive) cont.resumeWithException(IllegalStateException("earnings-conflict-with-existing-credits")) },
                onError = { if (cont.isActive) cont.resumeWithException(it) }
            )
        }

    fun updateEarningWithCredits(
        earning: Earnings,
        onSuccess: (JSONObject) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val id = earning.earningsId
        if (id <= 0L) {
            onError(IllegalArgumentException("earningsId is invalid"))
            return
        }

        val url = "$baseUrl/$id/with-credits"
        val body = mapper.buildUpdateEarningWithCreditsBody(earning)

        val req = JsonObjectRequest(
            Request.Method.PUT,
            url,
            body,
            { resp -> onSuccess(resp) },
            { err ->
                val resp = err.networkResponse
                val rawBody = runCatching { String(resp?.data ?: ByteArray(0), Charsets.UTF_8) }.getOrDefault("")
                val message = if (rawBody.isNotBlank()) parser.parseMessage(rawBody) else ""
                when {
                    resp?.statusCode == 409 || message == "earnings-conflict-with-existing-credits" -> onError(IllegalStateException("earnings-conflict-with-existing-credits"))
                    message == "period-range-too-short" -> onError(IllegalStateException("period-range-too-short"))
                    message == "startDate-must-be-before-or-equal-endDate" -> onError(IllegalStateException("startDate-must-be-before-or-equal-endDate"))
                    message == "invalid-date-format-expected-YYYY/MM/DD" -> onError(IllegalStateException("invalid-date-format-expected-YYYY/MM/DD"))
                    message == "invalid-amount" -> onError(IllegalStateException("invalid-amount"))
                    message == "invalid-buildingId" -> onError(IllegalStateException("invalid-buildingId"))
                    else -> onError(VolleyErrorMapper.toException("EarningApi(insertEarningsWithCredits)", err))
                }
            }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    suspend fun updateEarningWithCreditsSuspend(earning: Earnings): JSONObject =
        suspendCancellableCoroutine { cont ->
            updateEarningWithCredits(
                earning = earning,
                onSuccess = { if (cont.isActive) cont.resume(it) },
                onError = { if (cont.isActive) cont.resumeWithException(it) }
            )
        }

    fun earningNameExists(
        buildingId: Long?,
        earningName: String,
        onSuccess: (Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val name = earningName.trim()
        if (name.isEmpty()) {
            onError(IllegalArgumentException("earningName is empty"))
            return
        }

        val url = "$baseUrl/name-exists".toUri().buildUpon()
            .appendQueryParameter("earningsName", name)
            .apply { buildingId?.takeIf { it > 0L }?.let { appendQueryParameter("buildingId", it.toString()) } }
            .build()
            .toString()

        val req = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { obj -> onSuccess(obj.optBoolean("exists", false)) },
            { err -> onError(VolleyErrorMapper.toException("EarningApi(earningNameExists)", err)) }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    suspend fun earningNameExistsSuspend(buildingId: Long?, earningName: String): Boolean =
        suspendCancellableCoroutine { cont ->
            earningNameExists(
                buildingId = buildingId,
                earningName = earningName,
                onSuccess = { if (cont.isActive) cont.resume(it) },
                onError = { if (cont.isActive) cont.resumeWithException(it) }
            )
        }

    fun markSelectedCreditsAsReceived(
        earningId: Long,
        creditIds: List<Long>,
        onSuccess: (List<Credits>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (earningId <= 0L) {
            onError(IllegalArgumentException("earningId is invalid"))
            return
        }

        val ids = creditIds.filter { it > 0L }.distinct()
        if (ids.isEmpty()) {
            onError(IllegalArgumentException("creditIds is empty"))
            return
        }

        val url = "$baseUrl/mark-received"
        val body = mapper.buildMarkReceivedBody(earningId, ids)

        val req = JsonObjectRequest(
            Request.Method.POST,
            url,
            body,
            { obj ->
                runCatching {
                    val creditsJson = obj.optJSONArray("credits") ?: JSONArray()
                    parser.parseCredits(creditsJson)
                }.onSuccess(onSuccess).onFailure { onError(it.toException()) }
            },
            { err -> onError(VolleyErrorMapper.toException("EarningApi(markSelectedCreditsAsReceived)", err)) }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    suspend fun markSelectedCreditsAsReceivedSuspend(earningId: Long, creditIds: List<Long>): List<Credits> =
        suspendCancellableCoroutine { cont ->
            markSelectedCreditsAsReceived(
                earningId = earningId,
                creditIds = creditIds,
                onSuccess = { if (cont.isActive) cont.resume(it) },
                onError = { if (cont.isActive) cont.resumeWithException(it) }
            )
        }

    fun hasReceivedCredits(
        earningId: Long,
        onSuccess: (Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (earningId <= 0L) {
            onError(IllegalArgumentException("earningId is invalid"))
            return
        }

        val url = "$baseUrl/$earningId/has-received"

        val req = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { resp -> onSuccess(parser.parseHasReceived(resp, "hasReceived", false)) },
            { err -> onError(VolleyErrorMapper.toException("EarningApi(hasReceivedCredits)", err)) }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    suspend fun hasReceivedCreditsSuspend(earningId: Long): Boolean =
        suspendCancellableCoroutine { cont ->
            hasReceivedCredits(
                earningId = earningId,
                onSuccess = { if (cont.isActive) cont.resume(it) },
                onError = { if (cont.isActive) cont.resumeWithException(it) }
            )
        }

    fun deleteEarningWithCreditsIfNoReceived(
        earningId: Long,
        onSuccess: (Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (earningId <= 0L) {
            onError(IllegalArgumentException("earningId is invalid"))
            return
        }

        val url = "$baseUrl/$earningId/with-credits"

        val req = JsonObjectRequest(
            Request.Method.DELETE,
            url,
            null,
            { resp -> onSuccess(resp.optBoolean("ok", false)) },
            { err -> onError(VolleyErrorMapper.toException("EarningApi(deleteEarningWithCreditsIfNoReceived)", err)) }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    suspend fun deleteEarningWithCreditsIfNoReceivedSuspend(earningId: Long): Boolean =
        suspendCancellableCoroutine { cont ->
            deleteEarningWithCreditsIfNoReceived(
                earningId = earningId,
                onSuccess = { if (cont.isActive) cont.resume(it) },
                onError = { if (cont.isActive) cont.resumeWithException(it) }
            )
        }

    fun hasReceivedCredit(
        earningId: Long,
        onSuccess: (Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (earningId <= 0L) {
            onError(IllegalArgumentException("earningId is invalid"))
            return
        }

        val url = "$baseUrl/has-received-credit".toUri()
            .buildUpon()
            .appendQueryParameter("earningId", earningId.toString())
            .build()
            .toString()

        val req = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { o -> onSuccess(o.optBoolean("hasReceivedCredit", false)) },
            { err -> onError(VolleyErrorMapper.toException("EarningApi(hasReceivedCredit)", err)) }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    suspend fun hasReceivedCreditSuspend(earningId: Long): Boolean =
        suspendCancellableCoroutine { cont ->
            hasReceivedCredit(
                earningId = earningId,
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
