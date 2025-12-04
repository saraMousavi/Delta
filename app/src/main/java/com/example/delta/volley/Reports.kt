// com/example/delta/volley/ReportsApi.kt
package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.entity.Units
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Debts
import com.example.delta.data.entity.Earnings
import com.example.delta.data.entity.Credits
import com.example.delta.enums.CalculateMethod
import com.example.delta.enums.FundType
import com.example.delta.enums.PaymentLevel
import com.example.delta.enums.Period
import com.example.delta.enums.Responsible
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class DashboardResponse(
    val ok: Boolean,
    val buildingId: Long,
    val units: List<Units>,
    val debtList: List<Debts>,
    val paysList: List<Debts>,
    val costs: List<Costs>,
    val earnings: List<Earnings>,
    val pendingReceipt: List<Credits>,
    val receiptList: List<Credits>,
    val debts: List<Debts>,
    val credits: List<Credits>,

    val capitalSummary: ChartSummary,
    val chargeSummary: ChartSummary,
    val operationalSummary: ChartSummary,
    val capitalDetailByOwner: List<CapitalOwnerBreakdown>,
    val chargeDetailByUnit: List<UnitBreakdown>,
    val operationalDetailByUnit: List<UnitBreakdown>
)


data class ChartSummary(
    val paid: Double,
    val unpaid: Double
)

data class CapitalOwnerBreakdown(
    val ownerId: Long,
    val userId: Long?,
    val fullName: String,
    val paid: Double,
    val unpaid: Double
)

data class UnitBreakdown(
    val unitId: Long,
    val unitNumber: String,
    val paid: Double,
    val unpaid: Double
)



class Reports {

    private val baseUrl = "http://217.144.107.231:3000/reports"

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

    private fun parseChartSummary(obj: JSONObject?): ChartSummary {
        if (obj == null) return ChartSummary(0.0, 0.0)
        return ChartSummary(
            paid = obj.optDouble("paid", 0.0),
            unpaid = obj.optDouble("unpaid", 0.0)
        )
    }

    private fun parseCapitalOwnerBreakdown(arr: JSONArray?): List<CapitalOwnerBreakdown> {
        if (arr == null) return emptyList()
        val list = mutableListOf<CapitalOwnerBreakdown>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list += CapitalOwnerBreakdown(
                ownerId = o.optLong("ownerId"),
                userId = if (o.isNull("userId")) null else o.optLong("userId"),
                fullName = o.optString("fullName", ""),
                paid = o.optDouble("paid", 0.0),
                unpaid = o.optDouble("unpaid", 0.0)
            )
        }
        return list
    }

    private fun parseUnitBreakdown(arr: JSONArray?): List<UnitBreakdown> {
        if (arr == null) return emptyList()
        val list = mutableListOf<UnitBreakdown>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list += UnitBreakdown(
                unitId = o.optLong("unitId"),
                unitNumber = o.optString("unitNumber", ""),
                paid = o.optDouble("paid", 0.0),
                unpaid = o.optDouble("unpaid", 0.0)
            )
        }
        return list
    }

    fun getDashboardData(
        context: Context,
        buildingId: Long,
        onSuccess: (DashboardResponse) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/dashboard?buildingId=$buildingId"
        val queue = Volley.newRequestQueue(context)

        val request = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { resp ->
                try {
                    val result = parseDashboardResponse(resp)
                    Log.d("dashBoardResult", result.toString())
                    onSuccess(result)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err ->
                onError(formatVolleyError("ReportsApi(getDashboardData)", err))
            }
        )

        queue.add(request)
    }

    suspend fun getDashboardDataSuspend(
        context: Context,
        buildingId: Long
    ): DashboardResponse = suspendCancellableCoroutine { cont ->
        getDashboardData(
            context = context,
            buildingId = buildingId,
            onSuccess = { if (cont.isActive) cont.resume(it) },
            onError = { if (cont.isActive) cont.resumeWithException(it) }
        )
    }

    private fun parseDashboardResponse(resp: JSONObject): DashboardResponse {
        fun parseUnits(arr: JSONArray): List<Units> {
            val list = mutableListOf<Units>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list += Units(
                    unitId = o.optLong("unitId", 0L),
                    buildingId = o.optLong("buildingId", 0L),
                    unitNumber = o.optString("unitNumber", ""),
                    area = o.optDouble("area", 0.0).toString(),
                    numberOfRooms = o.optInt("numberOfRooms", 0).toString(),
                    numberOfParking = o.optInt("numberOfParking", 0).toString(),
                    numberOfWarehouse = o.optInt("numberOfWarehouse", 0).toString(),
                    postCode = o.optString("postCode")
                )
            }
            return list
        }

        fun parseDebts(arr: JSONArray): List<Debts> {
            val list = mutableListOf<Debts>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list += Debts(
                    debtId = o.optLong("debtId", 0L),
                    buildingId = o.optLong("buildingId", 0L),
                    unitId = o.optLong("unitId", 0L),
                    costId = o.optLong("costId", 0L),
                    ownerId = if (o.isNull("ownerId")) null else o.optLong("ownerId"),
                    description = o.optString("description", ""),
                    dueDate = o.optString("dueDate", ""),
                    amount = o.optDouble("amount", 0.0),
                    paymentFlag = o.optBoolean("paymentFlag", false)
                )
            }
            return list
        }

        fun parseCosts(arr: JSONArray): List<Costs> {
            val list = mutableListOf<Costs>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list += Costs(
                    costId        = o.optLong("costId"),
                    buildingId    = if (o.isNull("buildingId")) null else o.optLong("buildingId"),
                    costName      = o.optString("costName"),
                    tempAmount    = o.optDouble("tempAmount", 0.0),
                    period        = runCatching {
                        val raw = o.optString("period", "").trim()
                        if (raw.isEmpty()) {
                            Period.NONE
                        } else {
                            Period.valueOf(raw.uppercase(Locale.US))
                        }
                    }.getOrElse { Period.NONE },
                    calculateMethod = runCatching {
                        val raw = o.optString("calculateMethod", "").trim()
                        if (raw.isEmpty()) {
                            CalculateMethod.EQUAL
                        } else {
                            CalculateMethod.valueOf(raw.uppercase(Locale.US))
                        }
                    }.getOrElse { CalculateMethod.EQUAL},
                    paymentLevel  = runCatching {
                        val raw = o.optString("paymentLevel", "").trim()
                        if (raw.isEmpty()) {
                            PaymentLevel.BUILDING
                        } else {
                            PaymentLevel.valueOf(raw.uppercase(Locale.US))
                        }
                    }.getOrElse { PaymentLevel.BUILDING},
                    responsible   = Responsible.valueOf(o.optString("responsible")),
                    fundType      = FundType.valueOf(o.optString("fundType")),
                    chargeFlag    = o.optBoolean("chargeFlag", false),
                    dueDate       = o.optString("dueDate")
                )
            }
            return list
        }

        fun parseEarnings(arr: JSONArray): List<Earnings> {
            val list = mutableListOf<Earnings>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val periodStr = o.optString("period", "NONE")
                val periodEnum = try {
                    Period.valueOf(periodStr)
                } catch (_: Exception) {
                    Period.NONE
                }
                list += Earnings(
                    earningsId = o.optLong("earningsId", 0L),
                    earningsName = o.optString("earningsName", ""),
                    buildingId = if (o.isNull("buildingId")) null else o.optLong("buildingId"),
                    amount = o.optDouble("amount", 0.0),
                    period = periodEnum,
                    invoiceFlag = o.optBoolean("invoiceFlag", false),
                    startDate = o.optString("startDate", ""),
                    endDate = o.optString("endDate", "")
                )
            }
            return list
        }

        fun parseCredits(arr: JSONArray): List<Credits> {
            val list = mutableListOf<Credits>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list += Credits(
                    creditsId = o.optLong("creditsId", 0L),
                    earningsId = o.optLong("earningsId", 0L),
                    buildingId = o.optLong("buildingId", 0L),
                    description = o.optString("description", ""),
                    dueDate = o.optString("dueDate", ""),
                    amount = o.optDouble("amount", 0.0),
                    receiptFlag = o.optBoolean("receiptFlag", false)
                )
            }
            return list
        }

        val unitsArr          = resp.optJSONArray("units")          ?: JSONArray()
        val debtListArr       = resp.optJSONArray("debtList")       ?: JSONArray()
        val paysListArr       = resp.optJSONArray("paysList")       ?: JSONArray()
        val costsArr          = resp.optJSONArray("costs")          ?: JSONArray()
        val earningsArr       = resp.optJSONArray("earnings")       ?: JSONArray()
        val pendingReceiptArr = resp.optJSONArray("pendingReceipt") ?: JSONArray()
        val receiptListArr    = resp.optJSONArray("receiptList")    ?: JSONArray()
        val debts    = resp.optJSONArray("debts")    ?: JSONArray()
        val credits    = resp.optJSONArray("credits")    ?: JSONArray()

        val debtsArr          = resp.optJSONArray("debts")
        val creditsArr        = resp.optJSONArray("credits")

        val capitalSummaryObj     = resp.optJSONObject("capitalSummary")
        val chargeSummaryObj      = resp.optJSONObject("chargeSummary")
        val operationalSummaryObj = resp.optJSONObject("operationalSummary")

        val capitalDetailArr      = resp.optJSONArray("capitalDetailByOwner")
        val chargeDetailArr       = resp.optJSONArray("chargeDetailByUnit")
        val operationalDetailArr  = resp.optJSONArray("operationalDetailByUnit")

        return DashboardResponse(
            ok             = resp.optBoolean("ok", false),
            buildingId     = resp.optLong("buildingId"),
            units          = parseUnits(unitsArr ?: JSONArray()),
            debtList       = parseDebts(debtListArr ?: JSONArray()),
            paysList       = parseDebts(paysListArr ?: JSONArray()),
            costs          = parseCosts(costsArr ?: JSONArray()),
            earnings       = parseEarnings(earningsArr ?: JSONArray()),
            pendingReceipt = parseCredits(pendingReceiptArr ?: JSONArray()),
            receiptList    = parseCredits(receiptListArr ?: JSONArray()),
            debts          = parseDebts(debtsArr ?: JSONArray()),
            credits        = parseCredits(creditsArr ?: JSONArray()),
            capitalSummary = parseChartSummary(capitalSummaryObj),
            chargeSummary  = parseChartSummary(chargeSummaryObj),
            operationalSummary = parseChartSummary(operationalSummaryObj),
            capitalDetailByOwner = parseCapitalOwnerBreakdown(capitalDetailArr),
            chargeDetailByUnit = parseUnitBreakdown(chargeDetailArr),
            operationalDetailByUnit = parseUnitBreakdown(operationalDetailArr)
        )
    }


}
