package com.example.delta

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.core.content.edit
import com.example.delta.data.dao.AuthorizationDao
import com.example.delta.data.dao.RoleDao
import com.example.delta.data.entity.AuthorizationField
import com.example.delta.data.entity.RoleAuthorizationObjectFieldCrossRef
import com.example.delta.data.model.AppDatabase
import com.example.delta.enums.AuthObject
import com.example.delta.enums.BuildingProfileFields
import com.example.delta.enums.PermissionLevel
import com.example.delta.enums.Roles
import com.example.delta.init.FileManagement
import com.example.delta.viewmodel.SharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

private lateinit var roleDao: RoleDao // Initialize these DAOs as needed
private lateinit var authorizationDao: AuthorizationDao

class GuestActivity : ComponentActivity() {
    val sharedViewModel: SharedViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = AppDatabase.getDatabase(this)

        roleDao = database.roleDao() // Initialize Role DAO
        authorizationDao = database.authorizationDao() // Initialize Role DAO
        setContent {
            AppTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val context = LocalContext.current
                    Scaffold(
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Text(
                                        text = getString(R.string.guest_display),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                navigationIcon = {
                                    IconButton(onClick = { startActivity(Intent(context, LoginPage::class.java)) }) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back"
                                        )
                                    }
                                }
                            )
                        }
                    ) { innerPadding ->
                        GuestScreen(modifier = Modifier.padding(innerPadding), sharedViewModel)
                    }
                }
            }
        }
    }
}

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestScreen(modifier: Modifier, sharedViewModel: SharedViewModel) {
    val context = LocalContext.current

    var showRoleDialog by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf<Roles?>(null) }
    var confirmedRole by remember { mutableStateOf<Roles?>(null) }

    val roles = listOf(
        Roles.GUEST_BUILDING_MANAGER,
        Roles.GUEST_PROPERTY_OWNER,
        Roles.GUEST_PROPERTY_TENANT,
        Roles.GUEST_INDEPENDENT_USER
    )

    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(confirmedRole) {
        confirmedRole?.let { role ->

            try {
                isLoading = true
                insertUserBasedOnRole(confirmedRole, context)
                insertingSampleBuilding(context, sharedViewModel)
            } catch (e: Exception) {
//                Toast.makeText(context, context.getString(R.string.failed_opening), Toast.LENGTH_SHORT).show()
                Log.e("GuestScreen", "Error inserting sample building: $e")
            } finally {
                isLoading = false
            }
            confirmedRole = null
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = context.getString(R.string.guest_entrance),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.banner_first),
                contentDescription = "Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = context.getString(R.string.features),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FeatureItem(
                    icon = Icons.Default.Notifications,
                    text = context.getString(R.string.title_notifications),
                    onClick = {}
                )
                FeatureItem(
                    icon = Icons.Default.AttachMoney,
                    text = context.getString(R.string.cost_managing),
                    onClick = {}
                )
                FeatureItem(
                    icon = Icons.Default.BarChart,
                    text = context.getString(R.string.reporting),
                    onClick = {}
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = context.getString(R.string.guest_message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.weight(1f))
            Column(
                modifier = Modifier.fillMaxWidth(0.7f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                if (isLoading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Dialog(onDismissRequest = {}) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(Color.White, shape = RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { showRoleDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = context.getString(R.string.login_as_guest),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        if (showRoleDialog) {
            RoleSelectionBottomSheet(
                roles = roles,
                initialSelectedRole = selectedRole,
                onDismissRequest = { showRoleDialog = false },
                onConfirm = { confirmedRoleSelected ->
                    confirmedRole = confirmedRoleSelected
                    showRoleDialog = false
                }
            )
        }

    }
}

private fun insertUserBasedOnRole(role: Roles?, context: Context) {
    when (role) {
        Roles.GUEST_BUILDING_MANAGER -> {
            // Save login state etc.
            saveLoginState(context, true, userId = 1, mobile = "01111111111")
            saveFirstLoginState(context = context, isFirstLoggedIn = true)
        }

        Roles.GUEST_PROPERTY_OWNER -> {
            saveLoginState(context, true, userId = 2, mobile = "0222222222")
            saveFirstLoginState(context = context, isFirstLoggedIn = true)
        }

        Roles.GUEST_PROPERTY_TENANT -> {
            saveLoginState(context, true, userId = 3, mobile = "03333333333")
            saveFirstLoginState(context = context, isFirstLoggedIn = true)
        }

        Roles.GUEST_INDEPENDENT_USER -> {
            saveLoginState(context, true, userId = 4, mobile = "04444444444")
            saveFirstLoginState(context = context, isFirstLoggedIn = true)
        }

        else -> {
            // No action or error handling
        }
    }
}

@Composable
fun FeatureItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSelectionBottomSheet(
    roles: List<Roles>,
    initialSelectedRole: Roles?,
    onDismissRequest: () -> Unit,
    onConfirm: (Roles) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedRole by remember { mutableStateOf(initialSelectedRole) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = context.getString(R.string.select_role_for_different_feature), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            ExposedDropdownMenuBoxExample(
                items = roles,
                selectedItem = selectedRole,
                onItemSelected = {
                    selectedRole = it
                },
                label = context.getString(R.string.select_role),
                modifier = Modifier
                    .fillMaxWidth(1f),
                itemLabel = { it.getDisplayName(context) }
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Buttons row: Cancel and Confirm
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = {
                    coroutineScope.launch {
                        sheetState.hide()
                        onDismissRequest()
                    }
                }) {
                    Text(text = context.getString(R.string.cancel), style = MaterialTheme.typography.bodyLarge)
                }

                Spacer(modifier = Modifier.width(8.dp))

                TextButton(
                    onClick = {
                        selectedRole?.let {
                            coroutineScope.launch {
                                sheetState.hide()
                                onConfirm(it)
                            }
                        }
                    },
                    enabled = selectedRole != null
                ) {
                    Text(text = context.getString(R.string.confirm), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}


suspend fun insertingSampleBuilding(context: Context, sharedViewModel: SharedViewModel) {
    val prefs = context.getSharedPreferences("guest_prefs", Context.MODE_PRIVATE)
    val alreadyInserted = prefs.getBoolean("excel_inserted_guest", false)

    if (!alreadyInserted) {
        try {
            val file = File(context.cacheDir, "export_delta_template_guest.xlsx")

            withContext(Dispatchers.IO) {
                context.resources.openRawResource(R.raw.export_delta_template_guest).use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            context.contentResolver.openInputStream(fileUri)?.use { inputStream ->
                withContext(Dispatchers.IO) {

                    FileManagement().handleExcelFile(
                        inputStream,
                        context as Activity,
                        sharedViewModel
                    )
                }
            }
            CoroutineScope(Dispatchers.Default).launch {
                insertDefaultAuthorizationData()

                prefs.edit {
                    putBoolean("excel_inserted_guest", true)
                }
            }

        } catch (e: Exception) {
            Log.e("GuestScreen1", "Error inserting sample building: $e")
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    context.getString(R.string.failed_opening),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

private suspend fun insertDefaultAuthorizationData() {
    withContext(Dispatchers.IO) {
        val rolesList = roleDao.getRoles()
        val roleMap = rolesList.associateBy { it.roleName }

        suspend fun insertCrossRefs(
            roleId: Long,
            objectId: Long,
            fields: List<AuthorizationField>,
            permission: PermissionLevel
        ) {
            fields.forEach { field ->
                Log.d("field", field.toString())
                Log.d("roleId", roleId.toString())
                authorizationDao.insertRoleAuthorizationFieldCrossRef(
                    RoleAuthorizationObjectFieldCrossRef(
                        roleId = roleId,
                        objectId = objectId,
                        fieldId = field.fieldId,
                        permissionLevel = permission.value
                    )
                )
            }
        }

        val adminManagerRoles = listOf(Roles.GUEST_BUILDING_MANAGER)
        adminManagerRoles.forEach { roleName ->
            val role = roleMap[roleName] ?: return@forEach
            AuthObject.getAll().forEach { authObject ->
                val fields = authorizationDao.getFieldsForObject(authObject.id)
                insertCrossRefs(role.roleId, authObject.id, fields, PermissionLevel.FULL)
            }
        }

        roleMap[Roles.GUEST_PROPERTY_TENANT]?.let { tenantRole ->
            val tenantFieldNames = listOf(
                BuildingProfileFields.UNITS_TAB.fieldNameRes,
                BuildingProfileFields.USERS_OWNERS.fieldNameRes,
                BuildingProfileFields.USERS_TENANTS.fieldNameRes,
                BuildingProfileFields.TENANTS_TAB.fieldNameRes
            )
            val allFields = authorizationDao.getFieldsForObject(3L)
            val tenantFields = allFields.filter { it.name in tenantFieldNames }
            insertCrossRefs(tenantRole.roleId, 3L, tenantFields, PermissionLevel.FULL)
        }

        roleMap[Roles.GUEST_PROPERTY_OWNER]?.let { ownerRole ->
            val ownerFieldNames = listOf(
                BuildingProfileFields.UNITS_TAB.fieldNameRes,
                BuildingProfileFields.USERS_OWNERS.fieldNameRes,
                BuildingProfileFields.USERS_TENANTS.fieldNameRes,
                BuildingProfileFields.TENANTS_TAB.fieldNameRes,
                BuildingProfileFields.OWNERS_TAB.fieldNameRes
            )
            val allFields = authorizationDao.getFieldsForObject(3L)
            val ownerFields = allFields.filter { it.name in ownerFieldNames }
            insertCrossRefs(ownerRole.roleId, 3L, ownerFields, PermissionLevel.FULL)
        }
    }
}

