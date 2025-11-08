package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.entity.Role
import com.example.delta.data.entity.User
import com.example.delta.data.entity.UserRoleCrossRef
import com.example.delta.enums.Roles
import org.json.JSONArray
import org.json.JSONObject

class Users {

    private val BASE_URL = "http://217.144.107.231:3000"

    /** Optional: keep existing list fetch */
    fun fetchUsers(context: Context?, onSuccess: (List<User>) -> Unit, onError: (Exception) -> Unit) {
        val url = "$BASE_URL/user"
        val queue = Volley.newRequestQueue(context)

        val req = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                val out = ArrayList<User>(response.length())
                try {
                    for (i in 0 until response.length()) {
                        val o = response.getJSONObject(i)
                        out.add(
                            User(
                                userId = o.getLong("userId"),
                                mobileNumber = o.getString("mobileNumber"),
                                password = o.getString("password"),
                                roleId = o.getLong("roleId")
                            )
                        )
                    }
                    onSuccess(out)
                } catch (e: Exception) { onError(e) }
            },
            { error -> onError(Exception(error)) }
        )
        queue.add(req)
    }

    /** Insert user on server (if needed elsewhere) */
    fun insertUser(context: Context, userJson: JSONObject, onSuccess: () -> Unit = {}, onError: (Throwable) -> Unit = {}) {
        val url = "$BASE_URL/user"
        val queue = Volley.newRequestQueue(context)

        val req = JsonObjectRequest(
            Request.Method.POST, url, userJson,
            { onSuccess() },
            { error -> onError(error) }
        )
        queue.add(req)
    }

    /**
     * Login against server.
     * Prefer a real auth endpoint (POST /auth/login). If your backend exposes a different path,
     * adjust LOGIN_PATH accordingly.
     */
    private fun normalizeFa(s: String): String =
        s.trim()
            .replace('\u200c'.toString(), "")
            .replace('ي', 'ی')
            .replace('ك', 'ک')

    private fun roleDisplayToEnum(displayRaw: String): Roles? {
        val display = normalizeFa(displayRaw)
        val map = mapOf(
            normalizeFa("مدیر سیستم")   to Roles.ADMIN,
            normalizeFa("مدیر ساختمان") to Roles.BUILDING_MANAGER,
            normalizeFa("مالک")          to Roles.PROPERTY_OWNER,
            normalizeFa("ساکن")          to Roles.PROPERTY_TENANT,
            normalizeFa("کاربر مستقل")   to Roles.INDEPENDENT_USER
        )
        return map[display]
    }

    fun login(
        context: Context,
        mobileNumber: String,
        password: String,
        onSuccess: (ArrayList<Role>) -> Unit,
        onInvalid: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val url = "$BASE_URL/user/login?mobileNumber=$mobileNumber&password=$password"
        Log.d("url", url)
        val queue = Volley.newRequestQueue(context)

        val req = JsonObjectRequest(
            Request.Method.GET, url, null,
            { resp ->
                try {
                    if (!resp.has("user")) {
                        onInvalid(); return@JsonObjectRequest
                    }
                    val u = resp.getJSONObject("user")
                    val user = User(
                        userId = u.getLong("userId"),
                        mobileNumber = u.getString("mobileNumber"),
                        password = password,
                        roleId = u.optLong("roleId", 0L)
                    )
                    Log.d("resp", resp.toString())

                    val rolesJson: JSONArray = resp.optJSONArray("roles") ?: JSONArray()
                    val userRoles = ArrayList<Role>(rolesJson.length())

                    for (i in 0 until rolesJson.length()) {
                        val r: JSONObject = rolesJson.getJSONObject(i)

                        val roleId = r.optLong("roleId")
                        val roleKey = r.optString("roleKey", "")
                        val roleNameDisplay = r.optString("roleName", "")

                        val enumVal: Roles? =
//                            if (roleKey.isNotBlank()) {
//                                runCatching { Roles.valueOf(roleKey) }.getOrNull()
//                            } else {
                                roleDisplayToEnum(roleNameDisplay)
//                            }

                        if (enumVal == null) {
                            Log.w("login", "Unknown role display/key: '$roleNameDisplay'/'$roleKey' (id=$roleId)")
                            continue
                        }

                        userRoles.add(
                            Role(
                                roleId = roleId,
                                roleName = enumVal,
                                roleDescription = r.optString("roleDescription", "")
                            )
                        )
                    }

                    onSuccess(userRoles)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err ->
                val code = err.networkResponse?.statusCode
                if (code == 401 || code == 403 || code == 404) onInvalid() else onError(err)
            }
        )
        queue.add(req)
    }


    /**
     * Fallback login when no auth endpoint exists.
     * It downloads all users and matches client-side. Use only for testing.
     */
    fun loginFallbackByFetchingAll(
        context: Context,
        mobileNumber: String,
        password: String,
        onSuccess: (User) -> Unit,
        onInvalid: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        fetchUsers(context,
            onSuccess = { list ->
                val matched = list.firstOrNull { it.mobileNumber == mobileNumber && it.password == password }
                if (matched != null) onSuccess(matched) else onInvalid()
            },
            onError = { onError(it) }
        )
    }

    private fun roleDisplayToEnum(context: Context, display: String): Roles? {
        return enumValues<Roles>().firstOrNull { it.getDisplayName(context) == display.trim() }
    }

}