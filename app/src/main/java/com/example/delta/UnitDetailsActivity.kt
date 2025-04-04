package com.example.delta

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.delta.data.entity.Debts
import com.example.delta.data.entity.Units
import com.example.delta.factory.CostViewModelFactory
import com.example.delta.factory.DebtsViewModelFactory
import com.example.delta.factory.UnitsViewModelFactory
import com.example.delta.viewmodel.CostViewModel
import com.example.delta.viewmodel.DebtsViewModel
import com.example.delta.viewmodel.UnitsViewModel
import kotlin.getValue


class UnitDetailsActivity : ComponentActivity() {

    private val unitsViewModel: UnitsViewModel by viewModels {
        UnitsViewModelFactory(application = this.application)
    }


    private val debtsViewModel: DebtsViewModel by viewModels {
        DebtsViewModelFactory(application = this.application)
    }

    private val costsViewModel: CostViewModel by viewModels {
        CostViewModelFactory(application = this.application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val unit = intent.getParcelableExtra("UNIT_DATA") as? Units
        setContent {
            AppTheme {
                MaterialTheme{
                    unit?.let { UnitDetailsScreen(it) }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun UnitDetailsScreen(unit: Units) {
        var tabIndex by remember { mutableStateOf(0) }

        val tabs = listOf("Overview", "Debt", "Payments", "Report")

        Scaffold(
            topBar = {
                TabRow(selectedTabIndex = tabIndex) {
                    tabs.forEachIndexed { index, text ->
                        Tab(
                            selected = tabIndex == index,
                            onClick = { tabIndex = index },
                            text = { Text(text) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (tabIndex) {
                    0 -> OverviewSection(unitId = unit.unitId)
                    1 -> DebtSection(unitId = unit.unitId)
                    2 -> PaymentsSection(unitId = unit.unitId)
                    3 -> ReportSection(unitId = unit.unitId)
                }
            }
        }
    }

    @Composable
    fun OverviewSection(unitId: Long) {
        Text(text = "Overview for Unit $unitId")
    }

    @Composable
    fun ReportSection(unitId: Long) {
        Text(text = "Reports for Unit $unitId")
    }

    @Composable
    fun DebtSection(unitId: Long) {
        val debts by debtsViewModel.fetchAndProcessDebts(unitId).collectAsState(initial = emptyList())

        Column {
            if (debts.isEmpty()) {
                Text("No debts found.")
            } else {
                debts.forEach { debt ->
                    DebtItem(debt = debt, onPayment = {
                        debt.paymentFlag = true
                        debtsViewModel.updateDebt(debt)
                    })
                }
            }
        }
    }

    @Composable
    fun DebtItem(debt: Debts, onPayment: () -> Unit) {
        Row {
            var cost = costsViewModel.getCost(debt.costId)
            Text(text = cost.costName)
            if(debt.paymentFlag) {
                Button(onClick = onPayment) {
                    Text("Pay")
                }
            } else {
                //@TODO text(payment done)
            }
        }
    }

    @Composable
    fun PaymentsSection(unitId: Long) {
        val pays by debtsViewModel.fetchAndProcessPays(unitId).collectAsState(initial = emptyList())

        Column {
            if (pays.isEmpty()) {
                Text("No pays found.")
            } else {
                pays.forEach { pay ->
                    DebtItem(debt = pay, onPayment = {
                    })
                }
            }
        }
    }

}

