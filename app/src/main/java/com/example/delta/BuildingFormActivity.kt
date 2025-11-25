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
import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.Surface
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
import com.example.delta.data.entity.CityComplexes
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Debts
import com.example.delta.data.entity.Owners
import com.example.delta.data.entity.OwnersUnitsCrossRef
import com.example.delta.data.entity.Tenants
import com.example.delta.data.entity.Units
import com.example.delta.enums.BuildingProfileFields
import com.example.delta.enums.CalculateMethod
import com.example.delta.enums.FundType
import com.example.delta.enums.PaymentLevel
import com.example.delta.enums.Period
import com.example.delta.enums.PermissionLevel
import com.example.delta.enums.Responsible
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.round
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.window.DialogProperties
import com.example.delta.volley.Owner.OwnerWithUnitsDto
import androidx.compose.material3.OutlinedButton
import com.example.delta.volley.Cost


class BuildingFormActivity : ComponentActivity() {
    private val tenantViewModel: TenantViewModel by viewModels()
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
            AppTheme(useDarkTheme = sharedViewModel.isDarkModeEnabled) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {

                    val context = LocalContext.current

                    LaunchedEffect(Unit) {
                        sharedViewModel.loadOverviewData(
                            context = context
                        )
                    }

                    val remoteBuildingTypes by sharedViewModel.buildingTypes
                        .collectAsState(initial = emptyList())
                    val remoteBuildingUsages by sharedViewModel.buildingUsages
                        .collectAsState(initial = emptyList())
                    val remoteCityComplexes by sharedViewModel.cityComplexes
                        .collectAsState(initial = emptyList())

                    BuildingFormScreen(
                        unitsViewModel = unitsViewModel,
                        tenantViewModel = tenantViewModel,
                        buildingTypeViewModel = buildingTypeViewModel,
                        buildingUsageViewModel = buildingUsageViewModel,
                        buildingTypes = remoteBuildingTypes,
                        buildingUsages = remoteBuildingUsages,
                        cityComplexes = remoteCityComplexes,
                        sharedViewModel = sharedViewModel
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("onResume", true.toString())
    }
}
@Composable
fun BuildingFormScreen(
    unitsViewModel: UnitsViewModel,
    tenantViewModel: TenantViewModel,
    buildingTypeViewModel: BuildingTypeViewModel,
    buildingUsageViewModel: BuildingUsageViewModel,
    buildingTypes: List<BuildingTypes>,
    buildingUsages: List<BuildingUsages>,
    cityComplexes: List<CityComplexes>,
    sharedViewModel: SharedViewModel,
) {
    var currentPage by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

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
                cityComplexes = cityComplexes,
                onSave = {
                    sharedViewModel.isLoading = true
                    lifecycleOwner.lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            val tenantsUnitsCrossRef =
                                tenantViewModel.getAllTenantUnitRelations()
                            sharedViewModel.insertBuildingToServer(
                                context = context,
                                tenantsUnitsCrossRef = tenantsUnitsCrossRef,
                                onSuccess = { message ->
                                    Log.d("SaveSuccess", "saving building on Server: $message")
                                    sharedViewModel.resetState()
                                    val intent = Intent(context, HomePageActivity::class.java)
                                    sharedViewModel.isLoading = false
                                    context.startActivity(intent)
                                },
                                onError = { e ->
                                    Log.e(
                                        "SaveError",
                                        "Error saving building on Server: ${e.message}"
                                    )
                                    sharedViewModel.resetState()
                                    val intent = Intent(context, HomePageActivity::class.java)
                                    sharedViewModel.isLoading = false
                                    context.startActivity(intent)
                                }
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
    val costApi = remember { Cost() }

    val costs by sharedViewModel.costsList.collectAsState()
    val addNewLabel = context.getString(R.string.addNew)

    var selectedCostNames by remember { mutableStateOf<List<String>>(emptyList()) }

    val chipItems = costs + Costs(
        costName = addNewLabel,
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
            if (newSelectionStrings.contains(addNewLabel)) {
                showChargeCostDialog = true
                selectedCostNames =
                    newSelectionStrings.filter { it != addNewLabel }
            } else {
                val newlySelected = newSelectionStrings - selectedCostNames
                val newlyDeselected = selectedCostNames - newSelectionStrings

                selectedCostNames = newSelectionStrings

                val updatedCosts: List<Costs> = costs.map { cost ->
                    when {
                        cost.costName in newlySelected ->
                            cost.copy(tempAmount = 1.0)

                        cost.costName in newlyDeselected ->
                            cost.copy(tempAmount = 0.0)

                        else -> cost
                    }
                }

                // persist in ViewModel
                sharedViewModel.updateCosts(updatedCosts)
            }
        },
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

                    costApi.createGlobalCost(
                        context = context,
                        cost = newCost,
                        onSuccess = { created ->
                            val updated = costs + created
                            sharedViewModel.updateCosts(updated)
                            showChargeCostDialog = false
                        },
                        onError = {
                            showChargeCostDialog = false
                        }
                    )
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildingInfoPage(
    sharedViewModel: SharedViewModel,
    buildingTypeViewModel: BuildingTypeViewModel,
    buildingUsageViewModel: BuildingUsageViewModel,
    unitsViewModel: UnitsViewModel,
    buildingTypes: List<BuildingTypes>,
    buildingUsages: List<BuildingUsages>,
    cityComplexes: List<CityComplexes>,
    onSave: () -> Unit
) {
    var showBuildingTypeDialog by remember { mutableStateOf(false) }
    var showBuildingUsageDialog by remember { mutableStateOf(false) }
    var showAddCityComplexDialog by remember { mutableStateOf(false) }
    var showSameAreaUnitDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val isValid = Validation().isBuildingInfoValid(sharedViewModel)
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    fun scrollToBottom() {
        scope.launch {
            val lastIndex = listState.layoutInfo.totalItemsCount - 1
            if (lastIndex >= 0) {
                listState.animateScrollToItem(lastIndex)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
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

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = sharedViewModel.name,
                    onValueChange = {
                        sharedViewModel.name = it
                    },
                    label = { RequiredLabel(context.getString(R.string.building_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                ExposedDropdownMenuBoxExample(
                    sharedViewModel = sharedViewModel,
                    items = buildingTypes + BuildingTypes(
                        0,
                        context.getString(R.string.addNew)
                    ),
                    selectedItem = sharedViewModel.selectedBuildingTypes,
                    onItemSelected = {
                        if (it.buildingTypeName == context.getString(R.string.addNew)) {
                            showBuildingTypeDialog = true
                        } else {
                            sharedViewModel.selectedBuildingTypes = it
                        }
                    },
                    label = context.getString(R.string.building_type),
                    modifier = Modifier.fillMaxWidth(),
                    itemLabel = { it.buildingTypeName }
                )
            }

            val buildingTypeName = context.getString(R.string.city_complex)
            if (sharedViewModel.selectedBuildingTypes?.buildingTypeName == buildingTypeName) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    ExposedDropdownMenuBoxExample(
                        sharedViewModel = sharedViewModel,
                        items = cityComplexes + CityComplexes(
                            complexId = 0L,
                            name = context.getString(R.string.addNew),
                            address = null
                        ),
                        selectedItem = sharedViewModel.selectedCityComplexes,
                        onItemSelected = {
                            if (it.name == context.getString(R.string.addNew)) {
                                showAddCityComplexDialog = true
                            } else {
                                sharedViewModel.selectedCityComplexes = it
                            }
                        },
                        label = context.getString(R.string.city_complex),
                        modifier = Modifier.fillMaxWidth(),
                        itemLabel = { it.name }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                ExposedDropdownMenuBoxExample(
                    sharedViewModel = sharedViewModel,
                    items = buildingUsages + BuildingUsages(
                        0,
                        context.getString(R.string.addNew)
                    ),
                    selectedItem = sharedViewModel.selectedBuildingUsages,
                    onItemSelected = {
                        if (it.buildingUsageName == context.getString(R.string.addNew)) {
                            showBuildingUsageDialog = true
                        } else {
                            sharedViewModel.selectedBuildingUsages = it
                        }
                    },
                    label = context.getString(R.string.building_usage),
                    modifier = Modifier.fillMaxWidth(),
                    itemLabel = { it.buildingUsageName }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                ProvinceStateSelector(sharedViewModel = sharedViewModel)
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                OutlinedTextField(
                    value = sharedViewModel.street,
                    onValueChange = {
                        sharedViewModel.street = it
                        scrollToBottom()
                    },
                    label = { RequiredLabel(context.getString(R.string.street)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                val postCodeError =
                    sharedViewModel.postCode.isNotBlank() &&
                            !Validation().isValidPostalCode(sharedViewModel.postCode)

                Column {
                    OutlinedTextField(
                        value = sharedViewModel.postCode,
                        onValueChange = {
                            sharedViewModel.postCode = it
                            scrollToBottom()
                        },
                        label = { RequiredLabel(context.getString(R.string.post_code)) },
                        isError = postCodeError,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (postCodeError) {
                        Text(
                            text = context.getString(R.string.invalid_post_code),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = sharedViewModel.sameArea,
                        onCheckedChange = { checked ->
                            sharedViewModel.sameArea = checked
                            if (checked) {
                                showSameAreaUnitDialog = true
                            } else {
                                sharedViewModel.numberOfUnits = ""
                                sharedViewModel.unitArea = ""
                                sharedViewModel.unitsAdded = false
                            }
                            scrollToBottom()
                        }
                    )
                    Text(
                        text = context.getString(R.string.all_units_same_area),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            item {
                ChargesChipGroup(sharedViewModel)
            }

            item {
                UploadFile(
                    sharedViewModel = sharedViewModel,
                    context = context,
                    onFileSaved = { uploaded ->
                        sharedViewModel.addFileList(uploaded)
                    }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { onSave() },
                enabled = isValid
            ) {
                Text(
                    context.getString(R.string.insert),
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
                    sharedViewModel.insertCityComplexRemote(
                        context = context,
                        name = newName,
                        address = newAddress
                    ) { created ->
                        if (created != null) {
                            sharedViewModel.selectedCityComplexes = created
                        }
                        showAddCityComplexDialog = false
                    }
                }
            )
        }

        if (showBuildingTypeDialog) {
            AddItemDialog(
                sharedViewModel = sharedViewModel,
                onDismiss = { showBuildingTypeDialog = false },
                onInsert = { newItem ->
                    sharedViewModel.insertBuildingTypeRemote(
                        context = context,
                        name = newItem
                    ) { created ->
                        if (created != null) {
                            sharedViewModel.selectedBuildingTypes = created
                        }
                        showBuildingTypeDialog = false
                    }
                }
            )
        }

        if (showBuildingUsageDialog) {
            AddItemDialog(
                sharedViewModel = sharedViewModel,
                onDismiss = { showBuildingUsageDialog = false },
                onInsert = { name ->
                    sharedViewModel.insertBuildingUsageRemote(
                        context = context,
                        name = name
                    ) { created ->
                        if (created != null) {
                            sharedViewModel.selectedBuildingUsages = created
                        }
                        showBuildingUsageDialog = false
                    }
                }
            )
        }

        if (showSameAreaUnitDialog) {
            SameAreaUnitDialog(
                onDismiss = {
                    showSameAreaUnitDialog = false
                    if (!sharedViewModel.unitsAdded) {
                        sharedViewModel.sameArea = false
                    }
                },
                onAddUnits = { count, area, rooms, parking, warehouse, postCode ->
                    lifecycleOwner.lifecycleScope.launch {
                        for (i in 1..count) {
                            val newUnit = Units(
                                unitNumber = i.toString(),
                                area = area,
                                numberOfRooms = rooms,
                                numberOfParking = parking,
                                numberOfWarehouse = warehouse,
                                postCode = postCode
                            )
                            try {
                                val unitId = unitsViewModel.insertUnit(newUnit)
                                sharedViewModel.unitsList.add(newUnit.copy(unitId = unitId))
                            } catch (e: Exception) {
                                Log.e("InsertUnitError", "Failed to insert unit: ${e.message}")
                            }
                        }
                        sharedViewModel.numberOfUnits = count.toString()
                        sharedViewModel.unitArea = area
                        sharedViewModel.unitsAdded = true
                    }
                    showSameAreaUnitDialog = false
                }
            )
        }
    }
}


@SuppressLint("SuspiciousIndentation")
@Composable
fun SameAreaUnitDialog(
    onDismiss: () -> Unit,
    onAddUnits: (count: Int, area: String, numberOfRooms: String, numberOfParking: String, numberOfWarehouse: String, postCode: String) -> Unit
) {
    var unitCount by remember { mutableStateOf("1") }
    var area by remember { mutableStateOf("") }
    var numberOfRooms by remember { mutableStateOf("") }
    var numberOfParking by remember { mutableStateOf("") }
    var numberOfWarehouse by remember { mutableStateOf("") }
    var postCode by remember { mutableStateOf("") }

    val context = LocalContext.current
    val validation = remember { Validation() }

    Dialog(onDismissRequest = {}) {
        BackHandler(onBack = onDismiss)

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = context.getString(R.string.add_new_unit),
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = unitCount,
                    onValueChange = { unitCount = it },
                    label = { RequiredLabel(context.getString(R.string.number_of_units)) },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { RequiredLabel(context.getString(R.string.area)) },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = numberOfRooms,
                    onValueChange = { numberOfRooms = it },
                    label = { RequiredLabel(context.getString(R.string.number_of_rooms)) },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = numberOfParking,
                    onValueChange = { numberOfParking = it },
                    label = { RequiredLabel(context.getString(R.string.number_of_parking)) },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = numberOfWarehouse,
                    onValueChange = { numberOfWarehouse = it },
                    label = { RequiredLabel(context.getString(R.string.number_of_warehouse)) },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                val postCodeError =
                    postCode.isNotBlank() && !validation.isValidPostalCode(postCode)

                OutlinedTextField(
                    value = postCode,
                    onValueChange = { postCode = it },
                    label = {
                        Text(
                            text = context.getString(R.string.post_code),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )


                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = context.getString(R.string.cancel),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val countInt = unitCount.toIntOrNull()
                            if (countInt == null || countInt <= 0) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.invalid_number_of_units),
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }
                            if (area.isBlank() ||
                                numberOfRooms.isBlank() ||
                                numberOfParking.isBlank() ||
                                numberOfWarehouse.isBlank()
                            ) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.fill_required_fields),
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }
                            if (postCode.isNotEmpty()){
                                if (!validation.isValidPostalCode(postCode)) {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.invalid_post_code),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }
                        }

                            onAddUnits(
                                countInt,
                                area,
                                numberOfRooms,
                                numberOfParking,
                                numberOfWarehouse,
                                postCode
                            )
                        }
                    ) {
                        Text(
                            text = context.getString(R.string.insert),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun OwnerItem(
    owner: OwnerWithUnitsDto,
    buildingId: Long,
    modifier: Modifier = Modifier,
    sharedViewModel: SharedViewModel,
    onDelete: (Owners) -> Unit,
    activity: Activity?
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    val ownerEntity = owner.owner
    val unitsForOwner = owner.units

    val dangSumsMap = remember(unitsForOwner) {
        unitsForOwner
            .groupBy { it.unit.unitId }
            .mapValues { (_, list) -> list.sumOf { it.dang } }
    }

    val userId = Preference().getUserId(context = context)
    val roleId = Preference().getRoleId(context = context)
    val mobile = Preference().getUserMobile(context = context)

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
//                        if (permissionLevelAllOwnersTab == PermissionLevel.FULL ||
//                            permissionLevelAllOwnersTab == PermissionLevel.WRITE
//                        ) {
                            val intent = Intent(context, OwnerDetailsActivity::class.java)
                            intent.putExtra("ownerId", ownerEntity.ownerId)
                            context.startActivity(intent)
//                        } else if (
//                            ownerEntity.mobileNumber == mobile &&
//                            (permissionLevelOwnersTab == PermissionLevel.FULL ||
//                                    permissionLevelOwnersTab == PermissionLevel.WRITE)
//                        ) {
//                            val intent = Intent(context, OwnerDetailsActivity::class.java)
//                            intent.putExtra("ownerId", ownerEntity.ownerId)
//                            context.startActivity(intent)
//                        }
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
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Owner Avatar",
                modifier = Modifier
                    .size(48.dp)
                    .padding(end = 16.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Column(modifier = Modifier.weight(1f)) {

                Row {
                    if (activity?.javaClass?.name?.endsWith("BuildingProfileActivity") == true) {
//                        val isManager = ownerEntity.isManager
//                        if (isManager) {
//                            Spacer(Modifier.width(16.dp))
//                            Box(
//                                modifier = Modifier
//                                    .background(
//                                        color = MaterialTheme.colorScheme.secondary,
//                                        shape = RoundedCornerShape(8.dp)
//                                    )
//                                    .padding(horizontal = 8.dp, vertical = 4.dp)
//                            ) {
//                                Text(
//                                    text = context.getString(R.string.manager),
//                                    style = MaterialTheme.typography.bodyMedium,
//                                    color = MaterialTheme.colorScheme.onSecondary
//                                )
//                            }
//                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${context.getString(R.string.first_name)}: ${ownerEntity.firstName}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = "${context.getString(R.string.last_name)}: ${ownerEntity.lastName}",
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
                    unitsForOwner.forEach { unitWithDang ->
                        Row {
                            Text(
                                text =
                                    "${context.getString(R.string.unit_number)}: ${unitWithDang.unit.unitNumber}, " +
                                            "${context.getString(R.string.area)}: ${unitWithDang.unit.area}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = "${context.getString(R.string.dang)}: ${unitWithDang.dang}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

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
                            onDelete(ownerEntity)
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
                owner = ownerEntity,
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
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MutableCollectionMutableState", "StringFormatInvalid")
@Composable
fun OwnerDialog(
    units: List<Units>,
    dangSums: Map<Long, Double>,
    onDismiss: () -> Unit,
    onAddOwner: (Owners, List<OwnersUnitsCrossRef>, Boolean, Buildings?) -> Unit,
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var isManager by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var mobileError by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val selectedUnitsList = remember { mutableStateListOf<OwnersUnitsCrossRef>() }
    var showUnitsSheet by remember { mutableStateOf(false) }
    var unitsError by remember { mutableStateOf(false) }

    val selectableUnits = remember(units, dangSums) {
        units.filter { u ->
            val usedDang = dangSums[u.unitId] ?: 0.0
            usedDang < 6.0
        }
    }

    val noUnits = units.isEmpty()
    val controlsEnabled = !noUnits

    val allSelectedDangValid by remember(selectedUnitsList, dangSums) {
        mutableStateOf(
            selectedUnitsList.all { sel ->
                val usedDang = dangSums[sel.unitId] ?: 0.0
                val maxAllowed = (6.0 - usedDang).coerceAtLeast(0.0)
                sel.dang > 0.0 && sel.dang <= maxAllowed
            }
        )
    }

    val isFormValid by remember(
        firstName, lastName, address,
        email, mobileNumber,
        emailError, phoneError, mobileError,
        selectedUnitsList, allSelectedDangValid, units
    ) {
        mutableStateOf(
            firstName.isNotBlank() &&
                    lastName.isNotBlank() &&
                    address.isNotBlank() &&
                    email.isNotBlank() &&
                    mobileNumber.isNotBlank() &&
                    !emailError &&
                    !mobileError &&
                    selectedUnitsList.isNotEmpty() &&
                    allSelectedDangValid &&
                    units.isNotEmpty()
        )
    }

    val selectedUnitsText: String = remember(selectedUnitsList.toList()) {
        if (selectedUnitsList.isEmpty()) ""
        else {
            val count = selectedUnitsList.size
            val totalDang = selectedUnitsList.sumOf { it.dang }
            context.getString(R.string.selected_units_count, count, totalDang)
        }
    }

    AlertDialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        title = {
            Text(
                text = context.getString(R.string.add_new_owner),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        text = {
            Column {
                if (noUnits) {
                    Text(
                        text = context.getString(R.string.first_compete_units),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                OutlinedTextField(
                    value = firstName,
                    onValueChange = { if (controlsEnabled) firstName = it },
                    label = { RequiredLabel(context.getString(R.string.first_name)) },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = controlsEnabled
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { if (controlsEnabled) lastName = it },
                    label = { RequiredLabel(context.getString(R.string.last_name)) },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = controlsEnabled
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { if (controlsEnabled) address = it },
                    label = { RequiredLabel(context.getString(R.string.address)) },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = controlsEnabled
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        if (!controlsEnabled) return@OutlinedTextField
                        email = it
                        emailError = !Validation().isValidEmail(it)
                    },
                    label = { RequiredLabel(context.getString(R.string.email)) },
                    isError = emailError,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = controlsEnabled
                )
                if (emailError) {
                    Text(
                        text = context.getString(R.string.invalid_email),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = {
                        if (!controlsEnabled) return@OutlinedTextField
                        phoneNumber = it
                        phoneError = it.isNotBlank() && !Validation().isValidPhone(it)
                    },
                    label = {
                        Text(
                            text = context.getString(R.string.phone_number),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    isError = phoneError,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = controlsEnabled
                )
                if (phoneError) {
                    Text(
                        text = context.getString(R.string.invalid_phone_number),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = {
                        if (!controlsEnabled) return@OutlinedTextField
                        mobileNumber = it
                        mobileError = !Validation().isValidIranMobile(it)
                    },
                    label = { RequiredLabel(context.getString(R.string.mobile_number)) },
                    isError = mobileError,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = controlsEnabled
                )
                if (mobileError) {
                    Text(
                        text = context.getString(R.string.invalid_mobile_number),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isManager,
                        onCheckedChange = { if (controlsEnabled) isManager = it },
                        enabled = controlsEnabled
                    )
                    Text(
                        text = context.getString(R.string.manager_teams),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(Modifier.height(8.dp))

                Column {
                    RequiredLabel(context.getString(R.string.units))
                    Spacer(Modifier.height(4.dp))

                    OutlinedButton(
                        onClick = {
                            if (!controlsEnabled) return@OutlinedButton
                            showUnitsSheet = true
                            unitsError = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = controlsEnabled
                    ) {
                        Text(
                            text = if (selectedUnitsText.isBlank()) {
                                context.getString(R.string.select_units_and_dang)
                            } else {
                                selectedUnitsText
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                if (unitsError && selectedUnitsList.isEmpty()) {
                    Text(
                        text = context.getString(R.string.select_at_least_one_unit),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedUnitsList.isEmpty()) {
                        unitsError = true
                        return@Button
                    }
                    val newOwner = Owners(
                        firstName = firstName,
                        lastName = lastName,
                        address = address,
                        email = email,
                        phoneNumber = phoneNumber,
                        mobileNumber = mobileNumber,
                        birthday = ""
                    )
                    Log.d("selectedUnitsList", selectedUnitsList.toString())
                    onAddOwner(newOwner, selectedUnitsList.toList(), isManager, null)
                },
                enabled = isFormValid
            ) {
                Text(
                    text = context.getString(R.string.insert),
                    style = MaterialTheme.typography.bodyLarge
                )
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

    if (showUnitsSheet && !noUnits) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { showUnitsSheet = false },
            sheetState = sheetState
        ) {
            val scrollState = rememberLazyListState()

            Text(
                text = context.getString(R.string.select_units_and_dang),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                state = scrollState
            ) {
                items(selectableUnits) { unit ->
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
                            Text(
                                text = "${context.getString(R.string.area)}: ${unit.area}",
                                style = MaterialTheme.typography.bodyLarge
                            )

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
                    Divider()
                }
            }

            Button(
                onClick = { showUnitsSheet = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = context.getString(R.string.insert),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
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
    val userMobile = Preference().getUserMobile(context = context)

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
                        val canOpenAll =
                            permissionLevelAllTenantsTab == PermissionLevel.FULL ||
                                    permissionLevelAllTenantsTab == PermissionLevel.WRITE

                        val isCurrentTenant = tenants.mobileNumber == userMobile
                        val canOpenSelf =
                            isCurrentTenant &&
                                    (permissionLevelTenantsTab == PermissionLevel.FULL ||
                                            permissionLevelTenantsTab == PermissionLevel.WRITE)

//                        if (canOpenAll || canOpenSelf) {
                            onClick()
//                        } else {
//                            Toast
//                                .makeText(
//                                    context,
//                                    context.getString(R.string.auth_cancel),
//                                    Toast.LENGTH_LONG
//                                )
//                                .show()
//                        }
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
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Tenant Avatar",
                modifier = Modifier
                    .size(48.dp)
                    .padding(end = 16.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth()
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
    var endDate by remember { mutableStateOf("") }
    var numberOfTenants by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf(context.getString(R.string.active)) }
    var selectedUnit by remember { mutableStateOf<Units?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var mobileError by remember { mutableStateOf(false) }
    var periodConflictError by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val noOwnerUnits = units.isEmpty()
    val controlsEnabled = !noOwnerUnits

    val dismissDatePicker: () -> Unit = {
        showStartDatePicker = false
        showEndDatePicker = false
    }

    LaunchedEffect(startDate) {
        if (startDate.isNotEmpty()) {
            endDate = sharedViewModel
                .getNextYearSameDaySafe(sharedViewModel.parsePersianDate(startDate))
                .persianShortDate
        }
    }

    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(
                context.getString(R.string.add_new_tenant),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        text = {
            LazyColumn(state = listState) {
                item {
                    if (noOwnerUnits) {
                        Text(
                            text = context.getString(R.string.no_units_with_owner),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
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
                        onValueChange = { if (controlsEnabled) firstName = it },
                        label = { RequiredLabel(context.getString(R.string.first_name)) },
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = controlsEnabled
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { if (controlsEnabled) lastName = it },
                        label = { RequiredLabel(context.getString(R.string.last_name)) },
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = controlsEnabled
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            if (!controlsEnabled) return@OutlinedTextField
                            email = it
                            emailError = it.isNotBlank() && !Validation().isValidEmail(it)
                        },
                        label = { Text(context.getString(R.string.email)) },
                        isError = emailError,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = controlsEnabled
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
                        value = phoneNumber,
                        onValueChange = {
                            if (!controlsEnabled) return@OutlinedTextField
                            phoneNumber = it
                            phoneError = it.isNotBlank() && !Validation().isValidPhone(it)
                        },
                        label = { Text(context.getString(R.string.phone_number)) },
                        isError = phoneError,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = controlsEnabled
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
                            if (!controlsEnabled) return@OutlinedTextField
                            mobileNumber = it
                            mobileError = !Validation().isValidIranMobile(it)
                        },
                        label = { RequiredLabel(context.getString(R.string.mobile_number)) },
                        isError = mobileError,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { fs ->
                                if (controlsEnabled && fs.isFocused) {
                                    scope.launch {
                                        listState.animateScrollToItem(6)
                                    }
                                }
                            },
                        enabled = controlsEnabled
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
                        onValueChange = { if (controlsEnabled) numberOfTenants = it },
                        label = { RequiredLabel(context.getString(R.string.number_of_tenants)) },
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = controlsEnabled
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { if (controlsEnabled) startDate = it },
                        label = { RequiredLabel(context.getString(R.string.start_date)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged {
                                if (controlsEnabled && it.isFocused) {
                                    showStartDatePicker = true
                                }
                            },
                        readOnly = true,
                        enabled = controlsEnabled
                    )

                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { if (controlsEnabled) endDate = it },
                        label = { RequiredLabel(context.getString(R.string.end_date)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged {
                                if (controlsEnabled && it.isFocused) {
                                    showEndDatePicker = true
                                }
                            },
                        readOnly = true,
                        enabled = controlsEnabled
                    )

                    Spacer(Modifier.height(8.dp))

                    StatusDropdown(
                        selectedStatus = selectedStatus,
                        onStatusSelected = { if (controlsEnabled) selectedStatus = it },
                        enabled = controlsEnabled
                    )
                }

                item {
                    Spacer(Modifier.height(12.dp))

                    if (!noOwnerUnits) {
                        ExposedDropdownMenuBoxExample(
                            sharedViewModel = sharedViewModel,
                            items = units,
                            selectedItem = selectedUnit,
                            onItemSelected = { if (controlsEnabled) selectedUnit = it },
                            label = context.getString(R.string.unit_number),
                            modifier = Modifier.fillMaxWidth(),
                            itemLabel = { it.unitNumber }
                        )
                    }
                }
            }

            if (controlsEnabled && showStartDatePicker) {
                PersianDatePickerDialogContent(
                    sharedViewModel = sharedViewModel,
                    onDateSelected = {
                        startDate = it
                        dismissDatePicker()
                    },
                    onDismiss = dismissDatePicker,
                    context = context
                )
            }

            if (controlsEnabled && showEndDatePicker) {
                PersianDatePickerDialogContent(
                    sharedViewModel = sharedViewModel,
                    onDateSelected = {
                        endDate = it
                        dismissDatePicker()
                    },
                    onDismiss = dismissDatePicker,
                    context = context
                )
            }
        },
        confirmButton = {
            val isFormValid =
                firstName.isNotBlank() &&
                        lastName.isNotBlank() &&
                        mobileNumber.isNotBlank() &&
                        numberOfTenants.isNotBlank() &&
                        startDate.isNotBlank() &&
                        endDate.isNotBlank() &&
                        selectedUnit != null &&
                        !emailError &&
                        !phoneError &&
                        !mobileError &&
                        !noOwnerUnits

            Button(
                onClick = {
                    if (!isFormValid) return@Button

                    val unit = selectedUnit ?: return@Button

                    if (selectedStatus == context.getString(R.string.active)) {
                        val conflict = Validation().isTenantPeriodConflicted(
                            unit.unitId, startDate, endDate, sharedViewModel
                        )
                        if (conflict) {
                            periodConflictError = true
                            return@Button
                        }
                    }

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

                    onAddTenant(newTenant, unit)
                },
                enabled = isFormValid
            ) {
                Text(context.getString(R.string.insert))
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




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusDropdown(selectedStatus: String, onStatusSelected: (String) -> Unit, enabled : Boolean = true) {
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
                    , enabled = enabled
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
    var numberOfWarehouse by remember { mutableStateOf("") }
    var postCode by remember { mutableStateOf("") }

    val context = LocalContext.current

    val postCodeError = postCode.isNotEmpty() && !Validation().isValidPostalCode(postCode)

    val isFormValid =
        unitNumber.isNotBlank() &&
                area.isNotBlank() &&
                numberOfRooms.isNotBlank() &&
                numberOfParking.isNotBlank() &&
                !postCodeError

    AlertDialog(
        onDismissRequest = { /* no-op: only cancel button closes */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
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
                    label = { RequiredLabel(context.getString(R.string.unit_number)) },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { RequiredLabel(context.getString(R.string.area)) },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = numberOfRooms,
                    onValueChange = { numberOfRooms = it },
                    label = { RequiredLabel(context.getString(R.string.number_of_rooms)) },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = numberOfParking,
                    onValueChange = { numberOfParking = it },
                    label = { RequiredLabel(context.getString(R.string.number_of_parking)) },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = numberOfWarehouse,
                    onValueChange = { numberOfWarehouse = it },
                    label = {
                        Text(
                            text = context.getString(R.string.number_of_warehouse),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = postCode,
                    onValueChange = { postCode = it },
                    label = {
                        Text(
                            text = context.getString(R.string.post_code),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    isError = postCodeError,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )

                if (postCodeError) {
                    Text(
                        text = context.getString(R.string.invalid_post_code),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
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
                        numberOfWarehouse = numberOfWarehouse,
                        postCode = postCode
                    )
                    onAddUnit(newUnit)
                },
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
                onClick = onDismiss
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
    var numberOfWarehouse by remember { mutableStateOf(unit.numberOfWarehouse) }
    val context = LocalContext.current
    var postCode by remember { mutableStateOf(unit.postCode) }

    AlertDialog(
        onDismissRequest = {},   // important: disable outside dismiss
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
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text(context.getString(R.string.area)) },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = numberOfRooms,
                    onValueChange = { numberOfRooms = it },
                    label = { Text(context.getString(R.string.number_of_rooms)) },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = numberOfParking,
                    onValueChange = { numberOfParking = it },
                    label = { Text(context.getString(R.string.number_of_parking)) },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = numberOfWarehouse,
                    onValueChange = { numberOfWarehouse = it },
                    label = { Text(context.getString(R.string.number_of_warehouse)) },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = postCode,
                    onValueChange = { postCode = it },
                    label = { Text(context.getString(R.string.post_code)) },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
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
                        numberOfWarehouse = numberOfWarehouse,
                        postCode = postCode
                    )
                    onUpdateUnit(updatedUnit)
                    // removed onDismiss()
                }
            ) {
                Text(
                    text = context.getString(R.string.update),
                    style = MaterialTheme.typography.bodyLarge
                )
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


@RequiresApi(Build.VERSION_CODES.M)
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
//            sharedViewModel.costsList.value = updatedCostsList
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
//                            sharedViewModel.updateCostPeriod(nonNullCost, selectedPeriod)
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
//                                    sharedViewModel.updateCostAmount(
//                                        cost,
//                                        filtered.persianToEnglishDigits().toDoubleOrNull() ?: 0.0
//                                    )
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

@Composable
fun RequiredLabel(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = " * ",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Red
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )

    }
}
