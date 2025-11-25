package com.example.delta

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.example.delta.data.entity.Notification
import com.example.delta.enums.NotificationType
import com.example.delta.enums.UserWithUnit
import com.example.delta.init.Preference
import com.example.delta.viewmodel.SharedViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailDrawer(
    title: String,
    imageId: Int,
    sharedViewModel: SharedViewModel,
    onImportExcel: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    AppTheme (useDarkTheme = sharedViewModel.isDarkModeEnabled){
        // Right drawer state (main menu)
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        // Left drawer state (notifications)
        val notificationsDrawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        val notificationsWithRead by sharedViewModel
            .getNotificationsWithReadStatus()
            .collectAsState(initial = emptyList())

        val isDarkModeEnabled = sharedViewModel.isDarkModeEnabled
        val unreadCount = notificationsWithRead.count { !it.crossRef.isRead }
        var showSheet by remember { mutableStateOf(false) }
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        // *** Left drawer: Notifications drawer wrapped in LTR to open from Left ***
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            ModalNavigationDrawer(
                drawerContent = {
                    ModalDrawerSheet {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        NotificationsDrawerContent(
                            sharedViewModel = sharedViewModel,
                            onNotificationRead = { notificationId ->
                                scope.launch {
                                    val crossRef =
                                        sharedViewModel.getUsersNotificationsById(notificationId)
                                            .firstOrNull()
                                    if (crossRef != null && !crossRef.isRead) {
                                        val updatedCrossRef = crossRef.copy(isRead = true)
                                        sharedViewModel.updateUserNotificationCrossRef(
                                            updatedCrossRef
                                        )
                                    }
                                }
                            }
                        )
                        }
                    }
                },
                drawerState = notificationsDrawerState,
            ) {
                // *** Right drawer: Main Menu drawer wrapped in RTL to open from Right ***
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    ModalNavigationDrawer(
                        drawerContent = {
                            ModalDrawerSheet {
                                Column(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .verticalScroll(rememberScrollState()),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(
                                        text = title,
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = imageId),
                                            contentDescription = "My Image",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(120.dp)
                                                .clip(CircleShape)
                                        )
                                    }
                                    Spacer(Modifier.height(12.dp))

                                    // Your NavigationDrawerItems below
                                    NavigationDrawerItem(
                                        label = { Text(context.getString(R.string.account)) },
                                        selected = false,
                                        onClick = {
                                            context.startActivity(Intent(context, UserProfileActivity::class.java))
                                        }
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    // Your NavigationDrawerItems below
                                    NavigationDrawerItem(
                                        label = { Text(context.getString(R.string.financial_dashboard)) },
                                        selected = false,
                                        onClick = {
                                            context.startActivity(Intent(context, DashboardActivity::class.java))
                                        }
                                    )
                                    Spacer(Modifier.height(12.dp))
//                                    NavigationDrawerItem(
//                                        label = { Text(context.getString(R.string.customizing)) },
//                                        selected = false,
//                                        onClick = {}
//                                    )
//                                    Spacer(Modifier.height(12.dp))
//                                    NavigationDrawerItem(
//                                        label = { Text(context.getString(R.string.change_language)) },
//                                        selected = false,
//                                        onClick = {}
//                                    )
//                                    Spacer(Modifier.height(12.dp))
                                    NavigationDrawerItem(
                                        label = { Text(context.getString(R.string.import_from_excel)) },
                                        selected = false,
                                        onClick = {
                                            showSheet = true
                                        }
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    NavigationDrawerItem(
                                        label = { Text(context.getString(R.string.dark_mode)) },
                                        selected = false,
                                        onClick = {
                                            sharedViewModel.isDarkModeEnabled = !isDarkModeEnabled
                                            if (isDarkModeEnabled){
                                                sharedViewModel.saveDarkModeState(context, false)
                                            } else {
                                                sharedViewModel.saveDarkModeState(context, true)
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (isDarkModeEnabled) Icons.Default.DarkMode else Icons.Default.LightMode,
                                                contentDescription = null
                                            )
                                        }
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    NavigationDrawerItem(
                                        label = { Text(context.getString(R.string.sharing)) },
                                        selected = false,
                                        onClick = {}
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    NavigationDrawerItem(
                                        label = { Text(context.getString(R.string.app_info)) },
                                        selected = false,
                                        onClick = {}
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    NavigationDrawerItem(
                                        label = { Text(context.getString(R.string.guide_active)) },
                                        selected = false,
                                        onClick = {}
                                    )

                                    Spacer(Modifier.height(12.dp))
                                    NavigationDrawerItem(
                                        label = { Text(context.getString(R.string.logout)) },
                                        selected = false,
                                        onClick = { val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                                            prefs.edit { putBoolean("is_logged_in", false) }
                                            context.startActivity(Intent(context, LoginPage::class.java))
                                        }
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    NavigationDrawerItem(
                                        label = { Text(context.getString(R.string.app_version)) },
                                        selected = false,
                                        onClick = {
                                        }
                                    )
                                    Spacer(Modifier.height(12.dp))
                                }
                            }
                        },
                        drawerState = drawerState,
                    ) {
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = { Text("") },
                                    navigationIcon = {
                                        IconButton(onClick = {
                                            scope.launch {
                                                if (drawerState.isClosed) {
                                                    drawerState.open()
                                                } else {
                                                    drawerState.close()
                                                }
                                            }
                                        }) {
                                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                                        }
                                    },
                                    actions = {
                                        Box {
                                            IconButton(
                                                onClick = {
                                                    scope.launch {
                                                        notificationsDrawerState.open()
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Notifications,
                                                    contentDescription = "Notifications"
                                                )
                                            }
                                            if (unreadCount > 0) {
                                                Badge(
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .align(Alignment.TopEnd),
                                                    containerColor = Color.Red,
                                                    contentColor = Color.White
                                                ) {
                                                    Text(
                                                        text = unreadCount.toString(),
                                                        style = MaterialTheme.typography.labelSmall
                                                    )
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        ) { innerPadding ->
                            content(innerPadding)
                            if (showSheet) {
                                ModalBottomSheet(
                                    onDismissRequest = { showSheet = false },
                                    sheetState = sheetState,
                                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = context.getString(R.string.select_data_input_method),
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.padding(bottom = 16.dp)
                                        )
                                        Text(
                                            text = context.getString(R.string.template_download),
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.clickable {
                                                // Handle template download here
                                            },
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Button(
                                            onClick = {
                                                showSheet = false
                                                onImportExcel()  // Trigger your import Excel logic
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(52.dp),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Text(
                                                text = context.getString(R.string.import_from_excel),
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
        }
    }
}

@Composable
fun NotificationsDrawerContent(
    sharedViewModel: SharedViewModel,
    onNotificationRead: (Long) -> Unit
) {
    val systemNotifications by sharedViewModel.systemNotifications.collectAsState()
    val managerNotifications by sharedViewModel.managerNotifications.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val userId = Preference().getUserId(context)

    LaunchedEffect(userId) {
        if (userId != 0L) {
            sharedViewModel.refreshNotificationsForUser(userId)
        }
    }

    val onRead: (Long) -> Unit = { notificationId ->
        if (userId != 0L) {
            sharedViewModel.markNotificationReadForUser(userId, notificationId)
        }
    }

    if (showDialog) {
        NotificationCreationDialog(
            onDismiss = { showDialog = false },
            onCreate = { notification, selectedOwnerIds, selectedTenantIds ->
                val targetUserIds = selectedOwnerIds + selectedTenantIds
                sharedViewModel.sendNotificationToUsers(
                    notification = notification,
                    targetUserIds = targetUserIds,
                    buildingId = null
                )
                showDialog = false
            },
            sharedViewModel = sharedViewModel
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        Spacer(Modifier.height(24.dp))

        Row {
            Button(
                onClick = { showDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = context.getString(R.string.create_notification),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                context.getString(R.string.manager_notification),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(8.dp))

            if (managerNotifications.isEmpty()) {
                Text(
                    text = context.getString(R.string.no_manager_notification),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            } else {
                managerNotifications.forEach { item ->
                    NotificationCard(
                        notification = item.notification,
                        onNotificationRead = onRead,
                        isRead = item.crossRef.isRead,
                        sharedViewModel = sharedViewModel
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = context.getString(R.string.system_notification),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(8.dp))

            if (systemNotifications.isEmpty()) {
                Text(
                    text = context.getString(R.string.no_system_notification),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            } else {
                systemNotifications.forEach { item ->
                    NotificationCard(
                        notification = item.notification,
                        onNotificationRead = onRead,
                        isRead = item.crossRef.isRead,
                        sharedViewModel = sharedViewModel
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: Notification,
    onNotificationRead: (Long) -> Unit,
    isRead: Boolean,
    sharedViewModel: SharedViewModel
) {
    val context = LocalContext.current
    val userId = Preference().getUserId(context = context)
    var expanded by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(expanded) {
        if (expanded && !isRead) {
            onNotificationRead(notification.notificationId)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = if (isRead)
            CardDefaults.cardElevation(defaultElevation = 0.dp)
        else
            CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRead) Color.LightGray else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .clickable { expanded = !expanded }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                if (!isRead) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color.Red, shape = CircleShape)
                    )
                }

                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options"
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                context.getString(R.string.delete_notification),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            scope.launch {
                                sharedViewModel.deleteUserNotificationCrossRef(
                                    userId = userId,
                                    notificationId = notification.notificationId
                                )
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = if (expanded) {
                    notification.message
                } else {
                    notification.message.take(100) +
                            if (notification.message.length > 100) "..." else ""
                },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (expanded) Int.MAX_VALUE else 2
            )
        }
    }
}
@Composable
fun NotificationCreationDialog(
    onDismiss: () -> Unit,
    onCreate: (notification: Notification, selectedOwnerIds: List<Long>, selectedTenantIds: List<Long>) -> Unit,
    sharedViewModel: SharedViewModel
) {
    val context = LocalContext.current
    val userId = Preference().getUserId(context)
    val mobileNumber = Preference().getUserMobile(context)

    LaunchedEffect(mobileNumber) {
        if (!mobileNumber.isNullOrBlank()) {
            sharedViewModel.refreshBuildingsForUserFromServer(mobileNumber)
        }
    }

    val buildings by sharedViewModel
        .getBuildingsForUser(userId)
        .collectAsState(initial = emptyList())

    var selectedBuildingId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(selectedBuildingId) {
        selectedBuildingId?.let { bId ->
            sharedViewModel.loadBuildingFullForNotifications(bId)
        }
    }

    val owners by sharedViewModel.ownersForNotification
        .collectAsState(initial = emptyList())

    val tenants by sharedViewModel.tenantsForNotification
        .collectAsState(initial = emptyList())

    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(NotificationType.MANAGER) }

    var selectedOwnerIds by remember { mutableStateOf(setOf<Long>()) }
    var selectedTenantIds by remember { mutableStateOf(setOf<Long>()) }

    val notification = Notification(
        title = title,
        message = message,
        type = type,
        userId = userId,
        timestamp = System.currentTimeMillis()
    )

    val isTitleValid = title.isNotBlank()
    val isMessageValid = message.isNotBlank()
    val isBuildingSelected = selectedBuildingId != null
    val isAnyOwnerSelected = selectedOwnerIds.isNotEmpty()
    val isAnyTenantSelected = selectedTenantIds.isNotEmpty()
    // At least one owner or tenant must be selected
    val isUserSelected = isAnyOwnerSelected || isAnyTenantSelected
    val isFormValid = isTitleValid && isMessageValid && isBuildingSelected && isUserSelected

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onCreate(notification, selectedOwnerIds.toList(), selectedTenantIds.toList()) },
                enabled = isFormValid
            ) {
                Text(text = context.getString(R.string.insert), style = MaterialTheme.typography.bodyLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = context.getString(R.string.cancel), style = MaterialTheme.typography.bodyLarge)
            }
        },
        title = {
            Text(text = context.getString(R.string.create_notification), style = MaterialTheme.typography.bodyLarge)
        },
        text = {
            Column(modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())) {

                // Title input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(text = context.getString(R.string.title), style = MaterialTheme.typography.bodyLarge) },
                    isError = !isTitleValid,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                // Message input
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text(text = context.getString(R.string.message), style = MaterialTheme.typography.bodyLarge) },
                    isError = !isMessageValid,
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    maxLines = 6
                )
                Spacer(Modifier.height(8.dp))

                // Notification type dropdown (same as before)...

                var expandedTypeDropdown by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = type.name,
                        onValueChange = {},
                        label = { Text(text = context.getString(R.string.type), style = MaterialTheme.typography.bodyLarge) },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expandedTypeDropdown = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = context.getString(R.string.select_type))
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(expanded = expandedTypeDropdown, onDismissRequest = { expandedTypeDropdown = false }) {
                        NotificationType.entries.forEach { enumType ->
                            DropdownMenuItem(
                                text = { Text(enumType.name) },
                                onClick = {
                                    type = enumType
                                    expandedTypeDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Buildings selection
                Text(text = context.getString(R.string.building_select), style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))

                if (buildings.isEmpty()) {
                    Text(
                        text = context.getString(R.string.no_building_recorded),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                } else {
                    buildings.forEach { building ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedBuildingId = building.buildingId }
                                .padding(vertical = 6.dp)
                                .background(
                                    if (selectedBuildingId == building.buildingId)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    else Color.Transparent
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedBuildingId == building.buildingId,
                                onClick = { selectedBuildingId = building.buildingId }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(text = building.name, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Owners section
                // Owners section
                if (selectedBuildingId != null) {
                    Text(
                        text = context.getString(R.string.owners),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(8.dp))

                    if (owners.isEmpty()) {
                        Text(
                            text = context.getString(R.string.no_owner_recorded),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    } else {
                        val allOwnersSelected = selectedOwnerIds.size == owners.size && owners.isNotEmpty()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable {
                                    selectedOwnerIds = if (allOwnersSelected) {
                                        emptySet()
                                    } else {
                                        owners.map { it.id }.toSet()
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = allOwnersSelected,
                                onCheckedChange = { checked ->
                                    selectedOwnerIds = if (checked) {
                                        owners.map { it.id }.toSet()
                                    } else {
                                        emptySet()
                                    }
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = context.getString(R.string.all_owners),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        owners.forEach { owner ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedOwnerIds = if (selectedOwnerIds.contains(owner.id)) {
                                            selectedOwnerIds - owner.id
                                        } else {
                                            selectedOwnerIds + owner.id
                                        }
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedOwnerIds.contains(owner.id),
                                    onCheckedChange = { checked ->
                                        selectedOwnerIds = if (checked) {
                                            selectedOwnerIds + owner.id
                                        } else {
                                            selectedOwnerIds - owner.id
                                        }
                                    }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "${owner.firstName} ${owner.lastName} - ${context.getString(R.string.unit)} ${owner.unitNumber ?: "-"}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }


                Spacer(Modifier.height(8.dp))

                // Tenants section
                // Tenants section
                if (selectedBuildingId != null) {
                    Text(
                        text = context.getString(R.string.tenants),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(8.dp))

                    if (tenants.isEmpty()) {
                        Text(
                            text = context.getString(R.string.no_tenant_recorded),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    } else {
                        val allTenantsSelected = selectedTenantIds.size == tenants.size && tenants.isNotEmpty()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable {
                                    selectedTenantIds = if (allTenantsSelected) {
                                        emptySet()
                                    } else {
                                        tenants.map { it.id }.toSet()
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = allTenantsSelected,
                                onCheckedChange = { checked ->
                                    selectedTenantIds = if (checked) {
                                        tenants.map { it.id }.toSet()
                                    } else {
                                        emptySet()
                                    }
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = context.getString(R.string.all_tenants),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        tenants.forEach { tenant ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedTenantIds = if (selectedTenantIds.contains(tenant.id)) {
                                            selectedTenantIds - tenant.id
                                        } else {
                                            selectedTenantIds + tenant.id
                                        }
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedTenantIds.contains(tenant.id),
                                    onCheckedChange = { checked ->
                                        selectedTenantIds = if (checked) {
                                            selectedTenantIds + tenant.id
                                        } else {
                                            selectedTenantIds - tenant.id
                                        }
                                    }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "${tenant.firstName} ${tenant.lastName} - ${context.getString(R.string.unit)} ${tenant.unitNumber ?: "-"}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }

            }
        }
    )
}


