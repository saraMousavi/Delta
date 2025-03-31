package com.example.delta

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.delta.data.entity.BuildingWithCosts
import com.example.delta.data.entity.BuildingWithIncomes
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.Cost
import com.example.delta.data.entity.Income
import com.example.delta.factory.BuildingsViewModelFactory
import com.example.delta.factory.IncomeViewModelFactory
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BuildingProfileActivity : ComponentActivity() {
    private val incomeViewModel: IncomeViewModel by viewModels {
        IncomeViewModelFactory(application = this.application)
    }

    private val buildingViewModel: BuildingsViewModel by viewModels {
        BuildingsViewModelFactory(application = this.application)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
                val building = intent.getParcelableExtra("BUILDING_DATA") as? Buildings
                setContent {
                    AppTheme {


                            MaterialTheme {
                                building?.let { BuildingProfileScreen(it) }
                            }
                    }
                }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun BuildingProfileScreen(building: Buildings) {
        val tabTitles = listOf("Overview", "Funds", "Units", "Reports", "Members")
        var selectedTab by remember { mutableStateOf(0) }
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text( text = building.name , style = MaterialTheme.typography.titleLarge) },
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
                            text = { Text(title) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    0 -> OverviewTab(building)
                    1 -> FundsTab(building)
                    2 -> UnitsTab(building)
                    3 -> ReportsTab()
                    4 -> MembersTab(building)
                }
            }
        }
    }

    @Composable
    fun OverviewTab(building: Buildings) {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            item {
                Text("Building Name: ${building.name}", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Owner: ${building.ownerName}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Address: ${building.address}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Phone: ${building.phone}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Email: ${building.email}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Postal Code: ${building.postCode}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("National Code: ${building.nationalCode}", style = MaterialTheme.typography.bodyMedium)

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun FundsTab(building: Buildings) {
        val buildingWithIncomes by buildingViewModel.buildingWithIncomes.collectAsState(initial = null)
        val buildingWithCosts by buildingViewModel.buildingWithCosts.collectAsState(initial = null)
        LaunchedEffect(building.buildingId) {
            buildingViewModel.selectBuilding(building.buildingId)
        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Balance Card
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Fund Number: ${building.fundNumber}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Balance: ${building.currentBalance}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Tab Row
            var selectedTab by remember { mutableStateOf(0) }
            val tabTitles = listOf("Income", "Cost")

            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = { Divider(thickness = 2.dp) },
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(text = title.uppercase()) },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Content Area
            when (selectedTab) {
                0 -> IncomeSection(
                    incomes = buildingWithIncomes?.incomes ?: emptyList(),
                    onAddIncome = { buildingViewModel.showIncomeDialog(building.buildingId) }
                )
                1 -> CostSection(
                    costs = buildingWithCosts?.costs ?: emptyList(),
                    onAddCost = { buildingViewModel.showCostDialog(building.buildingId) }
                )
            }
        }

        // Dialog handling
        if (buildingViewModel.showIncomeDialog.value) {
            IncomeDialog(
                building = building,
                onDismiss = { buildingViewModel.hideDialogs() },
                incomeFlow = incomeViewModel.getAllIncome(),
                onConfirm = { income ->
                    buildingViewModel.insertIncome(income)
                    buildingViewModel.hideDialogs()
                }
            )
        }
        if (buildingViewModel.showCostDialog.value) {
//            CostDialog(
//                onDismiss = { viewModel.hideDialogs() },
//                onConfirm = { cost ->
//                    viewModel.insertCost(cost)
//                    viewModel.hideDialogs()
//                }
//            )
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    private fun IncomeSection(
        incomes: List<Income>,
        onAddIncome: () -> Unit
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 80.dp),
                state = rememberLazyListState()
            ) {
                itemsIndexed(incomes) { index, income ->
                    IncomeItem(income = income)
                    if (index < incomes.lastIndex) {
                        Divider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }

                if (incomes.isEmpty()) {
                    item {
                        Text(
                            text = "No incomes recorded",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            ExtendedFloatingActionButton(
                onClick = onAddIncome,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                icon = { Icon(Icons.Default.Add, "Add Income") },
                text = { Text("Add Income") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    private fun CostSection(
        costs: List<Cost>,
        onAddCost: () -> Unit
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 80.dp),
                state = rememberLazyListState()
            ) {
                itemsIndexed(costs) { index, cost ->
                    CostItem(cost = cost)
                    if (index < costs.lastIndex) {
                        Divider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }

                if (costs.isEmpty()) {
                    item {
                        Text(
                            text = "No costs recorded",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            ExtendedFloatingActionButton(
                onClick = onAddCost,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                icon = { Icon(Icons.Default.Add, "Add Cost") },
                text = { Text("Add Cost") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    private fun IncomeItem(income: Income) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = income.incomeName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
//                    Text(
//                        text = income.date.format(DateTimeFormatter.ISO_DATE),
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
                    Text(
                        text = "$${income.amount}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    private fun CostItem(cost: Cost) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = cost.costName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
//                    Text(
//                        text = cost.date.format(DateTimeFormatter.ISO_DATE),
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
                    Text(
                        text = "$${cost.amount}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }




    @Composable
    fun FloatingActionButtons(
        onClick: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            val context = LocalContext.current
            FloatingActionButton(
                onClick = onClick,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.Bottom),
                containerColor = Color(context.getColor(R.color.secondary_color))
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Building")
            }
        }
    }

    @Composable
    fun UnitsTab(building: Buildings) {
//        LazyColumn(modifier = Modifier.padding(16.dp)) {
//            items(building.incomes) { income ->
//                Card(
//                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
//                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//                ) {
//                    Row(
//                        modifier = Modifier.padding(16.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(income, style = MaterialTheme.typography.bodyMedium)
//                    }
//                }
//            }
//        }
    }

    @Composable
    fun ReportsTab() {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Reports will be displayed here.", style = MaterialTheme.typography.bodyMedium)
        }
    }

    @Composable
    fun MembersTab(building: Buildings) {
//        LazyColumn(modifier = Modifier.padding(16.dp)) {
//            items(building.members) { member ->
//                Card(
//                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
//                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//                ) {
//                    Row(
//                        modifier = Modifier.padding(16.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(member, style = MaterialTheme.typography.bodyMedium)
//                    }
//                }
//            }
//        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun IncomeDialog(
        building: Buildings,
        incomeFlow: Flow<List<Income>>,
        onDismiss: () -> Unit,
        onConfirm: (Income) -> Unit
    ) {
        val description = remember { mutableStateOf("") }
        val amount = remember { mutableStateOf("") }
        val selectedIncomeName = remember { mutableStateOf("") }
        var expanded by remember { mutableStateOf(false) }
        val incomes by incomeFlow.collectAsState(initial = emptyList())
        val selectedIncome = remember { mutableStateOf<Income?>(null) }

        Dialog(onDismissRequest = onDismiss) {
            Surface(
                modifier = Modifier
                    .padding(16.dp)
                    .width(IntrinsicSize.Min),
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Add Income",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = amount.value,
                        onValueChange = { amount.value = it },
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )

                    // ComboBox for incomeName
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        TextField(
                            value = selectedIncomeName.value,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Income Name") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                            },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor() // Align dropdown with TextField
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            incomes.forEach { income ->
                                DropdownMenuItem(
                                    text = { Text(income.incomeName) },
                                    onClick = {
                                        selectedIncome.value = income
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        val context = LocalContext.current
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                val income = Income(
                                    buildingId = building.buildingId,
                                    amount = (amount.value.toDoubleOrNull() ?: 0.0),
                                    incomeName = description.value,
//                                    date = LocalDate.now(),
                                    currency =  LocalDate.now().toString()
                                )
                                onConfirm(income)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
                            )
                        ) {
                            Text(
                                text = context.getString(R.string.insert),
                                modifier = Modifier.padding(2.dp),
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
            }
        }
    }






}




