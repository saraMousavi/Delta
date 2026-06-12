package com.example.delta

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.example.delta.data.dao.AuthorizationDao
import com.example.delta.data.entity.AuthorizationField
import com.example.delta.data.entity.AuthorizationObject
import com.example.delta.data.entity.BuildingWithCounts
import com.example.delta.data.entity.Role
import com.example.delta.data.entity.RoleAuthorizationObjectFieldCrossRef
import com.example.delta.data.entity.User
import com.example.delta.data.entity.UserRoleBuildingUnitCrossRef
import com.example.delta.enums.Gender
import com.example.delta.enums.PermissionLevel
import com.example.delta.init.Preference
import com.example.delta.init.Validation
import com.example.delta.viewmodel.SharedViewModel
import com.example.delta.volley.AuthObjectFieldCross
import com.example.delta.volley.AuthorizationObj
import com.example.delta.volley.Building
import com.example.delta.volley.RoleApi
import com.example.delta.volley.Users
import com.example.delta.volley.Users.UserRoleBuilding
import kotlinx.coroutines.launch

class UserManagementActivity : ComponentActivity() {
    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme(useDarkTheme = sharedViewModel.isDarkModeEnabled) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MaterialTheme {
                        UserManagementScreen(sharedViewModel)
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(viewModel: SharedViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var fabExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedBuilding by remember { mutableStateOf<BuildingWithCounts?>(null) }
    var buildings by remember { mutableStateOf<List<BuildingWithCounts>>(emptyList()) }

    var mobileNumber by remember { mutableStateOf("") }
    var searchError by remember { mutableStateOf<String?>(null) }
    var isSearching by remember { mutableStateOf(false) }

    var userRoleBuilding by remember { mutableStateOf<UserRoleBuilding?>(null) }
    var showCreateUserCard by remember { mutableStateOf(false) }

    var roles by remember { mutableStateOf<List<Role>>(emptyList()) }
    var rolesLoading by remember { mutableStateOf(false) }
    var rolesError by remember { mutableStateOf<String?>(null) }

    var currentUser by remember { mutableStateOf<User?>(null) }
    var createdUser by remember { mutableStateOf<User?>(null) }

    var selectedBuildingId by rememberSaveable { mutableLongStateOf(0L) }
    var selectedRole by remember { mutableStateOf<Role?>(null) }
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    var showNewUserDialog by remember { mutableStateOf(false) }

    var showEditAuthDialog by remember { mutableStateOf(false) }
    var showAddAuthDialog by remember { mutableStateOf(false) }
    var editObject by remember { mutableStateOf<AuthorizationObject?>(null) }
    var editFields by remember { mutableStateOf<List<AuthorizationDao.FieldWithPermission>>(emptyList()) }

    var showAssignRoleDialog by remember { mutableStateOf(false) }
    var isFromNewUserDialog by remember { mutableStateOf(false) }

    var permissionsReloadKey by remember { mutableIntStateOf(0) }
    var showRemoveRoleConfirm by remember { mutableStateOf(false) }
    var pendingRemoveRole by remember { mutableStateOf<Role?>(null) }
    var pendingRemoveBuildingId by remember { mutableLongStateOf(0L) }
    var pendingRemoveName by remember { mutableStateOf("") }

    LaunchedEffect(selectedBuildingId) {
        if (selectedBuildingId == 0L) {
            roles = emptyList()
            return@LaunchedEffect
        }

        rolesLoading = true
        rolesError = null
        try {
            roles = RoleApi().getRolesSuspend(context, buildingId = selectedBuildingId)
        } catch (e: Exception) {
            rolesError = e.message ?: context.getString(R.string.failed)
            roles = emptyList()
        } finally {
            rolesLoading = false
        }
    }


    fun isProtectedRole(role: Role): Boolean {
        val name = role.roleName.trim()
        return name == "مدیر ساختمان" || name == "ساکن" || name == "مستأجر" || name == "مالک" || name == "مستاجر"
    }

    fun resetSelections() {
        selectedBuildingId = 0L
        selectedRole = null
        selectedTabIndex = 0
        permissionsReloadKey++
    }

    fun refreshUserByMobile(mobile: String, onDone: (() -> Unit)? = null) {
        scope.launch {
            isSearching = true
            try {
                Building(context).fetchBuildingsForUser(
                    mobileNumber = Preference().getUserMobile(context).toString(),
                    roleId = Preference().getRoleId(context),
                    onSuccess = { list ->
                        buildings = list
                        if (list.size == 1 && selectedBuilding == null) {
                            selectedBuilding = list.first()
                        }
                    },
                    onError = { }
                )
                Users().fetchUserRoleByMobile(
                    context = context,
                    mobileNumber = mobile,

                    onSuccess = { ur ->
                        userRoleBuilding = ur
                        currentUser = ur.user
                        showCreateUserCard = false

                        val buildingIds = ur.userRoles.map { it.buildingId }.distinct()
                        if(selectedBuildingId == 0L) {
                            selectedBuildingId = buildingIds.firstOrNull() ?: 0L
                        }

                        val rolesForBuilding = ur.userRoles
                            .filter { it.buildingId == selectedBuildingId }
                            .map { it.roleId }
                            .distinct()

                        val firstRole = roles.firstOrNull { rolesForBuilding.contains(it.roleId) }
                        selectedRole = firstRole
                        viewModel.currentRoleId = firstRole?.roleId ?: 0L
                        selectedTabIndex = 0
                        permissionsReloadKey++
                    },
                    onNotFound = {
                        userRoleBuilding = null
                        currentUser = null
                        showCreateUserCard = true
                        resetSelections()
                    },
                    onAccessDenied = {
                        searchError = context.getString(R.string.access_denied_user_info)
                        userRoleBuilding = null
                        currentUser = null
                        showCreateUserCard = false
                        resetSelections()
                    },
                    onError = { e ->
                        searchError = e.message ?: context.getString(R.string.failed)
                        userRoleBuilding = null
                        currentUser = null
                        showCreateUserCard = false
                        resetSelections()
                    }
                )
            } finally {
                isSearching = false
                onDone?.invoke()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = context.getString(R.string.user_management),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { (context as? UserManagementActivity)?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentUser != null) {
                Box {
                    FloatingActionButton(
                        onClick = { fabExpanded = !fabExpanded }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                    }

                    DropdownMenu(
                        expanded = fabExpanded,
                        onDismissRequest = { fabExpanded = false },
                        modifier = Modifier
                            .background(color = if(viewModel.isDarkModeEnabled) Color(0xFF121212) else Color.White)
                            .width(220.dp)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = context.getString(R.string.assign_new_role),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if(viewModel.isDarkModeEnabled) DarkColorScheme.onSurface else LightColorScheme.onSurface
                                )
                            },
                            onClick = {
                                fabExpanded = false
                                isFromNewUserDialog = false
                                showAssignRoleDialog = true
                            }
                        )

                        Divider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            thickness = 1.dp
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = context.getString(R.string.add_new_auth),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if(viewModel.isDarkModeEnabled) DarkColorScheme.onSurface else LightColorScheme.onSurface
                                )
                            },
                            onClick = {
                                fabExpanded = false
                                editObject = null
                                editFields = emptyList()
                                showAddAuthDialog = true
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = context.getString(R.string.insert_mobile_for_access),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = mobileNumber,
                onValueChange = { newValue ->
                    mobileNumber = newValue
                    searchError = null
                    userRoleBuilding = null
                    currentUser = null
                    createdUser = null
                    showCreateUserCard = false
                    resetSelections()

                    val normalized = newValue.trim()
                    if (Validation().isValidIranMobile(normalized)) {
                        refreshUserByMobile(normalized)
                    }
                },
                label = {
                    Text(
                        text = context.getString(R.string.mobile_number),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (searchError != null) {
                Text(
                    text = searchError.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }


            val ur = userRoleBuilding
            val userRoles = ur?.userRoles ?: emptyList()

            if (showCreateUserCard && Validation().isValidIranMobile(mobileNumber.trim())) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = context.getString(R.string.user_not_found),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showNewUserDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = context.getString(R.string.create_new_user),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            } else if(Validation().isValidIranMobile(mobileNumber.trim())){
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if(ur != null) ur.user.firstName + " " + ur.user.lastName else "",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }


            if (currentUser != null) {

                val buildingIdsForUser = remember(userRoles) {
                    userRoles.map { it.buildingId }.distinct()
                }

                val availableBuildings = buildings
//                remember(buildings, buildingIdsForUser) {
//                    buildings//.filter { it.buildingId in buildingIdsForUser }
//                }

                LaunchedEffect(availableBuildings, selectedBuildingId) {
                    if (availableBuildings.isEmpty()) {
                        selectedBuildingId = 0L
                        selectedBuilding = null
                        return@LaunchedEffect
                    }

                    if (selectedBuildingId == 0L || availableBuildings.none { it.buildingId == selectedBuildingId }) {
                        val first = availableBuildings.first()
                        selectedBuildingId = first.buildingId
                        selectedBuilding = first
                    } else {
                        selectedBuilding = availableBuildings.firstOrNull { it.buildingId == selectedBuildingId }
                    }
                }


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExposedDropdownMenuBoxExample(
                        sharedViewModel = viewModel,
                        items = availableBuildings,
                        selectedItem = selectedBuilding,
                        onItemSelected = { b ->
                            selectedBuilding = b
                            selectedBuildingId = b.buildingId

                            val rolesForBuilding = userRoles
                                .filter { it.buildingId == selectedBuildingId }
                                .map { it.roleId }
                                .distinct()

                            val firstRole = roles.firstOrNull { it.roleId in rolesForBuilding }
                            selectedRole = firstRole
                            viewModel.currentRoleId = firstRole?.roleId ?: 0L
                            selectedTabIndex = 0
                            permissionsReloadKey++
                        },
                        label = context.getString(R.string.building),
                        itemLabel = { it.name },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                val roleIdsForBuilding = remember(userRoles, selectedBuildingId) {
                    userRoles
                        .filter { it.buildingId == selectedBuildingId }
                        .map { it.roleId }
                        .distinct()
                }
                val roleTabs = remember(roles, roleIdsForBuilding) {
                    roleIdsForBuilding.mapNotNull { id -> roles.firstOrNull { it.roleId == id } }
                }
                if (roleTabs.isNotEmpty()) {
                    val selectedRoleId = selectedRole?.roleId

                    val selectedIndex = remember(roleTabs, selectedRoleId) {
                        roleTabs.indexOfFirst { it.roleId == selectedRoleId }.let { idx ->
                            if (idx >= 0) idx else 0
                        }
                    }

                    LaunchedEffect(roleTabs, selectedRoleId, selectedBuildingId) {
                        val exists = selectedRoleId != null && roleTabs.any { it.roleId == selectedRoleId }
                        if (!exists) {
                            val first = roleTabs.first()
                            selectedRole = first
                            selectedTabIndex = 0
                            viewModel.currentRoleId = first.roleId
                            permissionsReloadKey++
                        } else {
                            if (selectedTabIndex != selectedIndex) {
                                selectedTabIndex = selectedIndex
                            }
                            if (viewModel.currentRoleId != selectedRoleId) {
                                viewModel.currentRoleId = selectedRoleId ?: 0L
                            }
                        }
                    }

                    ScrollableTabRow(
                        selectedTabIndex = selectedTabIndex,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        roleTabs.forEachIndexed { index, role ->
                            Tab(
                                selected = index == selectedTabIndex,
                                onClick = {
                                    if (selectedTabIndex != index) {
                                        selectedTabIndex = index
                                    }
                                    if (selectedRole?.roleId != role.roleId) {
                                        selectedRole = role
                                        viewModel.currentRoleId = role.roleId
                                        permissionsReloadKey++
                                    }
                                },
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    ) {
                                        Text(
                                            text = role.roleName,
                                            style = MaterialTheme.typography.bodyLarge
                                        )

                                        if (!isProtectedRole(role)) {
                                            IconButton(
                                                onClick = {
                                                    pendingRemoveRole = role

                                                    val bId = userRoleBuilding?.userRoles
                                                        ?.firstOrNull { it.roleId == role.roleId && it.buildingId == selectedBuildingId }
                                                        ?.buildingId ?: selectedBuildingId

                                                    val bName = buildings
                                                        .firstOrNull { it.buildingId == bId }
                                                        ?.name
                                                        ?: ""

                                                    pendingRemoveBuildingId = bId
                                                    pendingRemoveName = bName
                                                    showRemoveRoleConfirm = true

                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = null
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val sr = selectedRole
                    if (currentUser != null && sr != null) {
                        key(sr.roleId, selectedBuildingId, permissionsReloadKey) {
                            AuthorizationObjectsList(
                                viewModel = viewModel,
                                user = currentUser,
                                selectedRoleId = sr.roleId,
                                buildingId = selectedBuildingId,
                                reloadKey = permissionsReloadKey,
                                roleName = sr.roleName,
                                onEdit = { obj, fields ->
                                    editObject = obj
                                    editFields = fields
                                    showEditAuthDialog = true
                                },
                                onAddRequest = {
                                    editObject = null
                                    editFields = emptyList()
                                    showAddAuthDialog = true
                                }
                            )
                        }
                    }

                } else {
                    Text(
                        text = context.getString(R.string.no_permissions_for_user),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )
                }
            } else {
                when {
                    rolesLoading -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    rolesError != null -> {
                        Text(
                            text = rolesError.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        if (showRemoveRoleConfirm && currentUser != null && pendingRemoveRole != null && pendingRemoveBuildingId != 0L) {
            val role = pendingRemoveRole!!
            AlertDialog(
                onDismissRequest = { showRemoveRoleConfirm = false },
                title = { Text(text = context.getString(R.string.are_you_sure), style = MaterialTheme.typography.bodyLarge) },
                text = {
                    Text(
                        text = "${context.getString(R.string.role)}: ${role.roleName}\n${context.getString(R.string.building)}: $pendingRemoveName",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                try {
                                    Users().deleteRoleForUserSuspend(
                                        context = context,
                                        userId = currentUser!!.userId,
                                        roleId = role.roleId,
                                        buildingId = pendingRemoveBuildingId
                                    )
                                    refreshUserByMobile(mobileNumber.trim()) {
                                        showRemoveRoleConfirm = false
                                        pendingRemoveRole = null
                                        pendingRemoveBuildingId = 0L
                                    }
                                } catch (e: Exception) {
                                    searchError = e.message ?: context.getString(R.string.failed)
                                    showRemoveRoleConfirm = false
                                }
                            }
                        }
                    ) { Text(text = context.getString(R.string.delete), style = MaterialTheme.typography.bodyLarge) }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showRemoveRoleConfirm = false }) {
                        Text(text = context.getString(R.string.cancel), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            )
        }

        if (showNewUserDialog) {
            NewUserDialog(
                mobile = mobileNumber.trim(),
                roles = roles,
                sharedViewModel = viewModel,
                onDismiss = { showNewUserDialog = false },
                onUserCreated = { user ->
                    currentUser = user
                    createdUser = user
                    isFromNewUserDialog = true
                    showNewUserDialog = false
                    showAssignRoleDialog = true
                }
            )
        }

        if (showAddAuthDialog && currentUser != null && selectedRole != null && selectedBuildingId != 0L) {
            AddAuthorizationDialog(
                sharedViewModel = viewModel,
                onDismiss = { showAddAuthDialog = false },
                onAddComplete = {
                    showAddAuthDialog = false
                    permissionsReloadKey++
                },
                userId = currentUser!!.userId,
                buildingId = selectedBuildingId,
                selectedRoleId = selectedRole!!.roleId
            )
        }

        if (showEditAuthDialog && currentUser != null && selectedRole != null && selectedBuildingId != 0L) {
            EditAuthorizationDialog(
                sharedViewModel = viewModel,
                onDismiss = { showEditAuthDialog = false },
                onEditComplete = {
                    showEditAuthDialog = false
                    permissionsReloadKey++
                },
                userId = currentUser!!.userId,
                buildingId = selectedBuildingId,
                selectedRoleId = selectedRole!!.roleId,
                editObject = editObject!!
            )
        }

        if (showAssignRoleDialog && currentUser != null) {
            AssignRoleDialog(
                currentUser = currentUser!!,
                sharedViewModel = viewModel,
                onDismiss = { showAssignRoleDialog = false },
                user = createdUser,
                assignedUserRoles = userRoleBuilding?.userRoles ?: emptyList(),
                onRoleAssigned = { newRole, _ ->
                    rolesLoading = true
                    refreshUserByMobile(mobileNumber.trim()) {
                        scope.launch {
                            try {
                                if (selectedBuildingId != 0L) {
                                    roles = RoleApi().getRolesSuspend(context, buildingId = selectedBuildingId)
                                }
                            } catch (_: Exception) {
                            } finally {
                                selectedRole = newRole
                                permissionsReloadKey++
                                rolesLoading = false
                                showAssignRoleDialog = false
                            }
                        }
                    }

                }
            )
        }
    }
}

@Composable
fun AuthorizationObjectsList(
    viewModel: SharedViewModel,
    user: User?,
    selectedRoleId: Long,
    buildingId: Long,
    reloadKey: Int,
    roleName: String,
    onEdit: (AuthorizationObject, List<AuthorizationDao.FieldWithPermission>) -> Unit,
    onAddRequest: () -> Unit
) {
    val context = LocalContext.current
    var fields by remember { mutableStateOf<List<AuthorizationDao.FieldWithPermission>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    var confirmDeleteObject by remember { mutableStateOf<Triple<Long, Long, String>?>(null) }
    var confirmDeleteField by remember { mutableStateOf<AuthorizationDao.FieldWithPermission?>(null) }

   fun isGlobalRole(roleId: Long): Boolean = roleId in 1L..6L

    fun applyRoleFilter(
        list: List<AuthorizationDao.FieldWithPermission>
    ): List<AuthorizationDao.FieldWithPermission> {
        return if (isGlobalRole(selectedRoleId)) {
            list.filter { it.crossRef.roleId == selectedRoleId }
        } else {
            list.filter {
                it.crossRef.roleId == selectedRoleId //&&
                      //  it.crossRef.buildingId == buildingId
            }
        }
    }


    LaunchedEffect(user?.userId, selectedRoleId, buildingId, reloadKey) {
        if (user == null || selectedRoleId == 0L || buildingId == 0L) {
            fields = emptyList()
            errorText = null
            return@LaunchedEffect
        }
        isLoading = true
        errorText = null
        AuthObjectFieldCross().fetchFieldsWithPermissionsForRole(
            context = context,
//            userId = user.userId,
//            buildingId = buildingId,
            roleId = selectedRoleId,
            onSuccess = {
                fields = applyRoleFilter(it)
                isLoading = false
            },
            onError = { e ->
                errorText = e.message
                fields = emptyList()
                isLoading = false
            }
        )
    }

    if (user == null || selectedRoleId == 0L || buildingId == 0L) return

    when {
        isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        errorText != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = errorText.orEmpty(), color = MaterialTheme.colorScheme.error)
            }
        }
        else -> {
            val groupedFields = remember(fields) { fields.groupBy { it.objectName } }

            if (groupedFields.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = context.getString(R.string.no_permissions_for_role),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { onAddRequest() }) {
                            Text(text = context.getString(R.string.add_auth), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    groupedFields.forEach { (objectName, fieldsForObject) ->
                        item {
                            ExpandableAuthObjectCard(
                                objectName = objectName,
                                fields = fieldsForObject,
                                onEdit = {
                                    val obj = fieldsForObject.firstOrNull()?.field?.let {
                                        AuthorizationObject(it.objectId, objectName, "")
                                    } ?: AuthorizationObject(0, objectName, "")
                                    onEdit(obj, fieldsForObject)
                                },
                                onDeleteObject = {
                                    val objectId = fieldsForObject.firstOrNull()?.field?.objectId ?: 0L
                                    if (selectedRoleId != 0L && objectId != 0L) {
                                        confirmDeleteObject = Triple(selectedRoleId, objectId, objectName)
                                    }
                                },
                                roleName = roleName,
                                onDeleteField = { fieldWithPermission ->
                                    confirmDeleteField = fieldWithPermission
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(134.dp))
            }
        }
    }

    if (confirmDeleteObject != null && user != null) {
        val (roleId, objectId, objectName) = confirmDeleteObject!!
        AlertDialog(
            onDismissRequest = { confirmDeleteObject = null },
            title = { Text(text = context.getString(R.string.are_you_sure), style = MaterialTheme.typography.bodyLarge) },
            text = {
                Text(
                    text = "${context.getString(R.string.confirm_delete_object_message)}\n$objectName",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.viewModelScope.launch {
                            try {
                                AuthObjectFieldCross().deleteObjectForRoleSuspend(
                                    context = context,
                                    userId = user.userId,
                                    roleId = roleId,
                                    buildingId = buildingId,
                                    objectId = objectId
                                )
                                AuthObjectFieldCross().fetchFieldsWithPermissionsForUser(
                                    context = context,
                                    userId = user.userId,
                                    buildingId = buildingId,
                                    roleId = selectedRoleId,
                                    onSuccess = { fields = applyRoleFilter(it) },
                                    onError = { e -> errorText = e.message }
                                )
                            } catch (e: Exception) {
                                errorText = e.message
                            } finally {
                                confirmDeleteObject = null
                            }
                        }
                    }
                ) { Text(text = context.getString(R.string.delete), style = MaterialTheme.typography.bodyLarge) }
            },
            dismissButton = {
                OutlinedButton(onClick = { confirmDeleteObject = null }) {
                    Text(text = context.getString(R.string.cancel), style = MaterialTheme.typography.bodyLarge)
                }
            }
        )
    }

    if (confirmDeleteField != null && user != null) {
        val f = confirmDeleteField!!
        AlertDialog(
            onDismissRequest = { confirmDeleteField = null },
            title = { Text(text = context.getString(R.string.are_you_sure), style = MaterialTheme.typography.bodyLarge) },
            text = { Text(text = context.getString(R.string.confirm_delete_field_message), style = MaterialTheme.typography.bodyLarge) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.viewModelScope.launch {
                            try {
                                AuthObjectFieldCross().deleteSingleFieldSuspend(
                                    context = context,
                                    userId = user.userId,
                                    roleId = f.crossRef.roleId,
                                    buildingId = buildingId,
                                    objectId = f.crossRef.objectId,
                                    fieldId = f.crossRef.fieldId
                                )
                                AuthObjectFieldCross().fetchFieldsWithPermissionsForUser(
                                    context = context,
                                    userId = user.userId,
                                    buildingId = buildingId,
                                    roleId = selectedRoleId,
                                    onSuccess = { fields = applyRoleFilter(it) },
                                    onError = { e -> errorText = e.message }
                                )
                            } catch (e: Exception) {
                                errorText = e.message
                            } finally {
                                confirmDeleteField = null
                            }
                        }
                    }
                ) { Text(text = context.getString(R.string.delete), style = MaterialTheme.typography.bodyLarge) }
            },
            dismissButton = {
                OutlinedButton(onClick = { confirmDeleteField = null }) {
                    Text(text = context.getString(R.string.cancel), style = MaterialTheme.typography.bodyLarge)
                }
            }
        )
    }
}

@Composable
fun ExpandableAuthObjectCard(
    objectName: String,
    fields: List<AuthorizationDao.FieldWithPermission>,
    onEdit: () -> Unit,
    onDeleteObject: () -> Unit,
    roleName: String,
    onDeleteField: (AuthorizationDao.FieldWithPermission) -> Unit
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    val protectedRoleNames = remember {
        setOf("مدیر ساختمان", "ساکن", "مالک", "مستأجر")
    }

    val isProtected = remember(roleName) {
        val n = roleName.trim()
        n in protectedRoleNames || n.contains("مدیر ساختمان")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = objectName,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )

                if (!isProtected) {
//                    IconButton(onClick = onEdit) {
//                        Icon(Icons.Default.Edit, contentDescription = "Edit")
//                    }
                    IconButton(onClick = onDeleteObject) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Object")
                    }
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = context.getString(R.string.field_name),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
//                        Text(
//                            text = context.getString(R.string.permission_level),
//                            style = MaterialTheme.typography.bodyMedium,
//                            modifier = Modifier.width(120.dp),
//                            textAlign = TextAlign.Center
//                        )
                        if (!isProtected) {
                            Text(
                                text = context.getString(R.string.delete),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.width(56.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                        thickness = 1.dp
                    )

                    fields.forEach { field ->
                        FieldPermissionRow(
                            field = field,
                            onDeleteField = { onDeleteField(field) },
                            showDelete = !isProtected
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FieldPermissionRow(
    field: AuthorizationDao.FieldWithPermission,
    onDeleteField: () -> Unit,
    showDelete: Boolean
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = field.field.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
//        Text(
//            text = context.getString(field.crossRef.permissionLevel.labelRes),
//            style = MaterialTheme.typography.bodyLarge,
//            modifier = Modifier.width(120.dp),
//            textAlign = TextAlign.Center
//        )
        if(showDelete) {
            IconButton(onClick = onDeleteField) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Field")
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAuthorizationDialog(
    sharedViewModel: SharedViewModel,
    onDismiss: () -> Unit,
    onAddComplete: () -> Unit,
    userId: Long,
    buildingId: Long,
    selectedRoleId: Long
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var allAuthObjects by remember { mutableStateOf<List<AuthorizationObject>>(emptyList()) }
    var fields by remember { mutableStateOf<List<AuthorizationField>>(emptyList()) }
    var assignedFieldIdsForObject by remember { mutableStateOf<Set<Long>>(emptySet()) }

    var isLoadingObjects by remember { mutableStateOf(false) }
    var isLoadingFields by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    var selectedObject by remember { mutableStateOf<AuthorizationObject?>(null) }
    var selectedFields by remember { mutableStateOf<Set<AuthorizationField>>(emptySet()) }
    var selectedPermissions by remember { mutableStateOf<Map<Long, PermissionLevel>>(emptyMap()) }

    LaunchedEffect(Unit) {
        isLoadingObjects = true
        errorText = null
        try {
            allAuthObjects = AuthorizationObj().fetchAuthorizationObjectsSuspend(context)
        } catch (e: Exception) {
            errorText = e.message
        } finally {
            isLoadingObjects = false
        }
    }

    LaunchedEffect(userId, buildingId, selectedRoleId, selectedObject?.objectId) {
        try {
            val objId = selectedObject?.objectId ?: run {
                assignedFieldIdsForObject = emptySet()
                return@LaunchedEffect
            }

            val all = AuthObjectFieldCross().fetchFieldsWithPermissionsForUserSuspend(
                context = context,
                userId = userId,
                roleId = selectedRoleId
            )

            val assignedForThis = all.filter {
                it.crossRef.roleId == selectedRoleId &&
                        it.crossRef.buildingId == buildingId &&
                        it.crossRef.objectId == objId
            }

            assignedFieldIdsForObject = assignedForThis.map { it.crossRef.fieldId }.toSet()
        } catch (_: Exception) {
            assignedFieldIdsForObject = emptySet()
        }
    }

    LaunchedEffect(selectedObject?.objectId, assignedFieldIdsForObject) {
        val objId = selectedObject?.objectId ?: return@LaunchedEffect
        isLoadingFields = true
        errorText = null
        try {
            val allFields = AuthorizationObj().fetchFieldsForObjectSuspend(context, objId)
            fields = allFields.filterNot { it.fieldId in assignedFieldIdsForObject }
        } catch (e: Exception) {
            errorText = e.message
            fields = emptyList()
        } finally {
            isLoadingFields = false
        }
    }

    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(
                text = context.getString(R.string.add_auth),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        text = {
            Column {
                if (isLoadingObjects) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) { CircularProgressIndicator() }
                } else {
                    ExposedDropdownMenuBoxExample(
                        sharedViewModel = sharedViewModel,
                        items = allAuthObjects,
                        selectedItem = selectedObject,
                        onItemSelected = {
                            selectedObject = it
                            selectedFields = emptySet()
                            selectedPermissions = emptyMap()
                        },
                        label = context.getString(R.string.auth_object),
                        itemLabel = { it.name },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoadingFields && selectedObject != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) { CircularProgressIndicator() }
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                        items(fields) { field ->
                            val checked = selectedFields.contains(field)
                            val permission = selectedPermissions[field.fieldId] ?: PermissionLevel.READ

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (checked) {
                                            selectedFields = selectedFields - field
                                            selectedPermissions = selectedPermissions.toMutableMap().apply {
                                                remove(field.fieldId)
                                            }
                                        } else {
                                            selectedFields = selectedFields + field
                                            if (!selectedPermissions.containsKey(field.fieldId)) {
                                                selectedPermissions = selectedPermissions.toMutableMap().apply {
                                                    put(field.fieldId, PermissionLevel.READ)
                                                }
                                            }
                                        }
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(checked = checked, onCheckedChange = null)
                                Text(
                                    text = field.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                                )
//                                ExposedDropdownMenuBoxExample(
//                                    sharedViewModel = sharedViewModel,
//                                    items = PermissionLevel.entries.toList(),
//                                    selectedItem = permission,
//                                    onItemSelected = { newPerm ->
//                                        selectedPermissions = selectedPermissions.toMutableMap().apply {
//                                            put(field.fieldId, newPerm)
//                                        }
//                                    },
//                                    label = "",
//                                    itemLabel = { context.getString(it.labelRes) },
//                                    modifier = Modifier.width(120.dp)
//                                )
                            }
                        }
                    }
                }

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorText.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val obj = selectedObject ?: return@Button
                    if (selectedFields.isEmpty()) return@Button

                    errorText = null
                    scope.launch {
                        isSubmitting = true
                        try {
                            val items = selectedFields.map { field ->
//                                val perm = selectedPermissions[field.fieldId] ?: PermissionLevel.READ
                                RoleAuthorizationObjectFieldCrossRef(
                                    userId = userId,
                                    roleId = selectedRoleId,
                                    buildingId = buildingId,
                                    objectId = obj.objectId,
                                    fieldId = field.fieldId,
                                    permissionLevel = PermissionLevel.WRITE
                                )
                            }

                            AuthObjectFieldCross().addObjectPermissionsSuspend(
                                context = context,
                                userId = userId,
                                roleId = selectedRoleId,
                                buildingId = buildingId,
                                objectId = obj.objectId,
                                list = items
                            )

                            onAddComplete()
                        } catch (e: Exception) {
                            errorText = e.message
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                enabled = selectedObject != null && selectedFields.isNotEmpty() && !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text(text = context.getString(R.string.insert), style = MaterialTheme.typography.bodyLarge)
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = { if (!isSubmitting) onDismiss() },
                enabled = !isSubmitting
            ) {
                Text(text = context.getString(R.string.cancel), style = MaterialTheme.typography.bodyLarge)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAuthorizationDialog(
    sharedViewModel: SharedViewModel,
    onDismiss: () -> Unit,
    onEditComplete: () -> Unit,
    userId: Long,
    buildingId: Long,
    selectedRoleId: Long,
    editObject: AuthorizationObject
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var assigned by remember { mutableStateOf<List<AuthorizationDao.FieldWithPermission>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    var permissionsByFieldId by remember { mutableStateOf<Map<Long, PermissionLevel>>(emptyMap()) }
    var removedFieldIds by remember { mutableStateOf<Set<Long>>(emptySet()) }

    LaunchedEffect(userId, buildingId, selectedRoleId, editObject.objectId) {
        isLoading = true
        errorText = null
        try {
            val all = AuthObjectFieldCross().fetchFieldsWithPermissionsForUserSuspend(
                context = context,
                userId = userId,
                roleId = selectedRoleId
            )

            val assignedForThis = all.filter {
                it.crossRef.roleId == selectedRoleId &&
                        it.crossRef.buildingId == buildingId &&
                        it.crossRef.objectId == editObject.objectId
            }

            assigned = assignedForThis
            removedFieldIds = emptySet()
            permissionsByFieldId = assignedForThis.associate { it.field.fieldId to it.crossRef.permissionLevel }
        } catch (e: Exception) {
            errorText = e.message
            assigned = emptyList()
            permissionsByFieldId = emptyMap()
        } finally {
            isLoading = false
        }
    }

    val visibleAssigned = remember(assigned, removedFieldIds) {
        assigned.filterNot { it.field.fieldId in removedFieldIds }
    }

    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(
                text = context.getString(R.string.edit_auth),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        text = {
            Column {
                if (isLoading) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                        items(visibleAssigned) { item ->
                            val field = item.field
                            val currentPerm = permissionsByFieldId[field.fieldId] ?: PermissionLevel.READ

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = field.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )

                                ExposedDropdownMenuBoxExample(
                                    sharedViewModel = sharedViewModel,
                                    items = PermissionLevel.entries.toList(),
                                    selectedItem = currentPerm,
                                    onItemSelected = { newPerm ->
                                        permissionsByFieldId = permissionsByFieldId.toMutableMap().apply {
                                            put(field.fieldId, newPerm)
                                        }
                                    },
                                    label = "",
                                    itemLabel = { context.getString(it.labelRes) },
                                    modifier = Modifier.width(130.dp)
                                )

                            }
                        }
                    }

                }

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorText.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isLoading && !isSubmitting,
                onClick = {
                    scope.launch {
                        isSubmitting = true
                        errorText = null
                        try {
                            val remaining = visibleAssigned

                            val items = remaining.map { it2 ->
                                val fid = it2.field.fieldId
                                val perm = permissionsByFieldId[fid] ?: PermissionLevel.READ

                                RoleAuthorizationObjectFieldCrossRef(
                                    userId = userId,
                                    roleId = selectedRoleId,
                                    buildingId = buildingId,
                                    objectId = editObject.objectId,
                                    fieldId = fid,
                                    permissionLevel = perm
                                )
                            }

                            AuthObjectFieldCross().replaceObjectPermissionsSuspend(
                                context = context,
                                userId = userId,
                                roleId = selectedRoleId,
                                buildingId = buildingId,
                                objectId = editObject.objectId,
                                list = items
                            )

                            onEditComplete()
                        } catch (e: Exception) {
                            errorText = e.message
                        } finally {
                            isSubmitting = false
                        }
                    }
                }
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text(text = context.getString(R.string.edit), style = MaterialTheme.typography.bodyLarge)
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = { if (!isSubmitting) onDismiss() },
                enabled = !isSubmitting
            ) {
                Text(text = context.getString(R.string.cancel), style = MaterialTheme.typography.bodyLarge)
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewUserDialog(
    mobile: String,
    roles: List<Role>,
    sharedViewModel: SharedViewModel,
    onDismiss: () -> Unit,
    onUserCreated: (User) -> Unit
) {
    val context = LocalContext.current

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    var selectedGender by remember { mutableStateOf<Gender?>(null) }

    var creatingUser by remember { mutableStateOf(false) }
    var newUserError by remember { mutableStateOf<String?>(null) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(
                text = context.getString(R.string.create_new_user),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = context.getString(R.string.mobile_number) + ": $mobile",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { RequiredLabel(context.getString(R.string.first_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { RequiredLabel(context.getString(R.string.last_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it
                        emailError = if (it.isBlank()) {
                            null
                        } else if (!Validation().isValidEmail(it)) {
                            context.getString(R.string.invalid_email)
                        } else {
                            null
                        }
                                    },
                    isError = emailError != null,
                    label = {
                        Text(
                            text = context.getString(R.string.email),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = {
                        if (emailError != null) {
                            Text(
                                text = emailError.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it
                        phoneError = it.isNotBlank() && !Validation().isValidPhone(it)
                    },
                    isError = phoneError,
                    label = {
                        Text(
                            text = context.getString(R.string.phone_number),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    supportingText = {
                        if (phoneError) {
                            Text(
                                text = context.getString(R.string.invalid_phone_number),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = {
                        Text(
                            text = context.getString(R.string.address),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                GenderDropdown(
                    sharedViewModel = sharedViewModel,
                    selectedGender = selectedGender,
                    onGenderSelected = { selectedGender = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = context.getString(R.string.gender)
                )

                if (newUserError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = newUserError.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    creatingUser = true
                    newUserError = null
                    try {
                        val genderFinal = selectedGender ?: Gender.FEMALE
                        val user = User(
                            mobileNumber = mobile,
                            firstName = firstName.trim(),
                            lastName = lastName.trim(),
                            email = email.trim().ifBlank { null },
                            phoneNumber = phoneNumber.trim().ifBlank { null },
                            address = address.trim().ifBlank { null },
                            gender = genderFinal,
                            password = "123456"
                        )
                        onUserCreated(user)
                    } catch (e: Exception) {
                        newUserError = e.message ?: context.getString(R.string.failed)
                    } finally {
                        creatingUser = false
                    }
                },
                enabled = !creatingUser && firstName.isNotBlank() && lastName.isNotBlank()&& !phoneError && emailError == null
            ) {
                if (creatingUser) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = context.getString(R.string.insert),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(
                    text = context.getString(R.string.cancel),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    )
}

fun isProtectedRole(role: Role): Boolean {
    val name = role.roleName.trim()
    val lower = name.lowercase()
    return lower.contains("مدیر ساختمان") ||
            lower == "مالک" ||
            lower == "ساکن" ||
            lower == "مستاجر" ||
            lower == "مستأجر" ||
            lower.contains("tenant") ||
            lower.contains("owner")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignRoleDialog(
    currentUser: User,
    sharedViewModel: SharedViewModel,
    onDismiss: () -> Unit,
    onRoleAssigned: (Role, Boolean) -> Unit,
    user: User? = null,
    assignedUserRoles: List<UserRoleBuildingUnitCrossRef>
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isNewlyCreatedRole by remember { mutableStateOf(false) }
    var roles by remember { mutableStateOf<List<Role>>(emptyList()) }
    var rolesLoading by remember { mutableStateOf(false) }
    var rolesError by remember { mutableStateOf<String?>(null) }
    var buildings by remember { mutableStateOf<List<BuildingWithCounts>>(emptyList()) }
    var selectedBuilding by remember { mutableStateOf<BuildingWithCounts?>(null) }

    LaunchedEffect(selectedBuilding?.buildingId) {
        val bid = selectedBuilding?.buildingId ?: run {
            roles = emptyList()
            return@LaunchedEffect
        }

        rolesLoading = true
        rolesError = null
        try {
            roles = RoleApi().getRolesSuspend(context, buildingId = bid)
        } catch (e: Exception) {
            rolesError = e.message ?: context.getString(R.string.failed)
            roles = emptyList()
        } finally {
            rolesLoading = false
        }
    }


    val baseRoles = remember(roles) {
        roles.filterNot { role ->
            val name = role.roleName.lowercase()
            name == "مالک" ||
            name == "مستأجر" ||
                    name == "ساکن" ||
                    name.contains("مهمان") ||
                    name.contains("سیستم")
        }
    }



    val assignedRoleIdsForSelectedBuilding = remember(assignedUserRoles, selectedBuilding?.buildingId) {
        val bid = selectedBuilding?.buildingId
        if (bid == null) emptySet()
        else assignedUserRoles
            .filter { it.buildingId == bid }
            .map { it.roleId }
            .toSet()
    }

    val availableRoles = remember(baseRoles, assignedRoleIdsForSelectedBuilding) {
        baseRoles.filterNot { it.roleId in assignedRoleIdsForSelectedBuilding }
    }

    var selectedRole by remember { mutableStateOf<Role?>(null) }


    LaunchedEffect(selectedBuilding?.buildingId) {
        selectedRole = null
    }

    val dropdownItems = remember(availableRoles, context) {
        availableRoles + Role(
            roleId = -1L,
            roleName = context.getString(R.string.create_new_role),
            roleDescription = ""
        )
    }

    LaunchedEffect(availableRoles, selectedBuilding?.buildingId) {
        if (availableRoles.isEmpty()) {
            selectedRole = null
            return@LaunchedEffect
        }
        val currentId = selectedRole?.roleId
        if (currentId == null || availableRoles.none { it.roleId == currentId }) {
            selectedRole = availableRoles.first()
        }
    }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    var buildingsLoading by remember { mutableStateOf(false) }
    var buildingsError by remember { mutableStateOf<String?>(null) }

    var showNewRoleSheet by remember { mutableStateOf(false) }
    var newRoleNameTitle by remember { mutableStateOf("") }
    var newRoleError by remember { mutableStateOf<String?>(null) }
    var isCreatingRole by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        buildingsLoading = true
        buildingsError = null
        try {
            val mobile = Preference().getUserMobile(context)?.trim().orEmpty()
            if (mobile.isNotEmpty()) {
                Building(context).fetchBuildingsForUser(
                    mobileNumber = mobile,
                    roleId = Preference().getRoleId(context),
                    onSuccess = { list ->
                        buildings = list
                        selectedBuilding = list.firstOrNull()
                        buildingsLoading = false
                    },
                    onError = { e ->
                        buildingsError = e.message
                        buildingsLoading = false
                    }
                )
            } else {
                buildingsLoading = false
            }
        } catch (e: Exception) {
            buildingsError = e.message
            buildingsLoading = false
        }
    }

    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(
                text = context.getString(R.string.assign_new_role),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "${context.getString(R.string.mobile_number)}: ${currentUser.mobileNumber}",
                    style = MaterialTheme.typography.bodyLarge
                )

                when {
                    buildingsLoading -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                    buildingsError != null -> {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = buildingsError.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    else -> {
                        ExposedDropdownMenuBoxExample(
                            sharedViewModel = sharedViewModel,
                            items = buildings,
                            selectedItem = selectedBuilding,
                            onItemSelected = { selectedBuilding = it },
                            label = context.getString(R.string.building),
                            itemLabel = { it.name },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorText.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                when {
                    selectedBuilding == null -> {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = context.getString(R.string.building_required),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    rolesLoading -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                    rolesError != null -> {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = rolesError.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    else -> {
                        ExposedDropdownMenuBoxExample(
                            sharedViewModel = sharedViewModel,
                            items = dropdownItems,
                            selectedItem = selectedRole,
                            onItemSelected = { role ->
                                if (role.roleId == -1L) {
                                    selectedRole = null
                                    newRoleNameTitle = ""
                                    newRoleError = null
                                    showNewRoleSheet = true
                                    isNewlyCreatedRole = false
                                } else {
                                    selectedRole = role
                                    isNewlyCreatedRole = false
                                }
                            },
                            label = context.getString(R.string.role),
                            itemLabel = { it.roleName },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        isSubmitting = true
                        errorText = null
                        try {
                            val finalRole = selectedRole ?: run {
                                errorText = context.getString(R.string.role_required)
                                isSubmitting = false
                                return@launch
                            }

                            val building = selectedBuilding
                            if (building == null) {
                                errorText = context.getString(R.string.building_required)
                                isSubmitting = false
                                return@launch
                            }

                            if (user == null) {
                                Users().addUserRoleSuspend(
                                    context = context,
                                    userId = currentUser.userId,
                                    roleId = finalRole.roleId,
                                    buildingId = building.buildingId
                                )
                            } else {
                                Users().addUserRoleSuspend(
                                    context = context,
                                    roleId = finalRole.roleId,
                                    buildingId = building.buildingId,
                                    user = user
                                )
                            }

                            onRoleAssigned(finalRole, isNewlyCreatedRole)
                        } catch (e: Exception) {
                            errorText = e.message ?: context.getString(R.string.failed)
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                enabled = !isSubmitting &&
                        selectedRole != null &&
                        selectedBuilding != null &&
                        !buildingsLoading
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = context.getString(R.string.insert),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = { if (!isSubmitting) onDismiss() },
                enabled = !isSubmitting
            ) {
                Text(
                    text = context.getString(R.string.cancel),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    )

    if (showNewRoleSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                if (!isCreatingRole) {
                    showNewRoleSheet = false
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = context.getString(R.string.create_new_role),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newRoleNameTitle,
                    onValueChange = { newRoleNameTitle = it },
                    label = {
                        Text(
                            text = context.getString(R.string.role_name),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (newRoleError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = newRoleError.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        scope.launch {
                            if (newRoleNameTitle.isBlank()) {
                                newRoleError = context.getString(R.string.role_name_required)
                                return@launch
                            }
                            isCreatingRole = true
                            newRoleError = null
                            try {
                                isCreatingRole = true

                                RoleApi().createRole(
                                    context = context,
                                    name = newRoleNameTitle.trim(),
                                    description = "",
                                    buildingId = selectedBuilding!!.buildingId,
                                    onSuccess = { createdRole ->
                                        isCreatingRole = false
                                        roles = (roles + createdRole).distinctBy { it.roleId }
                                        selectedRole = createdRole
                                        isNewlyCreatedRole = true
                                        showNewRoleSheet = false
                                    },
                                    onError = { e ->
                                        isCreatingRole = false
                                        Toast.makeText(context, e.message ?: e.toString(), Toast.LENGTH_LONG).show()
                                    }
                                )


                            } catch (e: Exception) {
                                newRoleError = e.message ?: context.getString(R.string.failed)
                            } finally {
                                isCreatingRole = false
                            }
                        }
                    },
                    enabled = !isCreatingRole,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isCreatingRole) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = context.getString(R.string.insert),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}
