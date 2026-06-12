
package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.example.delta.data.entity.BuildingWithCounts
import com.example.delta.data.entity.BuildingTypes
import com.example.delta.data.entity.BuildingUsages
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.CityComplexes
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.OwnersUnitsCrossRef
import com.example.delta.data.entity.TenantsUnitsCrossRef
import com.example.delta.data.entity.Units
import com.example.delta.data.entity.UploadedFileEntity
import com.example.delta.data.entity.User
import com.example.delta.enums.Roles
import com.example.delta.init.AppRequestQueue
import com.example.delta.server.JsonParser
import com.example.delta.server.JsonObjectArrayBodyRequest
import com.example.delta.server.VolleyErrorMapper
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import androidx.core.net.toUri
import com.android.volley.Request.Method
import com.android.volley.toolbox.Volley
import com.example.delta.server.JsonMapper
import com.example.delta.server.toException


class Building(
    private val appContext: Context,
    private val baseUrl: String = "http://185.129.197.6:443/building",
    private val queue: RequestQueue = AppRequestQueue.getInstance(appContext).requestQueue,
    private val parser: JsonParser = JsonParser(),
    private val mapper: JsonMapper = JsonMapper()
) {

    data class BuildingWithResidents(
        val building: Buildings,
        val units: List<Units>,
        val owners: List<User>,
        val tenants: List<User>,
        val ownerUnits: List<OwnersUnitsCrossRef>,
        val tenantUnits: List<TenantsUnitsCrossRef>
    )

    data class UserRole(
        val user: User,
        val roles: Roles
    )

    data class BuildingFullDto(
        val building: Buildings,
        val buildingType: BuildingTypes?,
        val buildingUsage: BuildingUsages?,
        val cityComplex: CityComplexes?,
        val files: List<UploadedFileEntity>,
        val units: List<Units>,
        val users: List<User>,
        val role: List<UserRole>,
        val ownerUnits: List<OwnersUnitsCrossRef>,
        val tenantUnits: List<TenantsUnitsCrossRef>,
        val costs: List<Costs>,
        val defaultChargeCosts: List<Costs>,
        val chargeCostsForYear: List<Costs>
    )

    fun fetchBuildingsWithResidents(
        mobileNumber: String,
        onSuccess: (List<BuildingWithResidents>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val safeMobile = mobileNumber.trim()
        if (safeMobile.isEmpty()) {
            onError(IllegalArgumentException("mobileNumber is empty"))
            return
        }

        val url = "$baseUrl/with-residents".toUri()
            .buildUpon()
            .appendQueryParameter("mobileNumber", safeMobile)
            .build()
            .toString()

        val req = JsonArrayRequest(
            Method.GET,
            url,
            null,
            { arr ->
                runCatching {
                    val out = ArrayList<BuildingWithResidents>(arr.length())
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        out.add(
                            BuildingWithResidents(
                                building = parser.parseBuilding(obj.getJSONObject("building")),
                                units = parser.parseUnits(obj.optJSONArray("units") ?: JSONArray()),
                                owners = parser.parseUsers(obj.optJSONArray("owners") ?: JSONArray()),
                                tenants = parser.parseUsers(obj.optJSONArray("tenants") ?: JSONArray()),
                                ownerUnits = parser.parseOwnerUnits(obj.optJSONArray("ownerUnits") ?: JSONArray()),
                                tenantUnits = parser.parseTenantUnits(obj.optJSONArray("tenantUnits") ?: JSONArray())
                            )
                        )
                    }
                    out
                }.onSuccess(onSuccess)
                    .onFailure { onError(it.toException()) }
            },
            { err -> onError(VolleyErrorMapper.toException("fetchBuildingsWithResidents", err)) }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }


    fun insertBuilding(
        mobileNumber: String,
        building: Buildings,
        buildingType: BuildingTypes?,
        buildingUsage: BuildingUsages?,
        units: List<Units>,
        costs: List<Costs>,
        filesJsonArray: JSONArray,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val safeMobile = mobileNumber.trim()
        if (safeMobile.isEmpty()) {
            onError(IllegalArgumentException("mobileNumber is empty"))
            return
        }

        val buildingJson = mapper.buildingToJson(building, buildingType, buildingUsage)
        val unitsJsonArray = mapper.listToJsonArray(units, mapper::unitToJson)
        val costsJsonArray = mapper.listToJsonArray(costs, mapper::costToJson)

        // remove paymentDate from each cost object
        for (i in 0 until costsJsonArray.length()) {
            val costObj = costsJsonArray.optJSONObject(i)
            if (costObj != null && costObj.has("paymentDate")) {
                costObj.remove("paymentDate")
            }
        }

        val item = JSONObject().apply {
            put("building", buildingJson)
            put("mobileNumber", safeMobile)
            put("units", unitsJsonArray)
            put("costs", costsJsonArray)
            put("files", filesJsonArray)
            put("idempotencyKey", "imp-${UUID.randomUUID()}")
        }

        val payload = JSONArray().put(item)

        val req = JsonObjectArrayBodyRequest(
            method = Method.POST,
            url = baseUrl,
            arrayBody = payload,
            onSuccess = { resp -> onSuccess(resp.toString()) },
            onVolleyError = { err -> onError(VolleyErrorMapper.toException("insertBuilding", err)) }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }


    fun fetchBuildingsForUser(
        mobileNumber: String,
        roleId: Long? = null,
        page: Int = 1,
        limit: Int = 50,
        onSuccess: (List<BuildingWithCounts>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val safeMobile = mobileNumber.trim()
        if (safeMobile.isEmpty()) {
            onError(IllegalArgumentException("mobileNumber is empty"))
            return
        }

        val safePage = page.coerceAtLeast(1)
        val safeLimit = limit.coerceIn(1, 200)

        val builder = "$baseUrl/by-user".toUri().buildUpon()
            .appendQueryParameter("mobileNumber", safeMobile)
            .appendQueryParameter("page", safePage.toString())
            .appendQueryParameter("limit", safeLimit.toString())

        if (roleId != null && roleId > 0) {
            builder.appendQueryParameter("roleId", roleId.toString())
        }

        val url = builder.build().toString()

        val req = JsonArrayRequest(
            Method.GET,
            url,
            null,
            { arr ->
                runCatching {
                    val out = ArrayList<BuildingWithCounts>(arr.length())
                    for (i in 0 until arr.length()) {
                        out.add(parser.parseBuildingWithCounts(arr.getJSONObject(i)))
                    }
                    out
                }.onSuccess(onSuccess)
                    .onFailure { onError(it.toException()) }
            },
            { err -> onError(VolleyErrorMapper.toException("fetchBuildingsForUser", err)) }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    suspend fun fetchBuildingFullSuspend(
        buildingId: Long,
        fiscalYear: String?
    ): BuildingFullDto = suspendCancellableCoroutine { cont ->
        val req = fetchBuildingFull(
            buildingId = buildingId,
            fiscalYear = fiscalYear,
            onSuccess = { dto -> if (cont.isActive) cont.resume(dto) },
            onError = { e -> if (cont.isActive) cont.resumeWithException(e) }
        )
        cont.invokeOnCancellation { req?.cancel() }
    }

    fun fetchBuildingFull(
        buildingId: Long,
        fiscalYear: String?,
        onSuccess: (BuildingFullDto) -> Unit,
        onError: (Exception) -> Unit
    ): Request<*>? {
        if (buildingId <= 0) {
            onError(IllegalArgumentException("buildingId is invalid"))
            return null
        }

        val url = "$baseUrl/$buildingId/full".toUri().buildUpon().apply {
            val fy = fiscalYear?.trim().orEmpty()
            if (fy.isNotEmpty()) appendQueryParameter("fiscalYear", fy)
        }.build().toString()

        val req = JsonObjectRequest(
            Method.GET,
            url,
            null,
            { root ->
                runCatching {
                    val building = parser.parseBuilding(root.getJSONObject("building"))
                    val buildingType = parser.parseBuildingType(root.optJSONObject("buildingType"))
                    val buildingUsage = parser.parseBuildingUsage(root.optJSONObject("buildingUsage"))
                    val cityComplex = parser.parseCityComplex(root.optJSONObject("cityComplex"))
                    val files = parser.parseFiles(root.optJSONArray("files") ?: JSONArray())

                    val units = parser.parseUnits(root.optJSONArray("units") ?: JSONArray())
                    val ownerUnits = parser.parseOwnerUnits(root.optJSONArray("ownerUnits") ?: JSONArray())
                    val tenantUnits = parser.parseTenantUnits(root.optJSONArray("tenantUnits") ?: JSONArray())
                    val costs = parser.parseCosts(root.optJSONArray("costs") ?: JSONArray())
                    val defaultChargeCosts = parser.parseCosts(root.optJSONArray("defaultChargeCosts") ?: JSONArray())
                    val chargeCostsForYear = parser.parseCosts(root.optJSONArray("chargeCostsForYear") ?: JSONArray())

                    BuildingFullDto(
                        building = building,
                        buildingType = buildingType,
                        buildingUsage = buildingUsage,
                        cityComplex = cityComplex,
                        files = files,
                        units = units,
                        users = emptyList(),
                        role = emptyList(),
                        ownerUnits = ownerUnits,
                        tenantUnits = tenantUnits,
                        costs = costs,
                        defaultChargeCosts = defaultChargeCosts,
                        chargeCostsForYear = chargeCostsForYear
                    )
                }.onSuccess(onSuccess)
                    .onFailure { onError(it.toException()) }
            },
            { err -> onError(VolleyErrorMapper.toException("fetchBuildingFull", err)) }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
        return req
    }

    fun clearTempBuildingTypesAndUsages(
        context: Context,
        onSuccess: (deletedTypes: Int, deletedUsages: Int) -> Unit,
        onError: (String) -> Unit
    ) {
        val request = object : JsonObjectRequest(
            Method.DELETE,
            "$baseUrl/clear-temp-building-types-usages",
            null,
            { response ->
                try {
                    val deleted = response.getJSONObject("deleted")
                    val types = deleted.getInt("buildingTypes")
                    val usages = deleted.getInt("buildingUsages")

                    onSuccess(types, usages)
                } catch (e: Exception) {
                    onError(e.message ?: "Parse error")
                }
            },
            { error ->
                onError(error.message ?: "Network error")
            }
        ) {}

        Volley.newRequestQueue(context).add(request)
    }

    fun updateBuilding(
        building: Buildings,
        selectedCostNames: List<String>,
        replaceCosts: Boolean,
        fileIds: List<Long>,
        onSuccess: (Buildings) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val id = building.buildingId
        if (id <= 0) {
            onError(IllegalArgumentException("buildingId is invalid"))
            return
        }

        val url = "$baseUrl/$id"

        val body = mapper.buildingToJson(
            b = building,
            t = null,
            u = null
        ).apply {
            put("costNames", JSONArray(selectedCostNames.distinct()))
            put("replaceCosts", replaceCosts)
            put("fileIds", JSONArray(fileIds.filter { it > 0 }.distinct()))
        }

        Log.d("body(UpdateBuilding)", body.toString())

        val req = JsonObjectRequest(
            Method.PUT,
            url,
            body,
            { obj ->
                runCatching { parser.parseBuilding(obj) }
                    .onSuccess(onSuccess)
                    .onFailure { onError(it.toException()) }
            },
            { err ->
                onError(VolleyErrorMapper.toException("updateBuilding", err))
            }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    fun deleteBuilding(
        buildingId: Long,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (buildingId <= 0) {
            onError(IllegalArgumentException("buildingId is invalid"))
            return
        }

        val url = "$baseUrl/$buildingId"
        val req = JsonObjectRequest(
            Method.DELETE,
            url,
            null,
            { onSuccess() },
            { err -> onError(VolleyErrorMapper.toException("deleteBuilding", err)) }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    fun sendBulk(
        data: List<Map<String, Any>>,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val body = JSONArray(data)
        val req = JsonArrayRequest(
            Method.POST,
            baseUrl,
            body,
            { onSuccess() },
            { onError(VolleyErrorMapper.toException("sendBulk", it)) }
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
