// com/example/delta/volley/ReportsApi.kt
package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.Units
import com.example.delta.data.entity.Owners
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
    val buildingId: Long,
    val units: List<Units>,
    val owners: List<Owners>,
    val debts: List<Debts>,
    val pays: List<Debts>,      // paid debts (paymentFlag = true)
    val costs: List<Costs>,
    val earnings: List<Earnings>,
    val credits: List<Credits>
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

    private fun parseDashboardResponse(json: JSONObject): DashboardResponse {
        val buildingId = json.optLong("buildingId", 0L)
        val units = parseUnitsArray(json.optJSONArray("units") ?: JSONArray())
        val owners = parseOwnersArray(json.optJSONArray("owners") ?: JSONArray())
        val debts = parseDebtsArray(json.optJSONArray("debts") ?: JSONArray())
        val pays = parseDebtsArray(json.optJSONArray("pays") ?: JSONArray())
        val costs = parseCostsArray(json.optJSONArray("costs") ?: JSONArray())
        val earnings = parseEarningsArray(json.optJSONArray("earnings") ?: JSONArray())
        val credits = parseCreditsArray(json.optJSONArray("credits") ?: JSONArray())

        return DashboardResponse(
            buildingId = buildingId,
            units = units,
            owners = owners,
            debts = debts,
            pays = pays,
            costs = costs,
            earnings = earnings,
            credits = credits
        )
    }

    private fun parseUnitsArray(arr: JSONArray): List<Units> {
        val out = ArrayList<Units>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                Units(
                    unitId = o.optLong("unitId", 0L),
                    buildingId = o.optLong("buildingId", 0L),
                    unitNumber = o.optString("unitNumber", ""),
                    area = o.optDouble("area", 0.0).toString(),
                    numberOfRooms = o.optInt("numberOfRooms", 0).toString(),
                    numberOfParking = o.optInt("numberOfParking", 0).toString(),
                    numberOfWarehouse = o.optInt("numberOfWarehouse", 0).toString(),
                    postCode = o.optString("postCode")
                )
            )
        }
        return out
    }

    private fun parseOwnersArray(arr: JSONArray): List<Owners> {
        val out = ArrayList<Owners>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                Owners(
                    ownerId = o.optLong("ownerId", 0L),
                    firstName = o.optString("firstName", ""),
                    lastName = o.optString("lastName", ""),
                    phoneNumber = o.optString("phoneNumber", ""),
                    mobileNumber = o.optString("mobileNumber", ""),
                    birthday = o.optString("birthday", ""),
                    address = o.optString("address", ""),
                    email = o.optString("email", "")
                )
            )
        }
        return out
    }

    private fun parseDebtsArray(arr: JSONArray): List<Debts> {
        val out = ArrayList<Debts>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                Debts(
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
            )
        }
        return out
    }

    private fun parseCostsArray(arr: JSONArray): List<Costs> {
        val out = ArrayList<Costs>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val periodStr = o.optString("period", "NONE")
            val periodEnum = try {
                Period.valueOf(periodStr)
            } catch (_: Exception) {
                Period.NONE
            }
            out.add(
                Costs(
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
            )
        }
        return out
    }

    private fun parseEarningsArray(arr: JSONArray): List<Earnings> {
        val out = ArrayList<Earnings>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val periodStr = o.optString("period", "NONE")
            val periodEnum = try {
                Period.valueOf(periodStr)
            } catch (_: Exception) {
                Period.NONE
            }
            out.add(
                Earnings(
                    earningsId = o.optLong("earningsId", 0L),
                    earningsName = o.optString("earningsName", ""),
                    buildingId = if (o.isNull("buildingId")) null else o.optLong("buildingId"),
                    amount = o.optDouble("amount", 0.0),
                    period = periodEnum,
                    invoiceFlag = o.optBoolean("invoiceFlag", false),
                    startDate = o.optString("startDate", ""),
                    endDate = o.optString("endDate", "")
                )
            )
        }
        return out
    }

    private fun parseCreditsArray(arr: JSONArray): List<Credits> {
        val out = ArrayList<Credits>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                Credits(
                    creditsId = o.optLong("creditsId", 0L),
                    earningsId = o.optLong("earningsId", 0L),
                    buildingId = o.optLong("buildingId", 0L),
                    description = o.optString("description", ""),
                    dueDate = o.optString("dueDate", ""),
                    amount = o.optDouble("amount", 0.0),
                    receiptFlag = o.optBoolean("receiptFlag", false)
                )
            )
        }
        return out
    }
}
