package com.example.delta

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MobileFriendly
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Debts
import com.example.delta.data.entity.OwnerTabItem
import com.example.delta.data.entity.OwnerTabType
import com.example.delta.data.entity.OwnersUnitsCrossRef
import com.example.delta.data.entity.Units
import com.example.delta.data.entity.User
import com.example.delta.enums.FilterType
import com.example.delta.enums.FundType
import com.example.delta.viewmodel.SharedViewModel
import com.example.delta.volley.Cost
import com.example.delta.volley.Fund
import com.example.delta.volley.Owner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import kotlin.math.roundToLong
import androidx.compose.material3.Surface
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.delta.init.FinancialReport
import com.example.delta.init.FinancialReportRow
import com.example.delta.init.Preference
import com.example.delta.server.JsonParser.OwnerWithUnitsDto
import com.example.delta.volley.Tenant
import kotlinx.coroutines.flow.channelFlow
import java.io.File


class OwnerDetailsActivity : ComponentActivity() {
    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ownerId = intent.getLongExtra("ownerId", -1L)
        val buildingId = intent.getLongExtra("buildingId", -1L)
        val hasOwnerAuth = intent.getBooleanExtra("hasOwnerAuth", false)
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
//                    val resultIntent = Intent().apply {
//                        putExtra("NEED_REFRESH", NEED_REFRESH)
//                    }
                    setResult(RESULT_OK)
                    finish()
                }
            }
        )
        setContent {
            AppTheme(useDarkTheme = sharedViewModel.isDarkModeEnabled) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    OwnerDetailsScreen(
                        ownerId = ownerId,
                        buildingId = buildingId,
                        sharedViewModel = sharedViewModel,
                        hasOwnerAuth = hasOwnerAuth,
                        onBack = { changed ->
                            if (changed) setResult(RESULT_OK)
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDetailsScreen(
    ownerId: Long,
    buildingId: Long,
    hasOwnerAuth: Boolean,
    sharedViewModel: SharedViewModel,
    onBack: (changed: Boolean) -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val tabs = listOf(
        OwnerTabItem(context.getString(R.string.overview), OwnerTabType.OVERVIEW),
        OwnerTabItem(context.getString(R.string.transaction), OwnerTabType.FINANCIALS)
    )

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var changed by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        context.getString(R.string.owner_details),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onBack(changed) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = context.getString(R.string.back)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OwnerSectionSelector(
                tabs = tabs,
                selectedIndex = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            when (tabs[selectedTab].type) {
                OwnerTabType.OVERVIEW -> OwnerOverviewTab(
                    ownerId = ownerId,
                    buildingId = buildingId,
                    hasOwnerAuth = hasOwnerAuth,
                    onChanged = { isChanged ->
                        if (isChanged) changed = true
                    }
                )

                OwnerTabType.FINANCIALS -> OwnerFinancialsTab(
                    ownerId = ownerId,
                    buildingId = buildingId,
                    sharedViewModel = sharedViewModel,
                    snackBarHostState = snackbarHostState,
                    coroutineScope = coroutineScope
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerOverviewTab(
    ownerId: Long,
    buildingId: Long,
    hasOwnerAuth : Boolean,
    modifier: Modifier = Modifier,
    onChanged: (changed: Boolean) -> Unit
) {
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val ownerApi = remember { Owner(context) }
    var currentRoleId = Preference().getRoleId(context)
    var user by remember { mutableStateOf<User?>(null) }
    var unitsForOwner by remember { mutableStateOf<List<Units>>(emptyList()) }
    var unitsForOwnerText by remember { mutableStateOf<List<String>>(emptyList()) }
    var ownerUnits by remember { mutableStateOf<List<OwnersUnitsCrossRef>>(emptyList()) }
    var editableOwnerUnits by remember { mutableStateOf<List<OwnersUnitsCrossRef>>(emptyList()) }

    var hasChanges by rememberSaveable { mutableStateOf(false) }

    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var isEditing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var units by remember { mutableStateOf<List<Units>>(emptyList()) }
    var ownersWithUnits by remember { mutableStateOf<List<OwnerWithUnitsDto>>(emptyList()) }
    var isManager by remember { mutableStateOf(false) }
    var isResident by remember { mutableStateOf(false) }
    var canEdit  by remember { mutableStateOf(true) }
    var residentUnit by remember { mutableStateOf<Units?>(null) }
    var showUnitsSheet by remember { mutableStateOf(false) }
    val selectedUnitsList = remember { mutableStateListOf<OwnersUnitsCrossRef>() }
    var selectedUnitsText by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedUnits by remember { mutableStateOf<List<Units>>(emptyList()) }
    var selectedUnitText by remember { mutableStateOf<List<String>>(emptyList()) }

    var isResidentUnitLoading by remember { mutableStateOf(false) }
    val uiLocked = isResidentUnitLoading || isLoading


    val dangSums = remember(ownersWithUnits) {
        ownersWithUnits
            .flatMap { it.ownerUnits }
            .groupBy { it.unitId }
            .mapValues { (_, list) -> list.sumOf { it.dang } }
    }

    val selectableUnits by remember(units, dangSums) {
        derivedStateOf {
            units.filter { u ->
                val usedDang = dangSums[u.unitId] ?: 0.0
                usedDang < 6.0
            }
        }
    }
    val mergedUnits = remember(selectableUnits, unitsForOwner) {
        (selectableUnits + unitsForOwner)
            .distinctBy { it.unitNumber }
    }

    LaunchedEffect(residentUnit) {
        if (residentUnit == null) {
            canEdit = true
            isResidentUnitLoading = false
            return@LaunchedEffect
        }

        isResidentUnitLoading = true

        Tenant().getTenantForUnitInBuilding(
            context = context,
            buildingId = buildingId,
            unitId = residentUnit!!.unitId,
            onSuccess = { dto ->
                val tUser = dto?.user
                if (tUser != null) {
                    if(dto.tenantUnit.status == context.getString(R.string.active)) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.it_has_active_tenant),
                            Toast.LENGTH_LONG
                        ).show()
                        canEdit = false
                    } else {
                        canEdit = true
                    }
                } else {
                    canEdit = true
                }
                isResidentUnitLoading = false
            },
            onError = { e ->
                Log.e("DebtCard", "Failed to fetch tenant for unit ${residentUnit!!.unitId}", e)
                canEdit = true
                isResidentUnitLoading = false
            }
        )
    }

    LaunchedEffect(ownerId, buildingId) {
        isLoading = true
        errorMessage = null

        Owner(context).getOwnersWithUnitsByBuilding(
            buildingId = buildingId,
            onSuccess = { list -> ownersWithUnits = list },
            onError = {
                Toast.makeText(
                    context,
                    it.message ?: context.getString(R.string.failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        com.example.delta.volley.Units().fetchUnitsForBuilding(
            context = context,
            buildingId = buildingId,
            onSuccess = { list -> units = list },
            onError = { e ->
                Toast.makeText(
                    context,
                    e.message ?: context.getString(R.string.failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        ownerApi.getOwnerWithUnits(
            ownerId = ownerId,
            buildingId = buildingId,
            onSuccess = { dto ->
                user = dto.user

                val distinctOwnerUnits = dto.ownerUnits.distinctBy { it.unitId }

                ownerUnits = distinctOwnerUnits
                editableOwnerUnits = distinctOwnerUnits
                unitsForOwner = dto.units.distinctBy { it.unitId }

                selectedUnitsList.clear()
                selectedUnitsList.addAll(distinctOwnerUnits)
                unitsForOwner.forEach { unit ->
                    selectedUnitsText = selectedUnitsText + unit.unitNumber
                }
                selectedUnits = unitsForOwner
                isManager = dto.isManager
                if(distinctOwnerUnits.any { it.isResident == true }){
                    isResident = true
                    residentUnit =
                        unitsForOwner.first { it.unitId == distinctOwnerUnits.find { it.isResident == true }!!.unitId }
                    selectedUnitText = listOf(residentUnit!!.unitNumber)
                }
                isLoading = false
            },
            onError = { e ->
                errorMessage = e.message ?: "Error loading owner"
                isLoading = false
                coroutineScope.launch {
                    snackBarHostState.showSnackbar(errorMessage ?: "")
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
            .imePadding()
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentPadding = PaddingValues(bottom = 72.dp),
                state = listState
            ) {
                if (user == null && isLoading) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(vertical = 24.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = context.getString(R.string.loading),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else if (user == null && errorMessage != null) {
                    item {
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else if (user != null) {
                    item {
                        OwnerInfoRow(
                            Icons.Default.Person,
                            "${user!!.firstName} ${user!!.lastName}"
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }

                    item {
                        OwnerInfoRow(
                            Icons.Default.Email,
                            user!!.email?.ifBlank { context.getString(R.string.no_email) } ?: ""
                        )
                    }
                    item { Spacer(Modifier.height(12.dp)) }

                    item {
                        OwnerInfoRow(
                            Icons.Default.Phone,
                            user!!.phoneNumber!!.ifBlank { context.getString(R.string.no_phone) }
                        )
                    }
                    item { Spacer(Modifier.height(12.dp)) }

                    item {
                        OwnerInfoRow(
                            Icons.Default.MobileFriendly,
                            user!!.mobileNumber.ifBlank { context.getString(R.string.no) }
                        )
                    }
                    item { Spacer(Modifier.height(12.dp)) }

                    item {
                        OwnerInfoRow(
                            Icons.Default.HomeWork,
                            user!!.address?.ifBlank { context.getString(R.string.no) }.toString()
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }



                    if (!isEditing) {
                        item {
                            OwnerInfoRow(
                                Icons.Default.Badge,
                                "${context.getString(R.string.manager_teams)}: " +
                                        if (isManager) context.getString(R.string.yes) else context.getString(R.string.none)
                            )
                        }
                        item { Spacer(Modifier.height(16.dp)) }


                        item {
                            Text(
                                text = context.getString(R.string.units),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        items(ownerUnits, key = { "own-${it.unitId}" }) { unitCross ->
                            val thisUnit = unitsForOwner.firstOrNull { it.unitId == unitCross.unitId }
                            if (thisUnit != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${context.getString(R.string.unit_number)}: ${thisUnit.unitNumber} ",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${context.getString(R.string.area)}: ${thisUnit.area}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                        item {
                            OwnerInfoRow(
                                Icons.Default.Home,
                                "${context.getString(R.string.owner_is_resident)}: " +
                                        if (isResident)
                                            (context.getString(R.string.yes) + " " + context.getString(
                                                R.string.resident_in_unit
                                            ) + " " + residentUnit?.unitNumber)
                                        else context.getString(R.string.none)
                            )
                        }
                    } else {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    enabled = hasOwnerAuth,
                                    checked = isManager,
                                    onCheckedChange = { isManager = it }
                                )
                                Text(context.getString(R.string.manager), style = MaterialTheme.typography.bodyLarge)
                            }
                        }


                        item { Spacer(Modifier.height(16.dp)) }
                        item {
                            Text(
                                text = context.getString(R.string.units),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        item { Spacer(Modifier.height(8.dp)) }
                        item {
                            OutlinedButton(
                                onClick = { showUnitsSheet = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "انتخاب واحدها (${selectedUnitsList.size})",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = isResident,
                                    onCheckedChange = { checked ->
                                        if (uiLocked) return@Checkbox

                                        isResident = checked

                                        if (!checked) {
                                            residentUnit = null
                                            selectedUnitText = emptyList()

                                            for (i in selectedUnitsList.indices) {
                                                val item = selectedUnitsList[i]
                                                if (item.isResident) {
                                                    selectedUnitsList[i] = item.copy(isResident = false)
                                                }
                                            }
                                        } else {
                                            if (selectedUnitsList.size == 1) {
                                                val only = selectedUnitsList.first()
                                                selectedUnitsList[0] = only.copy(isResident = true)
                                                val onlyUnitNumber = selectedUnits.firstOrNull { it.unitId == only.unitId }?.unitNumber
                                                selectedUnitText = listOfNotNull(onlyUnitNumber)
                                                residentUnit = units.firstOrNull { it.unitId == only.unitId } ?: unitsForOwner.firstOrNull { it.unitId == only.unitId }
                                            } else if (selectedUnitsList.size > 1) {
                                                for (i in selectedUnitsList.indices) {
                                                    val item = selectedUnitsList[i]
                                                    if (item.isResident) selectedUnitsList[i] = item.copy(isResident = false)
                                                }
                                                residentUnit = null
                                                selectedUnitText = emptyList()
                                            }
                                        }
                                    }
                                )

                                Text(context.getString(R.string.owner_is_resident), style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                        item { Spacer(Modifier.height(8.dp)) }

                        item {
                            if (isResidentUnitLoading) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            if (isResident && selectedUnitsList.size > 1) {
                                ChipGroupShared(
                                    selectedItems = selectedUnitText,
                                    onSelectionChange = { newSelectionStrings ->
                                        if (uiLocked) return@ChipGroupShared

                                        val chosenUnitNumber = newSelectionStrings.firstOrNull()
                                        if (chosenUnitNumber != null) {
                                            val chosenUnit = selectedUnits.firstOrNull { it.unitNumber == chosenUnitNumber }
                                                ?: unitsForOwner.firstOrNull { it.unitNumber == chosenUnitNumber }

                                            if (chosenUnit != null) {
                                                for (i in selectedUnitsList.indices) {
                                                    val item = selectedUnitsList[i]
                                                    selectedUnitsList[i] = item.copy(isResident = item.unitId == chosenUnit.unitId)
                                                }
                                                residentUnit = units.firstOrNull { it.unitId == chosenUnit.unitId }
                                                    ?: unitsForOwner.firstOrNull { it.unitId == chosenUnit.unitId }
                                            }
                                        }
                                        selectedUnitText = newSelectionStrings
                                    },
                                    items = selectedUnitsText,
                                    label = context.getString(R.string.unit_for_resident),
                                    singleSelection = true
                                )

                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackBarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )


        if (isEditing) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color(context.getColor(R.color.white)))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        isEditing = false
                        editableOwnerUnits = ownerUnits
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
                    enabled = canEdit && !uiLocked,
                    onClick = {
                        val updatedOwnerUnits =
                            selectedUnitsList
                                .filter { it.unitId > 0 }
                                .distinctBy { it.unitId }
                                .toList()

                        ownerApi.updateOwnerUnitsAndRoleVolley(
                            buildingId = buildingId,
                            userId = ownerId,
                            units = updatedOwnerUnits,
                            isManager = isManager,
//                            isResident = isResident,
                            onSuccess = {
                                ownerUnits = updatedOwnerUnits
                                unitsForOwner = units.filter { u ->
                                    updatedOwnerUnits.any { it.unitId == u.unitId }
                                }

                                ownersWithUnits = ownersWithUnits.map { dto ->
                                    if (dto.user?.userId == ownerId) {
                                        dto.copy(ownerUnits = updatedOwnerUnits)
                                    } else dto
                                }

                                hasChanges = true
                                onChanged(true)

                                isEditing = false
                                coroutineScope.launch {
                                    snackBarHostState.showSnackbar(
                                        context.getString(R.string.success_update)
                                    )
                                }
                            },
                            onError = { e ->
                                coroutineScope.launch {
                                    snackBarHostState.showSnackbar(
                                        e.message ?: context.getString(R.string.failed)
                                    )
                                }
                            }
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = context.getString(R.string.edit),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        if (showUnitsSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            ModalBottomSheet(
                onDismissRequest = { showUnitsSheet = false },
                sheetState = sheetState
            ) {

                val scrollState = rememberLazyListState()

                Text(
                    text = context.getString(R.string.select_units),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    state = scrollState
                ) {

                    items(mergedUnits) { unit ->

                        val isSelected =
                            selectedUnitsList.any { it.unitId == unit.unitId }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    if (uiLocked) return@Checkbox

                                    if (checked) {
                                        if (!isSelected) {
                                            selectedUnitsText = selectedUnitsText + unit.unitNumber
                                            selectedUnits = selectedUnits + unit

                                            val afterAddCount = selectedUnitsList.size + 1
                                            val residentFlag = isResident && afterAddCount == 1

                                            selectedUnitsList.add(
                                                OwnersUnitsCrossRef(
                                                    ownerId = ownerId,
                                                    unitId = unit.unitId,
                                                    dang = 6.0,
                                                    isResident = residentFlag
                                                )
                                            )

                                            if (residentFlag) {
                                                selectedUnitText = listOf(unit.unitNumber)
                                                residentUnit = units.firstOrNull { it.unitId == unit.unitId }
                                                    ?: unitsForOwner.firstOrNull { it.unitId == unit.unitId }
                                            }
                                        }
                                    } else {
                                        // remove selection
                                        selectedUnitsList.removeAll { it.unitId == unit.unitId }
                                        selectedUnitsText = selectedUnitsText - unit.unitNumber
                                        selectedUnits = selectedUnits - unit

                                        // if removed unit was resident, clear resident choice
                                        if (selectedUnitText.contains(unit.unitNumber)) {
                                            selectedUnitText = emptyList()
                                            residentUnit = null
                                        }

                                        // if isResident is on and only one unit remains, auto-set it as resident
                                        if (isResident && selectedUnitsList.size == 1) {
                                            val only = selectedUnitsList.first()
                                            selectedUnitsList[0] = only.copy(isResident = true)

                                            val onlyUnitNumber = selectedUnits.firstOrNull { it.unitId == only.unitId }?.unitNumber
                                                ?: unitsForOwner.firstOrNull { it.unitId == only.unitId }?.unitNumber
                                            selectedUnitText = listOfNotNull(onlyUnitNumber)

                                            residentUnit = units.firstOrNull { it.unitId == only.unitId }
                                                ?: unitsForOwner.firstOrNull { it.unitId == only.unitId }
                                        }
                                    }
                                }
                            )

                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text(
                                    text = "${context.getString(R.string.unit_number)}: ${unit.unitNumber}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "${context.getString(R.string.area)}: ${unit.area}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        HorizontalDivider()
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
        if (!isEditing && currentRoleId != 7L && currentRoleId != 9L && currentRoleId != 10L) {
            FloatingActionButton(
                onClick = { isEditing = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit owner units")
            }
        }
    }
}


@Composable
fun OwnerInfoRow(icon: ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun OwnerTextField(label: Int, value: String, onValueChange: (String) -> Unit) {
    val context = LocalContext.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(context.getString(label), style = MaterialTheme.typography.bodyLarge) },
        modifier = Modifier.fillMaxWidth()
    )
}
@Composable
fun OwnerFinancialsTab(
    ownerId: Long,
    buildingId:Long,
    sharedViewModel: SharedViewModel,
    snackBarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var allDebts by remember { mutableStateOf<List<Debts>>(emptyList()) }
    var allCosts by remember { mutableStateOf<List<Costs>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentRoleId = Preference().getRoleId(context)
    LaunchedEffect(ownerId) {
        isLoading = true
        errorMessage = null

        Cost(context).fetchCostsWithDebts(
            ownerId = ownerId,
            buildingId = buildingId,
            unitId = null,
            onSuccess = { costs, debts ->
                allCosts = costs
                allDebts = debts.filter { it.description != "رهن" && it.description != "اجاره" }
                isLoading = false
            },
            onError = { e ->
                errorMessage = e.message ?: context.getString(R.string.failed)
                isLoading = false
                coroutineScope.launch { snackBarHostState.showSnackbar(errorMessage ?: "") }
            }
        )
    }

    val unpaidDebts = remember(allDebts) { allDebts.filter { it.paymentFlag == false } }
    val payments = remember(allDebts) { allDebts.filter { it.paymentFlag == true } }

    val transactions = remember(allDebts, payments) {
        (unpaidDebts.map {
            TransactionItem(it.debtId, it.amount, it.dueDate, it.description, FilterType.DEBT)
        } + payments.map {
            TransactionItem(it.debtId, it.amount, it.dueDate, it.description, FilterType.PAYMENT)
        }).sortedByDescending { it.date }
    }

    var filterType by rememberSaveable { mutableStateOf(FilterType.ALL) }
    val filteredTransactions = remember(transactions, filterType) {
        when (filterType) {
            FilterType.ALL -> transactions.sortedBy { it.date }
            FilterType.DEBT -> transactions.filter { it.transactionType == FilterType.DEBT }.sortedBy { it.date }
            FilterType.PAYMENT -> transactions.filter { it.transactionType == FilterType.PAYMENT }.sortedBy { it.date }
        }
    }

    val totalDebtAmount = unpaidDebts.sumOf { it.amount }.toLong()
    val totalPaymentAmount = payments.sumOf { it.amount }.toLong()

    var showReportDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            Surface(tonalElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if(currentRoleId == -1L || currentRoleId == 7L || currentRoleId == 9L || currentRoleId == 10L ){
                                Toast.makeText(context, context.getString(R.string.auth_cancel),
                                    Toast.LENGTH_LONG).show()
                                } else {
                                showReportDialog = true
                            }

                                  },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = context.getString(R.string.financial_report),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "${context.getString(R.string.debt)}: ${
                                    formatNumberWithCommas(totalDebtAmount)
                                } ${context.getString(R.string.toman)}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "${context.getString(R.string.payments)}: ${
                                    formatNumberWithCommas(totalPaymentAmount)
                                } ${context.getString(R.string.toman)}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterType.entries.forEach { type ->
                        Button(
                            onClick = { filterType = type },
                            colors = if (filterType == type)
                                ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
                            else
                                ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(
                                text = type.getDisplayName(context),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (filterType == type)
                                    Color(context.getColor(R.color.white))
                                else
                                    if(sharedViewModel.isDarkModeEnabled)
                                        Color(context.getColor(R.color.white))
                                                else
                                    Color(context.getColor(R.color.grey))
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                when {
                    isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    errorMessage != null -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = errorMessage ?: "",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    filteredTransactions.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = context.getString(R.string.no_transactions_recorded),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredTransactions.size) { idx ->
                                val item = filteredTransactions[idx]
                                TransactionRow(
                                    transaction = item,
                                    onPayment = {
                                        coroutineScope.launch {
                                            val debt = allDebts.find { it.debtId == item.id }
                                            if (debt == null) {
                                                snackBarHostState.showSnackbar(context.getString(R.string.failed))
                                                return@launch
                                            }

                                            val updatedDebt = debt.copy(paymentFlag = true)
                                            sharedViewModel.updateDebtOnServer(context, updatedDebt)

                                            allDebts = allDebts.map {
                                                if (it.debtId == debt.debtId) updatedDebt else it
                                            }

                                            val cost = allCosts.find { it.costId == debt.costId }
                                            val fundType = cost?.fundType ?: FundType.OPERATIONAL
                                            Fund().increaseBalanceFundOnServer(
                                                context = context,
                                                buildingId = debt.buildingId,
                                                amount = debt.amount,
                                                fundType = fundType,
                                                onSuccess = {
                                                    coroutineScope.launch {
                                                        sharedViewModel.loadFundBalances(context, debt.buildingId)
                                                        snackBarHostState.showSnackbar(
                                                            context.getString(
                                                                if (fundType == FundType.OPERATIONAL)
                                                                    R.string.success_pay_tooperational_fund
                                                                else
                                                                    R.string.success_pay_tocapital_fund
                                                            )
                                                        )
                                                    }
                                                },
                                                onError = {
                                                    coroutineScope.launch {
                                                        snackBarHostState.showSnackbar(context.getString(R.string.failed))
                                                    }
                                                }
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (showReportDialog) {
                FinancialReportDialog(
                    sharedViewModel = sharedViewModel,
                    onDismiss = { showReportDialog = false },
                    onSubmit = { startDate, endDate ->
                        showReportDialog = false
                        sharedViewModel.requestOwnerFinancialReportPdf(
                            sharedViewModel = sharedViewModel,
                            context = context,
                            ownerId = ownerId,
                            startDate = startDate,
                            endDate = endDate,
                            onError = { e ->
                                coroutineScope.launch {
                                    snackBarHostState.showSnackbar(e.message ?: context.getString(R.string.failed))
                                }
                            }
                        )
                    }
                )
            }
        }
    }
}
private enum class ReportRangeType { THREE_MONTHS, SIX_MONTHS, ONE_YEAR, CUSTOM }
@Composable
fun FinancialReportDialog(
    sharedViewModel: SharedViewModel,
    onDismiss: () -> Unit,
    title: String ?= "",
    onSubmit: (startDate: String, endDate: String) -> Unit
) {
    val context = LocalContext.current

    var rangeType by remember { mutableStateOf(ReportRangeType.THREE_MONTHS) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    var activeDateField by remember { mutableStateOf<DateField?>(null) }

    val focusManager = LocalFocusManager.current

    val isConfirmEnabled = remember(rangeType, startDate, endDate) {
        rangeType != ReportRangeType.CUSTOM || (startDate.isNotBlank() && endDate.isNotBlank())
    }

    AlertDialog(
        onDismissRequest = {},
        title = {
            if (title.isNullOrEmpty()) {
                Text(
                    text = context.getString(R.string.get_report),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Text(
                    text = title.toString(),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                RangeRadioRow(
                    selected = rangeType == ReportRangeType.THREE_MONTHS,
                    label = context.getString(R.string.three_months),
                    onClick = { rangeType = ReportRangeType.THREE_MONTHS }
                )
                RangeRadioRow(
                    selected = rangeType == ReportRangeType.SIX_MONTHS,
                    label = context.getString(R.string.six_months),
                    onClick = { rangeType = ReportRangeType.SIX_MONTHS }
                )
                RangeRadioRow(
                    selected = rangeType == ReportRangeType.ONE_YEAR,
                    label = context.getString(R.string.one_year),
                    onClick = { rangeType = ReportRangeType.ONE_YEAR }
                )
                RangeRadioRow(
                    selected = rangeType == ReportRangeType.CUSTOM,
                    label = context.getString(R.string.date_range),
                    onClick = { rangeType = ReportRangeType.CUSTOM }
                )

                if (rangeType == ReportRangeType.CUSTOM) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { },
                        label = { RequiredLabel(context.getString(R.string.start_date)) },
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
        },
        confirmButton = {
            Button(
                enabled = isConfirmEnabled,
                onClick = {
                    val (s, e) = when (rangeType) {
                        ReportRangeType.THREE_MONTHS -> sharedViewModel.calcRelativePersianRange(monthsBack = 3)
                        ReportRangeType.SIX_MONTHS -> sharedViewModel.calcRelativePersianRange(monthsBack = 6)
                        ReportRangeType.ONE_YEAR -> sharedViewModel.calcRelativePersianRange(monthsBack = 12)
                        ReportRangeType.CUSTOM -> startDate to endDate
                    }
                    if (s.isBlank() || e.isBlank()) return@Button
                    onSubmit(s, e)
                }
            ) {
                Text(
                    text = context.getString(R.string.get_report),
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
private fun RangeRadioRow(selected: Boolean, label: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}


fun openPdfFromBytes(context: Context, pdfBytes: ByteArray, fileName: String) {
    val safeName = fileName
        .replace("/", "-")
        .replace("\\", "-")
        .replace(":", "-")
        .replace("*", "-")
        .replace("?", "-")
        .replace("\"", "-")
        .replace("<", "-")
        .replace(">", "-")
        .replace("|", "-")
        .replace(Regex("\\s+"), "_")

    val dir = File(context.cacheDir, "reports").apply { mkdirs() }
    val file = File(dir, safeName)
    file.writeBytes(pdfBytes)

    val uri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.open_pdf)))
}


@Composable
fun TransferDebtsDialog(
    debts: List<Debts>,
    onConfirm: (List<Debts>) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedDebts by remember { mutableStateOf(debts.toSet()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                context.getString(R.string.transfer_debt_to_tenant),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        text = {
            LazyColumn {
                items(debts) { debt ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedDebts = if (selectedDebts.contains(debt)) {
                                    selectedDebts - debt
                                } else {
                                    selectedDebts + debt
                                }
                            }
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = debt.description,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = formatNumberWithCommas(debt.amount),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedDebts.toList()) },
                enabled = selectedDebts.isNotEmpty()
            ) {
                Text(
                    context.getString(R.string.confirm),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(
                    context.getString(R.string.cancel),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    )
}

@Composable
fun TransactionRow(transaction: TransactionItem, onPayment: () -> Unit) {
    val context = LocalContext.current
    val color = when (transaction.transactionType) {
        FilterType.DEBT -> Color.Red.copy(alpha = 0.8f)
        FilterType.PAYMENT -> Color.Green.copy(alpha = 0.8f)
        else -> Color.Gray
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = transaction.description.ifBlank { "سایر" },
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(8.dp))
                Row {
                    Text(
                        text = "${context.getString(R.string.due)}:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = transaction.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${formatNumberWithCommas(transaction.amount.roundToLong())} ${context.getString(R.string.toman)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(Modifier.height(8.dp))
                if (transaction.transactionType == FilterType.PAYMENT) {
                    Text(
                        text = context.getString(R.string.payment_done),
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    Button(onClick = onPayment) {
                        Text(
                            text = context.getString(R.string.payment),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

data class TransactionItem(
    val id: Long,
    val amount: Double,
    val date: String,
    val description: String,
    val transactionType: FilterType
)

@Composable
fun OwnerSectionSelector(
    tabs: List<OwnerTabItem>,
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
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.medium
                    ),
                elevation = CardDefaults.cardElevation(if (isSelected) 8.dp else 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = when (tab.type) {
                            OwnerTabType.OVERVIEW -> Icons.Default.Info
                            OwnerTabType.FINANCIALS -> Icons.Default.Money
                        },
                        contentDescription = tab.title,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
