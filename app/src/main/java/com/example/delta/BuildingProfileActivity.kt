package com.example.delta

import android.annotation.SuppressLint
import android.content.Intent
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Debts
import com.example.delta.data.entity.Earnings
import com.example.delta.data.entity.Owners
import com.example.delta.data.entity.TabItem
import com.example.delta.data.entity.TabType
import com.example.delta.data.entity.Units
import com.example.delta.data.model.AppDatabase
import com.example.delta.enums.BuildingProfileFields
import com.example.delta.enums.CalculateMethod
import com.example.delta.enums.FundFlag
import com.example.delta.enums.PaymentLevel
import com.example.delta.enums.Period
import com.example.delta.enums.PermissionLevel
import com.example.delta.enums.Responsible
import com.example.delta.factory.BuildingsViewModelFactory
import com.example.delta.factory.EarningsViewModelFactory
import com.example.delta.init.AuthUtils
import com.example.delta.init.NumberCommaTransformation
import com.example.delta.init.Preference
import com.example.delta.viewmodel.BuildingsViewModel
import com.example.delta.viewmodel.EarningsViewModel
import com.example.delta.viewmodel.SharedViewModel
import ir.hamsaa.persiandatepicker.util.PersianCalendar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import java.text.NumberFormat
import java.util.Locale


class BuildingProfileActivity : ComponentActivity() {
    private val earningsViewModel: EarningsViewModel by viewModels {
        EarningsViewModelFactory(application = this.application)
    }

    private val buildingViewModel: BuildingsViewModel by viewModels {
        BuildingsViewModelFactory(application = this.application)
    }

    val sharedViewModel: SharedViewModel by viewModels()
    var buildingTypeName: String = ""
    var buildingUsageName: String = ""

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        val building = intent.getParcelableExtra<Parcelable>("BUILDING_DATA") as? Buildings

        buildingTypeName = intent.getStringExtra("BUILDING_TYPE_NAME") ?: "Unknown"
        buildingUsageName = intent.getStringExtra("BUILDING_USAGE_NAME") ?: "Unknown"
        Log.d("BuildingRetrieve", "Province: ${buildingUsageName}, State: $buildingTypeName")
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

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun BuildingProfileScreen(building: Buildings) {
        val context = LocalContext.current
        val userId = Preference().getUserId(context = context)
        Log.d("userId", userId.toString())
        val permissionLevelFundTab = AuthUtils.checkFieldPermission(
            userId, BuildingProfileFields.FUNDS_TAB.fieldNameRes, sharedViewModel
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

        val currentRole = sharedViewModel.getRoleByUserId(userId).collectAsState(initial = null)
        val currentRoleId = currentRole.value?.roleId ?: 1L



        val tabs = listOfNotNull(
            TabItem(context.getString(R.string.overview), TabType.OVERVIEW),
            if (permissionLevelOwnerTab == PermissionLevel.FULL || permissionLevelOwnerTab == PermissionLevel.WRITE
                || permissionLevelOwnerTab == PermissionLevel.READ
            ) {
                TabItem(context.getString(R.string.owners), TabType.OWNERS)
            } else null,
            if (permissionLevelUnitsTab == PermissionLevel.FULL || permissionLevelUnitsTab == PermissionLevel.WRITE
                || permissionLevelUnitsTab == PermissionLevel.READ
            ) {
                TabItem(context.getString(R.string.units), TabType.UNITS)
            } else null,
            if (permissionLevelTenantsTab == PermissionLevel.FULL || permissionLevelTenantsTab == PermissionLevel.WRITE
                || permissionLevelTenantsTab == PermissionLevel.READ
            ) {
                TabItem(context.getString(R.string.tenants), TabType.TENANTS)
            } else null,
            if (permissionLevelFundTab == PermissionLevel.FULL || permissionLevelFundTab == PermissionLevel.WRITE
                || permissionLevelFundTab == PermissionLevel.READ
            ) {
                TabItem(context.getString(R.string.funds), TabType.FUNDS)
            }
            else null,
            if (permissionLevelTenantsTab == PermissionLevel.FULL || permissionLevelTenantsTab == PermissionLevel.WRITE
                || permissionLevelTenantsTab == PermissionLevel.READ
            ) {
                TabItem(context.getString(R.string.reports), TabType.REPORTS)
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
                ScrollableTabRow(selectedTabIndex = selectedTab, modifier = Modifier.fillMaxWidth()) {
                    tabs.forEachIndexed { index, tabItem ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(text = tabItem.title, style = MaterialTheme.typography.bodyLarge) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (tabs.getOrNull(selectedTab)?.type) {
                    TabType.OVERVIEW -> OverviewTab(sharedViewModel, building, currentRoleId)
                    TabType.OWNERS -> OwnersTab(building, sharedViewModel)
                    TabType.UNITS -> UnitsTab(building, sharedViewModel)
                    TabType.TENANTS -> TenantsTab(building, sharedViewModel)
                    TabType.FUNDS -> FundsTab(building)
                    TabType.REPORTS -> ReportsTab()
                    null -> {} // Handle invalid index if needed
                }

            }
        }
    }


    @Composable
    fun OverviewTab(
        sharedViewModel: SharedViewModel,
        building: Buildings,
        roleId: Long
    ) {
        val context = LocalContext.current
        val fileList by sharedViewModel.getBuildingFiles(building.buildingId)
            .collectAsState(initial = emptyList())
        var selectedImagePath by remember { mutableStateOf<String?>(null) }
        val userId = Preference().getUserId(context = context)
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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(context.getColor(R.color.primary_color)) // Example: Light blue background
                    )
                ) {
                    Column(modifier = Modifier.padding(4.dp)) {

                        if (permissionLevelBuildingName == PermissionLevel.FULL || permissionLevelBuildingName == PermissionLevel.WRITE) {
                            Text(
                                text = "${context.getString(R.string.building_name)}: ${building.name}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${context.getString(R.string.street)}: ${building.street}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${context.getString(R.string.post_code)}: ${building.postCode}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(context.getColor(R.color.primary_color)) // Example: Light blue background
                    )
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "${context.getString(R.string.province)}: ${building.province}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${context.getString(R.string.state)}: ${building.state}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(context.getColor(R.color.primary_color)) // Example: Light blue background
                    )
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "${context.getString(R.string.building_type)}: $buildingTypeName",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${context.getString(R.string.building_usage)}: $buildingUsageName",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(context.getColor(R.color.primary_color)) // Example: Light blue background
                    )
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = context.getString(R.string.shared_things),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        building.utilities.forEach { utility ->
                            Text(
                                text = utility,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
            if (fileList.isNotEmpty() && ( permissionLevelDoc == PermissionLevel.FULL || permissionLevelDoc == PermissionLevel.WRITE) ) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(context.getColor(R.color.primary_color))
                        )
                    ) {
                        Row(modifier = Modifier.padding(8.dp)) {
                            fileList.forEach { file ->
                                val fileObj = File(file.fileUrl)
                                val extension = fileObj.extension.lowercase()
                                val painter = when (extension) {
                                    "jpg", "jpeg", "png", "gif", "bmp", "webp" -> rememberAsyncImagePainter(
                                        fileObj
                                    )

                                    else -> null
                                }

                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            // Open file externally on click
                                            openFile(context, file.fileUrl)
                                        },
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
                                        // Show icon for non-image files
                                        val icon = when (extension) {
                                            "pdf" -> Icons.Default.PictureAsPdf
                                            "xls", "xlsx" -> Icons.Default.TableChart // or your custom Excel icon
                                            "doc", "docx" -> Icons.Default.Description // or your custom Word icon
                                            else -> Icons.Default.InsertDriveFile
                                        }
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = "File icon",
                                            tint = Color.White,
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

    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun FundsTab(building: Buildings) {
        val context = LocalContext.current
        val buildingWithEarnings =
            earningsViewModel.fetchAndProcessEarnings(building.buildingId)
                .collectAsState(initial = null)

        val fundFlow = remember(building.buildingId) {
            calculateBuildingFundFlow(
                sharedViewModel,
                building.buildingId
            )
        }
        val fund by fundFlow.collectAsState(initial = 0.0)
        val formatedFund = formatNumberWithCommas(fund)


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Balance Card
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                FundInfoBox(formatedFund, context)
            }

            // Tab Row
            var selectedTab by remember { mutableIntStateOf(0) }
            val tabTitles =
                listOf(context.getString(R.string.incomes), context.getString(R.string.costs))

            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {
                    HorizontalDivider(
                        thickness = 2.dp
                    )
                },
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
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
                                if (selectedTab == index) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
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


            // Content Area
            when (selectedTab) {
                0 -> EarningsSection(
                    earnings = buildingWithEarnings.value ?: emptyList(),
                    onAddEarnings = { buildingViewModel.showEarningsDialog(building.buildingId) }
                )

                1 -> CostSection(
                    building = building,
                    sharedViewModel = sharedViewModel
                )
            }
        }

        // Dialog handling
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
        if (buildingViewModel.showCostDialog.value) {
            AddCostDialog(
                buildingId = building.buildingId,
                sharedViewModel = sharedViewModel,
                onDismiss = { buildingViewModel.hideDialogs() },
                onSave = { selectedCost, amount, period, fundFlag, calculateMethod, calculatedUnitMethod, responsible, selectedUnits, selectedOwners, dueDate, fundMinus ->
                    // Insert cost and debts using selectedCost info

                    sharedViewModel.insertDebtPerNewCost(
                        buildingId = building.buildingId,
                        amount = amount,
                        name = selectedCost.costName,
                        period = period,
                        fundFlag = fundFlag,
                        paymentLevel = selectedCost.paymentLevel,
                        calculateMethod = calculateMethod,
                        calculatedUnitMethod = calculatedUnitMethod,
                        responsible = responsible,
                        dueDate = dueDate,
                        selectedUnitIds = selectedUnits.map { it },
                        selectedOwnerIds = selectedOwners.map { it },
                        fundMinus = fundMinus
                    )

                    buildingViewModel.hideDialogs()
                }
            )

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
        val context = LocalContext.current
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
    fun ReportsTab() {
        Text(text = "Reports Tab Content")
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

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun CostSection(building: Buildings, sharedViewModel: SharedViewModel) {
        val context = LocalContext.current
        var showAddCostDialog by remember { mutableStateOf(false) }

        // Observe changes to costs using a Flow
        val costs by sharedViewModel.getCostsForBuilding(building.buildingId)
            .collectAsState(initial = emptyList())

        val allResponsibleDebts: List<Costs> = costs.filter { it.responsible == Responsible.ALL }


        Log.d("costs", costs.toString())

        val allCost by sharedViewModel.getAllCosts()
            .collectAsState(initial = emptyList())

        Log.d("allCost", allCost.toString())


        // Function to handle adding a new cost
        val onAddCost = {
            showAddCostDialog = true
        }

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 80.dp),
                state = rememberLazyListState()
            ) {
                items(costs) { cost -> // Directly use cost
                    val isExpanded = remember { mutableStateOf(false) } // Remember expansion state

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isExpanded.value = !isExpanded.value
                            }
                            .padding(16.dp)
                    ) {
                        // Basic Cost Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${context.getString(R.string.title)}: ${cost.costName}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Expanded Details
                        AnimatedVisibility(
                            visible = isExpanded.value,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            CostDetails(
                                building = building,
                                cost = cost,
                                sharedViewModel = sharedViewModel
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }

            ExtendedFloatingActionButton(
                onClick = onAddCost,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                icon = { Icon(Icons.Default.Add, "Add Costs") },
                text = {
                    Text(
                        text = context.getString(R.string.add_costs),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
//                containerColor = Color(context.getColor(R.color.secondary_color)),
//                contentColor = Color(context.getColor(R.color.white))
            )

            if (showAddCostDialog) {
                AddCostDialog(
                    buildingId = building.buildingId,
                    sharedViewModel = sharedViewModel,
                    onDismiss = { showAddCostDialog = false },
                    onSave = { selectedCost, amount, period, fundFlag, calculateMethod, calculatedUnitMethod, responsible, selectedUnits, selectedOwners, dueDate, fundMinus ->
                        // Insert cost and debts using selectedCost info

                        sharedViewModel.insertDebtPerNewCost(
                            buildingId = building.buildingId,
                            amount = amount,
                            name = selectedCost.costName,
                            period = period,
                            dueDate = dueDate,
                            fundFlag = fundFlag,
                            calculateMethod = calculateMethod,
                            calculatedUnitMethod = calculatedUnitMethod,
                            paymentLevel = selectedCost.paymentLevel,
                            responsible = responsible,
                            selectedUnitIds = selectedUnits.map { it },
                            selectedOwnerIds = selectedOwners.map { it },
                            fundMinus = fundMinus
                        )

                        showAddCostDialog = false
                    }
                )
            }
        }
    }


    @Composable
    fun CostDetails(building: Buildings, cost: Costs, sharedViewModel: SharedViewModel) {
        val context = LocalContext.current

        var selectedYear by rememberSaveable { mutableIntStateOf(PersianCalendar().persianYear) }
        var selectedMonth by rememberSaveable { mutableIntStateOf(PersianCalendar().persianMonth) }

        val units by sharedViewModel.getUnitsOfBuildingForCost(cost.id, building.buildingId)
            .collectAsState(initial = emptyList())
        val owners by sharedViewModel.getOwnersOfBuildingForCost(cost.id, building.buildingId)
            .collectAsState(initial = emptyList())
        Column {
            if (cost.responsible == Responsible.ALL) {
                val debts by sharedViewModel
                    .getDebtsFundMinus(
                        buildingId = building.buildingId,
                        costId = cost.id,
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
                                            costId = cost.id,
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
                                costId = cost.id,
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
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    val context = LocalContext.current

    val defaultEarnings by sharedViewModel.getFixedEarnings()
        .collectAsState(initial = emptyList())

    // Add "Add New" option
    val earningsList = remember(defaultEarnings) {
        defaultEarnings + Earnings(
            id = -1, // Special ID to identify "Add New" option
            earningsName = context.getString(R.string.add_new_earning),
            amount = 0.0,
            period = Period.MONTHLY,
            startDate = "",
            endDate = "",
            buildingId = null
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
                        value = startDate,
                        onValueChange = {},
                        label = { Text(context.getString(R.string.start_date)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showStartDatePicker = true },
                        readOnly = true
                    )



                    Spacer(modifier = Modifier.height(8.dp))

                    // End Date Picker
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = {},
                        label = { Text(context.getString(R.string.end_date)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showEndDatePicker = true },
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
                        startDate = startDate,
                        endDate = endDate
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
    onSave: (Costs, String, Period, FundFlag, CalculateMethod, CalculateMethod, Responsible, List<Long>, List<Long>, String, FundFlag) -> Unit
) {
    val context = LocalContext.current

    val costs by sharedViewModel.getCostsForBuilding(buildingId)
        .collectAsState(initial = emptyList())
    val activeUnits by sharedViewModel.getActiveUnits(buildingId)
        .collectAsState(initial = emptyList())
    val owners by sharedViewModel.getOwnersForBuilding(buildingId)
        .collectAsState(initial = emptyList())
    var selectedCost by remember { mutableStateOf<Costs?>(null) }
    var totalAmount by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf<Period?>(Period.MONTHLY) }
//    var fundFlagChecked by remember { mutableStateOf(false) }
    var showAddNewCostNameDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var selectedResponsible by remember {
        mutableStateOf(Responsible.OWNER.getDisplayName(context)) // Default to "Owners"
    }
    var selectedCalculateMethod by remember { mutableStateOf(context.getString(R.string.fixed)) }
    var selectedUnitCalculateMethod by remember { mutableStateOf(context.getString(R.string.fixed)) }


    val selectedUnits = remember { mutableStateListOf<Units>() }
    val selectedOwners = remember { mutableStateListOf<Owners>() }
    var dueDate by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val responsibleEnum = when (selectedResponsible) {
        Responsible.OWNER.getDisplayName(context) -> Responsible.OWNER
        Responsible.TENANT.getDisplayName(context) -> Responsible.TENANT
        else -> Responsible.OWNER
    }
    val calculateMethod = when (selectedCalculateMethod) {
        CalculateMethod.FIXED.getDisplayName(context) -> CalculateMethod.FIXED
        CalculateMethod.DANG.getDisplayName(context) -> CalculateMethod.DANG
        CalculateMethod.AREA.getDisplayName(context) -> CalculateMethod.AREA
        else -> CalculateMethod.FIXED
    }

    val calculateUnitMethod = when (selectedUnitCalculateMethod) {
        CalculateMethod.FIXED.getDisplayName(context) -> CalculateMethod.FIXED
        CalculateMethod.DANG.getDisplayName(context) -> CalculateMethod.PEOPLE
        CalculateMethod.AREA.getDisplayName(context) -> CalculateMethod.AREA
        else -> CalculateMethod.FIXED
    }

    // Add a dummy "Add New Cost" item for dropdown
    val costsWithAddNew = costs + listOf(
        Costs(
            id = -1,
            buildingId = buildingId,
            costName = context.getString(R.string.add_new_cost),
            tempAmount = 0.0,
            period = Period.NONE,
            calculateMethod = CalculateMethod.FIXED,
            paymentLevel = PaymentLevel.BUILDING,
            responsible = Responsible.OWNER,
            fundFlag = FundFlag.NEGATIVE_EFFECT
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
// Cost Dropdown
                    ExposedDropdownMenuBoxExample(
                        items = costsWithAddNew,
                        selectedItem = selectedCost,
                        onItemSelected = {
                            if (it.id == -1L) {
                                // "Add New Cost" selected
                                showAddNewCostNameDialog = true
                            } else {
                                selectedCost = it
                                totalAmount = it.tempAmount.toLong().toString()
                                selectedPeriod = it.period
                            }
                        },
                        label = context.getString(R.string.cost_name),
                        modifier = Modifier.fillMaxWidth(),
                        itemLabel = { it.costName }
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Total Amount Input
                    OutlinedTextField(
                        value = totalAmount,
                        onValueChange = { totalAmount = it.filter { ch -> ch.isDigit() } },
                        label = { Text(context.getString(R.string.amount)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Amount in words (optional)
                    val amountVal = totalAmount.toLongOrNull() ?: 0L
                    val amountInWords =
                        NumberCommaTransformation().numberToWords(context, amountVal)
                    Text(
                        text = "$amountInWords ${context.getString(R.string.toman)}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Period Dropdown
                    ExposedDropdownMenuBoxExample(
                        items = Period.entries,
                        selectedItem = selectedPeriod,
                        onItemSelected = { selectedPeriod = it },
                        label = context.getString(R.string.period),
                        modifier = Modifier.fillMaxWidth(),
                        itemLabel = { it.getDisplayName(context) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (selectedPeriod == Period.NONE) {
                        OutlinedTextField(
                            value = dueDate ?: "",
                            onValueChange = { dueDate = it },
                            label = {
                                Text(
                                    text = context.getString(R.string.due),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) showDatePicker = true
                                },
//                        enabled = false,
                            readOnly = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

// FundFlag Checkbox Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(0.dp)
                    ) {
                        Checkbox(
                            checked = sharedViewModel.fundMinus,
                            onCheckedChange = { sharedViewModel.fundMinus = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = context.getString(R.string.fund_minus))
                    }
                    // FundFlag Checkbox Row
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        modifier = Modifier.padding(0.dp)
//                    ) {
//                        Checkbox(
//                            checked = fundFlagChecked,
//                            onCheckedChange = { fundFlagChecked = it }
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text(text = context.getString(R.string.fund_flag_positive_effect))
//                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (!sharedViewModel.fundMinus) {
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
                        Spacer(Modifier.height(8.dp))
                        Log.d("selectedResponsible", selectedResponsible)
                        val responseEnum = when (selectedResponsible) {
                            Responsible.OWNER.getDisplayName(context) -> Responsible.OWNER
                            Responsible.TENANT.getDisplayName(context) -> Responsible.TENANT
                            else -> Responsible.OWNER
                        }
                        Log.d("responsibleEnum", responsibleEnum.toString())
                        Log.d(
                            "Responsible.TENANT.getDisplayName(context)",
                            Responsible.TENANT.getDisplayName(context)
                        )
                        if (selectedResponsible == Responsible.TENANT.getDisplayName(context)) {
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
                            Spacer(Modifier.height(8.dp))

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
                                modifier = Modifier
                                    .padding(vertical = 8.dp),
                                label = context.getString(R.string.acount_base),
                                singleSelection = true
                            )
                        }

                        if (responseEnum.getDisplayName(context) == Responsible.OWNER.getDisplayName(
                                context
                            )
                        ) {
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
                            Spacer(Modifier.height(8.dp))

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
                                modifier = Modifier
                                    .padding(vertical = 8.dp),
                                label = context.getString(R.string.acount_base),
                                singleSelection = true
                            )
                        }

                    }

                }
            }
            // Nested Add New Cost Name Dialog
            if (showAddNewCostNameDialog) {
                AddNewCostNameDialog(
                    buildingId = buildingId,
                    onDismiss = { showAddNewCostNameDialog = false },
                    onSave = { newCostName ->
                        coroutineScope.launch {
                            val newCost =
                                sharedViewModel.insertNewCostName(buildingId, newCostName)
                            showAddNewCostNameDialog = false
                            selectedCost = newCost
                            totalAmount = newCost.tempAmount.toLong().toString()
                            selectedPeriod = newCost.period
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


        },
        confirmButton = {
            Button(onClick = {
                val cost = selectedCost ?: return@Button
//                val fundFlag =
//                    if (fundFlagChecked) FundFlag.NEGATIVE_EFFECT else FundFlag.NO_EFFECT
                val fundMinus =
                    if (sharedViewModel.fundMinus) FundFlag.NEGATIVE_EFFECT else FundFlag.NO_EFFECT
                onSave(
                    cost,
                    totalAmount,
                    selectedPeriod ?: Period.NONE,
                    FundFlag.NEGATIVE_EFFECT,
                    calculateMethod,
                    calculateUnitMethod,
                    responsibleEnum,
                    selectedUnits.map { it.unitId },
                    selectedOwners.map { it.ownerId },
                    dueDate ?: "",
                    fundMinus
                )
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