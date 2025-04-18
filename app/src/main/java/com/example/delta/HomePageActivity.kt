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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.delta.data.entity.Buildings
import com.example.delta.viewmodel.BuildingsViewModel
import com.example.delta.viewmodel.SharedViewModel
import com.example.delta.data.entity.BuildingWithTypesAndUsages
import com.example.delta.data.model.AppDatabase
import com.example.delta.factory.BuildingsViewModelFactory


class HomePageActivity : ComponentActivity() {
    private val buildingViewModel: BuildingsViewModel by viewModels {
        BuildingsViewModelFactory(this.application)
    }
    val sharedViewModel: SharedViewModel by viewModels()

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            AppTheme {
                DetailDrawer(
                    title = getString(R.string.menu_title),
                    imageId = R.drawable.profilepic
                ) { innerPadding ->
                    val context = LocalContext.current
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        val navController = rememberNavController()

                        Scaffold(
                            bottomBar = {
                                CurvedBottomNavigation(
                                    navController = navController,
                                    items = Screen.items
                                )
                            }
                        ) { padding ->
                            NavHost(
                                navController = navController,
                                startDestination = Screen.Home.route,
                                modifier = Modifier.padding(padding)
                            ) {
                                composable(Screen.Home.route) {
                                    // List of buildings
                                    BuildingList(viewModel = buildingViewModel, sharedViewModel = sharedViewModel)

                                }

                                composable(Screen.Settings.route) { SettingsScreen() }
                            }
                        }

                    }
                }
            }
        }
    }
}

@Composable
fun ActivityLauncher(
    targetActivity: Class<*>,
    onLaunchComplete: () -> Unit = {}
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        context.startActivity(Intent(context, targetActivity))
        onLaunchComplete()
    }
}


@Composable
fun BuildingList(
    viewModel: BuildingsViewModel,
    sharedViewModel: SharedViewModel
) {
    val buildingsWithTypesAndUsages by viewModel.getAllBuildingsWithTypeAndUsage().collectAsState(initial = emptyList())
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        items(buildingsWithTypesAndUsages) { buildingWithTypesAndUsages ->
            BuildingCard(
                buildingWithTypesAndUsages = buildingWithTypesAndUsages,
                building = buildingWithTypesAndUsages.building,
                sharedViewModel = sharedViewModel,
                onClick = {
                    val intent = Intent(context, BuildingProfileActivity::class.java).apply {
                        putExtra("BUILDING_TYPE_NAME", buildingWithTypesAndUsages.buildingTypeName)
                        putExtra("BUILDING_USAGE_NAME", buildingWithTypesAndUsages.buildingUsageName)
                        // Only put Parcelable if your Buildings class implements Parcelable!
                        putExtra("BUILDING_DATA", buildingWithTypesAndUsages.building as? Parcelable)
                    }
                    context.startActivity(intent)
                },
                onDelete = { building ->
                    sharedViewModel.deleteBuildingWithRelations(
                        buildingId = building.buildingId,
                        onSuccess = {
                            Toast.makeText(context, context.getString(R.string.success_delete), Toast.LENGTH_SHORT).show()
                        },
                        onError = { error ->
                            Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            )
        }
    }
}


@Composable
fun BuildingCard(
    buildingWithTypesAndUsages: BuildingWithTypesAndUsages,
    building: Buildings,
    onClick: () -> Unit,
    sharedViewModel: SharedViewModel,
    onDelete: (Buildings) -> Unit // Pass a callback for deletion
) {
    // Load units and owners for this building
    val units by sharedViewModel.getUnitsForBuilding(building.buildingId).collectAsState(initial = emptyList())
    val owners by sharedViewModel.getOwnersForBuilding(building.buildingId).collectAsState(initial = emptyList())

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp) // Add padding around the card
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp), // Rounded corners
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface) // Set background color
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Image with matching corner radius
            Image(
                painter = painterResource(id = R.drawable.building_image),
                contentDescription = "Building Image",
                modifier = Modifier
                    .fillMaxWidth() // Match card width
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp)) // Top corners only
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${LocalContext.current.getString(R.string.building_name)} : ${building.name}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 4.dp),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Display building type and usage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${LocalContext.current.getString(R.string.building_type)}: ${buildingWithTypesAndUsages.buildingTypeName}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${LocalContext.current.getString(R.string.building_usage)}: ${buildingWithTypesAndUsages.buildingUsageName}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Number of units and owners
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${LocalContext.current.getString(R.string.number_of_units)}: ${units.size}", // Replace with actual unit count
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${LocalContext.current.getString(R.string.number_of_owners)}: ${owners.size}", // Replace with actual owner count
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Top-right action icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
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
                        text = { Text(LocalContext.current.getString(R.string.delete),
                            style = MaterialTheme.typography.bodyLarge) },
                        onClick = {
                            showMenu = false
                            showDeleteDialog = true
                        }
                    )
                }
            }

        }
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(text = LocalContext.current.getString(R.string.delete_building), style = MaterialTheme.typography.bodyLarge) },
                text = { Text(text = LocalContext.current.getString(R.string.are_you_sure), style = MaterialTheme.typography.bodyLarge) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            onDelete(building)
                        }
                    ) { Text(LocalContext.current.getString(R.string.delete), style = MaterialTheme.typography.bodyLarge) }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog = false }
                    ) {
                        Text(LocalContext.current.getString(R.string.cancel),
                            style = MaterialTheme.typography.bodyLarge) }
                }
            )
        }
    }
}
