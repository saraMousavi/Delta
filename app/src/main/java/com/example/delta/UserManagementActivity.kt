package com.example.delta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.delta.data.dao.AuthorizationDao
import com.example.delta.data.entity.AuthorizationObject
import com.example.delta.data.entity.Role
import com.example.delta.data.entity.User
import com.example.delta.enums.PermissionLevel
import com.example.delta.viewmodel.SharedViewModel
import kotlinx.coroutines.launch

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
    val context = LocalContext.current

    val mobileNumberState = remember { mutableStateOf(TextFieldValue()) }
    val mobileNumber = mobileNumberState.value.text

    val userWithRole by viewModel.getUserWithRoleByMobile(mobileNumber).collectAsState(initial = null)
    val roles by viewModel.getRoles().collectAsState(initial = emptyList())
    var selectedRole by remember { mutableStateOf<Role?>(null) }
    val userByMobile by viewModel.getUserByMobile(mobileNumber).collectAsState(initial = null)

    var showAuthDialog by remember { mutableStateOf(false) }
    var editObject by remember { mutableStateOf<AuthorizationObject?>(null) }
    var editFields by remember { mutableStateOf<List<AuthorizationDao.FieldWithPermission>>(emptyList()) }
    val selectedRoleId = selectedRole?.roleId ?: 0L

    LaunchedEffect(userWithRole) {
        userWithRole?.let {
            selectedRole = it
            viewModel.currentRoleId = it.roleId
        } ?: run {
            selectedRole = null
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
            ExtendedFloatingActionButton(
                onClick = {
                    editObject = null
                    editFields = emptyList()
                    showAuthDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                text = { Text(text = context.getString(R.string.add_auth), style = MaterialTheme.typography.bodyLarge) }
            )

            if (showAuthDialog) {
                AddAuthorizationDialog(
                    onDismiss = { showAuthDialog = false },
                    onAddComplete = { showAuthDialog = false },
                    viewModel = viewModel,
                    selectedRoleId = selectedRoleId,
                    editObject = editObject,
                    editFields = editFields
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
            OutlinedTextField(
                value = mobileNumberState.value,
                onValueChange = { mobileNumberState.value = it },
                label = { Text(text = context.getString(R.string.mobile_number), style = MaterialTheme.typography.bodyLarge) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBoxExample(
                items = roles,
                selectedItem = selectedRole,
                onItemSelected = { selectedRole = it },
                label = context.getString(R.string.role),
                itemLabel = { it.roleName },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            AuthorizationObjectsList(
                viewModel = viewModel,
                user = userByMobile,
                onEdit = { obj, fields ->
                    editObject = obj
                    editFields = fields
                    showAuthDialog = true
                }
            )
        }
    }
}

@Composable
fun AuthorizationObjectsList(
    viewModel: SharedViewModel,
    user: User?,
    onEdit: (AuthorizationObject, List<AuthorizationDao.FieldWithPermission>) -> Unit
) {
    if (user != null) {
        val fields by viewModel.getAuthorizationDetailsForUser(user.userId).collectAsStateWithLifecycle(initialValue = emptyList())
        val groupedFields = remember(fields) { fields.groupBy { it.objectName } }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            groupedFields.forEach { (objectNameResId, fields) ->
                item {
                    ExpandableAuthObjectCard(
                        objectName = LocalContext.current.getString(objectNameResId),
                        fields = fields,
                        onEdit = {
                            onEdit(fields.firstOrNull()?.field?.let {
                                AuthorizationObject(it.objectId, objectNameResId, 0)
                            } ?: AuthorizationObject(0, objectNameResId, 0), fields)
                        },
                        onDeleteObject = {
                            val roleId = viewModel.currentRoleId
                            val objectId = fields.firstOrNull()?.field?.objectId ?: 0L
                            viewModel.deleteObjectAuthorization(roleId, objectId)
                        },
                        onDeleteField = { fieldWithPermission ->
                            viewModel.deleteFieldAuthorization(fieldWithPermission.crossRef)
                        }
                    )
                }
            }
        }
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
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDeleteObject) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Object")
                }
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
            text = context.getString(field.field.name),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = context.getString(field.crossRef.permissionLevelEnum.labelRes),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(120.dp),
            textAlign = TextAlign.Center
        )
        IconButton(onClick = onDeleteField) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Field")
        }
    }
}


@Composable
fun AddAuthorizationDialog(
    onDismiss: () -> Unit,
    onAddComplete: () -> Unit,
    viewModel: SharedViewModel,
    selectedRoleId: Long,
    editObject: AuthorizationObject? = null,
    editFields: List<AuthorizationDao.FieldWithPermission> = emptyList()
) {
    val context = LocalContext.current

    var selectedObject by remember { mutableStateOf(editObject) }
    val allAuthObjects by viewModel.getAllAuthorizationObjects().collectAsState(initial = emptyList())
    val fields by viewModel.getFieldsForObject(selectedObject?.objectId ?: 0L).collectAsState(initial = emptyList())

    var selectedFields by remember { mutableStateOf(editFields.map { it.field }.toSet()) }
    var selectedPermissions by remember {
        mutableStateOf(editFields.associate { it.field.fieldId to it.crossRef.permissionLevelEnum })
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (editObject == null) context.getString(R.string.add_auth) else context.getString(R.string.edit_auth))
        },
        text = {
            Column {
                ExposedDropdownMenuBoxExample(
                    items = allAuthObjects,
                    selectedItem = selectedObject,
                    onItemSelected = {
                        selectedObject = it
                        selectedFields = emptySet()
                        selectedPermissions = emptyMap()
                    },
                    label = context.getString(R.string.auth_object),
                    itemLabel = { context.getString(it.name) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 250.dp)) {
                    items(fields) { field ->
                        val permission = selectedPermissions[field.fieldId] ?: PermissionLevel.READ
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedFields = if (field in selectedFields) {
                                        selectedFields - field
                                    } else {
                                        selectedFields + field
                                    }
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = field in selectedFields,
                                onCheckedChange = null
                            )
                            Text(
                                text = context.getString(field.name),
                                modifier = Modifier.weight(1f).padding(start = 8.dp)
                            )
                            ExposedDropdownMenuBoxExample(
                                items = PermissionLevel.entries,
                                selectedItem = permission,
                                onItemSelected = { newPerm ->
                                    selectedPermissions = selectedPermissions.toMutableMap().apply {
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
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedObject?.let { obj ->
                        viewModel.viewModelScope.launch {
                            selectedFields.forEach { field ->
                                val perm = selectedPermissions[field.fieldId] ?: PermissionLevel.READ
                                viewModel.insertRoleAuthorizationFieldCrossRef(
                                    roleId = selectedRoleId,
                                    objectId = obj.objectId,
                                    fields = listOf(field),
                                    permissionLevel = perm.value
                                )
                            }
                            onAddComplete()
                        }
                    }
                },
                enabled = selectedObject != null && selectedFields.isNotEmpty()
            ) {
                Text(if (editObject == null) context.getString(R.string.insert) else context.getString(R.string.edit))
            }
        }
    )
}
