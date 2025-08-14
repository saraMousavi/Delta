package com.example.delta

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Units
import com.example.delta.enums.CalculateMethod
import com.example.delta.enums.FundType
import com.example.delta.enums.PaymentLevel
import com.example.delta.enums.Period
import com.example.delta.enums.Responsible
import com.example.delta.init.Calculation
import com.example.delta.init.NumberCommaTransformation
import com.example.delta.viewmodel.SharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class ChargeCalculationActivity : ComponentActivity() {
    private val sharedViewModel: SharedViewModel by viewModels()

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme (useDarkTheme = sharedViewModel.isDarkModeEnabled){
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Scaffold(
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Text(
                                        text = getString(R.string.charges_calculation),
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
                        ChargeCalculationScreen(sharedViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun ChargeCalculationScreen(sharedViewModel: SharedViewModel) {
    val context = LocalContext.current
    var amountInputs by remember { mutableStateOf<List<String>>(emptyList()) }

    // State variables
    var selectedBuilding by remember { mutableStateOf<Buildings?>(null) }
    var isEditMode by remember { mutableStateOf(true) }
    var costItems by remember { mutableStateOf<List<Costs>>(emptyList()) }
    var equalChargeAmount by remember { mutableStateOf<String>("") }
    var showAddCostDialog by remember { mutableStateOf(false) }
    var showChargeResultDialog by remember { mutableStateOf(false) }
    var calculatedCharges by remember { mutableStateOf<Map<Units, Double>?>(null) }

    // Load buildings and periods (assumed from ViewModel or static)
    val buildings by sharedViewModel.getAllBuildings().collectAsState(initial = emptyList())
    val fiscalYears = (1404..1415).map { it.toString() }
    var selectedYear by remember { mutableStateOf(fiscalYears.first()) }

    var chargeAlreadyCalculated by remember { mutableStateOf(false) }
    var lastCalculatedCharges by remember { mutableStateOf<Map<Units, Double>?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    var selectedCalculation by remember {
        mutableStateOf(CalculateMethod.AUTOMATIC.getDisplayName(context)) // Default to "Owners"
    }
    val isCalculateEnabled by remember(costItems) {
        derivedStateOf {
            costItems.any { it.tempAmount > 0 }
        }
    }

    // On building selection, load costs from sharedViewModel, map to CostInputModel
    var lastSavedCostItems by remember { mutableStateOf<List<Costs>>(emptyList()) }
    val transformation = remember { NumberCommaTransformation() }
    LaunchedEffect(selectedBuilding, selectedYear) {
        selectedBuilding?.let { building ->
            sharedViewModel.getCostsForBuildingWithChargeFlagAndFiscalYear(building.buildingId, selectedYear)
                .flowOn(Dispatchers.IO)
                .collect { costs ->
                    if (costs.isEmpty()) {
                        // If cost list is empty, collect raw charges once and emit that instead
                        val rawCharges = sharedViewModel.getRawChargesCostsWithBuildingId(building.buildingId)
                            .flowOn(Dispatchers.IO)
                            .first()

                        costItems = rawCharges
                        amountInputs = rawCharges.map { it.tempAmount.toString() }
                        lastSavedCostItems = rawCharges
                        sharedViewModel.chargeCostsList.value = rawCharges
                        chargeAlreadyCalculated = rawCharges.isNotEmpty()

                        if (chargeAlreadyCalculated) {
                            isEditMode = false
                            lastCalculatedCharges = sharedViewModel.getLastCalculatedCharges(building.buildingId, selectedYear)
                        } else {
                            isEditMode = true
                            lastCalculatedCharges = null
                        }
                    } else {
                        // Normal case: costs from DB
                        costItems = costs
                        amountInputs = costs.map { it.tempAmount.toString() }
                        lastSavedCostItems = costs
                        sharedViewModel.chargeCostsList.value = costs
                        chargeAlreadyCalculated = costs.isNotEmpty()
                        if (chargeAlreadyCalculated) {
                            isEditMode = false
                            lastCalculatedCharges = sharedViewModel.getLastCalculatedCharges(building.buildingId, selectedYear)
                        } else {
                            isEditMode = true
                            lastCalculatedCharges = null
                        }
                    }
                }
        }
    }




    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 75.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExposedDropdownMenuBoxExample(
                sharedViewModel = sharedViewModel,
                items = buildings,
                selectedItem = selectedBuilding,
                onItemSelected = { selectedBuilding = it },
                label = context.getString(R.string.building),
                itemLabel = { it.name },
                modifier = Modifier
                    .weight(0.5f)
            )
            ExposedDropdownMenuBoxExample(
                sharedViewModel = sharedViewModel,
                items = fiscalYears,
                selectedItem = selectedYear,
                onItemSelected = { selectedYear = it },
                label = context.getString(R.string.fiscal_year),
                itemLabel = { it },
                modifier = Modifier
                    .weight(0.5f)
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp)
        )
        ChipGroupShared(
            selectedItems = listOf(selectedCalculation),
            onSelectionChange = { newSelection ->
                if (newSelection.isNotEmpty()) {
                    selectedCalculation = newSelection.first()
                }
            },
            items = listOf(
                CalculateMethod.AUTOMATIC.getDisplayName(context),
                CalculateMethod.EQUAL.getDisplayName(context)
            ),
            modifier = Modifier.padding(vertical = 8.dp),
            label = context.getString(R.string.charge_method),
            singleSelection = true
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp)
        )
        when (selectedCalculation) {
            CalculateMethod.AUTOMATIC.getDisplayName(context) -> {


                // List of cost items (input + dropdown)
                LazyColumn(modifier = Modifier.weight(1f)) {

                    // If charge has been calculated and isEditMode == false, append units charges list as last item section
                    if (!isEditMode && lastCalculatedCharges != null && lastCalculatedCharges!!.isNotEmpty()) {
//                        item {
//                            // Show calculated charges per unit
//                            lastCalculatedCharges!!.forEach { (unit, charge) ->
//                                FundInfoBox(
//                                    formattedFund = formatNumberWithCommas(charge),
//                                    context = context,
//                                    title = "${context.getString(R.string.unit) }${unit.unitNumber}:"
//                                )
//                                Spacer(Modifier.height(8.dp))
//                            }
//                            Spacer(Modifier.height(16.dp))
//                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = context.getString(R.string.title),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(0.4f)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = context.getString(R.string.amount),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(0.3f)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = context.getString(R.string.calculate_method),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(0.3f)
                            )
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    val costList = costItems   // Use costItems as source

                    items(costList.size) { index ->
                        CostInputRow(
                            sharedViewModel = sharedViewModel,
                            costInput = costList[index],
                            amount = amountInputs.getOrElse(index) { "0" },  // Use the string input, can be ""
                            calculateMethod = costItems[index].calculateMethod,
                            isEditMode = isEditMode,
                            onAmountChange = { newAmount ->
                                amountInputs = amountInputs.toMutableList().also { it[index] = newAmount }
                                costItems = costItems.toMutableList().also {
                                    it[index] = it[index].copy(tempAmount = newAmount.toDoubleOrNull() ?: 0.0)
                                }
                            },
                            onCalculateMethodChange = { newMethod ->
                                costItems = costItems.toMutableList().also {
                                    it[index] = it[index].copy(calculateMethod = newMethod)
                                }
                            }
                        )

                        Spacer(Modifier.height(8.dp))
                    }

                }

                if (isEditMode) {
                    Row(
                        horizontalArrangement  = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            modifier = Modifier.weight(0.4f).padding(),onClick = { showAddCostDialog = true }, enabled = selectedBuilding != null) {
                            Text(
                                context.getString(R.string.add_new_parameter),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Button(
                            modifier = Modifier.weight(0.25f),
                            onClick = {
                                costItems = lastSavedCostItems.map { it.copy() } // Defensive copy to avoid mutation issues
                                isEditMode = false
                            }
                        ) {
                            Text(
                                text = context.getString(R.string.display),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        Button(
                            modifier = Modifier.weight(0.35f), onClick = {
                            isEditMode = false
                            coroutineScope.launch {
                                val units = sharedViewModel.getUnitsForBuilding(selectedBuilding!!.buildingId).first()

                                val calculation = Calculation()

                                // 1. Fetch tenant counts for all units asynchronously in parallel or sequentially
                                val tenantCountsByUnitId: Map<Long, Int> = units.associate { unit ->
                                    // call DAO or ViewModel method to get tenant count per unit
                                    val tenantCountResult =
                                        sharedViewModel.getNumberOfUnitsTenantForUnit(unit.unitId)
                                    // assume tenantCountResult.numberOfTenants is String? or Int? convert to Int safely
                                    unit.unitId to (tenantCountResult?.toIntOrNull() ?: 1)
                                }

                                // 2. Define synchronous lookup function using the pre-fetched map
                                val getTenantCountForUnit: (Long) -> Int = { unitId ->
                                    tenantCountsByUnitId[unitId] ?: 1
                                }

                                // 3. Prepare CostInfo map
                                data class CostInfo(val amount: Double, val method: CalculateMethod)
                                val costsInfoMap: Map<String, CostInfo> = costItems.associate { cost ->
                                    cost.costName to CostInfo(cost.tempAmount, cost.calculateMethod)
                                }

                                // 4. Calculate charges for each unit using calculation method and pre-fetched tenant counts
                                val chargeMap = units.associateWith { unit ->
                                    costsInfoMap.entries.sumOf { (_, costInfo) ->
                                        calculation.calculateAmountByMethod(
                                            costAmount = costInfo.amount,
                                            unit = unit,
                                            allUnits = units,
                                            calculationMethod = costInfo.method,
                                            getTenantCountForUnit = getTenantCountForUnit
                                        )
                                    }
                                }

                                calculatedCharges = chargeMap
                                showChargeResultDialog = true
                            }


                        },
                            enabled = isCalculateEnabled) {
                            Text(
                                context.getString(R.string.calculate),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = {
                            isEditMode = true
                        }) {
                            Text(
                                context.getString(R.string.edit),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            CalculateMethod.EQUAL.getDisplayName(context) -> {
                val amountInWords = transformation.numberToWords(
                    context,
                    equalChargeAmount.toDoubleOrNull()?.toLong() ?: 0L  // Convert decimal input to long
                )
                // Show single input for total charge amount
                OutlinedTextField(
                    value = equalChargeAmount,
                    onValueChange = { equalChargeAmount = it },
                    label = {
                        Text(
                            context.getString(R.string.charge_amount),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = " $amountInWords ${context.getString(R.string.toman)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(context.getColor(R.color.grey)),
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column {
                    SnackbarHost(hostState = snackBarHostState)
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(modifier = Modifier.fillMaxWidth().height(52.dp) , onClick = {
                        coroutineScope.launch {
                            sharedViewModel.getUnitsForBuilding(selectedBuilding!!.buildingId)
                                .collect { units ->

                                    val map =
                                        units.associate { unit -> unit to equalChargeAmount.toDouble() }
                                    val costsAmountMap: Map<Costs, Double> =
                                        costItems.mapIndexed { index, cost ->
                                            val amount =
                                                amountInputs.getOrNull(index)?.toDoubleOrNull()
                                                    ?: 0.0
                                            cost to amount
                                        }.toMap()
                                    sharedViewModel.insertDebtForCharge(
                                        buildingId = selectedBuilding!!.buildingId,
                                        fiscalYear = selectedYear,
                                        chargeUnitMap = map,
                                        costsAmountMap = costsAmountMap,
                                        onError = {
                                            coroutineScope.launch {
                                                snackBarHostState.showSnackbar(context.getString(R.string.failed))
                                            }
                                        },
                                        onSuccess = {
                                            coroutineScope.launch {
                                                snackBarHostState.showSnackbar(context.getString(R.string.charge_calcualted_successfully))
                                            }
                                        }
                                    )
                                }
                        }

                    }) {
                        Text(
                            context.getString(R.string.calculate),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                }
            }

            else -> {
                // Handle other cases if needed or show empty
            }
        }


    }

    if (showAddCostDialog) {
        val notChargesCost by sharedViewModel.getChargesCostsNotInBuilding(buildingId = selectedBuilding!!.buildingId)
            .collectAsState(initial = emptyList())
        AddNewCostDialog(
            onDismiss = { showAddCostDialog = false },
            onChargeConfirm  = { costNames ->
                // Add new cost both locally and DB
                coroutineScope.launch {
                    costNames.forEach{ costName ->
                        val newCost = Costs(
                            buildingId = selectedBuilding!!.buildingId,
                            costName = costName,
                            chargeFlag = true,
                            fundType = FundType.OPERATIONAL,
                            responsible = Responsible.TENANT,
                            paymentLevel = PaymentLevel.UNIT,
                            calculateMethod = CalculateMethod.EQUAL,
                            period = Period.YEARLY,
                            tempAmount = 0.0,
                            dueDate = ""
                        )
                        sharedViewModel.insertNewCost(newCost)
                        sharedViewModel.chargeCostsList.value += newCost
                    }

                    showAddCostDialog = false
                }
            },
            costs = notChargesCost
        )
    }


    if (showChargeResultDialog) {
        ChargeResultDialog(
            results = calculatedCharges,
            onSave = {
                val costsAmountMap: Map<Costs, Double> = costItems.mapIndexed { index, cost ->
                    val amount = amountInputs.getOrNull(index)?.toDoubleOrNull() ?: 0.0
                    cost to amount
                }.toMap()
                sharedViewModel.insertDebtForCharge(
                    buildingId = selectedBuilding!!.buildingId,
                    fiscalYear = selectedYear,
                    chargeUnitMap = calculatedCharges,
                    onError = {
                        coroutineScope.launch {
                            showChargeResultDialog = false
                            snackBarHostState.showSnackbar(context.getString(R.string.failed))
                        }
                    },
                    costsAmountMap = costsAmountMap,
                    onSuccess = {
                        coroutineScope.launch {
                            lastCalculatedCharges = calculatedCharges
                            isEditMode = false
                            chargeAlreadyCalculated = true

                            showChargeResultDialog = false
                            snackBarHostState.showSnackbar(context.getString(R.string.charge_calcualted_successfully))
                        }
                    }
                )
            },
            onDismiss = { showChargeResultDialog = false }
        )
    }
}


@Composable
fun CostInputRow(
    sharedViewModel: SharedViewModel,
    costInput: Costs,
    amount: String,
    calculateMethod: CalculateMethod,
    isEditMode: Boolean,
    onAmountChange: (String) -> Unit,
    onCalculateMethodChange: (CalculateMethod) -> Unit
) {
    val context = LocalContext.current
    val transformation = remember { NumberCommaTransformation() }

    val filteredMethods = remember {
        CalculateMethod.entries.filter { it != CalculateMethod.DANG && it != CalculateMethod.AUTOMATIC }.toMutableList()
    }.apply {
        if (calculateMethod !in this) add(calculateMethod)
    }

    // Compute amountInWords directly on each recomposition according to amount param
    val amountInWords = transformation.numberToWords(
        context,
        amount.toDoubleOrNull()?.toLong() ?: 0L  // Convert decimal input to long
    )

    if (isEditMode) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*\$"))) {
                            onAmountChange(newValue)
                        }
                    },
                    label = { Text(costInput.costName, style = MaterialTheme.typography.bodyLarge) },
                    singleLine = true,
                    enabled = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.6f)
                )
                Spacer(Modifier.width(8.dp))
                ExposedDropdownMenuBoxExample(
                    sharedViewModel = sharedViewModel,
                    items = filteredMethods,
                    selectedItem = calculateMethod,
                    onItemSelected = onCalculateMethodChange,
                    label = context.getString(R.string.calculate_method),
                    modifier = Modifier.weight(0.4f),
                    itemLabel = { it.getDisplayName(context) }
                )
            }
            Text(
                text = " $amountInWords ${context.getString(R.string.toman)}",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(context.getColor(R.color.grey)),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    } else {
        // Display mode row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(costInput.costName, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(0.4f))
            Spacer(Modifier.height(8.dp))
            Text(formatNumberWithCommas(amount.toDouble()), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(0.3f))
            Spacer(Modifier.height(8.dp))
            Text(calculateMethod.getDisplayName(context), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(0.3f))
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
fun ChargeResultDialog(
    results: Map<Units, Double>?,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                context.getString(R.string.charge_calculated_result),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        text = {
            if (results == null || results.isEmpty()) {
                Text(
                    text = context.getString(R.string.no_data),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    results.forEach { (unit, charge) ->
                        Text(
                            text = " ${context.getString(R.string.unit)} ${unit.unitNumber} : ${
                                formatNumberWithCommas(charge)
                            } ${context.getString(R.string.toman)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onSave) {
                Text(
                    context.getString(R.string.confirm),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(
                    context.getString(R.string.cancel),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    )
}



