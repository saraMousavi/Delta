package com.example.delta

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Debts
import com.example.delta.data.entity.Owners
import com.example.delta.data.entity.OwnersUnitsCrossRef
import com.example.delta.data.entity.Units
import com.example.delta.enums.CalculateMethod
import com.example.delta.sharedui.DebtItem
import com.example.delta.viewmodel.SharedViewModel
import ir.hamsaa.persiandatepicker.util.PersianCalendar
import kotlin.collections.find
import kotlin.collections.set


class OwnerDetailsActivity : ComponentActivity() {
    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ownerId = intent.getLongExtra("ownerId", -1L)
        setContent {
            AppTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    OwnerDetailsScreen(
                        ownerId = ownerId,
                        sharedViewModel = sharedViewModel,
                        onBack = { finish() })
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDetailsScreen(
    ownerId: Long,
    sharedViewModel: SharedViewModel,
    onBack: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val context = LocalContext.current
    val tabTitles = listOf(
        context.getString(R.string.overview),
        context.getString(R.string.debt),
        context.getString(R.string.payment)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        context.getString(R.string.owner_details),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = context.getString(R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 0.dp
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, style = MaterialTheme.typography.bodyLarge) }
                    )
                }
            }
            when (selectedTab) {
                0 -> OwnerOverviewTab(ownerId = ownerId, sharedViewModel = sharedViewModel)
                1 -> OwnerDebtTab(ownerId = ownerId, sharedViewModel = sharedViewModel)
                2 -> OwnerPaymentTab(ownerId = ownerId, sharedViewModel = sharedViewModel)
            }
        }
    }
}

@Composable
fun OwnerOverviewTab(
    ownerId: Long,
    sharedViewModel: SharedViewModel,
    modifier: Modifier = Modifier
) {
    val owner by sharedViewModel.getOwner(ownerId).collectAsState(initial = null)
    var isEditing by remember { mutableStateOf(false) }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    LaunchedEffect(owner) {
        if (owner != null) {
            firstName = owner!!.firstName
            lastName = owner!!.lastName
            email = owner!!.email
            phone = owner!!.phoneNumber
        }
    }
    var context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                if (owner == null) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 24.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = context.getString(R.string.loading),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    Text(
                        text = context.getString(R.string.owner_details),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Editable fields
                    if (isEditing) {
                        OutlinedTextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            label = {
                                Text(
                                    context.getString(R.string.first_name),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            label = {
                                Text(
                                    context.getString(R.string.last_name),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = {
                                Text(
                                    context.getString(R.string.last_name),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = {
                                Text(
                                    context.getString(R.string.phone_number),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(16.dp))
                        Row {
                            Button(
                                onClick = {
                                    // Save changes
//                                    sharedViewModel.updateOwnerWithUnits(
//                                        owner!!.copy(
//                                            firstName = firstName,
//                                            lastName = lastName,
//                                            email = email,
//                                            phoneNumber = phone
//                                        )
//                                    )
                                    isEditing = false
                                }
                            ) {
                                Text(
                                    context.getString(R.string.insert),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            OutlinedButton(onClick = { isEditing = false }) {
                                Text(
                                    context.getString(R.string.cancel),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    } else {
                        // Display fields
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Owner",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "${owner!!.firstName} ${owner!!.lastName}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = owner!!.email.ifBlank { context.getString(R.string.no_email) },
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Phone",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = owner!!.phoneNumber.ifBlank { context.getString(R.string.no_phone) },
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // FAB for editing
        if (!isEditing && owner != null) {
            FloatingActionButton(
                onClick = { isEditing = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(32.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
        }
    }
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun OwnerDebtTab(ownerId: Long, sharedViewModel: SharedViewModel) {

    var selectedYear by rememberSaveable { mutableIntStateOf(PersianCalendar().persianYear) }
    var selectedMonth by rememberSaveable { mutableIntStateOf(PersianCalendar().persianMonth) }
    var showDebtDialog by remember { mutableStateOf(false) }
    val debts by sharedViewModel.getDebtsForOwner(
        ownerId, selectedYear.toString(),
        selectedMonth.toString().padStart(2, '0')
    )
        .collectAsState(initial = emptyList())
    Log.d("debts", debts.toString())
    var context = LocalContext.current
    if (debts.isNotEmpty()) {
        val buildingsId = debts[0].buildingId
        val units by sharedViewModel.getUnitsForBuilding(buildingId = buildingsId)
            .collectAsState(initial = emptyList())
        val owners by sharedViewModel.getOwnersForBuilding(buildingsId)
            .collectAsState(initial = emptyList())
        val crossRef by sharedViewModel.getOwnerUnitsCrossRefs(ownerId)
            .collectAsState(initial = emptyList())
        val areaByOwner: Map<Long, Double> = calculateAreaByOwners(owners, units, crossRef)
//        val payments = calculateOwnerPaymentsPerCost(sharedViewModel, debts, areaByOwner, crossRef)
        // Collect all costs as a map for fast lookup
        // Get all unique costIds from debts
        val costIds = debts.map { it.costId }.distinct()
        Log.d("costIds", costIds.toString())
        val costs: Map<Long, Costs?> = costIds.associateWith { costId ->
            sharedViewModel.getCost(costId).collectAsState(initial = null).value
        }
        Log.d("costs", costs.toString())
        val payments by remember(debts, areaByOwner, crossRef) {
            derivedStateOf {
                calculateOwnerPaymentsPerCost(debts, areaByOwner, crossRef, costs)
            }
        }

        Log.d("payments", payments.toString())
        LaunchedEffect(payments) {
            // Calculate payments per costId for this owner

            // Convert to a list of Debts or your UI model to add to unpaid debt list
            val unpaidDebts = payments.mapNotNull { (costId, amount) ->
                // Find a representative debt to get buildingId, description, dueDate for this costId
                Log.d("amount", amount.toString())
                val representativeDebt =
                    debts.find { it.costId == costId } ?: return@mapNotNull null
                Debts(
                    debtId = representativeDebt.debtId,
                    costId = costId,
                    unitId = representativeDebt.unitId, // or -1 if you want to indicate owner-level debt
                    buildingId = representativeDebt.buildingId,
                    description = representativeDebt.description,
                    dueDate = representativeDebt.dueDate,
                    amount = amount,
                    paymentFlag = false
                )
            }
            Log.d("unpaidDebts", unpaidDebts.toString())

            sharedViewModel.addUnpaidDebtListList(unpaidDebts)
        }
    } else {
        sharedViewModel.unpaidDebtList.value = emptyList()
    }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDebtDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            // debt items
            YearMonthSelector(
                selectedYear = selectedYear,
                onYearChange = { selectedYear = it },
                selectedMonth = selectedMonth,
                onMonthChange = { selectedMonth = it }
            )
            Log.d(
                "sharedViewModel.unpaidDebtList.value",
                sharedViewModel.unpaidDebtList.value.toString()
            )
            if (sharedViewModel.unpaidDebtList.value.isEmpty()) {
                Text(
                    text = context.getString(R.string.no_debts_recorded),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {

                sharedViewModel.unpaidDebtList.value.forEach { debt ->
                    DebtItem(debt = debt, onPayment = {
                        Log.d("debt", debt.toString())
                        val updatedDebt = debt.copy(paymentFlag = true)
                        sharedViewModel.updateDebt(updatedDebt) // Use SharedViewModel to update
                        sharedViewModel.updateDebtPaymentFlag(debt, true)
                        Toast.makeText(
                            context, context.getString(R.string.success_pay),
                            Toast.LENGTH_SHORT
                        ).show()
                    })
                }
            }
        }
    }

    if (showDebtDialog) {
//        EarningsDialog(
//            building = building,
//            onDismiss = { buildingViewModel.hideDialogs() },
//            onConfirm = { earning ->
//                buildingViewModel.insertEarnings(earning)
//                buildingViewModel.hideDialogs()
//            },
//            sharedViewModel = sharedViewModel
//        )
    }
}

@Composable
fun OwnerPaymentTab(ownerId: Long, sharedViewModel: SharedViewModel) {
    val payments by sharedViewModel.getPaysForOwner(ownerId = ownerId)
        .collectAsState(initial = emptyList())

    var context = LocalContext.current
    if (payments.isNotEmpty()) {
        val buildingsId = payments[0].buildingId
        val crossRef by sharedViewModel.getOwnerUnitsCrossRefs(ownerId)
            .collectAsState(initial = emptyList())
        val units by sharedViewModel.getUnitsForBuilding(buildingId = buildingsId)
            .collectAsState(initial = emptyList())
        val owners by sharedViewModel.getOwnersForBuilding(buildingsId)
            .collectAsState(initial = emptyList())
        val areaByOwner: Map<Long, Double> = calculateAreaByOwners(owners, units, crossRef)
        val costIds = payments.map { it.costId }.distinct()

        val costs: Map<Long, Costs?> = costIds.associateWith { costId ->
            sharedViewModel.getCost(costId).collectAsState(initial = null).value
        }
        val payment =
            calculateOwnerPaymentsPerCost( payments, areaByOwner, crossRef, costs)


        LaunchedEffect(payments) {
            // Calculate payments per costId for this owner

            // Convert to a list of Debts or your UI model to add to unpaid debt list
            val paidDebts = payment.mapNotNull { (costId, amount) ->
                // Find a representative debt to get buildingId, description, dueDate for this costId
                val representativeDebt =
                    payments.find { it.costId == costId } ?: return@mapNotNull null
                Debts(
                    costId = costId,
                    unitId = representativeDebt.unitId, // or -1 if you want to indicate owner-level debt
                    buildingId = representativeDebt.buildingId,
                    description = representativeDebt.description,
                    dueDate = representativeDebt.dueDate,
                    amount = amount,
                    paymentFlag = true
                )
            }

            sharedViewModel.addUnpaidDebtListList(paidDebts)
        }
    }
    Column(modifier = Modifier.padding(16.dp)) {
        Log.d("sharedViewModel.unpaidDebtList.value","1")
        if (sharedViewModel.unpaidDebtList.value.isEmpty()) {
            Text(
                text = context.getString(R.string.no_payments_recorded),
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            payments.forEach { payment ->
                DebtItem(debt = payment, onPayment = {
                    sharedViewModel.updateDebt(payment)
                })
            }
        }
    }
}

fun calculateOwnerPaymentsPerCost(
    debts: List<Debts>,
    areaByOwner: Map<Long, Double>,
    ownersUnitsCrossRefs: List<OwnersUnitsCrossRef>,
    costs: Map<Long, Costs?>
): Map<Long, Double> {  // Map<ownerId, amount>
    if (debts.isEmpty()) return emptyMap()

    val costPayments = mutableMapOf<Long, Double>()

    // Group debts by costId
    val debtsByCost = debts.groupBy { it.costId }

    debtsByCost.forEach { (costId, debtsForCost) ->
        var totalAmountForCost = 0.0

        val cost = costs[costId]
        if (cost == null) return@forEach

        // We'll accumulate per-owner shares for this costId
        val ownerShares = mutableMapOf<Long, Double>()

        debtsForCost.forEach { debt ->
            val unitId = debt.unitId
            val debtAmount = debt.amount
            when (cost.calculateMethod.name) {
                    CalculateMethod.DANG.name -> {
// Sum total dang for all owners across all units
                        val totalDang = ownersUnitsCrossRefs.sumOf { it.dang }
                        Log.d("totalDang", totalDang.toString())
                        // Calculate each owner's total dang
                        val dangByOwner = ownersUnitsCrossRefs.groupBy { it.ownerId }
                            .mapValues { entry -> entry.value.sumOf { it.dang } }

                        // Calculate proportional share
                        dangByOwner.forEach { (ownerId, ownerDang) ->
                            val share =
                                if (totalDang > 0) (ownerDang / totalDang) * debt.amount else 0.0
                            costPayments[ownerId] = share
                        }
                    }

                    CalculateMethod.AREA.name -> {
                        val totalArea: Double = areaByOwner.values.sumByDouble { it }


                        // Calculate each owner's total area (sum of areas of units they own)
//                    val areaByOwner = ownersUnitsCrossRefs.groupBy { it.ownerId }
//                        .mapValues { entry ->
//                            entry.value.sumOf { ou ->
//                                units.find { it.unitId == ou.unitId }?.area ?: 0.0
//                            }
//                        }

                        // Calculate proportional share by area
                        areaByOwner.forEach { (ownerId, ownerArea) ->
                            val share =
                                if (totalArea > 0) (ownerArea / totalArea) * debt.amount else 0.0
                            costPayments[ownerId] = share
                        }
                    }

                    CalculateMethod.FIXED.name -> {
// Get distinct owners
                        val distinctOwners = ownersUnitsCrossRefs.map { it.ownerId }.distinct()
                        Log.d("distinctOwners", distinctOwners.toString())
                        val sharePerOwner =
                            if (distinctOwners.isNotEmpty()) debt.amount / distinctOwners.size else 0.0
                        Log.d("sharePerOwner", sharePerOwner.toString())
                        distinctOwners.forEach { ownerId ->
                            costPayments[ownerId] = sharePerOwner
                        }
                    }

                }
            // Find owner's dang for this unit
            val ownerUnit = ownersUnitsCrossRefs.find { it.unitId == unitId }
            val ownerDang = ownerUnit?.dang ?: 0.0
            Log.d("ownerDang", ownerDang.toString())
            val share = (ownerDang / 6) * debtAmount
            Log.d("share", share.toString())
            totalAmountForCost += share
            Log.d("totalAmountForCost", totalAmountForCost.toString())

        }
        // Add the per-owner shares for this costId to the final result
//        ownerShares.forEach { (ownerId, amount) ->
//            Log.d("amounttt", amount.toString())
//            costPayments[costId] = (costPayments[ownerId] ?: 0.0) + amount
//        }
        costPayments[costId] = totalAmountForCost
    }

    return costPayments
}


/**
 * Calculate total owned area per owner.
 *
 * @param owners List of owners to calculate area for.
 * @param units List of all units in the building.
 * @param ownersUnitsCrossRefs List of ownership shares (dang) linking owners to units.
 * @return Map where key = ownerId, value = total area owned by that owner.
 */
fun calculateAreaByOwners(
    owners: List<Owners>,
    units: List<Units>,
    ownersUnitsCrossRefs: List<OwnersUnitsCrossRef>
): Map<Long, Double> {
    // Group ownership shares by ownerId for quick lookup
    val crossRefsByOwner = ownersUnitsCrossRefs.groupBy { it.ownerId }

    // Map each owner to their total owned area
    return owners.associate { owner ->
        val ownerCrossRefs = crossRefsByOwner[owner.ownerId] ?: emptyList()

        // Sum area of units weighted by owner's dang share
        val totalArea = ownerCrossRefs.sumOf { crossRef ->
            val unitArea =
                units.find { it.unitId == crossRef.unitId }?.area?.persianToEnglishDigits()
                    ?.toDouble() ?: 0.0
            unitArea * crossRef.dang
        }

        Log.d("totalArea", totalArea.toString())

        owner.ownerId to totalArea
    }
}


