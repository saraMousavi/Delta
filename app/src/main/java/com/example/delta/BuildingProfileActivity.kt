package com.example.delta

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.example.delta.data.entity.BuildingTypes
import com.example.delta.data.entity.BuildingUsages
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.CityComplex
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Debts
import com.example.delta.data.entity.Earnings
import com.example.delta.data.entity.Owners
import com.example.delta.data.entity.PhonebookEntry
import com.example.delta.data.entity.BuildingTabItem
import com.example.delta.data.entity.BuildingTabType
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
import ir.hamsaa.persiandatepicker.util.PersianCalendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.NumberFormat
import java.util.Locale


class BuildingProfileActivity : ComponentActivity() {


    private val buildingViewModel: BuildingsViewModel by viewModels {
        BuildingsViewModelFactory(application = this.application)
    }

    val sharedViewModel: SharedViewModel by viewModels()
    private val buildingTypeViewModel: BuildingTypeViewModel by viewModels()
    private val buildingUsageViewModel: BuildingUsageViewModel by viewModels()
    var buildingTypeName: String = ""
    var buildingUsageName: String = ""

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        val building = intent.getParcelableExtra<Parcelable>("BUILDING_DATA") as? Buildings

        buildingTypeName = intent.getStringExtra("BUILDING_TYPE_NAME") ?: "Unknown"
        buildingUsageName = intent.getStringExtra("BUILDING_USAGE_NAME") ?: "Unknown"
        setContent {
            AppTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MaterialTheme {
                        building?.let { BuildingProfileScreen(it) }
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
            if (permissionLevelOwnerTab == PermissionLevel.FULL || permissionLevelOwnerTab == PermissionLevel.WRITE
                || permissionLevelOwnerTab == PermissionLevel.READ
            ) {
                BuildingTabItem(context.getString(R.string.owners), BuildingTabType.OWNERS)
            } else null,
            if (permissionLevelUnitsTab == PermissionLevel.FULL || permissionLevelUnitsTab == PermissionLevel.WRITE
                || permissionLevelUnitsTab == PermissionLevel.READ
            ) {
                BuildingTabItem(context.getString(R.string.units), BuildingTabType.UNITS)
            } else null,
            if (permissionLevelTenantsTab == PermissionLevel.FULL || permissionLevelTenantsTab == PermissionLevel.WRITE
                || permissionLevelTenantsTab == PermissionLevel.READ
            ) {
                BuildingTabItem(context.getString(R.string.tenants), BuildingTabType.TENANTS)
            } else null,
            if (permissionLevelFundTab == PermissionLevel.FULL || permissionLevelFundTab == PermissionLevel.WRITE
                || permissionLevelFundTab == PermissionLevel.READ
            ) {
                BuildingTabItem(context.getString(R.string.funds), BuildingTabType.FUNDS)
            }
            else null,
            if (permissionLevelTransactionTab == PermissionLevel.FULL || permissionLevelTransactionTab == PermissionLevel.WRITE
                || permissionLevelTransactionTab == PermissionLevel.READ
            ) {
                BuildingTabItem(context.getString(R.string.transaction), BuildingTabType.TRANSACTIONS)
            } else null,
            if (permissionLevelPhonebookTab == PermissionLevel.FULL || permissionLevelPhonebookTab == PermissionLevel.WRITE
                || permissionLevelPhonebookTab == PermissionLevel.READ
            ) {
                BuildingTabItem(context.getString(R.string.phone_number), BuildingTabType.PHONEBOOK_TAB)
            }
            else null
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
                    BuildingTabType.UNITS -> UnitsTab(building, sharedViewModel)
                    BuildingTabType.TENANTS -> TenantsTab(building, sharedViewModel)
                    BuildingTabType.FUNDS -> FundsTab(
                        building = building, sharedViewModel = sharedViewModel,
                        onOpenCostDetail = { costId, fundType ->

                            coroutineScope.launch {
                                sharedViewModel.getCost(costId).collect { cost ->
                                    val intent =
                                        Intent(context, CostDetailActivity::class.java).apply {
                                            putExtra("COST_DATA", cost as Parcelable)
                                            putExtra("FUND_TYPE", fundType.ordinal)
                                        }
                                    context.startActivity(intent)
                                }
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
        // Initialize editableBuilding from building param on building change:
        var editableBuilding by remember(building) { mutableStateOf(building) }

        val userId = Preference().getUserId(context)

        val buildingTypes by buildingTypeViewModel.getAllBuildingType()
            .collectAsState(initial = emptyList())
        val buildingUsages by buildingUsageViewModel.getAllBuildingUsage()
            .collectAsState(initial = emptyList())

        val cityComplexes by sharedViewModel.getAllCityComplex().collectAsState(initial = emptyList())
        // Maintain local selected items for dropdowns
        val selectedBuildingType = remember(buildingTypes, editableBuilding.buildingTypeId) {
            buildingTypes.find { it.buildingTypeId == editableBuilding.buildingTypeId }
        }

        val selectedBuildingUsage = remember(buildingUsages, editableBuilding.buildingUsageId) {
            buildingUsages.find { it.buildingUsageId == editableBuilding.buildingUsageId }
        }

        val selectedCityComplex = remember(cityComplexes, editableBuilding.complexId) {
            cityComplexes.find { it.complexId == editableBuilding.complexId }
        }


        val buildingTypeName = context.getString(R.string.city_complex)
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


        val chargesCost by sharedViewModel.getCostsForBuildingWithChargeFlag(buildingId = building.buildingId)
            .collectAsState(initial = emptyList())
        val fileList by sharedViewModel.getBuildingFiles(building.buildingId)
            .collectAsState(initial = emptyList())

        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentPadding = PaddingValues(bottom = 72.dp) // Padding for bottom buttons
                ) {
                    // Group 1: Building Name & Address
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
                                if (permissionLevelBuildingName == PermissionLevel.FULL || permissionLevelBuildingName == PermissionLevel.WRITE) {
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

                    // Group 2: Province & State
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
                                    // Get the list of all provinces
                                    val provinces = IranianLocations.provinces.keys.toList()
                                    val availableStates =
                                        IranianLocations.provinces[editableBuilding.province]
                                            ?: emptyList()
                                    ExposedDropdownMenuBoxExample(
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

                                    // State Selector
                                    ExposedDropdownMenuBoxExample(
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

                    // Group 3: Building Type & Usage
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
                                        items = buildingTypes + BuildingTypes(0, context.getString(R.string.addNew)),
                                        selectedItem = selectedBuildingType,
                                        onItemSelected = {
                                            if (it.buildingTypeName == context.getString(R.string.addNew)) {
                                                showBuildingTypeDialog = true
                                            } else {
                                                editableBuilding = editableBuilding.copy(buildingTypeId = it.buildingTypeId)
                                            }
                                        },
                                        label = context.getString(R.string.building_type),
                                        modifier = Modifier.fillMaxWidth(),
                                        itemLabel = { it.buildingTypeName }
                                    )

                                    if (selectedBuildingType?.buildingTypeName == buildingTypeName) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            ExposedDropdownMenuBoxExample(
                                                items = cityComplexes + CityComplex(
                                                    complexId = 0L,
                                                    name = context.getString(R.string.addNew),
                                                    address = null
                                                ),
                                                selectedItem = selectedCityComplex,
                                                onItemSelected = {
                                                    if (it.name == context.getString(R.string.addNew)) {
                                                        showAddCityComplexDialog = true
                                                    } else {
                                                        editableBuilding =
                                                            editableBuilding.copy(complexId = it.complexId)
                                                    }
                                                },
                                                label = context.getString(R.string.city_complex),
                                                modifier = Modifier.fillMaxWidth(),
                                                itemLabel = { it.name }
                                            )
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    ExposedDropdownMenuBoxExample(
                                        items = buildingUsages + BuildingUsages(
                                            0,
                                            context.getString(R.string.addNew)
                                        ), // Add "Add New" option
                                        selectedItem = selectedBuildingUsage,
                                        onItemSelected = {
                                            if (it.buildingUsageName == context.getString(R.string.addNew)) {
                                                // Open dialog to add new building usage
                                                showBuildingUsageDialog = true
                                            } else {
                                                editableBuilding =
                                                    editableBuilding.copy(buildingUsageId = it.buildingUsageId)
                                            }
                                        },
                                        label = context.getString(R.string.building_usage),
                                        modifier = Modifier
                                            .fillMaxWidth(1f),
                                        itemLabel = { it.buildingUsageName }
                                    )
                                } else {
                                    Text(
                                        text = "${context.getString(R.string.building_type)}: ${selectedBuildingType?.buildingTypeName ?: "-"}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    if (selectedBuildingType?.buildingTypeName == buildingTypeName) {
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

                    // Group 4: Charges Parameter
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
                                chargesCost.forEachIndexed { index, cost ->
                                    Text(
                                        text = cost.costName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                    if (index != chargesCost.lastIndex) {
                                        Spacer(Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Group 5: Documents
                    if (fileList.isNotEmpty() && (permissionLevelDoc == PermissionLevel.FULL || permissionLevelDoc == PermissionLevel.WRITE)) {
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
                                Row(modifier = Modifier.padding(16.dp)) {
                                    fileList.forEach { file ->
                                        val fileObj = File(file.fileUrl)
                                        val extension = fileObj.extension.lowercase()
                                        val painter = when (extension) {
                                            "jpg", "jpeg", "png", "gif", "bmp", "webp" ->
                                                rememberAsyncImagePainter(fileObj)

                                            else -> null
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { openFile(context, file.fileUrl) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (painter != null) {
                                                Image(
                                                    painter = painter,
                                                    contentDescription = "Image file",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                val icon = when (extension) {
                                                    "pdf" -> Icons.Default.PictureAsPdf
                                                    "xls", "xlsx" -> Icons.Default.TableChart
                                                    "doc", "docx" -> Icons.Default.Description
                                                    else -> Icons.Default.InsertDriveFile
                                                }
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = "File icon",
                                                    tint = Color.Gray,
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            }
                                        }
                                        Spacer(Modifier.width(8.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Editing action buttons at bottom
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
                                editableBuilding = building // revert changes
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

            // Floating Action Button to toggle editing
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
                    val newComplex = CityComplex(name = newName, address = newAddress)
                    sharedViewModel.insertCityComplex(newComplex) { id ->
                        val insertedComplex = cityComplexes.find { it.complexId == id }
                        if (insertedComplex != null) {
//                            selectedCityComplex = insertedComplex
                        }
                    }
                    showAddCityComplexDialog = false
                }
            )
        }

        if (showBuildingTypeDialog) {
            AddItemDialog(
                onDismiss = { showBuildingTypeDialog = false },
                onInsert = { newItem ->
                    val newType = BuildingTypes(buildingTypeName = newItem)
                    buildingTypeViewModel.insertBuildingType(newType) // Add the new item to the list
                }
            )
        }

        if (showBuildingUsageDialog) {
            AddItemDialog(
                onDismiss = { showBuildingUsageDialog = false },
                onInsert = { name ->
                    val newUsage = BuildingUsages(buildingUsageName = name)
                    buildingUsageViewModel.insertBuildingUsage(newUsage)
                }
            )
        }

    }


    @Composable
    fun FundsTab(
        building: Buildings,
        sharedViewModel: SharedViewModel,
        onOpenCostDetail: (costId: Long, fundType: FundType) -> Unit // callback for opening detail screen
    ) {
        val context = LocalContext.current

        // Observe Operational and Capital Fund balances (StateFlow or LiveData exposed by ViewModel)
        val operationalFund by sharedViewModel.getOperationalOrCapitalFundBalance(
            buildingId = building.buildingId,
            fundType = FundType.OPERATIONAL
        ).collectAsState(initial = 0)
        val capitalFund by sharedViewModel.getOperationalOrCapitalFundBalance(
            buildingId = building.buildingId,
            FundType.CAPITAL
        ).collectAsState(initial = 0)

        // Costs separated by charge type - Collect flows from ViewModel
        val operationalCosts by sharedViewModel.getPendingCostsByFundType(
            building.buildingId,
            FundType.OPERATIONAL
        )
            .collectAsState(initial = emptyList())
        val capitalCosts by sharedViewModel.getPendingCostsByFundType(
            building.buildingId,
            FundType.CAPITAL
        )
            .collectAsState(initial = emptyList())

        // Tab and UI state
        var selectedTab by remember { mutableIntStateOf(0) }
        val tabTitles =
            listOf(context.getString(R.string.incomes), context.getString(R.string.costs))

        // Load funds balances on first composition
        LaunchedEffect(building.buildingId) {
            sharedViewModel.loadFundBalances(building.buildingId)
        }

        Scaffold(
            floatingActionButton = {
                // FAB to open Add Cost Dialog
                FloatingActionButton(
                    onClick = { buildingViewModel.showCostDialog(building.buildingId) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Cost")
                }
            }
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Show Fund Balances in Cards

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier) {
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
                }

                // Tabs Row for "Incomes" / "Costs"

                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = { HorizontalDivider(thickness = 2.dp) },
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .background(
                                    if (selectedTab == index) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surface
                                )
                                .padding(vertical = 8.dp),
                            text = {
                                Text(
                                    text = title.uppercase(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (selectedTab == index) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Content Area

                when (selectedTab) {
                    0 -> {
                        EarningsSection(
                            buildingId = building.buildingId,
                            sharedViewModel = sharedViewModel,
                            onInvoiceClicked = { earning ->
                                sharedViewModel.invoiceEarning(earning)
                            }
                        )
                    }

                    1 -> {
                        // Costs section - show Capital and Operational costs separately

                        Text(
                            text = context.getString(R.string.capital_funds),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        if (capitalCosts.isEmpty()) {
                            Text(
                                context.getString(R.string.no_capital_cost),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .heightIn(max = 220.dp)
                                    .padding(16.dp)
                            ) {
                                items(capitalCosts) { cost ->
                                    CostListItem(cost = cost) {
                                        onOpenCostDetail(cost.costId, FundType.CAPITAL)
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = context.getString(R.string.operation_funds),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        if (operationalCosts.isEmpty()) {
                            Text(
                                context.getString(R.string.no_operation_cost),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 220.dp)
                            ) {
                                items(operationalCosts) { cost ->
                                    CostListItem(cost = cost) {
                                        onOpenCostDetail(cost.costId, FundType.OPERATIONAL)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Show dialogs for Adding Cost and Earnings
        if (buildingViewModel.showCostDialog.value) {
            AddCostDialog(
                buildingId = building.buildingId,
                sharedViewModel = sharedViewModel,
                onDismiss = { buildingViewModel.hideDialogs() },
                onSave = { selectedCost, amount, period, fundFlag, calculateMethod, calculatedUnitMethod, responsible, selectedUnits, selectedOwners, dueDate ->
                    // Insert cost and debts using selectedCost info
                    sharedViewModel.insertDebtPerNewCost(
                        buildingId = building.buildingId,
                        amount = amount,
                        name = selectedCost.costName,
                        period = period,
                        fundType = fundFlag,
                        paymentLevel = selectedCost.paymentLevel,
                        calculateMethod = calculateMethod,
                        calculatedUnitMethod = calculatedUnitMethod,
                        responsible = responsible,
                        dueDate = dueDate,
                        selectedUnitIds = selectedUnits.toList(),
                        selectedOwnerIds = selectedOwners.toList()
                    )
                    buildingViewModel.hideDialogs()
                }
            )
        }

        if (buildingViewModel.showEarningsDialog.value) {
            EarningsDialog(
                building = building,
                onDismiss = { buildingViewModel.hideDialogs() },
                onConfirm = { earning ->
                    buildingViewModel.insertEarnings(earning)
                    buildingViewModel.hideDialogs()
                },
                sharedViewModel = sharedViewModel
            )
        }
    }


    @Composable
    fun CostListItem(cost: Costs, onClick: () -> Unit) {
        val context = LocalContext.current
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { onClick() },
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cost.costName,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${context.getString(R.string.amount)}: ${formatNumberWithCommas(cost.tempAmount)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowBackIos,
                    contentDescription = "Go to details",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    @Composable
    fun TenantsTab(building: Buildings, sharedViewModel: SharedViewModel) {
        var showTenantDialog by remember { mutableStateOf(false) }
        val context = LocalContext.current
        val tenants by sharedViewModel.getTenantsForBuilding(building.buildingId)
            .collectAsState(initial = emptyList())
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                items(tenants) { tenant ->
                    val unit = sharedViewModel.getUnitForTenant(tenant.tenantId).collectAsState(initial = null)
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
                                    Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            )
                        },
                        activity = context.findActivity(),
                        onClick = {
                            val intent = Intent(context, TenantsDetailsActivity::class.java)
                            intent.putExtra("UNIT_DATA", unit.value!!.unitId)
                            intent.putExtra("TENANT_DATA", tenant.tenantId)
                            context.startActivity(intent)
                        }
                    )
                }
            }

            FloatingActionButton(
                onClick = { showTenantDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
//                containerColor = Color(context.getColor(R.color.secondary_color)),
//                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(Icons.Filled.Add, "Add")
            }

            if (showTenantDialog) {
                val units by sharedViewModel.getUnitsForBuilding(building.buildingId)
                    .collectAsState(initial = emptyList())
                TenantDialog(
                    sharedViewModel = sharedViewModel,
                    units = units,
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
    fun EarningsSection(
        buildingId: Long,
        sharedViewModel: SharedViewModel,
        onInvoiceClicked: (Earnings) -> Unit,
        modifier: Modifier = Modifier
    ) {
        val earnings by sharedViewModel.getNotInvoicedEarnings(buildingId)
            .collectAsState(initial = emptyList())
        val context = LocalContext.current

        Column(modifier = modifier.padding(16.dp)) {
            if (earnings.isEmpty()) {
                Text(
                    text = context.getString(R.string.no_earnings_pending),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn {
                    items(earnings) { earning ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = earning.earningsName,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "${formatNumberWithCommas(earning.amount)} ${
                                            context.getString(
                                                R.string.toman
                                            )
                                        }",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "${context.getString(R.string.due)}: ${earning.dueDate}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Button(onClick = { onInvoiceClicked(earning) }) {
                                    Text(
                                        text = context.getString(R.string.invoices),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }


//    @OptIn(ExperimentalMaterial3Api::class)
//    @Composable
//    fun UnitsTab(
//        building: Buildings,
//        sharedViewModel: SharedViewModel,
//        unitsViewModel: UnitsViewModel
//    ) {
//        var showUnitDialog by remember { mutableStateOf(false) }
//        val context = LocalContext.current
//        val units by sharedViewModel.getUnitsForBuilding(building.buildingId)
//            .collectAsState(initial = emptyList())
//        Log.d("building.buildingId", building.buildingId.toString())
//        val expandedUnits = remember { mutableStateMapOf<Long, Boolean>() }
//
//        Box(modifier = Modifier.fillMaxSize()) {
//            LazyColumn(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp)
//            ) {
//                items(units) { unit ->
//                    val isExpanded = expandedUnits[unit.unitId] ?: false
//
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .clickable {
//                                expandedUnits[unit.unitId] = !isExpanded
//                            }
//                            .padding(8.dp)
//                    ) {
//                        // Unit Basic Info
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween
//                        ) {
//                            Text(
//                                text = "${context.getString(R.string.unit_name)}: ${unit.unitNumber}",
//                                style = MaterialTheme.typography.bodyLarge
//                            )
//                            Icon(
//                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
//                                contentDescription = "Expand/Collapse"
//                            )
//                        }
//
//                        // Expanded Debt Details
//                        AnimatedVisibility(
//                            visible = isExpanded,
//                            enter = fadeIn() + expandVertically(),
//                            exit = fadeOut() + shrinkVertically()
//                        ) {
//                            Column {
//                                // Get all debts for this unit
//                                val debts by sharedViewModel.getDebtsOneUnit(unit.unitId)
//                                    .collectAsState(initial = emptyList())
//                                Log.d("unit.unitId", unit.unitId.toString())
//                                Log.d("debts", debts.toString())
//                                val allDebts by sharedViewModel.getAllDebts()
//                                    .collectAsState(initial = emptyList())
//                                Log.d("all debts", allDebts.toString())
//
//                                Spacer(modifier = Modifier.height(8.dp))
//
//                                if (debts.isEmpty()) {
//                                    Text(
//                                        text = context.getString(R.string.no_costs_recorded),
//                                        style = MaterialTheme.typography.bodyLarge,
//                                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                                    )
//                                } else {
//                                    debts.forEach { debt ->
//                                        Row(
//                                            modifier = Modifier
//                                                .fillMaxWidth()
//                                                .padding(8.dp),
//                                            horizontalArrangement = Arrangement.SpaceBetween
//                                        ) {
//                                            Column {
//                                                Text(
//                                                    text = debt.description,
//                                                    style = MaterialTheme.typography.bodyLarge
//                                                )
//                                                Text(
//                                                    text = "${context.getString(R.string.due)}: ${debt.dueDate}",
//                                                    style = MaterialTheme.typography.bodyLarge,
//                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                                                )
//                                            }
//                                            Text(
//                                                text = "${debt.amount}",
//                                                style = MaterialTheme.typography.bodyLarge,
//                                                color = if (debt.amount > 0) Color.Red else Color.Green
//                                            )
//                                        }
//                                        Divider(
//                                            modifier = Modifier.padding(horizontal = 8.dp),
//                                            thickness = 0.5.dp
//                                        )
//                                    }
//                                }
//                            }
//                        }
//                    }
//                    Divider()
//                }
//            }
//
//            FloatingActionButton(
//                onClick = { showUnitDialog = true },
//                modifier = Modifier
//                    .align(Alignment.BottomEnd)
//                    .padding(16.dp),
//                containerColor = Color(context.getColor(R.color.secondary_color)),
//                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
//            ) {
//                Icon(Icons.Filled.Add, "Add")
//            }
//
//            if (showUnitDialog) {
//                UnitDialog(
//                    onDismiss = { showUnitDialog = false },
//                    onAddUnit = { newUnit ->
//                        newUnit.buildingId = building.buildingId
////                        sharedViewModel.(newUnit)
//                        showUnitDialog = false
//                    }
//                )
//            }
//        }
//    }

    @Composable
    fun UnitsTab(
        building: Buildings,
        sharedViewModel: SharedViewModel
    ) {
        val units by sharedViewModel.getUnitsForBuilding(building.buildingId)
            .collectAsState(initial = emptyList())

        LazyColumn {
            items(units) { unit ->
                UnitItem(unit = unit) {

                }
            }
        }
    }

    @Composable
    fun UnitItem(
        unit: Units,
        onClick: () -> Unit
    ) {
        val context = LocalContext.current


        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
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
    }

    @Composable
    fun TransactionHistoryTab(building: Buildings, sharedViewModel: SharedViewModel) {
        val context = LocalContext.current
        val buildingId = building.buildingId ?: return

        // Collect the lists of invoiced costs separately for capital and operational funds
        val capitalInvoicedCosts by sharedViewModel.getInvoicedCostsByFundType(
            buildingId,
            FundType.CAPITAL
        )
            .collectAsState(initial = emptyList())
        val operationalInvoicedCosts by sharedViewModel.getInvoicedCostsByFundType(
            buildingId,
            FundType.OPERATIONAL
        )
            .collectAsState(initial = emptyList())

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {
            // Capital Costs Section
            Text(
                text = context.getString(R.string.capital_funds),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (capitalInvoicedCosts.isEmpty()) {
                Text(
                    text = context.getString(R.string.no_transactions_recorded),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(capitalInvoicedCosts) { cost ->
                        TransactionCostItem(cost = cost, context = context)
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Operational Costs Section
            Text(
                text = context.getString(R.string.operation_funds),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (operationalInvoicedCosts.isEmpty()) {
                Text(
                    text = context.getString(R.string.no_transactions_recorded),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(operationalInvoicedCosts) { cost ->
                        TransactionCostItem(cost = cost, context = context)
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }

    // A simple composable to display a cost as a transaction item
    @Composable
    fun TransactionCostItem(cost: Costs, context: Context) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = cost.costName,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${formatNumberWithCommas(cost.tempAmount)} تومان",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${context.getString(R.string.due)}: ${cost.dueDate}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${context.getString(R.string.fund_type)}: ${
                        cost.fundType.getDisplayName(context)
                    }",
                    style = MaterialTheme.typography.bodyMedium,
//                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    @Composable
    fun PhonebookTab(
        building: Buildings,
        sharedViewModel: SharedViewModel,
        permissionLevel: PermissionLevel
    ) {
        val residents by sharedViewModel.getResidents(building.buildingId).collectAsState(emptyList())
        Log.d("residents", residents.toString())
        val emergencyNumbers by sharedViewModel.getEmergencyNumbers(building.buildingId).collectAsState(emptyList())
        Log.d("emergencyNumbers", emergencyNumbers.toString())
        var showAddDialog by remember { mutableStateOf(false) }
        val context = LocalContext.current
        Scaffold(
            floatingActionButton = {
                if (permissionLevel >= PermissionLevel.WRITE) {
                    FloatingActionButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Entry")
                    }
                }
            }
        ) { padding ->
            if (residents.isEmpty() && emergencyNumbers.isEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = context.getString(R.string.no_phone_recorded),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(modifier = Modifier.padding(padding)) {
                    item {
                        Text(
                            context.getString(R.string.tenants),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    items(residents) { entry ->
                        PhonebookEntryItem(entry, permissionLevel)
                    }

                    item {
                        Text(
                            context.getString(R.string.emergency_calls),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    items(emergencyNumbers) { entry ->
                        PhonebookEntryItem(entry, permissionLevel)
                    }
                }
            }
        }

        if (showAddDialog) {
            AddPhonebookEntryDialog(
                buildingId = building.buildingId,
                onDismiss = { showAddDialog = false },
                onConfirm = { entry ->
                    sharedViewModel.addPhonebookEntry(entry)
                    showAddDialog = false
                }
            )
        }
    }

    @Composable
    fun PhonebookEntryItem(entry: PhonebookEntry, permissionLevel: PermissionLevel) {
        val context = LocalContext.current
        var showDeleteDialog by remember { mutableStateOf(false) }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${entry.phoneNumber}")
                    }
                    context.startActivity(intent)
                }
        ) {
            Row(
                modifier = Modifier
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
                title = { Text(text = context.getString(R.string.delete_call), style = MaterialTheme.typography.bodyLarge) },
                text = { Text(context.getString(R.string.are_you_sure)) },
                confirmButton = {
                    TextButton( onClick = {
                        sharedViewModel.deletePhonebookEntry(entry)
                        showDeleteDialog = false
                    }) { Text(context.getString(R.string.delete), style = MaterialTheme.typography.bodyLarge) }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                    }) {
                        Text(
                            context.getString(R.string.cancel),
                            style = MaterialTheme.typography.bodyLarge
                        )
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
        var showOwnerDialog by remember { mutableStateOf(false) }
        val context = LocalContext.current
        val owners by sharedViewModel.getOwnersForBuilding(building.buildingId)
            .collectAsState(initial = emptyList())
        val dangValues = remember { mutableStateMapOf<Long, Double>() }
        val ownerUnitsState =
            sharedViewModel.getDangSumsForAllUnits().collectAsState(initial = emptyList())
        val ownerUnits = ownerUnitsState.value
        val ownersUnitsCrossRefs by sharedViewModel.getAllOwnerUnitsCrossRefs()
            .collectAsState(initial = emptyList())

// Convert to map for fast lookup
        val dangSumsMap: Map<Long, Double> = ownerUnits.associate { it.unitId to it.totalDang }

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                items(owners) { owner ->

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
                                    Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            )
                        },
                        activity = context.findActivity(),
                        buildingId = building.buildingId
                    )
                }
            }

            FloatingActionButton(
                onClick = { showOwnerDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
//                containerColor = Color(context.getColor(R.color.secondary_color)),
//                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(Icons.Filled.Add, "Add")
            }

            if (showOwnerDialog) {
                val units by sharedViewModel.getUnitsForBuilding(building.buildingId)
                    .collectAsState(initial = emptyList())
                OwnerDialog(
                    units = units,
                    onDismiss = { showOwnerDialog = false },
                    dangSums = dangSumsMap,
                    onAddOwner = { newOwner, selectedUnits, isManager, selectedBuilding ->
                        Log.d("newOwner", newOwner.toString())
                        Log.d("selectedUnits", selectedUnits.toString())
                        sharedViewModel.saveOwnerWithUnits(
                            newOwner,
                            selectedUnits,
                            isManager,
                            true,
                            building.buildingId
                        )
                        showOwnerDialog = false
                    },
                    sharedViewModel = sharedViewModel,
                    building = building
                )
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
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

    @Composable
    fun CostDetails(building: Buildings, cost: Costs, sharedViewModel: SharedViewModel) {
        val context = LocalContext.current

        var selectedYear by rememberSaveable { mutableIntStateOf(PersianCalendar().persianYear) }
        var selectedMonth by rememberSaveable { mutableIntStateOf(PersianCalendar().persianMonth) }

        val units by sharedViewModel.getUnitsOfBuildingForCost(cost.costId, building.buildingId)
            .collectAsState(initial = emptyList())
        val owners by sharedViewModel.getOwnersOfBuildingForCost(cost.costId, building.buildingId)
            .collectAsState(initial = emptyList())
        Column {
            if (cost.responsible == Responsible.ALL) {
                val debts by sharedViewModel
                    .getDebtsFundMinus(
                        buildingId = building.buildingId,
                        costId = cost.costId,
                        yearStr = selectedYear.toString(),
                        monthStr = selectedMonth.toString().padStart(2, '0')
                    ).collectAsState(initial = emptyList())
                debts.forEach { debt ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "${context.getString(R.string.amount)}: ${
                                formatNumberWithCommas(
                                    debt.amount
                                )
                            }",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (debt.paymentFlag) Color(context.getColor(R.color.Green)) else Color(
                                context.getColor(R.color.Red)
                            )
                        )
                        Text(
                            text = "${context.getString(R.string.due_minus)}: ${debt.dueDate}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (debt.paymentFlag) Color(context.getColor(R.color.Green)) else Color(
                                context.getColor(R.color.Red)
                            )
                        )
                    }
                }
            } else {
                if (units.isEmpty()) {
                    if (owners.isNotEmpty()) {


                        Column {
                            if (owners.isEmpty()) {
                                Text(
                                    text = context.getString(R.string.no_costs_recorded),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                owners.forEach { owner ->
                                    val debts by sharedViewModel
                                        .getDebtsForOwnerCostCurrentAndPreviousUnpaid(
                                            buildingId = building.buildingId,
                                            costId = cost.costId,
                                            ownerId = owner.ownerId,
                                            yearStr = selectedYear.toString(),
                                            monthStr = selectedMonth.toString().padStart(2, '0')
                                        ).collectAsState(initial = emptyList())

                                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                        Text(
                                            text = "${context.getString(R.string.owner)}: ${owner.firstName} ${owner.lastName}",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Log.d("debts", debts.toString())
                                        if (debts.isEmpty()) {
                                            Text(
                                                text = context.getString(R.string.no_costs_recorded),
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        } else {
                                            debts.forEach { debt ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Spacer(Modifier.height(16.dp))
                                                    Text(
                                                        text = "${context.getString(R.string.amount)}: ${
                                                            formatNumberWithCommas(
                                                                debt.amount
                                                            )
                                                        }",
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        color = if (debt.paymentFlag) Color(
                                                            context.getColor(
                                                                R.color.Green
                                                            )
                                                        ) else Color(
                                                            context.getColor(R.color.Red)
                                                        )
                                                    )
                                                    Text(
                                                        text = "${context.getString(R.string.due)}: ${debt.dueDate}",
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        color = if (debt.paymentFlag) Color(
                                                            context.getColor(
                                                                R.color.Green
                                                            )
                                                        ) else Color(
                                                            context.getColor(R.color.Red)
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        thickness = 2.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                            }
                        }


                    } else {
                        Text(
                            text = context.getString(R.string.no_costs_recorded),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    units.forEach { unit ->
                        val debts by sharedViewModel
                            .getDebtsForUnitCostCurrentAndPreviousUnpaid(
                                buildingId = building.buildingId,
                                costId = cost.costId,
                                unitId = unit.unitId,
                                yearStr = selectedYear.toString(),
                                monthStr = selectedMonth.toString().padStart(2, '0')
                            ).collectAsState(initial = emptyList())

                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(
                                text = "${context.getString(R.string.unit)}: ${unit.unitNumber}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Log.d("debts", debts.toString())
                            if (debts.isEmpty()) {
                                Text(
                                    text = context.getString(R.string.no_costs_recorded),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                debts.forEach { debt ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Spacer(Modifier.height(16.dp))
                                        Text(
                                            text = "${context.getString(R.string.amount)}: ${
                                                formatNumberWithCommas(
                                                    debt.amount
                                                )
                                            }",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = if (debt.paymentFlag) Color(
                                                context.getColor(
                                                    R.color.Green
                                                )
                                            ) else Color(
                                                context.getColor(R.color.Red)
                                            )
                                        )
                                        Text(
                                            text = "${context.getString(R.string.due)}: ${debt.dueDate}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = if (debt.paymentFlag) Color(
                                                context.getColor(
                                                    R.color.Green
                                                )
                                            ) else Color(
                                                context.getColor(R.color.Red)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            thickness = 2.dp, color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }

    }
}

    @Composable
    fun UnitDebtItem(
        unit: Units,
        buildingId: Long,
        costId: Long,
        selectedYear: Int,
        selectedMonth: Int,
        sharedViewModel: SharedViewModel,
        context: android.content.Context
    ) {
        val debts by sharedViewModel.getDebtsForUnitCostCurrentAndPreviousUnpaid(
            buildingId = buildingId,
            costId = costId,
            unitId = unit.unitId,
            yearStr = selectedYear.toString(),
            monthStr = selectedMonth.toString().padStart(2, '0')
        ).collectAsState(initial = emptyList())

        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = "${context.getString(R.string.unit)}: ${unit.unitNumber}",
                style = MaterialTheme.typography.bodyLarge
            )

            if (debts.isEmpty()) {
                Text(
                    text = context.getString(R.string.no_costs_recorded),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                debts.forEach { debt ->
                    DebtRow(debt = debt, context = context)
                }
            }
        }
        Divider(
            modifier = Modifier.padding(vertical = 4.dp),
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }

    @Composable
    fun OwnerDebtItem(
        owner: Owners,
        buildingId: Long,
        costId: Long,
        selectedYear: Int,
        selectedMonth: Int,
        sharedViewModel: SharedViewModel,
        context: android.content.Context
    ) {
        val ownersDebts by sharedViewModel.getDebtsForOwnerCostCurrentAndPreviousUnpaid(
            buildingId = buildingId,
            costId = costId,
            ownerId = owner.ownerId,
            yearStr = selectedYear.toString(),
            monthStr = selectedMonth.toString().padStart(2, '0')
        ).collectAsState(initial = emptyList())

        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = "${context.getString(R.string.owner)}: ${owner.firstName} ${owner.lastName}",
                style = MaterialTheme.typography.bodyLarge
            )

            if (ownersDebts.isEmpty()) {
                Text(
                    text = context.getString(R.string.no_costs_recorded),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                ownersDebts.forEach { debt ->
                    DebtRow(debt = debt, context = context)
                }
            }
        }
        Divider(
            modifier = Modifier.padding(vertical = 4.dp),
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }

    @Composable
    fun DebtRow(debt: Debts, context: android.content.Context) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "${context.getString(R.string.amount)}: ${formatNumberWithCommas(debt.amount)}",
                style = MaterialTheme.typography.bodyLarge,
                color = if (debt.paymentFlag) Color.Green else Color.Red
            )
            Text(
                text = "${context.getString(R.string.due)}: ${debt.dueDate}",
                style = MaterialTheme.typography.bodyLarge,
                color = if (debt.paymentFlag) Color.Green else Color.Red
            )
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
//    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf<Period?>(Period.MONTHLY) }
    var dueDate by remember { mutableStateOf("") }
    val context = LocalContext.current

    val defaultEarnings by sharedViewModel.getFixedEarnings()
        .collectAsState(initial = emptyList())

    // Add "Add New" option
    val earningsList = remember(defaultEarnings) {
        defaultEarnings + Earnings(
            earningsId = -1, // Special ID to identify "Add New" option
            earningsName = context.getString(R.string.add_new_earning),
            amount = 0.0,
            period = Period.MONTHLY,
            buildingId = null,
            dueDate = dueDate
        )
    }

    var showEarningForm by remember { mutableStateOf(false) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    fun dismissStartDatePicker() {
        showStartDatePicker = false
    }

    fun dismissEndDatePicker() {
        showEndDatePicker = false
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBoxExample(
                        items = earningsList,
                        selectedItem = sharedViewModel.selectedEarnings,
                        onItemSelected = {
                            if (it.earningsName == context.getString(R.string.addNew)) {
                                // Open dialog to add new building type
                                showEarningForm = true
                            } else {
                                sharedViewModel.selectedEarnings = it
                            }
                        },
                        label = context.getString(R.string.earning_title),
                        modifier = Modifier
                            .fillMaxWidth(1f),
                        itemLabel = { it.earningsName }
                    )

//                OutlinedTextField(
//                    value = title,
//                    onValueChange = { title = it },
//                    label = { Text(context.getString(R.string.earning_title), style = MaterialTheme.typography.bodyLarge) },
//                    modifier = Modifier.fillMaxWidth()
//                )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = {
                            Text(
                                context.getString(R.string.amount),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Period Dropdown using your existing component
                    ExposedDropdownMenuBoxExample(
                        items = Period.entries,
                        selectedItem = selectedPeriod,
                        onItemSelected = { selectedPeriod = it },
                        label = context.getString(R.string.period),
                        modifier = Modifier.fillMaxWidth(),
                        itemLabel = { it.getDisplayName(context) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Start Date Picker
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = {},
                        label = { Text(context.getString(R.string.start_date)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showStartDatePicker = true },
                        readOnly = true
                    )

                }
            }
//            if (showStartDatePicker) {
//                PersianDatePickerDialogContent(
//                    onDateSelected = { selected ->
//                        startDate = selected
//                        dismissStartDatePicker()
//                    },
//                    onDismiss = { dismissStartDatePicker() },
//                    context = context
//                )
//            }
//            if (showEndDatePicker) {
//                PersianDatePickerDialogContent(
//                    onDateSelected = { selected ->
//                        endDate = selected
//                        dismissEndDatePicker()
//                    },
//                    onDismiss = { dismissEndDatePicker() },
//                    context = context
//                )
//            }

        },
        confirmButton = {
            Button(
                onClick = {
                    val newEarning = Earnings(
                        buildingId = building.buildingId,
                        earningsName = sharedViewModel.selectedEarnings?.earningsName ?: "",
                        amount = amount.toString().persianToEnglishDigits().toDoubleOrNull()
                            ?: 0.0,
                        period = selectedPeriod ?: Period.MONTHLY,
                        dueDate = dueDate
                    )
                    onConfirm(newEarning)
                }
//                        enabled = sharedViewModel.selectedEarnings?.earningsName ?: "".isNotBlank() && amount.isNotBlank() && startDate.isNotBlank() && endDate.isNotBlank() && selectedPeriod != null
            ) {
                Text(
                    text = context.getString(R.string.insert),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

        },
        dismissButton = {

        })
}


fun formatNumberWithCommas(number: Double): String {
    return NumberFormat.getNumberInstance(Locale.US).format(number)
}


@Composable
fun AddCostDialog(
    buildingId: Long,
    sharedViewModel: SharedViewModel,
    onDismiss: () -> Unit,
    initialFundType: FundType = FundType.CAPITAL,
    onSave: (
        Costs,
        String,
        Period,
        FundType,
        CalculateMethod,
        CalculateMethod,
        Responsible,
        List<Long>,
        List<Long>,
        String
    ) -> Unit
) {
    val context = LocalContext.current

    // Active units in building (needed for tenant selection)
    val activeUnits by sharedViewModel.getActiveUnits(buildingId)
        .collectAsState(initial = emptyList())

    // Costs eligible for direct charge to fund (chargeFlag == true)
    val defaultChargedCosts by sharedViewModel.getChargesCostsWithNullBuildingId()
        .collectAsState(initial = emptyList())

    // Costs eligible for direct charge to fund (chargeFlag == true)
    val chargeableCosts by sharedViewModel.getCostsForBuildingWithChargeFlag(buildingId)
        .collectAsState(initial = emptyList())

    // Owners list (needed for owner selection)
    val owners by sharedViewModel.getOwnersForBuilding(buildingId)
        .collectAsState(initial = emptyList())

    // State holders
    var selectedCost by remember { mutableStateOf<Costs?>(null) }
    var totalAmount by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf<Period?>(Period.MONTHLY) }
    var showAddNewCostNameDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    var selectedFundType by remember { mutableStateOf(initialFundType) }
    var selectedResponsible by remember {
        mutableStateOf(Responsible.OWNER.getDisplayName(context))
    }
    var selectedCalculateMethod by remember { mutableStateOf(context.getString(R.string.fixed)) }
    var selectedUnitCalculateMethod by remember { mutableStateOf(context.getString(R.string.fixed)) }

    val selectedUnits = remember { mutableStateListOf<Units>() }
    val selectedOwners = remember { mutableStateListOf<Owners>() }
    var dueDate by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isChargeableCost by remember { mutableStateOf(false) }

    // Map display strings to enums
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
            fundType = selectedFundType,
            dueDate = ""
        )
    }

    // Prepare full costs list with "Add new cost" option
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
            fundType = initialFundType,
            dueDate = ""
        )
    )

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
                    // Fund type selection chips (Capital or Operational)
                    ChipGroupShared(
                        selectedItems = listOf(selectedFundType.getDisplayName(context)),
                        onSelectionChange = { newSelection ->
                            if (newSelection.isNotEmpty()) {
                                selectedFundType = FundType.fromDisplayName(context, newSelection.first()) ?: FundType.CAPITAL
                                selectedCost = null
                                totalAmount = ""
                                isChargeableCost = false // reset chargeable on fund change
                            }
                        },
                        items = listOf(FundType.CAPITAL.getDisplayName(context), FundType.OPERATIONAL.getDisplayName(context)),
                        label = context.getString(R.string.fund_type),
                        singleSelection = true,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Cost dropdown showing only costs matching selected fund type
                    if (selectedFundType == FundType.OPERATIONAL) {
                        ExposedDropdownMenuBoxExample(
                            items = costsWithAddNew.filter { it.fundType == selectedFundType },
                            selectedItem = selectedCost,
                            onItemSelected = {
                                if (it.costId == -1L) {
                                    // Add new cost
                                    showAddNewCostNameDialog = true
                                } else {
                                    selectedCost = it
                                    totalAmount = it.tempAmount.toLong().toString()
                                    selectedPeriod = it.period

                                    // Determine if selected cost is chargeable (i.e. reduces fund directly)
                                    val chargeable =
                                        chargeableCosts.any { cost -> cost.costId == it.costId }
                                    isChargeableCost = chargeable
                                }
                            },
                            label = context.getString(R.string.cost_name),
                            modifier = Modifier.fillMaxWidth(),
                            itemLabel = { it.costName }
                        )
                    } else {
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

                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Amount input, enabled if cost is NOT chargeable
                    OutlinedTextField(
                        value = totalAmount,
                        onValueChange = {
                            if (!isChargeableCost) {  // allow input only if cost is NOT chargeable
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

                    // Show amount in words
                    val amountVal = totalAmount.toLongOrNull() ?: 0L
                    val amountInWords = NumberCommaTransformation().numberToWords(context, amountVal)
                    Text(
                        text = "$amountInWords ${context.getString(R.string.toman)}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Due date input
                    OutlinedTextField(
                        value = dueDate ?: "",
                        onValueChange = { dueDate = it },
                        label = { Text(context.getString(R.string.due)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) showDatePicker = true
                            },
                        readOnly = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Conditional chips depending on fund type and responsible

                    if (selectedFundType == FundType.CAPITAL) {
                        // Show Owners chip group (multi-selection)
                        ChipGroupOwners(
                            selectedOwners = selectedOwners,
                            onSelectionChange = { newSelection ->
                                selectedOwners.clear()
                                selectedOwners.addAll(newSelection)
                                sharedViewModel.selectedOwners.clear()
                                sharedViewModel.selectedOwners.addAll(newSelection)
                            },
                            owners = owners,
                            label = context.getString(R.string.owners)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    } else if (selectedFundType == FundType.OPERATIONAL && !isChargeableCost) {
                        // Show Responsible chip group (owner or tenant) only if NOT chargeable (cost creation)
                        ChipGroupShared(
                            selectedItems = listOf(selectedResponsible),
                            onSelectionChange = { newSelection ->
                                if (newSelection.isNotEmpty()) {
                                    selectedResponsible = newSelection.first()
                                }
                            },
                            items = listOf(
                                context.getString(R.string.owners),
                                context.getString(R.string.tenants)
                            ),
                            modifier = Modifier.padding(vertical = 8.dp),
                            label = context.getString(R.string.responsible),
                            singleSelection = true
                        )
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
                                label = context.getString(R.string.units)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

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
                        }

                        if (responsibleEnum == Responsible.OWNER) {
                            ChipGroupOwners(
                                selectedOwners = selectedOwners,
                                onSelectionChange = { newSelection ->
                                    selectedOwners.clear()
                                    selectedOwners.addAll(newSelection)
                                    sharedViewModel.selectedOwners.clear()
                                    sharedViewModel.selectedOwners.addAll(newSelection)
                                },
                                owners = owners,
                                label = context.getString(R.string.owners)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            ChipGroupShared(
                                selectedItems = listOf(selectedCalculateMethod),
                                onSelectionChange = { newSelection ->
                                    if (newSelection.isNotEmpty()) {
                                        selectedCalculateMethod = newSelection.first()
                                    }
                                },
                                items = listOf(
                                    context.getString(R.string.area),
                                    context.getString(R.string.dang),
                                    context.getString(R.string.fixed)
                                ),
                                modifier = Modifier.padding(vertical = 8.dp),
                                label = context.getString(R.string.acount_base),
                                singleSelection = true
                            )
                        }
                    }

                }
            }

            // Add new cost dialog (nested)
            if (showAddNewCostNameDialog) {
                AddNewCostNameDialog(
                    buildingId = buildingId,
                    onDismiss = { showAddNewCostNameDialog = false },
                    onSave = { newCostName ->
                        coroutineScope.launch {
                            val cost = Costs(
                                buildingId = buildingId,
                                costName = newCostName,
                                tempAmount = 0.0,
                                period = Period.NONE,
                                calculateMethod = CalculateMethod.EQUAL,
                                paymentLevel = PaymentLevel.BUILDING,
                                responsible = Responsible.OWNER,
                                fundType = selectedFundType,
                                dueDate = dueDate ?: ""
                            )
                            sharedViewModel.insertNewCost(cost)
                            showAddNewCostNameDialog = false
                            selectedCost = cost
                            totalAmount = cost.tempAmount.toLong().toString()
                            selectedPeriod = cost.period
                            isChargeableCost = false
                        }
                    },
                    checkCostNameExists = { bId, cName ->
                        sharedViewModel.checkCostNameExists(bId, cName).first()
                    }
                )
            }

            // Date picker dialog
            if (showDatePicker) {
                PersianDatePickerDialogContent(
                    onDateSelected = { selected ->
                        dueDate = selected
                        showDatePicker = false
                    },
                    onDismiss = { showDatePicker = false },
                    context = context
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val cost = selectedCost ?: return@Button

                // If operational and chargeable → decrease fund, no cost or debt insertion
                if (selectedFundType == FundType.OPERATIONAL && isChargeableCost) {
                    coroutineScope.launch {
                        val amountDouble = totalAmount.toDoubleOrNull() ?: 0.0
                        val success = sharedViewModel.decreaseOperationalFund(buildingId, amountDouble, FundType.OPERATIONAL)
                        if (success) {
                            Toast.makeText(context, context.getString(R.string.fund_decreased_successfully), Toast.LENGTH_SHORT).show()
                            onDismiss()
                        } else {
                            Toast.makeText(context, context.getString(R.string.insufficient_fund), Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    // For new cost or non-chargeable cost path
                    onSave(
                        cost,
                        totalAmount,
                        selectedPeriod ?: Period.NONE,
                        selectedFundType,
                        calculateMethod,
                        calculateUnitMethod,
                        responsibleEnum,
                        selectedUnits.mapNotNull { it.unitId },
                        selectedOwners.mapNotNull { it.ownerId },
                        dueDate ?: ""
                    )
                }
            }) {
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


fun calculateBuildingFundFlow(
    sharedViewModel: SharedViewModel,
    buildingId: Long
): Flow<Double> {
    val sumPositiveFlow = sharedViewModel.sumPaidFundFlagPositive(buildingId)
        .map { it }
    val sumNegativeFlow = sharedViewModel.sumUnpaidFundFlagNegative(buildingId)
        .map { it }
    val sumEarning = sharedViewModel.sumPaidEarnings(buildingId)
        .map { it }
    val unitCountFlow = sharedViewModel.countUnits(buildingId)
        .map { it.toDouble() }

    val sumFundMinus = sharedViewModel.sumFundMinus(buildingId)
        .map { it.toDouble() }

    return combine(
        sumPositiveFlow,
        sumNegativeFlow,
        unitCountFlow,
        sumEarning,
        sumFundMinus
    ) { sumPositive, sumNegative, unitCount, earning, fundMinus ->
        (sumPositive + earning) - (sumNegative + fundMinus)
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

