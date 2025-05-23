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
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.MobileFriendly
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    var mobile by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    LaunchedEffect(owner) {
        if (owner != null) {
            firstName = owner!!.firstName
            lastName = owner!!.lastName
            email = owner!!.email
            phone = owner!!.phoneNumber
            mobile = owner!!.mobileNumber
            address = owner!!.address
        }
    }
    var context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = Color(context.getColor(R.color.primary_color)))
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
                        OutlinedTextField(
                            value = mobile,
                            onValueChange = { mobile = it },
                            label = {
                                Text(
                                    context.getString(R.string.mobile_number),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = {
                                Text(
                                    context.getString(R.string.address),
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
                                    sharedViewModel.updateOwner(
                                        owner!!.copy(
                                            firstName = firstName,
                                            lastName = lastName,
                                            email = email,
                                            phoneNumber = phone,
                                            mobileNumber = mobile,
                                            address = address,
                                        ),
                                        onError = {
                                            Toast.makeText(context, context.getString(R.string.operation_problem),
                                                Toast.LENGTH_LONG).show()
                                        }
                                    )
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
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MobileFriendly,
                                contentDescription = "Mobile",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = owner!!.mobileNumber.ifBlank { context.getString(R.string.no) },
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HomeWork,
                                contentDescription = "Address",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = owner!!.address.ifBlank { context.getString(R.string.no) },
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
    var context = LocalContext.current
    var selectedYear by rememberSaveable { mutableIntStateOf(PersianCalendar().persianYear) }
    var selectedMonth by rememberSaveable { mutableIntStateOf(PersianCalendar().persianMonth) }
    var showDebtDialog by remember { mutableStateOf(false) }
    Log.d("ownerId", ownerId.toString())
    val debts by sharedViewModel.getDebtsForOwner(
        ownerId, selectedYear.toString(),
        selectedMonth.toString().padStart(2, '0')
    ).collectAsState(initial = emptyList())
    Log.d("debts pay", debts.toString())

    val buildingsId = debts.firstOrNull()?.buildingId

//    val units by sharedViewModel.getUnitsForBuilding(buildingId = buildingsId)
//        .collectAsState(initial = emptyList())
//    val owners by sharedViewModel.getOwnersForBuilding(buildingsId)
//        .collectAsState(initial = emptyList())
//    val crossRef by sharedViewModel.getOwnerUnitsCrossRefs(ownerId)
//        .collectAsState(initial = emptyList())

    // Fetch all costs at once (ideally from ViewModel, not in a loop)
//    val costIds = debts.map { it.costId }.distinct()
//    Log.d("costIds pay", costIds.toString())
//    val costs by sharedViewModel.getCostsByIds(costIds)
//        .collectAsState(initial = emptyList())
//    Log.d("costs pay", costs.toString())

//    val costsMap = remember(costs) { costs.associateBy { it.id } }
//    Log.d("costsMap pay", costsMap.toString())
//
//    val areaByOwner = remember(owners, units, crossRef) {
//        calculateAreaByOwners(owners, units, crossRef)
//    }
//    val payments = remember(debts, areaByOwner, crossRef, costsMap) {
//        calculateOwnerPaymentsPerCost(debts, areaByOwner, crossRef, costsMap)
//    }

    LaunchedEffect(debts) {
//        // Calculate payments per costId for this owner
//        Log.d("payments", payments.toString())
//        // Convert to a list of Debts or your UI model to add to unpaid debt list
//        val unpaidDebts = payments.mapNotNull { (costId, amount) ->
//            // Find a representative debt to get buildingId, description, dueDate for this costId
//            Log.d("amount", amount.toString())
//            val representativeDebt =
//                debts.find { it.costId == costId } ?: return@mapNotNull null
//            Debts(
//                debtId = representativeDebt.debtId,
//                costId = costId,
//                unitId = representativeDebt.unitId, // or -1 if you want to indicate owner-level debt
//                buildingId = representativeDebt.buildingId,
//                description = representativeDebt.description,
//                dueDate = representativeDebt.dueDate,
//                amount = amount,
//                paymentFlag = false
//            )
//        }
//        Log.d("unpaidDebts", unpaidDebts.toString())
//
        sharedViewModel.addUnpaidDebtListList(debts)
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
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
                Log.d(
                    "sharedViewModel.unpaidDebtList.value",
                    sharedViewModel.unpaidDebtList.value.toString()
                )
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
//    Log.d("payments debt", payments.toString())

    var context = LocalContext.current
//    val buildingsId = payments.firstOrNull()?.buildingId ?: return

//    val units by sharedViewModel.getUnitsForBuilding(buildingId = buildingsId)
//        .collectAsState(initial = emptyList())
//    val owners by sharedViewModel.getOwnersForBuilding(buildingsId)
//        .collectAsState(initial = emptyList())
//    val crossRef by sharedViewModel.getOwnerUnitsCrossRefs(ownerId)
//        .collectAsState(initial = emptyList())
//
//    // Fetch all costs at once (ideally from ViewModel, not in a loop)
//    val costIds = payments.map { it.costId }.distinct()
//    val costs by sharedViewModel.getCostsByIds(costIds)
//        .collectAsState(initial = emptyList())
//
//    val costsMap = remember(costs) { costs.associateBy { it.id } }
//    Log.d("cost mao", costsMap.toString())
//
//    val areaByOwner = remember(owners, units, crossRef) {
//        calculateAreaByOwners(owners, units, crossRef)
//    }
//    val payment = remember(payments, areaByOwner, crossRef, costsMap) {
//        calculateOwnerPaymentsPerCost(payments, areaByOwner, crossRef, costsMap)
//    }
//    Log.d("payments", payments.toString())


    LaunchedEffect(payments) {
        // Calculate payments per costId for this owner

        // Convert to a list of Debts or your UI model to add to unpaid debt list
//        val paidDebts = payment.mapNotNull { (costId, amount) ->
//            // Find a representative debt to get buildingId, description, dueDate for this costId
//            val representativeDebt =
//                payments.find { it.costId == costId } ?: return@mapNotNull null
//            Debts(
//                debtId = representativeDebt.debtId,
//                costId = costId,
//                unitId = representativeDebt.unitId, // or -1 if you want to indicate owner-level debt
//                buildingId = representativeDebt.buildingId,
//                description = representativeDebt.description,
//                dueDate = representativeDebt.dueDate,
//                amount = amount,
//                paymentFlag = true
//            )
//        }

        sharedViewModel.addUnpaidDebtListList(payments)
    }
    Column(modifier = Modifier.padding(16.dp)) {
        Log.d("sharedViewModel.unpaidDebtList.value", "1")
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
