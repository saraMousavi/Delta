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
import androidx.compose.foundation.Image
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import coil.compose.rememberAsyncImagePainter
import com.example.delta.data.entity.BuildingTabItem
import com.example.delta.data.entity.BuildingTabType
import com.example.delta.data.entity.BuildingTypes
import com.example.delta.data.entity.BuildingUsages
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.CityComplex
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Earnings
import com.example.delta.data.entity.Owners
import com.example.delta.data.entity.PhonebookEntry
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
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


        val chargesCost by sharedViewModel.getRawChargesCostsWithBuildingId(buildingId = building.buildingId)
            .collectAsState(initial = emptyList())
        Log.d("chargesCost", chargesCost.toString())
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
//                        val insertedComplex = cityComplexes.find { it.complexId == id }
//                        if (insertedComplex != null) {
//                            selectedCityComplex = insertedComplex
//                        }
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

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun FundsTab(
        building: Buildings,
        sharedViewModel: SharedViewModel,
        onOpenCostDetail: (costId: Long, fundType: FundType) -> Unit
    ) {
        val context = LocalContext.current

        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()
        val operationalFund by sharedViewModel.getOperationalOrCapitalFundBalance(
            buildingId = building.buildingId,
            fundType = FundType.OPERATIONAL
        ).collectAsState(initial = 0.0)

        val capitalFund by sharedViewModel.getOperationalOrCapitalFundBalance(
            buildingId = building.buildingId,
            fundType = FundType.CAPITAL
        ).collectAsState(initial = 0.0)

        val operationalCosts by sharedViewModel.getPendingCostsByFundType(
            building.buildingId,
            FundType.OPERATIONAL
        ).collectAsState(initial = emptyList())

        val capitalCosts by sharedViewModel.getPendingCostsByFundType(
            building.buildingId,
            FundType.CAPITAL
        ).collectAsState(initial = emptyList())

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
                                            onOpenCostDetail(cost.costId, FundType.CAPITAL)
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
                                            onOpenCostDetail(cost.costId, FundType.OPERATIONAL)
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

                SnackbarHost(hostState = snackbarHostState)
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
                AddCapitalCostDialog(
                buildingId = building.buildingId,
                sharedViewModel = sharedViewModel,
                onDismiss = { buildingViewModel.showCapitalCostDialog.value = false },
                onSave = { selectedCost, amount, period, calculateMethod, calculatedUnitMethod, responsible, selectedUnits, selectedOwners, dueDate, message ->
                    coroutineScope.launch {
                        sharedViewModel.insertDebtPerNewCost(
                            buildingId = building.buildingId,
                            amount = amount,
                            name = selectedCost.costName,
                            period = period,
                            fundType = FundType.CAPITAL,
                            paymentLevel = selectedCost.paymentLevel,
                            calculateMethod = calculateMethod,
                            calculatedUnitMethod = calculatedUnitMethod,
                            responsible = responsible,
                            dueDate = dueDate,
                            selectedUnitIds = selectedUnits.toList(),
                            selectedOwnerIds = selectedOwners.toList()
                        )
                        buildingViewModel.showCapitalCostDialog.value = false
                        snackbarHostState.showSnackbar(message)
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
                    snackbarHostState.showSnackbar(message)
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
                            sharedViewModel.insertEarningsWithCredits(earningsToInsert)
                            snackbarHostState.showSnackbar(context.getString(R.string.earning_inserted_successfully))
                        } catch (e: IllegalStateException) {
                            Log.e("error", e.message.toString())
                            snackbarHostState.showSnackbar(context.getString(R.string.earnings_conflict_error))
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
        val buildingId = building.buildingId

        // Collect the lists of invoiced costs separately for capital and operational funds
        val capitalInvoicedCosts by sharedViewModel.getInvoicedCostsByFundType(
            buildingId,
            FundType.CAPITAL
        ).collectAsState(initial = emptyList())

        val operationalInvoicedCosts by sharedViewModel.getInvoicedCostsByFundType(
            buildingId,
            FundType.OPERATIONAL
        ).collectAsState(initial = emptyList())

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

        // Two tabs: Emergency and Residents(Tenants)
        var selectedTab by remember { mutableStateOf(0) }
        val tabTitles = listOf(
            context.getString(R.string.emergency_calls).uppercase(),
            context.getString(R.string.tenants).uppercase()
        )

        // Load residents and emergency numbers separately
        val residents by sharedViewModel.getResidents(buildingId).collectAsState(emptyList())
        val emergencyNumbers by sharedViewModel.getEmergencyNumbers(buildingId).collectAsState(emptyList())
        var showAddDialog by remember { mutableStateOf(false) }

        val coroutineScope = rememberCoroutineScope()

        // SnackbarHostState to show messages
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
                        sharedViewModel.addPhonebookEntry(entry)
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
                         sharedViewModel.deletePhonebookEntry(entry)
                        showDeleteDialog = false
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
        var showOwnerDialog by remember { mutableStateOf(false) }
        val context = LocalContext.current
        val owners by sharedViewModel.getOwnersForBuilding(building.buildingId)
            .collectAsState(initial = emptyList())
        val ownerUnitsState =
            sharedViewModel.getDangSumsForAllUnits().collectAsState(initial = emptyList())
        val ownerUnits = ownerUnitsState.value
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
        dueDate,
        selectedOwners.toList()
    ) {
        selectedCost != null &&
                !selectedCost!!.costName.isBlank() &&
                totalAmount.isNotBlank() &&
                totalAmount.toDoubleOrNull()?.let { it > 0.0 } == true &&
                dueDate.isNotBlank() &&
                selectedOwners.isNotEmpty()
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
                    sharedViewModel.insertNewCost(newCost)
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


    // Load necessary data (active units, owners, costs) same as before but filtered for capital if you want
    val activeUnits by sharedViewModel.getUnitsForBuilding(buildingId)
        .collectAsState(initial = emptyList())
    val owners by sharedViewModel.getOwnersForBuilding(buildingId)
        .collectAsState(initial = emptyList())
    val defaultChargedCosts by sharedViewModel.getChargesCostsWithNullBuildingId()
        .collectAsState(emptyList())
    var dueDate by remember { mutableStateOf("") }
    var success by remember { mutableStateOf(false) }
    val chargeableCosts by sharedViewModel.getCostsForBuildingWithChargeFlagAndFiscalYear(
        buildingId,
        dueDate.split("/")[0]
    )
        .collectAsState(emptyList())
    Log.d("chargeableCosts", chargeableCosts.toString())
    val operationalFund by sharedViewModel.getOperationalOrCapitalFundBalance(
        buildingId = buildingId,
        fundType = FundType.OPERATIONAL
    ).collectAsState(initial = 0)
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
    var selectedResponsible by remember { mutableStateOf(Responsible.OWNER.getDisplayName(context)) }
    var selectedCalculateMethod by remember { mutableStateOf(context.getString(R.string.fixed)) }
    var selectedUnitCalculateMethod by remember { mutableStateOf(context.getString(R.string.fixed)) }
    val selectedUnits = remember { mutableStateListOf<Units>() }
    val selectedOwners = remember { mutableStateListOf<Owners>() }
    var showDatePicker by remember { mutableStateOf(false) }
    var showAddNewCostNameDialog by remember { mutableStateOf(false) }
    var isChargeableCost by remember { mutableStateOf(false) }


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
        selectedResponsible,
        selectedCalculateMethod,
        selectedUnitCalculateMethod,
        dueDate,
        selectedUnits.toList(),  // important to copy to detect changes inside list
        selectedOwners.toList()
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
                isResponsibleValid &&
                isCalculateMethodValid &&
                isUnitCalculateMethodValid &&
                isDueDateValid &&
                (
                        // If owner responsible, owners must be selected
                        (responsibleIsOwner && isOwnersSelected)
                                // If tenant responsible, units must be selected
                                || (responsibleIsTenant && isUnitsSelected)
                        )
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
                    } else {
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
        },
        confirmButton = {
            Button(
                enabled = isValid,
                onClick = {
                    val cost = selectedCost ?: return@Button
                    if (selectedResponsible == Responsible.TENANT.getDisplayName(context) && isChargeableCost) {
                        coroutineScope.launch {
                            val amountDouble = totalAmount.toDoubleOrNull() ?: 0.0
                            if (amountDouble <= (operationalFund?.toDouble() ?: 0.0)) {
                                sharedViewModel.insertNewCost(
                                    Costs(
                                        buildingId = buildingId,
                                        costName = cost.costName,
                                        chargeFlag = true,
                                        fundType = FundType.OPERATIONAL,
                                        responsible = Responsible.TENANT,
                                        paymentLevel = PaymentLevel.UNIT,
                                        calculateMethod = CalculateMethod.EQUAL,
                                        period = Period.YEARLY,
                                        tempAmount = amountDouble,
                                        dueDate = dueDate,
                                        invoiceFlag = true
                                    )
                                )
                                success = sharedViewModel.decreaseOperationalFund(
                                    buildingId,
                                    amountDouble,
                                    FundType.OPERATIONAL
                                )

                                onSave(context.getString(R.string.fund_decreased_successfully))
                            } else {
                                success = false
                                onSave(context.getString(R.string.insufficient_fund))
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

