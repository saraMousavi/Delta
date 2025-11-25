package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.entity.BuildingTypes
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import org.json.JSONObject

class BuildingType(
    private val baseUrl: String = "http://217.144.107.231:3000/buildingtype"
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

    fun fetchBuildingTypes(
        context: Context,
        onSuccess: (List<BuildingTypes>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)

        val request = JsonArrayRequest(
            Request.Method.GET,
            baseUrl,
            null,
            { response ->
                try {
                    val list = mutableListOf<BuildingTypes>()
                    for (i in 0 until response.length()) {
                        val obj = response.getJSONObject(i)
                        val item = BuildingTypes(
                            buildingTypeId = obj.optLong("buildingTypeId"),
                            buildingTypeName = obj.optString("name", "")
                        )
                        list += item
                    }
                    onSuccess(list)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { error -> onError(formatVolleyError("BuildingType(fetch)", error)) }
        )

        queue.add(request)
    }

    suspend fun fetchAllSuspend(
        context: Context
    ): List<BuildingTypes> = suspendCancellableCoroutine { cont ->
        fetchBuildingTypes(
            context = context,
            onSuccess = { list ->
                if (cont.isActive) cont.resume(list)
            },
            onError = { e ->
                if (cont.isActive) cont.resumeWithException(e)
            }
        )
    }

    fun createBuildingType(
        context: Context,
        name: String,
        onSuccess: (BuildingTypes) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)

        val body = JSONObject().apply {
            put("name", name)
        }

        val request = JsonObjectRequest(
            Request.Method.POST,
            baseUrl,
            body,
            { response ->
                try {
                    val id = when {
                        response.has("buildingTypeId") -> response.optLong("buildingTypeId")
                        response.has("BuildingTypeId") -> response.optLong("BuildingTypeId")
                        else -> 0L
                    }
                    val item = BuildingTypes(
                        buildingTypeId = id,
                        buildingTypeName = name
                    )
                    onSuccess(item)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { error -> onError(formatVolleyError("BuildingType(create)", error)) }
        )

        queue.add(request)
    }

    suspend fun createBuildingTypeSuspend(
        context: Context,
        name: String
    ): BuildingTypes? = suspendCancellableCoroutine { cont ->
        createBuildingType(
            context = context,
            name = name,
            onSuccess = { item ->
                if (cont.isActive) cont.resume(item)
            },
            onError = { e ->
                if (cont.isActive) cont.resumeWithException(e)
            }
        )
    }

    fun deleteBuildingType(
        context: Context,
        buildingTypeId: Long,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)
        val url = "$baseUrl/$buildingTypeId"

        val request = StringRequest(
            Request.Method.DELETE,
            url,
            { onSuccess() },
            { error -> onError(formatVolleyError("BuildingType(delete)", error)) }
        )

        queue.add(request)
    }

    suspend fun deleteBuildingTypeSuspend(
        context: Context,
        buildingTypeId: Long
    ): Unit = suspendCancellableCoroutine { cont ->
        deleteBuildingType(
            context = context,
            buildingTypeId = buildingTypeId,
            onSuccess = { if (cont.isActive) cont.resume(Unit) },
            onError = { e -> if (cont.isActive) cont.resumeWithException(e) }
        )
    }
}
