package com.example.delta

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import com.example.delta.data.entity.BuildingTypes
import com.example.delta.data.entity.BuildingUsages
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Owners
import com.example.delta.data.entity.Tenants
import com.example.delta.data.entity.Units
import com.example.delta.factory.SharedViewModelFactory
import com.example.delta.init.NumberCommaTransformation
import com.example.delta.viewmodel.BuildingTypeViewModel
import com.example.delta.viewmodel.BuildingUsageViewModel
import com.example.delta.viewmodel.SharedViewModel
import com.example.delta.viewmodel.TenantViewModel
import com.example.delta.viewmodel.UnitsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale


class BuildingFormActivity : ComponentActivity() {
    private val tenantViewModel: TenantViewModel by viewModels()
    private val buildingTypeViewModel: BuildingTypeViewModel by viewModels()
    private val buildingUsageViewModel: BuildingUsageViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by viewModels {
        SharedViewModelFactory(application = this.application)
    }
    private val unitsViewModel: UnitsViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
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
                        buildingTypes = buildingTypes,
                        tenantViewModel = tenantViewModel,
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BuildingFormScreen(
    unitsViewModel: UnitsViewModel,
    tenantViewModel: TenantViewModel,
    buildingTypeViewModel: BuildingTypeViewModel,
    buildingUsageViewModel: BuildingUsageViewModel,
    buildingTypes: List<BuildingTypes>,
    buildingUsages: List<BuildingUsages>,
    sharedViewModel: SharedViewModel,
) {
    var currentPage by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    Column {
        if (currentPage == 0) {
            BuildingInfoPage(
                sharedViewModel = sharedViewModel,
                buildingTypeViewModel = buildingTypeViewModel,
                buildingUsageViewModel = buildingUsageViewModel,
                unitsViewModel = unitsViewModel,
                buildingTypes = buildingTypes,
                buildingUsages = buildingUsages,
                onNext = { currentPage++ }
            )
        } else if (currentPage == 1) {
            UnitPage(
                unitsViewModel = unitsViewModel,
                sharedViewModel = sharedViewModel,
                onNext = { currentPage++ },
                onBack = { currentPage = 0 })

        } else if (currentPage == 2) {
            OwnersPage(
                sharedViewModel = sharedViewModel,
                onNext = { currentPage++ },
                onBack = { currentPage = 1 }
            )

        } else if (currentPage == 3) {
            TenantsPage(
                sharedViewModel = sharedViewModel,
                onBack = { currentPage = 2 },
                onNext = { currentPage++ }
            )

        } else if (currentPage == 4) {
            CostPage(
                sharedViewModel = sharedViewModel,
                onSave = {

                    lifecycleOwner.lifecycleScope.launch {
                        withContext(Dispatchers.IO) {

                            sharedViewModel.saveBuildingWithUnitsAndOwnersAndTenants(
                                tenantsUnitsCrossRef = tenantViewModel.getAllTenantUnitRelations(),
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

                    }
                },
                onBack = { currentPage = 3 }
            )
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildingInfoPage(
    sharedViewModel: SharedViewModel,
    buildingTypeViewModel: BuildingTypeViewModel,
    buildingUsageViewModel: BuildingUsageViewModel,
    unitsViewModel: UnitsViewModel,
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
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = context.getString(R.string.back)
                    )
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
                    items = buildingTypes + BuildingTypes(
                        0,
                        context.getString(R.string.addNew)
                    ), // Add "Add New" option
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
                    items = buildingUsages + BuildingUsages(
                        0,
                        context.getString(R.string.addNew)
                    ), // Add "Add New" option
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
                    modifier = Modifier
                        .fillMaxWidth(1f),
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
                    items = listOf(
                        context.getString(R.string.gas),
                        context.getString(R.string.water),
                        context.getString(R.string.electricity)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    label = context.getString(R.string.shared_things)
                )
            }

            item {
                UploadFile(
                    context = LocalContext.current,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Fixed Bottom Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End // Align button to the right
        ) {
            val lifecycleOwner = LocalLifecycleOwner.current
            Button(
                onClick = {
                    lifecycleOwner.lifecycleScope.launch {
                        if (sharedViewModel.sameArea && !sharedViewModel.unitsAdded) {
                            val numUnits = sharedViewModel.numberOfUnits.toInt()
                            val area = sharedViewModel.unitArea.toString()
                            for (i in 1..numUnits) {
                                val newUnit = Units(
                                    unitNumber = i.toString(),
                                    area = area,
                                    numberOfRooms = "1",
                                    numberOfParking = "1"
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
                Text(
                    context.getString(R.string.next),
                    modifier = Modifier.padding(2.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
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
                    sharedViewModel.selectedBuildingTypes =
                        newType // Update selected item in dropdown
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
    val context = LocalContext.current
    val ownersList = sharedViewModel.ownersList.toList()
    var showOwnerDialog by remember { mutableStateOf(false) }
    var selectedOwner by remember { mutableStateOf<Owners?>(null) } // Track selected owner

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
                OwnerItem(
                    owner = owner,
                    sharedViewModel = sharedViewModel,
                    onDelete = {
                        sharedViewModel.deleteOwner(
                            owner = owner,
                            onSuccess = {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.success_delete),
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onError = { error ->
                                Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                            }
                        )
                    })
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
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
                )
            ) {
                Text(
                    context.getString(R.string.back),
                    modifier = Modifier.padding(2.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
                )
            ) {
                Text(
                    context.getString(R.string.next),
                    modifier = Modifier.padding(2.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
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
    sharedViewModel: SharedViewModel,
    modifier: Modifier = Modifier,
    onDelete: (Owners) -> Unit // Pass a callback for deletion
) {
    var context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = { showEditDialog = true })
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "${context.getString(R.string.first_name)}: ${owner.firstName}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = "${context.getString(R.string.last_name)}: ${owner.lastName}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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

            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Actions",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            LocalContext.current.getString(R.string.delete),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    onClick = {
                        showMenu = false
                        showDeleteDialog = true
                    }
                )
            }
        }
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = {
                    Text(
                        text = LocalContext.current.getString(R.string.delete_owner),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                text = {
                    Text(
                        text = LocalContext.current.getString(R.string.are_you_sure),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            onDelete(owner)
                        }
                    ) {
                        Text(
                            LocalContext.current.getString(R.string.delete),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog = false }
                    ) {
                        Text(
                            LocalContext.current.getString(R.string.cancel),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            )
        }
        if (showEditDialog) {
            EditOwnerDialog(
                units = sharedViewModel.unitsList,
                owner = owner,
                onDismiss = { showEditDialog = false },
                onSave = { updatedOwner, selectedUnits ->
                    sharedViewModel.updateOwnerWithUnits(updatedOwner, selectedUnits)
                    showEditDialog = false
                },
                sharedViewModel = sharedViewModel
            )
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
    var selectedUnits by remember { mutableStateOf(sharedViewModel.selectedUnits) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = context.getString(R.string.add_new_owner),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { newValue -> // Update the ViewModel on change
                        firstName = newValue
                    },
                    label = {
                        Text(
                            text = context.getString(R.string.first_name),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge, // <-- This line is key!
                    modifier = Modifier.fillMaxWidth()
                )
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
            Button(
                onClick = {
                    val newOwner =
                        Owners(
                            firstName = firstName,
                            lastName = lastName,
                            address = address,
                            email = email,
                            phoneNumber = phoneNumber,
                            mobileNumber = mobileNumber,
                            birthday = ""
                        )
                    onAddOwner(newOwner, selectedUnits)
                    sharedViewModel.addOwner(newOwner)
                    sharedViewModel.addOwnerUnits(newOwner, selectedUnits)
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantsPage(
    sharedViewModel: SharedViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    var showTenantDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current

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
                TenantItem(
                    tenants = tenant,
                    sharedViewModel = sharedViewModel,
                    onDelete = {
                        sharedViewModel.deleteTenant(
                            tenant = tenant,
                            onSuccess = {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.success_delete),
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onError = { error ->
                                Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                            }
                        )
                    })
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

            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
                )
            ) {
                Text(
                    context.getString(R.string.back),
                    modifier = Modifier.padding(2.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
                )
            ) {
                Text(
                    context.getString(R.string.next),
                    modifier = Modifier.padding(2.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
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
                    lifecycleOwner.lifecycleScope.launch {
                        sharedViewModel.saveTenantWithUnit(newTenant, selectedUnit)
                        showTenantDialog = false
                    }
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
    sharedViewModel: SharedViewModel,
    modifier: Modifier = Modifier,
    onDelete: (Tenants) -> Unit
) {
    var context = LocalContext.current
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = { showEditDialog = true }),
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

            // Tenant Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "${context.getString(R.string.first_name)}: ${tenants.firstName}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = "${context.getString(R.string.last_name)}: ${tenants.lastName}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
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
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Actions",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            LocalContext.current.getString(R.string.delete),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    onClick = {
                        showMenu = false
                        showDeleteDialog = true
                    }
                )
            }
        }
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = {
                    Text(
                        text = LocalContext.current.getString(R.string.delete_owner),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                text = {
                    Text(
                        text = LocalContext.current.getString(R.string.are_you_sure),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            onDelete(tenants)
                        }
                    ) {
                        Text(
                            LocalContext.current.getString(R.string.delete),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog = false }
                    ) {
                        Text(
                            LocalContext.current.getString(R.string.cancel),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            )
        }
        if (showEditDialog) {
            tenants.let { tenant ->
                EditTenantDialog(
                    tenant = tenant,
                    units = sharedViewModel.unitsList,
                    sharedViewModel = sharedViewModel,
                    onDismiss = { showEditDialog = false },
                    onSave = { updatedTenant, selectedUnit ->
                        // Call ViewModel to update both tenant and association
                        sharedViewModel.updateTenantWithUnit(updatedTenant, selectedUnit)
                        showEditDialog = false
                    }
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
    val context = LocalContext.current
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var numberOfTenants by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf(context.getString(R.string.active)) }
    var selectedUnit by remember { mutableStateOf<Units?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // Check if date pickers are shown and dismiss others
    val dismissDatePicker: () -> Unit = {
        showStartDatePicker = false
        showEndDatePicker = false
    }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                context.getString(R.string.add_new_tenant),
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
                        label = { Text(context.getString(R.string.start_date)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) showStartDatePicker = true
                            },
                        readOnly = true
                    )

                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text(context.getString(R.string.end_date)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) showEndDatePicker = true
                            },
                        readOnly = true
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
            if (showStartDatePicker) {
                PersianDatePickerDialogContent(
                    onDateSelected = { selected ->
                        startDate = selected
                        dismissDatePicker()
                    },
                    onDismiss = { dismissDatePicker() },
                    context = context
                )
            }

            if (showEndDatePicker) {
                PersianDatePickerDialogContent(
                    onDateSelected = { selected ->
                        endDate = selected
                        dismissDatePicker()
                    },
                    onDismiss = { dismissDatePicker() },
                    context = context
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newTenant = Tenants(
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        phoneNumber = phoneNumber,
                        mobileNumber = mobileNumber,
                        startDate = startDate,
                        endDate = endDate,
                        status = selectedStatus,
                        birthday = "",
                        numberOfTenants = numberOfTenants
                    )
                    selectedUnit?.let {
                        onAddTenant(newTenant, it)
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
                )
            ) {
                Text(
                    context.getString(R.string.insert),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusDropdown(selectedStatus: String, onStatusSelected: (String) -> Unit) {
    val context = LocalContext.current
    val statuses = remember {
        listOf(
            context.getString(R.string.active),
            context.getString(R.string.inactive)
        )
    }
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selectedStatus,
            onValueChange = { },
            label = {
                Text(
                    text = context.getString(R.string.status),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            statuses.forEach { status ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = status,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
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
                UnitItem(
                    unit = unit,
                    sharedViewModel = sharedViewModel,
                    unitsViewModel = unitsViewModel
                )
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
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
                )
            ) {
                Text(
                    context.getString(R.string.back),
                    modifier = Modifier.padding(2.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
                )
            ) {
                Text(
                    context.getString(R.string.next),
                    modifier = Modifier.padding(2.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
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
                    sharedViewModel.unitsList.add(newUnit.copy(unitId = unitId)) // Update the unitId
                    showUnitDialog = false
                }
            }
        )

    }
}

@Composable
fun UnitItem(unit: Units, sharedViewModel: SharedViewModel, unitsViewModel: UnitsViewModel) {
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
            modifier = Modifier
                .fillMaxSize()
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
                    val index =
                        sharedViewModel.unitsList.indexOfFirst { it.unitId == updatedUnit.unitId }
                    if (index != -1) {
                        sharedViewModel.unitsList[index] = updatedUnit
                    }
                    Toast.makeText(
                        context,
                        context.getString(R.string.success_update),
                        Toast.LENGTH_SHORT
                    ).show()
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

@SuppressLint("SuspiciousIndentation")
@Composable
fun UnitDialog(
    onDismiss: () -> Unit,
    onAddUnit: (Units) -> Unit
) {
    var unitNumber by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var numberOfRooms by remember { mutableStateOf("") }
    var numberOfParking by remember { mutableStateOf("") }
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
                    label = {
                        Text(
                            text = context.getString(R.string.unit_number),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge, // <-- This line is key!
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = {
                        Text(
                            text = context.getString(R.string.area),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge, // <-- This line is key!
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = numberOfRooms,
                    onValueChange = { numberOfRooms = it },
                    label = {
                        Text(
                            text = context.getString(R.string.number_of_rooms),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge, // <-- This line is key!
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = numberOfParking,
                    onValueChange = { numberOfParking = it },
                    label = {
                        Text(
                            text = context.getString(R.string.number_of_parking),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge, // <-- This line is key!
                    modifier = Modifier.fillMaxWidth()
                )

                // Owner Selection


            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newUnit = Units(
                        unitNumber = unitNumber,
                        area = area,
                        numberOfRooms = numberOfRooms,
                        numberOfParking = numberOfParking
                    )
                    onAddUnit(newUnit)
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
    var numberOfParking by remember { mutableStateOf(unit.numberOfParking) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = context.getString(R.string.edit_unit),
                style = MaterialTheme.typography.bodyLarge
            )
        },
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

                OutlinedTextField(
                    value = numberOfParking,
                    onValueChange = { numberOfParking = it },
                    label = { Text(context.getString(R.string.number_of_parking)) },
                    textStyle = MaterialTheme.typography.bodyLarge, // <-- This line is key!
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedUnit = Units(
                        unitNumber = unitNumber,
                        area = area,
                        numberOfRooms = numberOfRooms,
                        unitId = unit.unitId,
                        numberOfParking = numberOfParking
                    )
                    onUpdateUnit(updatedUnit)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color))
                )
            ) {
                Text(
                    text = context.getString(R.string.update),
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

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CostPage(
    sharedViewModel: SharedViewModel,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showUnitCostDialog by remember { mutableStateOf<Units?>(null) } // Track which unit dialog to show
    val amount = sharedViewModel.fixedAmount.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val amountInWords = NumberCommaTransformation().numberToWords(context, amount)

    val chargeAmount = sharedViewModel.chargeAmount.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val chargeAmountInWords = NumberCommaTransformation().numberToWords(context, chargeAmount)

    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar with Back Button
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = context.getString(R.string.costs),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = sharedViewModel.automaticCharge,
                onCheckedChange = { sharedViewModel.automaticCharge = it }
            )

            Text(
                text = context.getString(R.string.automatic_charge),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Only show when automaticCharge is FALSE
            if (!sharedViewModel.automaticCharge) {
                Column(
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                ) {
                    OutlinedTextField(
                        value = sharedViewModel.fixedAmount,
                        onValueChange = { sharedViewModel.updateFixedAmountFormat(it) },
                        visualTransformation = NumberCommaTransformation(),
                        label = { Text(context.getString(R.string.fixed_amount)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    if (amount > 0) {
                        Text(
                            text = "$amountInWords  ${context.getString(R.string.toman)}",
                            modifier = Modifier.padding(top = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(context.getColor(R.color.grey))
                        )

                    }
                }

            }
        }

        if (sharedViewModel.automaticCharge) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChipGroupShared(
                    selectedItems = sharedViewModel.selectedChargeType,
                    onSelectionChange = { newSelection ->
                        sharedViewModel.selectedChargeType = newSelection
                    },
                    items = listOf(
                        context.getString(R.string.area),
                        context.getString(R.string.nafari)
                    ),
                    modifier = Modifier
                        .padding(vertical = 8.dp),
                    label = context.getString(R.string.acount_base),
                    singleSelection = true
                )
                Spacer(Modifier.width(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                ) {
                    OutlinedTextField(
                        value = sharedViewModel.chargeAmount,
                        onValueChange = { sharedViewModel.updateChargeAmountFormat(it) },
                        visualTransformation = NumberCommaTransformation(),
                        label = { Text(context.getString(R.string.base_amount)) },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
//                    modifier = Modifier.fillMaxWidth()
                    )
                    if (chargeAmount > 0) {
                        Text(
                            text = "$chargeAmountInWords  ${context.getString(R.string.toman)}",
                            modifier = Modifier.padding(top = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(context.getColor(R.color.grey))
                        )

                    }
                }

            }
        }


        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = sharedViewModel.sameCosts,
                onCheckedChange = {
                    sharedViewModel.sameCosts = it
                    sharedViewModel.clearAllCostAmount()
                    sharedViewModel.clearDebtList()
                }
            )
            Text(
                text = context.getString(R.string.all_units_same_costs),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        if (sharedViewModel.sameCosts) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(sharedViewModel.costsList.value) { cost ->
                    CostItem(
                        cost = cost,
                        sharedViewModel = sharedViewModel
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
                        onUnitClick = { clickedUnit -> // Callback when a unit is clicked
                            showUnitCostDialog = clickedUnit
                        }
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
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
                )
            ) {
                Text(
                    context.getString(R.string.back),
                    modifier = Modifier.padding(2.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
                )
            ) {
                Text(
                    context.getString(R.string.insert),
                    modifier = Modifier.padding(2.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        Spacer(Modifier.height(32.dp))

        //Conditionally display unit cost dialog, UnitCostDialo will display and get value of each unit if it clicked
        showUnitCostDialog?.let { selectedUnit ->
            UnitCostDialog(
                unit = selectedUnit, sharedViewModel = sharedViewModel,
                onSave = {
                    showUnitCostDialog = null
                },
                onDismiss = { showUnitCostDialog = null })
        }
    }
}

@Composable
fun CostItem(
    cost: Costs,
    sharedViewModel: SharedViewModel
) {
    val context = LocalContext.current
    val transformation = remember { NumberCommaTransformation() }

    // State for raw input (digits only)
    var amount by remember { mutableStateOf(cost.tempAmount.toString()) }


    // State for formatted display
    val displayAmount = remember(amount) {
        derivedStateOf {
            amount.toLongOrNull()?.let {
                NumberFormat.getNumberInstance(Locale.US).format(it)
            } ?: ""
        }
    }

    // Convert to words
    val amountInWords = remember(amount) {
        derivedStateOf {
            transformation.numberToWords(
                context,
                amount.toLongOrNull() ?: 0L
            )
        }
    }


    val coroutineScope = rememberCoroutineScope()

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
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Period Dropdown (30%)
                ExposedDropdownMenuBoxExample(
                    items = sharedViewModel.periods,
                    selectedItem = cost.period.firstOrNull() ?: "",
                    onItemSelected = { sharedViewModel.updateCostPeriod(cost, it) },
                    label = context.getString(R.string.period),
                    modifier = Modifier.weight(0.4f),
                    itemLabel = { it }
                )

                Spacer(Modifier.width(4.dp))

                // Amount Section (70%)
                Column(
                    modifier = Modifier.weight(0.6f)
                ) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { newValue ->
                            val filtered = newValue.filter { it.isDigit() }
                            amount = newValue.filter { it.isDigit() }
                            coroutineScope.launch {
                                delay(500L)
                                sharedViewModel.updateCostAmount(
                                    cost,
                                    filtered.persianToEnglishDigits().toDoubleOrNull() ?: 0.0
                                )
                            }
                        },
                        label = { Text(context.getString(R.string.amount)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = transformation,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = " ${amountInWords.value} ${context.getString(R.string.toman)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(context.getColor(R.color.grey)),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun UnitCostDialog(
    unit: Units,
    sharedViewModel: SharedViewModel,
    onDismiss: () -> Unit,
    onSave: () -> Unit
//    onAddDebt: (Long, List<Costs>) -> Unit
) {
    val context = LocalContext.current
    // Local amount state
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false), // Critical for padding control
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp), // Remove outer padding
        title = {
            Text(
                text = context.getString(R.string.costs),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(0.dp)
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp)
            ) {
                items(sharedViewModel.costsList.value) { cost ->
                    CostItem(
                        cost = cost,
                        sharedViewModel = sharedViewModel
                    )

                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    sharedViewModel.addTempDebt(unit.unitId)
                    onSave()
//                    onAddDebt(unit.unitId, sharedViewModel.costsList.value)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
                )
            ) {
                Text(
                    text = context.getString(R.string.insert),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    )
}


@Composable
fun UnitCostItem(
    unit: Units,
    onUnitClick: (Units) -> Unit // Callback when the unit is clicked
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = { onUnitClick(unit) }), // Call the callback
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${LocalContext.current.getString(R.string.unit_number)} : ${unit.unitNumber}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

fun String.persianToEnglishDigits(): String {
    return this.map { char ->
        when (char) {
            '۰' -> '0'
            '۱' -> '1'
            '۲' -> '2'
            '۳' -> '3'
            '۴' -> '4'
            '۵' -> '5'
            '۶' -> '6'
            '۷' -> '7'
            '۸' -> '8'
            '۹' -> '9'
            else -> char
        }
    }.joinToString("")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditOwnerDialog(
    owner: Owners,
    units: List<Units>,
    sharedViewModel: SharedViewModel,
    onDismiss: () -> Unit,
    onSave: (Owners, List<Units>) -> Unit // Modified onSave
) {
    val context = LocalContext.current
    var firstName by remember { mutableStateOf(owner.firstName) }
    var phone by remember { mutableStateOf(owner.phoneNumber) }
    var lastName by remember { mutableStateOf(owner.lastName) }
    var address by remember { mutableStateOf(owner.address) }
    var email by remember { mutableStateOf(owner.email) }
    var mobileNumber by remember { mutableStateOf(owner.mobileNumber) }

    val unitsForOwner by sharedViewModel.getUnitsForOwners(ownerId = owner.ownerId)
        .collectAsState(initial = emptyList())

    // Local selection state
    val selectedUnits = remember { mutableStateListOf<Units>() }

    // Initialize selection when units load
    LaunchedEffect(unitsForOwner) {
        selectedUnits.clear()
        selectedUnits.addAll(unitsForOwner)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = context.getString(R.string.edit_owner),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { newValue -> // Update the ViewModel on change
                        firstName = newValue
                    },
                    label = {
                        Text(
                            text = context.getString(R.string.first_name),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge, // <-- This line is key!
                    modifier = Modifier.fillMaxWidth()
                )
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
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = {
                        Text(
                            text = context.getString(R.string.phone_number),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge, // <-- This line is key!
                    modifier = Modifier.fillMaxWidth()
                )
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
                ChipGroupUnits(
                    selectedUnits = selectedUnits,
                    onSelectionChange = { newSelection ->
                        selectedUnits.clear()
                        selectedUnits.addAll(newSelection)
                    },
                    units = units,
                    label = context.getString(R.string.units)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedOwner = owner.copy(
                        firstName = firstName,
                        lastName = lastName,
                        address = address,
                        email = email,
                        phoneNumber = phone,
                        mobileNumber = mobileNumber,
                        birthday = ""
                    )
                    onSave(updatedOwner, selectedUnits.toList())
                }, colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
                )
            ) {
                Text(
                    text = context.getString(R.string.edit),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTenantDialog(
    tenant: Tenants,
    units: List<Units>,
    sharedViewModel: SharedViewModel,
    onDismiss: () -> Unit,
    onSave: (Tenants, Units?) -> Unit
) {
    val context = LocalContext.current
    var firstName by remember { mutableStateOf(tenant.firstName) }
    var lastName by remember { mutableStateOf(tenant.lastName) }
    var phone by remember { mutableStateOf(tenant.phoneNumber) }
    var email by remember { mutableStateOf(tenant.email) }
    var mobileNumber by remember { mutableStateOf(tenant.mobileNumber) }
    var startDate by remember { mutableStateOf(tenant.startDate) }
    var numberOfTenants by remember { mutableStateOf(tenant.numberOfTenants) }
    var endDate by remember { mutableStateOf(tenant.endDate) }
    var selectedStatus by remember { mutableStateOf(tenant.status) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var selectedUnit by remember {
        mutableStateOf(sharedViewModel.getUnitForTenant(tenant))
    }

    val dismissDatePicker: () -> Unit = {
        showStartDatePicker = false
        showEndDatePicker = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = context.getString(R.string.edit_tenant),
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
                        value = phone,
                        onValueChange = { phone = it },
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
                        label = { Text(context.getString(R.string.start_date)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) showStartDatePicker = true
                            },
                        readOnly = true
                    )

                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text(context.getString(R.string.end_date)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) showEndDatePicker = true
                            },
                        readOnly = true
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
            if (showStartDatePicker) {
                PersianDatePickerDialogContent(
                    onDateSelected = { selected ->
                        startDate = selected
                        dismissDatePicker()
                    },
                    onDismiss = { dismissDatePicker() },
                    context = context
                )
            }

            if (showEndDatePicker) {
                PersianDatePickerDialogContent(
                    onDateSelected = { selected ->
                        endDate = selected
                        dismissDatePicker()
                    },
                    onDismiss = { dismissDatePicker() },
                    context = context
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedTenant =
                        tenant.copy(
                            firstName = firstName,
                            lastName = lastName,
                            email = email,
                            phoneNumber = phone,
                            mobileNumber = mobileNumber,
                            startDate = startDate,
                            endDate = endDate,
                            status = selectedStatus,
                            birthday = "",
                            numberOfTenants = numberOfTenants
                        )
                    onSave(updatedTenant, selectedUnit)
                    onDismiss()
                }, colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
                )
            ) {
                Text(
                    context.getString(R.string.edit),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
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

// Helper extension


