package com.example.delta.init

import android.content.Context
import androidx.core.content.edit
import com.example.delta.data.entity.Role
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.forEach

private const val PREFS_NAME = "user_prefs"
private const val KEY_IS_LOGGED_IN = "is_logged_in"
private const val KEY_USER_ID = "user_id"
private const val KEY_USER_MOBILE = "user_mobile"
private const val KEY_ROLES_JSON = "roles_json"

class Preference {
    fun getUserId(context: Context): Long {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getLong("user_id", -1L) // -1L or any invalid ID as default
    }

    fun getRoleId(context: Context): Long {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getLong("role_id", -1L) // -1L or any invalid ID as default
    }

    fun getDarkModeState(context: Context): Boolean {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("is_dark_mode", false) // -1L or any invalid ID as default
    }


    fun isFirstLogin(context: Context): Boolean {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("first_login", true)
    }

    fun setRoleId(context: Context, roleID: Long) {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit() { putLong("role_id", roleID) }
    }

    fun getUserMobile(context: Context): String? {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getString("user_mobile", "") // -1L or any invalid ID as default
    }

//    fun getCurrentRoleName(context: Context): String? {
//        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
//        return prefs.getStringSet(KEY_ROLES_JSON, "").forEach { roleId ->
//            if(roleId == getRoleId(context)){
//                ""
//            }
//
//        }
//    }




    fun saveLoginState(
        context: Context,
        isLoggedIn: Boolean,
        userId: Long,
        mobile: String,
        roles: List<Role>
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val rolesJson = JSONArray().apply {
            roles.forEach { r ->
                put(
                    JSONObject().apply {
                        put("role_id", r.roleId)
                        put("role_name", r.roleName)
                    }
                )
            }
        }.toString()

        prefs.edit {
            putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
            putLong(KEY_USER_ID, userId)
            putString(KEY_USER_MOBILE, mobile)
            putString(KEY_ROLES_JSON, rolesJson)
        }
    }



    fun getSavedRoles(context: Context): List<Role> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_ROLES_JSON, null) ?: return emptyList()

        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(
                        Role(
                            roleId = o.optLong("role_id", 0L),
                            roleName = o.optString("role_name", ""),
                            roleDescription = o.optString("role_description", "")
                        )
                    )
                }
            }.filter { it.roleId != 0L && it.roleName.isNotBlank() }
        }.getOrElse { emptyList() }
    }


}