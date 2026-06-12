package com.example.delta.volley


import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.entity.Role
import com.example.delta.enums.Roles
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class RoleApi {
    private val baseUrl = "http://185.129.197.6:443/role"

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

    suspend fun getRolesSuspend(
        context: Context,
        buildingId: Long? = null
    ): List<Role> = suspendCancellableCoroutine { cont ->
        val queue = Volley.newRequestQueue(context)

        val url = if (buildingId != null) {
            "$baseUrl?buildingId=$buildingId"
        } else {
            baseUrl
        }

        val req = JsonArrayRequest(
            Request.Method.GET,
            url,
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

    fun createRole(
        context: Context,
        name: String,
        description: String,
        buildingId: Long? = null,
        onSuccess: (Role) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)

        val body = JSONObject().apply {
            put("roleName", name)
            put("roleDescription", description)
            if (buildingId != null) put("buildingId", buildingId)
        }

        val req = object : JsonObjectRequest(
            Method.POST,
            baseUrl,
            body,
            { resp ->
                try {
                    val roleId = resp.optLong("roleId", 0L)
                    val roleNameStr = resp.optString("roleName", name)
                    val roleDesc = resp.optString("roleDescription", description)

                    val role = Role(
                        roleId = roleId,
                        roleName = roleNameStr,
                        roleDescription = roleDesc,
                        buildingId = buildingId
                    )

                    onSuccess(role)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err ->
                val serverMsg = extractServerMessage(err)
                val ex = if (!serverMsg.isNullOrBlank()) {
                    Exception(serverMsg)
                } else {
                    formatError("RoleApi(createRole)", err)
                }
                onError(ex)
            }
        ) {
            override fun getBodyContentType(): String =
                "application/json; charset=utf-8"
        }

        queue.add(req)
    }


    private fun extractServerMessage(err: VolleyError): String? {
        val data = err.networkResponse?.data ?: return null
        return try {
            val body = String(data, Charsets.UTF_8)
            val json = JSONObject(body)
            json.optString("message").takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }



}
