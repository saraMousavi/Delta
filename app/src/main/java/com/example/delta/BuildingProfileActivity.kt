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
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Debts
import com.example.delta.data.entity.Earnings
import com.example.delta.data.entity.Units
import com.example.delta.factory.BuildingsViewModelFactory
import com.example.delta.factory.CostViewModelFactory
import com.example.delta.factory.EarningsViewModelFactory
import com.example.delta.factory.UnitsViewModelFactory
import com.example.delta.viewmodel.BuildingsViewModel
import com.example.delta.viewmodel.CostViewModel
import com.example.delta.viewmodel.EarningsViewModel
import com.example.delta.viewmodel.UnitsViewModel
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import kotlin.collections.forEach

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

    private val unitsViewModel: UnitsViewModel by viewModels {
        UnitsViewModelFactory(application = this.application)
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
        val buildingWithEarnings = earningsViewModel.fetchAndProcessEarnings(building.buildingId).collectAsState(initial = null)
        val buildingWithCosts = costsViewModel.fetchAndProcessCosts(building.buildingId).collectAsState(initial = null)
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
            val tabTitles = listOf("Earnings", "Costs")

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
                0 -> EarningsSection(
                    earnings = buildingWithEarnings?.value ?: emptyList(),
                    onAddEarnings = { buildingViewModel.showEarningsDialog(building.buildingId) }
                )
                1 -> CostSection(
                    costs = buildingWithCosts?.value ?: emptyList(),
                    onAddCost = { buildingViewModel.showCostDialog(building.buildingId) }
                )
            }
        }

        // Dialog handling
        if (buildingViewModel.showEarningsDialog.value) {
            EarningsDialog(
                building = building,
                onDismiss = { buildingViewModel.hideDialogs() },
                earningsFlow = earningsViewModel.getAllMenuEarnings(),
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
                onConfirm = { cost ->
                    buildingViewModel.insertCost(cost)
                    buildingViewModel.hideDialogs()
                },
                onInsertDebt = { debt ->
                    buildingViewModel.insertDebt(debt)
                }
            )
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
                        Divider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }

                if (earnings.isEmpty()) {
                    item {
                        Text(
                            text = "No earnings recorded",
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
                icon = { Icon(Icons.Default.Add, "Add Costs") },
                text = { Text("Add Costs") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    private fun EarningsItem(earnings: Earnings) {
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
                    text = earnings.earningsName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${earnings.amount}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    private fun CostItem(costs: Costs) {
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
                    text = costs.costName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${costs.amount}",
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

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun UnitsTab(building: Buildings) {
        val buildingWithUnits = unitsViewModel.fetchAndProcessUnits(building.buildingId).collectAsState(initial = null)

        Column {
            UnitsList(units = buildingWithUnits?.value ?: emptyList(),
                onAddUnits = { buildingViewModel.showUnitsDialog(building.buildingId)})

        }
        // Dialog handling
        if (buildingViewModel.showUnitsDialog.value) {
            UnitsDialog(
                building = building,
                onDismiss = { buildingViewModel.hideDialogs() },
                onConfirm = { unit ->
                    buildingViewModel.insertUnits(unit)
                    buildingViewModel.hideDialogs()
                },
                onInsertDebt = { debt ->
                    buildingViewModel.insertDebt(debt)
                }
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    private fun UnitsList(
        units: List<Units>,
        onAddUnits: () -> Unit
    ) {
        val context = LocalContext.current
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 80.dp),
                state = rememberLazyListState()
            ) {
                itemsIndexed(units) { index, unit ->
                    UnitItem(unit = unit){
                        val intent = Intent(context, UnitDetailsActivity::class.java).apply {
                            putExtra("UNIT_DATA", unit as Parcelable)
                        }
                        context.startActivity(intent)
                    }
                    if (index < units.lastIndex) {
                        Divider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }

                if (units.isEmpty()) {
                    item {
                        Text(
                            text = "No units recorded",
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
                onClick = onAddUnits,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                icon = { Icon(Icons.Default.Add, "Add Units") },
                text = { Text("Add Units") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    private fun UnitItem(unit: Units, onClick: () -> Unit) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable{onClick},
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = unit.unitNumber.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
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
    fun EarningsDialog(
        building: Buildings,
        earningsFlow: Flow<List<Earnings>>,
        onDismiss: () -> Unit,
        onConfirm: (Earnings) -> Unit
    ) {
        val description = remember { mutableStateOf("") }
        val amount = remember { mutableStateOf("") }
        val earnings by earningsFlow.collectAsState(initial = emptyList())
        var selectedEarnings by remember { mutableStateOf<Earnings?>(null) }

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
                        text = "Add Earnings",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = amount.value,
                        onValueChange = { amount.value = it },
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )

                    // ComboBox for earningName
                    val context = LocalContext.current
                    ExposedDropdownMenuBoxExample(
                        items = earnings,
                        selectedItem = selectedEarnings,
                        onItemSelected = { selectedEarnings = it },
                        label = context.getString(R.string.costs),
                        itemLabel = { it.earningsName }
                    )

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
                                val amountValue = amount.value
                                // Convert to ASCII digits
                                val asciiAmount = amountValue.replace("۰", "0").replace("۱", "1").replace("۲", "2")
                                    .replace("۳", "3").replace("۴", "4").replace("۵", "5")
                                    .replace("۶", "6").replace("۷", "7").replace("۸", "8")
                                    .replace("۹", "9")
                                if (asciiAmount.isNotEmpty()) {
                                    val amountDouble = asciiAmount.toDoubleOrNull() ?: 0.0

                                    val earnings = Earnings(
                                        buildingId = building.buildingId,
                                        amount = amountDouble,
                                        earningsName = description.value,
//                                    date = LocalDate.now(),
                                        currency = LocalDate.now().toString()
                                    )
                                    onConfirm(earnings)
                                    onDismiss()
                                } else {
                                    Log.d("EarningsDialog", "Amount is empty")
                                    // Handle empty input
                                }
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

    @OptIn(ExperimentalMaterial3Api::class)
    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun CostDialog(
        building: Buildings,
        costsFlow: Flow<List<Costs>>,
        onDismiss: () -> Unit,
        onConfirm: (Costs) -> Unit,
        onInsertDebt: (Debts) -> Unit
    ) {
        val amount = remember { mutableStateOf("") }
        val costs by costsFlow.collectAsState(initial = emptyList())
        var selectedCosts by remember { mutableStateOf<Costs?>(null) }
        val units by unitsViewModel.fetchAndProcessUnits(building.buildingId).collectAsState(initial = emptyList())
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
                        text = "Add Cost",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = amount.value,
                        onValueChange = { amount.value = it },
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )

                    // ComboBox for earningName
                    val context = LocalContext.current
                    ExposedDropdownMenuBoxExample(
                        items = costs,
                        selectedItem = selectedCosts,
                        onItemSelected = { selectedCosts = it },
                        label = context.getString(R.string.costs),
                        itemLabel = { it.costName }
                    )
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
                                val amountValue = amount.value
                                // Convert to ASCII digits
                                val asciiAmount = amountValue.replace("۰", "0").replace("۱", "1").replace("۲", "2")
                                    .replace("۳", "3").replace("۴", "4").replace("۵", "5")
                                    .replace("۶", "6").replace("۷", "7").replace("۸", "8")
                                    .replace("۹", "9")
                                if (asciiAmount.isNotEmpty()) {
                                    val amountDouble = asciiAmount.toDoubleOrNull() ?: 0.0


                                    val costs = Costs(
                                        buildingId = building.buildingId,
                                        amount = amountDouble,
                                        costName = selectedCosts?.costName ?: "",
                                        currency = "USD" // Example currency
                                    )
                                    onConfirm(costs)
                                    units.forEach { unit ->
                                        val debt = Debts(
                                            unitId = unit.unitId,
                                            costId = costsViewModel.getLastCostId(),
                                            description = "",
                                            dueDate = "",
                                            paymentFlag = false
                                        )
                                        onInsertDebt(debt)
                                    }
                                    onDismiss()
                                    // Proceed with costs
                                } else {
                                    Log.d("CostDialog", "Amount is empty")
                                    // Handle empty input
                                }

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

    @OptIn(ExperimentalMaterial3Api::class)
    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun UnitsDialog(
        building: Buildings,
        onDismiss: () -> Unit,
        onConfirm: (Units) -> Unit,
        onInsertDebt: (Debts) -> Unit
    ) {

        var metrage by remember { mutableStateOf("") }
        var ownerName by remember { mutableStateOf("") }
        var tenantName by remember { mutableStateOf("") }
        var unitNumber by remember { mutableStateOf("") }
        var numberOfTenant by remember { mutableStateOf("") }
        val costs by costsViewModel.fetchAndProcessCosts(building.buildingId).collectAsState(initial = emptyList())

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
                        text = "Add Unit",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = unitNumber,
                        onValueChange = { unitNumber = it },
                        label = { Text("Unit Number") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = metrage,
                        onValueChange = { metrage = it },
                        label = { Text("Metrage") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text("Owner Name") }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = tenantName,
                        onValueChange = { tenantName = it },
                        label = { Text("Tenant Name") }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = numberOfTenant,
                        onValueChange = { numberOfTenant = it },
                        label = { Text("Number of Tenant") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )

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
                                // Convert to ASCII digits
                                val asciiUnitNumber = unitNumber.replace("۰", "0").replace("۱", "1").replace("۲", "2")
                                    .replace("۳", "3").replace("۴", "4").replace("۵", "5")
                                    .replace("۶", "6").replace("۷", "7").replace("۸", "8")
                                    .replace("۹", "9")
                                val asciiMetrage = metrage.replace("۰", "0").replace("۱", "1").replace("۲", "2")
                                    .replace("۳", "3").replace("۴", "4").replace("۵", "5")
                                    .replace("۶", "6").replace("۷", "7").replace("۸", "8")
                                    .replace("۹", "9")
                                val asciiNumberOfTenant = numberOfTenant.replace("۰", "0").replace("۱", "1").replace("۲", "2")
                                    .replace("۳", "3").replace("۴", "4").replace("۵", "5")
                                    .replace("۶", "6").replace("۷", "7").replace("۸", "8")
                                    .replace("۹", "9")
                                if (asciiUnitNumber.isNotEmpty()) {
                                    val unitNumberInt = asciiUnitNumber.toInt()
                                    val metrageDouble = asciiMetrage.toDoubleOrNull() ?: 0.0
                                    val numberOfTenantInt = asciiNumberOfTenant.toInt()

                                    val units = Units(
                                        buildingId = building.buildingId,
                                        unitNumber = unitNumberInt,
                                        metrage = metrageDouble,
                                        ownerName = ownerName,
                                        tenantName = tenantName,
                                        numberOfTenants = numberOfTenantInt
                                    )

                                    var unit = onConfirm(units)
                                    costs.forEach { cost ->
                                        val debt = Debts(
                                            unitId = (unit as Long),
                                            costId = cost.id,
                                            description = cost.costName,
                                            dueDate = "",
                                            paymentFlag = false
                                        )
                                        onInsertDebt(debt)
                                    }
                                    onDismiss()
                                } else {
                                    Log.d("UnitsDialog", "Number of Unit is empty")
                                    // Handle empty input
                                }
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






