package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.entity.UploadedFileEntity
import org.json.JSONObject

class BuildingFile(
    private val baseUrl: String = "http://217.144.107.231:3000/file"
) {

    private fun formatVolleyError(tag: String, error: com.android.volley.VolleyError): Exception {
        val resp = error.networkResponse
        if (resp != null) {
            val charsetName = resp.headers?.get("Content-Type")
                ?.substringAfter("charset=", "UTF-8") ?: "UTF-8"
            val body = try {
                String(resp.data ?: ByteArray(0), charset(charsetName))
            } catch (_: Exception) {
                String(resp.data ?: ByteArray(0))
            }
            Log.e(tag, "HTTP ${resp.statusCode}")
            Log.e(tag, "Headers: ${resp.headers}")
            Log.e(tag, "Body: $body")
            return Exception("HTTP ${resp.statusCode}: $body")
        } else {
            Log.e(tag, "No networkResponse: ${error.message}", error)
            return Exception(error.toString())
        }
    }

    fun createFile(
        context: Context,
        fileUrl: String,
        buildingId: Long? = null,
        onSuccess: (UploadedFileEntity) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)

        val body = JSONObject().apply {
            put("fileUrl", fileUrl)
            if (buildingId != null && buildingId > 0) {
                put("buildingId", buildingId)
            }
        }

        val req = JsonObjectRequest(
            Request.Method.POST,
            baseUrl,
            body,
            { resp ->
                try {
                    val fileId = resp.optLong("fileId")
                    val url = resp.optString("fileUrl", fileUrl)
                    onSuccess(
                        UploadedFileEntity(
                            fileId = fileId,
                            fileUrl = url
                        )
                    )
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err ->
                onError(formatVolleyError("BuildingFileApi(createFile)", err))
            }
        )

        queue.add(req)
    }

    fun fetchFilesForBuilding(
        context: Context,
        buildingId: Long,
        onSuccess: (List<UploadedFileEntity>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/by-building?buildingId=$buildingId"
        Log.d("BuildingFileApi", "GET $url")

        val queue = Volley.newRequestQueue(context)
        val req = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { arr ->
                try {
                    val out = mutableListOf<UploadedFileEntity>()
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        out += UploadedFileEntity(
                            fileId = o.optLong("fileId"),
                            fileUrl = o.optString("fileUrl", "")
                        )
                    }
                    onSuccess(out)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err ->
                onError(formatVolleyError("BuildingFileApi(fetchFilesForBuilding)", err))
            }
        )

        queue.add(req)
    }

    fun deleteFile(
        context: Context,
        fileId: Long,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/$fileId"
        val queue = Volley.newRequestQueue(context)

        val req = JsonObjectRequest(
            Request.Method.DELETE,
            url,
            null,
            { _ -> onSuccess() },
            { err -> onError(formatVolleyError("BuildingFileApi(deleteFile)", err)) }
        )

        queue.add(req)
    }
}
