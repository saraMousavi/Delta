package com.example.delta

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.delta.data.entity.TenantWithRelation
import com.example.delta.data.entity.Units
import com.example.delta.enums.PaymentLevel
import com.example.delta.enums.Responsible
import com.example.delta.sharedui.DebtItem
import com.example.delta.viewmodel.SharedViewModel
import ir.hamsaa.persiandatepicker.util.PersianCalendar

class TenantsDetailsActivity : ComponentActivity() {

    // Use SharedViewModel for shared state
    val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val unitId = intent.getLongExtra("UNIT_DATA", -1L)
        Log.d("unitId", unitId.toString())
        setContent {
            AppTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MaterialTheme {
                        unitId.let { this.UnitDetailsScreen(it) }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun UnitDetailsScreen(unitId: Long) {
        var context = LocalContext.current

        val unit = sharedViewModel.getUnit(unitId).collectAsState(initial = null)
        if (unit.value != null) {
        val tabTitles = listOf(
            context.getString(R.string.overview),
            context.getString(R.string.debt),
            context.getString(R.string.payments),
            context.getString(R.string.report)
        )
        var selectedTab by remember { mutableIntStateOf(0) }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "${context.getString(R.string.unit_name)} : ${unit.value!!.unitNumber}",
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
                var showAddCostDialog by remember { mutableStateOf(false) }
                when (selectedTab) {
                    0 -> OverviewSection(unit = unit.value!!)
                    1 -> DebtSection(
                        unitId = unit.value!!.unitId,
                        sharedViewModel = sharedViewModel,
                        onAddCostClick = { showAddCostDialog = true }
                    ) // Pass SharedViewModel
                    2 -> PaymentsSection(
                        unitId = unit.value!!.unitId,
                        sharedViewModel = sharedViewModel
                    ) // Pass SharedViewModel
                    3 -> ReportSection(unitId = unit.value!!.unitId)
                }
                if (showAddCostDialog) {
                    AddCostDialog(
                        buildingId = unit.value!!.buildingId ?: 0,
                        sharedViewModel = sharedViewModel,
                        onDismiss = { showAddCostDialog = false },
                        onSave = { selectedCost, amount, period, fundFlag, calculatedMethod, calculatedUnitMethod, responsible, selectedUnits, selectedOwners, dueDate, fundMinus ->
                            // Insert cost and debts using selectedCost info

                            sharedViewModel.insertDebtPerNewCost(
                                buildingId = unit.value!!.buildingId ?: 0,
                                amount = amount,
                                name = selectedCost.costName,
                                period = period,
                                dueDate = dueDate,
                                fundFlag = fundFlag,
                                paymentLevel = PaymentLevel.UNIT,
                                calculateMethod = calculatedMethod,
                                calculatedUnitMethod = calculatedUnitMethod,
                                responsible = Responsible.TENANT,
                                selectedUnitIds = selectedUnits.map { it },
                                fundMinus = fundMinus
                            )

                            showAddCostDialog = false
                        }
                    )
                }
            }
        }
        }
    }

    @Composable
    fun OverviewSection(unit: Units) {
        val context = LocalContext.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Unit info card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
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
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tenants section
            Text(
                text = context.getString(R.string.tenants),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            val tenantsWithRelation by sharedViewModel
                .getTenantUnitRelationshipsOfUnit(unit.unitId)
                .collectAsState(initial = emptyList())
            if (tenantsWithRelation.isEmpty()) {
                Text(
                    text = context.getString(R.string.no_tenants_found),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // List tenants
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(tenantsWithRelation) { tenantWithRelation ->
                        TenantCard(tenantWithRelation)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

            }
        }
    }

    @Composable
    fun TenantCard(tenantWithRelation: TenantWithRelation) {
        val context = LocalContext.current
        val tenant = tenantWithRelation.tenant
        val relation = tenantWithRelation.crossRef

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "${tenant.firstName} ${tenant.lastName}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${context.getString(R.string.phone_number)}: ${tenant.phoneNumber}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${context.getString(R.string.mobile_number)}: ${tenant.mobileNumber}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${context.getString(R.string.email)}: ${tenant.email}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    Text(
                        text = "${context.getString(R.string.start_date)}: ${relation.startDate}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "${context.getString(R.string.end_date)}: ${relation.endDate}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${context.getString(R.string.status)}: ${relation.status}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }

    @Composable
    fun ReportSection(unitId: Long) {
        Text(text = "Reports for Unit $unitId")
    }

    @Composable
    fun DebtSection(
        unitId: Long,
        sharedViewModel: SharedViewModel,
        onAddCostClick: () -> Unit
    ) {
        var selectedYear by rememberSaveable { mutableStateOf<Int?>(PersianCalendar().persianYear) }
        var selectedMonth by rememberSaveable { mutableStateOf(PersianCalendar().persianMonth) }
        var showPayAllDialog by remember { mutableStateOf(false) }
        val context = LocalContext.current

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
                            val updatedDebt = debt.copy(paymentFlag = true)
                            sharedViewModel.updateDebt(updatedDebt)
                            sharedViewModel.updateDebtPaymentFlag(debt, true)
                            Toast.makeText(
                                context, context.getString(R.string.success_pay),
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Add Debt item at end of list
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAddCostClick() },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = context.getString(R.string.add_new_debt),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = context.getString(R.string.add_new_debt),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
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

