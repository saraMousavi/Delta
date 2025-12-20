package com.example.delta.volley


import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.entity.Role
import com.example.delta.enums.Roles
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class RoleApi {
    private val baseUrl = "http://217.144.107.231:3000/role"

    private fun formatError(tag: String, error: VolleyError): Exception {
        val resp = error.networkResponse
        if (resp != null) {
            val body = try {
                String(resp.data ?: ByteArray(0), Charsets.UTF_8)
            } catch (_: Exception) {
                String(resp.data ?: ByteArray(0))
            }
            Log.e(tag, "HTTP ${resp.statusCode}: $body")
            return Exception("HTTP ${resp.statusCode}: $body")
        } else {
            Log.e(tag, "No networkResponse: ${error.message}", error)
            return Exception(error.toString())
        }
    }

    private fun parseRoles(arr: JSONArray): List<Role> {
        val out = mutableListOf<Role>()
        for (i in 0 until arr.length()) {
            val o: JSONObject = arr.getJSONObject(i)
            val nameRaw = o.optString("roleName", "GUEST_INDEPENDENT_USER")
            val roleNameEnum = try {
                Roles.valueOf(nameRaw)
            } catch (e: Exception) {
                Roles.GUEST_INDEPENDENT_USER
            }
            out += Role(
                roleId = o.optLong("roleId", 0L),
                roleName = nameRaw,
                roleDescription = o.optString("roleDescription", "")
            )
        }
        return out
    }

    suspend fun getRolesSuspend(context: Context): List<Role> =
        suspendCancellableCoroutine { cont ->
            val queue = Volley.newRequestQueue(context)
            val req = JsonArrayRequest(
                Request.Method.GET,
                baseUrl,
                null,
                { arr ->
                    try {
                        val list = parseRoles(arr)
                        if (cont.isActive) cont.resume(list)
                    } catch (e: Exception) {
                        if (cont.isActive) cont.resumeWithException(e)
                    }
                },
                { err ->
                    val ex = formatError("RoleApi(getRoles)", err)
                    if (cont.isActive) cont.resumeWithException(ex)
                }
            )
            queue.add(req)
        }

    suspend fun createRoleSuspend(
        context: Context,
        name: String,
        description: String
    ): Role = suspendCancellableCoroutine { cont ->
        val url = baseUrl
        val queue = Volley.newRequestQueue(context)

        val body = JSONObject().apply {
            put("roleName", name)
            put("roleDescription", description)
        }

        val req = object : com.android.volley.toolbox.JsonObjectRequest(
            Method.POST,
            url,
            body,
            { resp ->
                try {
                    val obj = if (resp.has("role")) resp.getJSONObject("role") else resp

                    val roleId = obj.optLong("roleId", 0L)
                    val roleNameStr = obj.optString("roleName", name)
                    val roleDesc = obj.optString("roleDescription", description)

                    val enumRole = runCatching { Roles.valueOf(roleNameStr) }
                        .getOrElse { Roles.GUEST_INDEPENDENT_USER }

                    val role = Role(
                        roleId = roleId,
                        roleName = roleNameStr,
                        roleDescription = roleDesc
                    )
                    Log.d("role", role.toString())
                    if (cont.isActive) {
                        cont.resume(role, onCancellation = null)
                    }
                } catch (e: Exception) {
                    if (cont.isActive) {
                        cont.resumeWith(Result.failure(e))
                    }
                }
            },
            { err ->
                val ex = formatError("RoleApi(createRoleSuspend)", err)
                if (cont.isActive) {
                    cont.resumeWith(Result.failure(ex))
                }
            }
        ) {
            override fun getBodyContentType(): String =
                "application/json; charset=utf-8"
        }

        queue.add(req)
    }

}
