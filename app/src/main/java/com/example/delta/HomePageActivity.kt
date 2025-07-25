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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
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
import com.example.delta.data.entity.User
import com.example.delta.factory.BuildingsViewModelFactory
import com.example.delta.init.Preference
import com.example.delta.interfaces.RolePermissionsManager
import kotlinx.coroutines.flow.first


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
                    sharedViewModel = sharedViewModel,
                    imageId = R.drawable.profilepic
                ) { innerPadding ->
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
                                    items = Screen.items,
                                    sharedViewModel = sharedViewModel
                                )
                            }
                        ) { padding ->
                            NavHost(
                                navController = navController,
                                startDestination = Screen.Home.route
//                                modifier = Modifier.padding(padding)
                            ) {
                                composable(Screen.Home.route) {
                                    // List of buildings
                                    BuildingList(
                                        viewModel = buildingViewModel,
                                        sharedViewModel = sharedViewModel
                                    )

                                }

                                composable(Screen.Settings.route) {
                                    SettingsScreen(
                                        LocalContext.current
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildingList(
    viewModel: BuildingsViewModel,
    sharedViewModel: SharedViewModel
) {

    val context = LocalContext.current
    val userId = Preference().getUserId(context = context)
    Log.d("userId", userId.toString())
    var showGuestDialog by remember { mutableStateOf(false) }

    var user by remember { mutableStateOf<User?>(null) }
    LaunchedEffect(userId) {
        val fetchedUser = sharedViewModel.getUserById(userId).first()
        user = fetchedUser
        Log.d("MyScreen", "user: $fetchedUser")

        if (fetchedUser != null && fetchedUser.roleId >= 6) {
            showGuestDialog = true
        }
    }








    if(showGuestDialog) {
        AlertDialog(
            onDismissRequest = { showGuestDialog = false },
            title = {
                Text(
                    text = LocalContext.current.getString(R.string.guest_user),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            text = {
                Text(
                    text = LocalContext.current.getString(R.string.guest_dialog_info),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showGuestDialog = false
                    }
                ) {
                    Text(
                        LocalContext.current.getString(R.string.confirm),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        )
    }
//    val buildingsWithTypesAndUsages by sharedViewModel.buildingList.collectAsState()
    val buildingsWithTypesAndUsages by sharedViewModel.getBuildingsWithUserRole(userId)//getAllBuildingsWithTypeAndUsage()
        .collectAsState(initial = emptyList())
    Log.d("buildingsWithTypesAndUsages", buildingsWithTypesAndUsages.toString())
    sharedViewModel.allUsers(context = context)
//    val userRole = permissionsManager.getUserRole()
//    val permissions = permissionsManager.getPermissionsForRole(userRole)

//    if (permissions?.authorizationObject?.contains(AppActivities.HomePageActivity.activityName) == true) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp, bottom = 32.dp)

    ) {
        items(buildingsWithTypesAndUsages) { buildingWithTypesAndUsages ->
            BuildingCard(
                buildingWithTypesAndUsages = buildingWithTypesAndUsages,
                building = buildingWithTypesAndUsages.building,
                sharedViewModel = sharedViewModel,
                onClick = {
                    val intent = Intent(context, BuildingProfileActivity::class.java).apply {
                        putExtra("BUILDING_TYPE_NAME", buildingWithTypesAndUsages.buildingTypeName)
                        putExtra(
                            "BUILDING_USAGE_NAME",
                            buildingWithTypesAndUsages.buildingUsageName
                        )
                        // Only put Parcelable if your Buildings class implements Parcelable!
                        putExtra(
                            "BUILDING_DATA",
                            buildingWithTypesAndUsages.building as? Parcelable
                        )
                    }
                    context.startActivity(intent)
                },
                onDelete = { building ->
                    sharedViewModel.deleteBuildingWithRelations(
                        buildingId = building.buildingId,
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
                }
            )
        }
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
//}


@Composable
fun BuildingCard(
    buildingWithTypesAndUsages: BuildingWithTypesAndUsages,
    building: Buildings,
    onClick: () -> Unit,
    sharedViewModel: SharedViewModel,
    onDelete: (Buildings) -> Unit // Pass a callback for deletion
) {
    // Load units and owners for this building
    val units by sharedViewModel.getUnitsForBuilding(building.buildingId)
        .collectAsState(initial = emptyList())
    val owners by sharedViewModel.getOwnersForBuilding(building.buildingId)
        .collectAsState(initial = emptyList())
    Log.d("owners_size", owners.toString())

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp), // Rounded corners
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface) // Set background color
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // Image with matching corner radius
            Image(
                painter = painterResource(id = R.drawable.building_image),
                contentDescription = "Building Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth() // Match card width
                    .height(200.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,  // Match card's top corners
                            topEnd = 16.dp
                        )
                    ) // Top corners only
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${LocalContext.current.getString(R.string.building_name)} : ${building.name}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 4.dp, start = 16.dp, end = 16.dp),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(16.dp))
            // Display building type and usage
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
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
            Spacer(Modifier.height(16.dp))
            // Number of units and owners
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${LocalContext.current.getString(R.string.number_of_units)}: ${units.size}", // Replace with actual unit count
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${LocalContext.current.getString(R.string.number_of_owners)}: ${owners.size}", // Replace with actual owner count
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Top-right action icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
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

        }
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = {
                    Text(
                        text = LocalContext.current.getString(R.string.delete_building),
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
                            onDelete(building)
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
    }
}
