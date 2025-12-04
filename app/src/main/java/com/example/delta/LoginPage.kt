package com.example.delta

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.example.delta.data.dao.AuthorizationDao
import com.example.delta.data.dao.RoleDao
import com.example.delta.data.entity.AuthorizationField
import com.example.delta.data.entity.Role
import com.example.delta.data.entity.RoleAuthorizationObjectFieldCrossRef
import com.example.delta.data.entity.User
import com.example.delta.data.model.AppDatabase
import com.example.delta.enums.AuthObject
import com.example.delta.enums.BuildingProfileFields
import com.example.delta.enums.PermissionLevel
import com.example.delta.enums.Roles
import com.example.delta.init.FileManagement
import com.example.delta.init.Preference
import com.example.delta.init.Validation
import com.example.delta.screens.OnboardingScreenWithModalSheet
import com.example.delta.screens.OtpScreen
import com.example.delta.viewmodel.BuildingsViewModel
import com.example.delta.viewmodel.SharedViewModel
import com.example.delta.volley.Cost
import com.example.delta.volley.TokenUploader
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private lateinit var roleDao: RoleDao
private lateinit var authorizationDao: AuthorizationDao

class LoginPage : ComponentActivity() {
    private val viewModel: BuildingsViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by viewModels()
    private val REQUEST_CODE_PICK_EXCEL = 1001

    @Deprecated(
        "Use Activity Result API instead."
    )
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_EXCEL && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            try {
                contentResolver.openInputStream(uri)?.use { input ->
                    FileManagement().handleExcelFile(input, this, this)
                } ?: Toast.makeText(this, getString(R.string.failed), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "خطا: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)
        roleDao = database.roleDao()
        authorizationDao = database.authorizationDao()

        setContent {
            AppTheme(useDarkTheme = sharedViewModel.isDarkModeEnabled) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val initialIsLoggedIn = isUserLoggedIn(this@LoginPage)
                    val initialIsFirstLogin = isFirstLoggedIn(this@LoginPage)

                    var isLoggedIn by remember { mutableStateOf(initialIsLoggedIn) }
                    var showOnboardingModal by remember { mutableStateOf(initialIsLoggedIn && initialIsFirstLogin) }
                    var navigateToIntent by remember { mutableStateOf<Intent?>(null) }
                    val context = LocalContext.current

                    if (isLoggedIn) {
                        val userId = Preference().getUserId(context = this@LoginPage)
                        val userRole = Preference().getRoleId(context = this@LoginPage)

                        if (showOnboardingModal) {
                            OnboardingScreenWithModalSheet(
                                onManualEntry = {
                                    saveFirstLoginState(this@LoginPage, false)
                                    val intent = Intent(this@LoginPage, BuildingFormActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    }
                                    startActivity(intent)
                                    finish()
                                },
                                onImportExcel = {
                                    saveFirstLoginState(this@LoginPage, false)
                                    val selectFileIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                                        type = "*/*"
                                        addCategory(Intent.CATEGORY_OPENABLE)
                                    }
                                    startActivityForResult(selectFileIntent, REQUEST_CODE_PICK_EXCEL)
                                },
                                roleId = userRole,
                                onFinish = {
                                    saveFirstLoginState(this@LoginPage, false)
                                    val userId = Preference().getUserId(context = this@LoginPage)

                                    navigateAfterLoginWithCostsCheck(
                                        context = this@LoginPage,
                                        roleId = userRole,
                                        userId = userId
                                    )
                                }
                            )
                        } else {
                            LaunchedEffect(userRole) {
                                val userId = Preference().getUserId(context = context)
                                navigateAfterLoginWithCostsCheck(
                                    context = context,
                                    roleId = userRole,
                                    userId = userId
                                )
                            }

                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }

                    } else {
                        val selectedTab = remember { mutableIntStateOf(0) }
                        when (selectedTab.intValue) {
                            0 -> LoginFormScreen(
                                buildingsViewModel = viewModel,
                                sharedViewModel = sharedViewModel,
                                onTabChange = { selectedTab.intValue = it }
                            )
                            1 -> SignUpScreen(
                                sharedViewModel = sharedViewModel,
                                onSignUpSuccess = { user ->
                                    saveLoginState(
                                        this@LoginPage,
                                        true,
                                        user.userId,
                                        user.mobileNumber,
                                        roleId = 1
                                    )
                                    saveFirstLoginState(this@LoginPage, true)
                                    isLoggedIn = true
                                    showOnboardingModal = true
                                },
                                onShowBoarding = {
                                    isLoggedIn = true
                                    showOnboardingModal = true
                                },
                                onTabChange = { selectedTab.intValue = it }
                            )

                        }
                    }

                    navigateToIntent?.let { intent ->
                        LaunchedEffect(intent) {
                            context.startActivity(intent)
                            if (context is Activity) context.finish()
                            navigateToIntent = null
                        }
                    }
            }
        }
    }
}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginFormScreen(
    sharedViewModel: SharedViewModel,
    buildingsViewModel: BuildingsViewModel,
    onTabChange: (Int) -> Unit
) {
    val context = LocalContext.current

    val tabTitles = listOf(context.getString(R.string.login), context.getString(R.string.sign_up))
    var selectedTab by rememberSaveable { mutableStateOf(0) }

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var userId by remember { mutableLongStateOf(0L) }
    var roleDialogVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    var serverRoles by remember { mutableStateOf<List<Role>>(emptyList()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        TabRow(selectedTabIndex = selectedTab) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = {
                        selectedTab = index
                        onTabChange(index)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    text = { Text(text = title, style = MaterialTheme.typography.bodyLarge) }
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text(text = context.getString(R.string.mobile_number), style = MaterialTheme.typography.bodyLarge) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text(text = context.getString(R.string.password), style = MaterialTheme.typography.bodyLarge) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        if (errorText != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = errorText!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(Modifier.height(4.dp))

            TextButton(
                onClick = {
                    selectedTab = 1
                    onTabChange(1)
                }
            ) {
                Text(
                    text = context.getString(R.string.go_to_sign_up),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }


        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                if (username.isBlank() || password.isBlank()) return@Button
                errorText = null
                isLoading = true

                com.example.delta.volley.Users().login(
                    context = context,
                    mobileNumber = username.trim(),
                    password = password,
                    onSuccess = { result ->
                        serverRoles = result.roles
                        userId = result.user.userId
                        Log.d("serverRoles", serverRoles.toString())
                        isLoading = false
                        roleDialogVisible = true
                    },
                    onInvalid = {
                        isLoading = false
                        errorText = context.getString(R.string.login_user_not_found)
                    },
                    onError = { t ->
                        isLoading = false
                        errorText = t.message ?: context.getString(R.string.login_user_not_found)
                    }
                )

            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(
                    text = if (selectedTab == 0) context.getString(R.string.login) else context.getString(R.string.sign_up),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }

    if (roleDialogVisible) {
            RolePickerDialog(
                context = context,
                roles = serverRoles,
                onDismiss = { roleDialogVisible = false },
                onConfirm = { chosenRole ->
                    // Save role properly
                    saveLoginState(
                        context,
                        true,
                        userId,
                        mobile = username,
                        roleId = chosenRole.roleId
                    )
                    navigateAfterLoginWithCostsCheck(
                        context = context,
                        roleId = chosenRole.roleId,
                        userId = userId
                    )

                    // Upload token
                    FirebaseMessaging.getInstance().token
                        .addOnSuccessListener { token ->
                            com.example.delta.volley.Users().fetchUserByMobile(
                                context = context,
                                mobileNumber = username.trim(),
                                onSuccess = { user ->
                                    if (user != null) {
                                        TokenUploader.uploadFcmToken(
                                            context = context,
                                            user = user,
                                            fcmToken = token
                                        )
                                    }
                                },
                                onNotFound = {},
                                onError = {}
                            )
                        }

                    roleDialogVisible = false
                }
            )

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    sharedViewModel: SharedViewModel,
    onSignUpSuccess: (User) -> Unit,
    onShowBoarding: () -> Unit,
    onTabChange: (Int) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var mobile by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mobileError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var showOtp by remember { mutableStateOf(false) }
    var pendingUser by remember { mutableStateOf<User?>(null) }


    if (showOtp) {
        OtpScreen(
            phone = pendingUser?.mobileNumber.orEmpty(),
            onVerified = {
                val user = pendingUser ?: return@OtpScreen
                scope.launch(Dispatchers.IO) {
                    sharedViewModel.insertUser(
                        context,
                        user,
                        onSuccess = { userId ->
                            user.userId = userId
                            FirebaseMessaging.getInstance().token
                                .addOnSuccessListener { token ->
                                    TokenUploader.uploadFcmToken(
                                        context = context,
                                        user = user,
                                        fcmToken = token,
                                        onSuccess = {},
                                        onError = { e ->
                                            Log.e("FCM", "Failed to upload token", e)
                                        }
                                    )
                                }
                            saveLoginState(context, true, user.userId, user.mobileNumber, roleId = 3)
                            saveFirstLoginState(context, true)

                            scope.launch(Dispatchers.Main) {
                                onSignUpSuccess(user)
                                showOtp = false
                                onShowBoarding()
                            }
                        },
                        onError = {
                            scope.launch(Dispatchers.Main) {
                                Toast.makeText(context, context.getString(R.string.failed), Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            },
            onBack = { showOtp = false }
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            TabRow(
                selectedTabIndex = 1,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[1]).height(3.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                divider = {}
            ) {
                Tab(
                    selected = false,
                    onClick = { onTabChange(0) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        stringResource(R.string.login),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Tab(
                    selected = true,
                    onClick = {},
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        stringResource(R.string.sign_up),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = mobile,
                onValueChange = {
                    mobile = it
                    mobileError = !Validation().isValidIranMobile(it)
                },
                label = { Text(stringResource(R.string.mobile_number)) },
                isError = mobileError,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            if (mobileError) {
                Text(
                    text = stringResource(R.string.invalid_mobile_number),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = it.length < 6
                },
                label = { Text(stringResource(R.string.password)) },
                isError = passwordError,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )
            if (passwordError) {
                Text(
                    text = stringResource(R.string.password_too_short),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val candidate = User(
                        mobileNumber = mobile,
                        password = password
                    )
                    pendingUser = candidate
                    showOtp = true
                },
                enabled = !mobileError && !passwordError && mobile.isNotBlank() && password.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = stringResource(R.string.sign_up), style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(24.dp))

            LoginFooter(
                onGuestClick = {
                    context.startActivity(
                        Intent(context, GuestActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                    )
                }
            )
        }
    }
}

@Composable
fun LoginFooter(onGuestClick: () -> Unit) {
    val annotatedText = buildAnnotatedString {
        val guestTag = "guest_entrance"
        pushStringAnnotation(tag = guestTag, annotation = "guest_entrance")
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
            append(stringResource(R.string.guest_entrance))
        }
        pop()
    }

    ClickableText(
        text = annotatedText,
        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
        onClick = { offset ->
            annotatedText.getStringAnnotations(tag = "guest_entrance", start = offset, end = offset)
                .firstOrNull()?.let { _ -> onGuestClick() }
        },
        modifier = Modifier.padding(top = 8.dp),
        maxLines = 2
    )
}

fun saveLoginState(context: Context, isLoggedIn: Boolean, userId: Long, mobile: String, roleId: Long) {
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    prefs.edit {
        putBoolean("is_logged_in", isLoggedIn)
        putLong("user_id", userId)
        putLong("role_id", roleId)
        putString("user_mobile", mobile)
    }
}


fun saveFirstLoginState(context: Context, isFirstLoggedIn: Boolean) {
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    prefs.edit { putBoolean("first_login", isFirstLoggedIn) }
}

fun isUserLoggedIn(context: Context): Boolean {
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    return prefs.getBoolean("is_logged_in", false)
}

fun isFirstLoggedIn(context: Context): Boolean {
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    return prefs.getBoolean("first_login", false)
}

//private fun insertDefaultAuthorizationData() {
//    CoroutineScope(Dispatchers.IO).launch {
//        val existingCount = authorizationDao.getAuthorizationCrossRefCount()
//        if (existingCount > 0) {
//            return@launch
//        }
//        val rolesList = roleDao.getRoles()
//        val roleMap = rolesList.associateBy { it.roleName }
//
//        suspend fun insertCrossRefs(
//            roleId: Long,
//            objectId: Long,
//            fields: List<AuthorizationField>,
//            permission: PermissionLevel
//        ) {
//            fields.forEach { field ->
//                authorizationDao.insertRoleAuthorizationFieldCrossRef(
//                    RoleAuthorizationObjectFieldCrossRef(
//                        roleId = roleId,
//                        objectId = objectId,
//                        fieldId = field.fieldId,
//                        permissionLevel = permission
//                    )
//                )
//            }
//        }
//
//        val adminManagerRoles = listOf(Roles.ADMIN, Roles.BUILDING_MANAGER)
//        adminManagerRoles.forEach { roleName ->
//            val role = roleMap[roleName] ?: return@forEach
//            AuthObject.getAll().forEach { authObject ->
//                val fields = authorizationDao.getFieldsForObject(authObject.id)
//                insertCrossRefs(role.roleId, authObject.id, fields, PermissionLevel.FULL)
//            }
//        }
//
//        roleMap[Roles.PROPERTY_TENANT]?.let { tenantRole ->
//            val tenantFieldNames = listOf(
//                BuildingProfileFields.UNITS_TAB.fieldNameRes,
//                BuildingProfileFields.USERS_OWNERS.fieldNameRes,
//                BuildingProfileFields.USERS_TENANTS.fieldNameRes,
//                BuildingProfileFields.TENANTS_TAB.fieldNameRes
//            )
//            val allFields = authorizationDao.getFieldsForObject(3L)
//            val tenantFields = allFields.filter { it.name in tenantFieldNames }
//            insertCrossRefs(tenantRole.roleId, 3L, tenantFields, PermissionLevel.FULL)
//        }
//
//        roleMap[Roles.PROPERTY_OWNER]?.let { ownerRole ->
//            val ownerFieldNames = listOf(
//                BuildingProfileFields.UNITS_TAB.fieldNameRes,
//                BuildingProfileFields.USERS_OWNERS.fieldNameRes,
//                BuildingProfileFields.USERS_TENANTS.fieldNameRes,
//                BuildingProfileFields.TENANTS_TAB.fieldNameRes,
//                BuildingProfileFields.OWNERS_TAB.fieldNameRes
//            )
//            val allFields = authorizationDao.getFieldsForObject(3L)
//            val ownerFields = allFields.filter { it.name in ownerFieldNames }
//            insertCrossRefs(ownerRole.roleId, 3L, ownerFields, PermissionLevel.FULL)
//        }
//    }
//}


@Composable
fun RolePickerDialog(
    context: Context,
    roles: List<Role>,
    onDismiss: () -> Unit,
    onConfirm: (Role) -> Unit
) {
    val uniqueRoles = remember(roles) { roles.distinctBy { it.roleId to it.roleName } }
    var selected by remember(uniqueRoles) { mutableStateOf<Role?>(uniqueRoles.firstOrNull()) }
    Log.d("selected", selected.toString())
    Log.d("uniqueRoles", uniqueRoles.toString())
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { selected?.let(onConfirm) }, enabled = selected != null) {
                Text(context.getString(R.string.continue_to), style = MaterialTheme.typography.bodyLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(context.getString(R.string.cancel), style = MaterialTheme.typography.bodyLarge)
            }
        },
        title = { Text(context.getString(R.string.choose_role), style = MaterialTheme.typography.bodyLarge) },
        text = {
            if (uniqueRoles.isEmpty()) {
                Text(context.getString(R.string.no_role_found), style = MaterialTheme.typography.bodyLarge)
            } else {
                LazyColumn(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                ) {
                    items(
                        items = uniqueRoles,
                        key = { "${it.roleId}-${it.roleName}" }
                    ) { role ->
                        val isSelected = selected?.roleId == role.roleId && selected?.roleName == role.roleName
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selected = role }
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    else Color.Transparent
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = role.roleName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    )
}

fun navigateAfterLoginWithCostsCheck(
    context: Context,
    roleId: Long,
    userId: Long
) {
    if (roleId != 1L && roleId != 3L) {
        val intent = Intent(context, HomePageActivity::class.java).apply {
            putExtra("user_id", userId)
            putExtra("role_id", roleId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
        if (context is Activity) context.finish()
        return
    }

    Cost().fetchCostsWithDebts(
        context = context,
        userId = userId,
        onSuccess = { costs, debts ->
            // list: List<BuildingWithCosts>

            val noCosts = costs.isEmpty() || debts.isEmpty()

            val targetActivity =
                if (noCosts) HomePageActivity::class.java
                else DashboardActivity::class.java

            val intent = Intent(context, targetActivity).apply {
                putExtra("user_id", userId)
                putExtra("role_id", roleId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            context.startActivity(intent)
            if (context is Activity) context.finish()

        },
        onError = { e ->
            Toast.makeText(
                context,
                e.message ?: context.getString(R.string.failed),
                Toast.LENGTH_SHORT
            ).show()

            val intent = Intent(context, HomePageActivity::class.java).apply {
                putExtra("user_id", userId)
                putExtra("role_id", roleId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
            if (context is Activity) context.finish()
        }
    )
}
