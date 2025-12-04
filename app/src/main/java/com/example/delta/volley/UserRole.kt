package com.example.delta.volley
import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.entity.Role
import com.example.delta.enums.Roles
import org.json.JSONArray
import org.json.JSONObject

class UserRole {

        private val BASE_URL = "http://217.144.107.231:3000/"

        fun getRolesByMobile(
            context: Context,
            mobile: String,
            onSuccess: (List<Role>) -> Unit,
            onError: (Throwable) -> Unit
        ) {
            val url = "${BASE_URL}userRoles/by-mobile/$mobile"
            Log.d("url", url.toString())
            val queue = Volley.newRequestQueue(context)
            val req = object : StringRequest(
                Request.Method.GET, url,
                { resp ->
                    try {
                        val arr = JSONArray(resp)
                        val roles = buildList {
                            for (i in 0 until arr.length()) {
                                val o: JSONObject = arr.getJSONObject(i)
                                val roleName = o.optString("roleName")
                                add(
                                    Role(
                                        roleId = o.optLong("roleId"),
                                        roleName = roleName,
                                        roleDescription = o.optString("roleDescription")
                                    )
                                )
                            }
                        }
                        onSuccess(roles)
                    } catch (e: Exception) {
                        onError(e)
                    }
                },
                { err -> onError(err) }
            ) {}
            queue.add(req)
        }
    }
