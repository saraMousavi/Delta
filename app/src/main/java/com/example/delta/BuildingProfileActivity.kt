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
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.delta.data.entity.BuildingTabItem
import com.example.delta.data.entity.BuildingTabType
import com.example.delta.data.entity.BuildingTypes
import com.example.delta.data.entity.BuildingUsages
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.CityComplexes
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Earnings
import com.example.delta.data.entity.PhonebookEntry
import com.example.delta.data.entity.Units
import com.example.delta.data.entity.User
import com.example.delta.enums.CalculateMethod
import com.example.delta.enums.FundType
import com.example.delta.enums.Gender
import com.example.delta.enums.PaymentLevel
import com.example.delta.enums.Period
import com.example.delta.enums.PermissionLevel
import com.example.delta.enums.Responsible
import com.example.delta.extentions.findActivity
import com.example.delta.factory.BuildingsViewModelFactory
import com.example.delta.init.AuthUtils.AuthUtils.permissionFor
import com.example.delta.init.AuthUtils.AuthorizationFieldsBuildingProfile
import com.example.delta.init.AuthUtils.AuthorizationFieldsHome
import com.example.delta.init.AuthUtils.AuthorizationObjects
import com.example.delta.init.IranianLocations
import com.example.delta.init.NumberCommaTransformation
import com.example.delta.init.Preference
import com.example.delta.init.Validation
import com.example.delta.viewmodel.BuildingsViewModel
import com.example.delta.viewmodel.SharedViewModel
import com.example.delta.volley.Building
import com.example.delta.volley.Building.BuildingFullDto
import com.example.delta.volley.BuildingType
import com.example.delta.volley.BuildingUsage
import com.example.delta.volley.CityComplex
import com.example.delta.volley.Cost
import com.example.delta.volley.Earning
import com.example.delta.volley.Owner
import com.example.delta.volley.Tenant
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.math.roundToLong

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

                    val effectiveRoleId = Preference().getRoleId(context)
                    LaunchedEffect(effectiveRoleId) {
                        if (effectiveRoleId != 0L) {
                            sharedViewModel.loadRolePermissions(context, effectiveRoleId)
                        }
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
                                building = building!!,
                                sharedViewModel = sharedViewModel
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
    fun BuildingProfileScreen(building: Buildings, sharedViewModel: SharedViewModel) {
        val context = LocalContext.current
        val currentRoleId = Preference().getRoleId(context)
        val coroutineScope = rememberCoroutineScope()
        val perms = sharedViewModel.rolePermissions
        val ownerPerm = perms.permissionFor(
            AuthorizationObjects.BUILDING_PROFILE,
            AuthorizationFieldsBuildingProfile.OWNERS_TAB
        )

        val tenantPerm = perms.permissionFor(
            AuthorizationObjects.BUILDING_PROFILE,
            AuthorizationFieldsBuildingProfile.TENANTS_TAB
        )

        val fundPerm = perms.permissionFor(
            AuthorizationObjects.BUILDING_PROFILE,
            AuthorizationFieldsBuildingProfile.FUND_TAB
        )

        val transactionPerm = perms.permissionFor(
            AuthorizationObjects.BUILDING_PROFILE,
            AuthorizationFieldsBuildingProfile.TRANSACTIONS_TAB
        )

        val unitPerm = perms.permissionFor(
            AuthorizationObjects.BUILDING_PROFILE,
            AuthorizationFieldsBuildingProfile.UNITS_TAB
        )

        val phonePerm = perms.permissionFor(
            AuthorizationObjects.BUILDING_PROFILE,
            AuthorizationFieldsBuildingProfile.PHONEBOOK_TAB
        )


        val tabs = listOfNotNull(
            BuildingTabItem(context.getString(R.string.overview), BuildingTabType.OVERVIEW),
            if (currentRoleId==7L || unitPerm == PermissionLevel.WRITE || unitPerm == PermissionLevel.FULL ) {
                BuildingTabItem(context.getString(R.string.units), BuildingTabType.UNITS)
            } else null,

            if (currentRoleId==7L || currentRoleId==9L || ownerPerm == PermissionLevel.WRITE || ownerPerm == PermissionLevel.FULL ) {
                BuildingTabItem(context.getString(R.string.owners), BuildingTabType.OWNERS)
            } else null,
            if (currentRoleId==7L || currentRoleId==10L || tenantPerm == PermissionLevel.WRITE || tenantPerm == PermissionLevel.FULL ) {
                BuildingTabItem(context.getString(R.string.tenants), BuildingTabType.TENANTS)
            } else null,
            if (currentRoleId==7L || fundPerm == PermissionLevel.WRITE || fundPerm == PermissionLevel.FULL ) {
                BuildingTabItem(context.getString(R.string.funds), BuildingTabType.FUNDS)
            } else null,
            if (currentRoleId==7L  || transactionPerm == PermissionLevel.READ || transactionPerm == PermissionLevel.WRITE || transactionPerm == PermissionLevel.FULL ) {
                BuildingTabItem(
                    context.getString(R.string.transaction),
                    BuildingTabType.TRANSACTIONS
                )
            } else null,
            if (currentRoleId==7L || currentRoleId==9L || currentRoleId==10L || phonePerm == PermissionLevel.READ || phonePerm == PermissionLevel.WRITE || phonePerm == PermissionLevel.FULL ) {
                BuildingTabItem(
                    context.getString(R.string.phone_number),
                    BuildingTabType.PHONEBOOK_TAB
                )
            } else null,
        )

        var selectedTab by remember { mutableIntStateOf(0) }
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = building.name,
                            style = MaterialTheme.typography.headlineSmall
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
                when (tabs.getOrNull(selectedTab)?.type) {
                    BuildingTabType.OVERVIEW -> {
                        OverviewTab(
                            sharedViewModel = sharedViewModel,
                            building = building,
                            onUpdateBuilding = { updatedBuilding, selectedCostName, hasCalculatedCharge, fileIds ->
                                Building().updateBuilding(
                                    context = context,
                                    building = updatedBuilding,
                                    selectedCostNames = selectedCostName,
                                    replaceCosts = !hasCalculatedCharge,
                                    fileIds = fileIds,
                                    onSuccess = { serverBuilding ->
                                        coroutineScope.launch {
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.success_update),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    onError = { e ->
                                        coroutineScope.launch {
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
                    BuildingTabType.UNITS -> UnitsTab(sharedViewModel, building)
                    BuildingTabType.OWNERS -> OwnersTab(building, sharedViewModel)
                    BuildingTabType.TENANTS -> TenantsTab(building, sharedViewModel)
                    BuildingTabType.FUNDS -> FundsTab(
                        building = building, sharedViewModel = sharedViewModel,
                        buildingViewModel = buildingViewModel,
                        onOpenCostDetail = { cost, fundType ->

                            coroutineScope.launch {
                                val intent =
                                    Intent(context, CostDetailActivity::class.java).apply {
                                        putExtra("COST_DATA", cost as Parcelable)
                                        putExtra("FUND_TYPE", fundType.ordinal)
                                    }
                                context.startActivity(intent)
                            }
                        })

                    BuildingTabType.TRANSACTIONS -> TransactionHistoryTab(building, sharedViewModel)
                    BuildingTabType.PHONEBOOK_TAB -> PhonebookTab(
                        building,
                        sharedViewModel,
                    )

                    null -> {} // Handle invalid index if needed
                }

            }
        }
    }
}

@Composable
fun OverviewTab(
    sharedViewModel: SharedViewModel,
    building: Buildings,
    onUpdateBuilding: (Buildings, List<String>, Boolean, List<Long>) -> Unit
) {
    val context = LocalContext.current

    var isEditing by remember { mutableStateOf(false) }
    var editableBuilding by remember(building) { mutableStateOf(building) }
    val validation = remember { Validation() }

    val buildingTypes by sharedViewModel.buildingTypes.collectAsState()
    val buildingUsages by sharedViewModel.buildingUsages.collectAsState()
    val cityComplexes by sharedViewModel.cityComplexes.collectAsState()

    val allCosts by sharedViewModel.costsList.collectAsState()
    val chargesCost by sharedViewModel.chargesCost.collectAsState()

    val costApi = remember { Cost() }
    val addNewLabel = context.getString(R.string.addNew)

    var selectedCostNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var allChargeCosts by remember { mutableStateOf<List<Costs>>(emptyList()) }
    val hasCalculatedCharges = chargesCost.any { it.tempAmount != 0.0 }

    val buildingFiles by sharedViewModel.buildingFiles.collectAsState()
    val fileIdsForUpdate = remember(buildingFiles) {
        buildingFiles.map { it.fileId }
    }

    val base = allCosts
        .filter { it.fundType == FundType.OPERATIONAL }
        .filter { it.chargeFlag == true }
        .filter { it.tempAmount == 0.0 }

    val buildingId = building.buildingId

    val defaults = base.filter {
        it.buildingId == 0L && (it.forBuildingId == null || it.forBuildingId == 0L)
    }

    val scoped = base.filter {
        it.forBuildingId == buildingId &&
                (it.buildingId == buildingId || it.buildingId == 0L || it.buildingId == null)
    }

    val pickedPerName = scoped
        .groupBy { it.costName }
        .mapNotNull { (_, list) ->
            list.firstOrNull { it.buildingId == buildingId && it.forBuildingId == buildingId }
                ?: list.firstOrNull { (it.buildingId == 0L || it.buildingId == null) && it.forBuildingId == buildingId }
        }

    allChargeCosts = (pickedPerName + defaults).distinctBy { it.costName }

    Log.d("allChargeCosts", allChargeCosts.toString())
    LaunchedEffect(chargesCost) {
        selectedCostNames = chargesCost
            .filter { it.chargeFlag == true }
            .filter { it.tempAmount == 0.0 }
            .map { it.costName }
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

    LaunchedEffect(Unit) {
        sharedViewModel.loadOverviewData(context = context, buildingId = building.buildingId)
    }

    val buildingTypeNameTrigger = context.getString(R.string.city_complex)

    var showAddCityComplexDialog by remember { mutableStateOf(false) }
    var showBuildingTypeDialog by remember { mutableStateOf(false) }
    var showBuildingUsageDialog by remember { mutableStateOf(false) }

    val serialHasError = remember(editableBuilding.serialNumber) {
        editableBuilding.serialNumber.isNotBlank() &&
                !validation.isValidDeedSerial(editableBuilding.serialNumber)
    }
    val canSave = !serialHasError

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentPadding = PaddingValues(bottom = 72.dp)
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
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (isEditing) {
                                OutlinedTextField(
                                    value = editableBuilding.name,
                                    onValueChange = {
                                        editableBuilding = editableBuilding.copy(name = it)
                                    },
                                    label = { RequiredLabel(context.getString(R.string.building_name)) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = editableBuilding.serialNumber,
                                    onValueChange = {
                                        editableBuilding = editableBuilding.copy(serialNumber = it)
                                    },
                                    label = { Text(context.getString(R.string.building_serial_number)) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = editableBuilding.floorCount.toString(),
                                    onValueChange = {
                                        editableBuilding = editableBuilding.copy(floorCount = it.toInt())
                                    },
                                    label = { RequiredLabel(context.getString(R.string.floor_count)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            } else {
                                Text(
                                    text = "${context.getString(R.string.building_name)}: ${editableBuilding.name}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "${context.getString(R.string.building_serial_number)}: ${editableBuilding.serialNumber}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "${context.getString(R.string.floor_count)}: ${editableBuilding.floorCount}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
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
                                            context.getString(R.string.address),
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Text(
                                    text = "${context.getString(R.string.address)}: ${editableBuilding.street}",
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
                                    IranianLocations.provinces[editableBuilding.province] ?: emptyList()

                                ExposedDropdownMenuBoxExample(
                                    sharedViewModel = sharedViewModel,
                                    items = provinces,
                                    selectedItem = editableBuilding.province,
                                    onItemSelected = { selectedProvince ->
                                        editableBuilding = editableBuilding.copy(province = selectedProvince)
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
                                        editableBuilding = editableBuilding.copy(state = selectedState)
                                    },
                                    label = context.getString(R.string.state),
                                    modifier = Modifier.fillMaxWidth(),
                                    itemLabel = { it }
                                )
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${context.getString(R.string.province)}: ${editableBuilding.province}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "${context.getString(R.string.state)}: ${editableBuilding.state}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }

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
                                    items = buildingTypes
                                        .filter {
                                            it.forBuildingId == null ||
                                                    it.forBuildingId == 0L ||
                                                    it.forBuildingId == building.buildingId
                                        } + BuildingTypes(
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
                                    itemLabel = { it.buildingTypeName },
                                    showDeleteFor = { type ->
                                        val c = buildingTypes.firstOrNull { it.buildingTypeName == type.buildingTypeName }
                                        val bId = c?.forBuildingId ?: 0L
                                        val tempBuildingType = c?.addedBeforeCreateBuilding
                                        bId == building.buildingId || tempBuildingType == true
                                    },
                                    onDeleteRequest = { type ->
                                        BuildingType().deleteBuildingType(
                                            context,
                                            type.buildingTypeId,
                                            onSuccess = {
                                                sharedViewModel.updateBuildingType(buildingTypes - type)
                                            },
                                            onError = {}
                                        )
                                    }
                                )

                                if (selectedBuildingType?.buildingTypeName == buildingTypeNameTrigger) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    ExposedDropdownMenuBoxExample(
                                        sharedViewModel = sharedViewModel,
                                        items = cityComplexes.filter {
                                            it.forBuildingId == null ||
                                                    it.forBuildingId == 0L ||
                                                    it.forBuildingId == building.buildingId
                                        } + CityComplexes(
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
                                        itemLabel = { it.name },
                                        showDeleteFor = { complex ->
                                            val c = cityComplexes.firstOrNull { it.name == complex.name }
                                            val bId = c?.forBuildingId ?: 0L
                                            val tempCityComplex = c?.addedBeforeCreateBuilding
                                            bId == building.buildingId || tempCityComplex == true
                                        },
                                        onDeleteRequest = { cityComplex ->
                                            CityComplex().deleteCityComplex(
                                                context,
                                                cityComplex.complexId,
                                                onSuccess = {
                                                    sharedViewModel.updateCityComplex(cityComplexes - cityComplex)
                                                },
                                                onError = {}
                                            )
                                        }
                                    )
                                }

                                Spacer(Modifier.height(8.dp))

                                ExposedDropdownMenuBoxExample(
                                    sharedViewModel = sharedViewModel,
                                    items = buildingUsages.filter {
                                        it.forBuildingId == null ||
                                                it.forBuildingId == 0L ||
                                                it.forBuildingId == building.buildingId
                                    } + BuildingUsages(
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
                                    itemLabel = { it.buildingUsageName },
                                    showDeleteFor = { usage ->
                                        val c = buildingUsages.firstOrNull { it.buildingUsageName == usage.buildingUsageName }
                                        val bId = c?.forBuildingId ?: 0L
                                        val tempBuildingUsage = c?.addedBeforeCreateBuilding
                                        bId == building.buildingId || tempBuildingUsage == true
                                    },
                                    onDeleteRequest = { usage ->
                                        BuildingUsage().deleteBuildingUsage(
                                            context,
                                            usage.buildingUsageId,
                                            onSuccess = {
                                                sharedViewModel.updateBuildingUsage(buildingUsages - usage)
                                            },
                                            onError = {}
                                        )
                                    }
                                )
                            } else {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${context.getString(R.string.building_type)}: ${selectedBuildingType?.buildingTypeName ?: "-"}",
                                            style = MaterialTheme.typography.bodyLarge
                                        )

                                        Text(
                                            text = "${context.getString(R.string.building_usage)}: ${selectedBuildingUsage?.buildingUsageName ?: "-"}",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                    if (selectedBuildingType?.buildingTypeName == buildingTypeNameTrigger) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "${context.getString(R.string.city_complex_name)}: ${selectedCityComplex?.name ?: "-"}",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

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
                            if (!isEditing) {
                                Text(
                                    text = context.getString(R.string.charges_parameter),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            if (isEditing) {
                                val chipItems =
                                    if (allChargeCosts.any { it.costName == addNewLabel }) {
                                        allChargeCosts
                                    } else {
                                        allChargeCosts + Costs(
                                            costName = addNewLabel,
                                            chargeFlag = true,
                                            fundType = FundType.OPERATIONAL,
                                            responsible = Responsible.TENANT,
                                            paymentLevel = PaymentLevel.UNIT,
                                            calculateMethod = CalculateMethod.EQUAL,
                                            period = Period.YEARLY,
                                            dueDate = "",
                                            tempAmount = 0.0,
                                            costFor = "",
                                            documentNumber = "",
                                            forBuildingId = 0
                                        )
                                    }


                                ChipGroupShared(
                                    selectedItems = selectedCostNames,
                                    onSelectionChange = { newSelection ->
                                        if (newSelection.contains(addNewLabel)) {
                                            showChargeCostDialog = true
                                            return@ChipGroupShared
                                        }
                                        if (hasCalculatedCharges) {
                                            val removed = selectedCostNames.filterNot { it in newSelection }

                                            val hasChargedRemoved = removed.any { removedName ->
                                                chargesCost.any { it.costName == removedName && it.tempAmount != 0.0 }
                                            }

                                            if (hasChargedRemoved) {
                                                Toast.makeText(
                                                    context,
                                                    context.getString(R.string.cannot_remove_charged_costs),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                return@ChipGroupShared
                                            }

                                            selectedCostNames = newSelection
                                        } else {
                                            selectedCostNames = newSelection
                                        }

                                    },
                                    items = chipItems.map { it.costName },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = context.getString(R.string.charges_parameter),
                                    singleSelection = false,
                                    showDeleteFor = { costName ->
                                        val c = chipItems.firstOrNull { it.costName == costName }
                                        val bId = c?.forBuildingId ?: 0L
                                        val tempCost = c?.addedBeforeCreateBuilding

                                        if (hasCalculatedCharges) {
                                            val hasChargeForThisCost = chargesCost.any {
                                                it.costName == costName && it.tempAmount != 0.0
                                            }
                                            if (hasChargeForThisCost) {
                                                false
                                            } else {
                                                bId == building.buildingId || tempCost == true
                                            }
                                        } else {
                                            bId == building.buildingId || tempCost == true
                                        }
                                    },
                                    onDeleteRequest = { costName ->
                                        val c = chipItems.firstOrNull { it.costName == costName }
                                        Cost().deleteCostWithLinked(
                                            context,
                                            buildingId = building.buildingId,
                                            costId = c!!.costId,
                                            onSuccess = {
                                                selectedCostNames = selectedCostNames - c.costName
                                                val updated = chipItems - c
                                                sharedViewModel.updateCosts(updated)
                                            },
                                            onNotFound = {

                                            },
                                            onError = {}
                                        )
                                    }
                                )
                            } else {
                                selectedCostNames.forEachIndexed { index, cost ->
                                    Text(
                                        text = cost,
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

                item {
                    UploadFile(
                        sharedViewModel = sharedViewModel,
                        context = context,
                        isEditing = isEditing
                    ) { uploaded ->
                        sharedViewModel.addFileList(uploaded)
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
                            onUpdateBuilding(
                                editableBuilding,
                                selectedCostNames,
                                hasCalculatedCharges,
                                fileIdsForUpdate
                            )
                        },
                        enabled = canSave,
                        modifier = Modifier.weight(1f)
                    ) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = context.getString(R.string.edit),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        val perms = sharedViewModel.rolePermissions
        val perm = perms.permissionFor(
            AuthorizationObjects.BUILDING_PROFILE,
            AuthorizationFieldsBuildingProfile.EDIT_BUILDING_BUTTON
        )

        if (perm == PermissionLevel.WRITE || perm == PermissionLevel.FULL) {
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
    }

    if (showAddCityComplexDialog) {
        AddCityComplexDialog(
            onDismiss = { showAddCityComplexDialog = false },
            onInsert = { newName, newAddress ->
                val newCityComplex = CityComplexes(
                    name = newName,
                    address = newAddress,
                    forBuildingId = building.buildingId
                )
                sharedViewModel.insertCityComplexRemote(
                    context = context,
                    cityComplexes = newCityComplex
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
                val newBuildingType = BuildingTypes(
                    buildingTypeName = newItem,
                    addedBeforeCreateBuilding = true
                )
                sharedViewModel.insertBuildingTypeRemote(
                    context = context,
                    buildingTypes = newBuildingType
                ) { inserted ->
                    if (inserted != null) {
                        showBuildingTypeDialog = false
                        editableBuilding = editableBuilding.copy(buildingTypeId = inserted.buildingTypeId)
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
                val newBuildingUsage = BuildingUsages(
                    buildingUsageName = name,
                    addedBeforeCreateBuilding = true
                )
                sharedViewModel.insertBuildingUsageRemote(
                    context = context,
                    buildingUsages = newBuildingUsage
                ) { inserted ->
                    if (inserted != null) {
                        showBuildingUsageDialog = false
                        editableBuilding = editableBuilding.copy(buildingUsageId = inserted.buildingUsageId)
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
                    dueDate = "",
                    costFor = newCostName,
                    documentNumber = "",
                    forBuildingId = building.buildingId
                )
                costApi.createGlobalCost(
                    context = context,
                    cost = newCost,
                    onSuccess = { created ->
                        sharedViewModel.appendGlobalCost(created)
                        selectedCostNames = selectedCostNames + created.costName
                        Log.d("selectedCostNames", selectedCostNames.toString())
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

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "StateFlowValueCalledInComposition")
@Composable
fun FundsTab(
    building: Buildings,
    sharedViewModel: SharedViewModel,
    buildingViewModel: BuildingsViewModel,
    onOpenCostDetail: (cost: Costs, fundType: FundType) -> Unit
) {
    val context = LocalContext.current

    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var earningsDialogError by remember { mutableStateOf<String?>(null) }
    var selectedCost by remember { mutableStateOf<Costs?>(null) }
    var selectedEarning by remember { mutableStateOf<Earnings?>(null) }
    val pendingCosts by sharedViewModel.pendingCostsForBuilding.collectAsState()

    val operationalFundBalance by sharedViewModel.operationalFundBalance.collectAsState()
    val capitalFundBalance by sharedViewModel.capitalFundBalance.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    val perms = sharedViewModel.rolePermissions

    val operationalPerm = perms.permissionFor(
        AuthorizationObjects.BUILDING_PROFILE,
        AuthorizationFieldsBuildingProfile.CREATE_OPERATIONAL_COST_BUTTON
    )

    val earningPerm = perms.permissionFor(
        AuthorizationObjects.BUILDING_PROFILE,
        AuthorizationFieldsBuildingProfile.CREATE_EARNING_BUTTON
    )

    val capitalPerm = perms.permissionFor(
        AuthorizationObjects.BUILDING_PROFILE,
        AuthorizationFieldsBuildingProfile.CREATE_CAPITAL_COST_BUTTON
    )
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            sharedViewModel.loadFundsForBuilding(context, building.buildingId)
            sharedViewModel.loadFundBalances(context, building.buildingId)
        }
    }


    val operationalCosts = remember(pendingCosts) {
        pendingCosts.filter { it.fundType == FundType.OPERATIONAL && it.chargeFlag == false && it.invoiceFlag == false }
    }
    val capitalCosts = remember(pendingCosts) {
        pendingCosts.filter { it.fundType == FundType.CAPITAL && it.invoiceFlag == false && it.capitalFlag == false }
    }

    var showCapitalEditDialog by remember { mutableStateOf(false) }
    var showOperationalEditDialog by remember { mutableStateOf(false) }
    var showEditEarningDialog by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf(
        context.getString(R.string.incomes),
        context.getString(R.string.capital_costs),
        context.getString(R.string.operational_costs)
    )

    var ownersWithUnits by remember { mutableStateOf<List<Owner.OwnerWithUnitsDto>>(emptyList()) }


    fun loadOwners() {
        Owner().getOwnersWithUnitsByBuilding(
            context = context,
            buildingId = building.buildingId,
            onSuccess = { list ->
                ownersWithUnits = list
            },
            onError = {
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

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {


            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    FundInfoBox(
                        formattedFund = formatNumberWithCommas(operationalFundBalance.roundToLong()),
                        context = context,
                        title = context.getString(R.string.operation_funds)
                    )
                    Spacer(Modifier.height(8.dp))
                    FundInfoBox(
                        formattedFund = formatNumberWithCommas(capitalFundBalance.roundToLong()),
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
                            colors =
                                if (selectedTab == index)
                                ButtonDefaults.textButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                )
                            else
                                ButtonDefaults.textButtonColors(),
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = title.uppercase(),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (selectedTab == index)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                when (selectedTab) {
                    0 -> {
                        EarningsSection(
                            buildingId = building.buildingId,
                            sharedViewModel = sharedViewModel,
                            onUpdateEarning = { earning ->
                                coroutineScope.launch {
                                    val canUpdate = sharedViewModel.canUpdateEarning(context, earning.earningsId)
                                    if (canUpdate) {
                                        selectedEarning = earning
                                        showEditEarningDialog = true
                                    } else {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.can_not_update_received_credits),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            },

                            onDeleteEarning = { earning ->

                                coroutineScope.launch {
                                    val canDelete = sharedViewModel.canUpdateEarning(context, earning.earningsId)
                                    if (canDelete) {
                                        sharedViewModel.deleteEarningSafe(context, earning.earningsId) { ok ->
                                            if (ok) {
                                                Toast.makeText(context, context.getString(R.string.success_delete),
                                                    Toast.LENGTH_LONG).show()

                                                sharedViewModel.loadNotInvoicedEarnings(context, building.buildingId)
                                            } else {
                                                Toast.makeText(context, context.getString(R.string.failed),
                                                    Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, context.getString(R.string.can_not_update_received_credits),
                                            Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            onInvoiceClicked = { earning ->
                                val intent = Intent(context, EarningDetailActivity::class.java)
                                intent.putExtra("extra_earning_id", earning.earningsId)
                                intent.putExtra("extra_building_id", building.buildingId)
                                context.startActivity(intent)
                            }
                        )
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
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    items(capitalCosts) { cost ->
                                        CostListItemWithDetailText(cost = cost,
                                            onUpdateClick = {
                                            selectedCost = cost
                                            showCapitalEditDialog = true
                                        },
                                        onDeleteClick = { cost ->
                                            sharedViewModel.deleteCost(context, cost.costId, onDone = {
                                                coroutineScope.launch {
                                                    snackBarHostState.showSnackbar(
                                                        context.getString(R.string.success_delete)
                                                    )
                                                    sharedViewModel.loadFundsForBuilding(
                                                        context,
                                                        building.buildingId
                                                    )
                                                    sharedViewModel.loadFundBalances(
                                                        context,
                                                        building.buildingId
                                                    )
                                                }
                                            })
                                            },
                                            onDetailClick = {
                                                selectedCost = cost

                                                sharedViewModel.invoiceCostIfEnoughFund(
                                                    context = context, cost = cost,
                                                    onSuccess = {
                                                        coroutineScope.launch {
                                                            snackBarHostState.showSnackbar(
                                                                context.getString(R.string.invoiced_succesfully)
                                                            )
                                                            sharedViewModel.loadFundsForBuilding(
                                                                context,
                                                                building.buildingId
                                                            )
                                                            sharedViewModel.loadFundBalances(
                                                                context,
                                                                building.buildingId
                                                            )
                                                        }
                                                    },
                                                    onError = { e ->
                                                        coroutineScope.launch {
                                                            snackBarHostState.showSnackbar(
                                                                context.getString(R.string.insufficient_fund_balance)
                                                            )
                                                        }
                                                        Log.e("InvoiceError", e.toString())
                                                    })
                                            })
                                    }
                                    item { Spacer(modifier = Modifier.height(82.dp)) }
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
                                        CostListItemWithDetailText(
                                            cost = cost,
                                            onUpdateClick = {
                                                selectedCost = cost
                                                showOperationalEditDialog = true
                                            },
                                            onDeleteClick = { cost ->
                                                sharedViewModel.deleteCost(context, cost.costId, onDone = {
                                                    coroutineScope.launch {
                                                        snackBarHostState.showSnackbar(
                                                            context.getString(R.string.success_delete)
                                                        )
                                                        sharedViewModel.loadFundsForBuilding(
                                                            context,
                                                            building.buildingId
                                                        )
                                                        sharedViewModel.loadFundBalances(
                                                            context,
                                                            building.buildingId
                                                        )
                                                    }
                                                })
                                            },
                                            onDetailClick = {
                                                selectedCost = cost

                                                sharedViewModel.invoiceCostIfEnoughFund(
                                                    context = context,
                                                    cost = cost,
                                                    onSuccess = {
                                                        coroutineScope.launch {
                                                            snackBarHostState.showSnackbar(
                                                                context.getString(R.string.invoiced_succesfully)
                                                            )
                                                            sharedViewModel.loadFundsForBuilding(
                                                                context,
                                                                building.buildingId
                                                            )
                                                            sharedViewModel.loadFundBalances(
                                                                context,
                                                                building.buildingId
                                                            )
                                                        }
                                                    },
                                                    onError = { e ->
                                                        coroutineScope.launch {
                                                            snackBarHostState.showSnackbar(
                                                                context.getString(R.string.insufficient_operational_fund_balance)
                                                            )
                                                        }
                                                        Log.e("InvoiceError", e.toString())
                                                    }
                                                )
                                            }
                                        )


                                    }
                                    item { Spacer(modifier = Modifier.height(82.dp)) }
                                }
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                if (selectedTab == 0) {
                    if(earningPerm == PermissionLevel.WRITE || earningPerm == PermissionLevel.FULL) {
                        FloatingActionButton(
                            onClick = { buildingViewModel.showEarningsDialog.value = true },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Earnings"
                            )
                        }
                    }
                } else if (selectedTab == 1) {
                    if(capitalPerm == PermissionLevel.WRITE || capitalPerm == PermissionLevel.FULL) {
                        ExtendedFloatingActionButton(
                            onClick = {
                                buildingViewModel.showCapitalCostDialog.value = true
                            },
                            icon = { Icon(Icons.Default.Add, null) },
                            text = {
                                Text(
                                    context.getString(R.string.insert_capital_doc),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                        )
                    }
                } else if (selectedTab == 2) {
                    if(operationalPerm == PermissionLevel.WRITE || operationalPerm == PermissionLevel.FULL) {
                        ExtendedFloatingActionButton(
                            onClick = {
                                buildingViewModel.showOperationalCostDialog.value = true
                            },
                            icon = { Icon(Icons.Default.Add, null) },
                            text = {
                                Text(
                                    context.getString(R.string.insert_operational_doc),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                        )
                    }

                }
            }

            if (buildingViewModel.showEarningsDialog.value) {
                EarningsDialog(
                    building = building,
                    errorMessage = earningsDialogError,
                    onDismiss = {
                        earningsDialogError = null
                        buildingViewModel.hideDialogs()
                    },
                    onConfirm = { earning ->
                        coroutineScope.launch {
                            val earningsToInsert = Earnings(
                                earningsId = sharedViewModel.selectedEarnings?.earningsId ?: 0,
                                buildingId = building.buildingId,
                                earningsName = earning.earningsName,
                                amount = earning.amount,
                                period = earning.period,
                                startDate = earning.startDate,
                                endDate = earning.endDate
                            )

                            try {
                                sharedViewModel.insertEarningsWithCreditsSuspend(
                                    context = context,
                                    earning = earningsToInsert
                                )

                                earningsDialogError = null


                                sharedViewModel.getNotInvoicedEarnings(
                                    context = context,
                                    buildingId = building.buildingId
                                )
                                sharedViewModel.loadFundBalances(context, building.buildingId)
                                sharedViewModel.loadFundsForBuilding(context, building.buildingId)
                                buildingViewModel.showEarningsDialog.value = false
                                snackBarHostState.showSnackbar(
                                    context.getString(R.string.earning_inserted_successfully)
                                )

                            } catch (e: IllegalStateException) {
                                val msg = when (e.message) {
                                    "period-range-too-short" ->
                                        context.getString(R.string.earning_period_too_short)

                                    "invalid-date-format-expected-YYYY/MM/DD" ->
                                        context.getString(R.string.invalid_date_format)

                                    "startDate-must-be-before-or-equal-endDate" ->
                                        context.getString(R.string.startdate_before_enddate)

                                    "invalid-buildingId" ->
                                        context.getString(R.string.invalid_building_id)

                                    "invalid-amount" ->
                                        context.getString(R.string.invalid_amount)

                                    "earnings-conflict-with-existing-credits" ->
                                        context.getString(R.string.earnings_conflict_error)

                                    else ->
                                        context.getString(R.string.failed)
                                }
                                earningsDialogError = msg
                            } catch (_: Exception) {
                                earningsDialogError = context.getString(R.string.failed)
                            }
                        }
                    },
                    sharedViewModel = sharedViewModel
                )
            }

            if(showCapitalEditDialog){
                selectedCost?.let {
                    EditCapitalCostDialog(
                        sharedViewModel = sharedViewModel,
                        updatedCost = it,
                        onSave = { updatedCost->
                            sharedViewModel.updateCost(
                                context = context,
                                cost = updatedCost,
                                onSuccess = {
                                    coroutineScope.launch {
                                        snackBarHostState.showSnackbar(
                                            context.getString(R.string.success_update)
                                        )
                                    }
                                    sharedViewModel.loadFundBalances(
                                        context,
                                        building.buildingId
                                    )
                                    sharedViewModel.loadFundsForBuilding(
                                        context,
                                        building.buildingId
                                    )
                                    showCapitalEditDialog = false
                                },
                                onError = { e ->
                                    coroutineScope.launch {
                                        snackBarHostState.showSnackbar(
                                            context.getString(R.string.failed)
                                        )
                                    }
                                    showCapitalEditDialog = false
                                    Log.e("UpdateCost", e.toString())
                                }
                            )
                        },
                        onDismiss = {
                            showCapitalEditDialog = false
                        }
                    )
                }
            }
            if(showEditEarningDialog){
                selectedEarning?.let {
                    EditEarningsDialog(
                        earnings = it,
                        building = building,
                        errorMessage = earningsDialogError,
                        onDismiss = {
                            earningsDialogError = null
                            showEditEarningDialog = false
                        },
                        onConfirm = { earning ->
                            coroutineScope.launch {
                                try {
                                    sharedViewModel.updateEarningWithCredits(
                                        context = context,
                                        earning = earning,
                                        onSuccess = { _ ->
                                            coroutineScope.launch {
                                                snackBarHostState.showSnackbar(
                                                    context.getString(R.string.success_update)
                                                )
                                            }
                                            sharedViewModel.loadNotInvoicedEarnings(context, building.buildingId)
                                        },
                                        onError = { e ->
                                            coroutineScope.launch {
                                                snackBarHostState.showSnackbar(
                                                    context.getString(R.string.failed)
                                                )
                                            }
                                            Log.e("UpdateEarning", e.toString())
                                        }
                                    )
                                    sharedViewModel.loadFundBalances(context, building.buildingId)
                                    sharedViewModel.loadFundsForBuilding(
                                        context,
                                        building.buildingId
                                    )
                                    showEditEarningDialog = false

                                } catch (e: IllegalStateException) {
                                    val msg = when (e.message) {
                                        "period-range-too-short" ->
                                            context.getString(R.string.earning_period_too_short)

                                        "invalid-date-format-expected-YYYY/MM/DD" ->
                                            context.getString(R.string.invalid_date_format)

                                        "startDate-must-be-before-or-equal-endDate" ->
                                            context.getString(R.string.startdate_before_enddate)

                                        "invalid-buildingId" ->
                                            context.getString(R.string.invalid_building_id)

                                        "invalid-amount" ->
                                            context.getString(R.string.invalid_amount)

                                        "earnings-conflict-with-existing-credits" ->
                                            context.getString(R.string.earnings_conflict_error)

                                        else ->
                                            context.getString(R.string.failed)
                                    }
                                    earningsDialogError = msg
                                } catch (_: Exception) {
                                    earningsDialogError = context.getString(R.string.failed)
                                }
                            }
                        },
                        sharedViewModel = sharedViewModel
                    )
                }
            }
            if(showOperationalEditDialog){
                selectedCost?.let {
                    EditOperationalCostDialog(
                        buildingId = building.buildingId,
                        sharedViewModel = sharedViewModel,
                        updatedCost = it,
                        onSave = {  newCost ->
                            sharedViewModel.updateCost(
                                context = context,
                                cost = newCost,
                                onSuccess = {
                                    coroutineScope.launch {
                                        snackBarHostState.showSnackbar(
                                            context.getString(R.string.success_update)
                                        )
                                    }
                                    sharedViewModel.loadFundBalances(
                                        context,
                                        building.buildingId
                                    )
                                    sharedViewModel.loadFundsForBuilding(
                                        context,
                                        building.buildingId
                                    )
                                    showOperationalEditDialog = false
                                },
                                onError = { e ->
                                    coroutineScope.launch {
                                        snackBarHostState.showSnackbar(
                                            context.getString(R.string.failed)
                                        )
                                    }
                                    showOperationalEditDialog = false
                                    Log.e("UpdateCost", e.toString())
                                }
                            )
                        },
                        onDismiss = {
                            showOperationalEditDialog = false
                        },
                        onShowSnackbar = { msg ->
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar(msg)
                            }
                        }
                    )
                }
            }

            if (buildingViewModel.showCapitalCostDialog.value) {
                AddCapitalCostDialog(
                sharedViewModel = sharedViewModel,
                onDismiss = { buildingViewModel.showCapitalCostDialog.value = false },
                    onSave = { selectedCost, costName, amount, period, calculateMethod, calculatedUnitMethod, responsible, selectedUnits, selectedOwners, dueDate, documentNumber, message ->
                    coroutineScope.launch {
//                        if (amount.toDouble() <= capitalFundBalance) {
                            val newCost = Costs(
                                buildingId = building.buildingId,
                                costName = costName,
                                chargeFlag = false,
                                capitalFlag = false,
                                invoiceFlag = false,
                                fundType = FundType.CAPITAL,
                                responsible = Responsible.OWNER,
                                paymentLevel = PaymentLevel.UNIT,
                                calculateMethod = CalculateMethod.EQUAL,
                                period = Period.NONE,
                                tempAmount = amount.toDouble(),
                                dueDate = dueDate,
                                costFor = selectedCost.costName,
                                documentNumber = documentNumber
                            )
                            sharedViewModel.insertCostToServer(
                                context = context,
                                buildingId = building.buildingId,
                                costs = listOf(newCost),
                                debts = emptyList(),
                                onSuccess = {
                                    sharedViewModel.loadFundBalances(
                                                context,
                                                building.buildingId
                                            )
                                            sharedViewModel.loadFundsForBuilding(
                                                context,
                                                building.buildingId
                                            )
                                },
                                onError = { e ->
                                    coroutineScope.launch {
                                        snackBarHostState.showSnackbar(
                                            context.getString(R.string.failed)
                                        )
                                    }
                                }
                            )
                        buildingViewModel.showCapitalCostDialog.value = false
                    }
                }
            )
        }

        if (buildingViewModel.showOperationalCostDialog.value) {
            AddOperationalCostDialog(
                buildingId = building.buildingId,
                sharedViewModel = sharedViewModel,
                operationalFundBalance = operationalFundBalance,
                onDismiss = { buildingViewModel.showOperationalCostDialog.value = false },
                onSave = { message ->
                    coroutineScope.launch {
                        sharedViewModel.loadFundsForBuilding(context, building.buildingId)
                        sharedViewModel.loadFundBalances(context, building.buildingId)
                        buildingViewModel.showOperationalCostDialog.value = false
                        snackBarHostState.showSnackbar(message)
                    }
                },
                onShowSnackbar = { msg ->
                    coroutineScope.launch {
                        snackBarHostState.showSnackbar(msg)
                    }
                }
            )
        }
    }
    }
}

@Composable
fun CostListItemWithDetailText(
    cost: Costs,
    onUpdateClick: () -> Unit,
    onDetailClick: () -> Unit,
    onDeleteClick: (Costs) -> Unit,
) {
    val context = LocalContext.current
    var menuOpen by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .clickable { onUpdateClick() },
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
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

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${formatNumberWithCommas(cost.tempAmount)} ${context.getString(R.string.toman)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )

                    Box {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = null)
                        }

                        DropdownMenu(
                            expanded = menuOpen,
                            onDismissRequest = { menuOpen = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(context.getString(R.string.delete), style = MaterialTheme.typography.bodyLarge) },
                                onClick = {
                                    menuOpen = false
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))
                Button(onClick = onDetailClick) {
                    Text(
                        text = context.getString(R.string.payment),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = {
                    Text(
                        text = context.getString(R.string.delete_document),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                text = {
                    Text(
                        text = context.getString(R.string.are_you_sure),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        onDeleteClick(cost)
                    }) {
                        Text(
                            context.getString(R.string.delete),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(
                            context.getString(R.string.cancel),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            )
        }
    }
}


@Composable
    fun TenantsTab(
        building: Buildings,
        sharedViewModel: SharedViewModel
    ) {
        val context = LocalContext.current
        val tenantApi = remember { Tenant() }
        val unitsApi = remember { com.example.delta.volley.Units() }

        var tenantsWithUnit by remember { mutableStateOf<List<Tenant.TenantWithUnitDto>>(emptyList()) }
        var loading by remember { mutableStateOf(false) }
        var showTenantDialog by remember { mutableStateOf(false) }

        var units by remember { mutableStateOf<List<Units>>(emptyList()) }
        var unitsLoading by remember { mutableStateOf(false) }

        val perms = sharedViewModel.rolePermissions

        val perm = perms.permissionFor(
            AuthorizationObjects.BUILDING_PROFILE,
            AuthorizationFieldsBuildingProfile.CREATE_TENANT_BUTTON
        )

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
                        val unit = dto.unit

                        TenantItem(
                            sharedViewModel = sharedViewModel,
                            onDelete = {
                                tenantApi.deleteTenant(
                                    context = context,
                                    buildingId = building.buildingId,
                                    tenantId = dto.tenantUnit.tenantId,
                                    onSuccess = {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.success_delete),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        loadTenants()
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
                            relation = dto.tenantUnit,
                            user = dto.user,
                            unit = unit,
                            onClick = {
                                    val intent = Intent(context, TenantsDetailsActivity::class.java)
                                    intent.putExtra("UNIT_DATA", unit.unitId)
                                    intent.putExtra("TENANT_DATA", dto.tenantUnit.tenantId)
                                    intent.putExtra("BUILDING_ID", building.buildingId)
                                    context.startActivity(intent)
                            }
                        )
                    }
                }
            }
            if(perm == PermissionLevel.FULL || perm == PermissionLevel.WRITE ){
            FloatingActionButton(
                onClick = { showTenantDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Filled.Add, "Add")
            }
                }

            if (showTenantDialog && !unitsLoading) {
                TenantDialog(
                    sharedViewModel = sharedViewModel,
                    units = units,
                    onDismiss = { showTenantDialog = false },
                    onAddTenant = { user, selectedUnit, tenantUnit ->
                        tenantApi.insertTenantWithUnit(
                            context = context,
                            buildingId = building.buildingId,
                            user = user,
                            tenantUnit = tenantUnit,
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
                                val raw = e.message ?: ""
                                val msg = when {
                                    raw.contains("This unit already has a resident owner") ->
                                       context.getString(R.string.this_unit_has_resident_owner)

                                    raw.contains("Active tenant must include today in date range") ->
                                        context.getString(R.string.active_tenant_must_incluse_current_day)

                                    raw.contains("Active tenant already exists within selected period") ->
                                        context.getString(R.string.active_tenant_already_exists_within_selected_period)

                                    raw.contains("Tenant is already active in another unit or building in the selected period") ->
                                        context.getString(R.string.tenant_is_already_active_in_another_place)

                                    else ->
                                        context.getString(R.string.failed)
                                }

                                Toast.makeText(
                                    context,
                                    msg,
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
fun EarningsSection(
    buildingId: Long,
    sharedViewModel: SharedViewModel,
    onInvoiceClicked: (Earnings) -> Unit,
    onUpdateEarning: (Earnings) -> Unit,
    onDeleteEarning: (Earnings) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val earnings by sharedViewModel
        .getNotInvoicedEarnings(context = context, buildingId)
        .collectAsState(initial = emptyList())

    Column(modifier = modifier.padding(16.dp)) {
        if (earnings.isEmpty()) {
            Text(
                text = context.getString(R.string.no_earnings_pending),
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            LazyColumn {
                items(earnings, key = { it.earningsId }) { earning ->
                    var menuOpen by remember { mutableStateOf(false) }
                    var showDeleteDialog by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                menuOpen = false
                                onUpdateEarning(earning)
                            }
                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(earning.earningsName, style = MaterialTheme.typography.bodyLarge)
                                Spacer(Modifier.height(4.dp))
                                Row {
                                    Text(
                                        text = if (earning.period == Period.NONE)
                                            "${context.getString(R.string.date)}:"
                                        else
                                            "${context.getString(R.string.start_date)}:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = earning.startDate,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (earning.period != Period.NONE) {
                                    Spacer(Modifier.height(4.dp))
                                    Row {
                                        Text(
                                            text = "${context.getString(R.string.end_date)}:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = earning.endDate,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = earning.period.getDisplayName(context) + ": ",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${formatNumberWithCommas(earning.amount)} ${context.getString(R.string.toman)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Box {
                                        IconButton(onClick = { menuOpen = true }) {
                                            Icon(Icons.Default.MoreVert, contentDescription = null)
                                        }

                                        DropdownMenu(
                                            expanded = menuOpen,
                                            onDismissRequest = { menuOpen = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text(context.getString(R.string.delete), style = MaterialTheme.typography.bodyLarge) },
                                                onClick = {
                                                    menuOpen = false
                                                    val perms = sharedViewModel.rolePermissions
                                                    val perm = perms.permissionFor(
                                                        AuthorizationObjects.BUILDING_PROFILE,
                                                        AuthorizationFieldsBuildingProfile.CREATE_EARNING_BUTTON
                                                    )
                                                    if (perm == PermissionLevel.WRITE || perm == PermissionLevel.FULL) {
                                                        showDeleteDialog = true
                                                    } else {
                                                        Toast.makeText(
                                                            context,
                                                            context.getString(R.string.auth_cancel),
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(4.dp))
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

                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            title = {
                                Text(
                                    text = context.getString(R.string.delete_earning),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            text = {
                                Text(
                                    text = context.getString(R.string.are_you_sure),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    showDeleteDialog = false
                                    onDeleteEarning(earning)
                                }) {
                                    Text(
                                        context.getString(R.string.delete),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteDialog = false }) {
                                    Text(
                                        context.getString(R.string.cancel),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
    fun UnitsTab(
        sharedViewModel: SharedViewModel,
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
                            sharedViewModel = sharedViewModel,
                            unit = unit,
                            building = building,
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
            val perms = sharedViewModel.rolePermissions

            val perm = perms.permissionFor(
                AuthorizationObjects.BUILDING_PROFILE,
                AuthorizationFieldsBuildingProfile.CREATE_UNIT_BUTTON
            )
            if (perm == PermissionLevel.WRITE || perm == PermissionLevel.FULL) {
                FloatingActionButton(
                    onClick = { showUnitDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Filled.Add, "Add")
                }
            }


            if (showUnitDialog) {
                UnitDialog(
                    units = units,
                    onDismiss = { showUnitDialog = false },
                    floorCount = building.floorCount,
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
        sharedViewModel: SharedViewModel,
        building: Buildings,
        unit: Units,
        onUpdateUnit: (Units) -> Unit
    ) {
        val context = LocalContext.current
        var showEditDialog by remember { mutableStateOf(false) }
        val perms = sharedViewModel.rolePermissions

        val perm = perms.permissionFor(
            AuthorizationObjects.BUILDING_PROFILE,
            AuthorizationFieldsBuildingProfile.CREATE_UNIT_BUTTON
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable(onClick = {
                    if (perm == PermissionLevel.WRITE || perm == PermissionLevel.FULL) {
                        showEditDialog = true
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.auth_cancel),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }),
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
                floorCount = building.floorCount,
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
        val invoicedCosts by sharedViewModel.pendingCostsForBuilding.collectAsState()
        LaunchedEffect(buildingId) {
            sharedViewModel.loadFundsForBuilding(context, buildingId)
        }

        val capitalInvoicedCosts = remember(invoicedCosts) {
            invoicedCosts.filter { it.fundType == FundType.CAPITAL && it.invoiceFlag == true }
        }

        val operationalInvoicedCosts = remember(invoicedCosts) {
            invoicedCosts.filter { it.fundType == FundType.OPERATIONAL && it.invoiceFlag == true }
        }

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
    ) {
        val context = LocalContext.current
        val buildingId = building.buildingId

        var selectedTab by remember { mutableStateOf(0) }
        val tabTitles = listOf(
            context.getString(R.string.emergency_calls).uppercase(),
            context.getString(R.string.buildings_member).uppercase()
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
        val snackBarHostState = remember { SnackbarHostState() }


        val perms = sharedViewModel.rolePermissions

        val perm = perms.permissionFor(
            AuthorizationObjects.BUILDING_PROFILE,
            AuthorizationFieldsBuildingProfile.CREATE_PHONE_ENTRY_BUTTON
        )

        Box(modifier = Modifier.fillMaxSize()) {
            SnackbarHost(
                hostState = snackBarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
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
                                    PhonebookEntryItem(
                                        entry,
//                                        permissionLevel,
                                        sharedViewModel
                                    )
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
                                    PhonebookEntryItem(
                                        entry,
//                                        permissionLevel,
                                        sharedViewModel
                                    )
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

                if (selectedTab == 0) {
                    if(perm == PermissionLevel.FULL || perm == PermissionLevel.WRITE) {
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
        }
        if (showAddDialog) {
            AddPhonebookEntryDialog(
                buildingId = building.buildingId,
                onDismiss = { showAddDialog = false },
                onConfirm = { entry ->
                    coroutineScope.launch {
                        sharedViewModel.addPhonebookEntry(context, entry)
                        showAddDialog = false
                        snackBarHostState.showSnackbar(context.getString(R.string.insert_emergency_phone_book_successfully))
                    }
                }
            )
        }
    }

@Composable
fun PhonebookEntryItem(
    entry: PhonebookEntry,
    sharedViewModel: SharedViewModel
) {
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.name, style = MaterialTheme.typography.bodyLarge)

                if (entry.type == "resident") {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        entry.roleLabel?.trim().orEmpty(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Text(
                entry.phoneNumber,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )
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
            text = { Text(context.getString(R.string.are_you_sure)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            sharedViewModel.deletePhonebookEntry(context, entry)
                            showDeleteDialog = false
                        }
                    }
                ) {
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
//            context.getString(R.string.tenants) -> "resident"
            context.getString(R.string.emergency_calls) -> "emergency"
            else -> "emergency"
        }

        AlertDialog(
            onDismissRequest = {},
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
                            items = listOf(context.getString(R.string.emergency_calls)),
                            label = context.getString(R.string.select_type)
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
    fun OwnersTab(
        building: Buildings,
        sharedViewModel: SharedViewModel
    ) {
        val context = LocalContext.current
        val ownerApi = remember { Owner() }
        val unitsApi = remember { com.example.delta.volley.Units() }

        var showOwnerDialog by remember { mutableStateOf(false) }
        var ownersWithUnits by remember { mutableStateOf<List<Owner.OwnerWithUnitsDto>>(emptyList()) }
        var loading by remember { mutableStateOf(false) }

        var units by remember { mutableStateOf<List<Units>>(emptyList()) }
        var unitsLoading by remember { mutableStateOf(false) }

        val dangSumsMap = remember(ownersWithUnits) {
            ownersWithUnits
                .flatMap { it.ownerUnits }
                .groupBy { it.unitId }
                .mapValues { (_, list) -> list.sumOf { it.dang } }
        }

        val perms = sharedViewModel.rolePermissions

        val perm = perms.permissionFor(
            AuthorizationObjects.BUILDING_PROFILE,
            AuthorizationFieldsBuildingProfile.CREATE_OWNER_BUTTON
        )

        fun loadOwners() {
            loading = true
            ownerApi.getOwnersWithUnitsByBuilding(
                context = context,
                buildingId = building.buildingId,
                onSuccess = { list ->
                    ownersWithUnits = list
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

            if (loading && ownersWithUnits.isEmpty()) {
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
                            ownerUnit = ownerDto,
                            sharedViewModel = sharedViewModel,
                            onDelete = { ownerUnitsList, userRoleCrossRefs ->
                                ownerApi.deleteOwner(
                                    context = context,
                                    buildingId = building.buildingId,
                                    ownerId = ownerDto.user!!.userId,
                                    onSuccess = {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.success_delete),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        loadOwners()
                                    },
                                    onError = { e ->
                                        val raw = e.message ?: ""
                                        val msg = when {

                                            raw.contains("cannot-delete-owner-with-active-tenant") ->
                                                context.getString(R.string.cannot_delete_owner_with_active_tenant)

                                            else ->
                                                context.getString(R.string.failed)
                                        }

                                        Toast.makeText(
                                            context,
                                            msg,
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
            if (perm == PermissionLevel.WRITE || perm == PermissionLevel.FULL  ) {
                FloatingActionButton(
                    onClick = { showOwnerDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add")
                }
            }


            if (showOwnerDialog) {
                OwnerDialog(
                    sharedViewModel = sharedViewModel,
                    units = units,
                    dangSums = dangSumsMap,
                    onDismiss = { showOwnerDialog = false },
                    onAddOwner = { firstName,
                                   lastName,
                                   address,
                                   email,
                                   phoneNumber,
                                   mobileNumber,
                                   isManager,
                                   isResident,
                                   selectedUnits ->

                        val newUser = User(
                            userId = 0L,
                            mobileNumber = mobileNumber,
                            password = "",
                            firstName = firstName,
                            lastName = lastName,
                            email = email,
                            gender = Gender.entries.first(),
                            profilePhoto = "",
                            nationalCode = "",
                            address = address,
                            phoneNumber = phoneNumber,
                            birthday = ""
                        )

                        ownerApi.insertOwnerWithUnits(
                            context = context,
                            units = selectedUnits,
                            user = newUser,
                            isManager = isManager,
                            isResident = isResident,
                            buildingId = building.buildingId,
                            onSuccess = {
                                showOwnerDialog = false

                                Toast.makeText(
                                    context,
                                    context.getString(R.string.insert_owner_successfully),
                                    Toast.LENGTH_SHORT
                                ).show()

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

enum class DateField {
    START, END
}
@Composable
fun EarningsDialog(
    building: Buildings,
    errorMessage: String?,
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
    var activeDateField by remember { mutableStateOf<DateField?>(null) }

    val defaultEarnings by sharedViewModel.getFixedEarnings(context, building.buildingId)
        .collectAsState(initial = emptyList())

    var localEarnings by remember { mutableStateOf<List<Earnings>>(emptyList()) }
    LaunchedEffect(defaultEarnings) {
        localEarnings = defaultEarnings
    }

    val focusManager = LocalFocusManager.current

    val earningsList = remember(localEarnings, context) {
        localEarnings + Earnings(
            earningsId = -1,
            earningsName = context.getString(R.string.add_new_earning),
            amount = 0.0,
            period = Period.MONTHLY,
            buildingId = null,
            startDate = "",
            endDate = ""
        )
    }

    var selectedEarning by remember { mutableStateOf<Earnings?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val isPeriodNone = selectedPeriod == Period.NONE

    fun numericAmount(): Double {
        val normalized = amount.replace(",", "").persianToEnglishDigits()
        return normalized.toDoubleOrNull() ?: 0.0
    }

    val isValid = remember(
        earningsName, amount, selectedPeriod, startDate, endDate, isPeriodNone
    ) {
        val earningNameValid = earningsName.isNotBlank()
        val amountVal = numericAmount()
        val amountValid = amount.isNotBlank() && amountVal > 0.0
        val periodValid = selectedPeriod != null
        val startDateValid = startDate.isNotBlank()
        val isEndDateValid = if (isPeriodNone) true else endDate.isNotBlank()
        earningNameValid && amountValid && periodValid && startDateValid && isEndDateValid
    }

    fun formatWithComma(input: String): String {
        if (input.isBlank()) return ""
        val clean = input.replace(",", "").persianToEnglishDigits()
        return clean.toLongOrNull()?.let { "%,d".format(it) } ?: input
    }

    AlertDialog(
        onDismissRequest = { },
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

                    ExposedDropdownMenuBoxExample(
                        sharedViewModel = sharedViewModel,
                        items = earningsList,
                        selectedItem = selectedEarning,
                        onItemSelected = { selected ->
                            if (selected.earningsId == -1L) {
                                showAddNewEarningNameDialog = true
                            } else {
                                selectedEarning = selected
                                earningsName = selected.earningsName
                                if (selected.amount > 0.0) {
                                    amount = formatWithComma(selected.amount.toLong().toString())
                                }
                                selectedPeriod = selected.period
                            }
                        },
                        label = context.getString(R.string.earning_title),
                        modifier = Modifier.fillMaxWidth(),
                        itemLabel = { it.earningsName },
                        showDeleteFor = { earning ->
                            val c = earningsList.firstOrNull { it.earningsName == earning.earningsName }
                            val bId = c?.forBuildingId ?: 0L
                            val tempEarning = c?.addedBeforeCreateBuilding
                            bId != 0L || tempEarning == true
                        },
                        onDeleteRequest = { earning ->
                            sharedViewModel.deleteEarningSafe(context, earning.earningsId, onDone = {
                                localEarnings = (localEarnings - earning)
                                    .distinctBy { it.earningsName.trim() }
                                selectedEarning = null
                            })
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { value ->
                            val cleaned = value.replace(",", "").persianToEnglishDigits()
                            if (cleaned.isEmpty()) amount = ""
                            else if (cleaned.matches(Regex("^\\d*$"))) amount = formatWithComma(cleaned)
                        },
                        label = { RequiredLabel(context.getString(R.string.amount)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val amountDouble = numericAmount()
                    val amountInWords =
                        NumberCommaTransformation().numberToWords(context, amountDouble.toLong())
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
                        onValueChange = { },
                        label = {
                            RequiredLabel(
                                if (!isPeriodNone) context.getString(R.string.start_date)
                                else context.getString(R.string.date)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    focusManager.clearFocus()
                                    activeDateField = DateField.START
                                }
                            ) {
                                Icon(Icons.Default.DateRange, contentDescription = null)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (!isPeriodNone) {
                        OutlinedTextField(
                            value = endDate,
                            onValueChange = { },
                            label = { RequiredLabel(context.getString(R.string.end_date)) },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        focusManager.clearFocus()
                                        activeDateField = DateField.END
                                    }
                                ) {
                                    Icon(Icons.Default.DateRange, contentDescription = null)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (!errorMessage.isNullOrBlank()) {
                    item { Spacer(Modifier.height(8.dp)) }
                    item {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (showAddNewEarningNameDialog) {
                AddNewEarningNameDialog(
                    buildingId = building.buildingId,
                    onDismiss = { showAddNewEarningNameDialog = false },
                    onSave = { earning ->
                        val normalized = earning.copy(
                            buildingId = earning.buildingId,
                            forBuildingId = earning.forBuildingId,
                            addedBeforeCreateBuilding = earning.addedBeforeCreateBuilding
                        )

                        localEarnings = (localEarnings + normalized)
                            .distinctBy { it.earningsName.trim() }

                        selectedEarning = normalized
                        earningsName = normalized.earningsName

                        coroutineScope.launch {
                            sharedViewModel.insertNewEarnings(context, normalized)
                            showAddNewEarningNameDialog = false
                        }
                    },

                    earningNameExists = { _, cName ->
                        sharedViewModel.earningNameExists(context, null, cName).first()
                    }
                )
            }
        },
        confirmButton = {
            Button(
                enabled = isValid,
                onClick = {
                    val finalAmount = numericAmount()
                    val newEarning = Earnings(
                        earningsId = sharedViewModel.selectedEarnings?.earningsId ?: 0,
                        buildingId = building.buildingId,
                        earningsName = earningsName,
                        amount = finalAmount,
                        period = selectedPeriod ?: Period.MONTHLY,
                        startDate = startDate,
                        endDate = if (isPeriodNone) "" else endDate
                    )
                    onConfirm(newEarning)
                }
            ) {
                Text(context.getString(R.string.insert), style = MaterialTheme.typography.bodyLarge)
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(context.getString(R.string.cancel), style = MaterialTheme.typography.bodyLarge)
            }
        }
    )

    if (activeDateField != null) {
        PersianDatePickerDialogContent(
            sharedViewModel = sharedViewModel,
            onDateSelected = { selected ->
                when (activeDateField) {
                    DateField.START -> startDate = selected
                    DateField.END -> endDate = selected
                    null -> {}
                }
                activeDateField = null
            },
            onDismiss = { activeDateField = null }
        )
    }
}

@Composable
fun EditEarningsDialog(
    earnings: Earnings,
    building: Buildings,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (Earnings) -> Unit,
    sharedViewModel: SharedViewModel
) {
    var earningsName by remember { mutableStateOf(earnings.earningsName) }
    var amount by remember { mutableStateOf(formatWithComma(earnings.amount.toLong().toString()))}
    var selectedPeriod by remember { mutableStateOf<Period?>(earnings.period) }
    var startDate by remember { mutableStateOf(earnings.startDate) }
    var endDate by remember { mutableStateOf(earnings.endDate) }
    val context = LocalContext.current

    var showAddNewEarningNameDialog by remember { mutableStateOf(false) }
    var activeDateField by remember { mutableStateOf<DateField?>(null) }
    val focusManager = LocalFocusManager.current
    val defaultEarnings by sharedViewModel.getFixedEarnings(context, building.buildingId)
        .collectAsState(initial = emptyList())

    var localEarnings by remember { mutableStateOf<List<Earnings>>(emptyList()) }
    LaunchedEffect(defaultEarnings) {
        localEarnings = defaultEarnings
    }

    val earningsList = remember(localEarnings, context) {
        localEarnings + Earnings(
            earningsId = -1,
            earningsName = context.getString(R.string.add_new_earning),
            amount = 0.0,
            period = Period.MONTHLY,
            buildingId = null,
            startDate = "",
            endDate = ""
        )
    }


    var selectedEarning by remember {
        mutableStateOf<Earnings?>(earnings)
    }

    val coroutineScope = rememberCoroutineScope()
    val isPeriodNone = selectedPeriod == Period.NONE

    fun numericAmount(): Double {
        val normalized = amount
            .replace(",", "")
            .persianToEnglishDigits()
        return normalized.toDoubleOrNull() ?: 0.0
    }

    val isValid = remember(
        earningsName,
        amount,
        selectedPeriod,
        startDate,
        endDate,
        isPeriodNone
    ) {
        val earningNameValid = earningsName.isNotBlank()
        val amountVal = numericAmount()
        val amountValid = amount.isNotBlank() && amountVal > 0.0
        val periodValid = selectedPeriod != null
        val startDateValid = startDate.isNotBlank()
        val isEndDateValid = if (isPeriodNone) true else endDate.isNotBlank()

        earningNameValid && amountValid && periodValid && startDateValid && isEndDateValid
    }

    fun formatWithComma(input: String): String {
        if (input.isBlank()) return ""
        val clean = input.replace(",", "").persianToEnglishDigits()
        return clean.toLongOrNull()
            ?.let { "%,d".format(it) }
            ?: input
    }

    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(
                "${context.getString(R.string.edit_earning)} ${building.name}",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBoxExample(
                        sharedViewModel = sharedViewModel,
                        items = earningsList,
                        selectedItem = selectedEarning,
                        onItemSelected = { selected ->
                            if (selected.earningsId == -1L) {
                                showAddNewEarningNameDialog = true
                            } else {
                                selectedEarning = selected
                                earningsName = selected.earningsName
                                if (selected.amount > 0.0) {
                                    amount = formatWithComma(
                                        selected.amount.toLong().toString()
                                    )
                                }
                                selectedPeriod = selected.period
                            }
                        },
                        label = context.getString(R.string.earning_title),
                        modifier = Modifier.fillMaxWidth(),
                        itemLabel = { it.earningsName },
                        showDeleteFor = { earning ->
                            val c = earningsList.firstOrNull { it.earningsName == earning.earningsName }
                            val bId = c?.forBuildingId ?: 0L
                            val tempEarning = c?.addedBeforeCreateBuilding
                            bId != 0L || tempEarning == true
                        },
                        onDeleteRequest = { earning ->
                            sharedViewModel.deleteEarningSafe(context, earning.earningsId, onDone = {
                                localEarnings = (localEarnings - earning)
                                    .distinctBy { it.earningsName.trim() }
                                selectedEarning = null
                            })
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { value ->
                            val cleaned = value
                                .replace(",", "")
                                .persianToEnglishDigits()

                            if (cleaned.isEmpty()) {
                                amount = ""
                            } else if (cleaned.matches(Regex("^\\d*$"))) {
                                amount = formatWithComma(cleaned)
                            }
                        },
                        label = { RequiredLabel(context.getString(R.string.amount)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val amountDouble = numericAmount()
                    val amountInWords =
                        NumberCommaTransformation().numberToWords(
                            context,
                            amountDouble.toLong()
                        )
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
                        onValueChange = { },
                        label = {
                            RequiredLabel(
                                if (!isPeriodNone) {
                                    context.getString(R.string.start_date)
                                } else {
                                    context.getString(R.string.date)
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    focusManager.clearFocus()
                                    activeDateField = DateField.START
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (!isPeriodNone) {
                        OutlinedTextField(
                            value = endDate,
                            onValueChange = { },
                            label = {
                                RequiredLabel(context.getString(R.string.end_date))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        focusManager.clearFocus()
                                        activeDateField = DateField.END
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null
                                    )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (!errorMessage.isNullOrBlank()) {
                    item { Spacer(Modifier.height(8.dp)) }
                    item {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (showAddNewEarningNameDialog) {
                AddNewEarningNameDialog(
                    buildingId = building.buildingId,
                    onDismiss = { showAddNewEarningNameDialog = false },
                    onSave = { earning ->
                        val normalized = earning.copy(
                            buildingId = earning.buildingId,
                            forBuildingId = earning.forBuildingId,
                            addedBeforeCreateBuilding = earning.addedBeforeCreateBuilding
                        )

                        localEarnings = (localEarnings + normalized)
                            .distinctBy { it.earningsName.trim() }

                        selectedEarning = normalized
                        earningsName = normalized.earningsName

                        coroutineScope.launch {
                            sharedViewModel.insertNewEarnings(context, normalized)
                            showAddNewEarningNameDialog = false
                        }
                    },
                    earningNameExists = { _, cName ->
                        sharedViewModel.earningNameExists(context, null, cName).first()
                    }
                )
            }
        },
        confirmButton = {
            Button(
                enabled = isValid,
                onClick = {
                    val finalAmount = numericAmount()
                    val newEarning = Earnings(
                        earningsId = earnings.earningsId,
                        buildingId = building.buildingId,
                        earningsName = earningsName,
                        amount = finalAmount,
                        period = selectedPeriod ?: Period.MONTHLY,
                        startDate = startDate,
                        endDate = if(isPeriodNone) "" else endDate
                    )
                    onConfirm(newEarning)
                }
            ) {
                Text(
                    text = context.getString(R.string.edit),
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

    if (activeDateField != null) {
        PersianDatePickerDialogContent(
            sharedViewModel = sharedViewModel,
            onDateSelected = { selected ->
                when (activeDateField) {
                    DateField.START -> startDate = selected
                    DateField.END -> endDate = selected
                    null -> {}
                }
                activeDateField = null
            },
            onDismiss = { activeDateField = null }
        )
    }
}


@Composable
fun AddOperationalCostDialog(
    buildingId: Long,
    sharedViewModel: SharedViewModel,
    operationalFundBalance: Double,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    val fixedFundType = FundType.OPERATIONAL
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val chargesCost by sharedViewModel.costsList.collectAsState()
    var selectedCostNames by remember { mutableStateOf<List<String>>(emptyList()) }
//    val addNewCostLabel = context.getString(R.string.add_new_cost)
    Log.d("chargesCost", chargesCost.toString())
    LaunchedEffect(chargesCost) {
        selectedCostNames = chargesCost
            .filter { it.chargeFlag == true }
            .filter { it.fundType == FundType.OPERATIONAL }
            .filter { it.tempAmount == 0.0 }
            .filter { it.buildingId == null  || it.buildingId == 0L }
            .map { it.costName }
        Log.d("selectedCostNames", selectedCostNames.toString())
    }

    LaunchedEffect(buildingId) {
        sharedViewModel.loadFundsForBuilding(context, buildingId)
    }

    var dueDate by remember { mutableStateOf("") }

    val fiscalYear = remember(dueDate) {
        dueDate.takeIf { it.contains("/") }?.split("/")?.getOrNull(0)
    }

    LaunchedEffect(buildingId, fiscalYear) {
        sharedViewModel.preloadTenantCountsForBuilding(
            context = context,
            buildingId = buildingId,
            fiscalYear = fiscalYear
        )
    }

    val overviewState by produceState<BuildingFullDto?>(initialValue = null, key1 = buildingId, key2 = fiscalYear) {
        value = sharedViewModel.loadBuildingOverview(
            context = context,
            buildingId = buildingId,
            fiscalYear = fiscalYear
        )
    }


    var selectedCost by remember { mutableStateOf<Costs?>(null) }
    var selectedCostLabel by remember { mutableStateOf<String?>(null) }

    var customCostTitle by remember { mutableStateOf<String?>(null) }

    var totalAmount by remember { mutableStateOf("") }
    var costName by remember { mutableStateOf("") }
    var documentNumber by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf<Period?>(Period.MONTHLY) }
    var selectedResponsible by remember { mutableStateOf(Responsible.TENANT.getDisplayName(context)) }
    var selectedUnitCalculateMethod by remember { mutableStateOf(context.getString(R.string.fixed)) }
    val selectedUnits = remember { mutableStateListOf<Units>() }

    var showDatePicker by remember { mutableStateOf(false) }
    var showAddNewCostNameDialog by remember { mutableStateOf(false) }

    val responsibleEnum = when (selectedResponsible) {
        Responsible.OWNER.getDisplayName(context) -> Responsible.OWNER
        Responsible.TENANT.getDisplayName(context) -> Responsible.TENANT
        else -> Responsible.OWNER
    }

    val calculateUnitMethod = when (selectedUnitCalculateMethod) {
        CalculateMethod.EQUAL.getDisplayName(context) -> CalculateMethod.EQUAL
        CalculateMethod.DANG.getDisplayName(context) -> CalculateMethod.PEOPLE
        CalculateMethod.AREA.getDisplayName(context) -> CalculateMethod.AREA
        else -> CalculateMethod.EQUAL
    }

    fun formatWithComma(input: String): String {
        if (input.isBlank()) return ""
        val clean = input.replace(",", "")
        return clean.toLongOrNull()
            ?.let { "%,d".format(it) }
            ?: input
    }

    val amountDouble = totalAmount.replace(",", "").toDoubleOrNull() ?: 0.0

    val isValid = remember(
        selectedCost,
        customCostTitle,
        costName,
        totalAmount,
        dueDate,
        selectedUnits.toList()
    ) {
        val clean = totalAmount.replace(",", "")
        val isCostNameValid = costName.isNotBlank()
        val isAmountValid =
            clean.isNotBlank() && clean.toDoubleOrNull()?.let { it > 0.0 } == true
        val isDueDateValid = dueDate.isNotBlank()
        isCostNameValid && isAmountValid && isDueDateValid &&
        selectedCost != null
    }

    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(
                text = context.getString(R.string.add_new_cost),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyColumn {
                    item {
                        OutlinedTextField(
                            value = costName,
                            onValueChange = { costName = it },
                            label = { RequiredLabel(context.getString(R.string.cost_description)) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(Modifier.height(8.dp))

                        ChipGroupShared(
                            selectedItems = selectedCostLabel?.let { listOf(it) } ?: emptyList(),
                            onSelectionChange = { newSelection ->
                                val label = newSelection.firstOrNull() ?: return@ChipGroupShared
                                selectedCostLabel = label
                                val cost = chargesCost.firstOrNull { it.costFor == label }
                                selectedCost = cost
                                if (cost != null && cost.tempAmount > 0.0) {
                                    totalAmount =
                                        formatWithComma(cost.tempAmount.toLong().toString())
                                    selectedPeriod = cost.period
                                }
                            },
                            items = selectedCostNames,
                            modifier = Modifier.padding(vertical = 8.dp),
                            label = context.getString(R.string.cost_for),
                            singleSelection = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = totalAmount,
                            onValueChange = { value ->
                                val cleaned = value.replace(",", "")
                                if (cleaned.isEmpty()) {
                                    totalAmount = ""
                                } else if (cleaned.matches(Regex("^\\d*$"))) {
                                    totalAmount = formatWithComma(cleaned)
                                }
                            },
                            label = { Text(context.getString(R.string.amount)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val amountVal = totalAmount.replace(",", "").toLongOrNull() ?: 0L
                        val amountInWords =
                            NumberCommaTransformation().numberToWords(context, amountVal)
                        Text(
                            text = "$amountInWords ${context.getString(R.string.toman)}",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = documentNumber,
                            onValueChange = { documentNumber = it },
                            label = { RequiredLabel(context.getString(R.string.document_number)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyLarge

                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = dueDate,
                            onValueChange = { },
                            label = {
                                RequiredLabel(
                                    context.getString(R.string.due)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        focusManager.clearFocus()
                                        showDatePicker = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null
                                    )
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = isValid,
                onClick = {
                    val amount = amountDouble

                    coroutineScope.launch {
                        try {
                            val newCost = Costs(
                                buildingId = buildingId,
                                costName = costName,
                                tempAmount = amount,
                                period = selectedPeriod ?: Period.NONE,
                                calculateMethod = calculateUnitMethod,
                                paymentLevel = PaymentLevel.UNIT,
                                responsible = responsibleEnum,
                                fundType = fixedFundType,
                                chargeFlag = false,
                                capitalFlag = false,
                                invoiceFlag = false,
                                dueDate = dueDate,
                                costFor = selectedCost?.costFor ?: "",
                                documentNumber = documentNumber
                            )

                            sharedViewModel.insertCostToServer(
                                context = context,
                                costs = listOf(newCost),
                                debts = emptyList(),
                                buildingId = buildingId,
                                onSuccess = {
                                    onSave(context.getString(R.string.cost_insert_doc))
                                },
                                onError = { e ->
                                    onShowSnackbar(context.getString(R.string.failed))
                                }
                            )
                        } catch (e: Exception) {
                            Log.e("failed", e.toString())
                            onShowSnackbar(context.getString(R.string.failed))
                        }
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
        }
    )

    if (showAddNewCostNameDialog) {
        AddNewCostNameDialog(
            buildingId = buildingId,
            initialName = customCostTitle ?: "",
            onDismiss = { showAddNewCostNameDialog = false },
            onSave = { newCostName ->
                customCostTitle = newCostName
                selectedCost = null
//                selectedCostLabel = addNewCostLabel
                showAddNewCostNameDialog = false
            },
            checkCostNameExists = { bId, cName ->
                sharedViewModel.checkCostNameExists(context, bId, cName).first()
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
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
fun EditOperationalCostDialog(
    buildingId: Long,
    updatedCost: Costs,
    sharedViewModel: SharedViewModel,
    onDismiss: () -> Unit,
    onSave: (Costs) -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    val fixedFundType = FundType.OPERATIONAL
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val chargesCost by sharedViewModel.costsList.collectAsState()
    var selectedCostNames by remember { mutableStateOf<List<String>>(emptyList()) }
//    val addNewCostLabel = context.getString(R.string.edit_operational_doc)

    LaunchedEffect(chargesCost) {
        selectedCostNames = chargesCost
            .filter { it.chargeFlag == true }
            .filter { it.fundType == FundType.OPERATIONAL }
            .filter { it.tempAmount == 0.0 }
            .filter { it.buildingId == null   || it.buildingId == 0L}
            .map { it.costName }
    }


    var dueDate by remember { mutableStateOf(updatedCost.dueDate) }

    val fiscalYear = remember(dueDate) {
        dueDate.takeIf { it.contains("/") }?.split("/")?.getOrNull(0)
    }



//    var selectedCost by remember { mutableStateOf<Costs?>(null) }
    var selectedCostLabel by remember { mutableStateOf<String?>(null) }

    var customCostTitle by remember { mutableStateOf<String?>(null) }

    var totalAmount by remember { mutableStateOf(formatWithComma(updatedCost.tempAmount.toLong().toString())) }

    var costName by remember { mutableStateOf(updatedCost.costName) }
    var documentNumber by remember { mutableStateOf(updatedCost.documentNumber) }
    var costFor by remember { mutableStateOf(updatedCost.costFor) }
    var selectedPeriod by remember { mutableStateOf<Period?>(Period.MONTHLY) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showAddNewCostNameDialog by remember { mutableStateOf(false) }



    fun formatWithComma(input: String): String {
        if (input.isBlank()) return ""
        val clean = input.replace(",", "")
        return clean.toLongOrNull()
            ?.let { "%,d".format(it) }
            ?: input
    }

    val amountDouble = totalAmount.replace(",", "").toDoubleOrNull() ?: 0.0

    val isValid = remember(
        updatedCost,
        customCostTitle,
        costName,
        totalAmount,
        dueDate
    ) {
        val clean = totalAmount.replace(",", "")
        val isCostNameValid = costName.isNotBlank()
        val isAmountValid =
            clean.isNotBlank() && clean.toDoubleOrNull()?.let { it > 0.0 } == true
        val isDueDateValid = dueDate.isNotBlank()
        isCostNameValid && isAmountValid && isDueDateValid
    }

    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(
                text = context.getString(R.string.edit_operational_doc),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyColumn {
                    item {
                        OutlinedTextField(
                            value = costName,
                            onValueChange = { costName = it },
                            label = { RequiredLabel(context.getString(R.string.cost_description)) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(Modifier.height(8.dp))

                        ChipGroupShared(
                            selectedItems = costFor.let { listOf(it) },
                            onSelectionChange = { newSelection ->
                                val label = newSelection.firstOrNull() ?: return@ChipGroupShared
                                selectedCostLabel = label
                                costFor = label
                                val cost = chargesCost.firstOrNull { it.costFor == label }
                                if (cost != null && cost.tempAmount > 0.0) {
                                    totalAmount =
                                        formatWithComma(cost.tempAmount.toLong().toString())
                                    selectedPeriod = cost.period
                                }
                            },
                            items = selectedCostNames,
                            modifier = Modifier.padding(vertical = 8.dp),
                            label = context.getString(R.string.cost_for),
                            singleSelection = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = totalAmount,
                            onValueChange = { value ->
                                val cleaned = value.replace(",", "")
                                if (cleaned.isEmpty()) {
                                    totalAmount = ""
                                } else if (cleaned.matches(Regex("^\\d*$"))) {
                                    totalAmount = formatWithComma(cleaned)
                                }
                            },
                            label = { Text(context.getString(R.string.amount)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val amountVal = totalAmount.replace(",", "").toLongOrNull() ?: 0L
                        val amountInWords =
                            NumberCommaTransformation().numberToWords(context, amountVal)
                        Text(
                            text = "$amountInWords ${context.getString(R.string.toman)}",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = documentNumber,
                            onValueChange = { documentNumber = it },
                            label = { RequiredLabel(context.getString(R.string.document_number)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyLarge

                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = dueDate,
                            onValueChange = { },
                            label = {
                                RequiredLabel(
                                    context.getString(R.string.due)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        focusManager.clearFocus()
                                        showDatePicker = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null
                                    )
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = isValid,
                onClick = {
                    val amount = amountDouble

                    coroutineScope.launch {
                        try {
                            val newCost = updatedCost.copy(
                                costName = costName,
                                tempAmount = amount,
                                period = selectedPeriod ?: Period.NONE,
                                calculateMethod = CalculateMethod.NONE,
                                paymentLevel = PaymentLevel.UNIT,
                                responsible = Responsible.TENANT,
                                fundType = fixedFundType,
                                chargeFlag = false,
                                capitalFlag = false,
                                invoiceFlag = false,
                                dueDate = dueDate,
                                costFor = costFor,
                                documentNumber = documentNumber
                            )
                            onSave(newCost)
                        } catch (e: Exception) {
                            Log.e("failed", e.toString())
                            onShowSnackbar(context.getString(R.string.failed))
                        }
                    }
                }
            ) {
                Text(
                    text = context.getString(R.string.edit),
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

    if (showAddNewCostNameDialog) {
        AddNewCostNameDialog(
            buildingId = buildingId,
            initialName = customCostTitle ?: "",
            onDismiss = { showAddNewCostNameDialog = false },
            onSave = { newCostName ->
                customCostTitle = newCostName
//                selectedCostLabel = addNewCostLabel
                showAddNewCostNameDialog = false
            },
            checkCostNameExists = { bId, cName ->
                sharedViewModel.checkCostNameExists(context, bId, cName).first()
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
            onDismiss = { showDatePicker = false }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCapitalCostDialog(
    sharedViewModel: SharedViewModel,
    onDismiss: () -> Unit,
    onSave: (
        Costs,
        String,
        String,
        Period,
        CalculateMethod,
        CalculateMethod,
        Responsible,
        List<Long>,
        List<Long>,
        String,
        String,
        String
    ) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var selectedCost by remember { mutableStateOf<Costs?>(null) }
    var totalAmount by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf<Period?>(Period.MONTHLY) }
    var selectedResponsible by remember { mutableStateOf(Responsible.OWNER.getDisplayName(context)) }
    var selectedCalculateMethod by remember { mutableStateOf(context.getString(R.string.fixed)) }
    var selectedUnitCalculateMethod by remember { mutableStateOf(context.getString(R.string.fixed)) }
    val selectedUnits = remember { mutableStateListOf<Units>() }
    val selectedOwners = remember { mutableStateListOf<User>() }
    var dueDate by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var costName by remember { mutableStateOf("") }
    var documentNumber by remember { mutableStateOf("") }
    var selectedCostLabel by remember { mutableStateOf<String?>(null) }

    val costs by sharedViewModel.costsList.collectAsState()
    Log.d("costs", costs.toString())
    var selectedCostNames by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(costs) {
        selectedCostNames = costs
            .filter { it.chargeFlag == false }
            .filter { it.fundType == FundType.CAPITAL }
            .filter { it.tempAmount == 0.0 }
            .filter { it.buildingId == null  || it.buildingId == 0L}
            .map { it.costName }
    }


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

    fun formatWithComma(input: String): String {
        if (input.isBlank()) return ""
        val clean = input.replace(",", "")
        return clean.toLongOrNull()
            ?.let { "%,d".format(it) }
            ?: input
    }

    val isValid = selectedCost != null &&
            totalAmount.replace(",", "").isNotBlank() &&
            totalAmount.replace(",", "").toDoubleOrNull()?.let { it > 0.0 } == true &&
            dueDate.isNotBlank()


    AlertDialog(
        onDismissRequest = {},
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
                        value = costName,
                        onValueChange = { costName = it },
                        label = { RequiredLabel(context.getString(R.string.cost_description)) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(Modifier.height(8.dp))
                    Log.d("selectedCostNames", selectedCostNames.toString())
                    ChipGroupShared(
                        selectedItems = selectedCostLabel?.let { listOf(it) } ?: emptyList(),
                        onSelectionChange = { newSelection ->
                            val label = newSelection.firstOrNull() ?: return@ChipGroupShared
                            selectedCostLabel = label
                            val cost = costs.firstOrNull { it.costFor == label }
                            selectedCost = cost
                            if (cost != null && cost.tempAmount > 0.0) {
                                totalAmount = formatWithComma(cost.tempAmount.toLong().toString())
                                selectedPeriod = cost.period
                            }
                        },
                        items = selectedCostNames,
                        modifier = Modifier.padding(vertical = 8.dp),
                        label = context.getString(R.string.cost_for),
                        singleSelection = true
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = totalAmount,
                        onValueChange = { value ->
                            val cleaned = value.replace(",", "")
                            if (cleaned.isEmpty()) {
                                totalAmount = ""
                            } else if (cleaned.matches(Regex("^\\d*$"))) {
                                totalAmount = formatWithComma(cleaned)
                            }
                        },
                        label = { RequiredLabel(context.getString(R.string.amount)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val amountVal = totalAmount.replace(",", "").toLongOrNull() ?: 0L
                    val amountInWords =
                        NumberCommaTransformation().numberToWords(context, amountVal)
                    Text(
                        text = "$amountInWords ${context.getString(R.string.toman)}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = documentNumber,
                        onValueChange = { documentNumber = it },
                        label = { RequiredLabel(context.getString(R.string.document_number)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { },
                        label = { RequiredLabel(context.getString(R.string.due)) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    focusManager.clearFocus()
                                    showDatePicker = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null
                                )
                            }
                        }
                    )

                    Spacer(Modifier.height(8.dp))
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
                        costName,
                        totalAmount.replace(",", ""),
                        selectedPeriod ?: Period.NONE,
                        calculateMethod,
                        calculateUnitMethod,
                        responsibleEnum,
                        selectedUnits.map { it.unitId },
                        selectedOwners.map { it.userId },
                        dueDate,
                        documentNumber,
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
        }
    )

    if (showDatePicker) {
        PersianDatePickerDialogContent(
            sharedViewModel = sharedViewModel,
            onDateSelected = { selected ->
                dueDate = selected
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCapitalCostDialog(
    updatedCost: Costs,
    sharedViewModel: SharedViewModel,
    onDismiss: () -> Unit,
    onSave: (
        Costs
    ) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var totalAmount by remember { mutableStateOf(formatWithComma(updatedCost.tempAmount.toLong().toString())) }
    var selectedCostLabel by remember { mutableStateOf<String?>(updatedCost.costFor.takeIf { it.isNotBlank() }) }

    var selectedPeriod by remember { mutableStateOf<Period?>(Period.MONTHLY) }
    val selectedUnits = remember { mutableStateListOf<Units>() }
    val selectedOwners = remember { mutableStateListOf<User>() }
    var dueDate by remember { mutableStateOf(updatedCost.dueDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    var costName by remember { mutableStateOf(updatedCost.costName) }
    var costFor by remember { mutableStateOf(updatedCost.costFor) }
    var documentNumber by remember { mutableStateOf(updatedCost.documentNumber) }

    val costs by sharedViewModel.costsList.collectAsState()

    var selectedCostNames by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(costs) {
        selectedCostNames = costs
            .filter { it.chargeFlag == false }
            .filter { it.fundType == FundType.CAPITAL }
            .filter { it.tempAmount == 0.0 }
            .filter { it.buildingId == null  || it.buildingId == 0L}
            .map { it.costName }
    }


    fun formatWithComma(input: String): String {
        if (input.isBlank()) return ""
        val clean = input.replace(",", "")
        return clean.toLongOrNull()
            ?.let { "%,d".format(it) }
            ?: input
    }

    val isValid = remember(costName, costFor, totalAmount, dueDate) {
        val clean = totalAmount.replace(",", "")
        costName.isNotBlank() &&
                costFor.isNotBlank() &&
                clean.isNotBlank() &&
                (clean.toDoubleOrNull() ?: 0.0) > 0.0 &&
                dueDate.isNotBlank()
    }


    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(
                text = context.getString(R.string.edit_capital_doc),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            LazyColumn {
                item {
                    OutlinedTextField(
                        value = costName,
                        onValueChange = { costName = it },
                        label = { RequiredLabel(context.getString(R.string.cost_description)) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(Modifier.height(8.dp))

                    ChipGroupShared(
                        selectedItems = costFor.let { listOf(it) },
                        onSelectionChange = { newSelection ->
                            val label = newSelection.firstOrNull() ?: return@ChipGroupShared
                            selectedCostLabel = label
                            costFor = label
                            val cost = costs.firstOrNull { it.costFor == label }
//                            selectedCost = cost
//                            Log.d("selectedCost", selectedCost.toString())
                            if (cost != null && cost.tempAmount > 0.0) {
                                totalAmount = formatWithComma(cost.tempAmount.toLong().toString())
                                selectedPeriod = cost.period
                            }
                        },
                        items = selectedCostNames,
                        modifier = Modifier.padding(vertical = 8.dp),
                        label = context.getString(R.string.cost_for),
                        singleSelection = true
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = totalAmount.toString(),
                        onValueChange = { value ->
                            val cleaned = value.replace(",", "")
                            if (cleaned.isEmpty()) {
                                totalAmount = ""
                            } else if (cleaned.matches(Regex("^\\d*$"))) {
                                totalAmount = formatWithComma(cleaned)
                            }
                        },
                        label = { RequiredLabel(context.getString(R.string.amount)) },
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

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = documentNumber,
                        onValueChange = { documentNumber = it },
                        label = { RequiredLabel(context.getString(R.string.document_number)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { },
                        label = { RequiredLabel(context.getString(R.string.due)) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    focusManager.clearFocus()
                                    showDatePicker = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null
                                )
                            }
                        }
                    )

                    Spacer(Modifier.height(8.dp))
                }
            }
        },
        confirmButton = {
            Button(
                enabled = isValid,
                onClick = {
                    var newCost = updatedCost.copy( costName = costName, tempAmount = totalAmount.replace(",","").toDouble()
                    , dueDate = dueDate, documentNumber = documentNumber, costFor = costFor)
                    onSave(
                        newCost
                    )
                }
            ) {
                Text(
                    text = context.getString(R.string.edit),
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

    if (showDatePicker) {
        PersianDatePickerDialogContent(
            sharedViewModel = sharedViewModel,
            onDateSelected = { selected ->
                dueDate = selected
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}


@Composable
fun AddNewCostNameDialog(
    buildingId: Long,
    initialName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    checkCostNameExists: suspend (buildingId: Long, costName: String) -> Boolean
) {
    val context = LocalContext.current
    var costName by remember { mutableStateOf(initialName) }
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
                        Text(
                            text = context.getString(R.string.cancel),
                            style = MaterialTheme.typography.bodyLarge
                        )
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
fun AddNewEarningNameDialog(
    buildingId: Long?,
    onDismiss: () -> Unit,
    onSave: (Earnings) -> Unit,
    earningNameExists: suspend (Long?, String) -> Boolean
) {
    val context = LocalContext.current
    var earningName by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Dialog(onDismissRequest = {}) {
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
                        Text(
                            text = context.getString(R.string.cancel),
                            style = MaterialTheme.typography.bodyLarge
                        )
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
                                        buildingId = null,
                                        amount = 0.0,
                                        startDate = "",
                                        endDate = "",
                                        addedBeforeCreateBuilding = true,
                                        forBuildingId = buildingId,
                                        period = Period.NONE

                                    )
                                    onSave(newEarning)
                                }
                            }
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


fun formatWithComma(input: String): String {
    if (input.isBlank()) return ""
    val clean = input.replace(",", "")
    return clean.toLongOrNull()
        ?.let { "%,d".format(it) }
        ?: input
}

