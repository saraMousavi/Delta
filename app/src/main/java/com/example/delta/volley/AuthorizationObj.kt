package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.entity.AuthorizationObject
import com.example.delta.data.entity.AuthorizationField
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.forEach
import kotlin.coroutines.resumeWithException

class AuthorizationObj {
    private val baseUrl = "http://217.144.107.231:3000/authorizationObject"


    fun insertAuthorizationObject(
        context: Context,
        authorizationObjectJsonArray: JSONArray,
        authorizationFieldJsonArray: JSONArray,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)

        // Build full JSON payload
        val payload = JSONObject().apply {
            put("authorizationObjectJsonArray", authorizationObjectJsonArray)
            put("authorizationFieldJsonArray", authorizationFieldJsonArray)
        }

        Log.d("AuthorizationObjectVolley", "Payload: $payload")

        val request = object : JsonObjectRequest(
            Method.POST, baseUrl, payload,
            { response ->
                Log.d("InsertAuthorizationObjectServer", "AuthorizationObjects inserted: $response")
                onSuccess(response.toString())
            },
            { error ->
                val ex = formatVolleyError("InsertAuthorizationObjectServer", error)
                onError(ex)
            }
        ) {
            override fun getBodyContentType(): String =
                "application/json; charset=utf-8"
        }

        queue.add(request)
    }

    fun authorizationObjectToJson(authorizationObject: AuthorizationObject): JSONObject {
        return JSONObject().apply {
//            put("authorizationObjectId", authorizationObject.authorizationObjectId)
            put("name", authorizationObject.name)
            put("description", authorizationObject.description)
        }
    }

    fun authorizationFieldToJson(authorizationFields: AuthorizationField, authorizationObjectTempId: String? = null): JSONObject {
        return JSONObject().apply {
            if (authorizationObjectTempId != null) put("authorizationObjectTempId", authorizationObjectTempId) else put("authorizationObjectId", authorizationFields.objectId)

            put("authorizationObjectId", authorizationFields.objectId)
            put("name", authorizationFields.name)
            put("fieldType", authorizationFields.fieldType)
        }
    }


    fun <T> listToJsonArray(list: List<T>, toJsonFunc: (T) -> JSONObject): JSONArray {
        val jsonArray = JSONArray()
        list.forEach { item ->
            jsonArray.put(toJsonFunc(item))
        }
        return jsonArray
    }

    private fun formatVolleyError(tag: String, error: VolleyError): Exception {
        val resp = error.networkResponse
        if (resp != null) {
            val status = resp.statusCode
            // سعی کن charset رو از هدر بخونی، وگرنه UTF-8
            val charset = resp.headers?.get("Content-Type")
                ?.substringAfter("charset=", "UTF-8")
                ?: "UTF-8"
            val body = try {
                String(resp.data ?: ByteArray(0), charset(charset))
            } catch (_: Exception) {
                String(resp.data ?: ByteArray(0))
            }

            Log.e(tag, "HTTP $status")
            Log.e(tag, "Headers: ${resp.headers}")
            Log.e(tag, "Body: $body")

            return Exception("HTTP $status: $body")
        } else {
            val klass = error::class.java.simpleName
            Log.e(tag, "No networkResponse ($klass): ${error.message}", error)
            return Exception("$klass: ${error.message ?: error.toString()}")
        }
    }

    suspend fun fetchAuthorizationObjectsSuspend(
        context: Context
    ): List<AuthorizationObject> = suspendCancellableCoroutine { cont ->
        val queue = Volley.newRequestQueue(context)
        val req = JsonArrayRequest(
            Request.Method.GET,
            baseUrl,
            null,
            { arr ->
                try {
                    val out = mutableListOf<AuthorizationObject>()
                    for (i in 0 until arr.length()) {
                        val o: JSONObject = arr.getJSONObject(i)
                        out += AuthorizationObject(
                            objectId = o.optLong("objectId", 0L),
                            name = o.optString("name", ""),
                            description = o.optString("description", "")
                        )
                    }
                    if (cont.isActive) cont.resume(out, onCancellation = null)
                } catch (e: Exception) {
                    if (cont.isActive) cont.resumeWithException(e)
                }
            },
            { err ->
                val ex = formatVolleyError("AuthorizationObj(fetchAuthorizationObjects)", err)
                if (cont.isActive) cont.resumeWithException(ex)
            }
        )
        queue.add(req)
    }

    suspend fun fetchFieldsForObjectSuspend(
        context: Context,
        objectId: Long
    ): List<AuthorizationField> = suspendCancellableCoroutine { cont ->
        val queue = Volley.newRequestQueue(context)
        val url = "$baseUrl/$objectId/fields"

        val req = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { arr ->
                try {
                    val out = mutableListOf<AuthorizationField>()
                    for (i in 0 until arr.length()) {
                        val o: JSONObject = arr.getJSONObject(i)
                        out += AuthorizationField(
                            fieldId = o.optLong("fieldId", 0L),
                            objectId = o.optLong("objectId", 0L),
                            name = o.optString("name", ""),
                            fieldType = o.optString("fieldType", null)
                        )
                    }
                    if (cont.isActive) cont.resume(out, onCancellation = null)
                } catch (e: Exception) {
                    if (cont.isActive) cont.resumeWithException(e)
                }
            },
            { err ->
                val ex = formatVolleyError("AuthorizationObj(fetchFieldsForObject)", err)
                if (cont.isActive) cont.resumeWithException(ex)
            }
        )

        queue.add(req)
    }

}