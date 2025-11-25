package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.entity.CityComplexes
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import org.json.JSONObject

class CityComplex(
    private val baseUrl: String = "http://217.144.107.231:3000/citycomplex"
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

    fun fetchCityComplexes(
        context: Context,
        onSuccess: (List<CityComplexes>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)

        val request = JsonArrayRequest(
            Request.Method.GET,
            baseUrl,
            null,
            { response ->
                try {
                    val list = mutableListOf<CityComplexes>()
                    for (i in 0 until response.length()) {
                        val obj = response.getJSONObject(i)
                        val item = CityComplexes(
                            complexId = obj.optLong("complexId"),
                            name = obj.optString("name", ""),
                            address = if (obj.isNull("address")) null else obj.optString("address", null)
                        )
                        list += item
                    }
                    onSuccess(list)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { error -> onError(formatVolleyError("CityComplex(fetch)", error)) }
        )

        queue.add(request)
    }

    suspend fun fetchAllSuspend(
        context: Context
    ): List<CityComplexes> = suspendCancellableCoroutine { cont ->
        fetchCityComplexes(
            context = context,
            onSuccess = { list ->
                if (cont.isActive) cont.resume(list)
            },
            onError = { e ->
                if (cont.isActive) cont.resumeWithException(e)
            }
        )
    }

    fun createCityComplex(
        context: Context,
        name: String,
        address: String?,
        onSuccess: (CityComplexes?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)

        val body = JSONObject().apply {
            put("name", name)
            if (!address.isNullOrBlank()) {
                put("address", address)
            }
        }

        val request = JsonObjectRequest(
            Request.Method.POST,
            baseUrl,
            body,
            { response ->
                try {
                    val complexId = when {
                        response.has("complexId") -> response.optLong("complexId")
                        response.has("ComplexId") -> response.optLong("ComplexId")
                        else -> 0L
                    }

                    val item = CityComplexes(
                        complexId = complexId,
                        name = response.optString("name", name),
                        address = if (response.isNull("address")) {
                            address
                        } else {
                            response.optString("address", address)
                        }
                    )

                    onSuccess(item)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { error ->
                onError(formatVolleyError("CityComplex(create)", error))
            }
        )

        queue.add(request)
    }


    suspend fun createCityComplexSuspend(
                context: Context,
                name: String,
                address: String?
            ): CityComplexes? = suspendCancellableCoroutine { cont ->
                createCityComplex(
                    context = context,
                    name = name,
                    address = address,
                    onSuccess = { item ->
                        if (cont.isActive) cont.resume(item)
                    },
                    onError = { e ->
                        if (cont.isActive) cont.resumeWithException(e)
                    }
                )
            }
    }