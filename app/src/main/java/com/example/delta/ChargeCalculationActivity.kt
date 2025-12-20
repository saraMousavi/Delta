package com.example.delta

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
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
import com.example.delta.data.entity.BuildingWithCounts
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Debts
import com.example.delta.data.entity.Units
import com.example.delta.enums.CalculateMethod
import com.example.delta.enums.FundType
import com.example.delta.enums.PaymentLevel
import com.example.delta.enums.Period
import com.example.delta.enums.Responsible
import com.example.delta.init.Calculation
import com.example.delta.init.NumberCommaTransformation
import com.example.delta.init.Preference
import com.example.delta.viewmodel.SharedViewModel
import com.example.delta.volley.Building
import com.example.delta.volley.Cost
import kotlinx.coroutines.launch
import org.json.JSONArray
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

class ChargeCalculationActivity : ComponentActivity() {
    private val sharedViewModel: SharedViewModel by viewModels()

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
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
                    ) { _ ->
                        OperationalChargeCalculationScreen(sharedViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun OperationalChargeCalculationScreen(sharedViewModel: SharedViewModel) {
    CalculationScreen(
        sharedViewModel = sharedViewModel,
        fundType = FundType.OPERATIONAL
    )
}

@Composable
fun SumChargeRow(amount: String) {
    val context = LocalContext.current
    val transformation = remember { NumberCommaTransformation() }
    val amountInWords = transformation.numberToWords(
        context,
        amount.toLong()
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${context.getString(R.string.sum)}:",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = "${amountInWords} ${context.getString(R.string.toman)}",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.weight(0.6f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}
@Composable
fun CostInputRow(
    sharedViewModel: SharedViewModel,
    costInput: Costs,
    amount: String,
    isAuto: Boolean,
    calculateMethod: CalculateMethod,
    isEditMode: Boolean,
    onAmountChange: (String) -> Unit,
    onCalculateMethodChange: (CalculateMethod) -> Unit,
    showDeleteIcon: Boolean = false,
    onDeleteClick: () -> Unit = {}
) {

    val context = LocalContext.current
    val transformation = remember { NumberCommaTransformation() }

    val filteredMethods = remember {
        CalculateMethod.entries.filter { it != CalculateMethod.DANG && it != CalculateMethod.AUTOMATIC && it != CalculateMethod.NONE }
            .toMutableList()
    }.apply {
        if (calculateMethod !in this) add(calculateMethod)
    }

    val amountInWords = transformation.numberToWords(
        context,
        amount.toDoubleOrNull()?.toLong() ?: 0L
    )

    fun formatWithComma(input: String): String {
        if (input.isBlank()) return ""
        val clean = input.replace(",", "")
        return clean.toLongOrNull()
            ?.let { "%,d".format(it) }
            ?: input
    }

    var localText by remember {
        mutableStateOf(formatWithComma(amount))
    }

    LaunchedEffect(amount) {
        val cleanProp = amount.replace(",", "")
        val cleanLocal = localText.replace(",", "")
        if (cleanProp != cleanLocal) {
            localText = formatWithComma(amount)
        }
    }

    if (isEditMode) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showDeleteIcon) {
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = context.getString(R.string.delete),
                        )
                    }
                }

                OutlinedTextField(
                    value = if (localText == "0.0") "" else localText,
                    onValueChange = { newText ->
                        val raw = newText.replace(",", "")
                        if (raw.isEmpty()) {
                            localText = ""
                            onAmountChange("")
                        } else if (raw.matches(Regex("^\\d*$"))) {
                            localText = formatWithComma(raw)
                            onAmountChange(raw)
                        }
                    },
                    label = {
                        Text(costInput.costName, style = MaterialTheme.typography.bodyLarge)
                    },
                    singleLine = true,
                    enabled = isEditMode,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.6f)
                )
                if (isAuto) {
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
            }
            Text(
                text = " $amountInWords ${context.getString(R.string.toman)}",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(context.getColor(R.color.grey)),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                costInput.costName,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(0.4f)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                formatNumberWithCommas(amount.toDoubleOrNull() ?: 0.0),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(0.3f)
            )
            if (isAuto) {
                Spacer(Modifier.height(8.dp))
                Text(
                    calculateMethod.getDisplayName(context),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(0.3f)
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
    }
}


@Composable
fun ChargeResultDialog(
    results: Map<Units, Double>?,
    fundType: FundType,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            if (fundType == FundType.OPERATIONAL) {
                Text(
                    context.getString(R.string.operational_charge_calculated_result),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else if (fundType == FundType.CAPITAL){
                Text(
                    context.getString(R.string.capital_charge_calculated_result),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

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
