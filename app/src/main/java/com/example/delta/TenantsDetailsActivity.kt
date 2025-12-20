package com.example.delta

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MobileFriendly
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.delta.data.entity.OwnerTabItem
import com.example.delta.data.entity.OwnerTabType
import com.example.delta.enums.FilterType
import com.example.delta.enums.FundType
import com.example.delta.viewmodel.SharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.example.delta.data.entity.Debts
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.TenantsUnitsCrossRef
import com.example.delta.data.entity.Units
import com.example.delta.data.entity.User
import com.example.delta.volley.Cost
import com.example.delta.volley.Fund
import com.example.delta.volley.Tenant
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.rememberLazyListState
import com.example.delta.init.NumberCommaTransformation
import androidx.compose.ui.platform.LocalFocusManager
import kotlin.collections.sortedBy
import kotlin.math.roundToLong

class TenantsDetailsActivity : ComponentActivity() {
    val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val unitId = intent.getLongExtra("UNIT_DATA", -1L)
        val tenantId = intent.getLongExtra("TENANT_DATA", -1L)
        val buildingId = intent.getLongExtra("BUILDING_ID", -1L)
        setContent {
            AppTheme (useDarkTheme = sharedViewModel.isDarkModeEnabled){
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    TenantDetailsScreen(
                        unitId = unitId,
                        tenantId = tenantId,
                        buildingId = buildingId,
                        sharedViewModel = sharedViewModel
                    )
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantDetailsScreen(
    unitId: Long,
    tenantId: Long,
    buildingId: Long,
    sharedViewModel: SharedViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    // --- NEW: load tenant name from server, not Room
    var tenantName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(unitId, tenantId) {
        Tenant().getTenantWithUnit(
            context = context,
            buildingId = buildingId,
            tenantId = tenantId,
            onSuccess = { t ->
                tenantName = "${t.user!!.firstName} ${t.user.lastName}"
            },
            onError = {
                tenantName = null
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = tenantName ?: context.getString(R.string.loading),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = context.getString(R.string.back)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            val tabs = listOf(
                OwnerTabItem(context.getString(R.string.overview), OwnerTabType.OVERVIEW),
                OwnerTabItem(context.getString(R.string.transaction), OwnerTabType.FINANCIALS)
            )
            var selectedTab by rememberSaveable { mutableIntStateOf(0) }

            OwnerSectionSelector(
                tabs = tabs,
                selectedIndex = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            when (tabs[selectedTab].type) {
                OwnerTabType.OVERVIEW -> TenantOverviewTab(
                    unitId = unitId,
                    tenantId = tenantId,
                    buildingId = buildingId,
                    sharedViewModel = sharedViewModel,
                    modifier = Modifier.fillMaxSize()
                )
                OwnerTabType.FINANCIALS -> TenantFinancialsTab(
                    unitId,
                    sharedViewModel = sharedViewModel,
                    tenantId = tenantId,
                    buildingId = buildingId,
                    snackBarHostState = snackBarHostState,
                    coroutineScope = coroutineScope,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

fun formatWithCommaTenant(input: String): String {
    if (input.isBlank()) return ""
    val clean = input.replace(",", "")
    return clean.toLongOrNull()
        ?.let { "%,d".format(it) }
        ?: input
}


@Composable
fun TenantOverviewTab(
    unitId: Long,
    tenantId: Long,
    buildingId: Long,
    sharedViewModel: SharedViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    val validation = remember { com.example.delta.init.Validation() }
    var isEditing by remember { mutableStateOf(false) }
    var activeDateField by remember { mutableStateOf<DateField?>(null) }
    val focusManager = LocalFocusManager.current

    val tenantApi = remember { Tenant() }
    val costApi = remember { Cost() }

    var tenantWithRelation by remember { mutableStateOf<TenantsUnitsCrossRef?>(null) }
    var user by remember { mutableStateOf<User?>(null) }
    var unit by remember { mutableStateOf<Units?>(null) }
    var rentDebt by remember { mutableStateOf(0.0) }
    var mortgageDebt by remember { mutableStateOf(0.0) }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasPaidAny by remember { mutableStateOf(false) }
    LaunchedEffect(unitId, tenantId) {
        isLoading = true
        errorMessage = null

        tenantApi.getTenantWithUnit(
            context = context,
            tenantId = tenantId,
            buildingId = buildingId,
            onSuccess = { t ->
                user = t.user
                unit = t.unit
                tenantWithRelation = t.tenantUnit
                isLoading = false
            },
            onError = { e ->
                errorMessage = e.message ?: "Error loading tenant"
                isLoading = false
                coroutineScope.launch {
                    snackBarHostState.showSnackbar(errorMessage ?: "")
                }
            }
        )

        costApi.fetchCostsWithDebts(
            context = context,
            ownerId = null,
            unitId = unitId,
            onSuccess = { costs, debts ->
                val rentCostIds = costs
                    .filter { it.costName == "اجاره" && it.fundType == FundType.NONE }
                    .map { it.costId }
                    .toSet()
                val mortgageCostIds = costs
                    .filter { it.costName == "رهن" && it.fundType == FundType.NONE }
                    .map { it.costId }
                    .toSet()

                rentDebt = debts
                    .firstOrNull { it.costId in rentCostIds}?.amount ?: 0.0
                mortgageDebt = debts
                    .firstOrNull { it.costId in mortgageCostIds}?.amount ?: 0.0

                hasPaidAny = debts.any { it.costId in rentCostIds || it.costId in mortgageCostIds && it.paymentFlag }
            },
            onError = { e ->
                errorMessage = e.message ?: "Error loading costs"
                coroutineScope.launch {
                    snackBarHostState.showSnackbar(errorMessage ?: "")
                }
            }
        )
    }

    if (isLoading) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val twr = tenantWithRelation
    if (twr == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = errorMessage ?: context.getString(R.string.no_tenants_found),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }
    var mobile by remember(isEditing, twr) { mutableStateOf(user!!.mobileNumber) }
    var email by remember(isEditing, twr) { mutableStateOf(user!!.email) }
    var startDate by remember(isEditing, twr) { mutableStateOf(tenantWithRelation!!.startDate) }
    var endDate by remember(isEditing, twr) { mutableStateOf(tenantWithRelation!!.endDate) }
    var numberOfTenant by remember(isEditing, twr) { mutableStateOf(tenantWithRelation!!.numberOfTenants) }
    var selectedStatus by remember(isEditing, twr) { mutableStateOf(tenantWithRelation!!.status) }

    var rentText by remember(isEditing, rentDebt) {
        mutableStateOf(
            if (rentDebt == 0.0) "" else formatWithCommaTenant(rentDebt.toLong().toString())
        )
    }
    var mortgageText by remember(isEditing, mortgageDebt) {
        mutableStateOf(
            if (mortgageDebt == 0.0) "" else formatWithCommaTenant(mortgageDebt.toLong().toString())
        )
    }


    val isInsertEnabled =
        startDate.isNotBlank() &&
                endDate.isNotBlank() &&
                numberOfTenant.isNotBlank() && numberOfTenant.toInt() > 0 &&
                selectedStatus.isNotBlank()

    val listState = rememberLazyListState()
    var units by remember { mutableStateOf<List<Units>>(emptyList()) }
    val noOwnerUnits = units.isEmpty()
    var selectedUnit by remember { mutableStateOf<Units?>(null) }
    LaunchedEffect(units) {
        selectedUnit = units.firstOrNull {
            it.unitId == unitId
        }
    }

    LaunchedEffect(buildingId) {
        com.example.delta.volley.Units().fetchUnitsWithOwnerForBuilding (
            context = context,
            buildingId = buildingId,
            onSuccess = { list ->
                units = list
            },
            onError = { e ->
                Toast.makeText(
                    context,
                    e.message ?: context.getString(R.string.failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .padding(8.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(
                    top = 0.dp,
                    bottom = 120.dp
                )
            ) {
                item {
                    OwnerInfoRow(Icons.Default.Person, "${user!!.firstName} ${user!!.lastName}")
                    Spacer(Modifier.height(8.dp))
                    OwnerInfoRow(
                        Icons.Default.Phone,
                        "${context.getString(R.string.phone_number)}: ${user!!.phoneNumber}"
                    )
                    Spacer(Modifier.height(8.dp))
                    OwnerInfoRow(
                        Icons.Default.MobileFriendly,
                        "${context.getString(R.string.mobile_number)}: ${user!!.mobileNumber}"
                    )
                    Spacer(Modifier.height(8.dp))
                    OwnerInfoRow(
                        Icons.Default.Email,
                        "${context.getString(R.string.email)}: ${user!!.email}"
                    )
                    Spacer(Modifier.height(8.dp))
                }

                if (isEditing) {
                    item {
                        Spacer(Modifier.height(12.dp))

                        if (!noOwnerUnits) {
                            ExposedDropdownMenuBoxExample(
                                sharedViewModel = sharedViewModel,
                                items = units,
                                selectedItem = selectedUnit,
                                onItemSelected = {
                                    selectedUnit = it
                                    tenantWithRelation = tenantWithRelation!!.copy(
                                        unitId = it.unitId
                                    )
                                },
                                label = context.getString(R.string.unit_number),
                                modifier = Modifier.fillMaxWidth(),
                                itemLabel = { it.unitNumber }
                            )
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = numberOfTenant,
                            onValueChange = { value ->
                                if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*\$"))) {
                                    numberOfTenant = value
                                    tenantWithRelation = tenantWithRelation!!.copy( numberOfTenants = numberOfTenant)
                                }
                            },
                            singleLine = true,
                            label = {
                                Text(
                                    context.getString(R.string.number_of_tenants),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = startDate,
                            onValueChange = {
                                tenantWithRelation = tenantWithRelation!!.copy(startDate =  startDate)
                            },
                            label = { Text(context.getString(R.string.start_date)) },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        focusManager.clearFocus()
                                        activeDateField = DateField.START
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null
                                    )
                                }
                            }
                        )

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = endDate,
                            onValueChange = {
                                tenantWithRelation = tenantWithRelation!!.copy(endDate =  endDate)
                            },
                            label = { Text(context.getString(R.string.end_date)) },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        focusManager.clearFocus()
                                        activeDateField = DateField.END
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null
                                    )
                                }
                            }
                        )

                        if (activeDateField != null) {
                            PersianDatePickerDialogContent(
                                sharedViewModel = sharedViewModel,
                                onDateSelected = { selected ->
                                    when (activeDateField) {
                                        DateField.START -> {tenantWithRelation = tenantWithRelation!!.copy(startDate = selected.toString())}
                                        DateField.END   -> {tenantWithRelation = tenantWithRelation!!.copy(endDate = selected.toString())}
                                        null -> {}
                                    }
                                    activeDateField = null
                                },
                                onDismiss = { activeDateField = null }
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        StatusDropdown(
                            selectedStatus = selectedStatus,
                            onStatusSelected = {
                                selectedStatus = it
                                tenantWithRelation = tenantWithRelation!!.copy(status = it)
                            }
                        )

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = if (rentText == "0.0") "" else rentText,
                            onValueChange = { value ->
                                val raw = value.replace(",", "")
                                if (raw.isEmpty()) {
                                    rentText = ""
                                    rentDebt = 0.0
                                } else if (raw.matches(Regex("^\\d*$"))) {
                                    rentText = formatWithComma(raw)
                                     rentDebt = raw.toDouble()
                                }
                            },
                            singleLine = true,
                            label = {
                                Text(
                                    context.getString(R.string.rent),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Spacer(Modifier.height(8.dp))

                        val amountVal = rentDebt.toLong()
                        val amountInWords =
                            NumberCommaTransformation().numberToWords(context, amountVal)
                        Text(
                            text = "$amountInWords ${context.getString(R.string.toman)}",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(Modifier.height(8.dp))


                        OutlinedTextField(
                            value = if (mortgageText == "0.0") "" else mortgageText,
                            onValueChange = { value ->
                                val raw = value.replace(",", "")
                                if (raw.isEmpty()) {
                                    mortgageText = ""
                                    mortgageDebt = 0.0
                                } else if (raw.matches(Regex("^\\d*$"))) {
                                    mortgageText = formatWithComma(raw)
                                    mortgageDebt = raw.toDouble()
                                    Log.d("mortgageDebt", mortgageDebt.toString())
                                }
                            },
                            singleLine = true,
                            label = {
                                Text(
                                    context.getString(R.string.mortgage),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        val amountMortgageVal = mortgageDebt.toLong()
                        val amountMortgageInWords =
                            NumberCommaTransformation().numberToWords(context, amountMortgageVal)
                        Text(
                            text = "$amountMortgageInWords ${context.getString(R.string.toman)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    item {
                        Spacer(Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                onClick = { isEditing = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    context.getString(R.string.cancel),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        if (email!!.isNotBlank() &&
                                            !validation.isValidEmail(email.toString())
                                        ) {
                                            snackBarHostState.showSnackbar(
                                                context.getString(R.string.invalid_email)
                                            )
                                            return@launch
                                        }

                                        if (!validation.isValidIranMobile(mobile)) {
                                            snackBarHostState.showSnackbar(
                                                context.getString(R.string.invalid_mobile_number)
                                            )
                                            return@launch
                                        }

                                        try {
                                            tenantApi.updateTenant(
                                                context = context,
                                                tenantId = tenantId,
                                                buildingId = buildingId,
                                                tenantUnit = tenantWithRelation,
                                                rentDebt = rentDebt,
                                                mortgageDebt = mortgageDebt,
                                                onSuccess = {
                                                    isEditing = false
                                                    coroutineScope.launch {
                                                        snackBarHostState.showSnackbar(
                                                            context.getString(R.string.success_update)
                                                        )
                                                    }
                                                },
                                                onError = { e ->
                                                    coroutineScope.launch {
                                                        val raw = e.message ?: ""
                                                        val msg = when {
                                                            raw.contains("tenant-active-elsewhere-in-period", ignoreCase = true) ->
                                                                context.getString(R.string.tenant_is_already_active_in_another_place)
                                                            raw.contains("Active tenant already exists within selected period", ignoreCase = true) ->
                                                                context.getString(R.string.active_tenant_already_exists_within_selected_period)
                                                            raw.contains("This unit already has a resident owner", ignoreCase = true) ->
                                                                context.getString(R.string.this_unit_has_resident_owner)
                                                            raw.contains("Active tenant must include today in date range", ignoreCase = true) ->
                                                                context.getString(R.string.active_tenant_must_incluse_current_day)
                                                            raw.contains("invalid-date-range", ignoreCase = true) ->
                                                                context.getString(R.string.error_invalid_date_range)
                                                            else ->
                                                                raw.ifBlank { context.getString(R.string.failed) }
                                                        }

                                                        snackBarHostState.showSnackbar(msg)
                                                    }
                                                }
                                            )

                                        } catch (e: Exception) {
                                            isEditing = false
                                            snackBarHostState.showSnackbar(
                                                context.getString(R.string.failed)
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = isInsertEnabled
                            ) {
                                Text(
                                    context.getString(R.string.insert),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                } else {
                    item {
                        OwnerInfoRow(
                            Icons.Default.Person,
                            "${context.getString(R.string.unit_number)}: ${unit!!.unitNumber}"
                        )
                        Spacer(Modifier.height(8.dp))
                        OwnerInfoRow(
                            Icons.Default.Person,
                            "${context.getString(R.string.number_of_tenants)}: ${tenantWithRelation!!.numberOfTenants}"
                        )
                        Spacer(Modifier.height(8.dp))
                        OwnerInfoRow(
                            Icons.Default.DateRange,
                            "${context.getString(R.string.start_date)}: ${tenantWithRelation!!.startDate}"
                        )
                        Spacer(Modifier.height(8.dp))
                        OwnerInfoRow(
                            Icons.Default.DateRange,
                            "${context.getString(R.string.end_date)}: ${tenantWithRelation!!.endDate}"
                        )
                        Spacer(Modifier.height(8.dp))
                        OwnerInfoRow(
                            Icons.Default.Info,
                            "${context.getString(R.string.status)}: ${tenantWithRelation!!.status}"
                        )
                        Spacer(Modifier.height(8.dp))
                        OwnerInfoRow(
                            Icons.Default.AttachMoney,
                            "${context.getString(R.string.rent)}: ${
                                formatNumberWithCommas(rentDebt)
                            } ${context.getString(R.string.toman)}"
                        )
                        Spacer(Modifier.height(8.dp))
                        OwnerInfoRow(
                            Icons.Default.AccountBalanceWallet,
                            "${context.getString(R.string.mortgage)}: ${
                                formatNumberWithCommas(
                                    mortgageDebt
                                )
                            } ${context.getString(R.string.toman)}"
                        )
                    }
                }
            }
        }

        if (!isEditing) {
            FloatingActionButton(
                onClick = {
//                    if (hasPaidAny) {
//                        coroutineScope.launch {
//                            snackBarHostState.showSnackbar(context.getString(R.string.edit_disable_because_of_payment))
//                        }
//                    } else {
                        isEditing = true
//                    }
                    },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = context.getString(R.string.edit))
            }
        }

        SnackbarHost(
            hostState = snackBarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}


fun formatNumberWithCommas(number: Double): String {
    return NumberFormat.getNumberInstance(Locale.US).format(number)
}

fun formatNumberWithCommas(number: Long): String {
    return NumberFormat.getNumberInstance(Locale.US).format(number)
}

@Composable
fun TenantFinancialsTab(
    unitId: Long,
    tenantId: Long,
    buildingId: Long,
    sharedViewModel: SharedViewModel,
    snackBarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var allDebts by remember { mutableStateOf<List<Debts>>(emptyList()) }
    var allCosts by remember { mutableStateOf<List<Costs>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var tenantWithRelation by remember { mutableStateOf<TenantsUnitsCrossRef?>(null) }

    LaunchedEffect(unitId, tenantId) {
        isLoading = true
        errorMessage = null

        Tenant().getTenantWithUnit(
            context = context,
            tenantId = tenantId,
            buildingId = buildingId,
            onSuccess = { t ->
                tenantWithRelation = t.tenantUnit
            },
            onError = {
                tenantWithRelation = null
                isLoading = false
            }
        )
    }

    LaunchedEffect(tenantWithRelation, unitId) {
        val tenant = tenantWithRelation ?: return@LaunchedEffect

        isLoading = true
        errorMessage = null

        Cost().fetchCostsWithDebts(
            context = context,
            ownerId = null,
            unitId = unitId,
            onSuccess = { costs, debts ->
                val start = tenant.startDate.padStartDate()
                val end   = tenant.endDate.padStartDate()


                allCosts = costs
                allDebts =
                    debts
                        .filter { it.description != "رهن" && it.description != "اجاره" }
                        .filter { it.ownerId == null || it.ownerId == 0L }
                        .filter { d ->
                            val due = d.dueDate.padStartDate()
                            Log.d("due", due)
                            due >= start && due <= end
                        }

                isLoading = false
            },
            onError = { e ->
                errorMessage = e.message ?: "خطا در دریافت اطلاعات"
                isLoading = false
                coroutineScope.launch {
                    snackBarHostState.showSnackbar(errorMessage ?: "")
                }
            }
        )
    }

    val debts = remember(allDebts) { allDebts.filter { it.paymentFlag == false } }
    val payments = remember(allDebts) { allDebts.filter { it.paymentFlag == true } }

    val transactions = remember(debts, payments) {
        (debts.map {
            TransactionItem(it.debtId, it.amount, it.dueDate, it.description, FilterType.DEBT)
        } + payments.map {
            TransactionItem(it.debtId, it.amount, it.dueDate, it.description, FilterType.PAYMENT)
        }).sortedBy { it.date }
    }

    var filterType by rememberSaveable { mutableStateOf(FilterType.ALL) }
    val filteredTransactions = remember(transactions, filterType) {
        when (filterType) {
            FilterType.ALL -> transactions
            FilterType.DEBT -> transactions.filter { it.transactionType == FilterType.DEBT }
            FilterType.PAYMENT -> transactions.filter { it.transactionType == FilterType.PAYMENT }
        }
    }

    val totalDebtAmount = debts.sumOf { it.amount }.roundToLong()
    val totalPaymentAmount = payments.sumOf { it.amount }.roundToLong()

    Column(modifier = modifier.padding(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${context.getString(R.string.debt)}: ${
                            formatNumberWithCommas(totalDebtAmount)
                        } ${context.getString(R.string.toman)}"
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${context.getString(R.string.payments)}: ${
                            formatNumberWithCommas(totalPaymentAmount)
                        } ${context.getString(R.string.toman)}"
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilterType.entries.forEach { type ->
                Button(
                    onClick = { filterType = type },
                    colors = if (filterType == type)
                        ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
                    else
                        ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = type.getDisplayName(context),
                        color = if (filterType == type)
                            Color(context.getColor(R.color.white))
                        else
                            Color(context.getColor(R.color.grey)),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            filteredTransactions.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = context.getString(R.string.no_transactions_recorded),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredTransactions) { item ->
                        TransactionRow(item, onPayment = {
                            coroutineScope.launch {
                                val debt = allDebts.find { it.debtId == item.id }
                                if (debt == null) {
                                    snackBarHostState.showSnackbar(
                                        context.getString(R.string.failed)
                                    )
                                    return@launch
                                }

                                val updatedDebt = debt.copy(paymentFlag = true)
                                sharedViewModel.updateDebtOnServer(context, updatedDebt)

                                allDebts = allDebts.map {
                                    if (it.debtId == debt.debtId) updatedDebt else it
                                }

                                val cost = allCosts.find { it.costId == debt.costId }
                                val fundType = cost?.fundType ?: FundType.OPERATIONAL

                                Fund().increaseBalanceFundOnServer(
                                    context = context,
                                    buildingId = debt.buildingId,
                                    amount = debt.amount,
                                    fundType = fundType,
                                    onSuccess = {
                                        coroutineScope.launch {
                                            sharedViewModel.loadFundBalances(context, debt.buildingId)
                                            snackBarHostState.showSnackbar(
                                                context.getString(
                                                    if (fundType == FundType.OPERATIONAL)
                                                        R.string.success_pay_tooperational_fund
                                                    else
                                                        R.string.success_pay_tocapital_fund
                                                )
                                            )
                                        }
                                    },
                                    onError = {
                                        coroutineScope.launch {
                                            snackBarHostState.showSnackbar(
                                                context.getString(R.string.failed)
                                            )
                                        }
                                    }
                                )
                            }
                        })
                    }
                }
            }
        }
    }
}

fun String.padStartDate(): String {
    val parts = this.split("/")
    if (parts.size != 3) return this
    val y = parts[0].padStart(4, '0')
    val m = parts[1].padStart(2, '0')
    val d = parts[2].padStart(2, '0')
    return "$y/$m/$d"
}
