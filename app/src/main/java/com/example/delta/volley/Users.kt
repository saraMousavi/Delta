package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.entity.Role
import com.example.delta.data.entity.User
import com.example.delta.data.entity.UserRoleBuildingUnitCrossRef
import com.example.delta.enums.Gender
import com.example.delta.enums.Roles
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resumeWithException

class Users {

    private val BASE_URL = "http://217.144.107.231:3000"

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

    fun sendWelcomeSms(
        context: Context,
        phone: String,
        roleName: String,
        link: String,
        template: String
    ) {
        val url = "${BASE_URL}/sms/welcome"

        val payload = JSONObject().apply {
            put("phone", phone)
            put("roleName", roleName)
            put("link", link)
            put("template", template)
        }

        val req = JsonObjectRequest(
            Request.Method.POST,
            url,
            payload,
            { },
            { }
        )
        Volley.newRequestQueue(context).add(req)
    }


    fun insertUser(
        context: Context,
        userJson: JSONObject,
        onSuccess: (Long) -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        val url = "$BASE_URL/user"
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

    private fun normalizeFa(s: String): String =
        s.trim()
            .replace('\u200c'.toString(), "")
            .replace('ي', 'ی')
            .replace('ك', 'ک')

    private fun roleDisplayToEnum(displayRaw: String): Roles? {
        val display = normalizeFa(displayRaw)
        val map = mapOf(
            normalizeFa("مدیر سیستم") to Roles.ADMIN,
            normalizeFa("مدیر ساختمان") to Roles.BUILDING_MANAGER,
            normalizeFa("مالک") to Roles.PROPERTY_OWNER,
            normalizeFa("ساکن") to Roles.PROPERTY_TENANT,
            normalizeFa("کاربر مستقل") to Roles.INDEPENDENT_USER
        )
        return map[display]
    }

    fun login(
        context: Context,
        mobileNumber: String,
        password: String,
        onSuccess: (LoginResult) -> Unit,
        onInvalid: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val url = "$BASE_URL/user/login?mobileNumber=$mobileNumber&password=$password"
        Log.d("Users.login", url)
        val queue = Volley.newRequestQueue(context)

        val req = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { resp ->
                try {
                    if (!resp.has("user")) {
                        onInvalid()
                        return@JsonObjectRequest
                    }

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
                if (code == 401 || code == 403 || code == 404) {
                    onInvalid()
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
        val url = "$BASE_URL/user/$userId"
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
        val url = "$BASE_URL/user/by-mobile?mobileNumber=${mobileNumber.trim()}"
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
        val url = "$BASE_URL/user/by-mobile?mobileNumber=${mobileNumber.trim()}"
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
        error: com.android.volley.VolleyError
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
        val url = "$BASE_URL/user/$userId"
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
            val url = "$BASE_URL/user/assign-role"
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


    fun addUserRole(
        context: Context,
        userId: Long,
        roleId: Long,
        buildingId: Long,
        unitId: Long = 0L,
        onSuccess: (Role) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$BASE_URL/user/assign-role"
        val queue = Volley.newRequestQueue(context)

        val body = JSONObject().apply {
            put("userId", userId)
            put("roleId", roleId)
            put("buildingId", buildingId)
            put("unitId", unitId)
        }

        val req = JsonObjectRequest(
            Request.Method.POST,
            url,
            body,
            { resp ->
                try {
                    val r = resp.getJSONObject("role")
                    val role = Role(
                        roleId = r.optLong("roleId"),
                        roleName = r.optString("roleName", ""),
                        roleDescription = r.optString("roleDescription", "")
                    )
                    onSuccess(role)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err -> onError(Exception(err)) }
        )

        queue.add(req)
    }

}
