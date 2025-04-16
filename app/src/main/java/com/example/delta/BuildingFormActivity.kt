package com.example.delta

import android.annotation.SuppressLint
import android.app.Application
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Owners
import com.example.delta.data.entity.Tenants
import com.example.delta.data.entity.Units
import com.example.delta.factory.SharedViewModelFactory
import com.example.delta.viewmodel.BuildingTypeViewModel
import com.example.delta.viewmodel.BuildingUsageViewModel
import com.example.delta.viewmodel.BuildingsViewModel
import com.example.delta.viewmodel.SharedViewModel
import com.example.delta.viewmodel.UnitsViewModel
import kotlinx.coroutines.launch


class BuildingFormActivity : ComponentActivity() {
    private val viewModel: BuildingsViewModel by viewModels()
    private val buildingTypeViewModel: BuildingTypeViewModel by viewModels()
    private val buildingUsageViewModel: BuildingUsageViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by viewModels {
        SharedViewModelFactory(application = this.application)
    }
    private val unitsViewModel: UnitsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel.resetState()
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
                        sharedViewModel = sharedViewModel,
                        unitsViewModel = unitsViewModel
                    )
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        sharedViewModel.resetState()
    }
}

@Composable
fun BuildingFormScreen(
    unitsViewModel: UnitsViewModel,
    viewModel: BuildingsViewModel,
    buildingTypeViewModel: BuildingTypeViewModel,
    buildingUsageViewModel: BuildingUsageViewModel,
    buildingTypes: List<BuildingTypes>,
    buildingUsages: List<BuildingUsages>,
    sharedViewModel: SharedViewModel,
) {
    var currentPage by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    Column {
        if (currentPage == 0) {
            BuildingInfoPage(
                sharedViewModel =  sharedViewModel,
                viewModel = viewModel,
                buildingTypeViewModel = buildingTypeViewModel,
                buildingUsageViewModel = buildingUsageViewModel,
                unitsViewModel = unitsViewModel,
                buildingTypes = buildingTypes,
                buildingUsages = buildingUsages,
                onNext = { currentPage++ }
            )
        } else if (currentPage == 1) {
            UnitPage(unitsViewModel = unitsViewModel,
                sharedViewModel = sharedViewModel,
                onNext = { currentPage++ },
                onBack = { currentPage = 0 })

        } else if (currentPage == 2) {
            OwnersPage(
                sharedViewModel = sharedViewModel,
                onNext = { currentPage++ },
                onBack = { currentPage = 1 }
            )

        } else if (currentPage == 4){
            TenantsPage(
                sharedViewModel = sharedViewModel,
                onBack = { currentPage = 3 },
                onSave = {
                    sharedViewModel.saveBuildingWithUnitsAndOwnersAndTenants(
                        onSuccess = {
                            sharedViewModel.resetState()
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

        } else if (currentPage == 3){
            CostPage(
                sharedViewModel = sharedViewModel,
                onNext = { currentPage++ },
                onBack = { currentPage = 2 }
            )
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildingInfoPage(
    sharedViewModel: SharedViewModel,
    viewModel: BuildingsViewModel,
    buildingTypeViewModel: BuildingTypeViewModel,
    buildingUsageViewModel: BuildingUsageViewModel,
    unitsViewModel : UnitsViewModel,
    buildingTypes: List<BuildingTypes>,
    buildingUsages: List<BuildingUsages>,
    onNext: () -> Unit
) {

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
                    selectedItem = sharedViewModel.selectedBuildingTypes,
                    onItemSelected = {
                        if (it.buildingTypeName == context.getString(R.string.addNew)) {
                            // Open dialog to add new building type
                            showBuildingTypeDialog = true
                        } else {
                            sharedViewModel.selectedBuildingTypes = it
                        }
                    },
                    label = context.getString(R.string.building_type),
                    modifier = Modifier
                        .fillMaxWidth(1f),
                    itemLabel = { it.buildingTypeName }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                ExposedDropdownMenuBoxExample(
                    items = buildingUsages + BuildingUsages(0, context.getString(R.string.addNew)), // Add "Add New" option
                    selectedItem = sharedViewModel.selectedBuildingUsages,
                    onItemSelected = {
                        if (it.buildingUsageName == context.getString(R.string.addNew)) {
                            // Open dialog to add new building usage
                            showBuildingUsageDialog = true
                        } else {
                            sharedViewModel.selectedBuildingUsages = it
                        }
                    },
                    label = context.getString(R.string.building_usage),
                    modifier = Modifier.weight(1f),
                    itemLabel = { it.buildingUsageName }
                )
            }



            item {
                Spacer(modifier = Modifier.height(16.dp))
            }


            item {
                ProvinceStateSelector(sharedViewModel = sharedViewModel)
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

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = sharedViewModel.sameArea,
                        onCheckedChange = {
                            sharedViewModel.sameArea = it
                        }
                    )
                    Text(
                        text = context.getString(R.string.all_units_same_area),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            item {
                if (sharedViewModel.sameArea) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedTextField(
                            value = sharedViewModel.numberOfUnits,
                            onValueChange = {
                                sharedViewModel.numberOfUnits = it
                            },
                            label = { Text(context.getString(R.string.number_of_units)) },
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = sharedViewModel.unitArea,
                            onValueChange = {
                                sharedViewModel.unitArea = it
                            },
                            label = { Text(context.getString(R.string.area)) },
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }


            item {
                ChipGroupShared(
                    selectedItems = sharedViewModel.sharedUtilities,
                    onSelectionChange = { newSelection ->
                        sharedViewModel.sharedUtilities = newSelection
                    },
                    items = listOf(context.getString(R.string.gas),
                        context.getString(R.string.water),
                        context.getString(R.string.electricity)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    label = context.getString(R.string.shared_things)
                )
            }

            item{Spacer(modifier = Modifier.height(16.dp))}
        }

        // Fixed Bottom Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End // Align button to the right
        ) {
            val lifecycleOwner = LocalLifecycleOwner.current
            Button(onClick = {
                lifecycleOwner.lifecycleScope.launch {
                    if (sharedViewModel.sameArea && !sharedViewModel.unitsAdded) {
                        val numUnits = sharedViewModel.numberOfUnits.toInt()
                        val area = sharedViewModel.unitArea.toString()
                        for (i in 1..numUnits) {
                            val newUnit = Units(
                                unitNumber = i.toString(),
                                area = area,
                                numberOfRooms = "1"
                            )
                            try {
                                val unitId = unitsViewModel.insertUnit(newUnit)
                                sharedViewModel.unitsList.add(newUnit.copy(unitId = unitId)) // Update the unitId

                            } catch (e: Exception) {
                                Log.e("InsertUnitError", "Failed to insert unit: ${e.message}")
                            }
                            sharedViewModel.unitsAdded = true
                        }
                    }
                }


                onNext()
            },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
                )
            ) {
                Text(context.getString(R.string.next),
                    modifier = Modifier.padding(2.dp),
                    style = MaterialTheme.typography.bodyLarge)
            }
        }
        Spacer(Modifier.height(32.dp))

        // Dialogs for Adding Items
        if (showBuildingTypeDialog) {
            AddItemDialog(
                onDismiss = { showBuildingTypeDialog = false },
                onInsert = { newItem ->
                    val newType = BuildingTypes(buildingTypeName = newItem)
                    buildingTypeViewModel.insertBuildingType(newType) // Add the new item to the list
                    sharedViewModel.selectedBuildingTypes = newType // Update selected item in dropdown
                }
            )
        }

        if (showBuildingUsageDialog) {
            AddItemDialog(
                onDismiss = { showBuildingUsageDialog = false },
                onInsert = { name ->
                    val newUsage = BuildingUsages(buildingUsageName = name)
                    buildingUsageViewModel.insertBuildingUsage(newUsage)
                    sharedViewModel.selectedBuildingUsages = newUsage
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnersPage(
    sharedViewModel: SharedViewModel,
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
                    style = MaterialTheme.typography.bodyLarge
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
            Button(onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
                )
            ) {
                Text(context.getString(R.string.back),
                    modifier = Modifier.padding(2.dp),
                    style = MaterialTheme.typography.bodyLarge)
            }

            Button(onClick = onNext,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
                )
            ) {
                Text(context.getString(R.string.next),
                    modifier = Modifier.padding(2.dp),
                    style = MaterialTheme.typography.bodyLarge)
            }
        }
        Spacer(Modifier.height(32.dp))

        // Owner Dialog
        if (showOwnerDialog) {
            OwnerDialog(
                units = sharedViewModel.unitsList,
                onDismiss = { showOwnerDialog = false },
                onAddOwner = { newOwner, selectedUnits ->
                    sharedViewModel.saveOwnerWithUnits(newOwner, selectedUnits)
                    showOwnerDialog = false
                },
                sharedViewModel = sharedViewModel
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
    var context = LocalContext.current
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
                    text = "${context.getString(R.string.first_name)}: ${owner.firstName}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${context.getString(R.string.address)}: ${owner.address}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${context.getString(R.string.email)}: ${owner.email}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${context.getString(R.string.phone_number)}: ${owner.phoneNumber}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${context.getString(R.string.mobile_number)}: ${owner.mobileNumber}",
                    style = MaterialTheme.typography.bodyLarge,
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

@SuppressLint("MutableCollectionMutableState")
@Composable
fun OwnerDialog(
    units: List<Units>,
    onDismiss: () -> Unit,
    onAddOwner: (Owners, List<Units>) -> Unit,
    sharedViewModel: SharedViewModel
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var context = LocalContext.current
    var selectedUnits by remember { mutableStateOf(sharedViewModel.selectedUnits)}

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = context.getString(R.string.add_new_owner),
            style = MaterialTheme.typography.bodyLarge) },
        text = {
            Column {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { newValue -> // Update the ViewModel on change
                        firstName = newValue
                    },
                    label = { Text(text = context.getString(R.string.first_name) ,
                        style = MaterialTheme.typography.bodyLarge) },
                    textStyle = MaterialTheme.typography.bodyLarge, // <-- This line is key!
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text(text = context.getString(R.string.last_name) ,
                        style = MaterialTheme.typography.bodyLarge) },
                    textStyle = MaterialTheme.typography.bodyLarge, // <-- This line is key!
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(text = context.getString(R.string.address) ,
                        style = MaterialTheme.typography.bodyLarge) },
                    textStyle = MaterialTheme.typography.bodyLarge, // <-- This line is key!
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(text = context.getString(R.string.email) ,
                        style = MaterialTheme.typography.bodyLarge) },
                    textStyle = MaterialTheme.typography.bodyLarge, // <-- This line is key!
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text(text = context.getString(R.string.phone_number) ,
                        style = MaterialTheme.typography.bodyLarge) },
                    textStyle = MaterialTheme.typography.bodyLarge, // <-- This line is key!
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = { mobileNumber = it },
                    label = { Text(text = context.getString(R.string.mobile_number) ,
                        style = MaterialTheme.typography.bodyLarge) },
                    textStyle = MaterialTheme.typography.bodyLarge, // <-- This line is key!
                    modifier = Modifier.fillMaxWidth()
                )
                // Unit selection with ChipGroup
                ChipGroupUnits(
                    selectedUnits = selectedUnits,
                    onSelectionChange = { newSelection ->
                        selectedUnits.clear()
                        selectedUnits.addAll(newSelection)
                        sharedViewModel.selectedUnits.clear()
                        sharedViewModel.selectedUnits.addAll(newSelection)
                    },
                    units = units,
                    label = context.getString(R.string.units)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val newOwner =
                    Owners(firstName = firstName, lastName = lastName, address = address, email = email, phoneNumber = phoneNumber, mobileNumber = mobileNumber, birthday = "")
                Log.d("selectedUnits confirm", selectedUnits.toString())
                onAddOwner(newOwner, selectedUnits)
                sharedViewModel.addOwner(newOwner)
                Log.d("sharedViewModel.addOwner(newOwner)", sharedViewModel.ownersList.toString())
                sharedViewModel.addOwnerUnits(newOwner, selectedUnits)
            },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color)))) {
                Text(text = context.getString(R.string.insert) ,
                    style = MaterialTheme.typography.bodyLarge)
            }
        },
        dismissButton = {
            Button(onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color)))) {
                Text(text = context.getString(R.string.cancel) ,
                    style = MaterialTheme.typography.bodyLarge)
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantsPage(
    sharedViewModel: SharedViewModel,
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
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(sharedViewModel.tenantsList.value) { tenant ->
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

            Button(onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
                )
            ) {
                Text(context.getString(R.string.back),
                    modifier = Modifier.padding(2.dp),
                    style = MaterialTheme.typography.bodyLarge)
            }

            Button(onClick = onSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
                )
            ) {
                Text(context.getString(R.string.insert),
                    modifier = Modifier.padding(2.dp),
                    style = MaterialTheme.typography.bodyLarge)
            }
        }

        Spacer(Modifier.height(32.dp))

        // Tenant Dialog
        if (showTenantDialog) {
            TenantDialog(
                sharedViewModel = sharedViewModel,
                units = sharedViewModel.unitsList,
                onDismiss = { showTenantDialog = false },
                onAddTenant = { newTenant, selectedUnit ->
                    sharedViewModel.saveTenantWithUnit(newTenant, selectedUnit)
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
    var context = LocalContext.current
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
                    text = "${context.getString(R.string.first_name)}: ${tenants.firstName}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${context.getString(R.string.address)}: ${tenants.address}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${context.getString(R.string.email)}: ${tenants.email}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${context.getString(R.string.phone_number)}: ${tenants.phoneNumber}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${context.getString(R.string.mobile_number)}: ${tenants.mobileNumber}",
                    style = MaterialTheme.typography.bodyLarge,
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
    sharedViewModel: SharedViewModel,
    units: List<Units>,
    onDismiss: () -> Unit,
    onAddTenant: (Tenants, Units) -> Unit
) {
    var context = LocalContext.current
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var numberOfTenants by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf(context.getString(R.string.active)) } // Default status
    var selectedUnit by remember { mutableStateOf<Units?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = context.getString(R.string.add_new_tenant),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        text = {
            LazyColumn {
                item {
                    Text(
                        text = context.getString(R.string.personal_information),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                item {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = {
                            Text(
                                text = context.getString(R.string.first_name),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyLarge, // <-- This line is key!
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = {
                            Text(
                                text = context.getString(R.string.last_name),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyLarge, // <-- This line is key!
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = {
                            Text(
                                text = context.getString(R.string.address),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyLarge, // <-- This line is key!
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = {
                            Text(
                                text = context.getString(R.string.email),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyLarge, // <-- This line is key!
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = {
                            Text(
                                text = context.getString(R.string.phone_number),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyLarge, // <-- This line is key!
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = { mobileNumber = it },
                        label = {
                            Text(
                                text = context.getString(R.string.mobile_number),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyLarge, // <-- This line is key!
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Text(
                        text = context.getString(R.string.lease_information),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                item {
                    OutlinedTextField(
                        value = numberOfTenants,
                        onValueChange = { numberOfTenants = it },
                        label = { Text(context.getString(R.string.number_of_tenants)) },
                        textStyle = MaterialTheme.typography.bodyLarge, // <-- This line is key!
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = {
                            Text(
                                text = context.getString(R.string.start_date),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyLarge, // <-- This line is key!
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = {
                            Text(
                                text = context.getString(R.string.end_date),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyLarge, // <-- This line is key!
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StatusDropdown(
                        selectedStatus = selectedStatus,
                        onStatusSelected = { selectedStatus = it }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBoxExample(
                        items = units,
                        selectedItem = selectedUnit,
                        onItemSelected = { unit ->
                            selectedUnit = unit
                        },
                        label = context.getString(R.string.unit_number),
                        modifier = Modifier
                            .fillMaxWidth(1f),
                        itemLabel = { unit -> unit.unitNumber.toString() }
                    )
                }
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
                    birthday = "",
                    numberOfTenants = numberOfTenants
                )
                onAddTenant(newTenant, selectedUnit!!)
                sharedViewModel.addTenant(newTenant)
                sharedViewModel.addTenantUnits(newTenant, selectedUnit!!)
                onDismiss()
            },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color))
                )) {
                Text(
                    text = context.getString(R.string.insert),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

        },
        dismissButton = {
            Button(onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color))
                )) {
                Text(
                    text = context.getString(R.string.cancel),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusDropdown(selectedStatus: String, onStatusSelected: (String) -> Unit) {
    val context = LocalContext.current
    val statuses = remember { listOf(context.getString(R.string.active), context.getString(R.string.inactive)) }
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selectedStatus,
            onValueChange = { },
            label = { Text(text = context.getString(R.string.status) ,
                style = MaterialTheme.typography.bodyLarge) },
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
                    text = { Text(text = status,
                            style = MaterialTheme.typography.bodyLarge,) },
                    onClick = {
                        onStatusSelected(status)
                        expanded = false
                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitPage(
    unitsViewModel: UnitsViewModel,
    sharedViewModel: SharedViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    var showUnitDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar with Back Button
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = context.getString(R.string.units_list),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(sharedViewModel.unitsList) { unit ->
                UnitItem(unit = unit, sharedViewModel = sharedViewModel, unitsViewModel = unitsViewModel)
            }

            // Add "Add New Unit" as the last item
            item {
                AddNewUnitItem(onClick = { showUnitDialog = true })
            }
        }

        // Navigation Buttons (Back/Next)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
                )
            ) {
                Text(context.getString(R.string.back),
                    modifier = Modifier.padding(2.dp),
                    style = MaterialTheme.typography.bodyLarge)
            }

            Button(onClick = onNext,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
                )
            ) {
                Text(context.getString(R.string.next),
                    modifier = Modifier.padding(2.dp),
                    style = MaterialTheme.typography.bodyLarge)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }

    // Unit Dialog
    if (showUnitDialog) {
        UnitDialog(
            onDismiss = { showUnitDialog = false },
            onAddUnit = { newUnit ->
                lifecycleOwner.lifecycleScope.launch {
                    val unitId = unitsViewModel.insertUnit(newUnit)
                    Log.d("unitId", unitId.toString())
                    sharedViewModel.unitsList.add(newUnit.copy(unitId = unitId)) // Update the unitId
                    showUnitDialog = false
                }
            }
        )

    }
}

@Composable
fun UnitItem(unit: Units, sharedViewModel : SharedViewModel, unitsViewModel: UnitsViewModel) {
    var showEditDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = { showEditDialog = true }),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = context.getString(R.string.unit_name) + ": ${unit.unitNumber}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }

    if (showEditDialog) {
        val lifecycleOwner = LocalLifecycleOwner.current
        EditUnitDialog(
            unit = unit,
            onDismiss = { showEditDialog = false },
            onUpdateUnit = { updatedUnit ->
                lifecycleOwner.lifecycleScope.launch {
                    val unitId = unitsViewModel.updateUnit(updatedUnit)
                    Log.d("unitId", unitId.toString())
                    val index = sharedViewModel.unitsList.indexOfFirst { it.unitId == updatedUnit.unitId }
                    if (index != -1) {
                        sharedViewModel.unitsList[index] = updatedUnit
                    }
                    Toast.makeText(context, context.getString(R.string.success_update), Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@Composable
fun AddNewUnitItem(onClick: () -> Unit) {
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
                contentDescription = "Add New Unit",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = LocalContext.current.getString(R.string.add_new_unit),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun UnitDialog(
    onDismiss: () -> Unit,
    onAddUnit: (Units) -> Unit
) {
    var unitNumber by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var numberOfRooms by remember { mutableStateOf("") }
//    var selectedOwner by remember { mutableStateOf<Owners?>(null) }
    val context = LocalContext.current

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = context.getString(R.string.add_new_unit),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = unitNumber,
                        onValueChange = { unitNumber = it },
                        label = { Text(text = context.getString(R.string.unit_number),
                            style = MaterialTheme.typography.bodyLarge) },
                        textStyle = MaterialTheme.typography.bodyLarge, // <-- This line is key!
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = area,
                        onValueChange = { area = it },
                        label = { Text(text = context.getString(R.string.area),
                            style = MaterialTheme.typography.bodyLarge) },
                        textStyle = MaterialTheme.typography.bodyLarge, // <-- This line is key!
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = numberOfRooms,
                        onValueChange = { numberOfRooms = it },
                        label = { Text(text = context.getString(R.string.number_of_rooms),
                            style = MaterialTheme.typography.bodyLarge) },
                        textStyle = MaterialTheme.typography.bodyLarge, // <-- This line is key!
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Owner Selection


                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newUnit = Units(
                            unitNumber = unitNumber,
                            area = area,
                            numberOfRooms = numberOfRooms
                        )
                        onAddUnit(newUnit)
                        Log.d("newUnit", newUnit.toString())
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(context.getColor(R.color.secondary_color))
                    )
                ) {
                    Text(
                        text = context.getString(R.string.insert),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(context.getColor(R.color.secondary_color))
                    )
                ) {
                    Text(
                        text = context.getString(R.string.cancel),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        )
}

@Composable
fun EditUnitDialog(
    unit: Units,
    onDismiss: () -> Unit,
    onUpdateUnit: (Units) -> Unit
) {
    var unitNumber by remember { mutableStateOf(unit.unitNumber) }
    var area by remember { mutableStateOf(unit.area) }
    var numberOfRooms by remember { mutableStateOf(unit.numberOfRooms) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = context.getString(R.string.edit_unit),
            style = MaterialTheme.typography.bodyLarge) },
        text = {
            Column {
                OutlinedTextField(
                    value = unitNumber,
                    onValueChange = { unitNumber = it },
                    label = { Text(context.getString(R.string.unit_number)) },
                    textStyle = MaterialTheme.typography.bodyLarge, // <-- This line is key!
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text(context.getString(R.string.area)) },
                    textStyle = MaterialTheme.typography.bodyLarge, // <-- This line is key!
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = numberOfRooms,
                    onValueChange = { numberOfRooms = it },
                    label = { Text(context.getString(R.string.number_of_rooms)) },
                    textStyle = MaterialTheme.typography.bodyLarge, // <-- This line is key!
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        },
        confirmButton = {
            Button(onClick = {
                val updatedUnit = Units(
                    unitNumber = unitNumber,
                    area = area,
                    numberOfRooms = numberOfRooms,
                    unitId = unit.unitId
                )
                onUpdateUnit(updatedUnit)
                onDismiss()
            },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color)))) {
                Text(text = context.getString(R.string.update) ,
                    style = MaterialTheme.typography.bodyLarge)
            }
        },
        dismissButton = {
            Button(onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color)))) {
                Text(text = context.getString(R.string.cancel) ,
                    style = MaterialTheme.typography.bodyLarge)
            }
        }
    )
}



//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CostPage(
//    sharedViewModel: SharedViewModel,
//    onNext: () -> Unit,
//    onBack: () -> Unit
//) {
//    val context = LocalContext.current
//
//
//
//    Column(modifier = Modifier.fillMaxSize()) {
//        // Top App Bar with Back Button
//        CenterAlignedTopAppBar(
//            title = {
//                Text(
//                    text = context.getString(R.string.costs),
//                    style = MaterialTheme.typography.bodyLarge
//                )
//            },
//            navigationIcon = {
//                IconButton(onClick = onBack) {
//                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = context.getString(R.string.back))
//                }
//            }
//        )
//
//        // Editable Costs List
//        LazyColumn(
//            modifier = Modifier
//                .weight(1f)
//                .fillMaxWidth()
//        ) {
//            items(sharedViewModel.costsList.value) { cost ->
//                CostItem(
//                    cost = cost,
//                    onAmountChange = { newAmount ->
//                        sharedViewModel.updateCostAmount(cost, newAmount)
//                    }
//                )
//            }
//        }
//
//        // Navigation Buttons (Back/Next)
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Button(onClick = onBack,
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
//                )
//            ) {
//                Text(context.getString(R.string.back),
//                    modifier = Modifier.padding(2.dp),
//                    style = MaterialTheme.typography.bodyLarge)
//            }
//
//            Button(onClick = onNext,
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
//                )
//            ) {
//                Text(context.getString(R.string.next),
//                    modifier = Modifier.padding(2.dp),
//                    style = MaterialTheme.typography.bodyLarge)
//            }
//        }
//        Spacer(Modifier.height(32.dp))
//    }
//}
//
//@Composable
//fun CostItem(cost: Costs, onAmountChange: (Double) -> Unit) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(8.dp),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
//        shape = RoundedCornerShape(16.dp),
//        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//        ) {
//            Text(
//                text = cost.costName,
//                style = MaterialTheme.typography.bodyLarge,
//                color = MaterialTheme.colorScheme.onSurface
//            )
//            var amount by remember { mutableStateOf(cost.amount.toString()) }
//            OutlinedTextField(
//                value = amount,
//                onValueChange = { newAmount ->
//                    amount = newAmount
//                    onAmountChange(newAmount.toDoubleOrNull() ?: 0.0)
//                },
//                label = { Text(text = LocalContext.current.getString(R.string.amount), style = MaterialTheme.typography.bodyLarge) },
//                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
//            )
//        }
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CostPage(
    sharedViewModel: SharedViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var sameCosts by remember { mutableStateOf(true) }
    var automaticCharge by remember { mutableStateOf(false) }
    var selectedChargeType by remember { mutableStateOf("") }
    var chargeAmount by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar with Back Button
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = context.getString(R.string.costs),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = context.getString(R.string.back))
                }
            }
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = automaticCharge,
                onCheckedChange = { automaticCharge = it }
            )
            Text(
                text = context.getString(R.string.automatic_charge),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        if (automaticCharge) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
////                verticalAlignment = Alignment.CenterVertically
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
            ChipGroupShared(
                selectedItems = listOf(selectedChargeType),
                onSelectionChange = { newSelection ->
                    selectedChargeType = newSelection.firstOrNull() ?: ""
                },
                items = listOf(context.getString(R.string.area), context.getString(R.string.nafari)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                label = context.getString(R.string.acount_base)
            )
            OutlinedTextField(
                value = chargeAmount,
                onValueChange = { chargeAmount = it },
                label = { Text(context.getString(R.string.base_amount)) },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
//            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = sameCosts,
                onCheckedChange = { sameCosts = it }
            )
            Text(
                text = context.getString(R.string.all_units_same_costs),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        if (sameCosts) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(sharedViewModel.costsList.value.drop(1)) { cost ->
                    CostItem(
                        cost = cost,
                        onAmountChange = { newAmount ->
                            sharedViewModel.updateCostAmount(cost, newAmount)
                        },
                        onPeriodChange = { newPeriod ->
                            sharedViewModel.updateCostPeriod(cost, newPeriod)
                        },
                        onAmountMoneyChange = { newAmountMoney ->
                            sharedViewModel.updateCostAmountMoney(cost, newAmountMoney)
                        }
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(sharedViewModel.unitsList) { unit ->
                    UnitCostItem(
                        unit = unit,
                        sharedViewModel = sharedViewModel
                    )
                }
            }
        }

        // Navigation Buttons (Back/Next)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
                )
            ) {
                Text(context.getString(R.string.back),
                    modifier = Modifier.padding(2.dp),
                    style = MaterialTheme.typography.bodyLarge)
            }

            Button(onClick = onNext,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
                )
            ) {
                Text(context.getString(R.string.next),
                    modifier = Modifier.padding(2.dp),
                    style = MaterialTheme.typography.bodyLarge)
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun CostItem(
    cost: Costs,
    onAmountChange: (Double) -> Unit,
    onPeriodChange: (String) -> Unit,
    onAmountMoneyChange: (String) -> Unit
) {
    val context = LocalContext.current
    val periods = listOf(context.getString(R.string.weekly),
        context.getString(R.string.monthly), context.getString(R.string.yearly))
    val amountUnitOptions = listOf(context.getString(R.string.hezar_toman),
        context.getString(R.string.milion_toman))
    var selectedPeriod by remember { mutableStateOf(cost.period) }
    var selectedAmountMoney by remember { mutableStateOf(cost.amountUnit) }
    var amount by remember { mutableStateOf(cost.amount.toString()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = cost.costName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
//                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ExposedDropdownMenuBoxExample(
                            items = periods,
                            selectedItem = selectedPeriod,
                            onItemSelected = {
                                selectedPeriod = it
                                onPeriodChange(it)
                            },
                            label = context.getString(R.string.period),
                            modifier = Modifier
                                .fillMaxWidth(0.4f),
                            itemLabel = { it }
                        )
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { newAmount ->
                                amount = newAmount
                                onAmountChange(newAmount.toDoubleOrNull() ?: 0.0)
                            },
                            label = { Text(context.getString(R.string.amount)) },
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.1f)
                        )
                        ExposedDropdownMenuBoxExample(
                            items = amountUnitOptions,
                            selectedItem = selectedAmountMoney,
                            onItemSelected = {
                                selectedAmountMoney = it
                                onAmountMoneyChange(it)
                            },
                            label = context.getString(R.string.amount_unit),
                            modifier = Modifier
                                .fillMaxWidth(0.4f),
                            itemLabel = { it }
                        )
                    }
                }
    }
}

@Composable
fun UnitCostItem(
    unit: Units,
    sharedViewModel: SharedViewModel
) {
    var showCostDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = { showCostDialog = true }),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${LocalContext.current.getString(R.string.unit_number)} : ${unit.unitNumber}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }

    if (showCostDialog) {
        UnitCostDialog(
            unit = unit,
            sharedViewModel = sharedViewModel,
            onDismiss = { showCostDialog = false }
        )
    }
}
@Composable
fun UnitCostDialog(
    unit: Units,
    sharedViewModel: SharedViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val periods = listOf(context.getString(R.string.weekly),
        context.getString(R.string.monthly), context.getString(R.string.yearly))
    val amountUnitOptions = listOf(context.getString(R.string.hezar_toman),
        context.getString(R.string.milion_toman))
    val costs = sharedViewModel.costsList.value.drop(1)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = context.getString(R.string.costs), style = MaterialTheme.typography.bodyLarge) },
        text = {
            Column {
                costs.forEach { cost ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = cost.costName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
//                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ExposedDropdownMenuBoxExample(
                                items = periods,
                                selectedItem = cost.period,
                                onItemSelected = {
                                    sharedViewModel.updateCostPeriod(cost, it)
                                },
                                label = context.getString(R.string.period),
                                modifier = Modifier
                                    .fillMaxWidth(0.4f),
                                itemLabel = { it }
                            )
                            OutlinedTextField(
                                value = cost.amount.toString(),
                                onValueChange = { newAmount ->
                                    sharedViewModel.updateCostAmount(cost, newAmount.toDoubleOrNull() ?: 0.0)
                                },
                                label = { Text(context.getString(R.string.amount)) },
                                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                                textStyle = MaterialTheme.typography.bodyLarge, // <-- This line is key!
                                modifier = Modifier.weight(0.2f)
                            )
                            ExposedDropdownMenuBoxExample(
                                items = amountUnitOptions,
                                selectedItem = cost.amountUnit,
                                onItemSelected = {
                                    sharedViewModel.updateCostAmountMoney(cost, it)
                                },
                                label = context.getString(R.string.amount_unit),
                                modifier = Modifier
                                    .fillMaxWidth(0.4f),
                                itemLabel = { it }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
                )) {
                Text(text = context.getString(R.string.insert), style = MaterialTheme.typography.bodyLarge)
            }
        }
    )
}
