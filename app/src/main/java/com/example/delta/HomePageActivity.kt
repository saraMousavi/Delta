package com.example.delta

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.delta.viewmodel.SharedViewModel
import com.example.delta.init.FileManagement
import com.example.delta.init.Preference
import com.example.delta.permission.Notification
import com.example.delta.volley.Building
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

class HomePageActivity : ComponentActivity() {
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
                        FileManagement().handleExcelFile(inputStream, this, this)
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
                                        roleId = roleId
                                    )
                                }
                                composable(Screen.Settings.route) {
                                    SettingsScreen(LocalContext.current)
                                }
                                composable(Screen.Chat.route) { ChatScreen(sharedViewModel) }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun ensureGeneralChannel() {
        val channelId = "general_channel"
        val name: CharSequence = "General"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, name, importance)
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildingList(
    roleId: Long?
) {
    val context = LocalContext.current
    val userId = Preference().getUserId(context)

    var showGuestDialog by remember { mutableStateOf(false) }
    var buildingUserList by remember { mutableStateOf<List<BuildingWithCounts>>(emptyList()) }
    var loadError by remember { mutableStateOf<String?>(null) }

    Log.d("userId", userId.toString())

    LaunchedEffect(userId) {
        // guest logic if needed
    }

    if (showGuestDialog) {
        AlertDialog(
            onDismissRequest = { showGuestDialog = false },
            title = {
                Text(
                    LocalContext.current.getString(R.string.guest_user),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            text = {
                Text(
                    LocalContext.current.getString(R.string.guest_dialog_info),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showGuestDialog = false
                    context.startActivity(Intent(context, LoginPage::class.java))
                }) {
                    Text(
                        LocalContext.current.getString(R.string.sign_up),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showGuestDialog = false }) {
                    Text(
                        LocalContext.current.getString(R.string.continue_to),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        )
    }

    val mobile = Preference().getUserMobile(context)

    LaunchedEffect(mobile, roleId) {
        if (mobile == null) return@LaunchedEffect
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

    when {
        loadError != null -> {
            Text(
                text = loadError!!,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
            )
        }

        buildingUserList.isEmpty() -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.building_image),
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    text = LocalContext.current.getString(R.string.no_building_found),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = LocalContext.current.getString(R.string.no_building_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        context.startActivity(
                            Intent(context, BuildingFormActivity::class.java)
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(50.dp)
                ) {
                    Text(
                        text = LocalContext.current.getString(R.string.add_building),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            Building().deleteBuilding(
                                context = context,
                                buildingId = buildingId,
                                onSuccess = {
                                    Toast
                                        .makeText(
                                            context,
                                            context.getString(R.string.success_delete),
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                    buildingUserList = buildingUserList.filterNot {
                                        it.buildingId == buildingId
                                    }
                                },
                                onError = { error ->
                                    Toast
                                        .makeText(
                                            context,
                                            error.message
                                                ?: context.getString(R.string.failed),
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
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

@Composable
fun BuildingCard(
    building: BuildingWithCounts,
    onClick: () -> Unit,
    onDelete: (Long) -> Unit
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.building_image),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = building.name,
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!building.street.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = building.street,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.Black.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    building.buildingTypeName?.let {
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Apartment,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }

                    building.buildingUsageName?.let {
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Domain,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "${context.getString(R.string.number_of_units)}: ${building.unitsCount}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "${context.getString(R.string.number_of_owners)}: ${building.ownersCount}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        context.getString(R.string.delete),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                onClick = {
                    showMenu = false
                    showDeleteDialog = true
                }
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = {
                    Text(
                        text = context.getString(R.string.delete_building),
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
                        onDelete(building.buildingId)
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

