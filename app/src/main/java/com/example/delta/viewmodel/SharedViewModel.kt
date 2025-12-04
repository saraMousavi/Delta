package com.example.delta.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.graphics.pdf.PdfDocument
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.graphics.Paint
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.delta.data.dao.AuthorizationDao.FieldWithPermission
import com.example.delta.data.entity.AuthorizationObject
import com.example.delta.data.entity.BuildingTypes
import com.example.delta.data.entity.BuildingUsages
import com.example.delta.data.entity.BuildingWithCounts
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.ChatManagerDto
import com.example.delta.data.entity.ChatMessageDto
import com.example.delta.data.entity.ChatThreadDto
import com.example.delta.data.entity.ChatUiState
import com.example.delta.data.entity.CityComplexes
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Credits
import com.example.delta.data.entity.Debts
import com.example.delta.data.entity.Earnings
import com.example.delta.data.entity.Funds
import com.example.delta.data.entity.Notification
import com.example.delta.data.entity.PhonebookEntry
import com.example.delta.data.entity.Units
import com.example.delta.data.entity.UploadedFileEntity
import com.example.delta.data.entity.User
import com.example.delta.data.entity.UsersNotificationCrossRef
import com.example.delta.data.model.AppDatabase
import com.example.delta.enums.FundType
import com.example.delta.enums.NotificationType
import com.example.delta.enums.Period
import com.example.delta.enums.Roles
import com.example.delta.enums.UserType
import com.example.delta.enums.UserWithUnit
import com.example.delta.init.Preference
import com.example.delta.volley.AuthObjectFieldCross
import com.example.delta.volley.Building
import com.example.delta.volley.BuildingFile
import com.example.delta.volley.BuildingType
import com.example.delta.volley.BuildingUsage
import com.example.delta.volley.CapitalOwnerBreakdown
import com.example.delta.volley.ChartSummary
import com.example.delta.volley.Chats
import com.example.delta.volley.CityComplex
import com.example.delta.volley.Cost
import com.example.delta.volley.Debt
import com.example.delta.volley.Earning
import com.example.delta.volley.Fund
import com.example.delta.volley.NotificationWithCrossRef
import com.example.delta.volley.Phonebook
import com.example.delta.volley.Reports
import com.example.delta.volley.UnitBreakdown
import com.example.delta.volley.Users
import ir.hamsaa.persiandatepicker.util.PersianCalendar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min


class SharedViewModel(application: Application) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val context = getApplication<Application>().applicationContext
    val userId = Preference().getUserId(context)

    private val uploadedFileDao = AppDatabase.getDatabase(application).uploadedFileDao()

//    private val _invoiceResult = MutableSharedFlow<Boolean>() // true=success, false=failure
//    val invoiceResult = _invoiceResult.asSharedFlow()

    // State for Building Info Page
    var name by mutableStateOf("")
    var phone by mutableStateOf("")
    var email by mutableStateOf("")
    var postCode by mutableStateOf("")
    var street by mutableStateOf("")
    var province by mutableStateOf("")
    var state by mutableStateOf("")
    var serialNumber by mutableStateOf("")
    var floorCount by mutableStateOf("")
    var sameArea by mutableStateOf(false)
    var numberOfUnits by mutableStateOf("")
    var unitArea by mutableStateOf("")
    var savedFilePaths = mutableStateListOf<String>()
    var isLoading by mutableStateOf(false)


    var unitsAdded by mutableStateOf(false)
    var buildingTypeId by mutableIntStateOf(0)
    var buildingUsageId by mutableIntStateOf(0)
        private set
//    var costsList = mutableStateOf(listOf<Costs>())
    var chargeCostsList = mutableStateOf(listOf<Costs>())
    var unpaidDebtList = mutableStateOf(listOf<Debts>())
    var charge = mutableStateOf(listOf<Costs>())
    var unitsList = mutableStateListOf<Units>()
    var debtsList = mutableStateListOf<Debts>()
    var unitDebtsList = mutableStateListOf<Debts>()
    var fileList = mutableStateListOf<UploadedFileEntity>()

    // These represent the selected items from your dropdowns
    private val _selectedCredits = MutableStateFlow<Set<Long>>(emptySet())
    val selectedCredits: StateFlow<Set<Long>> = _selectedCredits
    private val _creditsForEarning = MutableStateFlow<List<Credits>>(emptyList())

    var selectedBuildingTypes by mutableStateOf<BuildingTypes?>(null)
    var selectedCityComplexes by mutableStateOf<CityComplexes?>(null)
    var selectedBuildingUsages by mutableStateOf<BuildingUsages?>(null)
    var selectedEarnings by mutableStateOf<Earnings?>(null)

    // Unit selection state
    var selectedUnits = mutableStateListOf<Units>()

    val tenantUnitMap = mutableMapOf<User, Units>()

    private val _isDarkModeEnabled = mutableStateOf(false)  // Compose MutableState
    var isDarkModeEnabled: Boolean
        get() = _isDarkModeEnabled.value
        set(value) {
            _isDarkModeEnabled.value = value
            saveDarkModeState(context, value)
        }

    var automaticCharge by mutableStateOf(false)

    // State to hold current balance of operational fund
    private val _operationalFundBalance = MutableStateFlow(0.0)
    val operationalFundBalance: StateFlow<Double> = _operationalFundBalance
    // State to hold current balance of capital fund
    private val _capitalFundBalance = MutableStateFlow(0.0)
    val capitalFundBalance: StateFlow<Double> = _capitalFundBalance

    var sameCosts by mutableStateOf(true)
    var currentRoleId by mutableLongStateOf(0L)

    var fixedAmount by mutableStateOf("")
        private set // Prevent external direct modification




    // Options Lists
    val periods = Period.entries


    init {
        viewModelScope.launch {
            val savedValue =
                Preference().getDarkModeState(context) // suspend fun reading from DataStore
            _isDarkModeEnabled.value = savedValue
        }
    }

    fun saveDarkModeState(context: Context, isDarkMode: Boolean) {
        Log.d("isDarkMode", isDarkMode.toString())
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit {
            putBoolean("is_dark_mode", isDarkMode)
        }
    }

    /**
     * Loads the current balances of operational and capital funds for the given building.
     * Updates the public states that UI observes.
     */
    fun loadFundBalances(context: Context, buildingId: Long) {
        viewModelScope.launch {
            Fund().getFundsForBuilding(
                context = context,
                buildingId = buildingId,
                onSuccess = { list ->
                    val opFund = list.firstOrNull { it.fundType == FundType.OPERATIONAL }
                    val capFund = list.firstOrNull { it.fundType == FundType.CAPITAL }

                    _operationalFundBalance.value = opFund?.balance ?: 0.0
                    _capitalFundBalance.value = capFund?.balance ?: 0.0
                },
                onError = {
                    _operationalFundBalance.value = 0.0
                    _capitalFundBalance.value = 0.0
                }
            )
        }
    }




    fun insertNewEarnings(
        context: Context,
        earning: Earnings,
        onSuccess: (Long) -> Unit = {},
        onConflict: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val id = Earning().insertNewEarningSuspend(context, earning)
                onSuccess(id)
            } catch (e: IllegalStateException) {
                if (e.message == "earnings-name-exists") {
                    onConflict()
                } else {
                    onError(e)
                }
            } catch (e: Exception) {
                onError(e)
            }
        }
    }



//    fun getAllAuthorizationObjects(): Flow<List<AuthorizationObject>> = flow {
//        val obj = authorizationDao.getAllAuthorizationObjects()
//        emit(obj)
//    }.flowOn(Dispatchers.IO)


    private val _buildingsForUser =
        MutableStateFlow<List<BuildingWithCounts>>(emptyList())
        fun getBuildingsForUser(userId: Long): StateFlow<List<BuildingWithCounts>> =
            _buildingsForUser

    private val _currentBuildingFull =
        MutableStateFlow<Building.BuildingFullDto?>(null)
    val currentBuildingFull: StateFlow<Building.BuildingFullDto?> =
        _currentBuildingFull

    private val _ownersForNotification =
        MutableStateFlow<List<UserWithUnit>>(emptyList())
    val ownersForNotification: StateFlow<List<UserWithUnit>> =
        _ownersForNotification

    private val _tenantsForNotification =
        MutableStateFlow<List<UserWithUnit>>(emptyList())
    val tenantsForNotification: StateFlow<List<UserWithUnit>> =
        _tenantsForNotification

    // TODO: userDao یا Preferences خودت رو بزار اینجا

    fun refreshBuildingsForUserFromServer(mobileNumber: String) {
        val ctx = getApplication<Application>()
        Building().fetchBuildingsForUser(
            context = ctx,
            mobileNumber = mobileNumber,
            onSuccess = { list ->
                _buildingsForUser.value = list
            },
            onError = { e ->
                Log.e("SharedViewModel", "refreshBuildingsForUserFromServer", e)
            }
        )
    }

    fun loadBuildingFullForNotifications(buildingId: Long) {
        val ctx = getApplication<Application>()
        Building().fetchBuildingFull(
            context = ctx,
            buildingId = buildingId,
            fiscalYear = null,
            onSuccess = { dto ->
                _currentBuildingFull.value = dto
                buildOwnersAndTenantsFromDto(dto)
            },
            onError = { e ->
                Log.e("SharedViewModel", "loadBuildingFullForNotifications", e)
                _currentBuildingFull.value = null
                _ownersForNotification.value = emptyList()
                _tenantsForNotification.value = emptyList()
            }
        )
    }

    private fun buildOwnersAndTenantsFromDto(dto: Building.BuildingFullDto) {
        val ownerRoles = dto.role.filter { it.roles == Roles.PROPERTY_OWNER }
        val tenantRoles = dto.role.filter { it.roles == Roles.PROPERTY_TENANT }

        val owners = ownerRoles
            .map { ur ->
                UserWithUnit(
                    id = ur.user.userId,
                    firstName = ur.user.firstName,
                    lastName = ur.user.lastName,
                    unitNumber = null,
                    userType = UserType.OWNER
                )
            }
            .distinctBy { it.id }

        val tenants = tenantRoles
            .map { ur ->
                UserWithUnit(
                    id = ur.user.userId,
                    firstName = ur.user.firstName,
                    lastName = ur.user.lastName,
                    unitNumber = null,
                    userType = UserType.TENANT
                )
            }
            .distinctBy { it.id }

        _ownersForNotification.value = owners
        _tenantsForNotification.value = tenants
    }

    fun checkCostNameExists(
        context: Context,
        buildingId: Long?,
        costName: String
    ): Flow<Boolean> = flow {
        val exists = Cost().costNameExistsSuspend(context, buildingId, costName)
        emit(exists)
    }.flowOn(Dispatchers.IO)




//    fun checkCostNameExists(buildingId: Long, costName: String): Flow<Boolean> = flow {
//        val costs = costsDao.costNameExists(buildingId, costName)
//        emit(costs)
//    }.flowOn(Dispatchers.IO)
//

    fun earningNameExists(
        context: Context,
        buildingId: Long?,
        earningName: String
    ): Flow<Boolean> = flow {
        val exists = Earning().earningNameExistsSuspend(context, buildingId, earningName)
        emit(exists)
    }.flowOn(Dispatchers.IO)



    fun getUsersNotificationsById(
        context: Context,
        userId: Long,
        notificationId: Long
    ): Flow<UsersNotificationCrossRef?> = flow {
        try {
            val list = com.example.delta.volley.Notification().fetchNotificationsForUserSuspend(
                context = context,
                userId = userId
            )
            val crossRef = list
                .firstOrNull { it.crossRef.notificationId == notificationId }
                ?.crossRef
            emit(crossRef)
        } catch (e: Exception) {
            emit(null)
        }
    }.flowOn(Dispatchers.IO)

    fun updateUserNotificationCrossRef(
        context: Context,
        notification: UsersNotificationCrossRef
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (notification.isRead) {
                    com.example.delta.volley.Notification().markNotificationReadSuspend(
                        context = context,
                        userId = notification.userId,
                        notificationId = notification.notificationId
                    )
                } else {
                    Log.w(
                        "NotificationUpdate",
                        "Marking notification as unread is not supported on server"
                    )
                }
            } catch (e: Exception) {
                Log.e("NotificationUpdate", e.message ?: "updateUserNotificationCrossRef error", e)
            }
        }
    }

//    fun getUsersNotificationsById(notificationId: Long): Flow<UsersNotificationCrossRef> = flow {
//        val notification = notificationDao.getUsersNotificationsById(notificationId)
//        emit(notification)
//    }.flowOn(Dispatchers.IO)
//
//    fun updateUserNotificationCrossRef(notification: UsersNotificationCrossRef) {
//        viewModelScope.launch(Dispatchers.IO) {
//            notificationDao.updateUserNotificationCrossRef(notification)
//        }
//    }



    private val fundHelper = Fund()

    private val _fundsForBuilding = MutableStateFlow<List<Funds>>(emptyList())
    val fundsForBuilding: StateFlow<List<Funds>> = _fundsForBuilding
    private val _pendingCostsForBuilding = MutableStateFlow<List<Costs>>(emptyList())
    val pendingCostsForBuilding: StateFlow<List<Costs>> = _pendingCostsForBuilding
    fun loadFundsForBuilding(context: Context, buildingId: Long) {
        fundHelper.getFundsAndCostsForBuilding(
            context = context,
            buildingId = buildingId,
            onSuccess = { funds, costs ->
                Log.d("funds", funds.toString())
                Log.d("costs", costs.toString())
                _fundsForBuilding.value = funds
                _pendingCostsForBuilding.value = costs
            },
            onError = { e ->
                Log.d("costs error", e.toString())
                _fundsForBuilding.value = emptyList()
                _pendingCostsForBuilding.value = emptyList()
            }
        )
    }

    private val _invoicedCostsForBuilding = MutableStateFlow<List<Costs>>(emptyList())
    val invoicedCostsForBuilding: StateFlow<List<Costs>> = _invoicedCostsForBuilding

    fun loadInvoicedCostsForBuilding(context: Context, buildingId: Long) {
        viewModelScope.launch {
            try {
                val full = Building().fetchBuildingFullSuspend(
                    context = context,
                    buildingId = buildingId,
                    fiscalYear = null
                )
                Log.d("full", full.toString())

                val invoiced = full.costs.filter { cost ->
                    cost.invoiceFlag == true
                }

                _invoicedCostsForBuilding.value = invoiced
            } catch (e: Exception) {
            }
        }
    }




//    fun getUserByMobile(mobile: String): Flow<User?> = flow {
//        emit(userDao.getUserByMobile(mobile))
//    }.flowOn(Dispatchers.IO)
//
//
//
//    fun getRoleByUserId(userId: Long): Flow<Role> = flow {
//        val user = userDao.getRoleByUserId(userId)
//        emit(user)
//    }.flowOn(Dispatchers.IO)
//
//    fun getRoles(): Flow<List<Role>> = flow {
//        val role = roleDao.getRoles()
//        emit(role)
//    }.flowOn(Dispatchers.IO)
//
//
//    fun getUserWithRoleByMobile(mobileNumber: String): Flow<Role?> = flow {
//        val role = roleDao.getRoleNameByMobileNumber(mobileNumber = mobileNumber)
//        emit(role)
//    }.flowOn(Dispatchers.IO)
//
//
//
//

    fun getFixedEarnings(context: Context): Flow<List<Earnings>> =
        flow {
            val list = Earning().getAllMenuEarningsSuspend(context)
            emit(list)
        }.flowOn(Dispatchers.IO)



    private val _debtsForCost = MutableStateFlow<List<Debts>>(emptyList())
    val debtsForCost: StateFlow<List<Debts>> = _debtsForCost


    fun loadDebtsForCost(context: Context, costId: Long) {
        viewModelScope.launch {
            try {
                val list = Debt().getDebtsForCostSuspend(
                    context = context,
                    costId = costId
                )
                _debtsForCost.value = list
            } catch (e: Exception) {
                // TODO: error handling / log
                _debtsForCost.value = emptyList()
            }
        }
    }



    // SharedViewModel (or wherever you keep this)
    fun getAuthorizationDetailsForUser(
        context: Context,
        userId: Long
    ): Flow<List<FieldWithPermission>> = flow {
        val api = AuthObjectFieldCross()
        val list = api.fetchFieldsWithPermissionsForUserSuspend(context, userId)
        emit(list)
    }.flowOn(Dispatchers.IO)


    fun appendGlobalCost(cost: Costs) {
        _costsList.value = _costsList.value + cost
    }



    fun insertUser(
        context: Context,
        user: User,
        directRegistration : Boolean = false,
        onSuccess: (Long) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {

                val userJson = JSONObject().apply {
                    put("mobileNumber", user.mobileNumber)
                    put("firstName", user.firstName)
                    put("lastName", user.lastName)
                    put("email", user.email)
                    put("address", user.address)
                    put("directRegistration", directRegistration)
                    put("password", user.password)
                }

                Users().insertUser(
                    context,
                    userJson,
                    onSuccess = { userId ->
                        onSuccess(userId)
                    },
                    onError = { e ->
                        onError(e.toString())
                    }
                )
            } catch (e: Exception) {
                onError(e.message.orEmpty())
            }
        }
    }


    fun isMonthInTenantPeriod(
        fiscalYear: Int,
        month: Int,
        startDate: String?,
        endDate: String?
    ): Boolean {
        val start = parsePersianDate(startDate ?: "") ?: return false
        val end = parsePersianDate(endDate ?: "") ?: return false

        val currentYearMonth = Pair(fiscalYear, month)

        fun yearMonthOf(date: PersianCalendar) = Pair(date.persianYear, date.persianMonth)

        val startYM = yearMonthOf(start)
        val endYM = yearMonthOf(end)

        fun isYearMonthGE(a: Pair<Int, Int>, b: Pair<Int, Int>): Boolean =
            a.first > b.first || (a.first == b.first && a.second > b.second)

        fun isYearMonthLE(a: Pair<Int, Int>, b: Pair<Int, Int>): Boolean =
            a.first < b.first || (a.first == b.first && a.second < b.second)

        return isYearMonthGE(currentYearMonth, startYM) && isYearMonthLE(currentYearMonth, endYM)
    }


    fun insertDebtForCapitalCost(
        context: Context,
        buildingId: Long,
        cost: Costs,
        amountPerOwner: Double,
        dueDate: String,
        description: String,
        onSuccess: (insertedCosts: List<Costs>, insertedDebts: List<Debts>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Cost().insertDebtForCapitalCostOnServer(
                    context = context,
                    buildingId = buildingId,
                    cost = cost,
                    amountPerOwner = amountPerOwner,
                    dueDate = dueDate,
                    description = description,
                    onSuccess = { c, d ->
                        viewModelScope.launch(Dispatchers.Main) {
                            onSuccess(c, d)
                        }
                    },
                    onError = { e ->
                        viewModelScope.launch(Dispatchers.Main) {
                            onError(e)
                        }
                    }
                )
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }


    fun insertCostToServer(
        context: Context,
        costs: List<Costs>,
        debts: List<Debts>,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val costsHelper = Cost()

        CoroutineScope(Dispatchers.IO).launch {
            // Convert all lists to JSONArray using the helper's toJson functions
            Log.d("debtsListSer", debtsList.toString())
            val debtsJsonArray =
                costsHelper.listToJsonArray(debts, costsHelper::debtToJson)

            // Convert building object to JSONObject
            val costJsonArray = costsHelper.listToJsonArray(
                costs, costsHelper::costToJson
            )

            // Call your Volley helper to insert the building and related data
            costsHelper.insertCost(
                context,
                costJsonArray,
                debtsJsonArray,
                onSuccess = { response ->
                    onSuccess(response)
                },
                onError = { error ->
                    Log.e("InsertCostServer", "Error: ${error.message}")
                    onError(error)
                }
            )
        }
    }
    val costApi = Cost()
    fun insertBuildingToServer(
        context: Context,
        userId: Long,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        Log.d("building userid", userId.toString())
        val building = Buildings(
            name = name,
            postCode = postCode,
            buildingTypeId = selectedBuildingTypes?.buildingTypeId ?: 0,
            buildingUsageId = selectedBuildingUsages?.buildingUsageId ?: 0,
            street = street,
            province = province,
            state = state,
            fund = 0.0,
            userId = userId,
            serialNumber = serialNumber,
            complexId = selectedCityComplexes?.complexId,
            floorCount = floorCount.toIntOrNull() ?: 0,
        )
        val buildingHelper = Building()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val unitsJsonArray =
                    buildingHelper.listToJsonArray(unitsList, buildingHelper::unitToJson)




                val selectedCosts = costsList.value.filter { it.tempAmount == 1.0 }
                val costsJsonArray =
                    costApi.listToJsonArray(selectedCosts, costApi::costToJson)
                Log.d("costsJsonArray", costsJsonArray.toString())
                val uploadedFilesLocal: List<UploadedFileEntity> = getAllUploadedFiles()
                val filesJsonArray = JSONArray().apply {
                    uploadedFilesLocal.forEach { f ->
                        put(
                            JSONObject().apply {
                                put("fileId", if (f.fileId == 0L) JSONObject.NULL else f.fileId)
                                put("fileUrl", f.fileUrl)
                            }
                        )
                    }
                }

                val mobileNumber = Preference().getUserMobile(context)
                val buildingJson = buildingHelper.buildingToJson(
                    mobileNumber ?: "",
                    building,
                    selectedBuildingTypes,
                    selectedBuildingUsages
                )
                Log.d("buildingJson", buildingJson.toString())
                buildingHelper.insertBuilding(
                    mobileNumber ?: "",
                    context,
                    buildingJson,
                    unitsJsonArray,
                    costsJsonArray = costsJsonArray,
                    filesJsonArray,          // NEW
                    onSuccess = { response ->
                        onSuccess(response)
                        resetFiles()
                    },
                    onError = { error ->
                        onError(error)
                    }
                )
            } catch (e: Exception) {
                onError(e)
            }
        }
    }




//    fun getFieldsForObject(objectId: Long): Flow<List<AuthorizationField>> = flow {
//        val auth = authorizationDao.getFieldsForObject(objectId)
//        emit(auth)
//    }.flowOn(Dispatchers.IO)






    fun updateDebtOnServer(context: Context, debt: Debts) {
        viewModelScope.launch {
            try {
                val updated = Debt().updateDebtSuspend(context, debt)
                unpaidDebtList.value = unpaidDebtList.value.map {
                    if (it.debtId == updated.debtId) updated else it
                }
            } catch (e: Exception) {
                Log.e("updateDebtOnServer", e.toString())
            }
        }
    }



    fun resetState() {
        name = ""
        phone = ""
        email = ""
        postCode = ""
        street = ""
        province = ""
        state = ""
        floorCount = ""
        serialNumber = ""
        fixedAmount = ""
        automaticCharge = false
        sameArea = false
        sameCosts = true
        buildingTypeId = 0
        buildingUsageId = 0
        unitDebtsList.clear()
        unitDebtsList = mutableStateListOf()
        charge.value = emptyList()
        selectedBuildingTypes = null
        selectedBuildingUsages = null
        unitsList.clear()
        fileList.clear()
        Log.d("debtsList.clear()", debtsList.toString())
        debtsList.clear()
    }


    // Parses a date string "yyyy/MM/dd" into PersianCalendar
    fun parsePersianDate(dateStr: String): PersianCalendar? {
        val parts = dateStr.split("/")
        if (parts.size != 3) return null
        val year = parts[0].toIntOrNull() ?: return null
        val month =
            parts[1].toIntOrNull()?.minus(1) ?: return null // zero-based month in PersianCalendar
        val day = parts[2].toIntOrNull() ?: return null

        return PersianCalendar().apply {
            setPersianDate(year, month, day)
        }
    }

    fun getNextYearSameDaySafe(inputDate: PersianCalendar?): PersianCalendar {
        val date = inputDate ?: PersianCalendar() // Use current date if null
        val newYear = date.persianYear + 1
        val newMonth = date.persianMonth// zero-based month

        // Temporary PersianCalendar for max days in the same month next year
        val temp = PersianCalendar().apply {
            setPersianDate(newYear, newMonth, 1)
        }
        val maxDay = temp.getMaxDaysInMonth()

        val result = PersianCalendar().apply {
            setPersianDate(
                newYear,
                newMonth,
                min(date.persianDay, maxDay)
            )
        }
        return result
    }

    fun PersianCalendar.getMaxDaysInMonth(): Int {
        val temp = PersianCalendar().apply {
            setPersianDate(
                this@getMaxDaysInMonth.persianYear,
                this@getMaxDaysInMonth.persianMonth,
                1
            )
        }

        // Find last day of month
        while (temp.persianMonth == this.persianMonth) {
            temp.addPersianDate(PersianCalendar.DAY_OF_MONTH, 1)
        }
        temp.addPersianDate(PersianCalendar.DAY_OF_MONTH, -1)

        return temp.persianDay
    }

    // Usage
    val calendar = PersianCalendar().apply {
        setPersianDate(1402, 5, 1) // Shahrivar (month 6)
    }
    private val _authObjects = MutableStateFlow(listOf<AuthorizationObject>())



    fun deleteFile(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                }
                // Remove from savedFilePaths list safely on main thread
                withContext(Dispatchers.Main) {
                    savedFilePaths.remove(path)
                }
            } catch (e: Exception) {
                Log.e("SharedViewModel", "Error deleting file", e)
            }
        }
    }



    private val _invoiceResult = MutableStateFlow<Boolean?>(null)
    val invoiceResult: StateFlow<Boolean?> = _invoiceResult

    fun invoiceCostIfEnoughFund(context: Context, cost: Costs) {
        viewModelScope.launch {
            val buildingId = cost.buildingId ?: return@launch
            val fundType = cost.fundType
            val costAmount = cost.tempAmount

            try {
                Fund().getFundsForBuilding(
                    context = context,
                    buildingId = buildingId,
                    onSuccess = { funds ->
                        val fund = funds.firstOrNull { it.fundType == fundType }
                        if (fund == null) {
                            _invoiceResult.value = false
                            return@getFundsForBuilding
                        }

                        if (fund.balance >= costAmount) {

                            Cost().markCostInvoicedOnServer(
                                context = context,
                                costId = cost.costId,
                                onSuccess = { ok ->
                                    if (!ok) {
                                        _invoiceResult.value = false
                                        return@markCostInvoicedOnServer
                                    }

                                    Fund().decreaseOperationalFundOnServer(
                                        context = context,
                                        buildingId = buildingId,
                                        amount = costAmount,
                                        fundType = fundType,
                                        onSuccess = { ok2 ->
                                            _invoiceResult.value = ok2
                                        },
                                        onError = {
                                            _invoiceResult.value = false
                                        }
                                    )
                                },
                                onError = {
                                    _invoiceResult.value = false
                                }
                            )
                        } else {
                            _invoiceResult.value = false
                        }
                    },
                    onError = {
                        _invoiceResult.value = false
                    }
                )
            } catch (_: Exception) {
                _invoiceResult.value = false
            }
        }
    }


//    fun getNotInvoicedEarnings(buildingId: Long): Flow<List<Earnings>> =
//        earningsDao.getNotInvoicedEarnings(buildingId).flowOn(Dispatchers.IO)

    val dashboardUnits = MutableStateFlow<List<Units>>(emptyList())
    val dashboardDebts = MutableStateFlow<List<Debts>>(emptyList())
    val dashboardCapitalSummary = MutableStateFlow<ChartSummary?>(null)
    val dashboardChargeSummary = MutableStateFlow<ChartSummary?>(null)
    val dashboardOperationalSummary = MutableStateFlow<ChartSummary?>(null)
    val dashboardCapitalDetailByOwner = MutableStateFlow<List<CapitalOwnerBreakdown>>(emptyList())
    val dashboardChargeDetailByUnit = MutableStateFlow<List<UnitBreakdown>>(emptyList())
    val dashboardOperationalDetailByUnit = MutableStateFlow<List<UnitBreakdown>>(emptyList())
    val dashboardPays = MutableStateFlow<List<Debts>>(emptyList())
    val dashboardCosts = MutableStateFlow<List<Costs>>(emptyList())
    val dashboardReceipt = MutableStateFlow<List<Credits>>(emptyList())
    val dashboardPendingReceipt = MutableStateFlow<List<Credits>>(emptyList())
    val dashboardCredits = MutableStateFlow<List<Credits>>(emptyList())


    fun loadDashboard(context: Context, buildingId: Long) {
        viewModelScope.launch {
            try {
                val resp = Reports().getDashboardDataSuspend(context, buildingId)

                dashboardUnits.value = resp.units
                dashboardDebts.value = resp.debtList
                dashboardCapitalSummary.value = resp.capitalSummary
                dashboardChargeSummary.value = resp.chargeSummary
                dashboardOperationalSummary.value = resp.operationalSummary
                dashboardCapitalDetailByOwner.value = resp.capitalDetailByOwner
                dashboardChargeDetailByUnit.value = resp.chargeDetailByUnit
                dashboardOperationalDetailByUnit.value = resp.operationalDetailByUnit
                dashboardPays.value = resp.paysList
                dashboardCosts.value = resp.costs
                dashboardReceipt.value = resp.receiptList
                dashboardPendingReceipt.value = resp.pendingReceipt
                dashboardCredits.value = resp.credits
            } catch (e: Exception) {
                Log.e("SharedViewModel", "loadDashboard failed: ${e.message}", e)
            }
        }
    }



    private val _notInvoicedEarnings =
        MutableStateFlow<List<Earnings>>(emptyList())
    val notInvoicedEarnings =
        _notInvoicedEarnings as StateFlow<List<Earnings>>

    fun loadNotInvoicedEarnings(context: Context, buildingId: Long) {
        viewModelScope.launch {
            try {
                val list = Earning().getNotInvoicedEarningsSuspend(
                    context = context,
                    buildingId = buildingId
                )
                _notInvoicedEarnings.value = list
            } catch (e: Exception) {
                _notInvoicedEarnings.value = emptyList()
            }
        }
    }

    fun getNotInvoicedEarnings(context: Context, buildingId: Long)
            : StateFlow<List<Earnings>> {
        loadNotInvoicedEarnings(context, buildingId)
        return notInvoicedEarnings
    }


    fun updateCredits(credits: List<Credits>) {
        _creditsForEarning.value = credits
    }

    fun toggleCreditSelection(creditId: Long) {
        val credit = _creditsForEarning.value.find { it.creditsId == creditId } ?: return
        if (credit.receiptFlag == true) return  // Don't toggle if already received

        val currentSelection = _selectedCredits.value.toMutableSet()
        if (currentSelection.contains(creditId)) {
            currentSelection.remove(creditId)
        } else {
            currentSelection.add(creditId)
        }
        _selectedCredits.value = currentSelection
    }


    val sumSelectedAmount = combine(_creditsForEarning, _selectedCredits) { credits, selectedIds ->
        credits.filter { selectedIds.contains(it.creditsId) }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    fun setSelectedCredits(ids: Set<Long>) {
        _selectedCredits.value = ids
    }

    // Update selected credits as receiptFlag=1 (received)
    fun markSelectedAsReceived(
        context: Context,
        earningId: Long,
        buildingId: Long,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            val selectedIds = _selectedCredits.value.toList()
            if (selectedIds.isEmpty()) return@launch

            val totalAmount = sumSelectedAmount.value

            try {
                val updatedCredits = Earning().markSelectedCreditsAsReceivedSuspend(
                    context = context,
                    earningId = earningId,
                    creditIds = selectedIds
                )

                Fund().increaseBalanceFundOnServer(
                    context = context,
                    buildingId = buildingId,
                    amount = totalAmount,
                    fundType = FundType.OPERATIONAL,
                    onSuccess = {
                        loadFundBalances(context, buildingId)
                    },
                    onError = {}
                )

                _creditsForEarning.value = updatedCredits
                _selectedCredits.value = emptySet()

                loadEarningDetail(context, earningId)

                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }




    suspend fun loadBuildingOverview(
        context: Context,
        buildingId: Long,
        fiscalYear: String? = ""
    ): Building.BuildingFullDto {
        return Building().fetchBuildingFullSuspend(
            context = context,
            buildingId = buildingId,
            fiscalYear = fiscalYear
        )
    }


    fun loadOverviewData(
        context: Context
    ) {
        viewModelScope.launch {
            try {
                val typeApi = BuildingType()
                val usageApi = BuildingUsage()
                val cityApi = CityComplex()
                val costApi = Cost()

                val t = async { typeApi.fetchAllSuspend(context) }
                val u = async { usageApi.fetchAllSuspend(context) }
                val c = async { cityApi.fetchAllSuspend(context) }
                val g = async { costApi.fetchGlobalCostsSuspend(context) }

                _buildingTypes.value = t.await()
                _buildingUsages.value = u.await()
                _cityComplexes.value = c.await()
                _costsList.value = g.await()
            } catch (e: Exception) {
                Log.e("SharedViewModel", "loadOverviewData", e)
            }
        }
    }




    private val phonebookApi = Phonebook()

    private val _phonebookEntriesForBuilding =
        MutableStateFlow<List<PhonebookEntry>>(emptyList())
    val phonebookEntriesForBuilding: StateFlow<List<PhonebookEntry>> =
        _phonebookEntriesForBuilding

    fun loadPhonebookForBuilding(context: Context, buildingId: Long) {
        viewModelScope.launch {
            try {
                val list = phonebookApi.getPhonebookForBuildingSuspend(
                    context = context,
                    buildingId = buildingId
                )
                _phonebookEntriesForBuilding.value = list
            } catch (e: Exception) {
                // TODO: error handling / log
            }
        }
    }

    suspend fun addPhonebookEntry(context: Context, entry: PhonebookEntry) {
        val created = phonebookApi.addPhonebookEntrySuspend(
            context = context,
            entry = entry
        )
        _phonebookEntriesForBuilding.update { old ->
            old + created
        }
    }

    suspend fun deletePhonebookEntry(context: Context, entry: PhonebookEntry) {
        phonebookApi.deletePhonebookEntrySuspend(
            context = context,
            entryId = entry.entryId
        )
        _phonebookEntriesForBuilding.update { old ->
            old.filterNot { it.entryId == entry.entryId }
        }
    }


    private val _earningDetail = MutableStateFlow<Earnings?>(null)
    val earningDetail = _earningDetail

    private val _earningCredits = MutableStateFlow<List<Credits>>(emptyList())
    val earningCredits = _earningCredits

    fun loadEarningDetail(context: Context, earningId: Long) {
        viewModelScope.launch {
            try {
                val result = Earning().getEarningByIdSuspend(context, earningId)
                _earningDetail.value = result.earning
                _earningCredits.value = result.credits
                updateCredits(result.credits) // your existing selection/sum logic
            } catch (ex: Exception) {
                Log.e("SharedViewModel", "loadEarningDetail failed", ex)
                _earningDetail.value = null
                _earningCredits.value = emptyList()
                updateCredits(emptyList())
            }
        }
    }

    private val notificationApi = com.example.delta.volley.Notification()

    private val _systemNotifications =
        MutableStateFlow<List<NotificationWithCrossRef>>(emptyList())
    val systemNotifications: StateFlow<List<NotificationWithCrossRef>> =
        _systemNotifications

    private val _managerNotifications =
        MutableStateFlow<List<NotificationWithCrossRef>>(emptyList())
    val managerNotifications: StateFlow<List<NotificationWithCrossRef>> =
        _managerNotifications




    fun sendNotificationToUsers(
        notification: Notification,
        targetUserIds: List<Long>,
        buildingId: Long?
    ) {
        viewModelScope.launch {
            val ctx = getApplication<android.app.Application>()
            val api = notificationApi

            try {
                val currentUserId = com.example.delta.init.Preference().getUserId(ctx)

                val result = api.createNotificationSuspend(
                    context = ctx,
                    title = notification.title,
                    message = notification.message,
                    type = notification.type.name,
                    createdByUserId = currentUserId.takeIf { it != 0L },
                    buildingId = buildingId,
                    targetUserIds = targetUserIds,
                    targetMobiles = null
                )

                if (result.notificationId != 0L) {
                    Log.d("result.notificationId", result.notificationId.toString())
                    api.sendNotificationPushSuspend(
                        context = ctx,
                        notificationId = result.notificationId
                    )
                }

                if (currentUserId != 0L) {
                    refreshNotificationsForUser(currentUserId)
                }
            } catch (e: Exception) {
                android.util.Log.e("SharedViewModel", "sendNotificationToUsers error", e)
            }
        }
    }



    private val _notificationsWithRead =
        MutableStateFlow<List<NotificationWithCrossRef>>(emptyList())
    val notificationsWithRead: StateFlow<List<NotificationWithCrossRef>> =
        _notificationsWithRead

    fun getNotificationsWithReadStatus(): StateFlow<List<NotificationWithCrossRef>> =
        notificationsWithRead

    fun refreshNotificationsForUser(userId: Long) {
        viewModelScope.launch {
            val ctx = getApplication<android.app.Application>()
            try {
                val list = notificationApi.fetchNotificationsForUserSuspend(
                    context = ctx,
                    userId = userId
                )
                _notificationsWithRead.value = list
                _managerNotifications.value =
                    list.filter { it.notification.type == NotificationType.MANAGER }
                _systemNotifications.value =
                    list.filter { it.notification.type == NotificationType.SYSTEM }
            } catch (e: Exception) {
                android.util.Log.e("SharedViewModel", "refreshNotificationsForUser error", e)
            }
        }
    }

    fun markNotificationReadForUser(userId: Long, notificationId: Long) {
        viewModelScope.launch {
            val ctx = getApplication<android.app.Application>()
            try {
                notificationApi.markNotificationReadSuspend(
                    context = ctx,
                    userId = userId,
                    notificationId = notificationId
                )

                val updated = _notificationsWithRead.value.map {
                    if (it.notification.notificationId == notificationId &&
                        it.crossRef.userId == userId
                    ) {
                        it.copy(crossRef = it.crossRef.copy(isRead = true))
                    } else it
                }
                _notificationsWithRead.value = updated
                _managerNotifications.value =
                    updated.filter { it.notification.type == NotificationType.MANAGER }
                _systemNotifications.value =
                    updated.filter { it.notification.type == NotificationType.SYSTEM }
            } catch (e: Exception) {
                android.util.Log.e("SharedViewModel", "markNotificationReadForUser error", e)
            }
        }
    }

    fun deleteUserNotificationCrossRef(userId: Long, notificationId: Long) {
        viewModelScope.launch {
            val ctx = getApplication<android.app.Application>()
            try {
                notificationApi.deleteNotificationForUser(
                    context = ctx,
                    userId = userId,
                    notificationId = notificationId,
                    onSuccess = {},
                    onError = { throw it }
                )

                val updated = _notificationsWithRead.value.filterNot {
                    it.notification.notificationId == notificationId &&
                            it.crossRef.userId == userId
                }
                _notificationsWithRead.value = updated
                _managerNotifications.value =
                    updated.filter { it.notification.type == NotificationType.MANAGER }
                _systemNotifications.value =
                    updated.filter { it.notification.type == NotificationType.SYSTEM }
            } catch (e: Exception) {
                android.util.Log.e("SharedViewModel", "deleteUserNotificationCrossRef error", e)
            }
        }
    }


    private val buildingApi = Building()
    private val buildingTypeApi = BuildingType()
    private val buildingUsageApi = BuildingUsage()
    private val cityComplexApi = CityComplex()
    private val buildingFileApi = BuildingFile()

    private val _currentBuilding = MutableStateFlow<Buildings?>(null)
    val currentBuilding: StateFlow<Buildings?> = _currentBuilding.asStateFlow()

    private val _loadingBuilding = MutableStateFlow(false)
    val loadingBuilding: StateFlow<Boolean> = _loadingBuilding.asStateFlow()

    private val _buildingError = MutableStateFlow<String?>(null)
    val buildingError: StateFlow<String?> = _buildingError.asStateFlow()

    private val _buildingTypes = MutableStateFlow<List<BuildingTypes>>(emptyList())
    val buildingTypes: StateFlow<List<BuildingTypes>> = _buildingTypes.asStateFlow()

    private val _buildingUsages = MutableStateFlow<List<BuildingUsages>>(emptyList())
    val buildingUsages: StateFlow<List<BuildingUsages>> = _buildingUsages.asStateFlow()

    private val _cityComplexes = MutableStateFlow<List<CityComplexes>>(emptyList())
    val cityComplexes: StateFlow<List<CityComplexes>> = _cityComplexes.asStateFlow()
    private val _costsList = MutableStateFlow<List<Costs>>(emptyList())
    val costsList: StateFlow<List<Costs>> = _costsList.asStateFlow()
    fun updateCosts(newList: List<Costs>) {
        _costsList.value = newList
    }

    private val _chargesCost = MutableStateFlow<List<Costs>>(emptyList())
    val chargesCost: StateFlow<List<Costs>> = _chargesCost.asStateFlow()

    private val _buildingFiles = MutableStateFlow<List<UploadedFileEntity>>(emptyList())
    val buildingFiles: StateFlow<List<UploadedFileEntity>> = _buildingFiles.asStateFlow()

    fun loadBuildingFromServer(
        context: Context,
        buildingId: Long,
        fiscalYear: String?
    ) {
        viewModelScope.launch {
            _loadingBuilding.value = true
            _buildingError.value = null
            try {
                val dto = buildingApi.fetchBuildingFullSuspend(
                    context = context,
                    buildingId = buildingId,
                    fiscalYear = fiscalYear
                )

                _currentBuilding.value = dto.building
                _chargesCost.value = dto.costs

                // Fill overview-related state from the same DTO
                dto.buildingType?.let { type ->
                    _buildingTypes.value = listOf(type)
                }

                dto.buildingUsage?.let { usage ->
                    _buildingUsages.value = listOf(usage)
                }

                dto.cityComplex?.let { complex ->
                    _cityComplexes.value = listOf(complex)
                }

                _buildingFiles.value = dto.files
            } catch (t: Throwable) {
                Log.e("SharedViewModel", "loadBuildingFromServer", t)
                _buildingError.value = t.message ?: "Unknown error"
            } finally {
                _loadingBuilding.value = false
            }
        }
    }


    fun insertCityComplexRemote(
        context: Context,
        name: String,
        address: String?,
        onInserted: (CityComplexes?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val created = cityComplexApi.createCityComplexSuspend(
                    context = context,
                    name = name,
                    address = address
                )
                if (created != null) {
                    _cityComplexes.value = _cityComplexes.value + created
                }
                onInserted(created)
            } catch (t: Throwable) {
                Log.e("SharedViewModel", "insertCityComplexRemote", t)
                onInserted(null)
            }
        }
    }


    fun insertBuildingTypeRemote(
        context: Context,
        name: String,
        onInserted: (BuildingTypes?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val created = buildingTypeApi.createBuildingTypeSuspend(
                    context = context,
                    name = name
                )
                if (created != null) {
                    _buildingTypes.value = _buildingTypes.value + created
                }
                onInserted(created)
            } catch (t: Throwable) {
                Log.e("SharedViewModel", "insertBuildingTypeRemote", t)
                onInserted(null)
            }
        }
    }

    fun insertBuildingUsageRemote(
        context: Context,
        name: String,
        onInserted: (BuildingUsages?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val created = buildingUsageApi.createBuildingUsageSuspend(
                    context = context,
                    name = name
                )
                if (created != null) {
                    _buildingUsages.value = _buildingUsages.value + created
                }
                onInserted(created)
            } catch (t: Throwable) {
                Log.e("SharedViewModel", "insertBuildingUsageRemote", t)
                onInserted(null)
            }
        }
    }

    val uploadedFiles = mutableStateListOf<UploadedFileEntity>()

    fun addFileList(file: UploadedFileEntity) {
        viewModelScope.launch {
            val id = withContext(Dispatchers.IO) {
                uploadedFileDao.insertUploadedFile(file)
            }
            val withId = file.copy(fileId = id)

            withContext(Dispatchers.Main) {
                uploadedFiles.add(withId)

                _buildingFiles.value = _buildingFiles.value
                    .filterNot { existing ->
                        existing.fileId == withId.fileId || existing.fileUrl == withId.fileUrl
                    } + withId
            }
        }
    }


    suspend fun getAllUploadedFiles(): List<UploadedFileEntity> {
        return uploadedFileDao.getAllUploadedFile()
    }

    fun resetFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            uploadedFileDao.clearAll()
            withContext(Dispatchers.Main) {
                uploadedFiles.clear()
            }
        }
    }
    fun exportDashboardToPdf(context: Context) {
        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint()
        paint.textSize = 14f

        canvas.drawText("Dashboard Report", 200f, 40f, paint)

        var y = 80f
        debtsList.groupBy { it.description }.forEach { (desc, list) ->
            canvas.drawText("$desc : ${list.sumOf { it.amount }}", 40f, y, paint)
            y += 28f
        }

        pdf.finishPage(page)

        val file = File(context.getExternalFilesDir(null), "dashboard.pdf")
        pdf.writeTo(FileOutputStream(file))
        pdf.close()
    }

    fun insertEarningsWithCredits(
        context: Context,
        earning: com.example.delta.data.entity.Earnings,
        onSuccess: (Long, Int) -> Unit,
        onConflict: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        Earning().insertEarningsWithCredits(
            context = context,
            earning = earning,
            onSuccess = onSuccess,
            onConflict = onConflict,
            onError = onError
        )
    }

    suspend fun insertEarningsWithCreditsSuspend(
        context: Context,
        earning: com.example.delta.data.entity.Earnings
    ): Pair<Long, Int> {
        return Earning().insertEarningsWithCreditsSuspend(context, earning)
    }

    private val _tenantCountsByUnitId = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val tenantCountsByUnitId: StateFlow<Map<Long, Int>> = _tenantCountsByUnitId

    fun preloadTenantCountsForBuilding(
        context: Context,
        buildingId: Long,
        fiscalYear: String?
    ) {
        viewModelScope.launch {
            try {
                val dto = Building().fetchBuildingFullSuspend(
                    context = context,
                    buildingId = buildingId,
                    fiscalYear = fiscalYear
                )

                val tenantsById = dto.tenantUnits.associateBy { it.tenantId }
                val tenantUnits = dto.tenantUnits

                val counts: Map<Long, Int> =
                    tenantUnits
                        .groupBy { it.unitId }
                        .mapValues { (_, list) ->
                            val sum = list.sumOf { tu ->
                                val t = tenantsById[tu.tenantId]
                                t?.numberOfTenants?.toIntOrNull() ?: 1
                            }
                            if (sum <= 0) 1 else sum
                        }

                _tenantCountsByUnitId.value = counts
            } catch (e: Exception) {
                _tenantCountsByUnitId.value = emptyMap()
            }
        }
    }

    fun getTenantCountForUnit(unitId: Long): Int {
        return tenantCountsByUnitId.value[unitId] ?: 1
    }


    //*********************Chat********************************
    private val chatApi = Chats()

    private val _chatManagers = MutableStateFlow<List<ChatManagerDto>>(emptyList())
    val chatManagers: StateFlow<List<ChatManagerDto>> = _chatManagers

    private val _chatThreads = MutableStateFlow<List<ChatThreadDto>>(emptyList())
    val chatThreads: StateFlow<List<ChatThreadDto>> = _chatThreads

    private val _currentChat = MutableStateFlow<ChatUiState?>(null)
    val currentChat: StateFlow<ChatUiState?> = _currentChat

    private val _currentThreadId = MutableStateFlow<Long?>(null)
    val currentThreadId: StateFlow<Long?> = _currentThreadId

    private val _chatUnreadCount = MutableStateFlow(0)
    val chatUnreadCount: StateFlow<Int> = _chatUnreadCount

    fun loadChatManagersForCurrentUser() {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val userId = Preference().getUserId(ctx)
            if (userId == 0L) return@launch
            try {
                val list = chatApi.fetchManagersForUserSuspend(ctx, userId)
                _chatManagers.value = list
            } catch (_: Exception) {
            }
        }
    }

    fun loadChatThreadsForCurrentUser(asManager: Boolean = false) {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val userId = Preference().getUserId(ctx)
            if (userId == 0L) return@launch
            try {
                val list = chatApi.fetchThreadsForUserSuspend(
                    context = ctx,
                    userId = userId,
                    asManager = asManager
                )
                _chatThreads.value = list
            } catch (_: Exception) {
                _chatThreads.value = emptyList()
            }
        }
    }

    fun openChatWithManager(
        context: Context,
        managerUserId: Long,
        managerName: String?,
        buildingId: Long?
    ) {
        viewModelScope.launch {
            try {
                val userId = Preference().getUserId(context)
                if (userId == 0L) return@launch

                _currentChat.value = ChatUiState(
                    thread = ChatThreadDto(
                        threadId = 0L,
                        buildingId = buildingId,
                        participants = listOf(userId, managerUserId),
                        lastMessageAt = null,
                        lastMessageText = null
                    ),
                    peerId = managerUserId,
                    peerName = managerName,
                    messages = emptyList(),
                    isLoading = true,
                    error = null
                )

                val result = chatApi.openThreadSuspend(
                    context = context,
                    userId = userId,
                    peerId = managerUserId,
                    buildingId = buildingId
                )

                _currentThreadId.value = result.thread.threadId

                _currentChat.value = ChatUiState(
                    thread = result.thread,
                    peerId = managerUserId,
                    peerName = managerName,
                    messages = result.messages.sortedBy { it.createdAt },
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _currentChat.value = _currentChat.value?.copy(
                    isLoading = false,
                    error = e.message ?: "Chat error"
                )
            }
        }
    }

    fun refreshMessagesForCurrentThread() {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val tid = _currentThreadId.value ?: return@launch
            try {
                val list = chatApi.fetchMessagesSuspend(
                    context = ctx,
                    threadId = tid,
                    sinceTs = null
                )
                _currentChat.value = _currentChat.value?.copy(
                    messages = list.sortedBy { it.createdAt }
                )
            } catch (_: Exception) {
            }
        }
    }

    fun startChatPolling(context: Context) {
        viewModelScope.launch {
            while (true) {
                val state = _currentChat.value ?: break
                val thread = state.thread ?: break
                val threadId = thread.threadId
                if (threadId == 0L) break

                val lastTs = state.messages.maxOfOrNull { it.createdAt } ?: 0L

                try {
                    val newMessages = chatApi.fetchMessagesSuspend(
                        context = context,
                        threadId = threadId,
                        sinceTs = lastTs
                    )
                    if (newMessages.isNotEmpty()) {
                        _currentChat.value = _currentChat.value?.let { current ->
                            current.copy(
                                messages = (current.messages + newMessages)
                                    .distinctBy { it.messageId }
                                    .sortedBy { it.createdAt }
                            )
                        }
                    }
                } catch (_: Exception) {
                }

                kotlinx.coroutines.delay(3000)
            }
        }
    }

    fun sendChatMessage(
        context: Context,
        text: String
    ) {
        val state = _currentChat.value ?: return
        val thread = state.thread ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            try {
                val userId = Preference().getUserId(context)
                if (userId == 0L) return@launch

                val msg = chatApi.sendMessageSuspend(
                    context = context,
                    threadId = thread.threadId,
                    senderId = userId,
                    text = text.trim()
                )

                _currentChat.value = _currentChat.value?.copy(
                    messages = (state.messages + msg).sortedBy { it.createdAt },
                    error = null
                )
            } catch (e: Exception) {
                _currentChat.value = _currentChat.value?.copy(
                    error = e.message ?: "Send failed"
                )
            }
        }
    }

    fun markCurrentThreadRead() {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val tid = _currentThreadId.value ?: return@launch
            val userId = Preference().getUserId(ctx)
            if (userId == 0L) return@launch
            try {
                chatApi.markThreadRead(ctx, tid, userId, {}, {})
                refreshUnreadCount()
            } catch (_: Exception) {
            }
        }
    }

    fun refreshUnreadCount() {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val userId = Preference().getUserId(ctx)
            if (userId == 0L) return@launch
            try {
                chatApi.getUnreadCount(
                    context = ctx,
                    userId = userId,
                    asManager = false,
                    onSuccess = { count -> _chatUnreadCount.value = count },
                    onError = { _chatUnreadCount.value = 0 }
                )
            } catch (_: Exception) {
            }
        }
    }

}
