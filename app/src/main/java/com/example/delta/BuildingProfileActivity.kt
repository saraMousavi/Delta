package com.example.delta

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Debts
import com.example.delta.data.entity.Earnings
import com.example.delta.data.entity.Units
import com.example.delta.factory.BuildingsViewModelFactory
import com.example.delta.factory.CostViewModelFactory
import com.example.delta.factory.EarningsViewModelFactory
import com.example.delta.viewmodel.BuildingsViewModel
import com.example.delta.viewmodel.CostViewModel
import com.example.delta.viewmodel.EarningsViewModel
import com.example.delta.viewmodel.SharedViewModel
import com.example.delta.viewmodel.UnitsViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.collections.forEach
import kotlin.getValue

class BuildingProfileActivity : ComponentActivity() {
    private val earningsViewModel: EarningsViewModel by viewModels {
        EarningsViewModelFactory(application = this.application)
    }

    private val costsViewModel: CostViewModel by viewModels {
        CostViewModelFactory(application = this.application)
    }

    private val buildingViewModel: BuildingsViewModel by viewModels {
        BuildingsViewModelFactory(application = this.application)
    }

    val sharedViewModel: SharedViewModel by viewModels()
    val unitsViewModel: UnitsViewModel by viewModels()
    var buildingTypeName: String = ""
    var buildingUsageName: String = ""

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        val building = intent.getParcelableExtra<Parcelable>("BUILDING_DATA") as? Buildings

        buildingTypeName = intent.getStringExtra("BUILDING_TYPE_NAME") ?: "Unknown"
        buildingUsageName = intent.getStringExtra("BUILDING_USAGE_NAME") ?: "Unknown"
        Log.d("BuildingRetrieve", "Province: ${buildingUsageName}, State: $buildingTypeName")
        setContent {
            AppTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MaterialTheme {
                        building?.let { BuildingProfileScreen(it) }
                    }
                }
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun BuildingProfileScreen(building: Buildings) {
        var context = LocalContext.current
        val tabTitles = listOf(
            context.getString(R.string.overview),
            context.getString(R.string.owners),
            context.getString(R.string.units),
            context.getString(R.string.tenants),
            context.getString(R.string.funds),
            context.getString(R.string.reports)
        )
        var selectedTab by remember { mutableIntStateOf(0) }
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = building.name,
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
                    0 -> OverviewTab(building)
                    1 -> OwnersTab(building, sharedViewModel)
                    2 -> UnitsTab(building, sharedViewModel, unitsViewModel)
                    3 -> TenantsTab(building, sharedViewModel)  // Add Tenant Tab Content
                    4 -> FundsTab(building)
                    5 -> ReportsTab()
                }
            }
        }
    }


    @Composable
    fun OverviewTab(building: Buildings) {
        val context = LocalContext.current

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(context.getColor(R.color.primary_color)) // Example: Light blue background
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "${context.getString(R.string.building_name)}: ${building.name}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${context.getString(R.string.street)}: ${building.street}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${context.getString(R.string.post_code)}: ${building.postCode}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(context.getColor(R.color.primary_color)) // Example: Light blue background
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "${context.getString(R.string.province)}: ${building.province}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${context.getString(R.string.state)}: ${building.state}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(context.getColor(R.color.primary_color)) // Example: Light blue background
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "${context.getString(R.string.building_type)}: $buildingTypeName",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${context.getString(R.string.building_usage)}: $buildingUsageName",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(context.getColor(R.color.primary_color)) // Example: Light blue background
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = context.getString(R.string.shared_things),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        building.utilities.forEach { utility ->
                            Text(
                                text = utility,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun FundsTab(building: Buildings) {
        var context = LocalContext.current
        val buildingWithEarnings =
            earningsViewModel.fetchAndProcessEarnings(building.buildingId)
                .collectAsState(initial = null)
        val buildingWithCosts =
            costsViewModel.fetchAndProcessCosts(building.buildingId).collectAsState(initial = null)
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
                        text = "${context.getString(R.string.fund_lbl)}: ${building.fund}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // Tab Row
            var selectedTab by remember { mutableIntStateOf(0) }
            val tabTitles =
                listOf(context.getString(R.string.incomes), context.getString(R.string.costs))

            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {
                    HorizontalDivider(
                        thickness = 2.dp
                    )
                },
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .background(
                                if (selectedTab == index) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            )
                            .padding(vertical = 8.dp),
                        text = {
                            Text(
                                text = title.uppercase(),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (selectedTab == index) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    )
                }
            }


            // Content Area
            when (selectedTab) {
                0 -> EarningsSection(
                    earnings = buildingWithEarnings.value ?: emptyList(),
                    onAddEarnings = { buildingViewModel.showEarningsDialog(building.buildingId) }
                )

                1 -> CostSection(
                    costs = buildingWithCosts.value ?: emptyList(),
                    onAddCost = { buildingViewModel.showCostDialog(building.buildingId) }
                )
            }
        }

        // Dialog handling
        if (buildingViewModel.showEarningsDialog.value) {
            EarningsDialog(
                building = building,
                onDismiss = { buildingViewModel.hideDialogs() },
                onConfirm = { earning ->
                    buildingViewModel.insertEarnings(earning)
                    buildingViewModel.hideDialogs()
                }
            )
        }
        if (buildingViewModel.showCostDialog.value) {
            CostDialog(
                building = building,
                onDismiss = { buildingViewModel.hideDialogs() },
                costsFlow = costsViewModel.getAllMenuCost(),
                onConfirm = { cost -> buildingViewModel.insertCost(cost) },
                onInsertDebt = { debt -> buildingViewModel.insertDebt(debt) }
            )
        }


    }

    @Composable
    fun TenantsTab(building: Buildings, sharedViewModel: SharedViewModel) {
        var showTenantDialog by remember { mutableStateOf(false) }
        val context = LocalContext.current
//        val tenants by sharedViewModel.tenantsList
//            .collectAsState(initial = emptyList())  // Fetch tenants for the building
        val tenants by sharedViewModel.getTenantsForBuilding(building.buildingId)
            .collectAsState(initial = emptyList())
        Log.d("tenants in tenant tab", tenants.toString())
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                items(tenants) { tenant ->
                    TenantItem(tenants = tenant)
                }
            }

            FloatingActionButton(
                onClick = { showTenantDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = Color(context.getColor(R.color.secondary_color)),
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(Icons.Filled.Add, "Add")
            }

            if (showTenantDialog) {
                TenantDialog(
                    sharedViewModel = sharedViewModel,
                    units = sharedViewModel.unitsList,
                    onDismiss = { showTenantDialog = false },
                    onAddTenant = { newTenant, selectedUnit ->
                        sharedViewModel.saveTenantWithUnit(newTenant, selectedUnit)
                        showTenantDialog = false
                    }
                )
            }
        }
    }


    @Composable
    fun UnitsTab(
        building: Buildings,
        sharedViewModel: SharedViewModel,
        unitsViewModel: UnitsViewModel
    ) {
        var showUnitDialog by remember { mutableStateOf(false) }
        var context = LocalContext.current
        val units by sharedViewModel.getUnitsForBuilding(building.buildingId)
            .collectAsState(initial = emptyList())
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                items(units) { unit ->
                    UnitItem(
                        unit = unit,
                        sharedViewModel = sharedViewModel,
                        unitsViewModel = unitsViewModel
                    )
                }
            }

            FloatingActionButton(
                onClick = { showUnitDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = Color(context.getColor(R.color.secondary_color)),
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(Icons.Filled.Add, "Add")
            }

            if (showUnitDialog) {
                UnitDialog(
                    onDismiss = { showUnitDialog = false },
                    onAddUnit = { newUnit ->
                        newUnit.buildingId = building.buildingId
//                        sharedViewModel.addUnit(newUnit)
                        showUnitDialog = false
                    }
                )
            }
        }
    }


    @Composable
    fun ReportsTab() {
        Text(text = "Reports Tab Content")
    }

    @Composable
    fun OwnersTab(building: Buildings, sharedViewModel: SharedViewModel) {
        var showOwnerDialog by remember { mutableStateOf(false) }
        val context = LocalContext.current
        val owners by sharedViewModel.getOwnersForBuilding(building.buildingId)
            .collectAsState(initial = emptyList())
        Log.d("owners in ownerstab", owners.toString())
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                items(owners) { owner ->
                    OwnerItem(owner = owner)
                }
            }

            FloatingActionButton(
                onClick = { showOwnerDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = Color(context.getColor(R.color.secondary_color)),
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(Icons.Filled.Add, "Add")
            }

            if (showOwnerDialog) {
                OwnerDialog(
                    units = sharedViewModel.unitsList,
                    onDismiss = { showOwnerDialog = false },
                    onAddOwner = { newOwner, selectedUnits ->
                        Log.d("newOwner", newOwner.toString())
                        Log.d("selectedUnits", selectedUnits.toString())
                        sharedViewModel.saveOwnerWithUnits(newOwner, selectedUnits)
                        showOwnerDialog = false
                    },
                    sharedViewModel = sharedViewModel
                )
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    private fun EarningsSection(
        earnings: List<Earnings>,
        onAddEarnings: () -> Unit
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 80.dp),
                state = rememberLazyListState()
            ) {
                itemsIndexed(earnings) { index, earning ->
                    EarningsItem(earnings = earning)
                    if (index < earnings.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }

                if (earnings.isEmpty()) {
                    item {
                        Text(
                            text = LocalContext.current.getString(R.string.no_earnings_recorded),
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
                onClick = onAddEarnings,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                icon = { Icon(Icons.Default.Add, "Add Earnings") },
                text = { Text("Add Earnings") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    private fun CostSection(
        costs: List<Costs>,
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
                    CostItem(costs = cost)
                    if (index < costs.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }

                if (costs.isEmpty()) {
                    item {
                        Text(
                            text = LocalContext.current.getString(R.string.no_costs_recorded),
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
                icon = { Icon(Icons.Default.Add, "Add Costs") },
                text = { Text("Add Costs") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }

    @Composable
    fun EarningsItem(earnings: Earnings) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Title: ${earnings.earningsName}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Amount: ${earnings.amount}",
                    style = MaterialTheme.typography.bodyMedium
                )

            }
        }
    }

    @Composable
    fun CostItem(costs: Costs) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Title: ${costs.costName}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
//                Text(
////                    text = "Amount: ${costs.amount}",
////                    style = MaterialTheme.typography.bodyMedium
//                )

            }
        }
    }

    @Composable
    fun EarningsDialog(
        building: Buildings,
        onDismiss: () -> Unit,
        onConfirm: (Earnings) -> Unit
    ) {

        var title by remember { mutableStateOf("") }
        var amount by remember { mutableStateOf("") }

        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Add Earnings for ${building.name}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    TextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                val newEarning = Earnings(
                                    buildingId = building.buildingId,
                                    earningsName = title,
                                    amount = amount.toDoubleOrNull() ?: 0.0,
                                    currency = ""
                                )
                                onConfirm(newEarning)
                            },
                            enabled = title.isNotBlank() && amount.isNotBlank()
                        ) {
                            Text("Confirm")
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun CostDialog(
        building: Buildings,
        onDismiss: () -> Unit,
        costsFlow: Flow<List<Costs>>,
        onConfirm: suspend (Costs) -> Unit,
        onInsertDebt: suspend (Debts) -> Unit
    ) {
        var title by remember { mutableStateOf("") }
        var amount by remember { mutableStateOf("") }
        var isDebt by remember { mutableStateOf(false) }

        val scope = rememberCoroutineScope()

        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Add Cost for ${building.name}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    TextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Is Debt")
                        Spacer(modifier = Modifier.width(8.dp))
//                    Switch(
//                        checked = isDebt,
//                        onCheckedChange = { isDebt = it }
//                    )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
//                                val newCost = Costs(
//                                    buildingId = building.buildingId,
//                                    title = title,
//                                    amount = amount.toDoubleOrNull() ?: 0.0,
//                                    date = LocalDate.now().toString()
//                                )
//                                scope.launch {
//                                    val costId = onConfirm(newCost)
//
//                                    if (isDebt) {
//                                        val newDebt = Debts(
//                                            costId = costId,
//                                            buildingId = building.buildingId,
//                                            amount = amount.toDoubleOrNull() ?: 0.0,
//                                            date = LocalDate.now().toString()
//                                        )
//                                        onInsertDebt(newDebt)
//                                    }
//                                }
                            },
                            enabled = title.isNotBlank() && amount.isNotBlank()
                        ) {
                            Text("Confirm")
                        }
                    }
                }
            }
        }
    }
}


