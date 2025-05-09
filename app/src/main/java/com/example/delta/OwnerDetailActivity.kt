package com.example.delta

import android.os.Bundle
import android.util.Log
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
import com.example.delta.viewmodel.SharedViewModel
import ir.hamsaa.persiandatepicker.util.PersianCalendar

class OwnerDetailsActivity : ComponentActivity() {
    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ownerId = intent.getLongExtra("ownerId", -1L)
        setContent {
            AppTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    OwnerDetailsScreen(ownerId = ownerId, sharedViewModel = sharedViewModel, onBack = { finish() })
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
                title = { Text(context.getString(R.string.owner_details), style = MaterialTheme.typography.bodyLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = context.getString(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
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
                            label = { Text(context.getString(R.string.first_name),
                                style = MaterialTheme.typography.bodyLarge) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            label = { Text(context.getString(R.string.last_name),
                                style = MaterialTheme.typography.bodyLarge) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text(context.getString(R.string.last_name),
                                style = MaterialTheme.typography.bodyLarge) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = {  Text(context.getString(R.string.phone_number),
                                style = MaterialTheme.typography.bodyLarge)  },
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
                            ) {Text(context.getString(R.string.insert),
                                style = MaterialTheme.typography.bodyLarge)  }
                            Spacer(Modifier.width(8.dp))
                            OutlinedButton(onClick = { isEditing = false }) {  Text(context.getString(R.string.cancel),
                                style = MaterialTheme.typography.bodyLarge)  }
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



@Composable
fun OwnerDebtTab(ownerId: Long, sharedViewModel: SharedViewModel) {

    var selectedYear by rememberSaveable { mutableIntStateOf(PersianCalendar().persianYear) }
    var selectedMonth by rememberSaveable { mutableIntStateOf(PersianCalendar().persianMonth + 1) }
    var showDebtDialog by remember { mutableStateOf(false) }
    val debts by sharedViewModel.getDebtsForOwner(ownerId, selectedYear.toString(), selectedMonth.toString().padStart(2, '0'))
        .collectAsState(initial = emptyList())
    Log.d("debt", debts.toString())
    var context = LocalContext.current

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Debts", style = MaterialTheme.typography.bodyLarge)
        if (debts.isEmpty()) {
            Text(text = context.getString(R.string.debt), style = MaterialTheme.typography.bodyLarge)
        } else {
            debts.forEach { debt ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "${context.getString(R.string.title)}: ${debt.description}", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "${context.getString(R.string.amount)}: ${debt.amount}", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "${context.getString(R.string.due)}: ${debt.dueDate}", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
    FloatingActionButton(
        onClick = { showDebtDialog = true },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier
//            .align(Alignment.BottomEnd)
            .padding(32.dp)
    ) {
        Icon(Icons.Default.Add, contentDescription = "Add")
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
    val payments by sharedViewModel.getPaysForOwner(ownerId).collectAsState(initial = emptyList())
    var context = LocalContext.current
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = context.getString(R.string.payment), style = MaterialTheme.typography.bodyLarge)
        if (payments.isEmpty()) {
            Text(text = context.getString(R.string.no_debts_recorded), style = MaterialTheme.typography.bodyLarge)
        } else {
            payments.forEach { payment ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "${context.getString(R.string.amount)}: ${payment.amount}", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "${context.getString(R.string.due)}: ${payment.dueDate}", style = MaterialTheme.typography.bodyLarge)
                        // Add more fields as needed
                    }
                }
            }
        }
    }
}
