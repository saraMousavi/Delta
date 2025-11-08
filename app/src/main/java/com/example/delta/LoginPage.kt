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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.delta.data.entity.User
import com.example.delta.enums.Roles
import com.example.delta.init.Preference
import com.example.delta.viewmodel.BuildingsViewModel
import com.example.delta.viewmodel.SharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.content.edit
import com.example.delta.data.dao.AuthorizationDao
import com.example.delta.data.dao.RoleDao
import com.example.delta.data.entity.AuthorizationField
import com.example.delta.data.entity.RoleAuthorizationObjectFieldCrossRef
import com.example.delta.enums.AuthObject
import com.example.delta.enums.BuildingProfileFields
import com.example.delta.enums.PermissionLevel
import com.example.delta.init.Validation
import kotlin.collections.forEach

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import com.example.delta.data.model.AppDatabase
import com.example.delta.init.FileManagement
import com.example.delta.screens.OnboardingScreenWithModalSheet
import com.example.delta.screens.OtpScreen
import com.example.delta.volley.TokenUploader
import com.google.firebase.messaging.FirebaseMessaging
import com.example.delta.data.entity.Role
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.delta.LoginPage
import com.example.delta.data.entity.UserRoleCrossRef


private lateinit var roleDao: RoleDao // Add Role DAO
private lateinit var authorizationDao: AuthorizationDao

class LoginPage : ComponentActivity() {
    private val viewModel: BuildingsViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by viewModels()

    private val REQUEST_CODE_PICK_EXCEL = 1001


    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
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
                        Toast.makeText(this, "خطا: ${e.message}", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = AppDatabase.getDatabase(this)

        roleDao = database.roleDao() // Initialize Role DAO
        authorizationDao = database.authorizationDao() // Initialize Role DAO
        setContent {
            AppTheme (useDarkTheme = sharedViewModel.isDarkModeEnabled){
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    var showOnboardingModal by remember { mutableStateOf(false) }
                    var navigateToIntent by remember { mutableStateOf<Intent?>(null) }

                    val context = LocalContext.current

                    if (isUserLoggedIn(this@LoginPage)) {
                        val userId = Preference().getUserId(context = this@LoginPage)
                        val userRole by sharedViewModel.getRoleByUserId(userId).collectAsState(initial = null)

                        userRole?.let { role ->
                            // When not signing up and userRole is available, navigate accordingly
                            LaunchedEffect(role) {
                                // Navigate only if not showing onboarding modal
                                if (!showOnboardingModal) {
                                    val intent = when (role.roleName) {
                                        Roles.ADMIN, Roles.BUILDING_MANAGER, Roles.COMPLEX_MANAGER -> {
                                            Intent(context, DashboardActivity::class.java)
                                        }
                                        else -> {
                                            Intent(context, HomePageActivity::class.java)
                                        }
                                    }
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    navigateToIntent = intent
                                }
                            }

                            // Show circle progress until navigation triggered
                            if (navigateToIntent == null) {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .wrapContentSize(Alignment.Center)
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        } ?: run {
                            // If userRole still null, show loading
//                            Box(
//                                Modifier
//                                    .fillMaxSize()
//                                    .wrapContentSize(Alignment.Center)
//                            ) {
//                                CircularProgressIndicator()
//                            }
                        }
                    } else {
                        val selectedTab = remember { mutableIntStateOf(0) }

                        when (selectedTab.intValue) {
                            0 -> LoginFormScreen(buildingsViewModel = viewModel, sharedViewModel = sharedViewModel,
                                onTabChange = { selectedTab.intValue = it })
                            1 -> SignUpScreen(
                                sharedViewModel = sharedViewModel,
                                onSignUpSuccess = { user ->
                                    saveLoginState(this@LoginPage, true, user.userId, user.mobileNumber)
                                    saveFirstLoginState(this@LoginPage, true)
                                },
                                onShowBoarding = {
                                    showOnboardingModal = true
                                },
                                onTabChange = { selectedTab.intValue = it }
                            )
                        }
                    }

                    // Show onboarding modal only if flagged
                    if (showOnboardingModal) {
                        OnboardingScreenWithModalSheet(
                            onManualEntry = {
                                saveFirstLoginState(this@LoginPage, false)
                                val intent = Intent(this@LoginPage, BuildingFormActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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
                            }
                        )
                    }

                    // Trigger navigation after login if intent set
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
//
//fun handleServerLogin(
//    context: Context,
//    mobile: String,
//    password: String,
//    onSuccess: (User) -> Unit,
//    onInvalid: () -> Unit,
//    onError: (Throwable) -> Unit,
//    onFinally: () -> Unit
//) {
//    val api = com.example.delta.volley.Users()
//    api.login(
//        context = context,
//        mobileNumber = mobile,
//        password = password,
//        onSuccess = { user ->
//            onSuccess(user)
//            onFinally()
//        },
//        onInvalid = {
//            onInvalid()
//            onFinally()
//        },
//        onError = { err ->
//            onError(err)
//            onFinally()
//        }
//    )
//}
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
    var roleDialogVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    var serverRoles by remember { mutableStateOf<List<Role>>(emptyList()) }

    Column(modifier = Modifier
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
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
                        serverRoles = result
                        isLoading = false
                        roleDialogVisible = true
                    },
                    onInvalid = {
                        isLoading = false
                        errorText = context.getString(R.string.invalid_username)
                    },
                    onError = { t ->
                        isLoading = false
                        errorText = t.message ?: context.getString(R.string.invalid_username)
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
                    text = if (selectedTab == 0) context.getString(R.string.login)
                    else context.getString(R.string.sign_up),
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
            onConfirm = { chosen ->
                roleDialogVisible = false
                saveLoginState(context, true, sharedViewModel.userId, password)
                saveFirstLoginState(context, true)
                val intent = Intent(context, DashboardActivity::class.java).apply {
                    putExtra("user_id", sharedViewModel.userId)
                    putExtra("role_id", chosen.roleId)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(intent)
                if (context is Activity) context.finish()
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
                            insertDefaultAuthorizationData()
                            FirebaseMessaging.getInstance().token
                                .addOnSuccessListener { token ->
                                    TokenUploader.uploadFcmToken(
                                        context = context,
                                        user = user,
                                        fcmToken = token,
                                        onSuccess = {},
                                        onError = {}
                                    )
                                }
                            saveLoginState(context, true, user.userId, user.mobileNumber)
                            saveFirstLoginState(context, true)
                            onSignUpSuccess(user)
                            showOtp = false
                            onShowBoarding()
                        },
                        onError = {
                            Toast.makeText(context, context.getString(R.string.failed), Toast.LENGTH_SHORT).show()
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
                modifier = Modifier.fillMaxWidth()
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
                        password = password,
                        roleId = 1L
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




fun handleLogin(
    context: Context,
    users: List<User>,
    username: String,
    password: String,
    buildingsViewModel: BuildingsViewModel
) {
    val user = login(users, username, password)
    if (user != null) {
        saveLoginState(context, true, user.userId, user.mobileNumber)

        val buildings = buildingsViewModel.getAllBuildingsList()
        val intent = if (buildings.isEmpty()) {
            Intent(context, BuildingFormActivity::class.java).apply {
                putExtra("user_id", user.userId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        } else {
            Intent(context, DashboardActivity::class.java).apply {
                putExtra("user_id", user.userId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }
        context.startActivity(intent)

        if (context is Activity) context.finish()
    } else {
        Log.i("LoginDebug", "Invalid login attempt: username=$username")
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(
                context,
                context.getString(R.string.invalid_username),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

// Utility login function unchanged
fun login(users: List<User>, username: String, password: String): User? {
    Log.d("users", users.toString())
    Log.d("username", username)
    Log.d("password", password)
    return users.find { it.mobileNumber == username && it.password == password }
}

// --- Shared Preferences State ---

fun saveLoginState(context: Context, isLoggedIn: Boolean, userId: Long, mobile: String) {
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    prefs.edit {
        putBoolean("is_logged_in", isLoggedIn)
        putLong("user_id", userId)
        putString("user_mobile", mobile)
        putBoolean("first_login", true)
    }
}

fun saveFirstLoginState(context: Context, isFirstLoggedIn: Boolean) {
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    prefs.edit {
        putBoolean("first_login", isFirstLoggedIn)
    }
}

fun isUserLoggedIn(context: Context): Boolean {
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    return prefs.getBoolean("is_logged_in", false)
}

fun isFirstLoggedIn(context: Context): Boolean {
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    return prefs.getBoolean("first_login", false)
}


private fun insertDefaultAuthorizationData() {
    CoroutineScope(Dispatchers.IO).launch {
        val rolesList = roleDao.getRoles()
        val roleMap = rolesList.associateBy { it.roleName }

        suspend fun insertCrossRefs(roleId: Long, objectId: Long, fields: List<AuthorizationField>, permission: PermissionLevel) {
            fields.forEach { field ->
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

        // Admin and Manager: all auth objects and all fields with FULL permission
        val adminManagerRoles = listOf(Roles.ADMIN, Roles.BUILDING_MANAGER)
        adminManagerRoles.forEach { roleName ->
            val role = roleMap[roleName] ?: return@forEach
            AuthObject.getAll().forEach { authObject ->
                val fields = authorizationDao.getFieldsForObject(authObject.id)
                insertCrossRefs(role.roleId, authObject.id, fields, PermissionLevel.FULL)
            }
        }

        // Tenant role: get fields from Room for objectId = 3 (BuildingProfile)
        roleMap[Roles.PROPERTY_TENANT]?.let { tenantRole ->
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

        // Owner role: similar to tenant plus owners tab fields
        roleMap[Roles.PROPERTY_OWNER]?.let { ownerRole ->
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedTabRow(
    selectedTabIndex: Int,
    tabTitles: List<String>,
    onTabSelected: (Int) -> Unit
) {
    TabRow(
        selectedTabIndex = selectedTabIndex,
        indicator = { tabPositions ->
            // Animate the indicator's offset horizontally
            val currentTabPosition = tabPositions[selectedTabIndex]
            val indicatorOffset by animateDpAsState(targetValue = currentTabPosition.left)

            TabRowDefaults.Indicator(
                Modifier
                    .offset(x = indicatorOffset)
                    .width(currentTabPosition.width)
                    .height(3.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    ) {
        tabTitles.forEachIndexed { index, title ->
            Tab(
                selected = index == selectedTabIndex,
                onClick = { onTabSelected(index) },
                text = { Text(title, style = MaterialTheme.typography.bodyLarge) }
            )
        }
    }
}
@Composable
fun RolePickerDialog(
    context: Context,
    roles: List<Role>,
    onDismiss: () -> Unit,
    onConfirm: (Role) -> Unit
) {
    val uniqueRoles = remember(roles) { roles.distinctBy { it.roleId to it.roleName } }
    var selected by remember(uniqueRoles) { mutableStateOf<Role?>(uniqueRoles.firstOrNull()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { selected?.let(onConfirm) }, enabled = selected != null) {
                Text(
                    context.getString(R.string.continue_to),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    context.getString(R.string.cancel),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
        title = {
            Text(
                context.getString(R.string.choose_role),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        text = {
            if (uniqueRoles.isEmpty()) {
                Text(
                    context.getString(R.string.no_role_found),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(Modifier.fillMaxWidth().heightIn(max = 320.dp)) {
                    items(
                        items = uniqueRoles,
                        key = { "${it.roleId}-${it.roleName.name}" }
                    ) { role ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            RadioButton(
                                selected = selected?.roleId == role.roleId && selected?.roleName == role.roleName,
                                onClick = { selected = role }
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    role.roleName.getDisplayName(context),
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






