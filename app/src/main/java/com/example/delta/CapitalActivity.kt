package com.example.delta

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Debts
import com.example.delta.enums.CalculateMethod
import com.example.delta.enums.FundType
import com.example.delta.enums.PaymentLevel
import com.example.delta.enums.Period
import com.example.delta.enums.Responsible
import com.example.delta.init.NumberCommaTransformation
import com.example.delta.init.Preference
import com.example.delta.viewmodel.SharedViewModel
import com.example.delta.volley.Building
import com.example.delta.volley.Cost
import com.example.delta.volley.Cost.BuildingWithCosts
import kotlinx.coroutines.launch

class CapitalActivity : ComponentActivity() {

    val sharedViewModel: SharedViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme(useDarkTheme = sharedViewModel.isDarkModeEnabled) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Scaffold(
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Text(
                                        text = getString(R.string.capital_info),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                navigationIcon = {
                                    IconButton(onClick = { finish() }) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back"
                                        )
                                    }
                                }
                            )
                        }
                    ) { innerPadding ->
                        CapitalInfoList(sharedViewModel)
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapitalInfoList(
    sharedViewModel: SharedViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var buildingsWithCosts by remember { mutableStateOf<List<BuildingWithCosts>>(emptyList()) }
    var selectedBuilding by remember { mutableStateOf<Buildings?>(null) }
    var costItems by remember { mutableStateOf<List<Costs>>(emptyList()) }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showDialog by remember { mutableStateOf(false) }
    val snackBarHostState = remember { SnackbarHostState() }

    var selectedYear by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        Cost().fetchBuildingsWithCosts(
            context = context,
            mobileNumber = Preference().getUserMobile(context) ?: "",
            onSuccess = { list ->
                buildingsWithCosts = list
                isLoading = false

                if (list.isNotEmpty()) {
                    val first = list.first()
                    selectedBuilding = first.building
                    costItems = first.costs.filter { it.capitalFlag == true }
                }
            },
            onError = { e ->
                errorMessage = e.message
                isLoading = false
            }
        )
    }

    LaunchedEffect(selectedBuilding, buildingsWithCosts) {
        selectedBuilding?.let { b ->
            val match = buildingsWithCosts.find { it.building.buildingId == b.buildingId }
            costItems = (match?.costs ?: emptyList()).filter { it.capitalFlag == true && it.costName == context.getString(R.string.capital_costs) }
            selectedYear = null
        }
    }

    val availableYears = remember(costItems) {
        costItems
            .mapNotNull { c ->
                val d = c.dueDate
                if (d.length >= 4) d.substring(0, 4) else null
            }
            .distinct()
            .sorted()
    }

    val visibleCosts = remember(costItems, selectedYear) {
        if (selectedYear.isNullOrBlank()) {
            costItems
        } else {
            costItems.filter { it.dueDate.startsWith(selectedYear!!) }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 75.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        when {
            isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            errorMessage != null -> {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                val buildings = buildingsWithCosts.map { it.building }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ExposedDropdownMenuBoxExample(
                                sharedViewModel = sharedViewModel,
                                items = buildings,
                                selectedItem = selectedBuilding,
                                onItemSelected = { building ->
                                    selectedBuilding = building
                                },
                                label = context.getString(R.string.building),
                                itemLabel = { it.name },
                                modifier = Modifier.weight(0.5f)
                            )

                            ExposedDropdownMenuBoxExample(
                                sharedViewModel = sharedViewModel,
                                items = availableYears,
                                selectedItem = selectedYear,
                                onItemSelected = { year ->
                                    selectedYear = year
                                },
                                label = context.getString(R.string.fiscal_year),
                                itemLabel = { it },
                                modifier = Modifier.weight(0.5f)
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                context.getString(R.string.fiscal_year),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(0.5f)
                            )
                            Text(
                                text = context.getString(R.string.amount),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(0.5f),
                                textAlign = TextAlign.End
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    items(visibleCosts.size) { index ->
                        CapitalCostRow(
                            costInput = visibleCosts[index]
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }

                FloatingActionButton(
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add capital cost")
                }
            }
        }

        SnackbarHost(
            hostState = snackBarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showDialog) {
        AddNewCapitalDialog(
            sharedViewModel = sharedViewModel,
            onDismiss = { showDialog = false },
            onConfirm = { fiscalYear, amountStr ->
                coroutineScope.launch {
                    val building = selectedBuilding
                    if (building == null) {
                        showDialog = false
                        return@launch
                    }

                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    if (amount <= 0.0) {
                        snackBarHostState.showSnackbar(
                            context.getString(R.string.invalid_amount)
                        )
                        return@launch
                    }

                    try {
                        val dto = Building().fetchBuildingFullSuspend(
                            context = context,
                            buildingId = building.buildingId,
                            fiscalYear = fiscalYear
                        )

                        val unitIdsWithOwner = dto.ownerUnits.map { it.unitId }.toSet()
                        val allUnitsHaveOwner = dto.units.all { it.unitId in unitIdsWithOwner }

                        if (!allUnitsHaveOwner) {
                            snackBarHostState.showSnackbar(
                                context.getString(R.string.first_compete_owners)
                            )
                            return@launch
                        }

                        val ownerUnits = dto.ownerUnits

                        val ownerDangMap: Map<Long, Double> =
                            ownerUnits.groupBy { it.ownerId }
                                .mapValues { (_, list) -> list.sumOf { it.dang } }

                        if (ownerDangMap.isEmpty()) {
                            snackBarHostState.showSnackbar(
                                context.getString(R.string.first_compete_owners)
                            )
                            return@launch
                        }

                        val totalDang = ownerDangMap.values.sum()

                        val dueDate = "$fiscalYear/01/01"

                        val newCost = Costs(
                            buildingId = building.buildingId,
                            costName = context.getString(R.string.capital_costs),
                            chargeFlag = false,
                            capitalFlag = true,
                            fundType = FundType.CAPITAL,
                            responsible = Responsible.OWNER,
                            paymentLevel = PaymentLevel.UNIT,
                            calculateMethod = CalculateMethod.EQUAL,
                            period = Period.YEARLY,
                            tempAmount = amount,
                            dueDate = dueDate
                        )

                        val ownerCount = ownerDangMap.size.coerceAtLeast(1)
                        val debtsToSave = ownerDangMap.map { (ownerId, ownerDang) ->
                            val share = if (totalDang > 0.0) {
                                amount * (ownerDang / totalDang)
                            } else {
                                amount / ownerCount
                            }

                            Debts(
                                debtId = 0L,
                                unitId = 0L,
                                costId = 0L,
                                ownerId = ownerId,
                                buildingId = building.buildingId,
                                description = context.getString(R.string.capital_info),
                                dueDate = dueDate,
                                amount = share,
                                paymentFlag = false
                            )
                        }

                        sharedViewModel.insertCostToServer(
                            context = context,
                            costs = listOf(newCost),
                            debts = debtsToSave,
                            onSuccess = {
                                coroutineScope.launch {
                                    snackBarHostState.showSnackbar(
                                        context.getString(R.string.capital_calcualted_successfully)
                                    )
                                }

                                Cost().fetchBuildingsWithCosts(
                                    context = context,
                                    mobileNumber = Preference().getUserMobile(context) ?: "",
                                    onSuccess = { list ->
                                        buildingsWithCosts = list
                                        selectedBuilding?.let { sb ->
                                            val match =
                                                list.find { it.building.buildingId == sb.buildingId }
                                            costItems =
                                                (match?.costs ?: emptyList()).filter { it.capitalFlag == true }
                                        }
                                    },
                                    onError = { }
                                )
                            },
                            onError = {
                                coroutineScope.launch {
                                    snackBarHostState.showSnackbar(
                                        context.getString(R.string.failed)
                                    )
                                }
                            }
                        )
                    } catch (e: Exception) {
                        snackBarHostState.showSnackbar(
                            context.getString(R.string.failed)
                        )
                    } finally {
                        showDialog = false
                    }
                }
            }

        )
    }
}



@Composable
fun CapitalCostRow(
    costInput: Costs
) {
    val context = LocalContext.current
    val transformation = remember { NumberCommaTransformation() }
    val amountInWords = transformation.numberToWords(context, costInput.tempAmount.toLong())

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(costInput.dueDate.take(4), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(0.5f))
        Text(
            text = "$amountInWords ${context.getString(R.string.toman)}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(0.5f),
            textAlign = TextAlign.End
        )
    }
    HorizontalDivider(Modifier.padding(vertical = 8.dp))
}

@Composable
fun AddNewCapitalDialog(
    sharedViewModel: SharedViewModel,
    onDismiss: () -> Unit,
    onConfirm: (fiscalYear: String, amount: String) -> Unit
) {
    val context = LocalContext.current
    val fiscalYears = remember { (1404..1415).map { it.toString() } }
    var selectedFiscalYear by remember { mutableStateOf(fiscalYears.first()) }
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = context.getString(R.string.capital_info_insert)) },
        text = {
            Column {
                ExposedDropdownMenuBoxExample(
                    sharedViewModel = sharedViewModel,
                    items = fiscalYears,
                    selectedItem = selectedFiscalYear,
                    onItemSelected = { selectedFiscalYear = it },
                    label = context.getString(R.string.fiscal_year),
                    itemLabel = { it },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { newVal -> amount = newVal.filter { it.isDigit() } },
                    label = {
                        Text(
                            context.getString(R.string.amount),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                val amountVal = amount.toLongOrNull() ?: 0L
                val amountInWords = NumberCommaTransformation().numberToWords(context, amountVal)
                Text(
                    text = "$amountInWords ${context.getString(R.string.toman)}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedFiscalYear, amount) }) {
                Text(context.getString(R.string.confirm))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(context.getString(R.string.cancel))
            }
        }
    )
}
