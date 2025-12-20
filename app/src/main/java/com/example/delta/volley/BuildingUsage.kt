package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.entity.BuildingUsages
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import org.json.JSONObject

class BuildingUsage(
    private val baseUrl: String = "http://217.144.107.231:3000/buildingusage"
) {

    private fun formatVolleyError(tag: String, error: VolleyError): Exception {
        val resp = error.networkResponse
        return if (resp != null) {
            val charsetName = resp.headers?.get("Content-Type")
                ?.substringAfter("charset=", "UTF-8") ?: "UTF-8"
            val body = try {
                String(resp.data ?: ByteArray(0), charset(charsetName))
            } catch (_: Exception) {
                String(resp.data ?: ByteArray(0))
            }
            Log.e(tag, "HTTP ${resp.statusCode}")
            Log.e(tag, "Headers: ${resp.headers}")
            Log.e(tag, "Body: $body")
            Exception("HTTP ${resp.statusCode}: $body")
        } else {
            Log.e(tag, "No networkResponse: ${error.message}", error)
            Exception(error.toString())
        }
    }

    fun fetchBuildingUsages(
        context: Context,
        onSuccess: (List<BuildingUsages>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)

        val request = JsonArrayRequest(
            Request.Method.GET,
            baseUrl,
            null,
            { response ->
                try {
                    val list = mutableListOf<BuildingUsages>()
                    for (i in 0 until response.length()) {
                        val obj = response.getJSONObject(i)
                        val item = BuildingUsages(
                            buildingUsageId = obj.optLong("buildingUsageId"),
                            buildingUsageName = obj.optString("name", ""),
                            forBuildingId = obj.optLong("forBuildingId", 0L),
                            addedBeforeCreateBuilding = obj.optBoolean("addedBeforeCreateBuilding", false)
                        )
                        list += item
                    }
                    onSuccess(list)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { error -> onError(formatVolleyError("BuildingUsage(fetch)", error)) }
        )

        queue.add(request)
    }

    suspend fun fetchAllSuspend(
        context: Context
    ): List<BuildingUsages> = suspendCancellableCoroutine { cont ->
        fetchBuildingUsages(
            context = context,
            onSuccess = { list ->
                if (cont.isActive) cont.resume(list)
            },
            onError = { e ->
                if (cont.isActive) cont.resumeWithException(e)
            }
        )
    }

    fun createBuildingUsage(
        context: Context,
        buildingUsages: BuildingUsages,
        onSuccess: (BuildingUsages) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)

        val body = JSONObject().apply {
            put("name", buildingUsages.buildingUsageName)
            put("forBuildingId", buildingUsages.forBuildingId)
            put("addedBeforeCreateBuilding", buildingUsages.addedBeforeCreateBuilding)
        }
        Log.d("bodyBuildingUsage", body.toString())
        val request = JsonObjectRequest(
            Request.Method.POST,
            baseUrl,
            body,
            { response ->
                try {
                    val id = when {
                        response.has("buildingUsageId") -> response.optLong("buildingUsageId")
                        response.has("BuildingUsageId") -> response.optLong("BuildingUsageId")
                        else -> 0L
                    }
                    val item = BuildingUsages(
                        buildingUsageId = id,
                        buildingUsageName = buildingUsages.buildingUsageName,
                        addedBeforeCreateBuilding = buildingUsages.addedBeforeCreateBuilding,
                        forBuildingId = buildingUsages.forBuildingId
                    )
                    onSuccess(item)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { error -> onError(formatVolleyError("BuildingUsage(create)", error)) }
        )

        queue.add(request)
    }

    suspend fun createBuildingUsageSuspend(
        context: Context,
        buildingUsages: BuildingUsages
    ): BuildingUsages? = suspendCancellableCoroutine { cont ->
        createBuildingUsage(
            context = context,
            buildingUsages = buildingUsages,
            onSuccess = { item ->
                if (cont.isActive) cont.resume(item)
            },
            onError = { e ->
                if (cont.isActive) cont.resumeWithException(e)
            }
        )
    }

    fun deleteBuildingUsage(
        context: Context,
        buildingUsageId: Long,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)
        val url = "$baseUrl/$buildingUsageId"

        val request = StringRequest(
            Request.Method.DELETE,
            url,
            { onSuccess() },
            { error -> onError(formatVolleyError("BuildingUsage(delete)", error)) }
        )

        queue.add(request)
    }

    suspend fun deleteBuildingUsageSuspend(
        context: Context,
        buildingUsageId: Long
    ): Unit = suspendCancellableCoroutine { cont ->
        deleteBuildingUsage(
            context = context,
            buildingUsageId = buildingUsageId,
            onSuccess = { if (cont.isActive) cont.resume(Unit) },
            onError = { e -> if (cont.isActive) cont.resumeWithException(e) }
        )
    }
}
