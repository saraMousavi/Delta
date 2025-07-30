package com.example.delta

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.MobileFriendly
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.delta.sharedui.DebtItem
import com.example.delta.viewmodel.SharedViewModel
import ir.hamsaa.persiandatepicker.util.PersianCalendar
import kotlinx.coroutines.launch


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
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = Color(context.getColor(R.color.primary_color)))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                item {
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
                    }
                }
                if (owner != null) {
                    if (isEditing) {
                        // Editable fields as separate items:
                        item {
                            OutlinedTextField(
                                value = firstName,
                                onValueChange = { firstName = it },
                                label = { Text(context.getString(R.string.first_name), style = MaterialTheme.typography.bodyLarge) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                        item {
                            OutlinedTextField(
                                value = lastName,
                                onValueChange = { lastName = it },
                                label = { Text(context.getString(R.string.last_name), style = MaterialTheme.typography.bodyLarge) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                        item {
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text(context.getString(R.string.email), style = MaterialTheme.typography.bodyLarge) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                        item {
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text(context.getString(R.string.phone_number), style = MaterialTheme.typography.bodyLarge) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                        item {
                            OutlinedTextField(
                                value = mobile,
                                onValueChange = { mobile = it },
                                label = { Text(context.getString(R.string.mobile_number), style = MaterialTheme.typography.bodyLarge) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                        item {
                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text(context.getString(R.string.address), style = MaterialTheme.typography.bodyLarge) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                        item {
                            Row {
                                Button(
                                    onClick = {
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
                                                Toast.makeText(context, context.getString(R.string.operation_problem), Toast.LENGTH_LONG).show()
                                            }
                                        )
                                        isEditing = false
                                    }
                                ) {
                                    Text(context.getString(R.string.insert), style = MaterialTheme.typography.bodyLarge)
                                }
                                Spacer(Modifier.width(8.dp))
                                OutlinedButton(onClick = { isEditing = false }) {
                                    Text(context.getString(R.string.cancel), style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    } else {
                        // Display read-only fields as separate items:
                        item {
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
                        }
                        item { Spacer(Modifier.height(16.dp)) }

                        item {
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
                        }
                        item { Spacer(Modifier.height(12.dp)) }

                        item {
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
                        item { Spacer(Modifier.height(12.dp)) }

                        item {
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
                        }
                        item { Spacer(Modifier.height(12.dp)) }

                        item {
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
        }

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
    val context = LocalContext.current
    var selectedYear by rememberSaveable { mutableStateOf<Int?>(PersianCalendar().persianYear) }
    var selectedMonth by rememberSaveable { mutableIntStateOf(PersianCalendar().persianMonth) }
    var showSumDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val yearFilter = if (selectedYear == -1) null else selectedYear.toString()
    val monthFilter = if (selectedMonth == -1) null else selectedMonth.toString().padStart(2, '0')

    val debts by sharedViewModel.getDebtsForOwner(
        ownerId,
        yearFilter,
        monthFilter
    ).collectAsState(initial = emptyList())

    LaunchedEffect(debts) {
        sharedViewModel.addUnpaidDebtListList(debts)
    }

    Scaffold(
        bottomBar = {
            Button(
                onClick = { showSumDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = context.getString(R.string.full_payment),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                // Limit YearMonthSelector width so it doesn't take full width
                Box(modifier = Modifier.weight(1f)) {
                    YearMonthSelector(
                        selectedYear = selectedYear,
                        onYearChange = { selectedYear = it },
                        selectedMonth = selectedMonth,
                        onMonthChange = { selectedMonth = it }
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        selectedYear = null
                        selectedMonth = 0 // or your no-filter value
                    },
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear year and month",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            if (sharedViewModel.unpaidDebtList.value.isEmpty()) {
                Text(
                    text = context.getString(R.string.no_debts_recorded),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                sharedViewModel.unpaidDebtList.value.forEach { debt ->
                    DebtItem(debt = debt, onPayment = {
                        coroutineScope.launch {
                            sharedViewModel.getCost(debt.costId).collect { cost ->
                                val updatedDebt = debt.copy(paymentFlag = true)
                                sharedViewModel.updateDebt(updatedDebt)
                                sharedViewModel.updateDebtPaymentFlag(debt, true)
                                val amountDouble = debt.amount
                                val success = sharedViewModel.increaseOperationalFund(
                                    buildingId = debt.buildingId,
                                    amountDouble,
                                    fundType = cost.fundType
                                )
                                if (success) {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.success_pay_tocapital_fund),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context, context.getString(R.string.failed),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    })
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

        }
    }

    if (showSumDialog) {
        AlertDialog(
            onDismissRequest = { showSumDialog = false },
            text = {
                val totalAmount = formatNumberWithCommas(debts.sumOf { it.amount })
                Text(
                    text = "${context.getString(R.string.total_amount)}: $totalAmount",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    debts.forEach { debt ->
                        val updatedDebt = debt.copy(paymentFlag = true)
                        sharedViewModel.updateDebt(updatedDebt)
                        sharedViewModel.updateDebtPaymentFlag(debt, true)
                    }
                    Toast.makeText(
                        context,
                        context.getString(R.string.all_debts_marked_paid),
                        Toast.LENGTH_SHORT
                    ).show()
                    showSumDialog = false
                }) {
                    Text(text = context.getString(R.string.insert), style = MaterialTheme.typography.bodyLarge)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSumDialog = false }) {
                    Text(text = context.getString(R.string.cancel), style = MaterialTheme.typography.bodyLarge)
                }
            }
        )
    }

}


@Composable
fun OwnerPaymentTab(ownerId: Long, sharedViewModel: SharedViewModel) {
    val payments by sharedViewModel.getPaysForOwner(ownerId = ownerId)
        .collectAsState(initial = emptyList())
    val context = LocalContext.current

    LaunchedEffect(payments) {
        sharedViewModel.addUnpaidDebtListList(payments)
    }

    if (payments.isEmpty()) {
        Text(
            text = context.getString(R.string.no_payments_recorded),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp)
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(payments) { pay ->
                DebtItem(debt = pay, onPayment = {
                    sharedViewModel.updateDebt(pay)
                })
            }
        }
    }
}

