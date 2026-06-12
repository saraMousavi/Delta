// OwnerApi.kt
package com.example.delta.volley

import android.content.Context
import android.net.Uri
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.example.delta.data.entity.OwnersUnitsCrossRef
import com.example.delta.data.entity.User
import com.example.delta.init.AppRequestQueue
import com.example.delta.server.JsonMapper
import com.example.delta.server.JsonParser
import com.example.delta.server.VolleyErrorMapper
import com.example.delta.server.toException
import androidx.core.net.toUri

class Owner(
    appContext: Context,
    private val baseUrl: String = "http://185.129.197.6:443/owners",
    private val userRoleUrl: String = "http://185.129.197.6:443/userRole/update-role",
    private val queue: RequestQueue = AppRequestQueue.getInstance(appContext.applicationContext).requestQueue,
    private val mapper: JsonMapper = JsonMapper(),
    private val parser: JsonParser = JsonParser()
) {

    fun updateOwnerUnitsAndRole(
        buildingId: Long,
        userId: Long,
        units: List<OwnersUnitsCrossRef>,
        isManager: Boolean,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (buildingId <= 0L) {
            onError(IllegalArgumentException("buildingId is invalid"))
            return
        }
        if (userId <= 0L) {
            onError(IllegalArgumentException("userId is invalid"))
            return
        }

        val url = "$baseUrl/building/$buildingId/user/$userId/units-and-role"
        val safeUnits = units.filter { it.unitId > 0L }.distinctBy { it.unitId }
        val body = mapper.updateOwnerUnitsAndRoleBody(isManager, safeUnits)

        val req = JsonObjectRequest(
            Request.Method.PUT,
            url,
            body,
            { _ -> onSuccess() },
            { err -> onError(VolleyErrorMapper.toException("OwnerApi(updateOwnerUnitsAndRole)", err)) }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    fun insertOwnerWithUnits(
        buildingId: Long,
        user: User,
        units: List<OwnersUnitsCrossRef>,
        isManager: Boolean,
        onSuccess: (JsonParser.OwnerWithUnitsDto) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (buildingId <= 0L) {
            onError(IllegalArgumentException("buildingId is invalid"))
            return
        }
        if (user.mobileNumber.trim().isEmpty()) {
            onError(IllegalArgumentException("mobileNumber is empty"))
            return
        }

        val url = baseUrl
        val safeUnits = units.filter { it.unitId > 0L }.distinctBy { it.unitId }
        val body = mapper.ownerCreateBody(user = user, buildingId = buildingId, isManager = isManager, units = safeUnits)

        val req = JsonObjectRequest(
            Request.Method.POST,
            url,
            body,
            { resp ->
                runCatching { parser.parseOwnerWithUnits(resp) }
                    .onSuccess(onSuccess)
                    .onFailure { onError(it.toException()) }
            },
            { err -> onError(VolleyErrorMapper.toException("OwnerApi(insertOwnerWithUnits)", err)) }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    fun deleteOwner(
        ownerId: Long,
        buildingId: Long,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (ownerId <= 0L) {
            onError(IllegalArgumentException("ownerId is invalid"))
            return
        }
        if (buildingId <= 0L) {
            onError(IllegalArgumentException("buildingId is invalid"))
            return
        }

        val url = Uri.parse("$baseUrl/$ownerId")
            .buildUpon()
            .appendQueryParameter("buildingId", buildingId.toString())
            .build()
            .toString()

        val req = JsonObjectRequest(
            Request.Method.DELETE,
            url,
            null,
            { _ -> onSuccess() },
            { err -> onError(VolleyErrorMapper.toException("OwnerApi(deleteOwner)", err)) }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    fun getOwnerWithUnits(
        ownerId: Long,
        buildingId: Long,
        onSuccess: (JsonParser.OwnerWithUnitsDto) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (ownerId <= 0L) {
            onError(IllegalArgumentException("ownerId is invalid"))
            return
        }
        if (buildingId <= 0L) {
            onError(IllegalArgumentException("buildingId is invalid"))
            return
        }

        val url = Uri.parse("$baseUrl/$ownerId/with-units")
            .buildUpon()
            .appendQueryParameter("buildingId", buildingId.toString())
            .build()
            .toString()

        val req = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { obj ->
                runCatching { parser.parseOwnerWithUnits(obj) }
                    .onSuccess(onSuccess)
                    .onFailure { onError(it.toException()) }
            },
            { err -> onError(VolleyErrorMapper.toException("OwnerApi(getOwnerWithUnits)", err)) }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    fun hasOwnerForUnit(
        unitId: Long,
        onSuccess: (Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {


        val url = "$baseUrl/$unitId/for-units".toUri()
            .buildUpon()
            .build()
            .toString()

        val req = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { resp -> onSuccess(parser.parseHasReceived(resp, "hasOwner", false)) },
            { err -> onError(VolleyErrorMapper.toException("OwnerApi(getOwnerWithUnits", err)) }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    fun getOwnersWithUnitsByBuilding(
        buildingId: Long,
        onSuccess: (List<JsonParser.OwnerWithUnitsDto>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (buildingId <= 0L) {
            onError(IllegalArgumentException("buildingId is invalid"))
            return
        }

        val url = "$baseUrl/building/$buildingId/list"

        val req = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { arr ->
                runCatching { parser.parseOwnerWithUnitsList(arr) }
                    .onSuccess(onSuccess)
                    .onFailure { onError(it.toException()) }
            },
            { err -> onError(VolleyErrorMapper.toException("OwnerApi(getOwnersWithUnitsByBuilding)", err)) }
        ).apply { applyDefaultPolicy() }

        queue.add(req)
    }

    fun updateOwnerUnitsAndRoleVolley(
        buildingId: Long,
        userId: Long,
        units: List<OwnersUnitsCrossRef>,
        isManager: Boolean,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        updateOwnerUnitsAndRole(
            buildingId = buildingId,
            userId = userId,
            units = units,
            isManager = isManager,
            onSuccess = onSuccess,
            onError = onError
        )
    }


    fun updateOwnerRole(
        userId: Long,
        buildingId: Long,
        isManager: Boolean,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (userId <= 0L) {
            onError(IllegalArgumentException("userId is invalid"))
            return
        }
        if (buildingId <= 0L) {
            onError(IllegalArgumentException("buildingId is invalid"))
            return
        }

        val body = mapper.updateOwnerRoleBody(userId, buildingId, isManager)

        val req = JsonObjectRequest(
            Request.Method.PUT,
            userRoleUrl,
            body,
            { _ -> onSuccess() },
            { err -> onError(VolleyErrorMapper.toException("OwnerApi(updateOwnerRole)", err)) }
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
