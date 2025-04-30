package com.example.delta

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.example.delta.data.entity.Debts
import com.example.delta.data.entity.Units
import com.example.delta.enums.FundFlag
import com.example.delta.enums.PaymentLevel
import com.example.delta.enums.Period
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
                        onDismiss = { showAddCostDialog = false },
                        onSave = { name, amount, period ->
                            // Insert cost and debts
                            sharedViewModel.insertDebtPerNewCost(
                                buildingId = unit.buildingId ?: 0,
                                amount = amount, name = name, period = period,
                                fundFlag = FundFlag.NO_EFFECT, paymentLevel = PaymentLevel.UNIT)
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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer // Use MaterialTheme color
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "${context.getString(R.string.unit)}: ${unit.unitNumber}",
                        style = MaterialTheme.typography.bodyLarge
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
        }
    }

    @Composable
    fun ReportSection(unitId: Long) {
        Text(text = "Reports for Unit $unitId")
    }

    @Composable
    fun DebtSection(unitId: Long, sharedViewModel: SharedViewModel,
                    onAddCostClick: () -> Unit) {

        var selectedYear by rememberSaveable { mutableIntStateOf(PersianCalendar().persianYear) }
        var selectedMonth by rememberSaveable { mutableIntStateOf(PersianCalendar().persianMonth + 1) }


//        val debts by sharedViewModel.getDebtsOneUnit(unitId).collectAsState(initial = emptyList())
        val debts by sharedViewModel.getDebtsForUnitAndMonth(unitId, selectedYear.toString(), selectedMonth.toString().padStart(2, '0'))
            .collectAsState(initial = emptyList())
        Log.d("debt", debts.toString())


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

                if (debts.isEmpty()) {
                    Text(
                        text = context.getString(R.string.no_debts_recorded),
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    debts.forEach { debt ->
                        DebtItem(debt = debt, onPayment = {
                            Log.d("debt", debt.toString())
                            val updatedDebt = debt.copy(paymentFlag = true)
                            sharedViewModel.updateDebt(updatedDebt) // Use SharedViewModel to update
                        })
                    }
                }

            }
        }

    }

    @Composable
    fun DebtItem(debt: Debts, onPayment: () -> Unit) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "${LocalContext.current.getString(R.string.title)}: ${debt.description}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "${LocalContext.current.getString(R.string.amount)}: ${debt.amount}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "${LocalContext.current.getString(R.string.due)}: ${debt.dueDate}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                Log.d("debt.paymentFlag", debt.paymentFlag.toString())
                if (debt.paymentFlag) {
                    Text(
                        text = LocalContext.current.getString(R.string.payment_done),
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    Button(onClick = onPayment) {
                        Text(
                            text = LocalContext.current.getString(R.string.payment),
                            style = MaterialTheme.typography.bodyLarge
                        )
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

