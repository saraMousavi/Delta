package com.example.delta.server


import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONArray
import org.json.JSONObject

    class JsonObjectArrayBodyRequest(
    method: Int,
    url: String,
    private val arrayBody: JSONArray,
    onSuccess: (JSONObject) -> Unit,
    onVolleyError: (com.android.volley.VolleyError) -> Unit
) : JsonObjectRequest(
    method,
    url,
    null,
    Response.Listener { onSuccess(it) },
    Response.ErrorListener { onVolleyError(it) }
) {
    override fun getBody(): ByteArray = arrayBody.toString().toByteArray(Charsets.UTF_8)

    override fun getBodyContentType(): String = "application/json; charset=utf-8"

    @Throws(AuthFailureError::class)
    override fun getHeaders(): MutableMap<String, String> =
        hashMapOf("Accept" to "application/json")
}
