package com.example.delta.volley

import android.content.Context
import android.net.Uri
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Debts
import com.example.delta.init.AppRequestQueue
import com.example.delta.init.FinancialReportRow
import com.example.delta.server.JsonMapper
import com.example.delta.server.JsonParser
import com.example.delta.server.VolleyErrorMapper
import com.example.delta.server.toException
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import androidx.core.net.toUri

class Cost(
    appContext: Context,
    private val baseUrl: String = "http://185.129.197.6:443/costs",
    private val baseBuildingUrl: String = "http://185.129.197.6:443/building",
    private val queue: RequestQueue = AppRequestQueue.getInstance(appContext.applicationContext).requestQueue,
    private val mapper: JsonMapper = JsonMapper(),
    private val parser: JsonParser = JsonParser(),
    private val buildingParser: JsonParser = JsonParser()
) {

    data class BuildingWithCosts(
        val building: Buildings,
        val costs: List<Costs>
    )


    fun insertCost(
        costsJsonArray: JSONArray,
        debtsJsonArray: JSONArray,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        for (i in 0 until costsJsonArray.length()) {
            val costObj = costsJsonArray.optJSONObject(i)
            if (costObj != null && costObj.has("paymentDate")) {
                costObj.remove("paymentDate")
            }
        }

        val payload = JSONObject().apply {
            put("cost", costsJsonArray)
            put("debts", debtsJsonArray)
        }

        val req = JsonObjectRequest(
            Request.Method.POST,
            baseUrl,
            payload,
            { resp -> onSuccess(resp.toString()) },
            { err -> onError(VolleyErrorMapper.toException("CostApi(insertCost:raw)", err)) }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    suspend fun fetchAllCostsWithDebtsSuspend(
        buildingId: Long? = null
    ): Pair<List<Costs>, List<Debts>> =
        suspendCancellableCoroutine { cont ->
            fetchCostsWithDebts(
                buildingId = buildingId,
                onSuccess = { costs, debts ->
                    if (cont.isActive) cont.resume(costs to debts)
                },
                onError = { e ->
                    if (cont.isActive) cont.resumeWithException(e)
                }
            )
        }


    fun fetchBuildingsWithCosts(
        mobileNumber: String,
        roleId: Long,
        onSuccess: (List<BuildingWithCosts>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val safeMobile = mobileNumber.trim()
        if (safeMobile.isEmpty()) {
            onError(IllegalArgumentException("mobileNumber is empty"))
            return
        }

        val url = "$baseBuildingUrl/with-costs".toUri()
            .buildUpon()
            .appendQueryParameter("mobileNumber", safeMobile)
            .appendQueryParameter("roleId", roleId.toString())
            .build()
            .toString()

        val req = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { arr ->
                runCatching {
                    val out = ArrayList<BuildingWithCosts>(arr.length())
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val building = buildingParser.parseBuilding(obj)
                        val costs = parser.parseCosts(obj.optJSONArray("costs") ?: JSONArray())
                        out.add(BuildingWithCosts(building = building, costs = costs))
                    }
                    out
                }.onSuccess(onSuccess).onFailure { onError(it.toException()) }
            },
            { err -> onError(VolleyErrorMapper.toException("CostApi(fetchBuildingsWithCosts)", err)) }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    fun fetchCostsWithDebts(
        ownerId: Long? = null,
        unitId: Long? = null,
        userId: Long? = null,
        buildingId: Long? = null,
        onSuccess: (List<Costs>, List<Debts>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = Uri.parse("$baseUrl/full").buildUpon().apply {
            ownerId?.takeIf { it > 0 }?.let { appendQueryParameter("ownerId", it.toString()) }
            unitId?.takeIf { it > 0 }?.let { appendQueryParameter("unitId", it.toString()) }
            userId?.takeIf { it > 0 }?.let { appendQueryParameter("userId", it.toString()) }
            buildingId?.takeIf { it > 0 }?.let { appendQueryParameter("buildingId", it.toString()) }
        }.build().toString()

        val req = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { resp ->
                runCatching {
                    val costs = parser.parseCosts(resp.optJSONArray("costs") ?: JSONArray())
                    val debts = parser.parseDebts(resp.optJSONArray("debts") ?: JSONArray())
                    costs to debts
                }.onSuccess { (c, d) -> onSuccess(c, d) }
                    .onFailure { onError(it.toException()) }
            },
            { err -> onError(VolleyErrorMapper.toException("CostApi(fetchCostsWithDebts)", err)) }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    fun markCostInvoicedOnServer(
        costId: Long,
        onSuccess: (Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (costId <= 0L) {
            onError(IllegalArgumentException("costId is invalid"))
            return
        }

        val url = "$baseUrl/$costId/mark-invoiced"
        val payload = JSONObject().apply { put("costId", costId) }

        val req = JsonObjectRequest(
            Request.Method.POST,
            url,
            payload,
            { resp -> onSuccess(resp.optBoolean("ok", false)) },
            { err -> onError(VolleyErrorMapper.toException("CostApi(markCostInvoicedOnServer)", err)) }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    fun invoiceCostIfEnoughFund(
        costId: Long,
        onSuccess: (Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (costId <= 0L) {
            onError(IllegalArgumentException("costId is invalid"))
            return
        }

        val url = "$baseUrl/invoice"
        val body = JSONObject().apply { put("costId", costId) }

        val req = JsonObjectRequest(
            Request.Method.POST,
            url,
            body,
            { resp -> onSuccess(resp.optBoolean("ok", false)) },
            { err -> onError(VolleyErrorMapper.toException("CostApi(invoiceCostIfEnoughFund)", err)) }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    fun fetchGlobalCosts(
        buildingId: Long? = null,
        onSuccess: (List<Costs>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = baseUrl.toUri().buildUpon()
            .appendQueryParameter("page", "1")
            .appendQueryParameter("limit", "100")
            .apply {
                buildingId?.takeIf { it > 0 }?.let { appendQueryParameter("buildingId", it.toString()) }
            }
            .build()
            .toString()

        val req = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { arr ->
                runCatching { parser.parseCosts(arr) }
                    .onSuccess(onSuccess)
                    .onFailure { onError(it.toException()) }
            },
            { err -> onError(VolleyErrorMapper.toException("CostApi(fetchGlobalCosts)", err)) }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    fun createGlobalCost(
        cost: Costs,
        onSuccess: (Costs) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val costJson = mapper.costToJson(cost.copy(buildingId = 0L)).apply {
            put("buildingId", 0)
            put("capitalFlag", false)
            put("invoiceFlag", false)
        }

        val body = JSONObject().apply {
            put("cost", costJson)
            put("debts", JSONArray())
        }
        if (costJson.has("paymentDate")) costJson.remove("paymentDate")

        Log.e("createGlobalCost", costJson.toString(2))

        val req = JsonObjectRequest(
            Request.Method.POST,
            baseUrl,
            body,
            { resp ->
                runCatching {
                    val id = resp.optLong("costId", 0L)
                    cost.copy(costId = id)
                }.onSuccess(onSuccess).onFailure { onError(it.toException()) }
            },
            { err ->
                val status = err.networkResponse?.statusCode
                val raw = err.networkResponse?.data?.toString(Charsets.UTF_8)
                Log.e("CostApi(createGlobalCost)", "HTTP $status body=$raw", err)
                onError(VolleyErrorMapper.toException("CostApi(createGlobalCost)", err)) }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    suspend fun fetchGlobalCostsSuspend(buildingId: Long? = null): List<Costs> =
        suspendCancellableCoroutine { cont ->
            fetchGlobalCosts(
                buildingId = buildingId,
                onSuccess = { if (cont.isActive) cont.resume(it) },
                onError = { if (cont.isActive) cont.resumeWithException(it) }
            )
        }

    fun costNameExists(
        buildingId: Long?,
        costName: String,
        onSuccess: (Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val safeName = costName.trim()
        if (safeName.isEmpty()) {
            onError(IllegalArgumentException("costName is empty"))
            return
        }

        val url = Uri.parse("$baseUrl/name-exists").buildUpon()
            .appendQueryParameter("costName", safeName)
            .apply { buildingId?.takeIf { it > 0 }?.let { appendQueryParameter("buildingId", it.toString()) } }
            .build()
            .toString()

        val req = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { obj -> onSuccess(obj.optBoolean("exists", false)) },
            { err -> onError(VolleyErrorMapper.toException("CostApi(costNameExists)", err)) }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    suspend fun costNameExistsSuspend(buildingId: Long?, costName: String): Boolean =
        suspendCancellableCoroutine { cont ->
            costNameExists(
                buildingId = buildingId,
                costName = costName,
                onSuccess = { if (cont.isActive) cont.resume(it) },
                onError = { if (cont.isActive) cont.resumeWithException(it) }
            )
        }

    fun updateCost(
        costId: Long,
        payload: JSONObject,
        onSuccess: (JSONObject) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (costId <= 0L) {
            onError(IllegalArgumentException("costId is invalid"))
            return
        }

        val url = "$baseUrl/$costId"
        val req = JsonObjectRequest(
            Request.Method.PUT,
            url,
            payload,
            { resp -> onSuccess(resp) },
            { err -> onError(VolleyErrorMapper.toException("CostApi(updateCost)", err)) }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    suspend fun updateCostSuspend(cost: Costs): Unit =
        suspendCancellableCoroutine { cont ->
            val id = cost.costId
            if (id <= 0L) {
                cont.resumeWithException(IllegalArgumentException("Invalid costId"))
                return@suspendCancellableCoroutine
            }
            val payload = mapper.buildUpdatePayload(cost)
            updateCost(
                costId = id,
                payload = payload,
                onSuccess = { if (cont.isActive) cont.resume(Unit) },
                onError = { if (cont.isActive) cont.resumeWithException(it) }
            )
        }

    fun deleteCost(
        costId: Long,
        onSuccess: (Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (costId <= 0L) {
            onError(IllegalArgumentException("costId is invalid"))
            return
        }

        val url = "$baseUrl/$costId"
        val req = JsonObjectRequest(
            Request.Method.DELETE,
            url,
            null,
            { resp -> onSuccess(resp.optBoolean("ok", true)) },
            { err -> onError(VolleyErrorMapper.toException("CostApi(deleteCost)", err)) }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    fun deleteCostWithLinked(
        buildingId: Long,
        costId: Long,
        onSuccess: () -> Unit,
        onNotFound: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val url = "$baseUrl/cost/$buildingId/$costId/with-linked"

        val req = JsonObjectRequest(
            Request.Method.DELETE,
            url,
            null,
            { _ -> onSuccess() },
            { err ->
                when (err.networkResponse?.statusCode) {
                    404 -> onNotFound()
                    else -> onError(err)
                }
            }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    fun insertDebtForCapitalCostOnServer(
        buildingId: Long,
        cost: Costs,
        amountPerOwner: Double,
        dueDate: String,
        description: String,
        onSuccess: (List<Costs>, List<Debts>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (buildingId <= 0L) {
            onError(IllegalArgumentException("buildingId is invalid"))
            return
        }

        val costJson = mapper.costToJson(cost).apply {
            put("invoiceFlag", cost.invoiceFlag)
        }

        val payload = JSONObject().apply {
            put("buildingId", buildingId)
            put("amountPerOwner", amountPerOwner)
            put("dueDate", dueDate)
            put("description", description)
            put("cost", costJson)
        }

        val url = "$baseUrl/capital-with-debts"
        val req = JsonObjectRequest(
            Request.Method.POST,
            url,
            payload,
            { resp ->
                runCatching {
                    val insertedCosts = parser.parseCosts(resp.optJSONArray("insertedCosts") ?: JSONArray())
                    val insertedDebts = parser.parseDebts(resp.optJSONArray("insertedDebts") ?: JSONArray())
                    insertedCosts to insertedDebts
                }.onSuccess { (c, d) -> onSuccess(c, d) }
                    .onFailure { onError(it.toException()) }
            },
            { err -> onError(VolleyErrorMapper.toException("CostApi(insertDebtForCapitalCostOnServer)", err)) }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    suspend fun insertDebtForCapitalCostSuspend(
        buildingId: Long,
        cost: Costs,
        amountPerOwner: Double,
        dueDate: String,
        description: String
    ): Pair<List<Costs>, List<Debts>> =
        suspendCancellableCoroutine { cont ->
            insertDebtForCapitalCostOnServer(
                buildingId = buildingId,
                cost = cost,
                amountPerOwner = amountPerOwner,
                dueDate = dueDate,
                description = description,
                onSuccess = { c, d -> if (cont.isActive) cont.resume(c to d) },
                onError = { e -> if (cont.isActive) cont.resumeWithException(e) }
            )
        }

    fun fetchAllCostsWithDebtsSuspend(context: Context): Pair<List<Costs>, List<Debts>> =
        throw UnsupportedOperationException("Use fetchCostsWithDebts with suspend wrapper if needed")

    fun fetchOwnerFinancialReportRows(
        ownerId: Long,
        startDate: String,
        endDate: String,
        onSuccess: (List<FinancialReportRow>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (ownerId <= 0L) {
            onError(IllegalArgumentException("ownerId is invalid"))
            return
        }

        val url = Uri.parse("$baseUrl/report/pdf").buildUpon()
            .appendQueryParameter("ownerId", ownerId.toString())
            .appendQueryParameter("startDate", startDate)
            .appendQueryParameter("endDate", endDate)
            .build()
            .toString()

        val req = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { arr ->
                runCatching {
                    val out = ArrayList<FinancialReportRow>(arr.length())
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        out.add(
                            FinancialReportRow(
                                description = o.optString("description", ""),
                                dueDate = o.optString("dueDate", ""),
                                paymentDate = if (o.isNull("paymentDate")) null else o.optString("paymentDate"),
                                isPaid = o.optBoolean("paymentFlag", false),
                                amount = o.optDouble("amount", 0.0)
                            )
                        )
                    }
                    out
                }.onSuccess(onSuccess).onFailure { onError(it.toException()) }
            },
            { err -> onError(VolleyErrorMapper.toException("CostApi(fetchOwnerFinancialReportRows)", err)) }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
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

