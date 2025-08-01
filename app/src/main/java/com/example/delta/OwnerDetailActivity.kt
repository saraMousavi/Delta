package com.example.delta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MobileFriendly
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.delta.data.entity.OwnerTabItem
import com.example.delta.data.entity.OwnerTabType
import com.example.delta.enums.FilterType
import com.example.delta.enums.FundType
import com.example.delta.viewmodel.SharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                title = { Text(context.getString(R.string.owner_details), style = MaterialTheme.typography.bodyLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = context.getString(R.string.back))
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
                OwnerTabType.OVERVIEW -> OwnerOverviewTab(ownerId, sharedViewModel)
                OwnerTabType.FINANCIALS -> OwnerFinancialsTab(
                    ownerId,
                    sharedViewModel,
                    snackbarHostState = snackbarHostState,
                    coroutineScope = coroutineScope
                )
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
    val context = LocalContext.current
    val owner by sharedViewModel.getOwner(ownerId).collectAsState(initial = null)
    var isEditing by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    LaunchedEffect(owner) {
        owner?.let {
            firstName = it.firstName
            lastName = it.lastName
            email = it.email
            phone = it.phoneNumber
            mobile = it.mobileNumber
            address = it.address
        }
    }

    Box(modifier = modifier.fillMaxSize().padding(8.dp)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                if (owner == null) {
                    item {
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
                    }
                } else {
                    if (isEditing) {
                        item {
                            OwnerTextField(R.string.first_name, firstName) { firstName = it }
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                        item {
                            OwnerTextField(R.string.last_name, lastName) { lastName = it }
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                        item {
                            OwnerTextField(R.string.email, email) { email = it }
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                        item {
                            OwnerTextField(R.string.phone_number, phone) { phone = it }
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                        item {
                            OwnerTextField(R.string.mobile_number, mobile) { mobile = it }
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                        item {
                            OwnerTextField(R.string.address, address) { address = it }
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = {
                                    sharedViewModel.updateOwner(
                                        owner!!.copy(
                                            firstName = firstName,
                                            lastName = lastName,
                                            email = email,
                                            phoneNumber = phone,
                                            mobileNumber = mobile,
                                            address = address
                                        ),
                                        onError = {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(context.getString(R.string.operation_problem))
                                            }
                                        }
                                    )
                                    isEditing = false
                                }) {
                                    Text(context.getString(R.string.insert), style = MaterialTheme.typography.bodyLarge)
                                }
                                OutlinedButton(onClick = { isEditing = false }) {
                                    Text(context.getString(R.string.cancel), style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    } else {
                        item {
                            OwnerInfoRow(Icons.Default.Person, "${owner!!.firstName} ${owner!!.lastName}")
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                        item {
                            OwnerInfoRow(Icons.Default.Email, owner!!.email.ifBlank { context.getString(R.string.no_email) })
                        }
                        item { Spacer(Modifier.height(12.dp)) }
                        item {
                            OwnerInfoRow(Icons.Default.Phone, owner!!.phoneNumber.ifBlank { context.getString(R.string.no_phone) })
                        }
                        item { Spacer(Modifier.height(12.dp)) }
                        item {
                            OwnerInfoRow(Icons.Default.MobileFriendly, owner!!.mobileNumber.ifBlank { context.getString(R.string.no) })
                        }
                        item { Spacer(Modifier.height(12.dp)) }
                        item {
                            OwnerInfoRow(Icons.Default.HomeWork, owner!!.address.ifBlank { context.getString(R.string.no) })
                        }
                    }
                }
            }
        }

        if (!isEditing && owner != null) {
            FloatingActionButton(
                onClick = { isEditing = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit owner details")
            }
        }
    }
}

@Composable
fun OwnerInfoRow(icon: ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(12.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
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
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val debts by sharedViewModel.getDebtsForOwner(ownerId, null, "00").collectAsState(initial = emptyList())
    val payments by sharedViewModel.getPaysForOwner(ownerId).collectAsState(initial = emptyList())

    val transactions = remember(debts, payments) {
        (debts.map {
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

    val totalDebtAmount = debts.sumOf { it.amount }
    val totalPaymentAmount = payments.sumOf { it.amount }
//    val balance = totalDebtAmount - totalPaymentAmount

    Column(modifier = modifier.fillMaxSize()) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp),
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
                    modifier = Modifier
                        .padding(horizontal = 20.dp),
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
//                Text(text = "${context.getString(R.string.balance)}: ${formatNumberWithCommas(balance)} ${context.getString(R.string.toman)}")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
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
                        color = if (filterType == type) Color(context.getColor(R.color.white)) else Color(context.getColor(R.color.grey)),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        if (filteredTransactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = context.getString(R.string.no_transactions_recorded),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTransactions) { item ->
                    TransactionRow(item, onPayment = {
                        coroutineScope.launch {
                            val debt = sharedViewModel.getDebtById(item.id)
                            debt?.let {
                                val updatedDebt = it.copy(paymentFlag = true)
                                sharedViewModel.updateDebt(updatedDebt)
                                sharedViewModel.updateDebtPaymentFlag(it, true)

                                val cost = sharedViewModel.getCostById(it.costId)
                                val fundType = cost?.fundType ?: FundType.OPERATIONAL

                                val success = sharedViewModel.increaseBalanceFund(
                                    buildingId = it.buildingId,
                                    amount = it.amount,
                                    fundType = fundType
                                )
                                withContext(Dispatchers.Main) {
                                    snackbarHostState.showSnackbar(
                                        if (success) {
                                            context.getString(
                                                if (fundType == FundType.OPERATIONAL)
                                                    R.string.success_pay_tooperational_fund
                                                else
                                                    R.string.success_pay_tocapital_fund
                                            )
                                        } else {
                                            context.getString(R.string.failed)
                                        }
                                    )
                                }
                            }
                        }
                    })
                }
            }
        }
    }
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
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = transaction.description.ifBlank { "No Description" },
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

// Data class for unified transactions
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .width(90.dp)
                    .clickable { onTabSelected(index) }
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.medium
                    ),
                elevation = CardDefaults.cardElevation(if (isSelected) 8.dp else 2.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = when (tab.type) {
                            OwnerTabType.OVERVIEW -> Icons.Default.Info
                            OwnerTabType.FINANCIALS -> Icons.Default.Money
                        },
                        contentDescription = tab.title,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

