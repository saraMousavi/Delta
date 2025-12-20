package com.example.delta

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
            AppTheme (useDarkTheme = sharedViewModel.isDarkModeEnabled){
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

    var selectedRole by remember { mutableStateOf<Role?>(null) }
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    var showNewUserDialog by remember { mutableStateOf(false) }

    var showAuthDialog by remember { mutableStateOf(false) }
    var editObject by remember { mutableStateOf<AuthorizationObject?>(null) }
    var editFields by remember {
        mutableStateOf<List<AuthorizationDao.FieldWithPermission>>(emptyList())
    }

    var showAssignRoleDialog by remember { mutableStateOf(false) }
    var isFromNewUserDialog by remember { mutableStateOf(false) }
    var pendingCreatedUserId by remember { mutableStateOf<Long?>(null) }

    var permissionsReloadKey by remember { mutableIntStateOf(0) }


    LaunchedEffect(Unit) {
        rolesLoading = true
        rolesError = null
        try {
            roles = RoleApi().getRolesSuspend(context)
        } catch (e: Exception) {
            rolesError = e.message ?: context.getString(R.string.failed)
        } finally {
            rolesLoading = false
        }
    }

    LaunchedEffect(selectedRole) {
        selectedRole?.let {
            viewModel.currentRoleId = it.roleId
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
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentUser != null) {
                ExtendedFloatingActionButton(
                    onClick = {
                        pendingCreatedUserId = null
                        isFromNewUserDialog = false
                        showAssignRoleDialog = true
                    },
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text(context.getString(R.string.assign_new_role),
                            style = MaterialTheme.typography.bodyLarge,) }
                )

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
                    selectedRole = null
                    showCreateUserCard = false

                    val normalized = newValue.trim()
                    if (Validation().isValidIranMobile(normalized)) {
                        scope.launch {
                            isSearching = true
                            try {
                                Users().fetchUserRoleByMobile(
                                    context = context,
                                    mobileNumber = normalized,
                                    onSuccess = { ur ->
                                        userRoleBuilding = ur
                                        currentUser = ur.user
                                        showCreateUserCard = false

                                        val roleIds = ur.userRoles.map { it.roleId }.distinct()
                                        val firstRole = roles.firstOrNull { r ->
                                            roleIds.contains(r.roleId)
                                        }
                                        selectedRole = firstRole
                                        viewModel.currentRoleId = firstRole?.roleId ?: 0L
                                    },
                                    onNotFound = {
                                        userRoleBuilding = null
                                        currentUser = null
                                        selectedRole = null
                                        showCreateUserCard = true
                                    },
                                    onError = { e ->
                                        searchError = e.message
                                            ?: context.getString(R.string.failed)
                                        userRoleBuilding = null
                                        currentUser = null
                                        selectedRole = null
                                        showCreateUserCard = false
                                    }
                                )
                            } finally {
                                isSearching = false
                            }
                        }
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
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

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
            }

            val userRoles = userRoleBuilding?.userRoles ?: emptyList()
            Log.d("userRoles", userRoles.toString())

            if (userRoles.isNotEmpty()) {
                val roleIdsForUser = userRoles.map { it.roleId }.distinct()

                val roleTabs = roleIdsForUser.mapNotNull { id ->
                    roles.find { it.roleId == id }
                }

                val tabCount = roleTabs.size
                Log.d("roleTabs", "count=$tabCount, items=$roleTabs, selectedRole=${selectedRole?.roleId}, selectedTabIndex=$selectedTabIndex")

                if (tabCount > 0) {
                    LaunchedEffect(tabCount) {
                        if (selectedRole == null || roleTabs.none { it.roleId == selectedRole?.roleId }) {
                            val first = roleTabs.first()
                            selectedRole = first
                            selectedTabIndex = 0
                            viewModel.currentRoleId = first.roleId
                        }
                    }

                    val hasValidIndex = selectedTabIndex in 0 until tabCount

                    LaunchedEffect(tabCount, selectedRole?.roleId) {
                        if (tabCount > 0) {
                            val idx = roleTabs.indexOfFirst { it.roleId == selectedRole?.roleId }
                            if (idx >= 0 && idx != selectedTabIndex) {
                                selectedTabIndex = idx
                            }
                        }
                    }

                    if (hasValidIndex) {
                        ScrollableTabRow(
                            selectedTabIndex = selectedTabIndex,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            roleTabs.forEachIndexed { index, role ->
                                Tab(
                                    selected = index == selectedTabIndex,
                                    onClick = {
                                        selectedTabIndex = index
                                        selectedRole = role
                                        viewModel.currentRoleId = role.roleId
                                        permissionsReloadKey++
                                    },
                                    text = {
                                        Text(
                                            text = role.roleName,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                )
                            }
                        }
                    } else {

                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
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
                    else -> { }
                }
            }





            Spacer(modifier = Modifier.height(16.dp))

            if (currentUser != null && selectedRole != null) {
                AuthorizationObjectsList(
                    viewModel = viewModel,
                    user = currentUser,
                    selectedRoleId = selectedRole!!.roleId,
                    reloadKey = permissionsReloadKey,
                    onEdit = { obj, fields ->
                        editObject = obj
                        editFields = fields
                        showAuthDialog = true
                    },
                    onAddRequest = {
                        editObject = null
                        editFields = emptyList()
                        showAuthDialog = true
                    }
                )
            }

        }

        if (showNewUserDialog) {
            NewUserDialog(
                mobile = mobileNumber.trim(),
                roles = roles,
                sharedViewModel = viewModel,
                onDismiss = { showNewUserDialog = false },
                onUserCreated = { user->
                    currentUser = user
                    createdUser = user
                    isFromNewUserDialog = true

                    showNewUserDialog = false
                    showAssignRoleDialog = true
                }

            )
        }
        if (showAuthDialog && currentUser != null && selectedRole != null) {
            AddAuthorizationDialog(
                sharedViewModel = viewModel,
                onDismiss = { showAuthDialog = false },
                onAddComplete = {
                    showAuthDialog = false
                    permissionsReloadKey++
                },
                selectedRoleId = selectedRole!!.roleId,
                editObject = editObject,
                editFields = editFields
            )
        }
//        if (showAuthDialog && currentUser != null) {
//            AddAuthorizationDialog(
//                sharedViewModel = viewModel,
//                onDismiss = { showAuthDialog = false },
//                onAddComplete = { showAuthDialog = false },
//                selectedRoleId = selectedRole!!.roleId,
//                editObject = editObject,
//                editFields = editFields
//            )
//        }

        if (showAssignRoleDialog && currentUser != null) {
            AssignRoleDialog(
                roles = roles,
                currentUser = currentUser!!,
                sharedViewModel = viewModel,
                onDismiss = { showAssignRoleDialog = false },
                user = createdUser,
                onRoleAssigned = { newRole, isNewRole ->
                    scope.launch {
                        val normalized = mobileNumber.trim()
                        isSearching = true
                        try {
                            Users().fetchUserRoleByMobile(
                                context = context,
                                mobileNumber = normalized,
                                onSuccess = { ur ->
                                    userRoleBuilding = ur
                                    currentUser = ur.user
                                    showCreateUserCard = false

                                    if (isNewRole && roles.none { it.roleId == newRole.roleId }) {
                                        roles = roles + newRole
                                    }

                                    selectedRole = newRole
                                    permissionsReloadKey++
                                },
                                onNotFound = {
                                    searchError = context.getString(R.string.user_not_found)
                                },
                                onError = { e ->
                                    searchError = e.message ?: context.getString(R.string.failed)
                                }
                            )
                        } finally {
                            isSearching = false
                            showAssignRoleDialog = false
                        }
                    }
                }


            )
        }

    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignRoleDialog(
    roles: List<Role>,
    currentUser: User,
    sharedViewModel: SharedViewModel,
    onDismiss: () -> Unit,
    onRoleAssigned: (Role, Boolean) -> Unit,
    user: User? = null,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isNewlyCreatedRole by remember { mutableStateOf(false) }

    val baseRoles = remember(roles) {
        roles.filterNot { role ->
            val name = role.roleName.lowercase()
            name == "مالک" ||
                    name == "ساکن" ||
                    name.contains("مهمان" ) ||
                    name.contains("سیستم" )
        }
    }

    var localRoles by remember(baseRoles) { mutableStateOf(baseRoles) }

    var selectedRole by remember { mutableStateOf<Role?>(localRoles.firstOrNull()) }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    var buildings by remember { mutableStateOf<List<BuildingWithCounts>>(emptyList()) }
    var selectedBuilding by remember { mutableStateOf<BuildingWithCounts?>(null) }
    var buildingsLoading by remember { mutableStateOf(false) }
    var buildingsError by remember { mutableStateOf<String?>(null) }

    var showNewRoleSheet by remember { mutableStateOf(false) }
    var newRoleNameTitle by remember { mutableStateOf("") }
    var newRoleError by remember { mutableStateOf<String?>(null) }
    var isCreatingRole by remember { mutableStateOf(false) }

    val dropdownItems = remember(localRoles, context) {
        localRoles + Role(
            roleId = -1L,
            roleName = context.getString(R.string.create_new_role),
            roleDescription = ""
        )
    }

    LaunchedEffect(Unit) {
        buildingsLoading = true
        buildingsError = null
        try {
            val mobile = Preference().getUserMobile(context)?.trim().orEmpty()
            if (mobile.isNotEmpty()) {
                Building().fetchBuildingsForUser(
                    context = context,
                    mobileNumber = mobile,
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
                Spacer(modifier = Modifier.height(8.dp))

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

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = context.getString(R.string.building),
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
                            onItemSelected = {
                                selectedBuilding = it
                            },
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
                                Log.d("newRoleNameTitle", newRoleNameTitle.toString())
                                val createdRole = RoleApi().createRoleSuspend(
                                    context = context,
                                    name = newRoleNameTitle.trim(),
                                    description = ""
                                )
                                Log.d("createdRole", createdRole.toString())
                                localRoles = localRoles + createdRole
                                selectedRole = createdRole
                                isNewlyCreatedRole = true
                                showNewRoleSheet = false
                            } catch (e: Exception) {
                                newRoleError =
                                    e.message ?: context.getString(R.string.failed)
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


@Composable
fun AuthorizationObjectsList(
    viewModel: SharedViewModel,
    user: User?,
    selectedRoleId: Long,
    reloadKey: Int,
    onEdit: (AuthorizationObject, List<AuthorizationDao.FieldWithPermission>) -> Unit,
    onAddRequest: () -> Unit
) {
    val context = LocalContext.current
    var fields by remember { mutableStateOf<List<AuthorizationDao.FieldWithPermission>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    var confirmDeleteObject by remember {
        mutableStateOf<Triple<Long, Long, String>?>(null)
    }
    var confirmDeleteField by remember {
        mutableStateOf<AuthorizationDao.FieldWithPermission?>(null)
    }

    fun applyRoleFilter(list: List<AuthorizationDao.FieldWithPermission>): List<AuthorizationDao.FieldWithPermission> {
        return list.filter { it.crossRef.roleId == selectedRoleId }
    }

    LaunchedEffect(user?.userId, selectedRoleId, reloadKey) {
        if (user == null || selectedRoleId == 0L) {
            fields = emptyList()
            errorText = null
            return@LaunchedEffect
        }
        isLoading = true
        errorText = null
        AuthObjectFieldCross().fetchFieldsWithPermissionsForUser(
            context = context,
            userId = user.userId,
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

    if (user == null || selectedRoleId == 0L) return


    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        errorText != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorText.orEmpty(),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        else -> {
            val groupedFields = remember(fields) { fields.groupBy { it.objectName } }

            if (groupedFields.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = context.getString(R.string.no_permissions_for_role),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                onAddRequest()
                            }
                        ) {
                            Text(
                                text = context.getString(R.string.add_auth),
                                style = MaterialTheme.typography.bodyLarge
                            )
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
                                    val roleId = viewModel.currentRoleId
                                    val objectId = fieldsForObject.firstOrNull()?.field?.objectId ?: 0L
                                    if (roleId != 0L && objectId != 0L) {
                                        confirmDeleteObject = Triple(roleId, objectId, objectName)
                                    }
                                },
                                onDeleteField = { fieldWithPermission ->
                                    confirmDeleteField = fieldWithPermission
                                }
                            )
                        }
                    }
                }
            }

        }
    }

    // Confirm dialog for deleting whole object
    if (confirmDeleteObject != null && user != null) {
        val (roleId, objectId, objectName) = confirmDeleteObject!!
        AlertDialog(
            onDismissRequest = { confirmDeleteObject = null },
            title = {
                Text(
                    text = context.getString(R.string.are_you_sure),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            text = {
                Text(
                    text = context.getString(
                        R.string.confirm_delete_object_message
                    ),
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
                                    roleId = roleId,
                                    objectId = objectId
                                )
                                AuthObjectFieldCross().fetchFieldsWithPermissionsForUser(
                                    context = context,
                                    userId = user.userId,
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
                ) {
                    Text(
                        text = context.getString(R.string.delete),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { confirmDeleteObject = null }) {
                    Text(
                        text = context.getString(R.string.cancel),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        )
    }

    // Confirm dialog for deleting single field
    if (confirmDeleteField != null && user != null) {
        val f = confirmDeleteField!!
        AlertDialog(
            onDismissRequest = { confirmDeleteField = null },
            title = {
                Text(
                    text = context.getString(R.string.are_you_sure),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            text = {
                Text(
                    text = context.getString(
                        R.string.confirm_delete_field_message
                    ),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.viewModelScope.launch {
                            try {
                                AuthObjectFieldCross().deleteSingleFieldSuspend(
                                    context = context,
                                    roleId = f.crossRef.roleId,
                                    objectId = f.crossRef.objectId,
                                    fieldId = f.crossRef.fieldId
                                )
                                AuthObjectFieldCross().fetchFieldsWithPermissionsForUser(
                                    context = context,
                                    userId = user.userId,
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
                ) {
                    Text(
                        text = context.getString(R.string.delete),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { confirmDeleteField = null }) {
                    Text(
                        text = context.getString(R.string.cancel),
                        style = MaterialTheme.typography.bodyLarge
                    )
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
    onDeleteField: (AuthorizationDao.FieldWithPermission) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

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
//                IconButton(onClick = onEdit) {
//                    Icon(Icons.Default.Edit, contentDescription = "Edit")
//                }
//                IconButton(onClick = onDeleteObject) {
//                    Icon(Icons.Default.Delete, contentDescription = "Delete Object")
//                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    fields.forEach { field ->
                        FieldPermissionRow(
                            field = field,
                            onDeleteField = { onDeleteField(field) }
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
    onDeleteField: () -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = field.field.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = context.getString(field.crossRef.permissionLevel.labelRes),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.width(120.dp),
            textAlign = TextAlign.Center
        )
//        IconButton(onClick = onDeleteField) {
//            Icon(Icons.Default.Delete, contentDescription = "Delete Field")
//        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAuthorizationDialog(
    sharedViewModel: SharedViewModel,
    onDismiss: () -> Unit,
    onAddComplete: () -> Unit,
    selectedRoleId: Long,
    editObject: AuthorizationObject? = null,
    editFields: List<AuthorizationDao.FieldWithPermission> = emptyList()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var allAuthObjects by remember { mutableStateOf<List<AuthorizationObject>>(emptyList()) }
    var fields by remember { mutableStateOf<List<AuthorizationField>>(emptyList()) }

    var isLoadingObjects by remember { mutableStateOf(false) }
    var isLoadingFields by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    var selectedObject by remember { mutableStateOf(editObject) }
    var selectedFields by remember { mutableStateOf(editFields.map { it.field }.toSet()) }
    var selectedPermissions by remember {
        mutableStateOf(
            editFields.associate { it.field.fieldId to it.crossRef.permissionLevel }
        )
    }

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

    LaunchedEffect(selectedObject?.objectId) {
        val objId = selectedObject?.objectId ?: return@LaunchedEffect
        isLoadingFields = true
        errorText = null
        try {
            fields = AuthorizationObj().fetchFieldsForObjectSuspend(context, objId)
        } catch (e: Exception) {
            errorText = e.message
            fields = emptyList()
        } finally {
            isLoadingFields = false
        }
    }

    AlertDialog(
        onDismissRequest = {  },
        title = {
            Text(
                if (editObject == null)
                    context.getString(R.string.add_auth)
                else
                    context.getString(R.string.edit_auth),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        text = {
            Column {
                if (isLoadingObjects) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
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
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 250.dp)) {
                        items(fields) { field ->
                            val checked = selectedFields.contains(field)
                            val permission = selectedPermissions[field.fieldId] ?: PermissionLevel.READ
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedFields = if (checked) {
                                            selectedFields - field
                                        } else {
                                            selectedFields + field
                                        }
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = null
                                )
                                Text(
                                    text = field.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 8.dp)
                                )
                                ExposedDropdownMenuBoxExample(
                                    sharedViewModel = sharedViewModel,
                                    items = PermissionLevel.entries.toList(),
                                    selectedItem = permission,
                                    onItemSelected = { newPerm ->
                                        selectedPermissions =
                                            selectedPermissions.toMutableMap().apply {
                                                put(field.fieldId, newPerm)
                                            }
                                    },
                                    label = "",
                                    itemLabel = { context.getString(it.labelRes) },
                                    modifier = Modifier.width(120.dp)
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
                onClick = {
                    val obj = selectedObject ?: return@Button
                    if (selectedFields.isEmpty()) return@Button

                    errorText = null
                    scope.launch {
                        isSubmitting = true
                        try {
                            val items = selectedFields.map { field ->
                                val perm = selectedPermissions[field.fieldId] ?: PermissionLevel.READ
                                RoleAuthorizationObjectFieldCrossRef(
                                    roleId = selectedRoleId,
                                    objectId = obj.objectId,
                                    fieldId = field.fieldId,
                                    permissionLevel = perm
                                )
                            }
                            if (editObject != null) {
                                AuthObjectFieldCross().deleteObjectForRoleSuspend(
                                    context = context,
                                    roleId = selectedRoleId,
                                    objectId = obj.objectId
                                )
                            }
                            AuthObjectFieldCross().insertRoleAuthListSuspend(
                                context = context,
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
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        if (editObject == null)
                            context.getString(R.string.insert)
                        else
                            context.getString(R.string.edit),
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
                Text(text = context.getString(R.string.cancel),
                    style = MaterialTheme.typography.bodyLarge)
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
                    onValueChange = { email = it },
                    label = {
                        Text(
                            text = context.getString(R.string.email),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = {
                        Text(
                            text = context.getString(R.string.phone_number),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
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
                enabled = !creatingUser && firstName.isNotBlank() && lastName.isNotBlank()
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


@Composable
fun RolePermissionsList(fields: List<AuthorizationDao.FieldWithPermission>) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        fields.forEach { f ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = f.field.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = context.getString(f.crossRef.permissionLevel.labelRes),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

