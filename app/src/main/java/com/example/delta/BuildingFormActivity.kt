package com.example.delta

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.delta.data.entity.BuildingTypes
import com.example.delta.data.entity.BuildingUsages
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.CityComplex
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Debts
import com.example.delta.data.entity.Owners
import com.example.delta.data.entity.OwnersUnitsCrossRef
import com.example.delta.data.entity.Tenants
import com.example.delta.data.entity.Units
import com.example.delta.data.entity.UploadedFileEntity
import com.example.delta.enums.BuildingProfileFields
import com.example.delta.enums.CalculateMethod
import com.example.delta.enums.FundType
import com.example.delta.enums.PaymentLevel
import com.example.delta.enums.Period
import com.example.delta.enums.PermissionLevel
import com.example.delta.enums.Responsible
import com.example.delta.enums.Roles
import com.example.delta.factory.SharedViewModelFactory
import com.example.delta.init.AuthUtils
import com.example.delta.init.NumberCommaTransformation
import com.example.delta.init.Preference
import com.example.delta.init.Validation
import com.example.delta.viewmodel.BuildingTypeViewModel
import com.example.delta.viewmodel.BuildingUsageViewModel
import com.example.delta.viewmodel.SharedViewModel
import com.example.delta.viewmodel.TenantViewModel
import com.example.delta.viewmodel.UnitsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.round


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
        Log.d("onCreate", true.toString())
        sharedViewModel.resetState()
        setContent {
            AppTheme (useDarkTheme = sharedViewModel.isDarkModeEnabled){
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
        Log.d("onResume", true.toString())
//        sharedViewModel.resetState()
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
    // Show loading dialog when saving
    if (sharedViewModel.isLoading) {
        Dialog(onDismissRequest = {}) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.White, shape = RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
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
                onSave = {
                    sharedViewModel.isLoading = true
                    lifecycleOwner.lifecycleScope.launch {
                        withContext(Dispatchers.IO) {

                            val tenantsUnitsCrossRef = tenantViewModel.getAllTenantUnitRelations()
                            sharedViewModel.saveBuildingWithUnitsAndOwnersAndTenants(
                                onSuccess = { building ->
                                    //                                    sharedViewModel.insertBuildingToServer(
//                                        context = context,
//                                        building = building,
//                                        tenantsUnitsCrossRef = tenantsUnitsCrossRef,
//                                        onSuccess = { message ->
//                                            Log.e(
//                                                "SaveSuccess",
//                                                "Error saving building on Server: ${message}"
//                                            )
//                                            sharedViewModel.resetState()
//                                            // Create an Intent to start HomePageActivity
//                                            val intent =
//                                                Intent(context, HomePageActivity::class.java)
//                                            sharedViewModel.isLoading = false
//                                            // Start the activity
//                                            context.startActivity(intent)
//                                        },
//                                        onError = { e ->
//                                            Log.e(
//                                                "SaveError",
//                                                "Error saving building on Server: ${e.message}"
//                                            )
//                                            sharedViewModel.resetState()
//                                            // Create an Intent to start HomePageActivity
//                                            val intent =
//                                                Intent(context, HomePageActivity::class.java)
//                                            sharedViewModel.isLoading = false
//                                            // Start the activity
//                                            context.startActivity(intent)
//                                        }
//                                    )
                                    sharedViewModel.resetState()
                                    // Create an Intent to start HomePageActivity
                                    val intent =
                                        Intent(context, HomePageActivity::class.java)
                                    sharedViewModel.isLoading = false
                                    // Start the activity
                                    context.startActivity(intent)
                                },
                                onError = { errorMessage ->
                                    sharedViewModel.isLoading = false
//                                    Toast.makeText(context, context.getString(R.string.operation_problem),
//                                        Toast.LENGTH_LONG).show()
                                    // show a Toast, etc.
                                    Log.e("SaveError", "Error saving building: $errorMessage")
                                    sharedViewModel.resetState()
                                    // Create an Intent to start HomePageActivity
                                    val intent = Intent(context, HomePageActivity::class.java)
                                    sharedViewModel.isLoading = false
                                    // Start the activity
                                    context.startActivity(intent)
                                },
                                context = context
                            )
                        }

                    }
                }
            )

        }
    }
}

@Composable
fun ChargesChipGroup(
    sharedViewModel: SharedViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Selected cost names (initially all selected or none, customize as needed)
    var selectedCostNames by remember { mutableStateOf<List<String>>(emptyList()) }

    // Compose the list for chips: existing costNames + "Add New" chip
    val chipItems = sharedViewModel.costsList.value + Costs(
        costName = context.getString(R.string.addNew),
        chargeFlag = true,
        fundType = FundType.OPERATIONAL,
        responsible = Responsible.TENANT,
        paymentLevel = PaymentLevel.UNIT,
        calculateMethod = CalculateMethod.EQUAL,
        period = Period.YEARLY,
        dueDate = "",
        tempAmount = 0.0
    )

    var showChargeCostDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    ChipGroupShared(
        selectedItems = selectedCostNames,
        onSelectionChange = { newSelectionStrings ->

            if (newSelectionStrings.contains(context.getString(R.string.addNew))) {
                // If user clicked "Add New", show dialog
                showChargeCostDialog = true
                // Remove the addNew chip label from selection
                selectedCostNames =
                    newSelectionStrings.filter { it != context.getString(R.string.addNew) }
            } else {
                // Normal selection update
                val newlySelected = newSelectionStrings - selectedCostNames
                val newlyDeselected = selectedCostNames - newSelectionStrings

                // Update selectedCostNames state
                selectedCostNames = newSelectionStrings

                // Create updated list for costsList.value by adjusting tempAmount accordingly
                sharedViewModel.costsList.value = sharedViewModel.costsList.value.map { cost ->
                    when {
                        cost.costName in newlySelected -> cost.copy(tempAmount = 1.0)
                        cost.costName in newlyDeselected -> cost.copy(tempAmount = 0.0)
                        else -> cost
                    }
                }
            }
        },
        // Pass only the costName for each Costs item to ChipGroupShared as items
        items = chipItems.map { it.costName },
        modifier = modifier,
        label = context.getString(R.string.charges_parameter),
        singleSelection = false
    )

    if (showChargeCostDialog) {
        AddNewCostDialog(
            onDismiss = {
                showChargeCostDialog = false
            },
            onConfirm = { newCostName ->
                coroutineScope.launch {
                    val newCost = Costs(
                        costName = newCostName,
                        chargeFlag = true,
                        fundType = FundType.OPERATIONAL,
                        responsible = Responsible.TENANT,
                        paymentLevel = PaymentLevel.UNIT,
                        calculateMethod = CalculateMethod.EQUAL,
                        period = Period.YEARLY,
                        tempAmount = 0.0,
                        dueDate = ""
                    )
//                    sharedViewModel.insertNewCost(newCost)
                    // Update selection to include the new cost name after adding it
                    sharedViewModel.costsList.value += newCost

                    showChargeCostDialog = false
                }

            }
        )
    }
}

@Composable
fun AddNewCostDialog(
    onDismiss: () -> Unit,
    onChargeConfirm: ((newCostNameList: List<String>) -> Unit)? = null,
    onConfirm: ((newCostName: String) -> Unit)? = null,
    costs: List<Costs> = emptyList()
) {
    val context = LocalContext.current

    var costName by remember { mutableStateOf("") }
    var selectedCostNames by remember { mutableStateOf<List<String>>(emptyList()) }

    var chipItems by remember(costs) {
        mutableStateOf(costs.map { it.costName } + context.getString(R.string.addNew))
    }

    var showInputField by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = context.getString(R.string.add_new_cost),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        text = {
            LazyColumn {
                if (chipItems.isNotEmpty()) {
                    item {
                        ChipGroupShared(
                            selectedItems = selectedCostNames,
                            onSelectionChange = { newSelected ->
                                if (newSelected.contains(context.getString(R.string.addNew))) {
                                    showInputField = true
                                    selectedCostNames = newSelected.filter { it != context.getString(R.string.addNew) }
                                } else {
                                    selectedCostNames = newSelected
                                }
                            },
                            items = chipItems,
                            modifier = Modifier,
                            label = context.getString(R.string.charges_parameter),
                            singleSelection = false
                        )
                    }
                    if (showInputField) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = costName,
                                onValueChange = { costName = it },
                                label = { Text(context.getString(R.string.cost_name)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    item {
                        OutlinedTextField(
                            value = costName,
                            onValueChange = { costName = it },
                            label = { Text(context.getString(R.string.cost_name)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = (showInputField && costName.isNotBlank()) || (!showInputField && selectedCostNames.isNotEmpty()),
                onClick = {
                    if (showInputField && costName.isNotBlank()) {
                        val trimmedName = costName.trim()
                        // Add new cost name to chipItems (just before "Add New")
                        if (!chipItems.contains(trimmedName)) {
                            chipItems = chipItems.filter { it != context.getString(R.string.addNew) } + trimmedName + context.getString(R.string.addNew)
                        }
                        // Select the new cost
                        selectedCostNames = selectedCostNames + trimmedName

                        // Call corresponding callback with single item or entire list
                        onConfirm?.invoke(trimmedName)
                        onChargeConfirm?.invoke(selectedCostNames)

                        // Reset input
                        costName = ""
                        showInputField = false
                    } else {
                        // Confirm selected chips only
                        onChargeConfirm?.invoke(selectedCostNames)
                    }
                }
            ) {
                Text(
                    text = context.getString(R.string.confirm),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = context.getString(R.string.cancel),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    )
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
    val isValid = Validation().isBuildingInfoValid(sharedViewModel)
    val cityComplexes by sharedViewModel.getAllCityComplex().collectAsState(initial = emptyList())
    var showAddCityComplexDialog by remember { mutableStateOf(false) }

    val chargesCost by sharedViewModel.getAllCostsOfCharges()
        .collectAsState(initial = emptyList())


    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = context.getString(R.string.buildings_info),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            navigationIcon = {
                IconButton(onClick = {
                    context.startActivity(Intent(context, HomePageActivity::class.java))
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
                            style = MaterialTheme.typography.bodyLarge
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
                    sharedViewModel = sharedViewModel,
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
            val buildingTypeName = context.getString(R.string.city_complex)
            if (sharedViewModel.selectedBuildingTypes?.buildingTypeName == buildingTypeName) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    ExposedDropdownMenuBoxExample(
                        sharedViewModel = sharedViewModel,
                        items = cityComplexes + CityComplex(
                            complexId = 0L,
                            name = context.getString(R.string.addNew),
                            address = null
                        ),
                        selectedItem = sharedViewModel.selectedCityComplex,
                        onItemSelected = {
                            if (it.name == context.getString(R.string.addNew)) {
                                showAddCityComplexDialog = true
                            } else {
                                sharedViewModel.selectedCityComplex = it
                            }
                        },
                        label = context.getString(R.string.city_complex),
                        modifier = Modifier.fillMaxWidth(),
                        itemLabel = { it.name }
                    )
                }
            }


            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                ExposedDropdownMenuBoxExample(
                    sharedViewModel = sharedViewModel,
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
                            style = MaterialTheme.typography.bodyLarge
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
                            style = MaterialTheme.typography.bodyLarge
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
                ChargesChipGroup(sharedViewModel)
            }

            item {
                UploadFile(
                    sharedViewModel = sharedViewModel,
                    context = context,
                    onFileSaved = { localPath ->
                        sharedViewModel.addFileList(UploadedFileEntity(fileUrl = localPath))
                    }
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
                            val area = sharedViewModel.unitArea
                            for (i in 1..numUnits) {
                                val newUnit = Units(
                                    unitNumber = i.toString(),
                                    area = area,
                                    numberOfRooms = "1",
                                    numberOfParking = "1",
                                    numberOfWarehouse = "1"
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
                enabled = isValid,
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
//                )
            ) {
                Text(
                    context.getString(R.string.next),
                    modifier = Modifier.padding(2.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        Spacer(Modifier.height(32.dp))

        if (showAddCityComplexDialog) {
            AddCityComplexDialog(
                onDismiss = { showAddCityComplexDialog = false },
                onInsert = { newName, newAddress ->
                    val newComplex = CityComplex(name = newName, address = newAddress)
                    sharedViewModel.insertCityComplex(newComplex) { id ->
                        // After inserting, update selected city complex by id
//                        viewModelScope.launch {
                        val insertedComplex = cityComplexes.find { it.complexId == id }
                        if (insertedComplex != null) {
                            sharedViewModel.selectedCityComplex = insertedComplex
                        }
//                        }
                    }
                    showAddCityComplexDialog = false
                }
            )
        }


        // Dialogs for Adding Items
        if (showBuildingTypeDialog) {
            AddItemDialog(
                sharedViewModel = sharedViewModel,
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
                sharedViewModel = sharedViewModel,
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

    var showOwnerDialog by remember { mutableStateOf(false) }
    // Suppose you have a suspend function to get sums from DB:
    val ownerUnitsState =
        sharedViewModel.getDangSumsForAllUnits().collectAsState(initial = emptyList())
    val ownerUnits = ownerUnitsState.value

// Convert to map for fast lookup
    val dangSumsMap: Map<Long, Double> = ownerUnits.associate { it.unitId to it.totalDang }


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
                    },
                    activity = context.findActivity(),
                    buildingId = 0L
                )
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
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
//                )
            ) {
                Text(
                    context.getString(R.string.back),
                    modifier = Modifier.padding(2.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Button(
                onClick = onNext,
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
//                )
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
                dangSums = dangSumsMap,
                onAddOwner = { newOwner, selectedUnits, isManager, selectedBuilding ->
                    sharedViewModel.saveOwnerWithUnits(newOwner, selectedUnits, isManager, false, 0)
                    showOwnerDialog = false
                },
                sharedViewModel = sharedViewModel,
                isOwner = false,
                building = null
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
    buildingId:Long,
    sharedViewModel: SharedViewModel,
    modifier: Modifier = Modifier,
    onDelete: (Owners) -> Unit, // Pass a callback for deletion
    activity: Activity?
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    val unitsForOwner by sharedViewModel.getUnitsForOwners(ownerId = owner.ownerId)
        .collectAsState(initial = emptyList())

    // Suppose you have a suspend function to get sums from DB:
    val ownerUnitsState =
        sharedViewModel.getDangSumsForAllUnits().collectAsState(initial = emptyList())
    val ownerUnits = ownerUnitsState.value

// Convert to map for fast lookup
    val dangSumsMap: Map<Long, Double> = ownerUnits.associate { it.unitId to it.totalDang }

    val userId = Preference().getUserId(context = context)
    val user by sharedViewModel.getUserById(userId).collectAsState(initial = null)
    val role = sharedViewModel.getRoleByUserId(userId).collectAsState(initial = null)

    // Check permission level (assuming this returns Boolean or similar)
    val permissionLevelOwnersTab = AuthUtils.checkFieldPermission(
        userId,
        BuildingProfileFields.USERS_OWNERS.fieldNameRes,
        sharedViewModel
    )

    val permissionLevelAllOwnersTab = AuthUtils.checkFieldPermission(
        userId,
        BuildingProfileFields.ALL_OWNERS.fieldNameRes,
        sharedViewModel
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                when (activity) {
                    is BuildingFormActivity -> {
                        showEditDialog = true
                    }

                    is BuildingProfileActivity -> {
                        if (permissionLevelAllOwnersTab == PermissionLevel.FULL || permissionLevelAllOwnersTab == PermissionLevel.WRITE) {
                            val intent = Intent(context, OwnerDetailsActivity::class.java)
                            intent.putExtra("ownerId", owner.ownerId)
                            context.startActivity(intent)
                        } else if (owner.mobileNumber == user!!.mobileNumber && (permissionLevelOwnersTab == PermissionLevel.FULL
                                    || permissionLevelOwnersTab == PermissionLevel.WRITE)
                        ) {
                            val intent = Intent(context, OwnerDetailsActivity::class.java)
                            intent.putExtra("ownerId", owner.ownerId)
                            context.startActivity(intent)
                        } else {
                            Modifier
                        }
                    }
                }
            }
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
                Row{
                    if (activity?.javaClass?.name?.endsWith("BuildingProfileActivity") == true) {
                        // Collect isManager state
                        val isManager by sharedViewModel.isOwnerManager(owner.ownerId, buildingId)
                            .collectAsState(initial = false)
                        if(isManager) {
                            Spacer(Modifier.width(16.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.secondary,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = context.getString(R.string.manager), // Or context.getString(R.string.manager)
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                            }
                        }
                    }
                }
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
                if (unitsForOwner.isNotEmpty()) {
                    Text(
                        text = context.getString(R.string.units),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    unitsForOwner.forEach { unit ->
                        Row {
                            Text(
                                text = "${context.getString(R.string.unit_number)}: ${unit.unit.unitNumber}, " +
                                        "${context.getString(R.string.area)}: ${unit.unit.area}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = "${context.getString(R.string.dang)}: ${unit.dang}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
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
                owner = owner,
                units = sharedViewModel.unitsList,
                sharedViewModel = sharedViewModel,
                onDismiss = { showEditDialog = false },
                dangSums = dangSumsMap,
                onSave = { updatedOwner, updatedCrossRefs ->
                    sharedViewModel.updateOwnerWithUnits(updatedOwner, updatedCrossRefs)
                    showEditDialog = false
                }
            )

        }
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun OwnerDialog(
    units: List<Units>,
    dangSums: Map<Long, Double>, // Map<Long, Double>
    onDismiss: () -> Unit,
    onAddOwner: (Owners, List<OwnersUnitsCrossRef>, Boolean, Buildings?) -> Unit,
    sharedViewModel: SharedViewModel,
    isOwner: Boolean = false, // New parameter to determine context
    building: Buildings?
) {
    // Add building-related state
    var selectedBuilding by remember { mutableStateOf<Buildings?>(null) }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var isManager by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var emailError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var mobileError by remember { mutableStateOf(false) }

    // Local state map for dang values keyed by unitId
    val localDangMap = remember {
        mutableStateMapOf<Long, Double>().apply {
            units.forEach { put(it.unitId, 0.0) }
        }
    }

    val buildings by sharedViewModel.getAllBuildings().collectAsState(initial = emptyList())

    // Helper: update local dang value
    fun updateLocalDang(unitId: Long, newDang: Double) {
        val clamped = newDang.coerceIn(0.0, 6.0)
        val rounded = clamped.roundToOneDecimal()
        localDangMap[unitId] = rounded
    }


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
                // Add building selection row for owners
                if (isOwner) {
                    ExposedDropdownMenuBoxExample(
                        sharedViewModel = sharedViewModel,
                        items = buildings,
                        selectedItem = selectedBuilding,
                        onItemSelected = { selectedBuilding = it },
                        label = context.getString(R.string.building),
                        modifier = Modifier.fillMaxWidth(),
                        itemLabel = { building -> building.name }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = {
                        Text(
                            text = context.getString(R.string.first_name),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge,
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
                    textStyle = MaterialTheme.typography.bodyLarge,
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
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = !Validation().isValidEmail(it)
                    },
                    label = {
                        Text(
                            text = context.getString(R.string.email),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    isError = emailError,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
                if (emailError) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = context.getString(R.string.invalid_email),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = {
                        phoneNumber = it
                        phoneError = !Validation().isValidPhone(it)
                    },
                    label = {
                        Text(
                            text = context.getString(R.string.phone_number),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    isError = phoneError,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
                if (phoneError) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = context.getString(R.string.invalid_phone_number),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = {
                        mobileNumber = it
                        mobileError = !Validation().isValidIranMobile(it)
                    },
                    label = {
                        Text(
                            text = context.getString(R.string.mobile_number),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    isError = mobileError,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
                if (mobileError) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = context.getString(R.string.invalid_mobile_number),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isManager,
                        onCheckedChange = { isManager = it }
                    )
                    Text(
                        text = context.getString(R.string.manager_teams), // Add this string to your resources
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                // Unit selection with dang input
                // Modify units list to use building-filtered units
                Log.d("isOwner", isOwner.toString())
                Log.d("selectedBuilding", building.toString())
                Log.d("units", units.toString())

                val filteredUnits = if (isOwner && selectedBuilding != null) {
                    units.filter { it.buildingId == selectedBuilding?.buildingId }
                } else {
                    if (isOwner) {
                        emptyList()
                    } else {
                        units
                    }
                }
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    stickyHeader {
                        HeaderRow()  // Create this composable
                    }
                    items(filteredUnits) { unit ->
                        val dang = localDangMap[unit.unitId] ?: 0.0
                        val otherOwnersDang = dangSums[unit.unitId] ?: 0.0
                        val maxAllowed = (6.0 - otherOwnersDang).coerceAtLeast(0.0)
                        UnitDangRow(
                            unit = unit,
                            dang = dang,
                            maxAllowed = maxAllowed,
                            onDangChange = { newDang ->
                                updateLocalDang(unit.unitId, newDang)
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                        Divider()
                    }
                }

            }
        },
        confirmButton = {
            val isFormValid = !emailError && !phoneError && !mobileError &&
                    email.isNotBlank() && phoneNumber.isNotBlank() && mobileNumber.isNotBlank()
            Button(
                onClick = {
                    val newOwner = Owners(
                        firstName = firstName,
                        lastName = lastName,
                        address = address,
                        email = email,
                        phoneNumber = phoneNumber,
                        mobileNumber = mobileNumber,
                        birthday = ""
                    )
                    val updatedCrossRefs = localDangMap.map { (unitId, dang) ->
                        OwnersUnitsCrossRef(
                            ownerId = 0L, // or real ownerId if editing
                            unitId = unitId,
                            dang = dang
                        )
                    }
                    onAddOwner(newOwner, updatedCrossRefs, isManager, selectedBuilding)
                },
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(context.getColor(R.color.secondary_color))
//                ),
                enabled = isFormValid
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
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(context.getColor(R.color.secondary_color))
//                )
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
    onSave: () -> Unit
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
            items(sharedViewModel.tenantsList) { tenant ->
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
                    },
                    activity = context.findActivity(),
                    onClick = {})
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
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
//                )
            ) {
                Text(
                    context.getString(R.string.back),
                    modifier = Modifier.padding(2.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Button(
                onClick = onSave,
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
//                )
            ) {
                Text(
                    context.getString(R.string.insert),
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
        onDelete: (Tenants) -> Unit,
        onClick: () -> Unit,
        activity: Activity?
    ) {
        val context = LocalContext.current
        var showEditDialog by remember { mutableStateOf(false) }
        var showDeleteDialog by remember { mutableStateOf(false) }
        var showMenu by remember { mutableStateOf(false) }

        val userId = Preference().getUserId(context = context)
        val role = sharedViewModel.getRoleByUserId(userId).collectAsState(initial = null)
        // Collect user state
        val user by sharedViewModel.getUserById(userId).collectAsState(initial = null)
        val unit = sharedViewModel.getUnitForTenant(tenants.tenantId).collectAsState(initial = null)
        // Collect units for user's mobile number (nullable)
        val usersTenant by remember(user?.mobileNumber) {
            if (user?.mobileNumber != null) {
                sharedViewModel.getTenantForUserMobileNumber(user!!.mobileNumber)
            } else {
                // Return empty flow if no mobile number
                flowOf(null)
            }
        }.collectAsState(initial = null)
        Log.d("unit.value", unit.value.toString())
        val owners by sharedViewModel.getOwnersForUnit(unit.value?.unitId ?: 0L)
            .collectAsState(initial = emptyList())

        val allOwners by sharedViewModel.getAllOwnerUnitsCrossRefs().collectAsState(initial = emptyList())
        Log.d("allOwners", allOwners.toString())
        Log.d("owners", owners.toString())
        val isUnitOwner = remember(owners, user) {
            user?.mobileNumber?.let { userMobile ->
                owners.any { owner ->
                    Log.d("owner.mobileNumber", owner.mobileNumber.toString())
                    Log.d("userMobile ", userMobile.toString())
                    owner.mobileNumber == userMobile
                }
            } ?: false
        }
        val currentIsUnitOwner by rememberUpdatedState(isUnitOwner)
    Log.d("isUnitOwner", isUnitOwner.toString())

    // Check permission level (assuming this returns Boolean or similar)
    val permissionLevelTenantsTab = AuthUtils.checkFieldPermission(
        userId,
        BuildingProfileFields.USERS_TENANTS.fieldNameRes,
        sharedViewModel
    )

    val permissionLevelAllTenantsTab = AuthUtils.checkFieldPermission(
        userId,
        BuildingProfileFields.ALL_TENANTS.fieldNameRes,
        sharedViewModel
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                when (activity) {
                    is BuildingFormActivity -> {
                        showEditDialog = true
                    }

                    is BuildingProfileActivity -> {
                        Log.d("currentIsUnitOwner", currentIsUnitOwner.toString())
                        if (role.value!!.roleName == Roles.ADMIN || role.value!!.roleName == Roles.BUILDING_MANAGER) {
                            onClick()
                        } else if (currentIsUnitOwner) {
                            onClick()
                        } else if (permissionLevelAllTenantsTab == PermissionLevel.FULL || permissionLevelAllTenantsTab == PermissionLevel.WRITE) {
                            onClick()
                        } else if (usersTenant == tenants && permissionLevelTenantsTab == PermissionLevel.FULL) {
                            onClick()
                        } else {
                            Modifier
                            Toast.makeText(
                                context,
                                context.getString(R.string.auth_cancel),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            },
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
//                Text(
//                    text = "${context.getString(R.string.email)}: ${tenants.email}",
//                    style = MaterialTheme.typography.bodyLarge,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
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
            EditTenantDialog(
                tenant = tenants,
                units = sharedViewModel.unitsList,
                sharedViewModel = sharedViewModel,
                onDismiss = { showEditDialog = false },
                onSave = { updatedTenant, selectedUnit ->
                    // Call ViewModel to update both tenant and association
                    sharedViewModel.updateTenantWithUnit(updatedTenant, selectedUnit)
                    showEditDialog = false
                },
                isOwner = false
            )
        }
    }
}

@Composable
fun TenantDialog(
    sharedViewModel: SharedViewModel,
    units: List<Units>,
    onDismiss: () -> Unit,
    onAddTenant: (Tenants, Units) -> Unit,
    isTenant: Boolean = false
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
    var selectedBuilding by remember { mutableStateOf<Buildings?>(null) }

    var emailError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var mobileError by remember { mutableStateOf(false) }
    var periodConflictError by remember { mutableStateOf(false) }
    val buildings by sharedViewModel.getAllBuildings().collectAsState(initial = emptyList())

    // Check if date pickers are shown and dismiss others
    val dismissDatePicker: () -> Unit = {
        showStartDatePicker = false
        showEndDatePicker = false
    }
    LaunchedEffect(startDate) {
        if (startDate.isNotEmpty()) {
            endDate =
                sharedViewModel.getNextYearSameDaySafe(sharedViewModel.parsePersianDate(startDate)).persianShortDate

        }
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
                        onValueChange = {
                            email = it
                            emailError = !Validation().isValidEmail(it)
                        },
                        label = {
                            Text(
                                text = context.getString(R.string.email),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        isError = emailError,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (emailError) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = context.getString(R.string.invalid_email),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = {
                            phoneNumber = it
                            phoneError = !Validation().isValidPhone(it)
                        },
                        label = {
                            Text(
                                text = context.getString(R.string.phone_number),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        isError = phoneError,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (phoneError) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = context.getString(R.string.invalid_phone_number),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = {
                            mobileNumber = it
                            mobileError = !Validation().isValidIranMobile(it)
                        },
                        label = {
                            Text(
                                text = context.getString(R.string.mobile_number),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        isError = mobileError,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (mobileError) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = context.getString(R.string.invalid_mobile_number),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

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
                        onValueChange = {
                            endDate = it
                        },
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
                if (isTenant) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        ExposedDropdownMenuBoxExample(
                            sharedViewModel = sharedViewModel,
                            items = buildings,
                            selectedItem = selectedBuilding,
                            onItemSelected = { building ->
                                selectedBuilding = building
                            },
                            label = context.getString(R.string.building),
                            modifier = Modifier
                                .fillMaxWidth(1f),
                            itemLabel = { building -> building.name }
                        )
                    }
                }
                val filteredUnits = if (isTenant && selectedBuilding != null) {
                    units.filter { it.buildingId == selectedBuilding!!.buildingId }
                } else {
                    if (isTenant) {
                        emptyList()
                    } else {
                        units
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBoxExample(
                        sharedViewModel = sharedViewModel,
                        items = filteredUnits,
                        selectedItem = selectedUnit,
                        onItemSelected = { unit ->
                            selectedUnit = unit
                        },
                        label = context.getString(R.string.unit_number),
                        modifier = Modifier
                            .fillMaxWidth(1f),
                        itemLabel = { unit -> unit.unitNumber }
                    )
                }
            }
            if (showStartDatePicker) {
                PersianDatePickerDialogContent(
                    sharedViewModel = sharedViewModel,
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
                    sharedViewModel = sharedViewModel,
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
            val isFormValid = !emailError && !phoneError && !mobileError &&
                    email.isNotBlank() && phoneNumber.isNotBlank() && mobileNumber.isNotBlank()
            Button(
                onClick = {
                    periodConflictError = false
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
                    selectedUnit?.let { unit ->
                        if (selectedStatus == context.getString(R.string.active)) {
                            val hasConflict = Validation().isTenantPeriodConflicted(
                                unit.unitId, startDate, endDate, sharedViewModel
                            )
                            if (hasConflict) {
                                periodConflictError = true
                                return@Button
                            }
                        }
                        onAddTenant(newTenant, unit)
                        onDismiss()
                    }

                },
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(context.getColor(R.color.secondary_color))
//                ),
                enabled = isFormValid
            ) {
                if (periodConflictError) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.tenant_period_conflict),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                Text(
                    context.getString(R.string.insert),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
//                )
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
                UnitCard(
                    unit = unit,
                    sharedViewModel = sharedViewModel
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
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
//                )
            ) {
                Text(
                    context.getString(R.string.back),
                    modifier = Modifier.padding(2.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Button(
                onClick = onNext,
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
//                )
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
fun UnitCard(
    unit: Units,
    sharedViewModel: SharedViewModel
) {
    val context = LocalContext.current
    var showEditDialog by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = { showEditDialog = true }),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "${context.getString(R.string.unit_name)}: ${unit.unitNumber}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${context.getString(R.string.area)}:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = unit.area,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(8.dp))

                Text(
                    text = "${context.getString(R.string.number_of_room)}:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = unit.numberOfRooms,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(8.dp))

                Text(
                    text = "${context.getString(R.string.number_of_parking)}:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = unit.numberOfParking,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

        }
        if (showEditDialog) {
            val lifecycleOwner = LocalLifecycleOwner.current
            EditUnitDialog(
                unit = unit,
                onDismiss = { showEditDialog = false },
                onUpdateUnit = { updatedUnit ->
                    lifecycleOwner.lifecycleScope.launch {
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
                style = MaterialTheme.typography.bodyLarge,
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
    var numberOfWarhouse by remember { mutableStateOf("") }
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

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = numberOfWarhouse,
                    onValueChange = { numberOfWarhouse = it },
                    label = {
                        Text(
                            text = context.getString(R.string.number_of_warehouse),
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
                        numberOfParking = numberOfParking,
                        numberOfWarehouse = numberOfWarhouse
                    )
                    onAddUnit(newUnit)
                },
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(context.getColor(R.color.secondary_color))
//                )
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
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(context.getColor(R.color.secondary_color))
//                )
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
    var numberOfWarehouse by remember { mutableStateOf(unit.numberOfParking) }
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

                OutlinedTextField(
                    value = numberOfWarehouse,
                    onValueChange = { numberOfWarehouse = it },
                    label = { Text(context.getString(R.string.number_of_warehouse)) },
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
                        numberOfParking = numberOfParking,
                        numberOfWarehouse = numberOfWarehouse
                    )
                    onUpdateUnit(updatedUnit)
                    onDismiss()
                }
            ) {
                Text(
                    text = context.getString(R.string.update),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(context.getColor(R.color.secondary_color))
//                )
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
fun DebtItem(
    debts: Debts,
    sharedViewModel: SharedViewModel
) {
    val context = LocalContext.current
    val costState = sharedViewModel.getCost(debts.costId).collectAsState(initial = null)
    val cost = costState.value

    // State holds only digits (no commas)
    var amount by remember { mutableStateOf(debts.amount.toLong().toString()) }

// Visual transformation to show commas in UI
    val transformation = remember { NumberCommaTransformation() }

// Convert amount to words (optional)
    val amountInWords by remember(amount) {
        derivedStateOf {
            transformation.numberToWords(context, amount.toLongOrNull() ?: 0L)
        }
    }

    LaunchedEffect(debts.costId, debts.amount) {
        val currentCosts = sharedViewModel.costsList.value
        val index = currentCosts.indexOfFirst { it.costId == debts.costId }
        if (index != -1) {
            val updatedCost = currentCosts[index].copy(tempAmount = debts.amount)
            val updatedCostsList = currentCosts.toMutableList()
            updatedCostsList[index] = updatedCost
            sharedViewModel.costsList.value = updatedCostsList
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
                text = cost?.costName ?: "",
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
                    sharedViewModel = sharedViewModel,
                    items = sharedViewModel.periods,
                    selectedItem = cost?.period, // default value if empty
                    onItemSelected = { selectedPeriod ->
                        cost?.let { nonNullCost ->
                            sharedViewModel.updateCostPeriod(nonNullCost, selectedPeriod)
                        }
                    },
                    label = context.getString(R.string.period),
                    modifier = Modifier.weight(0.4f),
                    itemLabel = { it.getDisplayName(context) }
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
                                cost?.let {
                                    sharedViewModel.updateCostAmount(
                                        cost,
                                        filtered.persianToEnglishDigits().toDoubleOrNull() ?: 0.0
                                    )
                                }
                            }
                        },
                        label = { Text(context.getString(R.string.amount)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = transformation,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = " $amountInWords ${context.getString(R.string.toman)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(context.getColor(R.color.grey)),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
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

@Composable
fun EditOwnerDialog(
    owner: Owners,
    units: List<Units>,
    dangSums: Map<Long, Double>,
//    ownerUnits: List<OwnersUnitsCrossRef>, // <-- Add this parameter!
    sharedViewModel: SharedViewModel,
    onDismiss: () -> Unit,
    onSave: (Owners, List<OwnersUnitsCrossRef>) -> Unit
) {
    val context = LocalContext.current
    var firstName by remember { mutableStateOf(owner.firstName) }
    var phone by remember { mutableStateOf(owner.phoneNumber) }
    var lastName by remember { mutableStateOf(owner.lastName) }
    var address by remember { mutableStateOf(owner.address) }
    var email by remember { mutableStateOf(owner.email) }
    var mobileNumber by remember { mutableStateOf(owner.mobileNumber) }
    var emailError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var mobileError by remember { mutableStateOf(false) }
    val ownerUnitsState =
        sharedViewModel.getOwnerUnitsCrossRefs(owner.ownerId).collectAsState(initial = emptyList())
    val ownerUnits = ownerUnitsState.value

    val localDangMap = remember { mutableStateMapOf<Long, Double>() }

// Update localDangMap whenever ownerUnits changes
    LaunchedEffect(ownerUnits) {
        localDangMap.clear()
        units.forEach { unit ->
            val dang = ownerUnits.find { it.unitId == unit.unitId }?.dang ?: 0.0
            localDangMap[unit.unitId] = dang
        }
    }



    fun updateLocalDang(unitId: Long, newDang: Double) {
        val clamped = newDang.coerceIn(0.0, 6.0)
        val rounded = String.format("%.1f", clamped).toDouble()
        localDangMap[unitId] = rounded
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
                    onValueChange = { firstName = it },
                    label = {
                        Text(
                            context.getString(R.string.first_name),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = {
                        Text(
                            context.getString(R.string.last_name),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = {
                        Text(
                            context.getString(R.string.address),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = {
                        Text(
                            context.getString(R.string.email),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    isError = emailError,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
                if (emailError) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = context.getString(R.string.invalid_email),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
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
                    isError = phoneError,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
                if (phoneError) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = context.getString(R.string.invalid_phone_number),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = { mobileNumber = it },
                    label = {
                        Text(
                            context.getString(R.string.mobile_number),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    isError = mobileError,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
                if (mobileError) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = context.getString(R.string.invalid_mobile_number),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))

                Text(
                    text = context.getString(R.string.units),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    stickyHeader {
                        HeaderRow()  // Create this composable
                    }
                    items(units) { unit ->
                        val dang = localDangMap[unit.unitId] ?: 0.0
                        val otherOwnersDang = dangSums[unit.unitId] ?: 0.0
                        val maxAllowed = (6.0 - otherOwnersDang).coerceAtLeast(0.0)
                        UnitDangRow(
                            unit = unit,
                            dang = dang,
                            maxAllowed = maxAllowed,
                            onDangChange = { newDang ->
                                updateLocalDang(unit.unitId, newDang)
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                        Divider()
                    }
                }

            }
        },
        confirmButton = {
            val isFormValid = !emailError && !phoneError && !mobileError &&
                    email.isNotBlank() && phone.isNotBlank() && mobileNumber.isNotBlank()
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
                    val updatedCrossRefs = localDangMap.map { (unitId, dang) ->
                        OwnersUnitsCrossRef(
                            ownerId = owner.ownerId, // use real ownerId for editing
                            unitId = unitId,
                            dang = dang
                        )
                    }
                    onSave(updatedOwner, updatedCrossRefs)
                },
                enabled = isFormValid,
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(context.getColor(R.color.secondary_color))
//                )
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
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(context.getColor(R.color.secondary_color))
//                )
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
fun EditTenantDialog(
    tenant: Tenants,
    units: List<Units>,
    sharedViewModel: SharedViewModel,
    onDismiss: () -> Unit,
    onSave: (Tenants, Units?) -> Unit,
    isOwner: Boolean
) {
    val context = LocalContext.current

    // Fields (pre-filled with tenant's current values)
    var firstName by remember { mutableStateOf(tenant.firstName) }
    var lastName by remember { mutableStateOf(tenant.lastName) }
    var email by remember { mutableStateOf(tenant.email) }
    var phoneNumber by remember { mutableStateOf(tenant.phoneNumber) }
    var mobileNumber by remember { mutableStateOf(tenant.mobileNumber) }
    var startDate by remember { mutableStateOf(tenant.startDate) }
    var endDate by remember { mutableStateOf(tenant.endDate) }
    var numberOfTenants by remember { mutableStateOf(tenant.numberOfTenants ?: "") }
    var selectedStatus by remember { mutableStateOf(tenant.status) }
//    var selectedUnit by remember { mutableStateOf(units.find { it.unitId == tenant.unitId }) }
    var selectedBuilding by remember { mutableStateOf<Buildings?>(null) }
    // Fetch the current unit for this tenant from DB
    val currentUnit by sharedViewModel.getUnitForTenant(tenant.tenantId)
        .collectAsState(initial = null)

    // For selection in the dialog (initialize with currentUnit)
    var selectedUnit by remember(currentUnit) { mutableStateOf(currentUnit) }

    // Error states
    var emailError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var mobileError by remember { mutableStateOf(false) }
    var periodConflictError by remember { mutableStateOf(false) }

    // Date pickers
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val buildings by sharedViewModel.getAllBuildings().collectAsState(initial = emptyList())

    // Dismiss both date pickers
    val dismissDatePicker: () -> Unit = {
        showStartDatePicker = false
        showEndDatePicker = false
    }

    // Auto-set end date when start date changes
    LaunchedEffect(startDate) {
        if (startDate.isNotEmpty()) {
            endDate = sharedViewModel.getNextYearSameDaySafe(sharedViewModel.parsePersianDate(startDate)).persianShortDate
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                context.getString(R.string.edit_tenant),
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
                        label = { Text(context.getString(R.string.first_name), style = MaterialTheme.typography.bodyLarge) },
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text(context.getString(R.string.last_name), style = MaterialTheme.typography.bodyLarge) },
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = !Validation().isValidEmail(it)
                        },
                        label = { Text(context.getString(R.string.email), style = MaterialTheme.typography.bodyLarge) },
                        isError = emailError,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (emailError) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = context.getString(R.string.invalid_email),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = {
                            phoneNumber = it
                            phoneError = !Validation().isValidPhone(it)
                        },
                        label = { Text(context.getString(R.string.phone_number), style = MaterialTheme.typography.bodyLarge) },
                        isError = phoneError,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (phoneError) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = context.getString(R.string.invalid_phone_number),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = {
                            mobileNumber = it
                            mobileError = !Validation().isValidIranMobile(it)
                        },
                        label = { Text(context.getString(R.string.mobile_number), style = MaterialTheme.typography.bodyLarge) },
                        isError = mobileError,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (mobileError) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = context.getString(R.string.invalid_mobile_number),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
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
                        textStyle = MaterialTheme.typography.bodyLarge,
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
                // Building selection (optional, if your edit should allow changing building)
                if(isOwner) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        ExposedDropdownMenuBoxExample(
                            sharedViewModel = sharedViewModel,
                            items = buildings,
                            selectedItem = selectedBuilding,
                            onItemSelected = { building -> selectedBuilding = building },
                            label = context.getString(R.string.building),
                            modifier = Modifier.fillMaxWidth(1f),
                            itemLabel = { building -> building.name }
                        )
                    }
                }
                // Filter units by selected building if needed
                val filteredUnits = if (selectedBuilding != null) {
                    units.filter { it.buildingId == selectedBuilding!!.buildingId }
                } else {
                    units
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBoxExample(
                        sharedViewModel = sharedViewModel,
                        items = filteredUnits,
                        selectedItem = selectedUnit,
                        onItemSelected = { unit -> selectedUnit = unit },
                        label = context.getString(R.string.unit_number),
                        modifier = Modifier.fillMaxWidth(1f),
                        itemLabel = { unit -> unit.unitNumber }
                    )
                }
            }
            if (showStartDatePicker) {
                PersianDatePickerDialogContent(
                    sharedViewModel = sharedViewModel,
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
                    sharedViewModel = sharedViewModel,
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
            val isFormValid = !emailError && !phoneError && !mobileError &&
                    email.isNotBlank() && phoneNumber.isNotBlank() && mobileNumber.isNotBlank()
            Button(
                onClick = {
                    periodConflictError = false
                    val updatedTenant = tenant.copy(
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        phoneNumber = phoneNumber,
                        mobileNumber = mobileNumber,
                        startDate = startDate,
                        endDate = endDate,
                        status = selectedStatus,
                        numberOfTenants = numberOfTenants
                    )
                    selectedUnit?.let { unit ->
                        if (selectedStatus == context.getString(R.string.active)) {
                            val hasConflict = Validation().isTenantPeriodConflicted(
                                unit.unitId, startDate, endDate, sharedViewModel
                            )
                            if (hasConflict) {
                                periodConflictError = true
                                return@Button
                            }
                        }
                        onSave(updatedTenant, unit)
                        onDismiss()
                    }
                },
                enabled = isFormValid
            ) {
                if (periodConflictError) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.tenant_period_conflict),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Text(context.getString(R.string.edit), style = MaterialTheme.typography.bodyLarge)
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(
                    text = context.getString(R.string.cancel),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    )
}



@Composable
fun NumberInputWithArrows(
    value: Double,
    onValueChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
    step: Double = 0.1,
    min: Double = 0.0,
    max: Double = 6.0
) {
    var textValue by remember(value) { mutableStateOf(if (value == 0.0) "" else value.toString()) }

    var isError by remember { mutableStateOf(false) }
    var context = LocalContext.current
    var errorMessage = context.getString(R.string.invalid_dang)
    // Sync textValue with external value changes (e.g. reset)
    LaunchedEffect(value) {
        val newText = if (value == 0.0) "" else value.toString()
        if (newText != textValue) {
            textValue = newText
            isError = false
        }
    }
    OutlinedTextField(
        value = textValue,
        onValueChange = { input ->
            // Allow only digits, dot, and empty string
            if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d*\$"))) {
                textValue = input

                val parsed = input.toDoubleOrNull()
                if (parsed != null) {
                    if (parsed in min..max) {
                        isError = false
                        onValueChange(parsed)
                    } else {
                        isError = true
                    }
                } else if (input.isEmpty()) {
                    isError = false
                    onValueChange(0.0)
                } else {
                    isError = true
                }
            }
        },
        modifier = modifier,
        singleLine = true,
        isError = isError,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        trailingIcon = {
            IconButton(
                onClick = {
                    val newValue = (value + step).coerceAtMost(max)
                    onValueChange(newValue)
                },
                enabled = value < max
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increase")
            }
        },
        leadingIcon = {
            IconButton(
                onClick = {
                    val newValue = (value - step).coerceAtLeast(min)
                    onValueChange(newValue)
                },
                enabled = value > min
            ) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease")
            }
        },
        textStyle = MaterialTheme.typography.bodyLarge
    )
    if (isError) {
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
        )
    }
}

// Add this composable function
@Composable
fun HeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Unit Header
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = LocalContext.current.getString(R.string.units),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Dang Header
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = LocalContext.current.getString(R.string.dang),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun UnitDangRow(
    unit: Units,
    dang: Double,
    maxAllowed: Double,
    onDangChange: (Double) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Unit Number with border and smaller width
        Box(
            modifier = Modifier
                .weight(1f)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(vertical = 17.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = unit.unitNumber,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        NumberInputWithArrows(
            value = dang,
            onValueChange = onDangChange,
            max = maxAllowed, // Pass maxAllowed dynamically!
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun AddCityComplexDialog(
    onDismiss: () -> Unit,
    onInsert: (name: String, address: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = context.getString(R.string.add_new_city_complex), style = MaterialTheme.typography.bodyLarge) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(text = context.getString(R.string.city_complex_name), style = MaterialTheme.typography.bodyLarge) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(text = context.getString(R.string.address), style = MaterialTheme.typography.bodyLarge) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) {
                    onInsert(name, if (address.isBlank()) null else address)
                }
            }) {
                Text(text = context.getString(R.string.insert), style = MaterialTheme.typography.bodyLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = context.getString(R.string.cancel), style = MaterialTheme.typography.bodyLarge)
            }
        }
    )
}



fun Double.roundToOneDecimal(): Double {
    val factor = 10.0
    return round(this * factor) / factor
}

