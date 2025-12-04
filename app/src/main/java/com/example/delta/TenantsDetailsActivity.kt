package com.example.delta

import android.os.Bundle
import android.util.Log
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
import androidx.compose.ui.focus.onFocusChanged
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

    LaunchedEffect(tenantId) {
        Tenant().getTenantWithUnit(
            context = context,
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
                    sharedViewModel,
                    snackBarHostState = snackBarHostState,
                    coroutineScope = coroutineScope,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
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

    val tenantApi = remember { com.example.delta.volley.Tenant() }
    val costApi = remember { com.example.delta.volley.Cost() }

    var tenantWithRelation by remember { mutableStateOf<TenantsUnitsCrossRef?>(null) }
    var user by remember { mutableStateOf<User?>(null) }
    var unit by remember { mutableStateOf<Units?>(null) }
    var rentDebt by remember { mutableStateOf(0.0) }
    var mortgageDebt by remember { mutableStateOf(0.0) }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(unitId, tenantId) {
        isLoading = true
        errorMessage = null

        tenantApi.getTenantWithUnit(
            context = context,
            tenantId = tenantId,
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
                    .filter { it.costName == "اجاره" }
                    .map { it.costId }
                    .toSet()

                val mortgageCostIds = costs
                    .filter { it.costName == "رهن" }
                    .map { it.costId }
                    .toSet()

                rentDebt = debts
                    .filter { it.costId in rentCostIds && it.paymentFlag == false }
                    .sumOf { it.amount }

                mortgageDebt = debts
                    .filter { it.costId in mortgageCostIds && it.paymentFlag == false }
                    .sumOf { it.amount }
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
            modifier = modifier.fillMaxSize(),
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

    var firstName by remember(isEditing, twr) { mutableStateOf(user!!.firstName) }
    var lastName by remember(isEditing, twr) { mutableStateOf(user!!.lastName) }
    var phone by remember(isEditing, twr) { mutableStateOf(user!!.phoneNumber) }
    var mobile by remember(isEditing, twr) { mutableStateOf(user!!.mobileNumber) }
    var email by remember(isEditing, twr) { mutableStateOf(user!!.email) }
    var startDate by remember(isEditing, twr) { mutableStateOf(tenantWithRelation!!.startDate) }
    var endDate by remember(isEditing, twr) { mutableStateOf(tenantWithRelation!!.endDate) }
    var numberOfTenant by remember(isEditing, twr) { mutableStateOf(tenantWithRelation!!.numberOfTenants) }
    var selectedStatus by remember(isEditing, twr) { mutableStateOf(tenantWithRelation!!.status) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    var rentText by remember(isEditing) { mutableStateOf(rentDebt.toString()) }
    var mortgageText by remember(isEditing) { mutableStateOf(mortgageDebt.toString()) }

    val rentValue = rentText.toDoubleOrNull() ?: 0.0
    val mortgageValue = mortgageText.toDoubleOrNull() ?: 0.0

    val isInsertEnabled = //firstName.isNotBlank() &&
            //lastName.isNotBlank() &&
            //phone!!.isNotBlank() &&
            //mobile.isNotBlank() &&
            startDate.isNotBlank() &&
            endDate.isNotBlank() &&
            numberOfTenant.isNotBlank() &&
            selectedStatus.isNotBlank()

    Box(
        modifier = modifier
            .fillMaxSize()
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
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 84.dp)
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
                    if (isEditing) {
                        OutlinedTextField(
                            value = numberOfTenant,
                            onValueChange = { value ->
                                if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*\$"))) {
                                    numberOfTenant = value
                                }
                            },
                            singleLine = true,
                            label = { Text(context.getString(R.string.number_of_tenants), style = MaterialTheme.typography.bodyLarge) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = { },
                            label = { Text(context.getString(R.string.start_date)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) showStartDatePicker = true
                                },
                            readOnly = true
                        )
                        if (showStartDatePicker) {
                            PersianDatePickerDialogContent(
                                sharedViewModel = sharedViewModel,
                                onDateSelected = { selected ->
                                    startDate = selected
                                    showStartDatePicker = false
                                },
                                onDismiss = { showStartDatePicker = false },
                                context = context
                            )
                        }
                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = endDate,
                            onValueChange = { },
                            label = { Text(context.getString(R.string.end_date)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) showEndDatePicker = true
                                },
                            readOnly = true
                        )
                        if (showEndDatePicker) {
                            PersianDatePickerDialogContent(
                                sharedViewModel = sharedViewModel,
                                onDateSelected = { selected ->
                                    endDate = selected
                                    showEndDatePicker = false
                                },
                                onDismiss = { showEndDatePicker = false },
                                context = context
                            )
                        }
                        Spacer(Modifier.height(8.dp))

                        StatusDropdown(
                            selectedStatus = selectedStatus,
                            onStatusSelected = { selectedStatus = it }
                        )
                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = rentText,
                            onValueChange = { value ->
                                if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*\$"))) {
                                    rentText = value
                                }
                            },
                            singleLine = true,
                            label = { Text(context.getString(R.string.rent), style = MaterialTheme.typography.bodyLarge) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = mortgageText,
                            onValueChange = { value ->
                                if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*\$"))) {
                                    mortgageText = value
                                }
                            },
                            singleLine = true,
                            label = { Text(context.getString(R.string.mortgage), style = MaterialTheme.typography.bodyLarge) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    } else {
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
                                formatNumberWithCommas(
                                    rentDebt
                                )
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

        if (isEditing) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .background(Color(context.getColor(R.color.white)))
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { isEditing = false },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(context.getString(R.string.cancel), style = MaterialTheme.typography.bodyLarge)
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
                                    tenantUnit= tenantWithRelation,
                                    rentDebt = rentDebt,
                                    mortgageDebt= mortgageDebt,
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
                                                snackBarHostState.showSnackbar(
                                                    context.getString(R.string.failed)
                                                )
                                        }
                                    }
                                )
                            } catch (e: Exception) {
                                isEditing = false
                                snackBarHostState.showSnackbar(context.getString(R.string.failed))
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = isInsertEnabled
                ) {
                    Text(context.getString(R.string.insert), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        if (!isEditing) {
            FloatingActionButton(
                onClick = { isEditing = true },
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

@Composable
fun TenantFinancialsTab(
    unitId: Long,
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

    LaunchedEffect(unitId) {
        isLoading = true
        errorMessage = null

        Cost().fetchCostsWithDebts(
            context = context,
            ownerId = null,
            unitId = unitId,
            onSuccess = { costs, debts ->
                Log.d("costs", costs.toString())
                Log.d("debts", debts.toString())
                allCosts = costs
                allDebts = debts
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
        }).sortedByDescending { it.date }
    }

    var filterType by rememberSaveable { mutableStateOf(FilterType.ALL) }
    val filteredTransactions = remember(transactions, filterType) {
        when (filterType) {
            FilterType.ALL -> transactions
            FilterType.DEBT -> transactions.filter { it.transactionType == FilterType.DEBT }
            FilterType.PAYMENT -> transactions.filter { it.transactionType == FilterType.PAYMENT }
        }
    }

    val totalDebtAmount = debts.sumOf { it.amount }
    val totalPaymentAmount = payments.sumOf { it.amount }

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

