package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.entity.AuthorizationObject
import com.example.delta.data.entity.AuthorizationField
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.forEach

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


}