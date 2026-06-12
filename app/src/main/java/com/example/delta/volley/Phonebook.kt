package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.entity.PhonebookEntry
import com.example.delta.data.entity.PhonebookRole
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class Phonebook {

    private val baseUrl = "http://185.129.197.6:443/phonebook"

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

    private fun parseRoles(arr: JSONArray?): List<PhonebookRole> {
        if (arr == null) return emptyList()
        val out = ArrayList<PhonebookRole>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            out += PhonebookRole(
                unitId = o.optLong("unitId", 0L),
                role = o.optString("role", ""),
                roleLabel = o.optString("roleLabel", ""),
                unitNumber = if (o.isNull("unitNumber")) null else o.optString("unitNumber", null)
            )
        }
        return out
    }

    fun parseEntries(arr: JSONArray): List<PhonebookEntry> {
        val list = mutableListOf<PhonebookEntry>()

        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)

            val type = o.optString("type", "resident")
            val roles = parseRoles(o.optJSONArray("roles"))

            val entry = PhonebookEntry(
                entryId = o.optLong("entryId", 0L),
                buildingId = o.optLong("buildingId", 0L),
                userId = if (o.isNull("userId")) null else o.optLong("userId"),
                name = o.optString("name", ""),
                phoneNumber = o.optString("phoneNumber", ""),
                type = type,
                isEmergency = o.optBoolean("isEmergency", false),
                unitId = if (roles.isNotEmpty()) null else if (o.isNull("unitId")) null else o.optLong("unitId"),
                roleLabel = if (roles.isNotEmpty()) null else o.optString("roleLabel", null),
                roles = roles
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
                    onSuccess(parseEntries(arr))
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
            onSuccess = { list -> if (cont.isActive) cont.resume(list) },
            onError = { e -> if (cont.isActive) cont.resumeWithException(e) }
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
                        entryId = entryObj.optLong("entryId", 0L),
                        buildingId = entryObj.optLong("buildingId", entry.buildingId),
                        userId = if (entryObj.isNull("userId")) null else entryObj.optLong("userId"),
                        name = entryObj.optString("name", entry.name),
                        phoneNumber = entryObj.optString("phoneNumber", entry.phoneNumber),
                        type = entryObj.optString("type", entry.type),
                        unitId = if (entryObj.isNull("unitId")) null else entryObj.optLong("unitId"),
                        isEmergency = entryObj.optBoolean("isEmergency", entry.type == "emergency"),
                        roleLabel = entryObj.optString("roleLabel", null),
                        roles = parseRoles(entryObj.optJSONArray("roles"))
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
            onSuccess = { created -> if (cont.isActive) cont.resume(created) },
            onError = { e -> if (cont.isActive) cont.resumeWithException(e) }
        )
    }

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
            onSuccess = { if (cont.isActive) cont.resume(Unit) },
            onError = { e -> if (cont.isActive) cont.resumeWithException(e) }
        )
    }

    fun updatePhonebookEntry(
        context: Context,
        entry: com.example.delta.data.entity.PhonebookEntry,
        onSuccess: (com.example.delta.data.entity.PhonebookEntry) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val entryId = entry.entryId
        val url = "$baseUrl/$entryId"
        val queue = Volley.newRequestQueue(context)

        val body = JSONObject().apply {
            put("name", entry.name)
            put("phoneNumber", entry.phoneNumber)
            put("type", entry.type)
            put("buildingId", entry.buildingId)
        }

        val req = object : JsonObjectRequest(
            Method.PUT,
            url,
            body,
            { resp ->
                try {
                    val obj = resp.optJSONObject("entry") ?: resp
                    val updated = com.example.delta.data.entity.PhonebookEntry(
                        entryId = obj.optLong("entryId", entry.entryId),
                        buildingId = obj.optLong("buildingId", entry.buildingId),
                        userId = if (obj.isNull("userId")) entry.userId else obj.optLong("userId"),
                        name = obj.optString("name", entry.name),
                        phoneNumber = obj.optString("phoneNumber", entry.phoneNumber),
                        type = obj.optString("type", entry.type),
                        isEmergency = obj.optBoolean("isEmergency", true),
                        unitId = if (obj.isNull("unitId")) entry.unitId else obj.optLong("unitId"),
                        roleLabel = obj.optString("roleLabel", entry.roleLabel),
                        roles = entry.roles
                    )
                    onSuccess(updated)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            { err ->
                onError(formatVolleyError("PhonebookApi(updatePhonebookEntry)", err))
            }
        ) {
            override fun getBodyContentType(): String = "application/json; charset=utf-8"
        }

        queue.add(req)
    }

    suspend fun updatePhonebookEntrySuspend(
        context: Context,
        entry: com.example.delta.data.entity.PhonebookEntry
    ): com.example.delta.data.entity.PhonebookEntry = suspendCancellableCoroutine { cont ->
        updatePhonebookEntry(
            context = context,
            entry = entry,
            onSuccess = { updated -> if (cont.isActive) cont.resume(updated, onCancellation = null) },
            onError = { e -> if (cont.isActive) cont.resumeWith(Result.failure(e)) }
        )
    }

}
