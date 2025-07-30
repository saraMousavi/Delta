package com.example.delta

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
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
            AppTheme {
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
    val fiscalYears = (1400..1420).map { it.toString() }
    var selectedYear by remember { mutableStateOf(fiscalYears.first()) }

    var chargeAlreadyCalculated by remember { mutableStateOf(false) }
    var lastCalculatedCharges by remember { mutableStateOf<Map<Units, Double>?>(null) }


    val coroutineScope = rememberCoroutineScope()
    var selectedCalculation by remember {
        mutableStateOf(CalculateMethod.AUTOMATIC.getDisplayName(context)) // Default to "Owners"
    }
    // On building selection, load costs from sharedViewModel, map to CostInputModel
    LaunchedEffect(selectedBuilding, selectedYear) {
        selectedBuilding?.let { building ->
            // Get single snapshot of costs
            val costs = sharedViewModel.getCostsForBuildingWithChargeFlag(building.buildingId)
                .flowOn(Dispatchers.IO)
                .first() // Collect only first emission
            costItems = costs
            sharedViewModel.chargeCostsList.value = costs

            chargeAlreadyCalculated = costs.isNotEmpty()

            if (chargeAlreadyCalculated) {
                isEditMode = false
                lastCalculatedCharges =
                    sharedViewModel.getLastCalculatedCharges(building.buildingId, selectedYear)
            } else {
                isEditMode = true
                lastCalculatedCharges = null
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
                items = buildings,
                selectedItem = selectedBuilding,
                onItemSelected = { selectedBuilding = it },
                label = context.getString(R.string.building),
                itemLabel = { it.name },
                modifier = Modifier
                    .weight(0.5f)
            )
            ExposedDropdownMenuBoxExample(
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
                        item {
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = context.getString(R.string.this_year_charge_calculated),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            // Show calculated charges per unit
                            lastCalculatedCharges!!.forEach { (unit, charge) ->
                                Text(
                                    text = "${unit.unitNumber}: ${String.format("%.2f", charge)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                    val costList = sharedViewModel.chargeCostsList.value
                    items(costList.size) { index ->
                        val item = costList[index]
                        CostInputRow(
                            costInput = item,
                            isEditMode = isEditMode,
                            onAmountChange = { newAmount ->
                                costItems = costItems.toMutableList()
                                    .also {
                                        it[index] =
                                            it[index].copy(tempAmount = if (newAmount.isEmpty()) 0.0 else newAmount.toDouble())
                                    }
                            },
                            onCalculateMethodChange = { newMethod ->
                                costItems = costItems.toMutableList()
                                    .also {
                                        it[index] = it[index].copy(calculateMethod = newMethod)
                                    }
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }

                if (isEditMode) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = { showAddCostDialog = true }) {
                            Text(
                                context.getString(R.string.add_new_parameter),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Button(onClick = {
                            isEditMode = false
                            coroutineScope.launch {
                                val units =
                                    sharedViewModel.getUnitsForBuilding(selectedBuilding!!.buildingId)
                                        .first()
                                // or collect with a one-shot here, depending on your API
                                val chargeMap = units.associate { unit ->
                                    // Calculate charge here based on costItems and unit
                                    // For demonstration, sum of all tempAmounts equally divided or your logic
                                    val totalCharge = costItems.sumOf { it.tempAmount }
                                    unit to totalCharge // Replace with correct calculation logic
                                }
                                calculatedCharges = chargeMap
                                showChargeResultDialog = true
                            }
                        }) {
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
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = {
                        coroutineScope.launch {
                            sharedViewModel.getUnitsForBuilding(selectedBuilding!!.buildingId)
                                .collect { units ->

                                    val map =
                                        units.associate { unit -> unit to equalChargeAmount.toDouble() }
                                    sharedViewModel.insertDebtForCharge(
                                        buildingId = selectedBuilding!!.buildingId,
                                        fiscalYear = selectedYear,
                                        chargeUnitMap = map,
                                        onError = {
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.failed),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        onSuccess = {
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.charge_calcualted_successfully),
                                                Toast.LENGTH_SHORT
                                            ).show()
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
                sharedViewModel.insertDebtForCharge(
                    buildingId = selectedBuilding!!.buildingId,
                    fiscalYear = selectedYear,
                    chargeUnitMap = calculatedCharges,
                    onError = {
                        Toast.makeText(
                            context,
                            context.getString(R.string.failed),
                            Toast.LENGTH_SHORT
                        ).show()
                        showChargeResultDialog = false
                    },
                    onSuccess = {
                        Toast.makeText(
                            context,
                            context.getString(R.string.charge_calcualted_successfully),
                            Toast.LENGTH_SHORT
                        ).show()
                        lastCalculatedCharges = calculatedCharges
                        isEditMode = false
                        chargeAlreadyCalculated = true

                        showChargeResultDialog = false
                    }
                )
            },
            onDismiss = { showChargeResultDialog = false }
        )
    }
}

@Composable
fun CostInputRow(
    costInput: Costs,
    isEditMode: Boolean,
    onAmountChange: (String) -> Unit,
    onCalculateMethodChange: (CalculateMethod) -> Unit
) {
    val context = LocalContext.current
    // Keep local input state synchronized with costInput.tempAmount when editing
    var amount by remember(costInput.costId) { mutableStateOf(costInput.tempAmount.toString()) }
    val transformation = remember { NumberCommaTransformation() }
// Convert to words
    val amountInWords = remember(amount) {
        derivedStateOf {
            transformation.numberToWords(
                context,
                amount.toLongOrNull() ?: 0L
            )
        }
    }
    if (isEditMode) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                OutlinedTextField(
                    value = amount,
                    onValueChange = { newValue ->
                        // Allow only valid numbers (optional)
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*\$"))) {
                            amount = newValue
                            onAmountChange(newValue)
                        }
                    },
                    label = {
                        Text(
                            text = costInput.costName,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    singleLine = true,
                    enabled = true, // editable since isEditMode == true
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.6f)
                )

                Spacer(Modifier.width(8.dp))

                ExposedDropdownMenuBoxExample(
                    items = CalculateMethod.entries.toList(),
                    selectedItem = costInput.calculateMethod,
                    onItemSelected = { selectedMethod ->
                        onCalculateMethodChange(selectedMethod)
                    },
                    label = context.getString(R.string.calculate_method),
                    modifier = Modifier.weight(0.4f),
                    itemLabel = { it.getDisplayName(context) }
                )
            }
            Text(
                text = " ${amountInWords.value} ${context.getString(R.string.toman)}",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(context.getColor(R.color.grey)),
                modifier = Modifier.padding(top = 4.dp)
            )

    }
    } else {
        // Show non-editable row with texts
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = costInput.costName,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(0.4f)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(0.3f)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = costInput.calculateMethod.getDisplayName(context),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(0.3f)
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp)
        )
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
                                String.format(
                                    "%.2f",
                                    charge
                                )
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



