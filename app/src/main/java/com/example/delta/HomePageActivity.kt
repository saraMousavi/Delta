package com.example.delta

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.delta.data.entity.BuildingWithCounts
import com.example.delta.data.entity.Role
import com.example.delta.data.entity.User
import com.example.delta.enums.FundType
import com.example.delta.enums.Gender
import com.example.delta.enums.PermissionLevel
import com.example.delta.init.AuthUtils.AuthUtils.permissionFor
import com.example.delta.init.AuthUtils.AuthorizationFieldsHome
import com.example.delta.init.AuthUtils.AuthorizationObjects
import com.example.delta.init.FileManagement
import com.example.delta.init.NotificationPermissionHelper
import com.example.delta.init.Preference
import com.example.delta.init.Validation
import com.example.delta.permission.Notification
import com.example.delta.viewmodel.SharedViewModel
import com.example.delta.volley.Building
import com.example.delta.volley.Fund
import com.example.delta.volley.Users
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner


class HomePageActivity : ComponentActivity() {
    val sharedViewModel: SharedViewModel by viewModels()
    private val REQUEST_CODE_PICK_EXCEL = 1001

    private val showNotificationSettingsDialogState = mutableStateOf(false)

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                ensureGeneralChannel()
            } else {
                showNotificationSettingsDialogState.value = true
            }
        }



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

        requestNotificationPermissionIfNeeded()

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finishAffinity()
                }
            }
        )

        enableEdgeToEdge()
        setContent {

            AppTheme(useDarkTheme = sharedViewModel.isDarkModeEnabled) {
                val isNetworkAvailable by rememberNetworkState()

                val context = LocalContext.current
                val userId = remember { Preference().getUserId(context) }

                var user by remember { mutableStateOf<User?>(null) }
                var isLoadingUser by remember { mutableStateOf(true) }
                var showMandatoryDialog by remember { mutableStateOf(false) }

                val showNotifDialog by showNotificationSettingsDialogState

                if (showNotifDialog) {
                    AlertDialog(
                        onDismissRequest = { showNotificationSettingsDialogState.value = false },
                        title = { Text(context.getString(R.string.notification_permission_title)) },
                        text = { Text(context.getString(R.string.notification_permission_desc)) },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showNotificationSettingsDialogState.value = false
                                    openNotificationSettings(context)
                                }
                            ) { Text(context.getString(R.string.go_to_settings)) }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showNotificationSettingsDialogState.value = false }
                            ) { Text(context.getString(R.string.cancel)) }
                        }
                    )
                }

                LaunchedEffect(userId) {
                    isLoadingUser = true
                    Users().fetchUserById(
                        context = context,
                        userId = userId,
                        onSuccess = { fetched ->
                            user = fetched
                            isLoadingUser = false
                            showMandatoryDialog =
                                fetched.firstName.isBlank() || fetched.lastName.isBlank()
                        },
                        onError = { isLoadingUser = false }
                    )
                }

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
                ) { drawerPadding ->

                    val navController = rememberNavController()

                    val lifecycleOwner = LocalLifecycleOwner.current

                    val mobileNumber = Preference().getUserMobile(context)
                    val notAllowedRoleIds = setOf(7L, 9L, 10L)

                    var savedRoles by remember { mutableStateOf<List<Role>>(emptyList()) }
                    var rolesLoading by remember { mutableStateOf(false) }
                    var rolesLoaded by remember { mutableStateOf(false) }

                    var activeRoleId by rememberSaveable { mutableLongStateOf(0L) }
                    var ready by remember { mutableStateOf(false) }

                    val refreshRolesTick by sharedViewModel.rolesReloadTick.collectAsState()

                    LaunchedEffect(Unit) {
                        sharedViewModel.requestRolesReload()
                    }




                    fun pickInitialRoleId(roles: List<Role>): Long {
                        val pref = Preference().getRoleId(context)
                        val prefExists = roles.any { it.roleId == pref }
                        return when {
                            pref > 0 && prefExists -> pref
                            roles.isNotEmpty() -> roles.first().roleId
                            else -> 0L
                        }
                    }

                    LaunchedEffect(isNetworkAvailable, mobileNumber, refreshRolesTick) {
                        if (!isNetworkAvailable || mobileNumber.isNullOrBlank()) return@LaunchedEffect

                        rolesLoading = true
                        rolesLoaded = false
                        ready = false
                        activeRoleId = 0L
                        savedRoles = emptyList()

                        Users().getUserRoles(
                            context = context,
                            mobileNumber = mobileNumber,
                            onSuccess = { result ->
                                savedRoles = result
                                rolesLoading = false
                                rolesLoaded = true

                                val initial = pickInitialRoleId(result)
                                activeRoleId = initial
                            },
                            onError = {
                                rolesLoading = false
                                rolesLoaded = false
                                ready = false
                                Toast.makeText(context, context.getString(R.string.failed), Toast.LENGTH_LONG).show()
                            }
                        )
                    }


                    LaunchedEffect(rolesLoaded, activeRoleId) {
                        if (!rolesLoaded) return@LaunchedEffect
                        if (activeRoleId <= 0L) return@LaunchedEffect

                        Preference().setRoleId(context, activeRoleId)
                        sharedViewModel.loadRolePermissions(context, activeRoleId)
                        ready = true
                    }

                    val eligibleRoles = savedRoles.filter { it.roleId in notAllowedRoleIds }

                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(drawerPadding),
                        topBar = {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val isHome = navBackStackEntry
                                ?.destination
                                ?.hierarchy
                                ?.any { it.route == Screen.Home.route } == true
                            Log.d("eligibleRoles", eligibleRoles.toString())
                            Log.d("rolesLoaded", rolesLoaded.toString())
                            if (rolesLoaded && eligibleRoles.isEmpty() && isHome) {
                                HomeTopBarWithRoleDropdown(
                                    sharedViewModel = sharedViewModel,
                                    user = user,
                                    roles = savedRoles,
                                    activeRoleId = activeRoleId,
                                    onRoleChange = { selected -> activeRoleId = selected.roleId }
                                )
                            }

                        },
                        bottomBar = {
                            CurvedBottomNavigation(
                                currentRoleId = activeRoleId,
                                navController = navController,
                                items = Screen.items,
                                sharedViewModel = sharedViewModel
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = Screen.Home.route
                            ) {
                                composable(Screen.Home.route) {
                                    if (rolesLoaded && ready && activeRoleId > 0L) {
                                        BuildingList(
                                            roleId = activeRoleId,
                                            sharedViewModel = sharedViewModel
                                        )
                                    } else {
                                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                }
                                composable(Screen.Settings.route) {
                                    val perms = sharedViewModel.rolePermissions
                                    val perm = perms.permissionFor(
                                        AuthorizationObjects.HOME,
                                        AuthorizationFieldsHome.SETTINGS_BUTTON
                                    )
                                    if (perm == PermissionLevel.WRITE || perm == PermissionLevel.FULL) {
                                        SettingsScreen(LocalContext.current)
                                    } else {
                                        LaunchedEffect(Unit) {
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.auth_for_manager),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = context.getString(R.string.auth_for_manager),
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                    }
                                }
                            }

                            if (!isLoadingUser && showMandatoryDialog && user != null) {
                                MandatoryNameDialogInHome(
                                    sharedViewModel = sharedViewModel,
                                    initialUser = user!!,
                                    onSubmit = { updatedUser ->
                                        val payload = JSONObject().apply {
                                            put("firstName", updatedUser.firstName)
                                            put("lastName", updatedUser.lastName)
                                            put("email", updatedUser.email)
                                            put("gender", updatedUser.gender)
                                            put("nationalCode", updatedUser.nationalCode)
                                            put("address", updatedUser.address)
                                        }

                                        Users().updateUser(
                                            context = context,
                                            userId = userId,
                                            payload = payload,
                                            onSuccess = {
                                                user = updatedUser
                                                showMandatoryDialog = false
                                                Toast.makeText(
                                                    context,
                                                    context.getString(R.string.success_update),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            onError = {
                                                Toast.makeText(
                                                    context,
                                                    context.getString(R.string.failed),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                showMandatoryDialog = false
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } else {
                ensureGeneralChannel()
            }
        } else {
            ensureGeneralChannel()
            if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                showNotificationSettingsDialogState.value = true
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

fun openNotificationSettings(context: Context) {
    try {
        val intent = Intent().apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    action = android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                else -> {
                    action = "android.settings.APP_NOTIFICATION_SETTINGS"
                    putExtra("app_package", context.packageName)
                    putExtra("app_uid", context.applicationInfo.uid)
                }
            }
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        try {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.fromParts("package", context.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e2: Exception) {
            val intent = Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBarWithRoleDropdown(
    sharedViewModel: SharedViewModel,
    user: User?,
    roles: List<Role>,
    activeRoleId: Long,
    onRoleChange: (Role) -> Unit
) {
    val selectedRole = remember(roles, activeRoleId) {
        roles.firstOrNull { it.roleId == activeRoleId } ?: roles.firstOrNull()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = listOfNotNull(user?.firstName, user?.lastName)
                        .joinToString(" ")
                        .ifBlank { " " },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(8.dp))

                ExposedDropdownMenuBoxExample(
                    sharedViewModel = sharedViewModel,
                    items = roles,
                    selectedItem = selectedRole,
                    onItemSelected = onRoleChange,
                    label = stringResource(R.string.choose_role),
                    modifier = Modifier.fillMaxWidth(),
                    itemLabel = { it.roleName }
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.role_change_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MandatoryNameDialogInHome(
    sharedViewModel: SharedViewModel,
    initialUser: User,
    onSubmit: (User) -> Unit
) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    var firstName by remember { mutableStateOf(initialUser.firstName) }
    var lastName by remember { mutableStateOf(initialUser.lastName) }
    var email by remember { mutableStateOf(initialUser.email.orEmpty()) }
    var nationalCode by remember { mutableStateOf(initialUser.nationalCode.orEmpty()) }
    var address by remember { mutableStateOf(initialUser.address.orEmpty()) }
    var gender by remember { mutableStateOf(initialUser.gender ?: Gender.FEMALE) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var userState by remember { mutableStateOf(initialUser) }

    val canSubmit = remember(userState.firstName, userState.lastName) {
        userState.firstName.trim().isNotEmpty() && userState.lastName.trim().isNotEmpty()
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = { },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = context.getString(R.string.complete_user_info),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(bottom = 16.dp)
                ) {
                    UserProfileEditableField(
                        label = stringResource(id = R.string.first_name),
                        value = userState.firstName,
                        onValueChange = { v ->
                            userState = userState.copy(firstName = v)
                            firstName = v
                        },
                        required = true,
                        modifier = Modifier.focusRequester(focusRequester)
                    )

                    UserProfileEditableField(
                        label = stringResource(id = R.string.last_name),
                        value = userState.lastName,
                        onValueChange = { v ->
                            userState = userState.copy(lastName = v)
                            lastName = v
                        },
                        required = true
                    )

                    UserProfileEditableField(
                        label = stringResource(id = R.string.email),
                        value = userState.email.orEmpty(),
                        onValueChange = { newValue ->
                            userState = userState.copy(email = newValue)
                            email = newValue
                            emailError = if (newValue.isBlank()) {
                                null
                            } else if (!Validation().isValidEmail(newValue)) {
                                context.getString(R.string.invalid_email)
                            } else {
                                null
                            }
                        },
                        isError = emailError != null,
                        errorText = emailError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )

                    GenderDropdown(
                        sharedViewModel = sharedViewModel,
                        selectedGender = userState.gender?.let {
                            Gender.fromDisplayName(
                                context,
                                it.getDisplayName(context)
                            )
                        },
                        onGenderSelected = { g ->
                            userState = userState.copy(gender = g)
                            gender = g
                        },
                        label = stringResource(id = R.string.gender),
                        modifier = Modifier.fillMaxWidth()
                    )

                    UserProfileEditableField(
                        label = stringResource(id = R.string.national_code),
                        value = userState.nationalCode.orEmpty(),
                        onValueChange = { v ->
                            userState = userState.copy(nationalCode = v)
                            nationalCode = v
                        }
                    )

                    UserProfileEditableField(
                        label = stringResource(id = R.string.address),
                        value = userState.address.orEmpty(),
                        onValueChange = { v ->
                            userState = userState.copy(address = v)
                            address = v
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(top = 8.dp, bottom = 12.dp))

            Button(
                enabled = canSubmit,
                onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus()

                    val updated = userState.copy(
                        firstName = userState.firstName.trim(),
                        lastName = userState.lastName.trim(),
                        email = userState.email?.trim()?.ifBlank { null },
                        gender = userState.gender ?: Gender.FEMALE,
                        nationalCode = userState.nationalCode?.trim()?.ifBlank { null },
                        address = userState.address?.trim()?.ifBlank { null }
                    )

                    onSubmit(updated)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = context.getString(R.string.insert),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        delay(200)
        focusRequester.requestFocus()
        keyboardController?.show()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildingList(
    roleId: Long?,
    sharedViewModel: SharedViewModel
) {
    val context = LocalContext.current
    val userId = Preference().getUserId(context)
    val effectiveRoleId = roleId ?: Preference().getRoleId(context)

    var showGuestDialog by remember { mutableStateOf(false) }
    var buildingUserList by remember { mutableStateOf<List<BuildingWithCounts>>(emptyList()) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    var hasFetchedBuildings by remember { mutableStateOf(false) }

    LaunchedEffect(effectiveRoleId) {
        if (effectiveRoleId == 7L || effectiveRoleId == 10L || effectiveRoleId == 9L) {
            showGuestDialog = true
        }
        sharedViewModel.refreshUnreadCount()
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
                    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    prefs.edit { putBoolean("is_logged_in", false) }
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

    fun loadBuilding(roleId: Long, mobile : String) {
        sharedViewModel.refreshNotificationsForUser(userId)
        Building(context).fetchBuildingsForUser(
            mobileNumber = mobile,
            roleId = roleId,
            onSuccess = { list ->
                buildingUserList = list
                loadError = null
                isLoading = false
                hasFetchedBuildings = true
            },
            onError = { e ->
                loadError = e.message
                buildingUserList = emptyList()
                isLoading = false
                hasFetchedBuildings = true
            }
        )
    }

    LaunchedEffect(mobile, effectiveRoleId) {
        if (mobile == null) return@LaunchedEffect
        hasFetchedBuildings = false
        isLoading = true
        loadError = null
        loadBuilding(effectiveRoleId, mobile)
    }

    val refreshLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val shouldRefresh = result.data?.getBooleanExtra("NEED_REFRESH", false) ?: false
            Log.d("shouldRefresh", shouldRefresh.toString())
            if (shouldRefresh) {
                val mobileSafe = mobile
                if (mobileSafe.isNullOrBlank()) return@rememberLauncherForActivityResult

                hasFetchedBuildings = false
                isLoading = true
                loadBuilding(effectiveRoleId, mobileSafe)
                sharedViewModel.requestRolesReload()
            }

        }
    }


    val perms = sharedViewModel.rolePermissions

    when {
        !hasFetchedBuildings || isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

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
                        val perm = perms.permissionFor(
                            AuthorizationObjects.HOME,
                            AuthorizationFieldsHome.CREATE_BUILDING_BUTTON
                        )
                        if (perm == PermissionLevel.WRITE || perm == PermissionLevel.FULL) {
                            context.startActivity(
                                Intent(context, BuildingFormActivity::class.java)
                            )
                        } else {
                            Toast.makeText(
                                context,
                                context.getString(R.string.auth_for_manager),
                                Toast.LENGTH_SHORT
                            ).show()

                        }
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
                        sharedViewModel = sharedViewModel,
                        onClick = {
                                val intent = Intent(context, BuildingProfileActivity::class.java).apply {
                                    putExtra("BUILDING_TYPE_NAME", buildingWithTypesAndUsages.buildingTypeName)
                                    putExtra("BUILDING_USAGE_NAME", buildingWithTypesAndUsages.buildingUsageName)
                                    putExtra("BUILDING_DATA", buildingWithTypesAndUsages.buildingId)
                                }
                                refreshLauncher.launch(intent)
                        },
                        onDelete = { buildingId ->
                            Building(context).deleteBuilding(
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
    sharedViewModel: SharedViewModel,
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
            .clickable {
                showMenu = false
                onClick()
            },
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
                    onClick = { showMenu = !showMenu },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (showMenu) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 40.dp, end = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column {
                            Text(
                                text = context.getString(R.string.delete_building),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                                    .clickable {
                                        showMenu = false
                                        val perms = sharedViewModel.rolePermissions
                                        val perm = perms.permissionFor(
                                            AuthorizationObjects.HOME,
                                            AuthorizationFieldsHome.DELETE_BUILDING_BUTTON
                                        )
                                        if (perm == PermissionLevel.WRITE || perm == PermissionLevel.FULL) {
                                            showDeleteDialog = true
                                        } else {
                                            Toast
                                                .makeText(
                                                    context,
                                                    context.getString(R.string.auth_for_manager),
                                                    Toast.LENGTH_SHORT
                                                )
                                                .show()
                                        }
                                    }
                                    .padding(horizontal = 16.dp, vertical = 16.dp)
                            )
                        }
                    }
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
                            },
                            colors = AssistChipDefaults.assistChipColors()
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
                            },
                            colors = AssistChipDefaults.assistChipColors()
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
