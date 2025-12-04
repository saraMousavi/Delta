package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.dao.AuthorizationDao.FieldWithPermission
import com.example.delta.data.entity.AuthorizationField
import com.example.delta.data.entity.RoleAuthorizationObjectFieldCrossRef
import com.example.delta.enums.PermissionLevel
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.text.substringAfter

class AuthObjectFieldCross {
    private val baseUrl = "http://217.144.107.231:3000/authObjectFieldCross"

    suspend fun insertRoleAuthListSuspend(
        context: Context,
        list: List<RoleAuthorizationObjectFieldCrossRef>
    ) = suspendCancellableCoroutine { cont ->
        val queue = Volley.newRequestQueue(context)
        val payloadArray = listToJsonArray(list) { authObjectFieldCrossToJson(it) }

        val req = object : JsonArrayRequest(
            Method.POST,
            baseUrl,
            payloadArray,
            { _ ->
                if (cont.isActive) cont.resume(Unit)
            },
            { err ->
                val ex = formatVolleyError("AuthObjectFieldCross(insertRoleAuthList)", err)
                if (cont.isActive) cont.resumeWithException(ex)
            }
        ) {
            override fun getBodyContentType(): String =
                "application/json; charset=utf-8"
        }

        queue.add(req)
    }


    suspend fun deleteObjectForRoleSuspend(
        context: Context,
        roleId: Long,
        objectId: Long
    ) = suspendCancellableCoroutine { cont ->
        val queue = Volley.newRequestQueue(context)
        val url = "$baseUrl/by-object?roleId=$roleId&objectId=$objectId"

        val req = object : JsonObjectRequest(
            Method.DELETE,
            url,
            null,
            { _ ->
                if (cont.isActive) cont.resume(Unit)
            },
            { err ->
                val ex = formatVolleyError("AuthObjectFieldCross(deleteObjectForRole)", err)
                if (cont.isActive) cont.resumeWithException(ex)
            }
        ) {}

        queue.add(req)
    }

    suspend fun deleteSingleFieldSuspend(
        context: Context,
        roleId: Long,
        objectId: Long,
        fieldId: Long
    ) = suspendCancellableCoroutine { cont ->
        val queue = Volley.newRequestQueue(context)
        val url = "$baseUrl/by-field?roleId=$roleId&objectId=$objectId&fieldId=$fieldId"

        val req = object : JsonObjectRequest(
            Method.DELETE,
            url,
            null,
            { _ ->
                if (cont.isActive) cont.resume(Unit)
            },
            { err ->
                val ex = formatVolleyError("AuthObjectFieldCross(deleteSingleField)", err)
                if (cont.isActive) cont.resumeWithException(ex)
            }
        ) {}

        queue.add(req)
    }

    fun insertAuthorizationObject(
        context: Context,
        authObjectFieldCrossJsonArray: JSONArray,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)

        val payload = JSONObject().apply {
            put("items", authObjectFieldCrossJsonArray)
        }

        Log.d("AuthorizationObjectVolley", "Payload: $payload")

        val request = object : JsonObjectRequest(
            Method.POST, baseUrl, payload,
            { response ->
                Log.d(
                    "InsertAuthorizationObjectCrossServer",
                    "AuthorizationObjectsCross inserted: $response"
                )
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
            Request.Method.POST,
            baseUrl,
            authObjectFieldCrossJsonObject,
            { response -> onSuccess(response.toString()) },
            { error ->
                onError(formatVolleyError("InsertAuthObjectServer", error))
            }
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
            put("permissionLevel", authObjectFieldCross.permissionLevel.toString())
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

    private fun parseArray(arr: JSONArray): List<FieldWithPermission> {
        val out = ArrayList<FieldWithPermission>(arr.length())
        for (i in 0 until arr.length()) {
            val o: JSONObject = arr.getJSONObject(i)

            val perm = try {
                PermissionLevel.valueOf(
                    o.optString("cross_ref_permissionLevel", "READ")
                )
            } catch (e: Exception) {
                PermissionLevel.READ
            }

            val field = AuthorizationField(
                fieldId = o.optLong("fieldId"),
                objectId = o.optLong("objectId"),
                name = o.optString("name", ""),
                fieldType = o.optString("fieldType", "")
            )

            val cross = RoleAuthorizationObjectFieldCrossRef(
                roleId = o.optLong("cross_ref_roleId"),
                objectId = o.optLong("cross_ref_objectId"),
                fieldId = o.optLong("cross_ref_fieldId"),
                permissionLevel = perm
            )

            out += FieldWithPermission(
                field = field,
                crossRef = cross,
                objectName = o.optString("objectName", "")
            )
        }
        return out
    }

    fun fetchFieldsWithPermissionsForUser(
        context: Context,
        userId: Long,
        onSuccess: (List<FieldWithPermission>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/user-fields?userId=$userId"
        Log.d("AuthObjectFieldCross", "GET $url")

        val queue = Volley.newRequestQueue(context)
        val req = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { arr ->
                try {
                    val out = parseArray(arr)
                    onSuccess(out)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err ->
                onError(
                    formatVolleyError(
                        "AuthObjectFieldCross(fetchFieldsWithPermissionsForUser)",
                        err
                    )
                )
            }
        )
        queue.add(req)
    }

    fun fetchFieldsWithPermissionsForRole(
        context: Context,
        roleId: Long,
        onSuccess: (List<FieldWithPermission>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/role-fields?roleId=$roleId"
        Log.d("AuthObjectFieldCross", "GET $url")

        val queue = Volley.newRequestQueue(context)
        val req = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { arr ->
                try {
                    val out = parseArray(arr)
                    onSuccess(out)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err ->
                onError(
                    formatVolleyError(
                        "AuthObjectFieldCross(fetchFieldsWithPermissionsForRole)",
                        err
                    )
                )
            }
        )
        queue.add(req)
    }

    suspend fun fetchFieldsWithPermissionsForUserSuspend(
        context: Context,
        userId: Long
    ): List<FieldWithPermission> = suspendCancellableCoroutine { cont ->
        fetchFieldsWithPermissionsForUser(
            context = context,
            userId = userId,
            onSuccess = { list ->
                if (cont.isActive) {
                    cont.resume(list)
                }
            },
            onError = { e ->
                if (cont.isActive) {
                    cont.resumeWithException(e)
                }
            }
        )
    }

    suspend fun fetchFieldsWithPermissionsForRoleSuspend(
        context: Context,
        roleId: Long
    ): List<FieldWithPermission> = suspendCancellableCoroutine { cont ->
        fetchFieldsWithPermissionsForRole(
            context = context,
            roleId = roleId,
            onSuccess = { list ->
                if (cont.isActive) {
                    cont.resume(list)
                }
            },
            onError = { e ->
                if (cont.isActive) {
                    cont.resumeWithException(e)
                }
            }
        )
    }
}
