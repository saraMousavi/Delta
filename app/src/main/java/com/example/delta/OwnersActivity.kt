package com.example.delta

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import com.example.delta.data.entity.Owners
import com.example.delta.viewmodel.SharedViewModel
import com.example.delta.factory.SharedViewModelFactory

class OwnersActivity : ComponentActivity() {
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
                    var showOwnerDialog by remember { mutableStateOf(false) }
                    val ownerUnitsState =
                        sharedViewModel.getDangSumsForAllUnits().collectAsState(initial = emptyList())
                    val ownerUnits = ownerUnitsState.value
                    val dangSumsMap: Map<Long, Double> = ownerUnits.associate { it.unitId to it.totalDang }
                    val units by sharedViewModel.getAllUnits().collectAsState(initial = emptyList())
                    Scaffold(
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Text(
                                        text = getString(R.string.owners_list),
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
                                onClick = { showOwnerDialog = true }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Tenant")
                            }
                        }
                    ) { innerPadding ->
                        OwnersListScreen(
                            sharedViewModel = sharedViewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                        if (showOwnerDialog) {
                            Log.d("units", units.toString())
                            OwnerDialog(
                                units = units,
                                onDismiss = { showOwnerDialog = false },
                                dangSums = dangSumsMap,
                                onAddOwner = { newOwner, selectedUnits, isManager, selectedBuilding ->
//
                                    sharedViewModel.saveOwnerWithUnits(newOwner, selectedUnits, isManager, true, selectedBuilding!!.buildingId)

                                    showOwnerDialog = false
                                },
                                sharedViewModel = sharedViewModel,
                                isOwner = true,
                                building = null
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OwnersListScreen(
    sharedViewModel: SharedViewModel,
    modifier: Modifier = Modifier
) {
    val owners by sharedViewModel.getAllOwners().collectAsState(initial = emptyList())
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(owners) { owner ->
            OwnerWithUnitsAndBuildingsCard(owner, sharedViewModel)
        }
    }
}


@Composable
fun OwnerWithUnitsAndBuildingsCard(
    owner: Owners,
    sharedViewModel: SharedViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val units by sharedViewModel.getUnitsForOwners(owner.ownerId).collectAsState(initial = emptyList())

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
                    text = "${context.getString(R.string.first_name)}: ${owner.firstName}",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${context.getString(R.string.last_name)}: ${owner.lastName}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(Modifier.height(8.dp))
//            Text(
//                text = "${context.getString(R.string.email)}: ${owner.email}",
//                style = MaterialTheme.typography.bodyLarge
//            )
            Text(
                text = "${context.getString(R.string.phone_number)}: ${owner.phoneNumber}",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${context.getString(R.string.mobile_number)}: ${owner.mobileNumber}",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (units.isNotEmpty()) {
                Text(
                    text = context.getString(R.string.units),
                    style = MaterialTheme.typography.bodyLarge
                )
                units.forEach { unit ->
                    val building by sharedViewModel.getBuildingsForUnit(unit.unit.unitId).collectAsState(initial = null)
                    Column(modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            if (building != null) {
                                Text(
                                    text = "${context.getString(R.string.building)}: ${building?.name ?: ""}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "${context.getString(R.string.unit_number)}: ${unit.unit.unitNumber}, " +
                                        "${context.getString(R.string.area)}: ${unit.unit.area}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                    }
                }
            }
        }
    }
}

