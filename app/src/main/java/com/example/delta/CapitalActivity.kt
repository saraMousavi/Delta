package com.example.delta

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.collectAsState
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
import com.example.delta.enums.CalculateMethod
import com.example.delta.enums.FundType
import com.example.delta.enums.PaymentLevel
import com.example.delta.enums.Period
import com.example.delta.enums.Responsible
import com.example.delta.init.NumberCommaTransformation
import com.example.delta.viewmodel.SharedViewModel
import com.example.delta.volley.BuildingWithCosts
import com.example.delta.volley.Cost
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

    // 1) load buildings + costs from server once
    LaunchedEffect(Unit) {
        Cost().fetchBuildingsWithCosts(
            context = context,
            onSuccess = { list ->
                buildingsWithCosts = list
                isLoading = false

                if (list.isNotEmpty()) {
                    selectedBuilding = list.first().building
                    costItems = list.first().costs
                }
            },
            onError = { e ->
                errorMessage = e.message
                isLoading = false
            }
        )
    }

    // 2) whenever selectedBuilding changes, update costItems from buildingsWithCosts
    LaunchedEffect(selectedBuilding, buildingsWithCosts) {
        selectedBuilding?.let { b ->
            val match = buildingsWithCosts.find { it.building.buildingId == b.buildingId }
            costItems = match?.costs ?: emptyList()
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
                                modifier = Modifier.weight(1f)
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

                    items(costItems.size) { index ->
                        CapitalCostRow(
                            costInput = costItems[index]
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
            onConfirm = { fiscalYear, amount ->
                coroutineScope.launch {
                    selectedBuilding?.let { building ->
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
                            tempAmount = amount.toDouble(),
                            dueDate = "$fiscalYear/01/01"
                        )

                        sharedViewModel.insertDebtForCapitalCost(
                            building.buildingId,
                            newCost,
                            amount.toDouble(),
                            "$fiscalYear/01/01",
                            description = context.getString(R.string.capital_info),
                            onSuccess = { costs, debts ->
                                coroutineScope.launch {
                                    sharedViewModel.insertCostToServer(
                                        context,
                                        costs,
                                        debts,
                                        onSuccess = {
                                            coroutineScope.launch {
                                                snackBarHostState.showSnackbar(
                                                    context.getString(R.string.capital_calcualted_successfully)
                                                )
                                            }
                                            // Optional: refresh from server again after insert
                                            Cost().fetchBuildingsWithCosts(
                                                context = context,
                                                onSuccess = { list ->
                                                    buildingsWithCosts = list
                                                    selectedBuilding?.let { sb ->
                                                        val match = list.find { it.building.buildingId == sb.buildingId }
                                                        costItems = match?.costs ?: emptyList()
                                                    }
                                                },
                                                onError = { /* ignore for now */ }
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
                                }
                            },
                            onError = {
                                coroutineScope.launch {
                                    snackBarHostState.showSnackbar(
                                        context.getString(R.string.failed)
                                    )
                                }
                            }
                        )
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
            textAlign = androidx.compose.ui.text.style.TextAlign.End
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
