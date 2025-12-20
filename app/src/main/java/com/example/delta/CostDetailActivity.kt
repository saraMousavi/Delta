package com.example.delta

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Debts
import com.example.delta.enums.FundType
import com.example.delta.viewmodel.SharedViewModel
import com.example.delta.volley.Owner
import com.example.delta.volley.Tenant
import kotlinx.coroutines.flow.collectLatest

class CostDetailActivity : ComponentActivity() {

    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Get fund type ordinal and map to FundType
        val fundTypeOrdinal = intent.getIntExtra("FUND_TYPE", -1)
        val fundType = if (fundTypeOrdinal in FundType.entries.indices) {
            FundType.entries[fundTypeOrdinal]
        } else {
            FundType.OPERATIONAL // or handle error accordingly
        }

        // Get cost parcelable passed from intent
        val cost = intent.getParcelableExtra<Parcelable>("COST_DATA") as? Costs
        Log.d("cost", cost.toString())
        setContent {
            AppTheme (useDarkTheme = sharedViewModel.isDarkModeEnabled){
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MaterialTheme {
                        CostDetailScreen(
                            cost = cost,
                            sharedViewModel = sharedViewModel
                        )
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CostDetailScreen(
    cost: Costs?,
    sharedViewModel: SharedViewModel
) {
    val costName = cost!!.costName
    var showFundDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showInvoiceBtn by remember { mutableStateOf(true) }
    LaunchedEffect(cost.costId) {
        sharedViewModel.loadDebtsForCost(context, cost.costId)
    }

    val debts by sharedViewModel.debtsForCost.collectAsState()

    val snackBarHostState = remember { SnackbarHostState() }
    val activity = LocalActivity.current

    LaunchedEffect(sharedViewModel.invoiceResult) {
        sharedViewModel.invoiceResult.collectLatest { success ->
            if (success == true) {
                showInvoiceBtn = false
                snackBarHostState.showSnackbar(
                    context.getString(R.string.invoiced_succesfully)
                )
                showFundDialog = false
            } else {
                if (cost.fundType == FundType.OPERATIONAL) {
                    snackBarHostState.showSnackbar(
                        context.getString(R.string.insufficient_operational_fund_balance)
                    )
                } else {
                    snackBarHostState.showSnackbar(
                        context.getString(R.string.insufficient_fund_balance)
                    )
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.padding(16.dp),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "${context.getString(R.string.cost_name)} : $costName",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { activity!!.finish() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
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
            SnackbarHost(hostState = snackBarHostState)

            Spacer(Modifier.height(8.dp))

            Text(
                text = "${context.getString(R.string.due)}: ${cost.dueDate}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${context.getString(R.string.responsible)}: ${cost.responsible.getDisplayName(context)}",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            val listState = rememberLazyListState()
            val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

            if (debts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = context.getString(R.string.no_debts_recorded),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    flingBehavior = flingBehavior,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .weight(1f)
                ) {
                    items(debts) { debt ->
                        DebtCard(
                            debt = debt,
                            sharedViewModel = sharedViewModel
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { showFundDialog = true },
                modifier = Modifier.fillMaxWidth(),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = showInvoiceBtn
            ) {
                Text(
                    text = context.getString(R.string.invoice),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }

    if (showFundDialog) {
        FundUsageBottomSheetDialog(
            onConfirm = {
                sharedViewModel.invoiceCostIfEnoughFund(context = context, cost = cost, onSuccess = {}, onError = {})
                showFundDialog = false
            },
            onCancel = { showFundDialog = false }
        )
    }
}


// Enhanced DebtCard composable with styling & payment status icon + text
@Composable
fun DebtCard(
    debt: Debts,
    sharedViewModel: SharedViewModel
) {
    val context = LocalContext.current

    var ownerName by remember(debt.ownerId) { mutableStateOf<String?>(null) }
    var tenantName by remember(debt.unitId, debt.buildingId) { mutableStateOf<String?>(null) }

    var ownerLoaded by remember(debt.ownerId) { mutableStateOf(false) }
    var tenantLoaded by remember(debt.unitId, debt.buildingId) { mutableStateOf(false) }

    // Owner by ownerId
    LaunchedEffect(debt.ownerId) {
        val ownerId = debt.ownerId
        if (ownerId != null && ownerId > 0 && !ownerLoaded) {
            ownerLoaded = true
            Owner().getOwnerWithUnits(
                context = context,
                ownerId = ownerId,
                onSuccess = { dto ->
                    val name = listOfNotNull(dto.user?.firstName, dto.user?.lastName)
                        .joinToString(" ")
                        .trim()
                    ownerName = name.ifBlank { null }
                    Log.d("DebtCard", "Owner name from server = $ownerName")
                },
                onError = { e ->
                    Log.e("DebtCard", "Failed to fetch owner $ownerId", e)
                    ownerName = null
                }
            )
        }
    }

    // Tenant as unit resident (by buildingId + unitId)
    LaunchedEffect(debt.unitId, debt.buildingId) {
        val unitId = debt.unitId
        val buildingId = debt.buildingId
        if (unitId!! > 0 && buildingId > 0 && !tenantLoaded) {
            tenantLoaded = true
            Tenant().getTenantForUnitInBuilding(
                context = context,
                buildingId = buildingId,
                unitId = unitId,
                onSuccess = { dto ->
                    val user = dto?.user
                    val name = if (user != null) {
                        listOfNotNull(user.firstName, user.lastName)
                            .joinToString(" ")
                            .trim()
                    } else {
                        ""
                    }
                    tenantName = name.ifBlank { null }
                    Log.d("DebtCard", "Tenant name from server = $tenantName")
                },
                onError = { e ->
                    Log.e("DebtCard", "Failed to fetch tenant for unit $unitId", e)
                    tenantName = null
                }
            )
        }
    }

    // Prefer tenant (resident) over owner
    val personName = when {
        !tenantName.isNullOrBlank() -> tenantName!!
        !ownerName.isNullOrBlank() -> ownerName!!
        else -> context.getString(R.string.unknown_person)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, MaterialTheme.shapes.medium)
            .animateContentSize(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = debt.description,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${formatNumberWithCommas(debt.amount)} ${context.getString(R.string.toman)}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = personName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (debt.paymentFlag) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = context.getString(R.string.payments),
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = context.getString(R.string.payments),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color(context.getColor(R.color.Green))
                        )
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = context.getString(R.string.not_paid),
                        tint = Color(context.getColor(R.color.Red_light)),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = context.getString(R.string.not_paid),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color(context.getColor(R.color.Red_light))
                        )
                    )
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FundUsageBottomSheetDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onCancel,
        dragHandle = { /* Optional drag handle UI */ }
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = LocalContext.current.getString(R.string.fund_usage_dialog_message),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCancel) {
                    Text(LocalContext.current.getString(R.string.cancel),
                        style = MaterialTheme.typography.bodyLarge)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = onConfirm) {
                    Text(LocalContext.current.getString(R.string.confirm),
                        style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
