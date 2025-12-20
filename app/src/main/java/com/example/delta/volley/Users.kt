package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.entity.Role
import com.example.delta.data.entity.User
import com.example.delta.data.entity.UserRoleBuildingUnitCrossRef
import com.example.delta.enums.Gender
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.Charset

class Users {

    private val baseUrl = "http://217.144.107.231:3000"

    data class UserRoleBuilding(
        val user: User,
        val userRoles: List<UserRoleBuildingUnitCrossRef>
    )

    data class LoginUser(
        val userId: Long,
        val mobileNumber: String
    )

    data class LoginResult(
        val user: LoginUser,
        val roles: List<Role>
    )

    private fun VolleyError.bodyString(): String {
        val data = this.networkResponse?.data ?: return ""
        return try {
            String(data, Charset.forName(this.networkResponse?.headers?.get("Content-Type")?.let { "UTF-8" } ?: "UTF-8"))
        } catch (_: Exception) {
            String(data)
        }
    }

    private fun parseErrorMessage(raw: String): String {
        if (raw.isBlank()) return ""
        return try {
            val obj = JSONObject(raw)
            when {
                obj.has("message") -> obj.optString("message", "")
                obj.has("errmsg") -> obj.optString("errmsg", "")
                else -> raw
            }
        } catch (_: Exception) {
            raw
        }
    }

    fun changePassword(
        context: Context,
        mobileNumber: String,
        oldPassword: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onInvalidOldPassword: () -> Unit,
        onNotFound: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val url = "$baseUrl/user/change-password"
        val body = JSONObject().apply {
            put("mobileNumber", mobileNumber.trim())
            put("oldPassword", oldPassword)
            put("newPassword", newPassword)
        }

        val req = JsonObjectRequest(
            Request.Method.POST,
            url,
            body,
            { _ -> onSuccess() },
            { err ->
                val status = err.networkResponse?.statusCode
                val rawBody = err.bodyString()
                val msg = parseErrorMessage(rawBody).ifBlank { err.message.orEmpty() }

                when {
                    status == 404 -> onNotFound()
                    status == 401 -> onInvalidOldPassword()

                    msg.contains("Invalid old password", ignoreCase = true) ||
                            msg.contains("invalid old password", ignoreCase = true) -> onInvalidOldPassword()

                    msg.contains("User not found", ignoreCase = true) ||
                            msg.contains("not found", ignoreCase = true) -> onNotFound()

                    else -> onError(Exception(msg.ifBlank { "failed" }))
                }
            }
        )

        Volley.newRequestQueue(context).add(req)
    }


    fun sendForgetPassword(
        context: Context,
        mobileNumber: String,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val url = "$baseUrl/user/forget-password?mobileNumber=${mobileNumber.trim()}"

        val req = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { resp ->
                try {
                    val ok = resp.optBoolean("ok", false)
                    when {
                        ok -> onSuccess()
                        else -> onError(Exception("Unexpected response"))
                    }
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err ->
                onError(err)
            }
        )

        Volley.newRequestQueue(context).add(req)
    }



    fun insertUser(
        context: Context,
        userJson: JSONObject,
        onSuccess: (Long) -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        val url = "$baseUrl/user"
        val queue = Volley.newRequestQueue(context)

        val req = JsonObjectRequest(
            Request.Method.POST,
            url,
            userJson,
            { resp ->
                try {
                    val userId = resp.optLong("userId", 0L)
                    onSuccess(userId)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { error ->
                onError(error)
            }
        )

        queue.add(req)
    }


    fun checkMobileExists(
        context: Context,
        mobileNumber: String,
        onExists: (userId: Long?) -> Unit,
        onNotExists: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val url = "$baseUrl/user/exists?mobileNumber=${mobileNumber.trim()}"

        val queue = Volley.newRequestQueue(context)
        val req = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { resp ->
                try {
                    val exists = resp.optBoolean("exists", false)
                    val userId = if (resp.isNull("userId")) null else resp.optLong("userId")
                    if (exists) onExists(userId) else onNotExists()
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err ->
                onError(err)
            }
        )
        queue.add(req)
    }


    fun login(
        context: Context,
        mobileNumber: String,
        password: String,
        onSuccess: (LoginResult) -> Unit,
        onInvalidMobile: () -> Unit,
        onInvalidCredential: () -> Unit,
        onNotFound: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val url = "$baseUrl/user/login?mobileNumber=$mobileNumber&password=$password"
        val queue = Volley.newRequestQueue(context)

        val req = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { resp ->
                try {

                    val userJson = resp.getJSONObject("user")
                    val loginUser = LoginUser(
                        userId = userJson.optLong("userId", 0L),
                        mobileNumber = userJson.optString("mobileNumber", "")
                    )

                    val rolesJson: JSONArray = resp.optJSONArray("roles") ?: JSONArray()
                    val userRoles = ArrayList<Role>(rolesJson.length())

                    for (i in 0 until rolesJson.length()) {
                        val r: JSONObject = rolesJson.getJSONObject(i)

                        val roleId = r.optLong("roleId")
                        val roleNameRaw = r.optString("roleName", "")
                        val roleDescription = r.optString("roleDescription", "")

                        userRoles.add(
                            Role(
                                roleId = roleId,
                                roleName = roleNameRaw,
                                roleDescription = roleDescription.ifBlank { roleNameRaw }
                            )
                        )
                    }

                    val result = LoginResult(
                        user = loginUser,
                        roles = userRoles
                    )

                    onSuccess(result)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err ->
                val code = err.networkResponse?.statusCode
                if(code == 404){
                    onNotFound()
                } else if (code == 401){
                    onInvalidCredential()
                } else if (code == 400){
                    onInvalidMobile
                } else {
                    onError(err)
                }
            }
        )

        queue.add(req)
    }



    fun fetchUserById(
        context: Context,
        userId: Long,
        onSuccess: (User) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/user/$userId"
        val queue = Volley.newRequestQueue(context)

        val req = JsonObjectRequest(
            Request.Method.GET, url, null,
            { o ->
                try {
                    val user = User(
                        userId = o.optLong("userId", 0L),
                        mobileNumber = o.optString("mobileNumber", ""),
                        password = o.optString("password", ""),
                        firstName = o.optString("firstName", ""),
                        lastName = o.optString("lastName", ""),
                        email = o.optString("email", ""),
                        gender = runCatching {
                            Gender.valueOf(o.optString("gender", "UNKNOWN"))
                        }.getOrElse { Gender.FEMALE },
                        profilePhoto = o.optString("profilePhoto", ""),
                        nationalCode = o.optString("nationalCode", ""),
                        address = o.optString("address", ""),
                        phoneNumber = o.optString("phoneNumber", ""),
                        birthday = o.optString("birthday", "")
                    )
                    onSuccess(user)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err -> onError(Exception(err)) }
        )

        queue.add(req)
    }

    fun fetchUserRoleByMobile(
        context: Context,
        mobileNumber: String,
        onSuccess: (UserRoleBuilding) -> Unit,
        onNotFound: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/user/by-mobile?mobileNumber=${mobileNumber.trim()}"
        val queue = Volley.newRequestQueue(context)

        val req = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { obj ->
                try {
                    if (obj.has("notFound") && obj.optBoolean("notFound")) {
                        onNotFound()
                    } else {
                        val userObj = obj.optJSONObject("user") ?: obj

                        val user = User(
                            userId = userObj.optLong("userId", 0L),
                            mobileNumber = userObj.optString("mobileNumber", ""),
                            password = "",
                            firstName = userObj.optString("firstName", ""),
                            lastName = userObj.optString("lastName", ""),
                            email = userObj.optString("email", ""),
                            gender = runCatching {
                                Gender.valueOf(userObj.optString("gender", "UNKNOWN"))
                            }.getOrElse { Gender.FEMALE },
                            profilePhoto = userObj.optString("profilePhoto", ""),
                            nationalCode = userObj.optString("nationalCode", ""),
                            address = userObj.optString("address", ""),
                            phoneNumber = userObj.optString("phoneNumber", ""),
                            birthday = userObj.optString("birthday", "")
                        )

                        val rolesArr = obj.optJSONArray("roles") ?: JSONArray()
                        val roles = mutableListOf<UserRoleBuildingUnitCrossRef>()
                        for (i in 0 until rolesArr.length()) {
                            val r = rolesArr.getJSONObject(i)
                            roles += UserRoleBuildingUnitCrossRef(
                                roleId = r.optLong("roleId", 0L),
                                userId = r.optLong("userId", 0L),
                                buildingId = r.optLong("buildingId", 0L),
                                unitId = r.optLong("unitId", 0L)
                            )
                        }

                        onSuccess(UserRoleBuilding(user = user, userRoles = roles))
                    }
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err ->
                onError(formatVolleyError("Users(fetchUserRoleByMobile)", err))
            }
        )

        queue.add(req)
    }

    fun fetchUserByMobile(
        context: Context,
        mobileNumber: String,
        onSuccess: (User?) -> Unit,
        onNotFound: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/user/by-mobile?mobileNumber=${mobileNumber.trim()}"
        val queue = Volley.newRequestQueue(context)

        val req = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { obj ->
                try {
                    if (obj.has("notFound") && obj.optBoolean("notFound")) {
                        onNotFound()
                    } else {
                        val userObj = obj.optJSONObject("user") ?: obj

                        val user = User(
                            userId = userObj.optLong("userId", 0L),
                            mobileNumber = userObj.optString("mobileNumber", ""),
                            password = "",
                            firstName = userObj.optString("firstName", ""),
                            lastName = userObj.optString("lastName", ""),
                            email = userObj.optString("email", ""),
                            gender = runCatching {
                                Gender.valueOf(userObj.optString("gender", "UNKNOWN"))
                            }.getOrElse { Gender.FEMALE },
                            profilePhoto = userObj.optString("profilePhoto", ""),
                            nationalCode = userObj.optString("nationalCode", ""),
                            address = userObj.optString("address", ""),
                            phoneNumber = userObj.optString("phoneNumber", ""),
                            birthday = userObj.optString("birthday", "")
                        )
                        onSuccess(user)
                    }
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err ->
                onError(formatVolleyError("Users(fetchUserByMobile)", err))
            }
        )

        queue.add(req)
    }

    private fun formatVolleyError(
        tag: String,
        error: VolleyError
    ): Exception {
        val resp = error.networkResponse
        return if (resp != null) {
            val body = try {
                String(resp.data ?: ByteArray(0), Charsets.UTF_8)
            } catch (_: Exception) {
                String(resp.data ?: ByteArray(0))
            }
            Log.e(tag, "HTTP ${resp.statusCode}: $body")
            Exception("HTTP ${resp.statusCode}: $body")
        } else {
            Log.e(tag, "No networkResponse: ${error.message}", error)
            Exception(error.toString())
        }
    }

    fun updateUser(
        context: Context,
        userId: Long,
        payload: JSONObject,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/user/$userId"
        val queue = Volley.newRequestQueue(context)

        val req = JsonObjectRequest(
            Request.Method.PUT,
            url,
            payload,
            { _ -> onSuccess() },
            { err -> onError(Exception(err)) }
        )

        queue.add(req)
    }

    suspend fun addUserRoleSuspend(
        context: Context,
        userId: Long? = null,
        roleId: Long,
        buildingId: Long,
        unitId: Long = 0L,
        user: User? = null,
    ): Role {
        return suspendCancellableCoroutine { cont ->
            val url = "$baseUrl/user/assign-role"
            val queue = Volley.newRequestQueue(context)

            val body = JSONObject().apply {
                put("roleId", roleId)
                put("buildingId", buildingId)
                put("unitId", unitId)

                if (user != null) {
                    put("directRegistration", true)
                    put("mobileNumber", user.mobileNumber)
                    put("firstName", user.firstName ?: "")
                    put("lastName", user.lastName ?: "")
                    put("password", user.password ?: "123456")
                    put("gender", user.gender?.name ?: "FEMALE")
                    put("email", user.email ?: JSONObject.NULL)
                    put("phoneNumber", user.phoneNumber ?: JSONObject.NULL)
                    put("address", user.address ?: JSONObject.NULL)
                    put("birthday", user.birthday ?: JSONObject.NULL)
                    put("nationalCode", user.nationalCode ?: JSONObject.NULL)
                } else {
                    put("userId", userId ?: 0L)
                    put("directRegistration", false)
                }
            }

            val req = JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                { resp ->
                    try {
                        val obj = resp.getJSONObject("role")
                        val role = Role(
                            roleId = obj.optLong("roleId"),
                            roleName = obj.optString("roleName", ""),
                            roleDescription = obj.optString("roleDescription", "")
                        )
                        if (cont.isActive) cont.resume(role, null)
                    } catch (e: Exception) {
                        if (cont.isActive) cont.resumeWith(Result.failure(e))
                    }
                },
                { err ->
                    if (cont.isActive) cont.resumeWith(Result.failure(Exception(err)))
                }
            )

            queue.add(req)
        }
    }

}
