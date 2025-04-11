package com.example.delta

import android.content.Intent
import com.example.delta.data.entity.BuildingTypes
import com.example.delta.data.entity.BuildingUsages
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.delta.data.entity.BuildingOwnerCrossRef
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.Owners
import com.example.delta.data.entity.Tenants
import com.example.delta.viewmodel.BuildingTypeViewModel
import com.example.delta.viewmodel.BuildingUsageViewModel
import com.example.delta.viewmodel.BuildingsViewModel
import com.example.delta.viewmodel.OwnersViewModel
import com.example.delta.viewmodel.SharedViewModel
import com.example.delta.viewmodel.TenantViewModel


class BuildingFormActivity : ComponentActivity() {
    private val viewModel: BuildingsViewModel by viewModels()
    private val buildingTypeViewModel: BuildingTypeViewModel by viewModels()
    private val buildingUsageViewModel: BuildingUsageViewModel by viewModels()
    private val ownerViewModel: OwnersViewModel by viewModels()
    private val tenantViewModel: TenantViewModel by viewModels()
    val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    // Fetch Building Types and Usages from your ViewModels
                    val buildingTypes by buildingTypeViewModel.getAllBuildingType()
                        .collectAsState(initial = emptyList())
                    val buildingUsages by buildingUsageViewModel.getAllBuildingUsage()
                        .collectAsState(initial = emptyList())

                    BuildingFormScreen(
                        viewModel = viewModel,
                        buildingTypes = buildingTypes,
                        buildingTypeViewModel = buildingTypeViewModel,
                        buildingUsageViewModel = buildingUsageViewModel,
                        buildingUsages = buildingUsages,
                        ownerViewModel = ownerViewModel,
                        tenantViewModel = tenantViewModel,
                        sharedViewModel = sharedViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun BuildingFormScreen(
    viewModel: BuildingsViewModel,
    buildingTypeViewModel: BuildingTypeViewModel,
    buildingUsageViewModel: BuildingUsageViewModel,
    buildingTypes: List<BuildingTypes>,
    buildingUsages: List<BuildingUsages>,
    ownerViewModel: OwnersViewModel,
    tenantViewModel: TenantViewModel,
    sharedViewModel: SharedViewModel,
) {
    var currentPage by remember { mutableStateOf(0) }
    val context = LocalContext.current
    Column {
        if (currentPage == 0) {
            BuildingInfoPage(
                sharedViewModel =  sharedViewModel,
                viewModel = viewModel,
                buildingTypeViewModel = buildingTypeViewModel,
                buildingUsageViewModel = buildingUsageViewModel,
                buildingTypes = buildingTypes,
                buildingUsages = buildingUsages,
                onNext = { currentPage++ }
            )
        } else if (currentPage == 1) {
            OwnersPage(
                sharedViewModel = sharedViewModel,
                viewModel = ownerViewModel,
                onNext = { currentPage++ },
                onBack = { currentPage = 0 }
            )
        } else if (currentPage == 2) {
            TenantsPage(
                sharedViewModel = sharedViewModel,
                viewModel = tenantViewModel,
                onBack = { currentPage = 1 },
                onSave = {
                    sharedViewModel.saveBuildingWithOwners(
                        onSuccess = {
                            sharedViewModel.resetState()
                            // Get the context
                            val context = context

                            // Create an Intent to start HomePageActivity
                            val intent = Intent(context, HomePageActivity::class.java)

                            // Start the activity
                            context.startActivity(intent)
                        },
                        onError = { errorMessage ->
                            // show a Toast, etc.
                            Log.e("SaveError", "Error saving building: $errorMessage")
                        }
                    )
                }
            )

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildingInfoPage(
    sharedViewModel: SharedViewModel,
    viewModel: BuildingsViewModel,
    buildingTypeViewModel: BuildingTypeViewModel,
    buildingUsageViewModel: BuildingUsageViewModel,
    buildingTypes: List<BuildingTypes>,
    buildingUsages: List<BuildingUsages>,
    onNext: () -> Unit
) {

    var selectedBuildingTypes by remember { mutableStateOf<BuildingTypes?>(null) }
    var selectedBuildingUsages by remember { mutableStateOf<BuildingUsages?>(null) }


    var showBuildingTypeDialog by remember { mutableStateOf(false) }
    var showBuildingUsageDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = context.getString(R.string.buildings_form),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            navigationIcon = {
                IconButton(onClick = {
                    (context as? BuildingFormActivity)?.finish()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = context.getString(R.string.back))
                }
            }
        )

        // Scrollable Content
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = sharedViewModel.name,
                    onValueChange = { newValue -> // Update the ViewModel on change
                        sharedViewModel.name = newValue
                    },
                    label = {
                        Text(
                            text = context.getString(R.string.building_name),
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                ExposedDropdownMenuBoxExample(
                    items = buildingTypes + BuildingTypes(0, context.getString(R.string.addNew)), // Add "Add New" option
                    selectedItem = selectedBuildingTypes,
                    onItemSelected = {
                        if (it.buildingTypeName == context.getString(R.string.addNew)) {
                            // Open dialog to add new building type
                            showBuildingTypeDialog = true
                        } else {
                            selectedBuildingTypes = it
                        }
                    },
                    label = context.getString(R.string.building_type),
                    itemLabel = { it.buildingTypeName }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                ExposedDropdownMenuBoxExample(
                    items = buildingUsages + BuildingUsages(0, context.getString(R.string.addNew)), // Add "Add New" option
                    selectedItem = selectedBuildingUsages,
                    onItemSelected = {
                        if (it.buildingUsageName == context.getString(R.string.addNew)) {
                            // Open dialog to add new building usage
                            showBuildingUsageDialog = true
                        } else {
                            selectedBuildingUsages = it
                        }
                    },
                    label = context.getString(R.string.building_usage),
                    itemLabel = { it.buildingUsageName }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                ProvinceStateSelector(viewModel = viewModel)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                OutlinedTextField(
                    value = sharedViewModel.street,
                    onValueChange = { newValue -> // Update the ViewModel on change
                        sharedViewModel.street = newValue
                    },
                    label = {
                        Text(
                            text = context.getString(R.string.street),
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                OutlinedTextField(
                    value = sharedViewModel.postCode,
                    onValueChange = { newValue -> // Update the ViewModel on change
                        sharedViewModel.postCode = newValue
                    },
                    label = {
                        Text(
                            text = context.getString(R.string.post_code),
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Fixed Bottom Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End // Align button to the right
        ) {

            Button(onClick = onNext,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
                )) {
                Text(context.getString(R.string.next),
                    modifier = Modifier.padding(2.dp),
                    style = MaterialTheme.typography.titleLarge)
            }
        }

        // Dialogs for Adding Items
        if (showBuildingTypeDialog) {
            AddItemDialog(
                onDismiss = { showBuildingTypeDialog = false },
                onInsert = { name ->
                    buildingTypeViewModel.insertBuildingType(BuildingTypes(buildingTypeName = name))
                }
            )
        }

        if (showBuildingUsageDialog) {
            AddItemDialog(
                onDismiss = { showBuildingUsageDialog = false },
                onInsert = { name ->
                    buildingUsageViewModel.insertBuildingUsage(BuildingUsages(buildingUsageName = name))
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnersPage(
    sharedViewModel: SharedViewModel,
    viewModel: OwnersViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    var showOwnerDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        // Add Top App Bar with Back Button
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = context.getString(R.string.owners_list),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(sharedViewModel.ownersList) { owner ->
                OwnerItem(owner = owner)
            }

            // Add "Add New Owner" as the last item
            item {
                AddNewOwnerItem(onClick = { showOwnerDialog = true })
            }
        }

        // Navigation Buttons (Back/Next)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {


            Button(onClick = onNext,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
                )) {
                Text(context.getString(R.string.next),
                    modifier = Modifier.padding(2.dp),
                    style = MaterialTheme.typography.titleLarge)
            }
            Button(onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
                )) {
                Text(context.getString(R.string.back),
                    modifier = Modifier.padding(2.dp),
                    style = MaterialTheme.typography.titleLarge)
            }
        }
        Spacer(Modifier.height(32.dp))

        // Owner Dialog
        if (showOwnerDialog) {
            OwnerDialog(
                onDismiss = { showOwnerDialog = false },
                onAddOwner = { newOwner ->
                    sharedViewModel.ownersList = sharedViewModel.ownersList + newOwner
                    showOwnerDialog = false
                }
            )
        }
    }
}



@Composable
fun AddNewOwnerItem(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add New Owner",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = LocalContext.current.getString(R.string.add_new_owner),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}


@Composable
fun OwnerItem(
    owner: Owners,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar or Icon
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Owner Avatar",
                modifier = Modifier
                    .size(48.dp)
                    .padding(end = 16.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            // Owner Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Name: ${owner.firstName}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Address: ${owner.address}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Email: ${owner.email}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Phone: ${owner.phoneNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Mobile: ${owner.mobileNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Action Icon (Optional)
            IconButton(onClick = { /* Handle click */ }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Actions",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Composable
fun OwnerDialog(
    onDismiss: () -> Unit,
    onAddOwner: (Owners) -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Owner") },
        text = {
            Column {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = { mobileNumber = it },
                    label = { Text("Mobile Number") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val newOwner =
                    Owners(firstName = firstName, lastName = lastName, address = address, email = email, phoneNumber = phoneNumber, mobileNumber = mobileNumber, birthday = "")
                onAddOwner(newOwner)
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantsPage(
    sharedViewModel: SharedViewModel,
    viewModel: TenantViewModel,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    var showTenantDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        // Add Top App Bar with Back Button
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = context.getString(R.string.tenant_list),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(sharedViewModel.tenantsList) { tenant ->
                TenantItem(tenants = tenant)
            }

            // Add "Add New Tenant" as the last item
            item {
                AddNewTenantItem(onClick = { showTenantDialog = true })
            }
        }

        // Save Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Button(onClick = onSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
                )) {
                Text(context.getString(R.string.insert),
                    modifier = Modifier.padding(2.dp),
                    style = MaterialTheme.typography.titleLarge)
            }
            Button(onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
                )) {
                Text(context.getString(R.string.back),
                    modifier = Modifier.padding(2.dp),
                    style = MaterialTheme.typography.titleLarge)
            }
        }

        Spacer(Modifier.height(32.dp))

        // Tenant Dialog
        if (showTenantDialog) {
            TenantDialog(
                onDismiss = { showTenantDialog = false },
                onAddTenant = { newTenant ->
                    sharedViewModel.tenantsList = sharedViewModel.tenantsList + newTenant
                    showTenantDialog = false
                }
            )
        }
    }
}

@Composable
fun AddNewTenantItem(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add New Tenant",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = LocalContext.current.getString(R.string.add_new_tenant),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}


@Composable
fun TenantItem(
    tenants: Tenants,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar or Icon
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Owner Avatar",
                modifier = Modifier
                    .size(48.dp)
                    .padding(end = 16.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            // Owner Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Name: ${tenants.firstName}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Address: ${tenants.address}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Email: ${tenants.email}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Phone: ${tenants.phoneNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Mobile: ${tenants.mobileNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Action Icon (Optional)
            IconButton(onClick = { /* Handle click */ }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Actions",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Composable
fun TenantDialog(
    onDismiss: () -> Unit,
    onAddTenant: (Tenants) -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    val statuses = listOf("Active", "Inactive")
    var selectedStatus by remember { mutableStateOf("Active") } // Default status
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Tenant") },
        text = {
            Column {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") }
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") }
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") }
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") }
                )
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") }
                )
                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = { mobileNumber = it },
                    label = { Text("Mobile Number") }
                )
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Start Date") }
                )
                OutlinedTextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = { Text("End Date") }
                )
                // Status Dropdown
                StatusDropdown(
                    selectedStatus = selectedStatus,
                    onStatusSelected = { selectedStatus = it }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val newTenant = Tenants(
                    firstName = firstName,
                    lastName = lastName,
                    address = address,
                    email = email,
                    phoneNumber = phoneNumber,
                    mobileNumber = mobileNumber,
                    startDate = startDate,
                    endDate = endDate,
                    status = selectedStatus,
                    birthday = ""
                )
                onAddTenant(newTenant)
                onDismiss()
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun StatusDropdown(selectedStatus: String, onStatusSelected: (String) -> Unit) {
        val statuses = remember { listOf("Active", "Inactive") }
        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                readOnly = true,
                value = selectedStatus,
                onValueChange = { },
                label = { Text("Status") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                statuses.forEach { status ->
                    DropdownMenuItem(
                        text = { Text(status) },
                        onClick = {
                            onStatusSelected(status)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusDropdown(selectedStatus: String, onStatusSelected: (String) -> Unit) {
    val statuses = remember { listOf("Active", "Inactive") }
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selectedStatus,
            onValueChange = { },
            label = { Text("Status") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            statuses.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status) },
                    onClick = {
                        onStatusSelected(status)
                        expanded = false
                    }
                )
            }
        }
    }
}
