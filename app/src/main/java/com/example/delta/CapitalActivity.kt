package com.example.delta

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.delta.R.string.first_compete_units
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
import com.example.delta.server.JsonMapper
import com.example.delta.viewmodel.SharedViewModel
import com.example.delta.volley.Building
import com.example.delta.volley.Cost
import com.example.delta.volley.Owner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

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
                        CapitalChargeCalculationScreen(sharedViewModel)
                    }
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun CalculationScreen(
    sharedViewModel: SharedViewModel,
    fundType: FundType
) {
    val context = LocalContext.current
    var amountInputs by remember { mutableStateOf<List<String>>(emptyList()) }

    var selectedBuilding by remember { mutableStateOf<BuildingWithCounts?>(null) }
    var isEditMode by remember { mutableStateOf(true) }
    var costItems by remember { mutableStateOf<List<Costs>>(emptyList()) }
    var showAddCostDialog by remember { mutableStateOf(false) }
    var showChargeResultDialog by remember { mutableStateOf(false) }
    var calculatedCharges by remember { mutableStateOf<Map<Units, Double>?>(null) }

    var buildings by remember { mutableStateOf<List<BuildingWithCounts>>(emptyList()) }
    var buildingFull by remember { mutableStateOf<Building.BuildingFullDto?>(null) }
    var unitsForSelectedBuilding by remember { mutableStateOf<List<Units>>(emptyList()) }

    val fiscalYears = (1404..1415).map { it.toString() }
    var selectedYear by remember { mutableStateOf(fiscalYears.first()) }

    var chargeAlreadyCalculated by remember { mutableStateOf(false) }
    var lastCalculatedCharges by remember { mutableStateOf<Map<Units, Double>?>(null) }

    val coroutineScope = remember {
        (context as? ComponentActivity)?.lifecycleScope
            ?: CoroutineScope(SupervisorJob())
    }
    val snackBarHostState = remember { SnackbarHostState() }
    var selectedCalculation by remember {
        mutableStateOf(CalculateMethod.AUTOMATIC.getDisplayName(context))
    }
    val isCalculateEnabled by remember(costItems, amountInputs) {
        derivedStateOf {
            if (costItems.isEmpty()) {
                return@derivedStateOf false
            }

            costItems.indices.all { idx ->
                val raw = amountInputs
                    .getOrNull(idx)
                    ?.replace(",", "")
                    ?.trim()
                    .orEmpty()

                val value = raw.toDoubleOrNull() ?: return@derivedStateOf false
                value > 0.0
            }
        }
    }

    var isLoading by remember { mutableStateOf(false) }


    var lastSavedCostItems by remember { mutableStateOf<List<Costs>>(emptyList()) }
    val transformation = remember { NumberCommaTransformation() }

    val mobileNumber = remember { Preference().getUserMobile(context) }
    var allUnitsHaveOwner by remember { mutableStateOf(true) }

    val totalAmountForYear by remember(costItems) {
        mutableStateOf(
            costItems.sumOf { it.tempAmount }.toLong().coerceAtLeast(0L)
        )
    }
    val totalAmountForYearString = totalAmountForYear.toString()

    var validationMessage by remember { mutableStateOf<String?>(null) }

    var showDeleteCostDialog by remember { mutableStateOf(false) }
    var pendingDeleteCost by remember { mutableStateOf<Costs?>(null) }
    fun fiscalYearOf(cost: Costs): String? {
        val raw = cost.dueDate
        if (raw.isBlank()) return null
        val parts = raw.split("/")
        return parts.firstOrNull()
    }


    fun canDeleteCost(
        dto: Building.BuildingFullDto,
        buildingId: Long,
        currentYear: String,
        fundType: FundType,
        cost: Costs
    ): Boolean {

        // default (global) costs → not deletable
        val isDefaultCost =
            (cost.buildingId == null || cost.buildingId == 0L) &&
                    (cost.forBuildingId == null || cost.forBuildingId == 0L)

        if (isDefaultCost) return false

        val forBuildingId = cost.forBuildingId ?: cost.buildingId
        if (forBuildingId != buildingId) return false

        if (cost.fundType != fundType) return false

        val anyOtherYearCalculated = dto.costs.any { c ->
            val fb = c.forBuildingId ?: c.buildingId
            fb == buildingId &&
                    c.fundType == fundType &&
                    c.costName == cost.costName &&
                    c.tempAmount != 0.0 &&
                    fiscalYearOf(c) != null
//                    && fiscalYearOf(c) != currentYear
        }

        return !anyOtherYearCalculated
    }

    fun dedupeOperationalCosts(items: List<Costs>, selectedBuildingId: Long): List<Costs> {
        return items
            .groupBy { it.costName }
            .map { (_, list) ->
                list.firstOrNull { it.buildingId == selectedBuildingId } ?: list.first()
            }
    }


    LaunchedEffect(mobileNumber) {
        if (mobileNumber.isNullOrBlank()) return@LaunchedEffect
        Building(context).fetchBuildingsForUser(
            roleId = Preference().getRoleId(context),
            mobileNumber = mobileNumber,
            onSuccess = { list ->
                buildings = list
                if (list.size == 1 && selectedBuilding == null) {
                    selectedBuilding = list.first()
                }
            },
            onError = { }
        )
    }


    LaunchedEffect(selectedBuilding) {
        selectedBuilding?.let {
            sharedViewModel.loadOverviewData(context, it.buildingId)
        }
    }
    val globalCosts by sharedViewModel.costsList.collectAsState()
    LaunchedEffect(selectedBuilding, selectedYear, fundType, globalCosts) {
        if (selectedBuilding == null) {
            buildingFull = null
            unitsForSelectedBuilding = emptyList()
            costItems = emptyList()
            amountInputs = emptyList()
            lastSavedCostItems = emptyList()
            sharedViewModel.chargeCostsList.value = emptyList()
            chargeAlreadyCalculated = false
            isEditMode = true
            lastCalculatedCharges = null
            allUnitsHaveOwner = true
            return@LaunchedEffect
        }
        isLoading = true
        try {

            val (_, allDebts) = Cost(context).fetchAllCostsWithDebtsSuspend(buildingId = selectedBuilding!!.buildingId)
            val description = if (fundType == FundType.OPERATIONAL) "شارژ جاری" else "شارژ عمرانی"

            val debtsForYearAndBuilding = allDebts.filter { d ->
                d.buildingId == selectedBuilding!!.buildingId &&
                        d.dueDate.take(4) == selectedYear &&
                        d.description == description
            }
            val hasPaid = debtsForYearAndBuilding.any { it.paymentFlag }



            val dto = Building(context).fetchBuildingFullSuspend(
                buildingId = selectedBuilding!!.buildingId,
                fiscalYear = selectedYear
            )
            buildingFull = dto
            unitsForSelectedBuilding = dto.units

            val unitIdsWithOwner = dto.ownerUnits.map { it.unitId }.toSet()
            val hasOwnerForAllUnits = dto.units.all { it.unitId in unitIdsWithOwner }
            allUnitsHaveOwner = hasOwnerForAllUnits



            coroutineScope.launch {
                validationMessage = when {
                    unitsForSelectedBuilding.isEmpty() -> context.getString(first_compete_units)
                    !hasOwnerForAllUnits -> context.getString(R.string.first_compete_owners)
                    hasPaid -> context.getString(R.string.cannot_edit_charge_with_paid_debts)
                    else -> null
                }
                if(unitsForSelectedBuilding.isEmpty()){
                    Toast.makeText(
                        context, context.getString(first_compete_units),
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                } else {
                    if (!hasOwnerForAllUnits) {
                        Toast.makeText(
                            context, context.getString(R.string.first_compete_owners),
                            Toast.LENGTH_LONG
                        ).show()
                        return@launch
                    }
                }


            }






            val allCostsForFundType = dto.costs.filter { c ->
                val matchesFundFlag = when (fundType) {
                    FundType.OPERATIONAL -> c.chargeFlag == true
                    FundType.CAPITAL     -> c.capitalFlag == true
                    else                 -> false
                }
                c.fundType == fundType &&
                        matchesFundFlag &&
                        (c.buildingId == selectedBuilding!!.buildingId || c.buildingId == null)
                        && c.tempAmount != 0.0
            }
            val buildingId = selectedBuilding!!.buildingId

            val buildingYearCosts = allCostsForFundType.filter { c ->
                val matchesBuilding =
                    if (c.forBuildingId != null && c.forBuildingId != 0L) {
                        c.buildingId == buildingId && c.forBuildingId == buildingId
                    } else {
                        c.buildingId == buildingId
                    }

                matchesBuilding && fiscalYearOf(c) == selectedYear
            }



            fun isGlobalNoForBuilding(c: Costs): Boolean {
                val bOk = (c.buildingId == null || c.buildingId == 0L)
                val fbEmpty = (c.forBuildingId == null || c.forBuildingId == 0L)
                return bOk && fbEmpty
            }

            fun isForThisBuilding(c: Costs, selectedBId: Long): Boolean {
                return c.forBuildingId == selectedBId
            }

            val baseCosts: List<Costs> =
                if (buildingYearCosts.isNotEmpty()) {

                    val existingNames = buildingYearCosts.map { it.costName }.toSet()

                    val extraZeroCosts: List<Costs> = when (fundType) {
                        FundType.OPERATIONAL -> {
                            dto.costs
                                .filter { c ->
                                    c.fundType == FundType.OPERATIONAL &&
                                            c.chargeFlag == true &&
                                            c.buildingId == selectedBuilding!!.buildingId &&
                                            c.tempAmount == 0.0 &&
                                            c.dueDate.isBlank() &&
                                            c.costName !in existingNames
                                }
                                .groupBy { it.costName }
                                .map { (_, list) ->
                                    list.firstOrNull { it.buildingId == selectedBuilding!!.buildingId } ?: list.first()
                                }
                        }

                        FundType.CAPITAL -> {
                            val selectedBId = selectedBuilding!!.buildingId
                            val globalCosts = sharedViewModel.costsList.value
                            globalCosts
                                .filter { c ->
                                    c.fundType == FundType.CAPITAL &&
                                            fiscalYearOf(c) == null &&
                                            c.tempAmount == 0.0 &&
                                            c.costName !in existingNames
                                }
                                .filter { c ->
                                    isGlobalNoForBuilding(c) || isForThisBuilding(c, selectedBId)
                                }
                        }

                        else -> emptyList()
                    }
                    (buildingYearCosts + extraZeroCosts)

                } else {
                    when (fundType) {
                        FundType.OPERATIONAL -> {
                            dto.costs.filter { c ->
                                val matchesFundFlag = c.chargeFlag == true
                                c.fundType == FundType.OPERATIONAL &&
                                        matchesFundFlag &&
                                        c.buildingId == selectedBuilding!!.buildingId &&
                                        c.tempAmount == 0.0 &&
                                        c.dueDate.isBlank()
                            }.groupBy { it.costName }
                                .map { (_, list) ->
                                    list.firstOrNull { it.buildingId == selectedBuilding!!.buildingId } ?: list.first()
                                }
                        }

                        FundType.CAPITAL -> {
                            val selectedBId = selectedBuilding!!.buildingId
                            val globalCosts = sharedViewModel.costsList.value
                            globalCosts
                                .filter { c ->
                                    c.fundType == FundType.CAPITAL &&
                                            fiscalYearOf(c) == null
                                }
                                .filter { c ->
                                    isGlobalNoForBuilding(c) || isForThisBuilding(c, selectedBId)
                                }
                        }

                        else -> emptyList()
                    }
                }

            costItems = baseCosts
            amountInputs = baseCosts.map { cost ->
                val v = cost.tempAmount
                if (v == 0.0) "" else v.toLong().toString()
            }
            lastSavedCostItems = baseCosts
            sharedViewModel.chargeCostsList.value = baseCosts

            chargeAlreadyCalculated =
                buildingYearCosts.isNotEmpty() && baseCosts.any { it.tempAmount > 0.0 }

            isEditMode = !chargeAlreadyCalculated
            lastCalculatedCharges = null

        } catch (_: Exception) {
            buildingFull = null
            unitsForSelectedBuilding = emptyList()
            costItems = emptyList()
            amountInputs = emptyList()
            lastSavedCostItems = emptyList()
            sharedViewModel.chargeCostsList.value = emptyList()
            chargeAlreadyCalculated = false
            isEditMode = true
            lastCalculatedCharges = null
        } finally {
            isLoading = false
        }
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 75.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
            .imePadding()
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
                    .weight(0.5f),
                required = true
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
            modifier = Modifier.padding(vertical = 4.dp)
        )
        if (!validationMessage.isNullOrBlank()) {
            Text(
                text = validationMessage!!,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        }

        val dto = buildingFull
        val buildingId = selectedBuilding?.buildingId



        when (selectedCalculation) {
            CalculateMethod.AUTOMATIC.getDisplayName(context) -> {

                LazyColumn(modifier = Modifier.weight(1f)) {

                    item {
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
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        if (totalAmountForYear > 0L) {
                            SumChargeRow(totalAmountForYearString)
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }

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
                    val costList = costItems
                    items(costList.size) { index ->
                        val showDelete = dto != null && buildingId != null &&
                                isEditMode &&
                                canDeleteCost(
                                    dto = dto,
                                    buildingId = buildingId,
                                    currentYear = selectedYear,
                                    fundType = fundType,
                                    cost = costList[index]
                                )
                        CostInputRow(
                            sharedViewModel = sharedViewModel,
                            costInput = costList[index],
                            amount = amountInputs.getOrElse(index) { "0" },
                            calculateMethod = costItems[index].calculateMethod,
                            isAuto = true,
                            isEditMode = isEditMode,
                            onAmountChange = { newAmount ->
                                amountInputs = amountInputs.toMutableList().also {
                                    if (index < it.size) it[index] = newAmount else it.add(
                                        newAmount
                                    )
                                }
                                costItems = costItems.toMutableList().also {
                                    val numeric = newAmount.replace(",", "")
                                    it[index] = it[index].copy(
                                        tempAmount = numeric.toDoubleOrNull() ?: 0.0
                                    )
                                }
                            },
                            onCalculateMethodChange = { newMethod ->
                                costItems = costItems.toMutableList().also {
                                    it[index] = it[index].copy(calculateMethod = newMethod)
                                }
                            },
                            showDeleteIcon = showDelete,
                            onDeleteClick = {
                                pendingDeleteCost = costList[index]
                                showDeleteCostDialog = true
                            }
                        )

                        Spacer(Modifier.height(8.dp))
                    }
                }

                if (isEditMode) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            modifier = Modifier
                                .weight(0.4f)
                                .padding(),
                            onClick = { showAddCostDialog = true },
                            enabled = selectedBuilding != null
                        ) {
                            Text(
                                context.getString(R.string.add_new_parameter),
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1
                            )
                        }
                        Button(
                            modifier = Modifier.weight(0.25f),
                            enabled = !(isCalculateEnabled && selectedBuilding != null && allUnitsHaveOwner && unitsForSelectedBuilding.isNotEmpty()) ,
                            onClick = {
                                costItems = lastSavedCostItems.map { it.copy() }
                                isEditMode = false
                            }
                        ) {
                            Text(
                                text = context.getString(R.string.display),
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1
                            )
                        }

                        Button(
                            modifier = Modifier.weight(0.35f),
                            onClick = {
                                isEditMode = false
                                coroutineScope.launch {
                                    val dto = buildingFull ?: return@launch
                                    val units = unitsForSelectedBuilding
                                    if (units.isEmpty()) return@launch

                                    val tenantsById =
                                        dto.tenantUnits.associateBy { it.tenantId }
                                    val tenantUnits = dto.tenantUnits
                                    val tenantCountsByUnitId: Map<Long, Int> =
                                        tenantUnits.groupBy { it.unitId }.mapValues { (_, list) ->
                                            val sum = list.sumOf { tu ->
                                                val t = tenantsById[tu.tenantId]
                                                t?.numberOfTenants?.toIntOrNull() ?: 1
                                            }
                                            if (sum <= 0) 1 else sum
                                        }

                                    val getTenantCountForUnit: (Long) -> Int = { unitId ->
                                        tenantCountsByUnitId[unitId] ?: 1
                                    }

                                    data class CostInfo(
                                        val amount: Double,
                                        val method: CalculateMethod
                                    )

                                    val costsInfoMap: Map<String, CostInfo> =
                                        costItems.associate { cost ->
                                            cost.costName to CostInfo(
                                                cost.tempAmount,
                                                cost.calculateMethod
                                            )
                                        }

                                    val calculation = Calculation()

                                    val chargeMap = units.associateWith { unit ->
                                        costsInfoMap.entries.sumOf { (_, costInfo) ->
                                            calculation.calculateAmountByMethod(
                                                costAmount = if (fundType == FundType.OPERATIONAL) (costInfo.amount) / 12 else if (fundType == FundType.CAPITAL) (costInfo.amount) else (costInfo.amount),
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
                            enabled = isCalculateEnabled && selectedBuilding != null && allUnitsHaveOwner && unitsForSelectedBuilding.isNotEmpty() && validationMessage == null
                        ) {
                            Text(
                                text = context.getString(R.string.calculate),
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1
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
                        }, enabled = validationMessage == null) {
                            Text(
                                context.getString(R.string.edit),
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            CalculateMethod.EQUAL.getDisplayName(context) -> {

                LazyColumn(modifier = Modifier.weight(1f)) {

                    item {
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
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        if (totalAmountForYear > 0L) {
                            SumChargeRow(totalAmountForYearString)
                            Spacer(modifier = Modifier.height(16.dp))
                        }

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
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    val costList = costItems

                    items(costList.size) { index ->
                        val showDelete = dto != null && buildingId != null &&
                                isEditMode &&
                                canDeleteCost(
                                    dto = dto,
                                    buildingId = buildingId,
                                    currentYear = selectedYear,
                                    fundType = fundType,
                                    cost = costList[index]
                                )
                        CostInputRow(
                            sharedViewModel = sharedViewModel,
                            costInput = costList[index],
                            amount = amountInputs.getOrElse(index) { "0" },
                            calculateMethod = costItems[index].calculateMethod,
                            isEditMode = isEditMode,
                            onAmountChange = { newAmount ->
                                amountInputs = amountInputs.toMutableList().also {
                                    if (index < it.size) it[index] = newAmount else it.add(
                                        newAmount
                                    )
                                }
                                costItems = costItems.toMutableList().also {
                                    val numeric = newAmount.replace(",", "").trim()
                                    it[index] = it[index].copy(
                                        tempAmount = numeric.toDoubleOrNull() ?: 0.0
                                    )
                                }
                            },
                            isAuto = false,
                            onCalculateMethodChange = { newMethod ->
                                costItems = costItems.toMutableList().also {
                                    it[index] = it[index].copy(calculateMethod = newMethod)
                                }
                            },
                            showDeleteIcon = showDelete,
                            onDeleteClick = {
                                pendingDeleteCost = costList[index]
                                showDeleteCostDialog = true
                            }
                        )

                        Spacer(Modifier.height(8.dp))
                    }
                }

                if (isEditMode) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            modifier = Modifier
                                .weight(0.4f)
                                .padding(),
                            onClick = { showAddCostDialog = true },
                            enabled = selectedBuilding != null
                        ) {
                            Text(
                                context.getString(R.string.add_new_parameter),
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1
                            )
                        }
                        Button(
                            modifier = Modifier.weight(0.25f),
                            enabled = !(isCalculateEnabled && selectedBuilding != null && allUnitsHaveOwner && unitsForSelectedBuilding.isNotEmpty()),
                            onClick = {
                                costItems = lastSavedCostItems.map { it.copy() }
                                isEditMode = false
                            }
                        ) {
                            Text(
                                text = context.getString(R.string.display),
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1
                            )
                        }

                        Button(
                            modifier = Modifier.weight(0.35f),
                            onClick = {
                                isEditMode = false
                                coroutineScope.launch {
                                    val dto = buildingFull ?: return@launch
                                    val units = unitsForSelectedBuilding
                                    if (units.isEmpty()) return@launch

                                    val tenantsById =
                                        dto.tenantUnits.associateBy { it.tenantId }
                                    val tenantUnits = dto.tenantUnits
                                    val tenantCountsByUnitId: Map<Long, Int> =
                                        tenantUnits.groupBy { it.unitId }.mapValues { (_, list) ->
                                            val sum = list.sumOf { tu ->
                                                val t = tenantsById[tu.tenantId]
                                                t?.numberOfTenants?.toIntOrNull() ?: 1
                                            }
                                            if (sum <= 0) 1 else sum
                                        }

                                    val getTenantCountForUnit: (Long) -> Int = { unitId ->
                                        tenantCountsByUnitId[unitId] ?: 1
                                    }

                                    data class CostInfo(
                                        val amount: Double,
                                        val method: CalculateMethod
                                    )

                                    val costsInfoMap: Map<String, CostInfo> =
                                        costItems.associate { cost ->
                                            cost.costName to CostInfo(
                                                cost.tempAmount,
                                                cost.calculateMethod
                                            )
                                        }

                                    val calculation = Calculation()

                                    val chargeMap = units.associateWith { unit ->
                                        costsInfoMap.entries.sumOf { (_, costInfo) ->
                                            calculation.calculateAmountByMethod(
                                                costAmount = costInfo.amount,
                                                unit = unit,
                                                allUnits = units,
                                                calculationMethod = CalculateMethod.EQUAL,
                                                getTenantCountForUnit = getTenantCountForUnit
                                            )
                                        }
                                    }

                                    calculatedCharges = chargeMap
                                    showChargeResultDialog = true
                                }
                            },
                            enabled = isCalculateEnabled && selectedBuilding != null && allUnitsHaveOwner && unitsForSelectedBuilding.isNotEmpty() && validationMessage == null
                        ) {
                            Text(
                                text = context.getString(R.string.calculate),
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1
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
                        }, enabled = validationMessage == null) {
                            Text(
                                context.getString(R.string.edit),
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1
                            )
                        }
                    }
                }

            }

            else -> Unit
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

    }

    if (showDeleteCostDialog) {
        val cost = pendingDeleteCost
        AlertDialog(
            onDismissRequest = { showDeleteCostDialog = false },
            title = {
                Text(
                    text = context.getString(R.string.delete),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            text = {
                Text(
                    text = context.getString(R.string.are_you_sure),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (cost != null) {
                            val idx = costItems.indexOfFirst { it.costId == cost.costId }
                            if (idx != -1) {
                                costItems = costItems.toMutableList().also { it.removeAt(idx) }
                                amountInputs = amountInputs.toMutableList().also {
                                    if (idx < it.size) it.removeAt(idx)
                                }
                                lastSavedCostItems = costItems
                                sharedViewModel.chargeCostsList.value = costItems
                            }
                            if(fundType == FundType.OPERATIONAL){
                                Cost(context).deleteCostWithLinked(
                                    buildingId = selectedBuilding!!.buildingId,
                                    costId = cost.costId,
                                    onSuccess = {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.success_delete),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    },
                                    onNotFound = {

                                    },
                                    onError = {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.failed),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                )
                            } else {
                                Cost(context).deleteCost(
                                    costId = cost.costId,
                                    onSuccess = {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.success_delete),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    },
                                    onError = {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.failed),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    })
                            }
                        }

                        pendingDeleteCost = null
                        showDeleteCostDialog = false
                    }
                ) { Text(
                    context.getString(R.string.delete),
                    style = MaterialTheme.typography.bodyLarge
                ) }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        pendingDeleteCost = null
                        showDeleteCostDialog = false
                    }
                ) { Text(
                    context.getString(R.string.cancel),
                    style = MaterialTheme.typography.bodyLarge
                ) }
            }
        )
    }


    if (showAddCostDialog) {
        val notChargesCost: List<Costs> =
            buildingFull?.defaultChargeCosts
                ?.filter { defaultCost ->
                    costItems.none { it.costName == defaultCost.costName }
                }
                ?: emptyList()

        AddNewCostDialog(
            fundType = fundType,
            onDismiss = { showAddCostDialog = false },
            onChargeConfirm = { costNames ->
                coroutineScope.launch {
                    val building = selectedBuilding ?: return@launch
                    val api = Cost(context)
                    val jsonMapper = JsonMapper()

                    val newCosts = costNames.map { costName ->
                        Costs(
                            buildingId = 0L,
                            costName = costName,
                            chargeFlag = fundType == FundType.OPERATIONAL,
                            capitalFlag = fundType == FundType.CAPITAL,
                            fundType = fundType,
                            responsible = if (fundType == FundType.OPERATIONAL)
                                Responsible.TENANT else Responsible.OWNER,
                            paymentLevel = PaymentLevel.UNIT,
                            calculateMethod = CalculateMethod.EQUAL,
                            period = Period.MONTHLY,
                            tempAmount = 0.0,
                            dueDate = "",
                            costFor = costName,
                            documentNumber = "",
                            forBuildingId = building.buildingId,
                        )
                    }.toMutableList()

                    if (fundType == FundType.OPERATIONAL) {
                        newCosts += costNames.map { costName ->
                            Costs(
                                buildingId = building.buildingId,
                                costName = costName,
                                chargeFlag = fundType == FundType.OPERATIONAL,
                                capitalFlag = fundType == FundType.CAPITAL,
                                fundType = fundType,
                                responsible = if (fundType == FundType.OPERATIONAL)
                                    Responsible.TENANT else Responsible.OWNER,
                                paymentLevel = PaymentLevel.UNIT,
                                calculateMethod = CalculateMethod.EQUAL,
                                period = Period.MONTHLY,
                                tempAmount = 0.0,
                                dueDate = "",
                                costFor = costName,
                                documentNumber = "",
                                forBuildingId = building.buildingId,
                            )
                        }.toMutableList()
                    }

                    val costsJsonArray = jsonMapper.listToJsonArray(newCosts, jsonMapper::costToJson)
                    val debtsJsonArray = JSONArray()


                    api.insertCost(
                        costsJsonArray = costsJsonArray,
                        debtsJsonArray = debtsJsonArray,
                        onSuccess = { responseString ->
                            runCatching {
                                val responseJson = JSONObject(responseString)
                                val costIdsArray = responseJson.optJSONArray("costIds") ?: JSONArray()

                                val updatedNewCosts = attachServerIds(newCosts, costIdsArray)
                                val merged = costItems + updatedNewCosts

                                costItems = if (fundType == FundType.OPERATIONAL && selectedBuilding != null) {
                                    dedupeOperationalCosts(merged, selectedBuilding!!.buildingId)
                                } else {
                                    merged
                                }

                                amountInputs = costItems.map { c ->
                                    val v = c.tempAmount
                                    if (v == 0.0) "" else v.toLong().toString()
                                }

                                lastSavedCostItems = costItems
                                sharedViewModel.chargeCostsList.value = costItems
                                showAddCostDialog = false
                            }.onFailure { t ->
                                Log.e("ChargeCalculation", "insertCost parse failed: ${t.message}", t)
                            }
                        },
                        onError = { e ->
                            Log.e("ChargeCalculation", "insertCost failed: ${e.message}", e)
                        }
                    )

                }
            },
            costs = notChargesCost
        )
    }

    if (showChargeResultDialog) {
        ChargeResultDialog(
            results = calculatedCharges,
            fundType = fundType,
            onSave = {
                val building = selectedBuilding ?: return@ChargeResultDialog
                val results = calculatedCharges ?: return@ChargeResultDialog
                val fiscalYearInt = selectedYear.toIntOrNull() ?: return@ChargeResultDialog

                val api = Cost(context)
                val jsonMapper = JsonMapper()
                val fiscalYearStart = "$selectedYear/01/01"
                val description = if (fundType == FundType.OPERATIONAL) "شارژ جاری" else "شارژ عمرانی"
                coroutineScope.launch {
                    try {
                        val (_, allDebts) = api.fetchAllCostsWithDebtsSuspend(buildingId = building.buildingId)

                        val debtsForYearAndBuilding = allDebts.filter { d ->
                            d.buildingId == building.buildingId &&
                                    d.dueDate.take(4) == selectedYear &&
                                    d.description == description
                        }
                        val hasPaid = debtsForYearAndBuilding.any { it.paymentFlag }
                        if (hasPaid) {
                            showChargeResultDialog = false
                            Toast.makeText(context, context.getString(R.string.cannot_edit_charge_with_paid_debts),
                                Toast.LENGTH_LONG).show()
                            return@launch
                        }
                        if (chargeAlreadyCalculated && debtsForYearAndBuilding.isNotEmpty()) {
                            Toast.makeText(context, context.getString(R.string.charge_will_replace_previous),
                                Toast.LENGTH_LONG).show()
                        }

                        val existingDebtsIndex = debtsForYearAndBuilding.associateBy {
                            Triple(it.unitId, it.dueDate, it.description)
                        }

                        val costsToSave: List<Costs> = costItems.mapIndexed { index, c ->
                            val raw = amountInputs
                                .getOrNull(index)
                                ?.replace(",", "")
                                ?.trim()
                            val amount = raw?.toDoubleOrNull() ?: c.tempAmount
                            c.copy(
                                buildingId = building.buildingId,
                                tempAmount = amount,
                                dueDate = fiscalYearStart,
                                chargeFlag = fundType == FundType.OPERATIONAL,
                                capitalFlag = fundType == FundType.CAPITAL,
                                forBuildingId = if(c.forBuildingId != 0L) building.buildingId else 0L,
                            )
                        }
                        val costsJsonArray = jsonMapper.listToJsonArray(costsToSave, jsonMapper::costToJson)

                        val debtsToSave = mutableListOf<Debts>()

                        for ((unit, yearlyAmount) in results) {
                            if (yearlyAmount <= 0.0) continue
                            if (fundType == FundType.OPERATIONAL) {
                                for (month in 1..12) {
                                    val dueDate = String.format(
                                        "%04d/%02d/01",
                                        fiscalYearInt,
                                        month
                                    )
                                    val key = Triple(unit.unitId, dueDate, description)
                                    val existing = existingDebtsIndex[key]

                                    debtsToSave += Debts(
                                        debtId = existing?.debtId ?: 0L,
                                        unitId = unit.unitId,
                                        costId = existing?.costId ?: 1L,
                                        ownerId = existing?.ownerId ?: 0L,
                                        buildingId = building.buildingId,
                                        description = description,
                                        dueDate = dueDate,
                                        amount = yearlyAmount,
                                        paymentFlag = existing?.paymentFlag ?: false
                                    )
                                }
                            } else if (fundType == FundType.CAPITAL) {
                                val dueDate = fiscalYearStart
                                val key = Triple(unit.unitId, dueDate, description)
                                val existing = existingDebtsIndex[key]
                                val baseCostId = existing?.costId
                                    ?: costsToSave.firstOrNull()?.costId
                                    ?: 0L
                                debtsToSave += Debts(
                                    debtId = existing?.debtId ?: 0L,
                                    unitId = unit.unitId,
                                    costId = baseCostId,
                                    ownerId = existing?.ownerId ?: 0L,
                                    buildingId = building.buildingId,
                                    description = description,
                                    dueDate = dueDate,
                                    amount = yearlyAmount,
                                    paymentFlag = existing?.paymentFlag == true
                                )
                            }
                        }

                        val debtsJsonArray = JSONArray().apply {
                            debtsToSave.forEach { d ->
                                put(jsonMapper.debtToJson(d))
                            }
                        }
                        api.insertCost(
                            costsJsonArray = costsJsonArray,
                            debtsJsonArray = debtsJsonArray,
                            onSuccess = { _: String ->
                                coroutineScope.launch {
                                    lastCalculatedCharges = calculatedCharges
                                    isEditMode = false
                                    chargeAlreadyCalculated = true

                                    showChargeResultDialog = false
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.charge_calcualted_successfully),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            onError = { e ->
                                Log.e("ChargeCalculation", "insertCost failed", e)
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.failed),
                                    Toast.LENGTH_LONG
                                ).show()
                                showChargeResultDialog = false
                            }
                        )

                    } catch (e: Exception) {
                        Log.e("ChargeCalculation", "save failed", e)
                        Toast.makeText(context, context.getString(R.string.failed),
                            Toast.LENGTH_LONG).show()
                        showChargeResultDialog = false
                    }
                }
            },
            onDismiss = { showChargeResultDialog = false }
        )
    }
}

@Composable
fun CapitalChargeCalculationScreen(sharedViewModel: SharedViewModel) {
    CalculationScreen(
        sharedViewModel = sharedViewModel,
        fundType = FundType.CAPITAL
    )
}

private fun attachServerIds(
    localCosts: List<Costs>,
    costIdsArray: JSONArray
): List<Costs> {
    val ids = List(costIdsArray.length()) { i -> costIdsArray.getLong(i) }

    if (ids.size != localCosts.size) {
        Log.e("InsertCostServer", "IDs count (${ids.size}) != costs count (${localCosts.size})")
    }

    val n = minOf(ids.size, localCosts.size)
    val updated = localCosts.take(n).mapIndexed { index, cost ->
        cost.copy(costId = ids[index])
    }

    return if (localCosts.size > n) updated + localCosts.drop(n) else updated
}

