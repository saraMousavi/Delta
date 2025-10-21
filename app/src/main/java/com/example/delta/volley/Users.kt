package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.entity.User
import com.google.gson.Gson
import org.json.JSONObject


class Users {
    fun fetchUsers(context: Context?, onSuccess: (List<User>) -> Unit, onError: (Exception) -> Unit) {
        val url = "http://217.144.107.231:3000/user"

        val queue = Volley.newRequestQueue(context)

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                // Parse JSON array and map to your User model
                val userList = mutableListOf<User>()
                for (i in 0 until response.length()) {
                    try {
                        val userJson = response.getJSONObject(i)
                        val user = User(
                            userId = userJson.getLong("userId"), // Map from backend JSON
                            mobileNumber = userJson.getString("mobileNumber"),
                            password = userJson.getString("password"), // Changed from password
                            roleId = userJson.getLong("roleId")
                        )
                        userList.add(user)
                    } catch (e: Exception) {
                        onError(e)  // Handle JSON parsing error
                        return@JsonArrayRequest
                    }
                }
                onSuccess(userList)
            },
            { error ->
                // Handle error
                onError(error)
            })

        queue.add(jsonArrayRequest)
    }

    fun insertUser(context: Context, userJson: JSONObject) {
        val url = "http://217.144.107.231:3000/user"  // Replace with your VPS IP and port

        val queue = Volley.newRequestQueue(context)

        Log.d("userJson", userJson.toString())

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, userJson,
            { response ->
                // Success: handle response
                Log.d("InsertUser", "User inserted: $response")
            },
            { error ->
                // Error: handle error
                Log.e("InsertUser", "Error: $error")
            }
        )

        queue.add(jsonObjectRequest)
    }
}