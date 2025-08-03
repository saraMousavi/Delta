package com.example.delta

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TenantsDetailsActivity : ComponentActivity() {
    val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val unitId = intent.getLongExtra("UNIT_DATA", -1L)
        val tenantId = intent.getLongExtra("TENANT_DATA", -1L)
        setContent {
            AppTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    TenantDetailsScreen(
                        unitId = unitId,
                        tenantId = tenantId,
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
    sharedViewModel: SharedViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    Log.d("unitId", unitId.toString())

    val tabs = listOf(
        OwnerTabItem(context.getString(R.string.overview), OwnerTabType.OVERVIEW),
        OwnerTabItem(context.getString(R.string.transaction), OwnerTabType.FINANCIALS)
    )

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    val tenant =
                        sharedViewModel.getTenant(tenantId).collectAsState(initial = null).value
                    Text(
                        text = tenant?.let { "${it.firstName} ${it.lastName}" }
                            ?: context.getString(R.string.loading),
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
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            OwnerSectionSelector(
                tabs = tabs,
                selectedIndex = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            when (tabs[selectedTab].type) {
                OwnerTabType.OVERVIEW -> TenantOverviewTab(
                    unitId,
                    sharedViewModel,
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
    sharedViewModel: SharedViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    var isEditing by remember { mutableStateOf(false) }

    val tenantWithRelation by sharedViewModel.getActiveTenantsWithRelationForUnit(unitId)
        .collectAsState(initial = null)

    val tenantRentMap = sharedViewModel.tenantRentDebtMap
    val tenantMortgageMap = sharedViewModel.tenantMortgageDebtMap

    val unit by sharedViewModel.getUnit(unitId).collectAsState(initial = null)
    val rentCost by sharedViewModel.getCostByBuildingIdAndName(unit?.buildingId ?: 0L, "اجاره")
        .collectAsState(initial = null)
    val mortgageCost by sharedViewModel.getCostByBuildingIdAndName(unit?.buildingId ?: 0L, "رهن")
        .collectAsState(initial = null)

    // Load tenant rent and mortgage amounts
    LaunchedEffect(tenantWithRelation, rentCost, mortgageCost) {
        tenantWithRelation?.let { twr ->
            sharedViewModel.loadTenantDebtAmounts(
                unitId = unit?.unitId ?: 0,
                tenantId = twr.tenant.tenantId,
                rentCostId = rentCost?.costId ?: 0,
                mortgageCostId = mortgageCost?.costId ?: 0
            )
        }
        isEditing = false
    }

    // If tenant data null, show info
    if (tenantWithRelation == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = context.getString(R.string.no_tenants_found), style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    val twr = tenantWithRelation!!

    // Initialize editable states from tenant fields only once per isEditing toggling
    var firstName by remember(isEditing) { mutableStateOf(twr.tenant.firstName) }
    var lastName by remember(isEditing) { mutableStateOf(twr.tenant.lastName) }
    var phone by remember(isEditing) { mutableStateOf(twr.tenant.phoneNumber) }
    var mobile by remember(isEditing) { mutableStateOf(twr.tenant.mobileNumber) }
    var email by remember(isEditing) { mutableStateOf(twr.tenant.email) }
    var startDate by remember(isEditing) { mutableStateOf(twr.tenant.startDate) }
    var endDate by remember(isEditing) { mutableStateOf(twr.tenant.endDate) }
    var numberOfTenant by remember(isEditing) { mutableStateOf(twr.tenant.numberOfTenants) }

    var selectedStatus by remember(isEditing) { mutableStateOf(twr.crossRef.status) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val dismissStartDatePicker = { showStartDatePicker = false }
    val dismissEndDatePicker = { showEndDatePicker = false }

    // Rent and mortgage as mutable text fields with local state, initialized from ViewModel maps
    var rentText by remember(isEditing) { mutableStateOf((tenantRentMap[twr.tenant.tenantId] ?: 0.0).toString()) }
    var mortgageText by remember(isEditing) { mutableStateOf((tenantMortgageMap[twr.tenant.tenantId] ?: 0.0).toString()) }
    val rentDebt = rentText.toDoubleOrNull() ?: 0.0
    val mortgageDebt = mortgageText.toDoubleOrNull() ?: 0.0

    // Check if insert/save button should be enabled
    val isInsertEnabled = firstName.isNotBlank() &&
            lastName.isNotBlank() &&
            phone.isNotBlank() &&
            mobile.isNotBlank() &&
            email.isNotBlank() &&
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
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 84.dp) // Space for buttons row/fab
            ) {
                item {
                    Log.d("isEditing", isEditing.toString())
                    if (isEditing) {
                        // Editable fields using OwnerTextField and other inputs
                        OwnerTextField(R.string.first_name, firstName) { firstName = it }
                        Spacer(Modifier.height(8.dp))
                        OwnerTextField(R.string.last_name, lastName) { lastName = it }
                        Spacer(Modifier.height(8.dp))
                        OwnerTextField(R.string.number_of_tenants, numberOfTenant) { numberOfTenant = it }
                        Spacer(Modifier.height(8.dp))
                        OwnerTextField(R.string.phone_number, phone) { phone = it }
                        Spacer(Modifier.height(8.dp))
                        OwnerTextField(R.string.mobile_number, mobile) { mobile = it }
                        Spacer(Modifier.height(8.dp))
                        OwnerTextField(R.string.email, email) { email = it }
                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = startDate,
                            onValueChange = { /* no-op */ },
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
                                onDateSelected = { selected ->
                                    startDate = selected
                                    dismissStartDatePicker()
                                },
                                onDismiss = dismissStartDatePicker,
                                context = context
                            )
                        }
                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = endDate,
                            onValueChange = { /* no-op */ },
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
                                onDateSelected = { selected ->
                                    endDate = selected
                                    dismissEndDatePicker()
                                },
                                onDismiss = dismissEndDatePicker,
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
                        val tenantId = twr.tenant.tenantId
                        // Display mode -- use OwnerInfoRow with icons and labels
                        OwnerInfoRow(Icons.Default.Person, "${twr.tenant.firstName} ${twr.tenant.lastName}")
                        Spacer(Modifier.height(8.dp))
                        OwnerInfoRow(Icons.Default.Person,
                            "${context.getString(R.string.number_of_tenants)}:" +
                                    " ${twr.tenant.numberOfTenants}")
                        Spacer(Modifier.height(8.dp))
                        OwnerInfoRow(
                            Icons.Default.Phone,
                            "${context.getString(R.string.phone_number)}: ${twr.tenant.phoneNumber}"
                        )
                        Spacer(Modifier.height(8.dp))
                        OwnerInfoRow(
                            Icons.Default.MobileFriendly,
                            "${context.getString(R.string.mobile_number)}: ${twr.tenant.mobileNumber}"
                        )
                        Spacer(Modifier.height(8.dp))
                        OwnerInfoRow(
                            Icons.Default.Email,
                            "${context.getString(R.string.email)}: ${twr.tenant.email}"
                        )
                        Spacer(Modifier.height(8.dp))
                        OwnerInfoRow(
                            Icons.Default.DateRange,
                            "${context.getString(R.string.start_date)}: ${twr.tenant.startDate}"
                        )
                        Spacer(Modifier.height(8.dp))
                        OwnerInfoRow(
                            Icons.Default.DateRange,
                            "${context.getString(R.string.end_date)}: ${twr.tenant.endDate}"
                        )
                        Spacer(Modifier.height(8.dp))
                        OwnerInfoRow(
                            Icons.Default.Info,
                            "${context.getString(R.string.status)}: ${twr.crossRef.status}"
                        )
                        Spacer(Modifier.height(8.dp))
                        OwnerInfoRow(
                            Icons.Default.AttachMoney,
                            "${context.getString(R.string.rent)}: ${formatNumberWithCommas(sharedViewModel.tenantRentDebtMap[tenantId] ?: 0.0)} ${context.getString(R.string.toman)}"
                        )
                        Spacer(Modifier.height(8.dp))
                        OwnerInfoRow(
                            Icons.Default.AccountBalanceWallet,
                            "${context.getString(R.string.mortgage)}: ${formatNumberWithCommas(sharedViewModel.tenantMortgageDebtMap[tenantId] ?: 0.0)} ${context.getString(R.string.toman)}"
                        )
                    }
                }
            }
        }

        // Insert and Cancel buttons row at bottom, only visible in edit mode
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
                            try{
                                isEditing = false
                                sharedViewModel.updateTenantWithCostsAndDebts(
                                    tenantWithRelation = twr,
                                    updatedTenant = twr.tenant.copy(
                                        firstName = firstName.trim(),
                                        lastName = lastName.trim(),
                                        numberOfTenants = numberOfTenant.trim(),
                                        phoneNumber = phone.trim(),
                                        mobileNumber = mobile.trim(),
                                        email = email.trim(),
                                        startDate = startDate,
                                        endDate = endDate,
                                        status = selectedStatus
                                    ),
                                    rentAmount = rentDebt,
                                    mortgageAmount = mortgageDebt
                                )
                                withContext(Dispatchers.Main) {
                                    snackBarHostState.showSnackbar(context.getString(R.string.success_update))
                                }
                            } catch (e: Exception){
                                isEditing = false
                                withContext(Dispatchers.Main) {
                                    Log.e("error", e.message.toString())
                                    snackBarHostState.showSnackbar(context.getString(R.string.failed))
                                }
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

        // Floating Action Button toggles edit mode only when NOT editing
        if (!isEditing) {
            FloatingActionButton(
                onClick = { isEditing = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = context.getString(R.string.edit))
            }
        }
    }
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

    val debts by sharedViewModel.getDebtsForUnitAndMonth(unitId, "null", "00")
        .collectAsState(initial = emptyList())
    val payments by sharedViewModel.getPaysForUnit(unitId).collectAsState(initial = emptyList())
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
        // Summary card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${context.getString(R.string.debt)}: ${
                            formatNumberWithCommas(
                                totalDebtAmount
                            )
                        } ${context.getString(R.string.toman)}"
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${context.getString(R.string.payments)}: ${
                            formatNumberWithCommas(
                                totalPaymentAmount
                            )
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
                        color = if (filterType == type) Color(context.getColor(R.color.white)) else Color(
                            context.getColor(R.color.grey)
                        ),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        if (filteredTransactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = context.getString(R.string.no_transactions_recorded),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredTransactions) { item ->
                    TransactionRow(item, onPayment = {
                        coroutineScope.launch {
                            val debt = sharedViewModel.getDebtById(item.id)
                            debt?.let {
                                val updatedDebt = it.copy(paymentFlag = true)
                                sharedViewModel.updateDebt(updatedDebt)
                                sharedViewModel.updateDebtPaymentFlag(it, true)

                                val cost = sharedViewModel.getCostById(it.costId)
                                val fundType = cost?.fundType ?: FundType.OPERATIONAL

                                val success = sharedViewModel.increaseBalanceFund(
                                    buildingId = it.buildingId,
                                    amount = it.amount,
                                    fundType = fundType
                                )
                                withContext(Dispatchers.Main) {
                                    snackBarHostState.showSnackbar(
                                        if (success) {
                                            context.getString(
                                                if (fundType == FundType.OPERATIONAL)
                                                    R.string.success_pay_tooperational_fund
                                                else
                                                    R.string.success_pay_tocapital_fund
                                            )
                                        } else {
                                            context.getString(R.string.failed)
                                        }
                                    )
                                }
                            }
                        }
                    })
                }
            }
        }
    }
}

