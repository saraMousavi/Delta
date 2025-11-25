package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.entity.Funds
import com.example.delta.data.entity.RoleAuthorizationObjectFieldCrossRef
import com.example.delta.enums.PermissionLevel
import org.json.JSONArray
import org.json.JSONObject
import kotlin.text.substringAfter

class AuthObjectFieldCross {
    private val baseUrl = "http://217.144.107.231:3000/authObjectFieldCross"

    fun insertAuthorizationObject(
        context: Context,
        authObjectFieldCrossJsonArray: JSONArray,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)

        // Build full JSON payload
        val payload = JSONObject().apply {
            put("authObjectFieldCrossJsonArray", authObjectFieldCrossJsonArray)
        }

        Log.d("AuthorizationObjectVolley", "Payload: $payload")

        val request = object : JsonObjectRequest(
            Method.POST, baseUrl, payload,
            { response ->
                Log.d("InsertAuthorizationObjectCrossServer", "AuthorizationObjectsCross inserted: $response")
                onSuccess(response.toString())
            },
            { error ->
                val ex = formatVolleyError("InsertAuthorizationObjectCrossServer", error)
                onError(ex)
            }
        ) {
            override fun getBodyContentType(): String =
                "application/json; charset=utf-8"
        }

        queue.add(request)
    }

    fun insertAuthorizationObject(
        context: Context,
        authObjectFieldCrossJsonObject: JSONObject,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)

        val request = object : JsonObjectRequest(
            Request.Method.POST,            // Use PATCH here if your backend expects it
            baseUrl,
            authObjectFieldCrossJsonObject,
            { response -> onSuccess(response.toString()) },
            { error ->
                onError(formatVolleyError("InsertAuthObjectServer", error)) }
        ) {
            override fun getBodyContentType() = "application/json; charset=utf-8"
        }

        queue.add(request)
    }

    fun authObjectFieldCrossToJson(authObjectFieldCross: RoleAuthorizationObjectFieldCrossRef): JSONObject {
        return JSONObject().apply {
            put("objectId", authObjectFieldCross.objectId)
            put("roleId", authObjectFieldCross.roleId)
            put("fieldId", authObjectFieldCross.fieldId)
            put("permissionLevel", authObjectFieldCross.permissionLevel)
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

    fun fetchByRole(
        context: Context,
        roleId: Long,
        objectId: Long? = null,
        onSuccess: (List<RoleAuthorizationObjectFieldCrossRef>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val sb = StringBuilder("$baseUrl?roleId=$roleId&limit=500")
        if (objectId != null) {
            sb.append("&objectId=$objectId")
        }
        val url = sb.toString()
        Log.d("AuthObjectFieldCross", "GET $url")

        val queue = Volley.newRequestQueue(context)
        val req = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { arr ->
                try {
                    val list = parseArray(arr)
                    onSuccess(list)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err ->
                onError(formatVolleyError("AuthObjectFieldCross(fetchByRole)", err))
            }
        )
        queue.add(req)
    }

    private fun parseArray(arr: JSONArray): List<RoleAuthorizationObjectFieldCrossRef> {
        val out = ArrayList<RoleAuthorizationObjectFieldCrossRef>(arr.length())
        for (i in 0 until arr.length()) {
            val o: JSONObject = arr.getJSONObject(i)
//            out += RoleAuthorizationObjectFieldCrossRef(
//                roleId = o.optLong("roleId"),
//                objectId = o.optLong("objectId"),
//                fieldId = o.optLong("fieldId"),
////                permissionLevel = PermissionLevel.valueOf(o.optString("permissionLevel", "READ"))
//            )
        }
        return out
    }

}