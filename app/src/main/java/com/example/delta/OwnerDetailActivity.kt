package com.example.delta

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MobileFriendly
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Debts
import com.example.delta.data.entity.OwnerTabItem
import com.example.delta.data.entity.OwnerTabType
import com.example.delta.data.entity.OwnersUnitsCrossRef
import com.example.delta.data.entity.Units
import com.example.delta.data.entity.User
import com.example.delta.enums.FilterType
import com.example.delta.enums.FundType
import com.example.delta.viewmodel.SharedViewModel
import com.example.delta.volley.Cost
import com.example.delta.volley.Fund
import com.example.delta.volley.Owner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class OwnerDetailsActivity : ComponentActivity() {
    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ownerId = intent.getLongExtra("ownerId", -1L)
        val buildingId = intent.getLongExtra("buildingId", -1L)
        setContent {
            AppTheme(useDarkTheme = sharedViewModel.isDarkModeEnabled) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    OwnerDetailsScreen(
                        ownerId = ownerId,
                        buildingId = buildingId,
                        sharedViewModel = sharedViewModel,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDetailsScreen(
    ownerId: Long,
    buildingId:Long,
    sharedViewModel: SharedViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val tabs = listOf(
        OwnerTabItem(context.getString(R.string.overview), OwnerTabType.OVERVIEW),
        OwnerTabItem(context.getString(R.string.transaction), OwnerTabType.FINANCIALS)
    )

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

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
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OwnerSectionSelector(
                tabs = tabs,
                selectedIndex = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            when (tabs[selectedTab].type) {
                OwnerTabType.OVERVIEW -> OwnerOverviewTab(ownerId, buildingId = buildingId )
                OwnerTabType.FINANCIALS -> OwnerFinancialsTab(
                    ownerId = ownerId,
                    sharedViewModel = sharedViewModel,
                    snackBarHostState = snackbarHostState,
                    coroutineScope = coroutineScope
                )
            }
        }
    }
}
@Composable
fun OwnerOverviewTab(
    ownerId: Long,
    buildingId: Long,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val ownerApi = remember { com.example.delta.volley.Owner() }

    var user by remember { mutableStateOf<User?>(null) }
    var unitsForOwner by remember { mutableStateOf<List<Units>>(emptyList()) }
    var ownerUnits by remember { mutableStateOf<List<OwnersUnitsCrossRef>>(emptyList()) }
    var editableOwnerUnits by remember { mutableStateOf<List<OwnersUnitsCrossRef>>(emptyList()) }

    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var isEditing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var units by remember { mutableStateOf<List<Units>>(emptyList()) }
    var ownersWithUnits by remember { mutableStateOf<List<Owner.OwnerWithUnitsDto>>(emptyList()) }

    val dangSums = remember(ownersWithUnits) {
        ownersWithUnits
            .flatMap { it.ownerUnits }
            .groupBy { it.unitId }
            .mapValues { (_, list) -> list.sumOf { it.dang } }
    }

    val selectedUnitsList = remember { mutableStateListOf<OwnersUnitsCrossRef>() }

    val selectableUnits by remember(units, dangSums) {
        derivedStateOf {
            units.filter { u ->
                val usedDang = dangSums[u.unitId] ?: 0.0
                usedDang < 6.0
            }
        }
    }

    LaunchedEffect(ownerId, buildingId) {
        isLoading = true
        errorMessage = null

        Owner().getOwnersWithUnitsByBuilding(
            context = context,
            buildingId = buildingId,
            onSuccess = { list ->
                ownersWithUnits = list
            },
            onError = {
                Toast.makeText(
                    context,
                    it.message ?: context.getString(R.string.failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        com.example.delta.volley.Units().fetchUnitsForBuilding(
            context = context,
            buildingId = buildingId,
            onSuccess = { list ->
                units = list
            },
            onError = { e ->
                Toast.makeText(
                    context,
                    e.message ?: context.getString(R.string.failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        ownerApi.getOwnerWithUnits(
            context = context,
            ownerId = ownerId,
            onSuccess = { dto ->
                user = dto.user
                ownerUnits = dto.ownerUnits
                unitsForOwner = dto.units
                editableOwnerUnits = dto.ownerUnits
                isLoading = false
            },
            onError = { e ->
                errorMessage = e.message ?: "Error loading owner"
                isLoading = false
                coroutineScope.launch {
                    snackBarHostState.showSnackbar(errorMessage ?: "")
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(16.dp),
                contentPadding = PaddingValues(bottom = 72.dp),
                state = listState
            ) {
                if (user == null && isLoading) {
                    item {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(vertical = 24.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = context.getString(R.string.loading),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (user == null && errorMessage != null) {
                    item {
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else if (user != null) {
                    item {
                        OwnerInfoRow(
                            Icons.Default.Person,
                            "${user!!.firstName} ${user!!.lastName}"
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }

                    item {
                        OwnerInfoRow(
                            Icons.Default.Email,
                            user!!.email?.ifBlank { context.getString(R.string.no_email) } ?: ""
                        )
                    }
                    item { Spacer(Modifier.height(12.dp)) }

                    item {
                        OwnerInfoRow(
                            Icons.Default.Phone,
                            user!!.phoneNumber!!.ifBlank { context.getString(R.string.no_phone) }
                        )
                    }
                    item { Spacer(Modifier.height(12.dp)) }

                    item {
                        OwnerInfoRow(
                            Icons.Default.MobileFriendly,
                            user!!.mobileNumber.ifBlank { context.getString(R.string.no) }
                        )
                    }
                    item { Spacer(Modifier.height(12.dp)) }

                    item {
                        OwnerInfoRow(
                            Icons.Default.HomeWork,
                            user!!.address?.ifBlank { context.getString(R.string.no) }.toString()
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }

                    item {
                        Text(
                            text = context.getString(R.string.units),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    if (!isEditing) {
                        items(ownerUnits, key = { "own-${it.unitId}" }) { unitCross ->
                            val thisUnit = unitsForOwner.firstOrNull { it.unitId == unitCross.unitId }
                            if (thisUnit != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${context.getString(R.string.unit_number)}: ${thisUnit.unitNumber}, " +
                                                "${context.getString(R.string.area)}: ${thisUnit.area}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${context.getString(R.string.dang)}: ${unitCross.dang}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(editableOwnerUnits, key = { "editable-${it.unitId}" }) { unitCross ->
                            val thisUnit = unitsForOwner.firstOrNull { it.unitId == unitCross.unitId }
                            val unitLabel = if (thisUnit != null) {
                                "${context.getString(R.string.unit_number)}: ${thisUnit.unitNumber}, " +
                                        "${context.getString(R.string.area)}: ${thisUnit.area}"
                            } else {
                                "${context.getString(R.string.unit_number)}: ${unitCross.unitId}"
                            }

                            var dangText by remember(unitCross.unitId) {
                                mutableStateOf(unitCross.dang.toString())
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = unitLabel,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    OutlinedTextField(
                                        value = dangText,
                                        onValueChange = { newValue ->
                                            dangText = newValue
                                            val asDouble = newValue.toDoubleOrNull() ?: 0.0
                                            editableOwnerUnits = editableOwnerUnits.map {
                                                if (it.unitId == unitCross.unitId) {
                                                    it.copy(dang = asDouble)
                                                } else it
                                            }
                                        },
                                        label = { Text(context.getString(R.string.dang)) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        editableOwnerUnits =
                                            editableOwnerUnits.filterNot { it.unitId == unitCross.unitId }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove unit"
                                    )
                                }
                            }
                        }

                        item {
                            Text(
                                text = context.getString(R.string.select_new_units_and_dang),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        }

                        items(selectableUnits, key = { it.unitId }) { unit ->
                            val usedDang = dangSums[unit.unitId] ?: 0.0
                            val maxAllowed = (6.0 - usedDang).coerceAtLeast(0.0)

                            val currentSelection =
                                selectedUnitsList.firstOrNull { it.unitId == unit.unitId }

                            var localDangText by remember(
                                unit.unitId,
                                currentSelection?.dang
                            ) {
                                mutableStateOf(
                                    currentSelection?.dang
                                        ?.takeIf { it > 0.0 }
                                        ?.toString()
                                        ?: ""
                                )
                            }

                            val isChecked = currentSelection != null

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            val v = localDangText.toDoubleOrNull() ?: 0.0
                                            val clamped = v.coerceIn(0.0, maxAllowed)
                                            selectedUnitsList.removeAll { it.unitId == unit.unitId }
                                            selectedUnitsList.add(
                                                OwnersUnitsCrossRef(
                                                    ownerId = 0L,
                                                    unitId = unit.unitId,
                                                    dang = clamped
                                                )
                                            )
                                        } else {
                                            selectedUnitsList.removeAll { it.unitId == unit.unitId }
                                        }
                                    }
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${context.getString(R.string.unit_number)}: ${unit.unitNumber}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
//                                    Text(
//                                        text = "${context.getString(R.string.area)}: ${unit.area}",
//                                        style = MaterialTheme.typography.bodyLarge
//                                    )

                                    if (isChecked) {
                                        OutlinedTextField(
                                            value = localDangText,
                                            onValueChange = { text ->
                                                localDangText = text

                                                val v = text.toDoubleOrNull()
                                                if (v != null) {
                                                    val clamped = v.coerceIn(0.0, maxAllowed)
                                                    val idx = selectedUnitsList.indexOfFirst {
                                                        it.unitId == unit.unitId
                                                    }
                                                    if (idx >= 0) {
                                                        selectedUnitsList[idx] =
                                                            selectedUnitsList[idx].copy(dang = clamped)
                                                    }
                                                }
                                            },
                                            label = {
                                                Text(
                                                    text = context.getString(R.string.dang),
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                            },
                                            textStyle = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackBarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (isEditing) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color(context.getColor(R.color.white)))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        isEditing = false
                        editableOwnerUnits = ownerUnits
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = context.getString(R.string.cancel),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Button(
                    onClick = {
                        val payloadUnits = editableOwnerUnits.filter { it.unitId > 0 }
                        ownerApi.updateOwnerUnitsAndRoleVolley(
                            context = context,
                            buildingId = buildingId,
                            userId = ownerId,
                            units = payloadUnits,
                            isManager = false,
                            onSuccess = {
                                ownerUnits = payloadUnits
                                isEditing = false
                                coroutineScope.launch {
                                    snackBarHostState.showSnackbar(
                                        context.getString(R.string.success_update)
                                    )
                                }
                            },
                            onError = { e ->
                                coroutineScope.launch {
                                    snackBarHostState.showSnackbar(
                                        e.message ?: context.getString(R.string.failed)
                                    )
                                }
                            }
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = context.getString(R.string.edit))
                }
            }
        }

        if (!isEditing) {
            FloatingActionButton(
                onClick = { isEditing = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit owner units")
            }
        }
    }
}


@Composable
fun OwnerInfoRow(icon: ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun OwnerTextField(label: Int, value: String, onValueChange: (String) -> Unit) {
    val context = LocalContext.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(context.getString(label), style = MaterialTheme.typography.bodyLarge) },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun OwnerFinancialsTab(
    ownerId: Long,
    sharedViewModel: SharedViewModel,
    snackBarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var allDebts by remember { mutableStateOf<List<Debts>>(emptyList()) }
    var allCosts by remember { mutableStateOf<List<Costs>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(ownerId) {
        isLoading = true
        errorMessage = null

        Cost().fetchCostsWithDebts(
            context = context,
            ownerId = ownerId,
            unitId = null,
            onSuccess = { costs, debts ->
                allCosts = costs
                allDebts = debts
                isLoading = false
            },
            onError = { e ->
                errorMessage = e.message ?: "خطا در دریافت اطلاعات"
                isLoading = false
                coroutineScope.launch {
                    snackBarHostState.showSnackbar(errorMessage ?: "")
                }
            }
        )
    }

    val unpaidDebts = remember(allDebts) {
        allDebts.filter { it.paymentFlag == false }
    }
    Log.d("unpaidDebts", unpaidDebts.toString())
    val payments = remember(allDebts) {
        allDebts.filter { it.paymentFlag == true }
    }
    Log.d("payments", payments.toString())
//    val chargeDebts = remember(allDebts) {
//        allDebts.filter { it.paymentFlag == false && it.description == "شارژ" }
//    }

    val transactions = remember(allDebts, payments) {
        (unpaidDebts.map {
            TransactionItem(it.debtId, it.amount, it.dueDate, it.description, FilterType.DEBT)
        } + payments.map {
            TransactionItem(it.debtId, it.amount, it.dueDate, it.description, FilterType.PAYMENT)
        }).sortedByDescending { it.date }
    }

    var filterType by rememberSaveable { mutableStateOf(FilterType.ALL) }
    val filteredTransactions = remember(transactions, filterType) {
        when (filterType) {
            FilterType.ALL -> transactions
            FilterType.DEBT -> transactions.filter { it.transactionType == FilterType.DEBT }
            FilterType.PAYMENT -> transactions.filter { it.transactionType == FilterType.PAYMENT }
        }
    }

    val totalDebtAmount = allDebts.sumOf { it.amount }
    val totalPaymentAmount = payments.sumOf { it.amount }
    var showTransferDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = modifier.fillMaxSize()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${context.getString(R.string.debt)}: ${
                                formatNumberWithCommas(
                                    totalDebtAmount
                                )
                            } ${context.getString(R.string.toman)}"
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${context.getString(R.string.payments)}: ${
                                formatNumberWithCommas(
                                    totalPaymentAmount
                                )
                            } ${context.getString(R.string.toman)}"
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterType.entries.forEach { type ->
                    Button(
                        onClick = { filterType = type },
                        colors = if (filterType == type)
                            ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
                        else
                            ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = type.getDisplayName(context),
                            color = if (filterType == type)
                                Color(context.getColor(R.color.white))
                            else
                                Color(context.getColor(R.color.grey)),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                filteredTransactions.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = context.getString(R.string.no_transactions_recorded),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredTransactions) { item ->
                            TransactionRow(
                                transaction = item,
                                onPayment = {
                                    coroutineScope.launch {
                                        val debt = allDebts.find { it.debtId == item.id }
                                        if (debt == null) {
                                            snackBarHostState.showSnackbar(
                                                context.getString(R.string.failed)
                                            )
                                            return@launch
                                        }

                                        val updatedDebt = debt.copy(paymentFlag = true)

                                        sharedViewModel.updateDebtOnServer(context, updatedDebt)

                                        allDebts = allDebts.map {
                                            if (it.debtId == debt.debtId) updatedDebt else it
                                        }

                                        val cost = allCosts.find { it.costId == debt.costId }
                                        val fundType = cost?.fundType ?: FundType.OPERATIONAL

                                        Fund().increaseBalanceFundOnServer(
                                            context = context,
                                            buildingId = debt.buildingId,
                                            amount = debt.amount,
                                            fundType = fundType,
                                            onSuccess = {
                                                coroutineScope.launch {
                                                    sharedViewModel.loadFundBalances(context, debt.buildingId)
                                                    snackBarHostState.showSnackbar(
                                                        context.getString(
                                                            if (fundType == FundType.OPERATIONAL)
                                                                R.string.success_pay_tooperational_fund
                                                            else
                                                                R.string.success_pay_tocapital_fund
                                                        )
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
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

//        Column(
//            modifier = Modifier
//                .align(Alignment.BottomEnd)
//                .background(Color(context.getColor(R.color.white)))
//                .padding(16.dp)
//                .fillMaxWidth(),
//            verticalArrangement = Arrangement.spacedBy(12.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Button(
//                onClick = {
//                    coroutineScope.launch {
//                        if (chargeDebts.isEmpty()) {
//                            snackBarHostState.showSnackbar(
//                                context.getString(R.string.no_transactions_recorded)
//                            )
//                        } else {
//                            showTransferDialog = true
//                        }
//                    }
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(56.dp),
//                shape = RoundedCornerShape(28.dp),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = MaterialTheme.colorScheme.primaryContainer,
//                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
//                )
//            ) {
//                Text(
//                    text = context.getString(R.string.transfer_debt_to_tenant),
//                    style = MaterialTheme.typography.bodyLarge
//                )
//            }
//        }
    }

//    if (showTransferDialog) {
//        Log.d("chargeDebts", chargeDebts.toString())
//        TransferDebtsDialog(
//            debts = chargeDebts,
//            onConfirm = {
//                showTransferDialog = false
//            },
//            onDismiss = {
//                showTransferDialog = false
//            }
//        )
//    }
}

@Composable
fun TransferDebtsDialog(
    debts: List<Debts>,
    onConfirm: (List<Debts>) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedDebts by remember { mutableStateOf(debts.toSet()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                context.getString(R.string.transfer_debt_to_tenant),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        text = {
            LazyColumn {
                items(debts) { debt ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedDebts = if (selectedDebts.contains(debt)) {
                                    selectedDebts - debt
                                } else {
                                    selectedDebts + debt
                                }
                            }
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = debt.description,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = formatNumberWithCommas(debt.amount),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedDebts.toList()) },
                enabled = selectedDebts.isNotEmpty()
            ) {
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

@Composable
fun TransactionRow(transaction: TransactionItem, onPayment: () -> Unit) {
    val context = LocalContext.current
    val color = when (transaction.transactionType) {
        FilterType.DEBT -> Color.Red.copy(alpha = 0.8f)
        FilterType.PAYMENT -> Color.Green.copy(alpha = 0.8f)
        else -> Color.Gray
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = transaction.description.ifBlank { "سایر" },
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(8.dp))
                Row {
                    Text(
                        text = "${context.getString(R.string.due)}:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = transaction.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${formatNumberWithCommas(transaction.amount)} ${context.getString(R.string.toman)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(context.getColor(R.color.Green))
                )
                Spacer(Modifier.height(8.dp))
                if (transaction.transactionType == FilterType.PAYMENT) {
                    Text(
                        text = context.getString(R.string.payment_done),
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    Button(onClick = onPayment) {
                        Text(
                            text = context.getString(R.string.payment),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

data class TransactionItem(
    val id: Long,
    val amount: Double,
    val date: String,
    val description: String,
    val transactionType: FilterType
)

@Composable
fun OwnerSectionSelector(
    tabs: List<OwnerTabItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.padding(vertical = 4.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(tabs) { index, tab ->
            val isSelected = index == selectedIndex
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .width(90.dp)
                    .clickable { onTabSelected(index) }
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.medium
                    ),
                elevation = CardDefaults.cardElevation(if (isSelected) 8.dp else 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = when (tab.type) {
                            OwnerTabType.OVERVIEW -> Icons.Default.Info
                            OwnerTabType.FINANCIALS -> Icons.Default.Money
                        },
                        contentDescription = tab.title,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
