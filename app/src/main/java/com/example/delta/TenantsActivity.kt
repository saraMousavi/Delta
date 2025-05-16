package com.example.delta

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.delta.data.entity.TenantsUnitsCrossRef
import com.example.delta.viewmodel.SharedViewModel
import com.example.delta.factory.SharedViewModelFactory

class TenantsActivity : ComponentActivity() {
    private val sharedViewModel: SharedViewModel by viewModels {
        SharedViewModelFactory(application = this.application)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        setContent {
            AppTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    var showTenantDialog by remember { mutableStateOf(false) }
                    val units by sharedViewModel.getAllUnits().collectAsState(initial = emptyList())

                    Scaffold(
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Text(
                                        text = getString(R.string.tenant_list),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                navigationIcon = {
                                    IconButton(onClick = { finish() }) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back"
                                        )
                                    }
                                }
                            )
                        },
                        floatingActionButton = {
                            FloatingActionButton(
                                onClick = { showTenantDialog = true }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Tenant")
                            }
                        }
                    ) { innerPadding ->
                        TenantsListScreen(
                            sharedViewModel = sharedViewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                        if (showTenantDialog) {
                            TenantDialog(
                                sharedViewModel = sharedViewModel,
                                units = units, // or fetch units as needed
                                onDismiss = { showTenantDialog = false },
                                onAddTenant = { newTenant, selectedUnit ->
                                    // Save tenant (and building if needed)
                                    sharedViewModel.saveTenantWithUnit(newTenant, selectedUnit)
                                    showTenantDialog = false
                                }
                                , isTenant = true
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TenantsListScreen(
    sharedViewModel: SharedViewModel,
    modifier: Modifier = Modifier
) {
    val tenants by sharedViewModel.getAllTenants().collectAsState(initial = emptyList())
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(tenants) { tenant ->
            TenantWithUnitAndBuildingCard(tenant, sharedViewModel)
        }
    }
}

@Composable
fun TenantWithUnitAndBuildingCard(
    tenantUnit: TenantsUnitsCrossRef,
    sharedViewModel: SharedViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val unit by sharedViewModel.getUnit(tenantUnit.unitId).collectAsState(initial = null)
    val tenant by sharedViewModel.getTenant(tenantUnit.tenantId).collectAsState(initial = null)
    val building by remember(unit) {
        derivedStateOf {
            unit?.buildingId?.let { sharedViewModel.getBuilding(it) }
        }
    }.value?.collectAsState(initial = null) ?: remember { mutableStateOf(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth()) {
                Text(
                    text = "${context.getString(R.string.first_name)}: ${tenant?.firstName}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${context.getString(R.string.last_name)}: ${tenant?.lastName}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(Modifier.height(8.dp))
//            Text(
//                text = "${context.getString(R.string.email)}: ${tenant?.email}",
//                style = MaterialTheme.typography.bodyLarge
//            )
            Text(
                text = "${context.getString(R.string.phone_number)}: ${tenant?.phoneNumber}",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${context.getString(R.string.mobile_number)}: ${tenant?.mobileNumber}",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${context.getString(R.string.status)}: ${tenantUnit.status}",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(8.dp))
            if (unit != null) {
                Row(modifier = Modifier.fillMaxSize()) {
                    val buildingData = building
                    if (buildingData != null) {
                        Text(
                            text = "${context.getString(R.string.building)}: ${buildingData.name}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${context.getString(R.string.unit_number)}: ${unit?.unitNumber}, " +
                                "${context.getString(R.string.area)}: ${unit?.area}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
