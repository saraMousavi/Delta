package com.example.delta

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import androidx.compose.foundation.lazy.items
import com.example.delta.data.entity.Debts
import com.example.delta.data.entity.TenantWithRelation
import com.example.delta.data.entity.Units
import com.example.delta.enums.PaymentLevel
import com.example.delta.enums.Responsible
import com.example.delta.sharedui.DebtItem
import com.example.delta.viewmodel.SharedViewModel
import ir.hamsaa.persiandatepicker.util.PersianCalendar
import kotlin.getValue

class UnitDetailsActivity : ComponentActivity() {

    // Use SharedViewModel for shared state
    val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val unit = intent.getParcelableExtra("UNIT_DATA") as? Units

        setContent {
            AppTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MaterialTheme {
                        unit?.let { UnitDetailsScreen(it) }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun UnitDetailsScreen(unit: Units) {
        var context = LocalContext.current

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
                            text = "${context.getString(R.string.unit_name)} : ${unit.unitNumber}",
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
                    0 -> OverviewSection(unit = unit)
                    1 -> DebtSection(
                        unitId = unit.unitId,
                        sharedViewModel = sharedViewModel,
                        onAddCostClick = { showAddCostDialog = true }
                    ) // Pass SharedViewModel
                    2 -> PaymentsSection(
                        unitId = unit.unitId,
                        sharedViewModel = sharedViewModel
                    ) // Pass SharedViewModel
                    3 -> ReportSection(unitId = unit.unitId)
                }
                if (showAddCostDialog) {
                    AddCostDialog(
                        buildingId = unit.buildingId ?: 0,
                        sharedViewModel = sharedViewModel,
                        onDismiss = { showAddCostDialog = false },
                        onSave = { selectedCost, amount, period, fundFlag, calculatedMethod, calculatedUnitMethod, responsible, selectedUnits, selectedOwners, dueDate, fundMinus ->
                            // Insert cost and debts using selectedCost info

                            sharedViewModel.insertDebtPerNewCost(
                                buildingId = unit.buildingId ?: 0,
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
        unitId: Long, sharedViewModel: SharedViewModel,
        onAddCostClick: () -> Unit
    ) {

        var selectedYear by rememberSaveable { mutableIntStateOf(PersianCalendar().persianYear) }
        var selectedMonth by rememberSaveable { mutableIntStateOf(PersianCalendar().persianMonth) }


        val debts by sharedViewModel.getDebtsForUnitAndMonth(
            unitId,
            selectedYear.toString(),
            selectedMonth.toString().padStart(2, '0')
        ).collectAsState(initial = emptyList())

        LaunchedEffect(debts) {
            sharedViewModel.addUnpaidDebtListList(debts)
        }


        var context = LocalContext.current

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onAddCostClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Cost")
                }
            }
        ) { innerPadding ->
            // Your debt list content here, e.g. LazyColumn
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // debt items
                YearMonthSelector(
                    selectedYear = selectedYear,
                    onYearChange = { selectedYear = it },
                    selectedMonth = selectedMonth,
                    onMonthChange = { selectedMonth = it }
                )
                Log.d("slectedyear", selectedYear.toString())
                Log.d("selectedmonth", selectedMonth.toString())

                if (sharedViewModel.unpaidDebtList.value.isEmpty()) {
                    Text(
                        text = context.getString(R.string.no_debts_recorded),
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    sharedViewModel.unpaidDebtList.value.forEach { debt ->
                        DebtItem(debt = debt, onPayment = {
                            Log.d("debt", debt.toString())
                            val updatedDebt = debt.copy(paymentFlag = true)
                            sharedViewModel.updateDebt(updatedDebt) // Use SharedViewModel to update
                            sharedViewModel.updateDebtPaymentFlag(debt, true)
                            Toast.makeText(
                                context, context.getString(R.string.success_pay),
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                    }
                }

            }
        }

    }


    @Composable
    fun PaymentsSection(unitId: Long, sharedViewModel: SharedViewModel) { // Receive SharedViewModel
        val pays by sharedViewModel.getPaysForUnit(unitId).collectAsState(initial = emptyList())

        Column {
            if (pays.isEmpty()) {
                Text(text = LocalContext.current.getString(R.string.no_costs_recorded))
            } else {
                pays.forEach { pay ->
                    DebtItem(debt = pay, onPayment = {
                        sharedViewModel.updateDebt(pay)
                    })
                }
            }
        }
    }
}

