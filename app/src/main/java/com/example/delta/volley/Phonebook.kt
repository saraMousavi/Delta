package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.entity.PhonebookEntry
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.suspendCancellableCoroutine

class Phonebook {

    private val baseUrl = "http://217.144.107.231:3000/phonebook"

    private fun formatVolleyError(
        tag: String,
        error: com.android.volley.VolleyError
    ): Exception {
        val resp = error.networkResponse
        return if (resp != null) {
            val body = try { String(resp.data ?: ByteArray(0), Charsets.UTF_8) }
            catch (_: Exception) { String(resp.data ?: ByteArray(0)) }
            Log.e(tag, "HTTP ${resp.statusCode}: $body")
            Exception("HTTP ${resp.statusCode}: $body")
        } else {
            Log.e(tag, "No networkResponse: ${error.message}", error)
            Exception(error.toString())
        }
    }

    fun parseEntries(arr: JSONArray): List<PhonebookEntry> {
        val list = mutableListOf<PhonebookEntry>()

        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)

            val entry = PhonebookEntry(
                entryId = 0,
                buildingId = o.optLong("buildingId"),
                name = o.optString("name", ""),
                phoneNumber = o.optString("phoneNumber", ""),
                type = o.optString("type", "resident"),
                unitId = if (o.isNull("unitId")) null else o.optLong("unitId"),
                isEmergency = o.optBoolean("isEmergency", false)
            )

            list.add(entry)
        }

        return list
    }


    fun getPhonebookForBuilding(
        context: Context,
        buildingId: Long,
        onSuccess: (List<PhonebookEntry>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/by-building?buildingId=$buildingId"
        Log.d("PhonebookApi", "GET $url")

        val queue = Volley.newRequestQueue(context)
        val req = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { resp ->
                try {
                    val arr = resp.optJSONArray("entries") ?: JSONArray()
                    val list = parseEntries(arr)
                    onSuccess(list)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err ->
                onError(formatVolleyError("PhonebookApi(getPhonebookForBuilding)", err))
            }
        )
        queue.add(req)
    }

    suspend fun getPhonebookForBuildingSuspend(
        context: Context,
        buildingId: Long
    ): List<PhonebookEntry> = suspendCancellableCoroutine { cont ->
        getPhonebookForBuilding(
            context = context,
            buildingId = buildingId,
            onSuccess = { list ->
                if (cont.isActive) cont.resume(list, onCancellation = null)
            },
            onError = { e ->
                if (cont.isActive) cont.resumeWith(Result.failure(e))
            }
        )
    }

    fun addPhonebookEntry(
        context: Context,
        entry: PhonebookEntry,
        onSuccess: (PhonebookEntry) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = baseUrl
        val queue = Volley.newRequestQueue(context)

        val body = JSONObject().apply {
            put("buildingId", entry.buildingId)
            put("name", entry.name)
            put("phoneNumber", entry.phoneNumber)
            put("type", entry.type)
            if (entry.unitId != null) put("unitId", entry.unitId)
        }

        Log.d("PhonebookApi", "POST $url body=$body")

        val req = object : JsonObjectRequest(
            Method.POST,
            url,
            body,
            { resp ->
                try {
                    val entryObj = resp.optJSONObject("entry") ?: body
                    val created = PhonebookEntry(
                        entryId    = entryObj.optLong("entryId", 0L),
                        buildingId = entryObj.optLong("buildingId", entry.buildingId),
                        name       = entryObj.optString("name", entry.name),
                        phoneNumber= entryObj.optString("phoneNumber", entry.phoneNumber),
                        type       = entryObj.optString("type", entry.type),
                        unitId     = if (entryObj.isNull("unitId")) null else entryObj.optLong("unitId"),
                        isEmergency= entryObj.optBoolean("isEmergency", entry.type == "emergency")
                    )
                    onSuccess(created)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err ->
                onError(formatVolleyError("PhonebookApi(addPhonebookEntry)", err))
            }
        ) {
            override fun getBodyContentType(): String = "application/json; charset=utf-8"
        }

        queue.add(req)
    }

    suspend fun addPhonebookEntrySuspend(
        context: Context,
        entry: PhonebookEntry
    ): PhonebookEntry = suspendCancellableCoroutine { cont ->
        addPhonebookEntry(
            context = context,
            entry = entry,
            onSuccess = { created ->
                if (cont.isActive) cont.resume(created, onCancellation = null)
            },
            onError = { e ->
                if (cont.isActive) cont.resumeWith(Result.failure(e))
            }
        )
    }

    // ---------- DELETE: حذف رکورد ----------
    fun deletePhonebookEntry(
        context: Context,
        entryId: Long,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/$entryId"
        Log.d("PhonebookApi", "DELETE $url")

        val queue = Volley.newRequestQueue(context)
        val req = object : JsonObjectRequest(
            Method.DELETE,
            url,
            null,
            { _ -> onSuccess() },
            { err -> onError(formatVolleyError("PhonebookApi(deletePhonebookEntry)", err)) }
        ) {
            override fun getBodyContentType(): String = "application/json; charset=utf-8"
        }

        queue.add(req)
    }

    suspend fun deletePhonebookEntrySuspend(
        context: Context,
        entryId: Long
    ) = suspendCancellableCoroutine { cont ->
        deletePhonebookEntry(
            context = context,
            entryId = entryId,
            onSuccess = {
                if (cont.isActive) cont.resume(Unit, onCancellation = null)
            },
            onError = { e ->
                if (cont.isActive) cont.resumeWith(Result.failure(e))
            }
        )
    }
}
