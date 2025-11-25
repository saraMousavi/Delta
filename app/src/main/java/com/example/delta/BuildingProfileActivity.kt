package com.example.delta

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import com.example.delta.data.entity.BuildingTabItem
import com.example.delta.data.entity.BuildingTabType
import com.example.delta.data.entity.BuildingTypes
import com.example.delta.data.entity.BuildingUsages
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.CityComplexes
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Debts
import com.example.delta.data.entity.Earnings
import com.example.delta.data.entity.Owners
import com.example.delta.data.entity.PhonebookEntry
import com.example.delta.data.entity.Tenants
import com.example.delta.data.entity.Units
import com.example.delta.enums.BuildingProfileFields
import com.example.delta.enums.CalculateMethod
import com.example.delta.enums.FundType
import com.example.delta.enums.PaymentLevel
import com.example.delta.enums.Period
import com.example.delta.enums.PermissionLevel
import com.example.delta.enums.Responsible
import com.example.delta.factory.BuildingsViewModelFactory
import com.example.delta.init.AuthUtils
import com.example.delta.init.IranianLocations
import com.example.delta.init.NumberCommaTransformation
import com.example.delta.init.Preference
import com.example.delta.viewmodel.BuildingTypeViewModel
import com.example.delta.viewmodel.BuildingUsageViewModel
import com.example.delta.viewmodel.BuildingsViewModel
import com.example.delta.viewmodel.SharedViewModel
import com.example.delta.volley.Building.BuildingFullDto
import com.example.delta.volley.Cost
import com.example.delta.volley.Fund
import com.example.delta.volley.Owner
import com.example.delta.volley.Tenant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale


class BuildingProfileActivity : ComponentActivity() {


    private val buildingViewModel: BuildingsViewModel by viewModels {
        BuildingsViewModelFactory(application = this.application)
    }

    val sharedViewModel: SharedViewModel by viewModels()
    var buildingTypeName: String = ""
    var buildingUsageName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val buildingId = intent.getLongExtra("BUILDING_DATA", 0L)
        if (buildingId == 0L) {
            Toast.makeText(this, getString(R.string.failed), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val buildingTypeName = intent.getStringExtra("BUILDING_TYPE_NAME") ?: "Unknown"
        val buildingUsageName = intent.getStringExtra("BUILDING_USAGE_NAME") ?: "Unknown"
        Log.d("buildingTypeName", buildingTypeName)
        Log.d("buildingUsageName", buildingUsageName)

        setContent {
            AppTheme(useDarkTheme = sharedViewModel.isDarkModeEnabled) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {

                    val context = LocalContext.current

                    val building by sharedViewModel.currentBuilding.collectAsState()
                    val isLoading by sharedViewModel.loadingBuilding.collectAsState()
                    val error by sharedViewModel.buildingError.collectAsState()

                    LaunchedEffect(buildingId) {
                        sharedViewModel.loadBuildingFromServer(
                            context = context,
                            buildingId = buildingId,
                            fiscalYear = null
                        )
                    }

                    when {
                        isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        error != null -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = error ?: getString(R.string.failed),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        building != null -> {
                            BuildingProfileScreen(
                                building = building!!
                            )
                        }

                        else -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = getString(R.string.failed),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun BuildingProfileScreen(building: Buildings) {
        val context = LocalContext.current
        val userId = Preference().getUserId(context = context)

        val coroutineScope = rememberCoroutineScope()
        val permissionLevelFundTab = AuthUtils.checkFieldPermission(
            userId, BuildingProfileFields.FUNDS_TAB.fieldNameRes, sharedViewModel
        )

        val permissionLevelTransactionTab = AuthUtils.checkFieldPermission(
            userId, BuildingProfileFields.TRANSACTION_TAB.fieldNameRes, sharedViewModel
        )

        val permissionLevelOwnerTab = AuthUtils.checkFieldPermission(
            userId, BuildingProfileFields.OWNERS_TAB.fieldNameRes, sharedViewModel
        )
        Log.d("permissionLevelOwnerTab", permissionLevelOwnerTab.toString())
        val permissionLevelUnitsTab = AuthUtils.checkFieldPermission(
            userId, BuildingProfileFields.UNITS_TAB.fieldNameRes, sharedViewModel
        )

        val permissionLevelTenantsTab = AuthUtils.checkFieldPermission(
            userId, BuildingProfileFields.TENANTS_TAB.fieldNameRes, sharedViewModel
        )

        val permissionLevelPhonebookTab = AuthUtils.checkFieldPermission(
            userId,
            BuildingProfileFields.PHONEBOOK_TAB.fieldNameRes,
            sharedViewModel
        )

        val currentRole = sharedViewModel.getRoleByUserId(userId).collectAsState(initial = null)
        val currentRoleId = currentRole.value?.roleId ?: 1L



        val tabs = listOfNotNull(
            BuildingTabItem(context.getString(R.string.overview), BuildingTabType.OVERVIEW),
//            if (permissionLevelOwnerTab == PermissionLevel.FULL || permissionLevelOwnerTab == PermissionLevel.WRITE
//                || permissionLevelOwnerTab == PermissionLevel.READ
//            ) {
                BuildingTabItem(context.getString(R.string.owners), BuildingTabType.OWNERS),
//            } else null,
//            if (permissionLevelUnitsTab == PermissionLevel.FULL || permissionLevelUnitsTab == PermissionLevel.WRITE
//                || permissionLevelUnitsTab == PermissionLevel.READ
//            ) {
                BuildingTabItem(context.getString(R.string.units), BuildingTabType.UNITS),
//            } else null,
//            if (permissionLevelTenantsTab == PermissionLevel.FULL || permissionLevelTenantsTab == PermissionLevel.WRITE
//                || permissionLevelTenantsTab == PermissionLevel.READ
//            ) {
                BuildingTabItem(context.getString(R.string.tenants), BuildingTabType.TENANTS),
//            } else null,
//            if (permissionLevelFundTab == PermissionLevel.FULL || permissionLevelFundTab == PermissionLevel.WRITE
//                || permissionLevelFundTab == PermissionLevel.READ
//            ) {
                BuildingTabItem(context.getString(R.string.funds), BuildingTabType.FUNDS),
//            }
//            else null,
//            if (permissionLevelTransactionTab == PermissionLevel.FULL || permissionLevelTransactionTab == PermissionLevel.WRITE
//                || permissionLevelTransactionTab == PermissionLevel.READ
//            ) {
                BuildingTabItem(context.getString(R.string.transaction), BuildingTabType.TRANSACTIONS),
//            } else null,
//            if (permissionLevelPhonebookTab == PermissionLevel.FULL || permissionLevelPhonebookTab == PermissionLevel.WRITE
//                || permissionLevelPhonebookTab == PermissionLevel.READ
//            ) {
                BuildingTabItem(context.getString(R.string.phone_number), BuildingTabType.PHONEBOOK_TAB)
//            }
//            else null
        )

        var selectedTab by remember { mutableIntStateOf(0) }
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = building.name,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { finish() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                Spacer(modifier = Modifier.height(16.dp))
                BuildingProfileSectionSelector(
                    tabs = tabs,
                    selectedIndex = selectedTab,
                    onTabSelected = { selectedTab = it },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                Log.d("tabs",tabs.toString())
                when (tabs.getOrNull(selectedTab)?.type) {
                    BuildingTabType.OVERVIEW -> {
                        OverviewTab(
                            sharedViewModel, building, currentRoleId,
                            onUpdateBuilding = { updatedBuilding ->
                                coroutineScope.launch {
                                    try {
                                        sharedViewModel.updateBuilding(updatedBuilding)
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, context.getString(R.string.success_update), Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            Log.e("error", e.message.toString())
                                            Toast.makeText(context, context.getString(R.string.failed), Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }

                        )
                    }
                    BuildingTabType.OWNERS -> OwnersTab(building, sharedViewModel)
                    BuildingTabType.UNITS -> UnitsTab(building)
                    BuildingTabType.TENANTS -> TenantsTab(building, sharedViewModel)
                    BuildingTabType.FUNDS -> FundsTab(
                        building = building, sharedViewModel = sharedViewModel,
                        onOpenCostDetail = { cost, fundType ->

                            coroutineScope.launch {
//                                sharedViewModel.getCost(costId).collect { cost ->
                                    val intent =
                                        Intent(context, CostDetailActivity::class.java).apply {
                                            putExtra("COST_DATA", cost as Parcelable)
                                            putExtra("FUND_TYPE", fundType.ordinal)
                                        }
                                    context.startActivity(intent)
//                                }
                            }


                        })

                    BuildingTabType.TRANSACTIONS -> TransactionHistoryTab(building, sharedViewModel)
                    BuildingTabType.PHONEBOOK_TAB -> PhonebookTab(building, sharedViewModel, permissionLevelPhonebookTab!!)
                    null -> {} // Handle invalid index if needed
                }

            }
        }
    }

    @Composable
    fun OverviewTab(
        sharedViewModel: SharedViewModel,
        building: Buildings,
        roleId: Long,
        onUpdateBuilding: (Buildings) -> Unit
    ) {
        val context = LocalContext.current

        var isEditing by remember { mutableStateOf(false) }
        var editableBuilding by remember(building) { mutableStateOf(building) }

        val userId = remember { Preference().getUserId(context) }

        // Global reference data
        val buildingTypes by sharedViewModel.buildingTypes.collectAsState()
        val buildingUsages by sharedViewModel.buildingUsages.collectAsState()
        val cityComplexes by sharedViewModel.cityComplexes.collectAsState()

        // Costs
        val allCosts by sharedViewModel.costsList.collectAsState()
        val chargesCost by sharedViewModel.chargesCost.collectAsState()

        val costApi = remember { Cost() }
        val addNewLabel = context.getString(R.string.addNew)

        // Pre-selected costs for this building (only ones that are chargeFlag==true)
        var selectedCostNames by remember(chargesCost) {
            mutableStateOf(
                chargesCost
                    .filter { it.chargeFlag == true }
                    .map { it.costName }
            )
        }

        var showChargeCostDialog by remember { mutableStateOf(false) }

        val selectedBuildingType = remember(buildingTypes, editableBuilding.buildingTypeId) {
            buildingTypes.find { it.buildingTypeId == editableBuilding.buildingTypeId }
        }

        val selectedBuildingUsage = remember(buildingUsages, editableBuilding.buildingUsageId) {
            buildingUsages.find { it.buildingUsageId == editableBuilding.buildingUsageId }
        }

        val selectedCityComplex = remember(cityComplexes, editableBuilding.complexId) {
            cityComplexes.find { it.complexId == editableBuilding.complexId }
        }

        val buildingTypeNameTrigger = context.getString(R.string.city_complex)

        var showAddCityComplexDialog by remember { mutableStateOf(false) }
        var showBuildingTypeDialog by remember { mutableStateOf(false) }
        var showBuildingUsageDialog by remember { mutableStateOf(false) }

        val permissionLevelBuildingName = AuthUtils.checkFieldPermission(
            userId,
            BuildingProfileFields.BUILDING_NAME.fieldNameRes,
            sharedViewModel
        )
        val permissionLevelDoc = AuthUtils.checkFieldPermission(
            userId,
            BuildingProfileFields.DOCUMENTS.fieldNameRes,
            sharedViewModel
        )

        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentPadding = PaddingValues(bottom = 72.dp)
                ) {
                    // 1) Name & address (بدون تغییر نسبت به قبل)

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (permissionLevelBuildingName == PermissionLevel.FULL ||
                                    permissionLevelBuildingName == PermissionLevel.WRITE
                                ) {
                                    if (isEditing) {
                                        OutlinedTextField(
                                            value = editableBuilding.name,
                                            onValueChange = {
                                                editableBuilding = editableBuilding.copy(name = it)
                                            },
                                            label = { Text(context.getString(R.string.building_name)) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    } else {
                                        Text(
                                            text = "${context.getString(R.string.building_name)}: ${editableBuilding.name}",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                if (isEditing) {
                                    OutlinedTextField(
                                        value = editableBuilding.street,
                                        onValueChange = {
                                            editableBuilding = editableBuilding.copy(street = it)
                                        },
                                        label = {
                                            Text(
                                                context.getString(R.string.street),
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    Text(
                                        text = "${context.getString(R.string.street)}: ${editableBuilding.street}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                if (isEditing) {
                                    OutlinedTextField(
                                        value = editableBuilding.postCode,
                                        onValueChange = {
                                            editableBuilding = editableBuilding.copy(postCode = it)
                                        },
                                        label = {
                                            Text(
                                                context.getString(R.string.post_code),
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    Text(
                                        text = "${context.getString(R.string.post_code)}: ${editableBuilding.postCode}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }

                    // 2) Province & state (بدون تغییر، همون کدی که خودت داشتی)
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (isEditing) {
                                    val provinces = IranianLocations.provinces.keys.toList()
                                    val availableStates =
                                        IranianLocations.provinces[editableBuilding.province]
                                            ?: emptyList()

                                    ExposedDropdownMenuBoxExample(
                                        sharedViewModel = sharedViewModel,
                                        items = provinces,
                                        selectedItem = editableBuilding.province,
                                        onItemSelected = { selectedProvince ->
                                            editableBuilding =
                                                editableBuilding.copy(province = selectedProvince)
                                        },
                                        label = context.getString(R.string.province),
                                        modifier = Modifier.fillMaxWidth(),
                                        itemLabel = { it }
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    ExposedDropdownMenuBoxExample(
                                        sharedViewModel = sharedViewModel,
                                        items = availableStates,
                                        selectedItem = editableBuilding.state,
                                        onItemSelected = { selectedState ->
                                            editableBuilding =
                                                editableBuilding.copy(state = selectedState)
                                        },
                                        label = context.getString(R.string.state),
                                        modifier = Modifier.fillMaxWidth(),
                                        itemLabel = { it }
                                    )
                                } else {
                                    Text(
                                        text = "${context.getString(R.string.province)}: ${editableBuilding.province}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "${context.getString(R.string.state)}: ${editableBuilding.state}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }

                    // 3) Building type, usage, cityComplex (تقریباً همون قبله؛ فقط دست نزنیم)
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (isEditing) {
                                    ExposedDropdownMenuBoxExample(
                                        sharedViewModel = sharedViewModel,
                                        items = buildingTypes + BuildingTypes(
                                            buildingTypeId = 0,
                                            buildingTypeName = addNewLabel
                                        ),
                                        selectedItem = selectedBuildingType,
                                        onItemSelected = { selected ->
                                            if (selected.buildingTypeId == 0L &&
                                                selected.buildingTypeName == addNewLabel
                                            ) {
                                                showBuildingTypeDialog = true
                                            } else {
                                                editableBuilding =
                                                    editableBuilding.copy(buildingTypeId = selected.buildingTypeId)
                                            }
                                        },
                                        label = context.getString(R.string.building_type),
                                        modifier = Modifier.fillMaxWidth(),
                                        itemLabel = { it.buildingTypeName }
                                    )

                                    if (selectedBuildingType?.buildingTypeName == buildingTypeNameTrigger) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        ExposedDropdownMenuBoxExample(
                                            sharedViewModel = sharedViewModel,
                                            items = cityComplexes + CityComplexes(
                                                complexId = 0L,
                                                name = addNewLabel,
                                                address = null
                                            ),
                                            selectedItem = selectedCityComplex,
                                            onItemSelected = { selected ->
                                                if (selected.complexId == 0L &&
                                                    selected.name == addNewLabel
                                                ) {
                                                    showAddCityComplexDialog = true
                                                } else {
                                                    editableBuilding =
                                                        editableBuilding.copy(complexId = selected.complexId)
                                                }
                                            },
                                            label = context.getString(R.string.city_complex),
                                            modifier = Modifier.fillMaxWidth(),
                                            itemLabel = { it.name }
                                        )
                                    }

                                    Spacer(Modifier.height(8.dp))

                                    ExposedDropdownMenuBoxExample(
                                        sharedViewModel = sharedViewModel,
                                        items = buildingUsages + BuildingUsages(
                                            buildingUsageId = 0,
                                            buildingUsageName = addNewLabel
                                        ),
                                        selectedItem = selectedBuildingUsage,
                                        onItemSelected = { selected ->
                                            if (selected.buildingUsageId == 0L &&
                                                selected.buildingUsageName == addNewLabel
                                            ) {
                                                showBuildingUsageDialog = true
                                            } else {
                                                editableBuilding =
                                                    editableBuilding.copy(buildingUsageId = selected.buildingUsageId)
                                            }
                                        },
                                        label = context.getString(R.string.building_usage),
                                        modifier = Modifier.fillMaxWidth(),
                                        itemLabel = { it.buildingUsageName }
                                    )
                                } else {
                                    Text(
                                        text = "${context.getString(R.string.building_type)}: ${selectedBuildingType?.buildingTypeName ?: "-"}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    if (selectedBuildingType?.buildingTypeName == buildingTypeNameTrigger) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "${context.getString(R.string.city_complex_name)}: ${selectedCityComplex?.name ?: "-"}",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "${context.getString(R.string.building_usage)}: ${selectedBuildingUsage?.buildingUsageName ?: "-"}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }

                    // 4) Charges (new UI)
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = context.getString(R.string.charges_parameter),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                if (isEditing) {
                                    val chipItems = allCosts + Costs(
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

                                    ChipGroupShared(
                                        selectedItems = selectedCostNames,
                                        onSelectionChange = { newSelectionStrings ->
                                            if (newSelectionStrings.contains(addNewLabel)) {
                                                showChargeCostDialog = true
                                                selectedCostNames =
                                                    newSelectionStrings.filter { it != addNewLabel }
                                            } else {
                                                selectedCostNames = newSelectionStrings
                                            }
                                        },
                                        items = chipItems.map { it.costName },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = context.getString(R.string.charges_parameter),
                                        singleSelection = false
                                    )
                                } else {
                                    chargesCost.forEachIndexed { index, cost ->
                                        Text(
                                            text = cost.costName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                        if (index != chargesCost.lastIndex) {
                                            Spacer(Modifier.height(4.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 5) Documents + upload button
                    if (permissionLevelDoc == PermissionLevel.FULL ||
                        permissionLevelDoc == PermissionLevel.WRITE
                    ) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                shape = RoundedCornerShape(8.dp),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = context.getString(R.string.documents),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            // hook your existing document upload flow here
                                        },
                                        modifier = Modifier.align(Alignment.Start)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.CloudUpload,
                                            contentDescription = null
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(text = context.getString(R.string.documents))
                                    }
                                }
                            }
                        }
                    }
                }

                if (isEditing) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                isEditing = false
                                editableBuilding = building
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
                                isEditing = false
                                // you can also persist selectedCostNames here if needed
                                onUpdateBuilding(editableBuilding)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = context.getString(R.string.insert),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            if (!isEditing) {
                FloatingActionButton(
                    onClick = { isEditing = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
            }
        }

        if (showAddCityComplexDialog) {
            AddCityComplexDialog(
                onDismiss = { showAddCityComplexDialog = false },
                onInsert = { newName, newAddress ->
                    sharedViewModel.insertCityComplexRemote(
                        context = context,
                        name = newName,
                        address = newAddress
                    ) { inserted ->
                        if (inserted != null) {
                            editableBuilding = editableBuilding.copy(complexId = inserted.complexId)
                        }
                    }
                    showAddCityComplexDialog = false
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
                    ) { inserted ->
                        if (inserted != null) {
                            editableBuilding =
                                editableBuilding.copy(buildingTypeId = inserted.buildingTypeId)
                        }
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
                    ) { inserted ->
                        if (inserted != null) {
                            editableBuilding =
                                editableBuilding.copy(buildingUsageId = inserted.buildingUsageId)
                        }
                    }
                }
            )
        }

        if (showChargeCostDialog) {
            AddNewCostDialog(
                onDismiss = { showChargeCostDialog = false },
                onConfirm = { newCostName ->
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
                            sharedViewModel.appendGlobalCost(created)
                            selectedCostNames = selectedCostNames + created.costName
                            showChargeCostDialog = false
                        },
                        onError = {
                            showChargeCostDialog = false
                        }
                    )
                }
            )
        }
    }


    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun FundsTab(
        building: Buildings,
        sharedViewModel: SharedViewModel,
        onOpenCostDetail: (cost: Costs, fundType: FundType) -> Unit
    ) {
        val context = LocalContext.current

        val snackBarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()

        // Funds come from server
        val funds by sharedViewModel.fundsForBuilding.collectAsState()

        // Pending costs come from server
        val pendingCosts by sharedViewModel.pendingCostsForBuilding.collectAsState()

        // Load both funds and pending costs once per building
        LaunchedEffect(building.buildingId) {
            sharedViewModel.loadFundsForBuilding(context, building.buildingId)
        }

        // Extract operational / capital fund from funds list
        val operationalFund = remember(funds) {
            funds.firstOrNull { it.fundType == FundType.OPERATIONAL }?.balance ?: 0.0
        }

        val capitalFund = remember(funds) {
            funds.firstOrNull { it.fundType == FundType.CAPITAL }?.balance ?: 0.0
        }

        // Split pending costs by fundType (the server already returns "pending" items)
        val operationalCosts = remember(pendingCosts) {
            pendingCosts.filter { it.fundType == FundType.OPERATIONAL }
        }

        val capitalCosts = remember(pendingCosts) {
            pendingCosts.filter { it.fundType == FundType.CAPITAL }
        }

        var selectedTab by remember { mutableIntStateOf(0) }
        val tabTitles = listOf(
            context.getString(R.string.incomes),
            context.getString(R.string.capital_costs),
            context.getString(R.string.operational_costs)
        )

        LaunchedEffect(building.buildingId) {
            sharedViewModel.loadFundBalances(building.buildingId)
        }

        Box(modifier = Modifier.fillMaxSize()) { // Use Box to allow overlay positioning of FAB
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                // Fund balances card and tab row (same as yours)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    FundInfoBox(
                        formattedFund = formatNumberWithCommas(operationalFund!!.toDouble()),
                        context = context,
                        title = context.getString(R.string.operation_funds)
                    )
                    Spacer(Modifier.height(8.dp))
                    FundInfoBox(
                        formattedFund = formatNumberWithCommas(capitalFund!!.toDouble()),
                        context = context,
                        title = context.getString(R.string.capital_funds)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        TextButton(
                            onClick = { selectedTab = index },
                            colors = if (selectedTab == index)
                                ButtonDefaults.textButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(
                                        alpha = 0.12f
                                    )
                                )
                            else
                                ButtonDefaults.textButtonColors(),
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = title.uppercase(),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (selectedTab == index)
                                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                when (selectedTab) {
                    0 -> {
                        Column {
                        EarningsSection(
                            buildingId = building.buildingId,
                            sharedViewModel = sharedViewModel,
                            onInvoiceClicked = { earning ->
                                val intent = Intent(context, EarningDetailActivity::class.java)
                                intent.putExtra("extra_earning_id", earning.earningsId)
                                context.startActivity(intent)
                            }
                        )
                        }
                    }

                    1 -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (capitalCosts.isEmpty()) {
                                Text(
                                    context.getString(R.string.no_capital_cost),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .padding(8.dp)
                                ) {
                                    items(capitalCosts) { cost ->
                                        CostListItemWithDetailText(cost = cost) {
                                            onOpenCostDetail(cost, FundType.CAPITAL)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (operationalCosts.isEmpty()) {
                                Text(
                                    context.getString(R.string.no_operation_cost),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    items(operationalCosts) { cost ->
                                        CostListItemWithDetailText(cost = cost) {
                                            onOpenCostDetail(cost, FundType.OPERATIONAL)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // FABs, conditionally show one depending on selected tab
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {

                SnackbarHost(hostState = snackBarHostState)
                if (selectedTab == 0) {
                    FloatingActionButton(
                        onClick = { buildingViewModel.showEarningsDialog.value = true },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Earnings")
                    }
                } else if (selectedTab == 1) {
                    FloatingActionButton(
                        onClick = { buildingViewModel.showCapitalCostDialog.value = true },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Cost")
                    }
                } else if (selectedTab == 2) {
                    FloatingActionButton(
                        onClick = { buildingViewModel.showOperationalCostDialog.value = true },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Cost")
                    }
                }
            }
        }
        // Show dialogs if needed (unchanged)

            if (buildingViewModel.showCapitalCostDialog.value) {
//                val capitalFund by sharedViewModel.getOperationalOrCapitalFundBalance(
//                    buildingId = building.buildingId,
//                    fundType = FundType.CAPITAL
//                ).collectAsState(initial = 0)
                var success by remember { mutableStateOf(false) }
                AddCapitalCostDialog(
                buildingId = building.buildingId,
                sharedViewModel = sharedViewModel,
                onDismiss = { buildingViewModel.showCapitalCostDialog.value = false },
                onSave = { selectedCost, amount, period, calculateMethod, calculatedUnitMethod, responsible, selectedUnits, selectedOwners, dueDate, message ->
                    coroutineScope.launch {
                        Log.d("amount", amount.toString())
                        Log.d("capitalFund", capitalFund.toString())
                        if (amount.toDouble() <= (capitalFund.toDouble())) {
                            val cost = Costs(
                                buildingId = building.buildingId,
                                costName = selectedCost.costName,
                                fundType = FundType.CAPITAL,
                                responsible = Responsible.OWNER,
                                paymentLevel = PaymentLevel.UNIT,
                                calculateMethod = CalculateMethod.EQUAL,
                                period = Period.YEARLY,
                                tempAmount = amount.toDouble(),
                                dueDate = dueDate,
                                invoiceFlag = true
                            )
//                            sharedViewModel.insertNewCost(
//                                cost,
//                                onSuccess = {
//                                    sharedViewModel.insertCostToServer (context, listOf(cost),               // ⬅️ تبدیل به لیست یک‌عضوی
//                                        emptyList<Debts>(),
//                                        onSuccess = {
//                                            coroutineScope.launch {
//                                                snackBarHostState.showSnackbar(context.getString(R.string.charge_calcualted_successfully))
//                                            }
//                                        }, onError = {
//                                            coroutineScope.launch {
//                                                snackBarHostState.showSnackbar(context.getString(R.string.failed))
//                                            }
//                                        })
//                                }
//                            )
                            Fund().decreaseOperationalFundOnServer(
                                context = context,
                                buildingId = building.buildingId,
                                amount.toDouble(),
                                FundType.CAPITAL,
                                onSuccess = { ok ->
                                    coroutineScope.launch {
                                        snackBarHostState.showSnackbar(context.getString(R.string.fund_decreased_successfully))
                                    }
                                },
                                onError = { e->
                                    coroutineScope.launch {
                                        snackBarHostState.showSnackbar(context.getString(R.string.failed))
                                    }
                                }
                            )
//                            success = sharedViewModel.decreaseOperationalFund(
//                                building.buildingId,
//                                amount.toDouble(),
//                                FundType.CAPITAL,
//                                onSuccess = {},
//                                onError = {}
//                            )
                        } else {
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar(context.getString(R.string.insufficient_fund))
                            }
                        }
                        buildingViewModel.showCapitalCostDialog.value = false

                    }
                }
            )
        }

        if (buildingViewModel.showOperationalCostDialog.value) {
            AddOperationalCostDialog(
                buildingId = building.buildingId,
                sharedViewModel = sharedViewModel,
                onDismiss = { buildingViewModel.showOperationalCostDialog.value = false },
                onSave = { message ->
                    coroutineScope.launch {
                    buildingViewModel.showOperationalCostDialog.value = false
                    snackBarHostState.showSnackbar(message)
                    }
                }
            )
        }


        if (buildingViewModel.showEarningsDialog.value) {
            EarningsDialog(
                building = building,
                onDismiss = { buildingViewModel.hideDialogs() },
                onConfirm = { earning ->
                    coroutineScope.launch {
                        try {
                            val earningsToInsert = Earnings(
                                earningsId = sharedViewModel.selectedEarnings?.earningsId ?: 0,
                                buildingId = building.buildingId,
                                earningsName = earning.earningsName,
                                amount = earning.amount,
                                period = earning.period,
                                startDate = earning.startDate,
                                endDate = earning.endDate
                            )
                            sharedViewModel.insertEarningsWithCredits(context, earningsToInsert)
                            snackBarHostState.showSnackbar(context.getString(R.string.earning_inserted_successfully))
                        } catch (e: IllegalStateException) {
                            Log.e("error", e.message.toString())
                            snackBarHostState.showSnackbar(context.getString(R.string.earnings_conflict_error))
                        }
                    }
                    buildingViewModel.showEarningsDialog.value = false
                },
                sharedViewModel = sharedViewModel
            )
        }
    }


    @Composable
    fun CostListItemWithDetailText(
        cost: Costs,
        onDetailClick: () -> Unit
    ) {
        val context = LocalContext.current
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = cost.costName, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(4.dp))
                    Row {
                        Text(
                            text = "${context.getString(R.string.due)}:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = cost.dueDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${formatNumberWithCommas(cost.tempAmount)} ${
                            LocalContext.current.getString(R.string.toman)
                        }",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(4.dp))
                    TextButton(onClick = onDetailClick) {
                        Text(
                            text = LocalContext.current.getString(R.string.detail),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(LocalContext.current.getColor(R.color.grey))
                        )
                    }
                }
            }
        }

    }
    @Composable
    fun TenantsTab(
        building: Buildings,
        sharedViewModel: SharedViewModel
    ) {
        val context = LocalContext.current
        val tenantApi = remember { com.example.delta.volley.Tenant() }
        val unitsApi = remember { com.example.delta.volley.Units() }

        var tenantsWithUnit by remember { mutableStateOf<List<Tenant.TenantWithUnitDto>>(emptyList()) }
        var loading by remember { mutableStateOf(false) }
        var showTenantDialog by remember { mutableStateOf(false) }

        var units by remember { mutableStateOf<List<Units>>(emptyList()) }
        var unitsLoading by remember { mutableStateOf(false) }

        fun loadTenants() {
            loading = true
            tenantApi.fetchTenantsWithUnitsByBuilding(
                context = context,
                buildingId = building.buildingId,
                onSuccess = { list ->
                    tenantsWithUnit = list
                    loading = false
                },
                onError = { e ->
                    loading = false
                    Toast.makeText(
                        context,
                        e.message ?: context.getString(R.string.failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }

        LaunchedEffect(building.buildingId) {
            loadTenants()
        }

        LaunchedEffect(building.buildingId) {
            unitsLoading = true
            unitsApi.fetchUnitsWithOwnerForBuilding (
                context = context,
                buildingId = building.buildingId,
                onSuccess = { list ->
                    Log.d("list", list.toString())
                    units = list
                    unitsLoading = false
                },
                onError = { e ->
                    unitsLoading = false
                    Toast.makeText(
                        context,
                        e.message ?: context.getString(R.string.failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (loading && tenantsWithUnit.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    items(tenantsWithUnit) { dto ->
                        val tenant = dto.tenant
                        val unit = dto.unit

                        TenantItem(
                            tenants = tenant,
                            sharedViewModel = sharedViewModel,
                            onDelete = {
                                tenantApi.deleteTenant(
                                    context = context,
                                    tenantId = tenant.tenantId,
                                    onSuccess = {
                                        tenantsWithUnit = tenantsWithUnit.filterNot {
                                            it.tenant.tenantId == tenant.tenantId
                                        }
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.success_delete),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onError = { err ->
                                        Toast.makeText(
                                            context,
                                            err.message ?: context.getString(R.string.failed),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            },
                            activity = context.findActivity(),
                            onClick = {
                                if (unit != null) {
                                    val intent = Intent(context, TenantsDetailsActivity::class.java)
                                    intent.putExtra("UNIT_DATA", unit.unitId)
                                    intent.putExtra("TENANT_DATA", tenant.tenantId)
                                    context.startActivity(intent)
                                }
                            }
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = { showTenantDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Filled.Add, "Add")
            }

            if (showTenantDialog && !unitsLoading) {
                TenantDialog(
                    sharedViewModel = sharedViewModel,
                    units = units,
                    onDismiss = { showTenantDialog = false },
                    onAddTenant = { newTenant, selectedUnit ->
                        tenantApi.insertTenantWithUnit(
                            context = context,
                            buildingId = building.buildingId,
                            tenant = newTenant,
                            unitId = selectedUnit.unitId,
                            onSuccess = { createdDto ->
                                tenantsWithUnit = tenantsWithUnit + createdDto
                                showTenantDialog = false
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.insert_tenant_successfully),
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onError = { e ->
                                Toast.makeText(
                                    context,
                                    e.message ?: context.getString(R.string.failed),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                )
            }
        }
    }


    @Composable
    private fun TenantItemWithServerUnit(
        tenant: Tenants,
        tenantUnitApi: com.example.delta.volley.TenantUnit,
        sharedViewModel: SharedViewModel,
        building: Buildings,
        onDelete: () -> Unit
    ) {
        val context = LocalContext.current
        var unit by remember { mutableStateOf<Units?>(null) }
        var loadingUnit by remember { mutableStateOf(false) }

        LaunchedEffect(tenant.tenantId) {
            loadingUnit = true
            tenantUnitApi.fetchTenantUnitsByTenant(
                context = context,
                tenantId = tenant.tenantId,
                onSuccess = { units ->
                    unit = units.firstOrNull()
                    loadingUnit = false
                },
                onError = {
                    loadingUnit = false
                }
            )
        }

        TenantItem(
            tenants = tenant,
            sharedViewModel = sharedViewModel,
            onDelete = { onDelete() },
            activity = context.findActivity(),
            onClick = {
                val selectedUnit = unit
                if (selectedUnit != null) {
                    val intent = Intent(context, TenantsDetailsActivity::class.java)
                    intent.putExtra("UNIT_DATA", selectedUnit.unitId)
                    intent.putExtra("TENANT_DATA", tenant.tenantId)
                    context.startActivity(intent)
                } else {
//                    Toast.makeText(
//                        context,
//                        context.getString(R.string.unit_not_loaded),
//                        Toast.LENGTH_SHORT
//                    ).show()
                }
            }
        )
    }


    @Composable
    fun EarningsSection(
        buildingId: Long,
        sharedViewModel: SharedViewModel,
        onInvoiceClicked: (Earnings) -> Unit,
        modifier: Modifier = Modifier
    ) {
        val context = LocalContext.current
        val earnings by sharedViewModel.getNotInvoicedEarnings(context = context,  buildingId)
            .collectAsState(initial = emptyList())


        Column(modifier = modifier.padding(16.dp)) {
            if (earnings.isEmpty()) {
                Text(
                    text = context.getString(R.string.no_earnings_pending),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn {
                    items(earnings) { earning ->
                        Log.d("earning", earning.toString())
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = earning.earningsName,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row {
                                        Text(
                                            text = "${context.getString(R.string.start_date)}:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = earning.startDate,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${formatNumberWithCommas(earning.amount)} ${context.getString(R.string.toman)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.End,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    TextButton(onClick = { onInvoiceClicked(earning) }) {
                                        Text(
                                            text = context.getString(R.string.detail),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Color(context.getColor(R.color.grey))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    @Composable
    fun UnitsTab(
        building: Buildings
    ) {
        val context = LocalContext.current
        val unitsApi = remember { com.example.delta.volley.Units() }

        var units by remember { mutableStateOf<List<Units>>(emptyList()) }
        var loading by remember { mutableStateOf(false) }
        var showUnitDialog by remember { mutableStateOf(false) }

        LaunchedEffect(building.buildingId) {
            loading = true
            unitsApi.fetchUnitsForBuilding(
                context = context,
                buildingId = building.buildingId,
                onSuccess = { list ->
                    units = list
                    loading = false
                },
                onError = { e ->
                    loading = false
                    Toast.makeText(
                        context,
                        e.message ?: context.getString(R.string.failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (loading && units.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn {
                    items(units) { unit ->
                        UnitItem(
                            unit = unit,
                            onUpdateUnit = { updatedUnit ->
                                unitsApi.updateUnit(
                                    context = context,
                                    buildingId = building.buildingId,
                                    unit = updatedUnit,
                                    onSuccess = { serverUnit ->
                                        units = units.map {
                                            if (it.unitId == serverUnit.unitId) serverUnit else it
                                        }
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.success_update),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onError = { e ->
                                        Toast.makeText(
                                            context,
                                            e.message ?: context.getString(R.string.failed),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = { showUnitDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Filled.Add, "Add")
            }

            if (showUnitDialog) {
                UnitDialog(
                    onDismiss = { showUnitDialog = false },
                    onAddUnit = { newUnit ->
                        unitsApi.insertUnitForBuilding(
                            context = context,
                            buildingId = building.buildingId,
                            unit = newUnit,
                            onSuccess = { createdUnit ->
                                units = units + createdUnit
                                showUnitDialog = false
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.insert_unit_successfully),
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onError = { e ->
                                try {
                                    val json = JSONObject(e.message ?: "")
                                    val translated = translateServerError(
                                        json.optString("message"),
                                        json.optJSONObject("errors")
                                    )

                                    Toast.makeText(context, translated, Toast.LENGTH_LONG).show()

                                } catch (_: Exception) {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.failed),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                        )
                    }
                )
            }
        }
    }

    fun translateServerError(message: String?, errors: JSONObject?): String {
        val main = when (message) {
            "duplicate-key" -> "داده تکراری است."
            "validation-error" -> "اطلاعات وارد شده معتبر نیست."
            "missing-fields" -> "بعضی از فیلدهای لازم وارد نشده است."
            "not-found" -> "موردی با این مشخصات پیدا نشد."
            "db-error" -> "خطا در پایگاه داده."
            else -> "خطای نامشخص."
        }

        val details = if (errors != null) {
            errors.keys().asSequence().joinToString("\n") { key ->
                val value = errors.optString(key)

                val translatedField = when (key) {
                    "unitId" -> "شناسه واحد"
                    "unitNumber" -> "شماره واحد"
                    "buildingId" -> "شناسه ساختمان"
                    "area" -> "متراژ"
                    "numberOfRooms" -> "تعداد اتاق"
                    "numberOfParking" -> "تعداد پارکینگ"
                    "numberOfWarehouse" -> "تعداد انباری"
                    "postCode" -> "کدپستی"
                    else -> key
                }

                val translatedValue = when (value) {
                    "duplicate" -> "تکراری است"
                    "required" -> "باید وارد شود"
                    "invalid" -> "نامعتبر است"
                    else -> value
                }

                "$translatedField: $translatedValue"
            }
        } else ""

        return if (details.isBlank()) main else "$main\n$details"
    }

    @Composable
    fun UnitItem(
        unit: Units,
        onUpdateUnit: (Units) -> Unit
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
        }

        if (showEditDialog) {
            EditUnitDialog(
                unit = unit,
                onDismiss = { showEditDialog = false },
                onUpdateUnit = { updatedUnit ->
                    onUpdateUnit(updatedUnit)
                    showEditDialog = false
                }
            )
        }
    }


    @Composable
    fun TransactionHistoryTab(building: Buildings, sharedViewModel: SharedViewModel) {
        val context = LocalContext.current
        val buildingId = building.buildingId
        LaunchedEffect(buildingId) {
            sharedViewModel.loadInvoicedCostsForBuilding(context, buildingId)
        }

        val invoicedCosts by sharedViewModel.invoicedCostsForBuilding.collectAsState()


        val capitalInvoicedCosts = remember(invoicedCosts) {
            invoicedCosts.filter { it.fundType == FundType.CAPITAL }
        }

        val operationalInvoicedCosts = remember(invoicedCosts) {
            invoicedCosts.filter { it.fundType == FundType.OPERATIONAL }
        }
        // Collect the lists of invoiced costs separately for capital and operational funds
//        val capitalInvoicedCosts by sharedViewModel.getInvoicedCostsByFundType(
//            buildingId,
//            FundType.CAPITAL
//        ).collectAsState(initial = emptyList())
//
//        val operationalInvoicedCosts by sharedViewModel.getInvoicedCostsByFundType(
//            buildingId,
//            FundType.OPERATIONAL
//        ).collectAsState(initial = emptyList())

        var selectedTab by remember { mutableStateOf(0) }
        val tabTitles = listOf(
            context.getString(R.string.capital_funds),
            context.getString(R.string.operation_funds)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Row of TextButtons as tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                tabTitles.forEachIndexed { index, title ->
                    TextButton(
                        onClick = { selectedTab = index },
                        colors = if (selectedTab == index)
                            ButtonDefaults.textButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            )
                        else ButtonDefaults.textButtonColors(),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = title.uppercase(),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = if (selectedTab == index)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> {
                    if (capitalInvoicedCosts.isEmpty()) {
                        Text(
                            text = context.getString(R.string.no_transactions_recorded),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(capitalInvoicedCosts) { cost ->
                                TransactionCostItemBordered(cost = cost, context = context)
                            }
                        }
                    }
                }
                1 -> {
                    if (operationalInvoicedCosts.isEmpty()) {
                        Text(
                            text = context.getString(R.string.no_transactions_recorded),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(operationalInvoicedCosts) { cost ->
                                TransactionCostItemBordered(cost = cost, context = context)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun TransactionCostItemBordered(cost: Costs, context: android.content.Context) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .border(
                    width = 0.7.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(6.dp)
                ),
            color = Color.Transparent, // No background color
            shadowElevation = 0.dp,
            shape = RoundedCornerShape(6.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = cost.costName,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${formatNumberWithCommas(cost.tempAmount)} ${context.getString(R.string.toman)}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Column {
                    Text(
                        text = "${context.getString(R.string.due)}: ${cost.dueDate}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${context.getString(R.string.fund_type)}: ${
                            cost.fundType.getDisplayName(
                                context
                            )
                        }",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

            }
        }
    }

    @Composable
    fun PhonebookTab(
        building: Buildings,
        sharedViewModel: SharedViewModel,
        permissionLevel: PermissionLevel
    ) {
        val context = LocalContext.current
        val buildingId = building.buildingId

        var selectedTab by remember { mutableStateOf(0) }
        val tabTitles = listOf(
            context.getString(R.string.emergency_calls).uppercase(),
            context.getString(R.string.tenants).uppercase()
        )

        LaunchedEffect(buildingId) {
            sharedViewModel.loadPhonebookForBuilding(context, buildingId)
        }

        val entries by sharedViewModel.phonebookEntriesForBuilding.collectAsState()

        val residents = remember(entries, buildingId) {
            entries.filter { it.buildingId == buildingId && it.type == "resident" }
        }
        val emergencyNumbers = remember(entries, buildingId) {
            entries.filter { it.buildingId == buildingId && it.type == "emergency" }
        }

        var showAddDialog by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                // Tab buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        TextButton(
                            onClick = { selectedTab = index },
                            colors = if (selectedTab == index)
                                ButtonDefaults.textButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                ) else ButtonDefaults.textButtonColors(),
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = if (selectedTab == index)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (selectedTab) {
                    0 -> {
                        // Emergency Numbers tab content
                        if (emergencyNumbers.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = context.getString(R.string.no_phone_recorded),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(emergencyNumbers) { entry ->
                                    PhonebookEntryItem(entry, permissionLevel)
                                }
                            }
                        }
                    }
                    1 -> {
                        // Residents (Tenants) tab content
                        if (residents.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = context.getString(R.string.no_phone_recorded),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(residents) { entry ->
                                    PhonebookEntryItem(entry, permissionLevel)
                                }
                            }
                        }
                    }
                }
            }
            // FABs, conditionally show one depending on selected tab
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                SnackbarHost(hostState = snackbarHostState)
                if (selectedTab == 0) {
                    FloatingActionButton(
                        onClick = { showAddDialog = true },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Entry")
                    }
                } else if (selectedTab == 1) {
                    FloatingActionButton(
                        onClick = { showAddDialog = true },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Entry")
                    }
                }
            }
        }
        if (showAddDialog) {
            AddPhonebookEntryDialog(
                buildingId = building.buildingId,
                onDismiss = { showAddDialog = false },
                onConfirm = { entry ->
                    coroutineScope.launch {
                        sharedViewModel.addPhonebookEntry(context, entry)
                        showAddDialog = false
                        if(entry.type == "emergency") {
                            snackbarHostState.showSnackbar(context.getString(R.string.insert_emergency_phone_book_successfully))
                        } else {
                            snackbarHostState.showSnackbar(context.getString(R.string.insert_phone_book_successfully))
                        }
                    }
                }
            )
        }
    }

    @Composable
    fun PhonebookEntryItem(entry: PhonebookEntry, permissionLevel: PermissionLevel) {
        val context = LocalContext.current
        var showDeleteDialog by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .border(
                    width = 0.7.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                ),
            color = Color.Transparent,
            shadowElevation = 0.dp,
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .clickable {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = "tel:${entry.phoneNumber}".toUri()
                        }
                        context.startActivity(intent)
                    }
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(entry.name, style = MaterialTheme.typography.bodyLarge)
                    Text(entry.phoneNumber, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (permissionLevel >= PermissionLevel.FULL) {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = {
                    Text(
                        text = context.getString(R.string.delete_call),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                text = {
                    Text(context.getString(R.string.are_you_sure))
                },
                confirmButton = {
                    TextButton(onClick = {
                        // Your SharedViewModel's delete function here:
                        coroutineScope.launch {
                         sharedViewModel.deletePhonebookEntry(context, entry)
                        showDeleteDialog = false
                            }
                    }) {
                        Text(context.getString(R.string.delete), style = MaterialTheme.typography.bodyLarge)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(context.getString(R.string.cancel), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            )
        }
    }
    @Composable
    fun AddPhonebookEntryDialog(
        buildingId: Long,
        onDismiss: () -> Unit,
        onConfirm: (PhonebookEntry) -> Unit
    ) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
//        var type by remember { mutableStateOf("resident") }
        var context = LocalContext.current
        var selectedType by remember { mutableStateOf(context.getString(R.string.tenants)) }
//        var selectedTypeList by remember { mutableStateOf("resident") }

        val type = when (selectedType) {
            context.getString(R.string.tenants) -> "resident"
            context.getString(R.string.emergency_calls) -> "emergency"
            else -> "resident"
        }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(context.getString(R.string.add_new_phone), style = MaterialTheme.typography.bodyLarge) },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(context.getString(R.string.name), style = MaterialTheme.typography.bodyLarge) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = {
                            Text(
                                context.getString(R.string.phone_number),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        ChipGroupShared(
                            selectedItems = listOf(selectedType),
                            onSelectionChange = { newSelection ->
                                // Since singleSelection = true, newSelection will be a single-item list
                                if (newSelection.isNotEmpty()) {
                                    selectedType = newSelection.first()
                                }
                            },
                            items = listOf(context.getString(R.string.tenants), context.getString(R.string.emergency_calls)),
                            label = context.getString(R.string.select_type),
                            singleSelection = true
                        )

                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm(
                            PhonebookEntry(
                                buildingId = buildingId,
                                name = name,
                                phoneNumber = phone,
                                type = type
                            )
                        )
                    }
                ) { Text(context.getString(R.string.insert), style = MaterialTheme.typography.bodyLarge) }
            },
            dismissButton = {
                Button(onClick = onDismiss) { Text(context.getString(R.string.cancel), style = MaterialTheme.typography.bodyLarge) }
            }
        )
    }

    @Composable
    fun OwnersTab(building: Buildings, sharedViewModel: SharedViewModel) {
        val context = LocalContext.current
        val ownerApi = remember { Owner() }
        val unitsApi = remember { com.example.delta.volley.Units() }

        var showOwnerDialog by remember { mutableStateOf(false) }
        var owners by remember { mutableStateOf<List<Owners>>(emptyList()) }
        var ownersWithUnits by remember { mutableStateOf<List<Owner.OwnerWithUnitsDto>>(emptyList()) }
        var loading by remember { mutableStateOf(false) }

        var units by remember { mutableStateOf<List<Units>>(emptyList()) }
        var unitsLoading by remember { mutableStateOf(false) }

        val dangSumsMap = remember(ownersWithUnits) {
            ownersWithUnits
                .flatMap { it.units }
                .groupBy { it.unit.unitId }
                .mapValues { (_, list) -> list.sumOf { it.dang } }
        }

        fun loadOwners() {
            loading = true
            ownerApi.getOwnersWithUnitsByBuilding(
                context = context,
                buildingId = building.buildingId,
                onSuccess = { list ->
                    ownersWithUnits = list
                    owners = list.map { it.owner }
                    loading = false
                },
                onError = {
                    loading = false
                    Toast.makeText(
                        context,
                        it.message ?: context.getString(R.string.failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }

        LaunchedEffect(building.buildingId) {
            loadOwners()
        }

        LaunchedEffect(building.buildingId) {
            unitsLoading = true
            unitsApi.fetchUnitsForBuilding(
                context = context,
                buildingId = building.buildingId,
                onSuccess = { list ->
                    Log.d("list", list.toString())
                    units = list
                    unitsLoading = false
                },
                onError = { e ->
                    unitsLoading = false
                    Toast.makeText(
                        context,
                        e.message ?: context.getString(R.string.failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (loading && owners.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ownersWithUnits) { ownerDto ->
                        OwnerItem(
                            owner = ownerDto,
                            sharedViewModel = sharedViewModel,
                            onDelete = {
                                ownerApi.deleteOwner(
                                    context = context,
                                    ownerId = ownerDto.owner.ownerId,
                                    onSuccess = {
                                        owners = owners.filterNot { it.ownerId == ownerDto.owner.ownerId }
                                        ownersWithUnits = ownersWithUnits.filterNot {
                                            it.owner.ownerId == ownerDto.owner.ownerId
                                        }
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.success_delete),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onError = { e ->
                                        Toast.makeText(
                                            context,
                                            e.message ?: context.getString(R.string.failed),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            },
                            activity = context.findActivity(),
                            buildingId = building.buildingId
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = { showOwnerDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Filled.Add, "Add")
            }

            if (showOwnerDialog) {
                OwnerDialog(
                    units = units,
                    dangSums = dangSumsMap,
                    onDismiss = { showOwnerDialog = false },
                    onAddOwner = { newOwner, selectedUnits, isManager, selectedBuilding ->
                        ownerApi.insertOwnerWithUnits(
                            context = context,
                            owner = newOwner,
                            units = selectedUnits,
                            isManager = isManager,
                            buildingId = building.buildingId,
                            onSuccess = { _ ->
                                showOwnerDialog = false

                                Toast.makeText(
                                    context,
                                    context.getString(R.string.insert_owner_successfully),
                                    Toast.LENGTH_SHORT
                                ).show()

                                // Reload full owners + units from server
                                loadOwners()
                            },
                            onError = { e ->
                                Toast.makeText(
                                    context,
                                    e.message ?: context.getString(R.string.failed),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                )
            }
        }
    }



    @Composable
    private fun EarningsSection(
        earnings: List<Earnings>,
        onAddEarnings: () -> Unit
    ) {
        var context = LocalContext.current
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 80.dp),
                state = rememberLazyListState()
            ) {
                itemsIndexed(earnings) { index, earning ->
                    EarningsItem(earnings = earning)
                    if (index < earnings.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }

                if (earnings.isEmpty()) {
                    item {
                        Text(
                            text = LocalContext.current.getString(R.string.no_earnings_recorded),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            ExtendedFloatingActionButton(
                onClick = onAddEarnings,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                icon = { Icon(Icons.Default.Add, "Add Earnings") },
                text = {
                    Text(
                        text = context.getString(R.string.add_income),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
//                containerColor = Color(context.getColor(R.color.secondary_color)),
//                contentColor = Color(context.getColor(R.color.white))
            )
        }
    }
}



    @Composable
    fun EarningsItem(earnings: Earnings) {
        var context = LocalContext.current
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "${context.getString(R.string.title)}: ${earnings.earningsName}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${context.getString(R.string.amount)}: ${
                        formatNumberWithCommas(
                            earnings.amount
                        )
                    }",
                    style = MaterialTheme.typography.bodyLarge
                )

            }
        }
    }

@Composable
fun EarningsDialog(
    building: Buildings,
    onDismiss: () -> Unit,
    onConfirm: (Earnings) -> Unit,
    sharedViewModel: SharedViewModel
) {
    var earningsName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf<Period?>(Period.MONTHLY) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    val context = LocalContext.current
    var showAddNewEarningNameDialog by remember { mutableStateOf(false) }
    val defaultEarnings by sharedViewModel.getFixedEarnings()
        .collectAsState(initial = emptyList())

    val coroutineScope = rememberCoroutineScope()
    // Add "Add New" option
    val earningsList = remember(defaultEarnings) {
        defaultEarnings + Earnings(
            earningsId = -1, // Special ID to identify "Add New" option
            earningsName = context.getString(R.string.add_new_earning),
            amount = 0.0,
            period = Period.MONTHLY,
            buildingId = null, startDate =  startDate, endDate = endDate,
        )
    }
    var selectedEarning by remember { mutableStateOf(
        Earnings(
            earningsId = -1L,
            earningsName = earningsName,
            amount = 0.0,
            period = Period.MONTHLY,
            buildingId = building.buildingId,
            startDate = startDate,
            endDate = endDate
        )
    ) }

    var showEarningForm by remember { mutableStateOf(false) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }


    // Validation - all mandatory fields
    val isValid = remember(
        earningsName,
        amount,
        selectedPeriod,
        startDate,
        endDate
    ) {
        val earningNameValid = earningsName.isNotBlank()
        val amountValid = amount.isNotBlank() && amount.toDoubleOrNull()?.let { it > 0.0 } == true
        val periodValid = selectedPeriod != null
        val isPeriodNone = selectedPeriod == Period.NONE
        val isEndDateValid = if (isPeriodNone) true else endDate.isNotBlank()
        val startDateValid = startDate.isNotBlank()
        val endDateValid = endDate.isNotBlank()

        earningNameValid && amountValid && periodValid && startDateValid && isEndDateValid
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "${context.getString(R.string.add_new_earning)} ${building.name}",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = earningsName,
                        onValueChange = { newName ->
                            earningsName = newName
                        },
                        label = { Text(context.getString(R.string.earning_title)) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it.filter { ch -> ch.isDigit() } },
                        label = { Text(context.getString(R.string.amount), style = MaterialTheme.typography.bodyLarge) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    val amountVal = amount.toLongOrNull() ?: 0L
                    val amountInWords =
                        NumberCommaTransformation().numberToWords(context, amountVal)
                    Text(
                        text = "$amountInWords ${context.getString(R.string.toman)}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    ExposedDropdownMenuBoxExample(
                        sharedViewModel = sharedViewModel,
                        items = Period.entries,
                        selectedItem = selectedPeriod,
                        onItemSelected = { selectedPeriod = it },
                        label = context.getString(R.string.period),
                        modifier = Modifier.fillMaxWidth(),
                        itemLabel = { it.getDisplayName(context) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { newValue -> startDate = newValue },
                        label = { Text(if (selectedPeriod != Period.NONE)
                        {context.getString(R.string.start_date)} else {context.getString(R.string.date)}
                        ) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) showStartDatePicker = true
                            },
                        readOnly = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // End Date picker field
                    if (selectedPeriod != Period.NONE) {
                        OutlinedTextField(
                            value = endDate,
                            onValueChange = { newValue -> endDate = newValue },
                            label = { Text(context.getString(R.string.end_date)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) showEndDatePicker = true
                                },
                            readOnly = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Date pickers
            if (showStartDatePicker) {
                PersianDatePickerDialogContent(
                    sharedViewModel = sharedViewModel,
                    onDateSelected = { selected ->
                        startDate = selected
                        showStartDatePicker = false
                    },
                    onDismiss = { showStartDatePicker = false },
                    context = context
                )
            }

            if (showEndDatePicker) {
                PersianDatePickerDialogContent(
                    sharedViewModel = sharedViewModel,
                    onDateSelected = { selected ->
                        endDate = selected
                        showEndDatePicker = false
                    },
                    onDismiss = { showEndDatePicker = false },
                    context = context
                )
            }

            // Optionally show form for adding new earning if needed
            if (showEarningForm) {
                AddNewEarningNameDialog(
                    buildingId = building.buildingId,
                    onDismiss = { showAddNewEarningNameDialog = false },
                    onSave = { earning ->
                        coroutineScope.launch {
                            sharedViewModel.insertNewEarnings(earning)
                            showAddNewEarningNameDialog = false
                        }
                    },
                    earningNameExists = { bId, cName ->
                        sharedViewModel.earningNameExists(bId, cName).first()
                    }
                )
            }
        },
        confirmButton = {
            Button(
                enabled = isValid,
                onClick = {
                    val newEarning = Earnings(
                        earningsId = sharedViewModel.selectedEarnings?.earningsId ?: 0,
                        buildingId = building.buildingId,
                        earningsName = earningsName,
                        amount = amount.persianToEnglishDigits().toDoubleOrNull() ?: 0.0,
                        period = selectedPeriod ?: Period.MONTHLY,
                        startDate = startDate,
                        endDate = endDate
                    )
                    onConfirm(newEarning)
                }
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
}


fun formatNumberWithCommas(number: Double): String {
    return NumberFormat.getNumberInstance(Locale.US).format(number)
}

@Composable
fun AddCapitalCostDialog(
    buildingId: Long,
    sharedViewModel: SharedViewModel,
    onDismiss: () -> Unit,
    onSave: (
        Costs,
        String,
        Period,
        CalculateMethod,
        CalculateMethod,
        Responsible,
        List<Long>,
        List<Long>,
        String,
        String
    ) -> Unit
) {
    val fixedFundType = FundType.CAPITAL
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val owners by sharedViewModel.getOwnersForBuilding(buildingId)
        .collectAsState(initial = emptyList())


    // States
    var selectedCost by remember { mutableStateOf<Costs?>(null) }
    var totalAmount by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf<Period?>(Period.MONTHLY) }
    var selectedResponsible by remember { mutableStateOf(Responsible.OWNER.getDisplayName(context)) }
    var selectedCalculateMethod by remember { mutableStateOf(context.getString(R.string.fixed)) }
    var selectedUnitCalculateMethod by remember { mutableStateOf(context.getString(R.string.fixed)) }
    val selectedUnits = remember { mutableStateListOf<Units>() }
    val selectedOwners = remember { mutableStateListOf<Owners>() }
    var dueDate by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showAddNewCostNameDialog by remember { mutableStateOf(false) }
    var isChargeableCost by remember { mutableStateOf(false) }
    val snackBarHostState = remember { SnackbarHostState() }
    // If no cost selected init
    LaunchedEffect(Unit) {
        if (selectedCost == null) {
            selectedCost = Costs(
                costId = -1L,
                buildingId = buildingId,
                costName = "",
                tempAmount = 0.0,
                period = Period.NONE,
                calculateMethod = CalculateMethod.EQUAL,
                paymentLevel = PaymentLevel.BUILDING,
                responsible = Responsible.OWNER,
                fundType = fixedFundType,
                dueDate = ""
            )
        }
    }

    // Map display strings to enums for responsible & calculate methods
    val responsibleEnum = when (selectedResponsible) {
        Responsible.OWNER.getDisplayName(context) -> Responsible.OWNER
        Responsible.TENANT.getDisplayName(context) -> Responsible.TENANT
        else -> Responsible.OWNER
    }
    val calculateMethod = when (selectedCalculateMethod) {
        CalculateMethod.EQUAL.getDisplayName(context) -> CalculateMethod.EQUAL
        CalculateMethod.DANG.getDisplayName(context) -> CalculateMethod.DANG
        CalculateMethod.AREA.getDisplayName(context) -> CalculateMethod.AREA
        else -> CalculateMethod.EQUAL
    }
    val calculateUnitMethod = when (selectedUnitCalculateMethod) {
        CalculateMethod.EQUAL.getDisplayName(context) -> CalculateMethod.EQUAL
        CalculateMethod.DANG.getDisplayName(context) -> CalculateMethod.PEOPLE
        CalculateMethod.AREA.getDisplayName(context) -> CalculateMethod.AREA
        else -> CalculateMethod.EQUAL
    }

    // Validation for mandatory fields
    val isValid = remember(
        selectedCost,
        totalAmount,
        dueDate
    ) {
        selectedCost != null &&
                !selectedCost!!.costName.isBlank() &&
                totalAmount.isNotBlank() &&
                totalAmount.toDoubleOrNull()?.let { it > 0.0 } == true &&
                dueDate.isNotBlank()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = context.getString(R.string.add_new_cost),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            LazyColumn {
                item {
                    OutlinedTextField(
                        value = selectedCost?.costName ?: "",
                        onValueChange = { newName ->
                            if (!isChargeableCost && selectedCost != null) {
                                selectedCost = selectedCost!!.copy(costName = newName)
                            }
                        },
                        label = { Text(context.getString(R.string.cost_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        enabled = !isChargeableCost  // optionally disable input if chargeable
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = totalAmount,
                        onValueChange = {
                            if (!isChargeableCost) {
                                totalAmount = it.filter { ch -> ch.isDigit() }
                            }
                        },
                        label = { Text(context.getString(R.string.amount)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        enabled = !isChargeableCost
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val amountVal = totalAmount.toLongOrNull() ?: 0L
                    val amountInWords =
                        NumberCommaTransformation().numberToWords(context, amountVal)
                    Text(
                        text = "$amountInWords ${context.getString(R.string.toman)}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { newValue -> dueDate = newValue },
                        label = { Text(context.getString(R.string.due)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) showDatePicker = true
                            },
                        readOnly = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Owners chip group - only for Capital fund
//                    ChipGroupOwners(
//                        selectedOwners = selectedOwners,
//                        onSelectionChange = { newSelection ->
//                            selectedOwners.clear()
//                            selectedOwners.addAll(newSelection)
//                            sharedViewModel.selectedOwners.clear()
//                            sharedViewModel.selectedOwners.addAll(newSelection)
//                        },
//                        owners = owners,
//                        label = context.getString(R.string.owners)
//                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = isValid,
                onClick = {
                    val cost = selectedCost ?: return@Button
                    onSave(
                        cost,
                        totalAmount,
                        selectedPeriod ?: Period.NONE,
                        calculateMethod,
                        calculateUnitMethod,
                        responsibleEnum,
                        selectedUnits.map { it.unitId },
                        selectedOwners.map { it.ownerId },
                        dueDate,
                        context.getString(R.string.insert_capital_successfully)
                    )
                }
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
            Spacer(modifier = Modifier.width(8.dp))
        }
    )

    // Nested DatePicker and AddNewCostNameDialog as you already have
    if (showAddNewCostNameDialog) {
        AddNewCostNameDialog(
            buildingId = buildingId,
            onDismiss = { showAddNewCostNameDialog = false },
            onSave = { newCostName ->
                coroutineScope.launch {
                    val newCost = Costs(
                        buildingId = buildingId,
                        costName = newCostName,
                        tempAmount = 0.0,
                        period = Period.NONE,
                        calculateMethod = CalculateMethod.EQUAL,
                        paymentLevel = PaymentLevel.BUILDING,
                        responsible = Responsible.OWNER,
                        fundType = fixedFundType,
                        dueDate = dueDate
                    )
                    sharedViewModel.insertNewCost(newCost,
                        onSuccess = {
                            sharedViewModel.insertCostToServer (context, listOf(newCost),               // ⬅️ تبدیل به لیست یک‌عضوی
                                emptyList<Debts>(),
                                onSuccess = {
                                    coroutineScope.launch {
                                        snackBarHostState.showSnackbar(context.getString(R.string.charge_calcualted_successfully))
                                    }
                                }, onError = {
                                    coroutineScope.launch {
                                        snackBarHostState.showSnackbar(context.getString(R.string.failed))
                                    }
                                })
                        })
                    showAddNewCostNameDialog = false
                    selectedCost = newCost
                    totalAmount = "0"
                }
            },
            checkCostNameExists = { bId, cName ->
                sharedViewModel.checkCostNameExists(bId, cName).first()
            }
        )
    }
    if (showDatePicker) {
        PersianDatePickerDialogContent(
            sharedViewModel = sharedViewModel,
            onDateSelected = { selected ->
                dueDate = selected
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
            context = context
        )
    }
}

@Composable
fun AddOperationalCostDialog(
    buildingId: Long,
    sharedViewModel: SharedViewModel,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val fixedFundType = FundType.OPERATIONAL
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

//    val bulk = Building().fetchBuildingById(context, buildingId, onSuccess = {}, onError = {})
//    // Load necessary data (active units, owners, costs) same as before but filtered for capital if you want
//    val activeUnits by sharedViewModel.getUnitsForBuilding(buildingId)
//        .collectAsState(initial = emptyList())
//    val owners by sharedViewModel.getOwnersForBuilding(buildingId)
//        .collectAsState(initial = emptyList())
//    val defaultChargedCosts by sharedViewModel.getChargesCostsWithNullBuildingId()
//        .collectAsState(emptyList())
//    var dueDate by remember { mutableStateOf("") }
//    var success by remember { mutableStateOf(false) }
//    val chargeableCosts by sharedViewModel.getCostsForBuildingWithChargeFlagAndFiscalYear(
//        buildingId,
//        dueDate.split("/")[0]
//    )
//        .collectAsState(emptyList())
//    Log.d("chargeableCosts", chargeableCosts.toString())
//    val operationalFund by sharedViewModel.getOperationalOrCapitalFundBalance(
//        buildingId = buildingId,
//        fundType = FundType.OPERATIONAL
//    ).collectAsState(initial = 0)
    val funds by sharedViewModel.fundsForBuilding.collectAsState()

    LaunchedEffect(buildingId) {
        sharedViewModel.loadFundsForBuilding(context, buildingId)
    }

    val operationalFund = remember(funds) {
        funds.firstOrNull { it.fundType == FundType.OPERATIONAL }?.balance ?: 0.0
    }
    var dueDate by remember { mutableStateOf("") }

    val fiscalYear = remember(dueDate) {
        dueDate.takeIf { it.contains("/") }?.split("/")?.getOrNull(0)
    }

    val overviewState by produceState<BuildingFullDto?>(initialValue = null, key1 = buildingId, key2 = fiscalYear) {
        value = sharedViewModel.loadBuildingOverview(
            context = context,
            buildingId = buildingId,
            fiscalYear = fiscalYear
        )
    }

    val activeUnits        = overviewState?.units ?: emptyList()
    val owners             = overviewState?.owners ?: emptyList()
    val defaultChargedCosts= overviewState?.defaultChargeCosts ?: emptyList()
    val chargeableCosts    = overviewState?.chargeCostsForYear ?: emptyList()
    val costsWithAddNew = defaultChargedCosts + listOf(
        Costs(
            costId = -1,
            buildingId = buildingId,
            costName = context.getString(R.string.add_new_cost),
            tempAmount = 0.0,
            period = Period.NONE,
            calculateMethod = CalculateMethod.EQUAL,
            paymentLevel = PaymentLevel.BUILDING,
            responsible = Responsible.OWNER,
            fundType = fixedFundType,
            dueDate = ""
        )
    )

    // States
    var selectedCost by remember { mutableStateOf<Costs?>(null) }
    var totalAmount by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf<Period?>(Period.MONTHLY) }
    var selectedResponsible by remember { mutableStateOf(Responsible.TENANT.getDisplayName(context)) }
    var selectedCalculateMethod by remember { mutableStateOf(context.getString(R.string.fixed)) }
    var selectedUnitCalculateMethod by remember { mutableStateOf(context.getString(R.string.fixed)) }
    val selectedUnits = remember { mutableStateListOf<Units>() }
    val selectedOwners = remember { mutableStateListOf<Owners>() }
    var showDatePicker by remember { mutableStateOf(false) }
    var showAddNewCostNameDialog by remember { mutableStateOf(false) }
    var isChargeableCost by remember { mutableStateOf(false) }
    val snackBarHostState = remember { SnackbarHostState() }

    // If no cost selected init
    LaunchedEffect(Unit) {
        if (selectedCost == null) {
            selectedCost = Costs(
                costId = -1L,
                buildingId = buildingId,
                costName = "",
                tempAmount = 0.0,
                period = Period.NONE,
                calculateMethod = CalculateMethod.EQUAL,
                paymentLevel = PaymentLevel.BUILDING,
                responsible = Responsible.OWNER,
                fundType = fixedFundType,
                dueDate = ""
            )
        }
    }

    // Map display strings to enums for responsible & calculate methods
    val responsibleEnum = when (selectedResponsible) {
        Responsible.OWNER.getDisplayName(context) -> Responsible.OWNER
        Responsible.TENANT.getDisplayName(context) -> Responsible.TENANT
        else -> Responsible.OWNER
    }
    val calculateMethod = when (selectedCalculateMethod) {
        CalculateMethod.EQUAL.getDisplayName(context) -> CalculateMethod.EQUAL
        CalculateMethod.DANG.getDisplayName(context) -> CalculateMethod.DANG
        CalculateMethod.AREA.getDisplayName(context) -> CalculateMethod.AREA
        else -> CalculateMethod.EQUAL
    }
    val calculateUnitMethod = when (selectedUnitCalculateMethod) {
        CalculateMethod.EQUAL.getDisplayName(context) -> CalculateMethod.EQUAL
        CalculateMethod.DANG.getDisplayName(context) -> CalculateMethod.PEOPLE
        CalculateMethod.AREA.getDisplayName(context) -> CalculateMethod.AREA
        else -> CalculateMethod.EQUAL
    }

    val isValid = remember(
        selectedCost,
        totalAmount,
//        selectedResponsible,
//        selectedCalculateMethod,
        selectedUnitCalculateMethod,
        dueDate,
//        selectedUnits.toList(),  // important to copy to detect changes inside list
//        selectedOwners.toList()
    ) {

        val isCostNameValid = selectedCost != null && !selectedCost!!.costName.isBlank()
        val isAmountValid =
            totalAmount.isNotBlank() && totalAmount.toDoubleOrNull()?.let { it > 0.0 } == true
        val isResponsibleValid = selectedResponsible.isNotBlank()
        val isCalculateMethodValid = selectedCalculateMethod.isNotBlank()
        val isUnitCalculateMethodValid = selectedUnitCalculateMethod.isNotBlank()
        val isDueDateValid = dueDate.isNotBlank()

        val responsibleIsOwner = selectedResponsible == Responsible.OWNER.getDisplayName(context)
        val responsibleIsTenant = selectedResponsible == Responsible.TENANT.getDisplayName(context)

        val isOwnersSelected = selectedOwners.isNotEmpty()
        val isUnitsSelected = selectedUnits.isNotEmpty()

        isCostNameValid &&
                isAmountValid &&
//                isResponsibleValid &&
//                isCalculateMethodValid &&
                isUnitCalculateMethodValid &&
                isDueDateValid
//                &&
//                (
//                        // If owner responsible, owners must be selected
//                        (responsibleIsOwner && isOwnersSelected)
//                                // If tenant responsible, units must be selected
//                                || (responsibleIsTenant && isUnitsSelected)
//                        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = context.getString(R.string.add_new_cost),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            LazyColumn {

                item {
                    // Cost dropdown or add new cost (filtered to Capital fund type)
                    ExposedDropdownMenuBoxExample(
                        sharedViewModel = sharedViewModel,
                        items = costsWithAddNew.filter { it.fundType == fixedFundType },
                        selectedItem = selectedCost,
                        onItemSelected = {
                            if (it.costId == -1L) showAddNewCostNameDialog = true
                            else {
                                selectedCost = it
                                totalAmount = it.tempAmount.toLong().toString()
                                selectedPeriod = it.period
                                isChargeableCost =
                                    chargeableCosts.any { cc -> cc.costName == it.costName }
                            }
                        },
                        label = context.getString(R.string.cost_name),
                        modifier = Modifier.fillMaxWidth(),
                        itemLabel = { it.costName }
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = totalAmount,
                        onValueChange = {
//                            if (!isChargeableCost) {
                            totalAmount = it.filter { ch -> ch.isDigit() }
//                            }
                        },
                        label = { Text(context.getString(R.string.amount)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val amountVal = totalAmount.toLongOrNull() ?: 0L
                    val amountInWords =
                        NumberCommaTransformation().numberToWords(context, amountVal)
                    Text(
                        text = "$amountInWords ${context.getString(R.string.toman)}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { },
                        label = { Text(context.getString(R.string.due)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) showDatePicker = true
                            },
                        readOnly = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
//                    ChipGroupShared(
//                        selectedItems = listOf(selectedResponsible),
//                        onSelectionChange = { newSelection ->
//                            if (newSelection.isNotEmpty()) {
//                                selectedResponsible = newSelection.first()
//                            }
//                        },
//                        items = listOf(
//                            context.getString(R.string.owners),
//                            context.getString(R.string.tenants)
//                        ),
//                        modifier = Modifier.padding(vertical = 8.dp),
//                        label = context.getString(R.string.responsible),
//                        singleSelection = true
//                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (selectedResponsible == context.getString(R.string.tenants)) {
                        ChipGroupUnits(
                            selectedUnits = selectedUnits,
                            onSelectionChange = { newSelection ->
                                selectedUnits.clear()
                                selectedUnits.addAll(newSelection)
                                sharedViewModel.selectedUnits.clear()
                                sharedViewModel.selectedUnits.addAll(newSelection)
                            },
                            units = activeUnits,
                            label = context.getString(R.string.units),
                            context = context
                        )
                        Spacer(modifier = Modifier.height(8.dp))


                    }
                    ChipGroupShared(
                        selectedItems = listOf(selectedUnitCalculateMethod),
                        onSelectionChange = { newSelection ->
                            if (newSelection.isNotEmpty()) {
                                selectedUnitCalculateMethod = newSelection.first()
                            }
                        },
                        items = listOf(
                            context.getString(R.string.area),
                            context.getString(R.string.people),
                            context.getString(R.string.fixed)
                        ),
                        modifier = Modifier.padding(vertical = 8.dp),
                        label = context.getString(R.string.acount_base),
                        singleSelection = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
//                    else {
//                        ChipGroupOwners(
//                            selectedOwners = selectedOwners,
//                            onSelectionChange = { newSelection ->
//                                selectedOwners.clear()
//                                selectedOwners.addAll(newSelection)
//                                sharedViewModel.selectedOwners.clear()
//                                sharedViewModel.selectedOwners.addAll(newSelection)
//                            },
//                            owners = owners,
//                            label = context.getString(R.string.owners)
//                        )
//                        Spacer(modifier = Modifier.height(8.dp))
//
//                        ChipGroupShared(
//                            selectedItems = listOf(selectedCalculateMethod),
//                            onSelectionChange = { newSelection ->
//                                if (newSelection.isNotEmpty()) {
//                                    selectedCalculateMethod = newSelection.first()
//                                }
//                            },
//                            items = listOf(
//                                context.getString(R.string.area),
//                                context.getString(R.string.dang),
//                                context.getString(R.string.fixed)
//                            ),
//                            modifier = Modifier.padding(vertical = 8.dp),
//                            label = context.getString(R.string.acount_base),
//                            singleSelection = true
//                        )
//                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = isValid,
                onClick = {
                    val cost = selectedCost ?: return@Button
                    Log.d("selectedResponsible", selectedResponsible.toString())
                    if (selectedResponsible == Responsible.TENANT.getDisplayName(context) && isChargeableCost) {
                        coroutineScope.launch {
                            val amountDouble = totalAmount.toDoubleOrNull() ?: 0.0
                            if (amountDouble <= (operationalFund.toDouble())) {
                                val cost = Costs(
                                    buildingId = buildingId,
                                    costName = cost.costName,
                                    chargeFlag = false,
                                    fundType = FundType.OPERATIONAL,
                                    responsible = Responsible.TENANT,
                                    paymentLevel = PaymentLevel.UNIT,
                                    calculateMethod = CalculateMethod.EQUAL,
                                    period = Period.YEARLY,
                                    tempAmount = amountDouble,
                                    dueDate = dueDate,
                                    invoiceFlag = true
                                )
                                sharedViewModel.insertNewCost(
                                    cost,
                                    onSuccess = {
                                        sharedViewModel.insertCostToServer (context, listOf(cost),               // ⬅️ تبدیل به لیست یک‌عضوی
                                            emptyList<Debts>(),
                                            onSuccess = {
                                                coroutineScope.launch {
                                                    snackBarHostState.showSnackbar(context.getString(R.string.charge_calcualted_successfully))
                                                }
                                            }, onError = {
                                                coroutineScope.launch {
                                                    snackBarHostState.showSnackbar(context.getString(R.string.failed))
                                                }
                                        })
                                    }
                                )
                                Fund().decreaseOperationalFundOnServer(
                                    context = context,
                                    buildingId = buildingId,
                                    amountDouble,
                                    FundType.OPERATIONAL,
                                    onSuccess = {
                                        onSave(context.getString(R.string.fund_decreased_successfully))
//                                        success = true
                                    },
                                    onError = {
//                                        success = false
                                    }
                                )
//                                success = sharedViewModel.decreaseOperationalFund(
//                                    buildingId,
//                                    amountDouble,
//                                    FundType.OPERATIONAL,
//                                    onSuccess = {},
//                                    onError = {}
//                                )


                            } else {
//                                success = false

                            }
                        }
                    } else {
                        sharedViewModel.insertDebtPerNewCost(
                            buildingId = buildingId,
                            amount = totalAmount,
                            name = cost.costName,
                            period = selectedPeriod ?: Period.NONE,
                            fundType = FundType.OPERATIONAL,
                            paymentLevel = cost.paymentLevel,
                            calculateMethod = calculateMethod,
                            calculatedUnitMethod = calculateUnitMethod,
                            responsible = responsibleEnum,
                            dueDate = dueDate,
                            selectedUnitIds = selectedUnits.map { it.unitId },
                            selectedOwnerIds = selectedOwners.map { it.ownerId },
                            onSuccess = { costs, debts ->
                                sharedViewModel.insertCostToServer (context, costs, debts,
                                    onSuccess = {
                                        coroutineScope.launch {
                                            snackBarHostState.showSnackbar(context.getString(R.string.charge_calcualted_successfully))
                                        }
                                    }, onError = {
                                        coroutineScope.launch {
                                            snackBarHostState.showSnackbar(context.getString(R.string.failed))
                                        }
                                })
                            },
                            onError = {

                            }
                        )
                        onSave(context.getString(R.string.cost_insert_and_pending))
                    }
                }
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
            Spacer(modifier = Modifier.width(8.dp))
        }
    )

// Nested DatePicker and AddNewCostNameDialog as you already have
    if (showAddNewCostNameDialog) {
        AddNewCostNameDialog(
            buildingId = buildingId,
            onDismiss = { showAddNewCostNameDialog = false },
            onSave = { newCostName ->
                coroutineScope.launch {
                    val newCost = Costs(
                        buildingId = buildingId,
                        costName = newCostName,
                        tempAmount = 0.0,
                        period = Period.NONE,
                        calculateMethod = CalculateMethod.EQUAL,
                        paymentLevel = PaymentLevel.BUILDING,
                        responsible = Responsible.OWNER,
                        fundType = fixedFundType,
                        dueDate = dueDate
                    )
//                    sharedViewModel.insertNewCost(newCost)
                    showAddNewCostNameDialog = false
                    selectedCost = newCost
                    totalAmount = "0"
                }
            },
            checkCostNameExists = { bId, cName ->
                sharedViewModel.checkCostNameExists(bId, cName).first()
            }
        )
    }
    if (showDatePicker) {
        PersianDatePickerDialogContent(
            sharedViewModel = sharedViewModel,
            onDateSelected = { selected ->
                dueDate = selected
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
            context = context
        )
    }
}



@Composable
fun AddNewCostNameDialog(
    buildingId: Long,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    checkCostNameExists: suspend (buildingId: Long, costName: String) -> Boolean
) {
    val context = LocalContext.current
    var costName by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = context.getString(R.string.add_new_cost),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = costName,
                    onValueChange = { costName = it },
                    label = { Text(context.getString(R.string.cost_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = onDismiss) {
                        Text(text = context.getString(R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        enabled = costName.isNotBlank(),
                        onClick = {
                            coroutineScope.launch {
                                val exists = checkCostNameExists(buildingId, costName.trim())
                                if (exists) {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.cost_name_exists),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    onSave(costName.trim())
                                }
                            }
                        }
                    ) {
                        Text(text = context.getString(R.string.insert))
                    }
                }
            }
        }
    }
}


@Composable
fun AddNewEarningNameDialog(
    buildingId: Long,
    onDismiss: () -> Unit,
    onSave: (Earnings) -> Unit,
    earningNameExists: suspend (buildingId: Long, earningName: String) -> Boolean
) {
    val context = LocalContext.current
    var earningName by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = context.getString(R.string.add_new_earning),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = earningName,
                    onValueChange = { earningName = it },
                    label = { Text(context.getString(R.string.earning_title)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = onDismiss) {
                        Text(text = context.getString(R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        enabled = earningName.isNotBlank(),
                        onClick = {
                            coroutineScope.launch {
                                val exists = earningNameExists(buildingId, earningName.trim())
                                if (exists) {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.earning_name_exists),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    val newEarning = Earnings(
                                        earningsName = earningName.trim(),
                                        buildingId = buildingId,
                                        amount = 0.0,
                                        startDate = "",
                                        endDate = "",
                                        period = Period.NONE

                                    )
                                    onSave(newEarning)
                                }
                            }
                        }
                    ) {
                        Text(text = context.getString(R.string.insert))
                    }
                }
            }
        }
    }
}




@Composable
fun BuildingProfileSectionSelector(
    tabs: List<BuildingTabItem>,
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
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.medium
                    ),
                elevation = CardDefaults.cardElevation(if (isSelected) 8.dp else 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),  // padding only vertical - NO horizontal padding
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = when (tab.type) {
                            BuildingTabType.OVERVIEW -> Icons.Default.Info
                            BuildingTabType.OWNERS -> Icons.Default.Person
                            BuildingTabType.UNITS -> Icons.Default.Home
                            BuildingTabType.TENANTS -> Icons.Default.Group
                            BuildingTabType.FUNDS -> Icons.Default.AccountBalanceWallet
                            BuildingTabType.TRANSACTIONS -> Icons.Default.ReceiptLong
                            BuildingTabType.PHONEBOOK_TAB -> Icons.Default.Contacts
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

