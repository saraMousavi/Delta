package com.example.delta.volley

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.delta.data.dao.OwnersDao
import com.example.delta.data.entity.BuildingTypes
import com.example.delta.data.entity.BuildingUsages
import com.example.delta.data.entity.BuildingWithTypesAndUsages
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Owners
import com.example.delta.data.entity.OwnersUnitsCrossRef
import com.example.delta.data.entity.Tenants
import com.example.delta.data.entity.TenantsUnitsCrossRef
import com.example.delta.data.entity.Units
import org.json.JSONArray
import org.json.JSONObject

class Building {
    private val baseUrl = "http://89.42.211.69:3000/building"

    fun insertBuilding(
        context: Context,
        buildingJson: JSONObject,
        unitsJsonArray: JSONArray,
        ownersJsonArray: JSONArray,
        tenantsJsonArray: JSONArray,
        costsJsonArray: JSONArray,
        ownerUnitsJsonArray: JSONArray,
        tenantUnitsJsonArray: JSONArray,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)

        // Build full JSON payload
        val payload = JSONObject().apply {
            put("building", buildingJson)
            put("units", unitsJsonArray)
            put("owners", ownersJsonArray)
            put("tenants", tenantsJsonArray)
            put("costs", costsJsonArray)
            put("ownerUnits", ownerUnitsJsonArray)
            put("tenantUnits", tenantUnitsJsonArray)
        }

        Log.d("BuildingVolley", "Payload: $payload")

        val request = JsonObjectRequest(
            Request.Method.POST, baseUrl, payload,
            { response -> onSuccess(response.toString()) },
            { error -> onError(Exception(error.message)) }
        )

        queue.add(request)
    }

    fun buildingToJson(building: Buildings, buildingTypes: BuildingTypes?, buildingUsages: BuildingUsages?): JSONObject {
        return JSONObject().apply {
            put("name", building.name)
            put("phone", building.phone)
            put("email", building.email)
            put("postCode", building.postCode)
            put("street", building.street)
            put("province", building.province)
            put("state", building.state)
            put("buildingTypeName", buildingTypes?.buildingTypeName ?: "") // add this
            put("buildingUsageName", buildingUsages?.buildingUsageName ?: "") // add this
            put("fund", building.fund)
            put("userId", building.userId)
            put("utilities", JSONArray(building.utilities))
        }
    }

    fun unitToJson(unit: Units): JSONObject {
        return JSONObject().apply {
            put("ownerId", unit.ownerId ?: JSONObject.NULL)
            put("unitId", unit.unitId)
            put("unitNumber", unit.unitNumber)
            put("area", unit.area)
            put("numberOfRooms", unit.numberOfRooms)
            put("numberOfParking", unit.numberOfParking)
        }
    }

    fun ownerToJson(owner: Owners): JSONObject {
        return JSONObject().apply {
            put("ownerId", owner.ownerId)
            put("firstName", owner.firstName)
            put("lastName", owner.lastName)
            put("phoneNumber", owner.phoneNumber)
            put("mobileNumber", owner.mobileNumber)
            put("birthday", owner.birthday)
            put("address", owner.address)
            put("email", owner.email)
        }
    }

    fun tenantToJson(tenant: Tenants): JSONObject {
        return JSONObject().apply {
            put("tenantId", tenant.tenantId)
            put("firstName", tenant.firstName)
            put("lastName", tenant.lastName)
            put("phoneNumber", tenant.phoneNumber)
            put("mobileNumber", tenant.mobileNumber)
            put("email", tenant.email)
            put("birthday", tenant.birthday)
            put("numberOfTenants", tenant.numberOfTenants)
            put("startDate", tenant.startDate)
            put("endDate", tenant.endDate)
            put("status", tenant.status)
        }
    }

    fun costToJson(cost: Costs): JSONObject {
        return JSONObject().apply {
            put("costName", cost.costName)
            put("tempAmount", cost.tempAmount)
            put("period", cost.period)
            put("calculateMethod", cost.calculateMethod)
            put("paymentLevel", cost.paymentLevel)
            put("responsible", cost.responsible)
            put("fundFlag", cost.fundFlag)
        }
    }

    fun <T> listToJsonArray(list: List<T>, toJsonFunc: (T) -> JSONObject): JSONArray {
        val jsonArray = JSONArray()
        list.forEach { item ->
            jsonArray.put(toJsonFunc(item))
        }
        return jsonArray
    }

    fun tenantUnitListToJsonArray(tenantUnitList: List<TenantsUnitsCrossRef>): JSONArray {
        val jsonArray = JSONArray()
        tenantUnitList.forEach { tu ->
            val jsonObject = JSONObject().apply {
                put("tenantId", tu.tenantId)
                put("unitId", tu.unitId)
                put("startDate", tu.startDate)
                put("endDate", tu.endDate)
                put("status", tu.status)
            }
            jsonArray.put(jsonObject)
        }
        return jsonArray
    }

    suspend fun buildOwnerUnitsJsonArray(
        ownersList: List<Owners>,
        ownerUnitMap: Map<Owners, List<OwnersUnitsCrossRef>>,
        ownersDao: OwnersDao
    ): JSONArray {
        val ownerUnitsJsonArray = JSONArray()

        for (owner in ownersList) {
            // Insert owner and get generated ownerId (assumed suspend function)
            val ownerId = ownersDao.insertOwners(owner)

            // Get owner-unit relations for this owner
            val ownerUnits = ownerUnitMap[owner] ?: emptyList()

            // Build JSON objects for each owner-unit relation
            ownerUnits.forEach { ownerUnit ->
                val jsonObject = JSONObject().apply {
                    put("ownerId", ownerId)  // Use generated ownerId
                    put("unitId", ownerUnit.unitId)
                    put("dang", ownerUnit.dang)
                }
                ownerUnitsJsonArray.put(jsonObject)
            }
        }

        return ownerUnitsJsonArray
    }


    /**
     * Fetches buildings from the server with their related type and usage information
     */
    fun fetchBuildings(
        context: Context?,
        phone: String? = null,
        userId: String? = null,
        onSuccess: (List<BuildingWithTypesAndUsages>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val baseUrl = "http://89.42.211.69:3000/building/filter/"
        val urlBuilder = StringBuilder(baseUrl)

        val queryParams = mutableListOf<String>()
        phone?.let { queryParams.add("phone=$it") }
        userId?.let { queryParams.add("userId=$it") }

        if (queryParams.isNotEmpty()) {
            urlBuilder.append("?").append(queryParams.joinToString("&"))
        }

        val url = urlBuilder.toString()

        if (context == null) {
            onError(Exception("Context is null"))
            return
        }

        val queue = Volley.newRequestQueue(context)

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val buildingsList = mutableListOf<BuildingWithTypesAndUsages>()

                    for (i in 0 until response.length()) {
                        val buildingJson = response.getJSONObject(i)

                        // Parse building data
                        val building = parseBuilding(buildingJson)

                        // Get building type and usage names
                        val buildingTypeJson = buildingJson.optJSONObject("buildingTypeId")
                        val buildingUsageJson = buildingJson.optJSONObject("buildingUsageId")

                        val buildingTypeName = buildingTypeJson?.optString("buildingTypeName") ?: "Unknown"
                        val buildingUsageName = buildingUsageJson?.optString("buildingUsageName") ?: "Unknown"

                        // Create the combined object
                        val buildingWithTypesAndUsages = BuildingWithTypesAndUsages(
                            building = building,
                            buildingTypeName = buildingTypeName,
                            buildingUsageName = buildingUsageName
                        )

                        buildingsList.add(buildingWithTypesAndUsages)
                    }

                    onSuccess(buildingsList)

                } catch (e: Exception) {
                    Log.e("FetchBuildings", "Error parsing buildings: ${e.message}")
                    onError(e)
                }
            },
            { error ->
                Log.e("FetchBuildings", "Network error: ${error.message}")
                onError(error)
            }
        )

        queue.add(jsonArrayRequest)
    }

    /**
     * Parses a JSON object into a Buildings object
     */
    private fun parseBuilding(json: JSONObject): Buildings {
        // Extract utilities as a List<String>
        val utilitiesArray = json.optJSONArray("utilities") ?: JSONArray()
        val utilities = mutableListOf<String>()
        for (i in 0 until utilitiesArray.length()) {
            utilities.add(utilitiesArray.optString(i, ""))
        }

        // Map MongoDB _id to buildingId (Long)
        val buildingId = try {
            json.optString("_id", "0").hashCode().toLong() // Convert string ID to a numeric hash
        } catch (e: Exception) {
            0L
        }

        return Buildings(
            buildingId = buildingId,
            name = json.optString("name", ""),
            phone = json.optString("phone", ""),
            email = json.optString("email", ""),
            postCode = json.optString("postCode", ""),
            street = json.optString("street", ""),
            province = json.optString("province", "Tehran"),
            state = json.optString("state", "Central"),
            // These will be null as we're getting the actual names separately
            buildingTypeId = null,
            buildingUsageId = null,
            fund = json.optDouble("fund", 0.0),
            userId = json.optLong("userId", 0),
            utilities = utilities
        )
    }








}
