package com.example.delta

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.delta.data.entity.TenantWithRelation
import com.example.delta.data.entity.Units
import com.example.delta.enums.FundType
import com.example.delta.sharedui.DebtItem
import com.example.delta.viewmodel.SharedViewModel
import ir.hamsaa.persiandatepicker.util.PersianCalendar
import kotlinx.coroutines.launch

class TenantsDetailsActivity : ComponentActivity() {

    // Use SharedViewModel for shared state
    val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val unitId = intent.getLongExtra("UNIT_DATA", -1L)
        val tenantId = intent.getLongExtra("TENANT_DATA", -1L)
        Log.d("unitId", unitId.toString())
        setContent {
            AppTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MaterialTheme {
                        UnitDetailsScreen(unitId = unitId, tenantId = tenantId)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun UnitDetailsScreen(unitId: Long, tenantId: Long) {
        val context = LocalContext.current

        val unit = sharedViewModel.getUnit(unitId).collectAsState(initial = null)
        val tenant = sharedViewModel.getTenant(tenantId).collectAsState(initial = null)
        Log.d("tenant", tenant.toString())
        if (unit.value != null) {
        val tabTitles = listOf(
            context.getString(R.string.overview),
            context.getString(R.string.debt),
            context.getString(R.string.payments)
        )
        var selectedTab by remember { mutableIntStateOf(0) }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "${context.getString(R.string.name)} : ${tenant.value!!.firstName} ${tenant.value!!.lastName}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { finish() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                ScrollableTabRow(selectedTabIndex = selectedTab) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                when (selectedTab) {
                    0 -> OverviewSection(unit = unit.value!!, sharedViewModel = sharedViewModel)
                    1 -> DebtSection(
                        unitId = unit.value!!.unitId,
                        sharedViewModel = sharedViewModel
                    ) // Pass SharedViewModel
                    2 -> PaymentsSection(
                        unitId = unit.value!!.unitId,
                        sharedViewModel = sharedViewModel
                    ) // Pass SharedViewModel
                }
            }
        }
        }
    }


    @Composable
    fun OverviewSection(
        unit: Units,
        sharedViewModel: SharedViewModel,
        modifier: Modifier = Modifier
    ) {
        val context = LocalContext.current

        // Per-tenant edit state map: tenantId -> isEditing
        val tenantEditStates = remember { mutableStateMapOf<Long, Boolean>() }
        // Rent and mortgage for each tenant
        val tenantRentMap = remember { mutableStateMapOf<Long, Double>() }
        val tenantMortgageMap = remember { mutableStateMapOf<Long, Double>() }

        // Fetch tenants + relations
        val tenantsWithRelation by sharedViewModel.getTenantUnitRelationshipsOfUnit(unit.unitId)
            .collectAsState(initial = emptyList())
        val rentCost by sharedViewModel.getCostByBuildingIdAndName(unit.buildingId ?: 0L, "اجاره")
            .collectAsState(initial = null)

        val mortgageCost by sharedViewModel.getCostByBuildingIdAndName(unit.buildingId ?: 0L, "رهن")
            .collectAsState(initial = null)

        // Initialize maps on first load or tenants change
        LaunchedEffect(tenantsWithRelation, rentCost, mortgageCost) {
            tenantsWithRelation.forEach { twr ->
                val tenantId = twr.tenant.tenantId
                sharedViewModel.loadTenantDebtAmounts(
                    unitId = unit.unitId,
                    tenantId = twr.tenant.tenantId,
                    rentCostId = rentCost?.costId ?: 0,
                    mortgageCostId = mortgageCost?.costId ?: 0
                )
                tenantEditStates.putIfAbsent(tenantId, false)
                tenantRentMap.putIfAbsent(tenantId, 0.0)
                tenantMortgageMap.putIfAbsent(tenantId, 0.0)
            }
        }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Unit info card - unchanged, always non-editable
            // Tenants Header
            item {
                Text(
                    text = context.getString(R.string.unit_info),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "${context.getString(R.string.unit)}: ${unit.unitNumber}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${context.getString(R.string.area)}: ${unit.area}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${context.getString(R.string.number_of_room)}: ${unit.numberOfRooms}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${context.getString(R.string.number_of_parking)}: ${unit.numberOfParking}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${context.getString(R.string.number_of_warehouse)}: ${unit.numberOfWarehouse}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }


            // Tenants list
            items(tenantsWithRelation, key = { it.tenant.tenantId }) { tenantWithRelation ->
                val tenantId = tenantWithRelation.tenant.tenantId
                val rentDebtAmount = sharedViewModel.tenantRentDebtMap[tenantId] ?: 0.0
                val mortgageDebtAmount = sharedViewModel.tenantMortgageDebtMap[tenantId] ?: 0.0

                TenantCard(
                    tenantWithRelation = tenantWithRelation,
                    isTenantEditMode = tenantEditStates[tenantId] ?: false,
                    rentInitial = tenantRentMap[tenantId] ?: 0.0,
                    mortgageInitial = tenantMortgageMap[tenantId] ?: 0.0,
                    onSave = { updatedTenantWithRelation, rentValue, mortgageValue ->
                        sharedViewModel.updateTenantWithCostsAndDebts(
                            tenantWithRelation = updatedTenantWithRelation,
                            updatedTenant = updatedTenantWithRelation.tenant,
                            rentAmount = rentValue,
                            mortgageAmount = mortgageValue
                        )
                        // Implement ViewModel calls here to save data
//                        sharedViewModel.updateTenant(updatedTenant.tenant)

                        // Update local maps
                        tenantEditStates[tenantId] = false
//                        tenantRentMap[tenantId] = updatedRent
//                        tenantMortgageMap[tenantId] = updatedMortgage
                    },
                    rentDebtAmount = rentDebtAmount,
                    mortgageDebtAmount = mortgageDebtAmount,
                    onCancel = {
                        // Cancel editing - revert and close edit mode
                        tenantEditStates[tenantId] = false
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Show Edit button for tenant when not editing
                if (tenantEditStates[tenantId] != true) {
                    Button(onClick = { tenantEditStates[tenantId] = true }) {
                        Text(
                            text = context.getString(R.string.edit),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }

    @Composable
    fun TenantCard(
        tenantWithRelation: TenantWithRelation,
        isTenantEditMode: Boolean,
        rentInitial: Double? = null,
        mortgageInitial: Double? = null,
        rentDebtAmount: Double,
        mortgageDebtAmount: Double,
        onSave: (
            updatedTenantWithRelation: TenantWithRelation,
            updatedRent: Double,
            updatedMortgage: Double
        ) -> Unit,
        onCancel: () -> Unit
    ) {
        val context = LocalContext.current
        val tenant = tenantWithRelation.tenant
        val relation = tenantWithRelation.crossRef

        // Editable states for tenant personal info
        var firstName by remember { mutableStateOf(tenant.firstName) }
        var lastName by remember { mutableStateOf(tenant.lastName) }
        var phone by remember { mutableStateOf(tenant.phoneNumber) }
        var mobile by remember { mutableStateOf(tenant.mobileNumber) }
        var email by remember { mutableStateOf(tenant.email) }

        // Rent and mortgage input states as text
        var rentText by remember { mutableStateOf(rentInitial?.toString() ?: "") }
        var mortgageText by remember { mutableStateOf(mortgageInitial?.toString() ?: "") }

        // Status dropdown state
        var selectedStatus by remember { mutableStateOf(relation.status) }

        // StartDate and EndDate states for date pickers
        var startDate by remember { mutableStateOf(relation.startDate) }
        var endDate by remember { mutableStateOf(relation.endDate) }

        // Controls to show date pickers
        var showStartDatePicker by remember { mutableStateOf(false) }
        var showEndDatePicker by remember { mutableStateOf(false) }

        // Function to dismiss date pickers
        val dismissStartDatePicker = { showStartDatePicker = false }
        val dismissEndDatePicker = { showEndDatePicker = false }

        val isInsertEnabled = firstName.isNotBlank()
                && lastName.isNotBlank()
                && phone.isNotBlank()
                && mobile.isNotBlank()
                && email.isNotBlank()
                && startDate.isNotBlank()
                && endDate.isNotBlank()
                && selectedStatus.isNotBlank()

        LaunchedEffect(rentInitial) {
            rentText = rentInitial?.toString() ?: ""
        }

        LaunchedEffect(mortgageInitial) {
            mortgageText = mortgageInitial?.toString() ?: ""
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                if (isTenantEditMode) {
                    // Editable tenant fields
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text(context.getString(R.string.first_name)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text(context.getString(R.string.last_name)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(context.getString(R.string.phone_number)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { mobile = it },
                        label = { Text(context.getString(R.string.mobile_number)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(context.getString(R.string.email)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    // Editable start date field with date picker
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
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

                    // Editable end date field with date picker
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
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

                    // Editable status dropdown
                    StatusDropdown(
                        selectedStatus = selectedStatus,
                        onStatusSelected = { selectedStatus = it }
                    )
                    Spacer(Modifier.height(8.dp))

                    // Editable Rent field
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${context.getString(R.string.rent)}:",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                        )
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = rentText,
                            onValueChange = { value ->
                                if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*\$"))) {
                                    rentText = value
                                }
                            },
                            singleLine = true,
                            modifier = Modifier,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    Spacer(Modifier.height(8.dp))

                    // Editable Mortgage field
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${context.getString(R.string.mortgage)}:",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                        )
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = mortgageText,
                            onValueChange = { value ->
                                if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*\$"))) {
                                    mortgageText = value
                                }
                            },
                            singleLine = true,
                            modifier = Modifier,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
//                    Text(
//                        text = " ${amountInWords.value} ${context.getString(R.string.toman)}",
//                        style = MaterialTheme.typography.bodyLarge,
//                        color = Color(context.getColor(R.color.grey)),
//                        modifier = Modifier.padding(top = 4.dp)
//                    )
                    Spacer(Modifier.height(12.dp))
                    // Save and Cancel buttons inside the card at bottom
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(onClick = onCancel) {
                            Text(
                                text = context.getString(R.string.cancel),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                // Create updated entities for saving
                                val updatedTenant = tenant.copy(
                                    firstName = firstName.trim(),
                                    lastName = lastName.trim(),
                                    phoneNumber = phone.trim(),
                                    mobileNumber = mobile.trim(),
                                    email = email.trim()
                                )
                                // Create updated crossRef with modified dates and status
                                val updatedCrossRef = relation.copy(
                                    startDate = startDate,
                                    endDate = endDate,
                                    status = selectedStatus
                                )
                                // Return updated state wrapped in TenantWithRelation object (assuming copy exists for crossRef)
                                val updatedTenantWithRelation = tenantWithRelation.copy(
                                    tenant = updatedTenant,
                                    crossRef = updatedCrossRef
                                )

                                val rentValue = rentText.toDoubleOrNull() ?: 0.0
                                val mortgageValue = mortgageText.toDoubleOrNull() ?: 0.0

                                onSave(updatedTenantWithRelation, rentValue, mortgageValue)
                            },
                            enabled = isInsertEnabled
                        ) {
                            Text(
                                text = context.getString(R.string.insert),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                    }
                } else {
                    // Read-only view

                    Text(
                        text = "${tenant.firstName} ${tenant.lastName}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "${context.getString(R.string.phone_number)}: ${tenant.phoneNumber}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${context.getString(R.string.mobile_number)}: ${tenant.mobileNumber}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${context.getString(R.string.email)}: ${tenant.email}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(8.dp))

                    Row {
                        Text(
                            text = "${context.getString(R.string.start_date)}: ${relation.startDate}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${context.getString(R.string.end_date)}: ${relation.endDate}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "${context.getString(R.string.status)}: ${relation.status}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "${context.getString(R.string.rent)}: ${
                            String.format(
                                "%.2f",
                                rentDebtAmount
                            )
                        }\"",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${context.getString(R.string.mortgage)}: ${
                            String.format(
                                "%.2f",
                                mortgageDebtAmount
                            )
                        }\"",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }


    @Composable
    fun DebtSection(
        unitId: Long,
        sharedViewModel: SharedViewModel
    ) {
        var selectedYear by rememberSaveable { mutableStateOf<Int?>(PersianCalendar().persianYear) }
        var selectedMonth by rememberSaveable { mutableIntStateOf(PersianCalendar().persianMonth) }
        var showPayAllDialog by remember { mutableStateOf(false) }
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        val yearFilter = if (selectedYear == -1) null else selectedYear.toString()
        val monthFilter =
            if (selectedMonth == -1) null else selectedMonth.toString().padStart(2, '0')

        val debts by sharedViewModel.getDebtsForUnitAndMonth(
            unitId,
            yearFilter,
            monthFilter
        ).collectAsState(initial = emptyList())

        LaunchedEffect(debts) {
            sharedViewModel.addUnpaidDebtListList(debts)
        }

        Scaffold(
            bottomBar = {
                Button(
                    onClick = { showPayAllDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = context.getString(R.string.full_payment),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    // Limit YearMonthSelector width so it doesn't take full width
                    Box(modifier = Modifier.weight(1f)) {
                        YearMonthSelector(
                            selectedYear = selectedYear,
                            onYearChange = { selectedYear = it },
                            selectedMonth = selectedMonth,
                            onMonthChange = { selectedMonth = it }
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            selectedYear = null
                            selectedMonth = 0 // or your no-filter value
                        },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear year and month",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }


                Spacer(modifier = Modifier.height(16.dp))

                if (sharedViewModel.unpaidDebtList.value.isEmpty()) {
                    Text(
                        text = context.getString(R.string.no_debts_recorded),
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    // Show debts
                    sharedViewModel.unpaidDebtList.value.forEach { debt ->
                        DebtItem(debt = debt, onPayment = {
                            coroutineScope.launch {
                                val updatedDebt = debt.copy(paymentFlag = true)
                                sharedViewModel.updateDebt(updatedDebt)
                                sharedViewModel.updateDebtPaymentFlag(debt, true)
                                val amountDouble = debt.amount
                                val success = sharedViewModel.increaseBalanceFund(
                                    buildingId = debt.buildingId,
                                    amountDouble,
                                    fundType = FundType.OPERATIONAL
                                )
                                if (success) {
                                    Toast.makeText(
                                        context, context.getString(R.string.success_pay_tooperational_fund),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context, context.getString(R.string.failed),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        })
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Add Debt item at end of list
//                Card(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clickable { onAddCostClick() },
//                    shape = RoundedCornerShape(8.dp),
//                    colors = CardDefaults.cardColors(
//                        containerColor = MaterialTheme.colorScheme.primaryContainer
//                    )
//                ) {
//                    Row(
//                        modifier = Modifier.padding(16.dp),
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.Center
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Add,
//                            contentDescription = context.getString(R.string.add_new_debt),
//                            tint = MaterialTheme.colorScheme.onPrimaryContainer
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text(
//                            text = context.getString(R.string.add_new_debt),
//                            style = MaterialTheme.typography.bodyLarge,
//                            color = MaterialTheme.colorScheme.onPrimaryContainer
//                        )
//                    }
//                }
            }
        }

        if (showPayAllDialog) {
            AlertDialog(
                onDismissRequest = { showPayAllDialog = false },
                text = {
                    val totalAmount = debts.filter { !it.paymentFlag }.sumOf { it.amount }
                    Text(
                        text = "${context.getString(R.string.total_amount)}: ${
                            formatNumberWithCommas(
                                totalAmount
                            )
                        }",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        debts.filter { !it.paymentFlag }.forEach { debt ->
                            val updatedDebt = debt.copy(paymentFlag = true)
                            sharedViewModel.updateDebt(updatedDebt)
                            sharedViewModel.updateDebtPaymentFlag(debt, true)
                        }
                        Toast.makeText(
                            context,
                            context.getString(R.string.all_debts_marked_paid),
                            Toast.LENGTH_SHORT
                        ).show()
                        showPayAllDialog = false
                    }) {
                        Text(
                            text = context.getString(R.string.insert),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPayAllDialog = false }) {
                        Text(
                            text = context.getString(R.string.cancel),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            )
        }
    }


    @Composable
    fun PaymentsSection(unitId: Long, sharedViewModel: SharedViewModel) {
        val pays by sharedViewModel.getPaysForUnit(unitId).collectAsState(initial = emptyList())
        val context = LocalContext.current

        if (pays.isEmpty()) {
            Text(text = context.getString(R.string.no_costs_recorded))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pays) { pay ->
                    DebtItem(debt = pay, onPayment = {
                        sharedViewModel.updateDebt(pay)
                    })
                }
            }
        }
    }

}

