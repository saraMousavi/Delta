package com.example.delta

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import kotlinx.coroutines.flow.flowOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.delta.data.entity.AuthorizationField
import com.example.delta.data.entity.AuthorizationObject
import com.example.delta.data.entity.User
import com.example.delta.enums.PermissionLevel
import com.example.delta.viewmodel.SharedViewModel

@OptIn(ExperimentalMaterial3Api::class)
class UserManagementActivity : ComponentActivity() {
    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
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
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val tabTitles = listOf("User Info", "Authorization Objects")
    var selectedTab by remember { mutableIntStateOf(0) }
    var expanded by remember { mutableStateOf(false) }

    // User input states
    val mobileNumberState = remember { mutableStateOf(TextFieldValue()) }
    var selectedRole = remember { mutableStateOf("Select Role") }
    val roles by viewModel.getRoles().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = LocalContext.current.getString(R.string.user_management),
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
            if (selectedTab == 1) {
                var showAddAuthDialog by remember { mutableStateOf(false) }

                ExtendedFloatingActionButton(
                    onClick = { showAddAuthDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                    text = { Text("Add Authorization") }
                )

                if (showAddAuthDialog) {
                    AddAuthorizationDialog(
                        onDismiss = { showAddAuthDialog = false },
                        onAdd = { objectSelected, fieldSelected, permissionSelected ->
                            coroutineScope.launch {
                                viewModel.insertRoleAuthorizationObjectCrossRef(
                                    roleId = viewModel.currentRoleId,
                                    objectId = objectSelected.objectId,
                                    permissionLevel = permissionSelected.value
                                )
                                // Optionally handle fieldSelected if you have field-level cross refs
                            }
                            showAddAuthDialog = false
                        }
                    )
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
            OutlinedTextField(
                value = mobileNumberState.value,
                onValueChange = {
                    mobileNumberState.value = it
//                    coroutineScope.launch {
//                        val userRoleName = viewModel.getUserRole(it.text)
//                        val foundRole = roles.find { role -> role.roleName == userRoleName }
//                        if (foundRole != null) selectedRole = foundRole  // <-- fix here: was `c`
//                    }
                },
                label = { Text("Mobile Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )


            Spacer(modifier = Modifier.height(16.dp))

            // Role Selection Dropdown using ExposedDropdownMenuBoxExample
            OutlinedTextField(
                value = mobileNumberState.value,
                onValueChange = {
                    mobileNumberState.value = it
//                    coroutineScope.launch {
//                        val userRoleName = viewModel.getUserRole(it.text)
//                        val foundRole = roles.find { role -> role.roleName == userRoleName }
//                        if (foundRole != null) selectedRole = foundRole
//                    }
                },
                label = { Text("Mobile Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))


            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        height = 4.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> UserInfoTab(viewModel)
                1 -> AuthorizationObjectsTab(viewModel)
            }
        }
    }
}

@Composable
fun UserInfoTab(viewModel: SharedViewModel) {
    val users by viewModel.getUsers().collectAsState(initial = emptyList())

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            items(users) { user ->
                UserDetailCard(user)
            }
        }
    }
}

@Composable
fun UserDetailCard(user: User) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("User Details", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            DetailRow("Mobile", user.mobileNumber)
            DetailRow("RoleId", user.roleId.toString())
            // Add more fields as needed
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun AuthorizationObjectsTab(viewModel: SharedViewModel) {
    val authObjects by viewModel.authObjects.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(authObjects) { authObject ->
            ExpandableAuthObjectCard(authObject, viewModel)
        }
    }
}

@Composable
fun ExpandableAuthObjectCard(authObject: AuthorizationObject, viewModel: SharedViewModel) {
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = LocalContext.current.getString(authObject.name),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
//                    authObject.fields.forEach { field ->
//                        FieldRow(field, viewModel)
//                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAuthorizationDialog(
    onDismiss: () -> Unit,
    onAdd: (AuthorizationObject, AuthorizationField, PermissionLevel) -> Unit,
    viewModel: SharedViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val authObjects by viewModel.getAllAuthorizationObjects().collectAsState(initial = emptyList())
    var selectedAuthObject by remember { mutableStateOf<AuthorizationObject?>(null) }
    val fieldsFlow = remember(selectedAuthObject) {
        selectedAuthObject?.let { viewModel.getFieldsForAuthorizationObject(it.objectId) } ?: flowOf(emptyList())
    }
    val fieldsForSelectedObject by fieldsFlow.collectAsState(initial = emptyList())


    var selectedField by remember { mutableStateOf<AuthorizationField?>(null) }

    val permissionLevels = PermissionLevel.values().toList()
    var selectedPermission by remember { mutableStateOf<PermissionLevel?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedAuthObject != null && selectedField != null && selectedPermission != null) {
                        onAdd(selectedAuthObject!!, selectedField!!, selectedPermission!!)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Add Authorization") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                ExposedDropdownMenuBoxExample(
                    items = authObjects,
                    selectedItem = selectedAuthObject,
                    onItemSelected = {
                        selectedAuthObject = it
                        selectedField = null // Reset field selection on new object
                    },
                    label = "Authorization Object",
                    itemLabel = { context.getString(it.name) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                ExposedDropdownMenuBoxExample(
                    items = fieldsForSelectedObject,
                    selectedItem = selectedField,
                    onItemSelected = { selectedField = it },
                    label = "Authorization Field",
                    itemLabel = { context.getString(it.name)},
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(16.dp))

                ExposedDropdownMenuBoxExample(
                    items = permissionLevels,
                    selectedItem = selectedPermission,
                    onItemSelected = { selectedPermission = it },
                    label = "Permission Level",
                    itemLabel = { it.name },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}


@Composable
fun FieldRow(field: AuthorizationField, viewModel: SharedViewModel) {
    var selectedPermission by remember { mutableStateOf(field.fieldType) } // Or use permissionLevel if you have it

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(LocalContext.current.getString(field.name), modifier = Modifier.weight(1f))

        // You can add a dropdown for permission selection here if needed
//        OutlinedTextField(
//            value = selectedPermission.toString(),
//            onValueChange = { selectedPermission = it },
//            readOnly = true,
//            modifier = Modifier.width(120.dp),
//            colors = Color(LocalContext.current.getColor(R.color.grey))
//        )
    }
}
