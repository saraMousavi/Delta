package com.example.delta

import android.app.NotificationChannel
import android.app.NotificationManager
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
import com.example.delta.data.entity.BuildingWithCounts
import com.example.delta.viewmodel.BuildingsViewModel
import com.example.delta.viewmodel.SharedViewModel
import com.example.delta.data.entity.User
import com.example.delta.factory.BuildingsViewModelFactory
import com.example.delta.init.FileManagement
import com.example.delta.init.Preference
import com.example.delta.permission.Notification
import com.example.delta.volley.Building
import kotlinx.coroutines.flow.first

class HomePageActivity : ComponentActivity() {
    private val buildingViewModel: BuildingsViewModel by viewModels {
        BuildingsViewModelFactory(this.application)
    }
    val sharedViewModel: SharedViewModel by viewModels()
    private val REQUEST_CODE_PICK_EXCEL = 1001

    private lateinit var notifHelper: Notification

    @Deprecated("Use Activity Result API instead")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_EXCEL && resultCode == RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    if (inputStream != null) {
                        FileManagement().handleExcelFile(inputStream, this, sharedViewModel)
                        inputStream.close()
                    } else {
                        Toast.makeText(this, getString(R.string.failed), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(this, "خطا: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    @Suppress("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val roleId: Long? = intent.getLongExtra("role_id", 0L).let { if (it == 0L) null else it }

        // Build the permission helper BEFORE the Activity reaches STARTED/RESUMED
        notifHelper = Notification(
            caller = this,
            context = this,
            onGranted = { ensureGeneralChannel() },
            onDenied = { /* no-op */ }
        )
        // You can request immediately (or from UI later)
        notifHelper.ensurePermission()

        enableEdgeToEdge()
        setContent {
            AppTheme(useDarkTheme = sharedViewModel.isDarkModeEnabled) {
                DetailDrawer(
                    title = getString(R.string.menu_title),
                    sharedViewModel = sharedViewModel,
                    imageId = R.drawable.profilepic,
                    onImportExcel = {
                        val selectFileIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                            type = "*/*"
                            addCategory(Intent.CATEGORY_OPENABLE)
                        }
                        startActivityForResult(selectFileIntent, REQUEST_CODE_PICK_EXCEL)
                    }
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
                        ) { _ ->
                            NavHost(
                                navController = navController,
                                startDestination = Screen.Home.route
                            ) {
                                composable(Screen.Home.route) {
                                    BuildingList(
                                        sharedViewModel = sharedViewModel,
                                        roleId = roleId
                                    )
                                }
                                composable(Screen.Settings.route) {
                                    SettingsScreen(LocalContext.current)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun ensureGeneralChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "general_channel"
            val name: CharSequence = "General"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance)
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildingList(
    sharedViewModel: SharedViewModel,
    roleId: Long?
) {
    val context = LocalContext.current
    val userId = Preference().getUserId(context)

    var showGuestDialog by remember { mutableStateOf(false) }
//    var user by remember { mutableStateOf<User?>(null) }

    // load user once
    Log.d("userId", userId.toString())
    LaunchedEffect(userId) {
//        val fetched = sharedViewModel.getUserById(userId).first()
//        user = fetched
//        if (fetched != null && fetched.roleId >= 6) showGuestDialog = true
    }

    if (showGuestDialog) {
        AlertDialog(
            onDismissRequest = { showGuestDialog = false },
            title = { Text(LocalContext.current.getString(R.string.guest_user), style = MaterialTheme.typography.bodyLarge) },
            text = { Text(LocalContext.current.getString(R.string.guest_dialog_info), style = MaterialTheme.typography.bodyLarge) },
            confirmButton = {
                TextButton(onClick = {
                    showGuestDialog = false
                    context.startActivity(Intent(context, LoginPage::class.java))
                }) { Text(LocalContext.current.getString(R.string.sign_up), style = MaterialTheme.typography.bodyLarge) }
            },
            dismissButton = {
                TextButton(onClick = { showGuestDialog = false }) {
                    Text(LocalContext.current.getString(R.string.continue_to), style = MaterialTheme.typography.bodyLarge)
                }
            }
        )
    }

    var buildingUserList by remember { mutableStateOf<List<BuildingWithCounts>>(emptyList()) }
    var loadError by remember { mutableStateOf<String?>(null) }

    // run only when we actually have a non-blank mobile
    val mobile = Preference().getUserMobile(context)
    LaunchedEffect(mobile, roleId) {
        if (mobile == null) return@LaunchedEffect  // prevent first bogus call
        Building().fetchBuildingsForUser(
            context = context,
            mobileNumber = mobile,
            roleId = roleId,
            onSuccess = { list ->
                buildingUserList = list
                loadError = null
            },
            onError = { e ->
                loadError = e.message
                buildingUserList = emptyList()
            }
        )
    }

    if (loadError != null) {
        Text(
            text = loadError!!,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(16.dp)
        )
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp, bottom = 32.dp)
    ) {
        items(buildingUserList) { buildingWithTypesAndUsages ->
            BuildingCard(
                building = buildingWithTypesAndUsages,
                onClick = {
                    val intent = Intent(context, BuildingProfileActivity::class.java).apply {
                        putExtra("BUILDING_TYPE_NAME", buildingWithTypesAndUsages.buildingTypeName)
                        putExtra("BUILDING_USAGE_NAME", buildingWithTypesAndUsages.buildingUsageName)
                        putExtra("BUILDING_DATA", buildingWithTypesAndUsages.buildingId)
                    }
                    context.startActivity(intent)
                },
                onDelete = { buildingId ->
                    sharedViewModel.deleteBuildingWithRelations(
                        buildingId = buildingId,
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
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
fun BuildingCard(
    building: BuildingWithCounts,
    onClick: () -> Unit,
    onDelete: (Long) -> Unit
) {
//    val units by sharedViewModel.getUnitsForBuilding(building.buildingId)
//        .collectAsState(initial = emptyList())
//    val owners by sharedViewModel.getOwnersForBuilding(building.buildingId)
//        .collectAsState(initial = emptyList())
//    Log.d("owners_size", owners.toString())



    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.building_image),
                contentDescription = "Building Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${LocalContext.current.getString(R.string.building_name)} : ${building.name}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 4.dp, start = 16.dp, end = 16.dp),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${LocalContext.current.getString(R.string.building_type)}: ${building.buildingTypeName}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${LocalContext.current.getString(R.string.building_usage)}: ${building.buildingUsageName}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${LocalContext.current.getString(R.string.number_of_units)}: ${building.unitsCount}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${LocalContext.current.getString(R.string.number_of_owners)}: ${building.ownersCount}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

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
                    TextButton(onClick = {
                        showDeleteDialog = false
                        onDelete(building.buildingId)
                    }) {
                        Text(
                            LocalContext.current.getString(R.string.delete),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
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
