@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.delta

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MoneyOff
import androidx.compose.material.icons.outlined.Support
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import com.example.delta.data.entity.Units
import com.example.delta.init.IranianLocations
import androidx.compose.material3.InputChip
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.navigation.NavHostController
import com.example.delta.viewmodel.SharedViewModel
import ir.hamsaa.persiandatepicker.PersianDatePickerDialog
import ir.hamsaa.persiandatepicker.api.PersianPickerDate
import ir.hamsaa.persiandatepicker.api.PersianPickerListener
import com.example.delta.init.CurvedBottomNavShape
import com.example.delta.init.WaveIndicatorShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.delta.data.dao.AuthorizationDao
import com.example.delta.data.entity.RoleAuthorization
import com.example.delta.enums.PermissionLevel
import com.example.delta.init.NavItem
import com.example.delta.interfaces.RolePermissionsManager
import com.uploadcare.android.library.api.UploadcareClient
import com.uploadcare.android.library.api.UploadcareFile
import com.uploadcare.android.library.callbacks.UploadFileCallback
import com.uploadcare.android.library.exceptions.UploadcareApiException
import com.uploadcare.android.library.upload.FileUploader
import kotlin.math.roundToInt
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.People
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.text.font.FontWeight
import com.example.delta.data.entity.Owners
import kotlinx.coroutines.launch


@Composable
fun SimpleOutlinedTextFieldSample(name: String, modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf("") }

    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text(" $name") },
        modifier = modifier
    )
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun <VM : ViewModel> CostForm(
    viewModel: VM,
    insertItem: (String) -> Unit,
    listContent: @Composable (VM) -> Unit,
    contextString: Int,
    onFabClick: () -> Unit
) {
    AppTheme {
        var itemName by remember { mutableStateOf("") }
        val context = LocalContext.current
        var showDialog by remember { mutableStateOf(false) }

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    modifier = Modifier.padding(16.dp),
                    containerColor = Color(context.getColor(R.color.secondary_color))
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add")
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = context.getString(contextString),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Spacer(Modifier.height(32.dp))
                listContent(viewModel)
                Spacer(Modifier.height(12.dp))
            }
        }

        if (showDialog) {
            AddItemDialog(
                onDismiss = { showDialog = false },
                onInsert = insertItem
            )
        }
    }
}


@Composable
fun InputAndButton(insertItem: (String) -> Unit, itemNameState: String, onDismiss: () -> Unit) {
    var itemName by remember { mutableStateOf(itemNameState) }
    val context = LocalContext.current
    AppTheme {
        OutlinedTextField(
            value = itemName,
            modifier = Modifier.fillMaxWidth(),
            onValueChange = { itemName = it },
            keyboardOptions = KeyboardOptions.Default,
            label = {
                Text(
                    text = context.getString(R.string.type),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    insertItem(itemName)
                    onDismiss()
                    itemName = ""
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
                )
            ) {
                Text(
                    text = context.getString(R.string.insert),
                    modifier = Modifier.padding(2.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}


@Composable
fun <T, VM : ViewModel> GenericList(
    viewModel: VM,
    items: List<T>,
    itemContent: @Composable (T) -> Unit,
    onDeleteItem: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    AppTheme {
        LazyColumn(
            modifier = modifier.fillMaxSize()
        ) {
            itemsIndexed(items) { index, item ->
                SwipeToDeleteItem(
                    item = item,
                    onDelete = {
                        onDeleteItem(item)
                    },
                    content = { itemContent(item) }
                )
            }
        }
    }
}


@Composable
fun <T> GenericItem(
    item: T,
    itemName: (T) -> String, // Lambda to extract item name
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    AppTheme {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(context.getColor(R.color.primary_color))),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    style = MaterialTheme.typography.bodyMedium,
                    text = itemName(item) // Use the lambda to get the name
                )
            }
        }
    }
}


@Composable
fun <T> SwipeToDeleteItem(
    item: T,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true
                }

                else -> false
            }
        }
    )

    SwipeToDismissBox(
        modifier = modifier,
        state = dismissState,
        backgroundContent = {},
        enableDismissFromStartToEnd = false,
        content = { content() }
    )
}


@Composable
fun AddItemDialog(
    onDismiss: () -> Unit,
    onInsert: (String) -> Unit
) {
    AppTheme {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                modifier = Modifier
                    .width(300.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    var itemName by remember { mutableStateOf("") }
                    InputAndButton(
                        insertItem = onInsert,
                        itemNameState = itemName,
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ExposedDropdownMenuBoxExample(
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    label: String,
    itemLabel: (T) -> String,
    modifier: Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    AppTheme {
        ExposedDropdownMenuBox(
            modifier = modifier,
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                readOnly = true,
                value = selectedItem?.let { itemLabel(it) } ?: "",
                onValueChange = { },
                label = { Text(text = label, style = MaterialTheme.typography.bodyMedium) },
                textStyle = MaterialTheme.typography.bodyMedium,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expanded
                    )
                }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = itemLabel(item),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        onClick = {
                            onItemSelected(item)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProvinceStateSelector(
    sharedViewModel: SharedViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Get the list of all provinces
    val provinces = IranianLocations.provinces.keys.toList()

    // Get the list of states for the selected province, or empty if none
    val availableStates = remember(sharedViewModel.province) {
        IranianLocations.provinces[sharedViewModel.province] ?: emptyList()
    }

    Column(modifier = modifier) {
        // Province Selector
        ExposedDropdownMenuBoxExample(
            items = provinces,
            selectedItem = sharedViewModel.province,
            onItemSelected = { selectedProvince ->
                sharedViewModel.province = selectedProvince
                sharedViewModel.state = "" // Reset state when province changes
            },
            label = context.getString(R.string.province),
            modifier = Modifier.fillMaxWidth(),
            itemLabel = { it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // State Selector
        ExposedDropdownMenuBoxExample(
            items = availableStates,
            selectedItem = sharedViewModel.state,
            onItemSelected = { selectedState ->
                sharedViewModel.state = selectedState
            },
            label = context.getString(R.string.state),
            modifier = Modifier.fillMaxWidth(),
            itemLabel = { it }
        )
    }
}

//@TODO merge this function with ChipgroupShared
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChipGroupUnits(
    selectedUnits: List<Units>,
    onSelectionChange: (List<Units>) -> Unit,
    units: List<Units>,
    label: String
) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    FlowRow(
        modifier = Modifier.padding(8.dp)
    ) {
        units.forEach { unit ->
            InputChip(
                selected = selectedUnits.contains(unit),
                onClick = {
                    val newSelection = if (selectedUnits.contains(unit)) {
                        selectedUnits.filter { it != unit }
                    } else {
                        selectedUnits + unit
                    }
                    onSelectionChange(newSelection)
                },
                label = { Text(unit.unitNumber.toString()) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChipGroupOwners(
    selectedOwners: List<Owners>,
    onSelectionChange: (List<Owners>) -> Unit,
    owners: List<Owners>,
    label: String
) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    FlowRow(
        modifier = Modifier.padding(8.dp)
    ) {
        owners.forEach { owner ->
            InputChip(
                selected = selectedOwners.contains(owner),
                onClick = {
                    val newSelection = if (selectedOwners.contains(owner)) {
                        selectedOwners.filter { it != owner }
                    } else {
                        selectedOwners + owner
                    }
                    onSelectionChange(newSelection)
                },
                label = { Text(text = "${owner.firstName} ${owner.lastName}") }
            )
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChipGroupShared(
    modifier: Modifier = Modifier,
    selectedItems: List<String>,
    onSelectionChange: (List<String>) -> Unit,
    items: List<String>,
    label: String,
    singleSelection: Boolean = false // Add this flag
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items.forEach { item ->
                val isSelected = selectedItems.contains(item)
                InputChip(
                    selected = isSelected,
                    onClick = {
                        val newSelection = when {
                            singleSelection -> listOf(item) // Single selection
                            isSelected -> selectedItems - item // Toggle off
                            else -> selectedItems + item // Toggle on
                        }
                        onSelectionChange(newSelection)
                    },
                    label = { Text(item) }
                )
            }
        }
    }
}


// Example of checking user role in an activity
@Composable
fun checkUserRole(role: String): Boolean {
    when (role) {
        "owner" -> return true // Owner has access to all features
        "tenant" -> return false // Tenant has limited access
        "manager" -> return true // Manager has specific access
        "guest" -> return false // Guest has restricted access
        else -> return false
    }
}

// Example of displaying different UI based on user role
@Composable
fun Dashboard(role: String) {
    Column {
        if (role == "owner") {
            // Display owner-specific UI
            Text("Owner Dashboard")
        } else if (role == "tenant") {
            // Display tenant-specific UI
            Text("Tenant Dashboard")
        } else if (role == "manager") {
            // Display manager-specific UI
            Text("Manager Dashboard")
        } else if (role == "guest") {
            // Display guest-specific UI
            Text("Guest Dashboard")
        }
    }
}


@Composable
fun PasswordTextField(
    password: String,
    onPasswordChange: (String) -> Unit,
    context: Context
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = {
            Text(
                text = context.getString(R.string.prompt_password),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
//                Icon(
//                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
//                    contentDescription = "Toggle Password Visibility"
//                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

// Function for the date picker content to use
@Composable
fun PersianDatePickerDialogContent(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    context: Context
) {
    val activity = context as? Activity
    AppTheme {
        LaunchedEffect(key1 = Unit) {
            activity?.let {
                val picker = PersianDatePickerDialog(it)
                    .setPositiveButtonString(context.getString(R.string.insert))
                    .setNegativeButton(context.getString(R.string.cancel))
                    .setTodayButton(context.getString(R.string.today))
                    .setTodayButtonVisible(true)
                    .setMinYear(1300)
                    .setMaxYear(1450)
                    .setInitDate(1404, 1, 1)
                    .setActionTextColor(android.graphics.Color.GRAY)
                    .setTitleType(PersianDatePickerDialog.WEEKDAY_DAY_MONTH_YEAR)
                    .setShowInBottomSheet(true)
                    .setListener(object : PersianPickerListener {
                        override fun onDateSelected(persianPickerDate: PersianPickerDate) {
                            val dateStr =
                                "${persianPickerDate.persianYear}/${persianPickerDate.persianMonth}/${persianPickerDate.persianDay}"
                            onDateSelected(dateStr)
                        }

                        override fun onDismissed() {
                            onDismiss()
                        }
                    })
                picker.show()
            }


        }
    }
}

private fun currentRoute(navController: NavHostController): String? {
    return navController.currentBackStackEntry?.destination?.route
}


@Composable
fun AuthScreen(permissionsManager: RolePermissionsManager) {
    val context = LocalContext.current
    val userRole = permissionsManager.getUserRole()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Log.d("userRole", userRole.toString())
        if (userRole == "admin") {
            Text("Admin Settings", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            RoleManagementSection(permissionsManager = permissionsManager)
        } else {
            // Show an error message if not an admin
            Toast.makeText(
                context,
                "You don't have permission to access this settings.",
                Toast.LENGTH_SHORT
            ).show()
            Text(
                "You don't have permission to access this screen.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun RoleManagementSection(permissionsManager: RolePermissionsManager) {
    // State to hold all permissions
    val allPermissions = remember { mutableStateListOf<RoleAuthorization>() }

    // Load permissions on initial composition
    LaunchedEffect(Unit) {
        allPermissions.addAll(permissionsManager.getAllPermissions())
    }

    Column {
        Text("Manage Roles and Permissions", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // List all roles
        allPermissions.forEach { permission ->
            Text("Role: ${permission.role}")
            // Add options to edit/delete the role
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    context: Context,
    modifier: Modifier = Modifier,
    permissionsManager: RolePermissionsManager
) {
    var showAuthScreen by remember { mutableStateOf(false) }
    val items = listOf(
        NavItem(
            title = R.string.supporting,
            icon = Icons.Outlined.Support,
            onClick = { /* Handle support */ }
        ),
        NavItem(
            title = R.string.owners_list,
            icon = Icons.Outlined.Badge,
            onClick = { context.startActivity(Intent(context, OwnersActivity::class.java)) }
        ),
        NavItem(
            title = R.string.tenant_list,
            icon = Icons.Outlined.People,
            onClick = { context.startActivity(Intent(context, TenantsActivity::class.java)) }
        ),
        NavItem(
            title = R.string.cost_list,
            icon = Icons.Outlined.MoneyOff,
            onClick = { context.startActivity(Intent(context, CostActivity::class.java)) }
        ),
        NavItem(
            title = R.string.income_list,
            icon = Icons.Outlined.AttachMoney,
            onClick = { context.startActivity(Intent(context, EarningsActivity::class.java)) }
        ),
        NavItem(
            title = R.string.building_type_list,
            icon = Icons.Outlined.Apartment,
            onClick = { context.startActivity(Intent(context, BuildingTypeActivity::class.java)) }
        ),
        NavItem(
            title = R.string.building_usage_list,
            icon = Icons.Outlined.Business,
            onClick = { context.startActivity(Intent(context, BuildingUsageActivity::class.java)) }
        ),
        NavItem(
            title = R.string.user_management,
            icon = Icons.Outlined.Lock,
            onClick = { context.startActivity(Intent(context, UserManagementActivity::class.java)) }
        )
    )

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // debug background
                .padding(16.dp)
        ) {
            items.forEach { item ->
                ClickableSettingItem(
                    title = context.getString(item.title),
                    icon = item.icon,
                    onClick = item.onClick
                )
                Spacer(Modifier.height(8.dp))
            }
        }

    }
    if (showAuthScreen) {
        AuthScreen(permissionsManager = permissionsManager)
    }
}


@Composable
fun ClickableSettingItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


@Composable
fun CurvedBottomNavigation(
    navController: NavHostController,
    items: List<Screen>,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current
    val density = LocalDensity.current

    Box(modifier = modifier.fillMaxWidth()) {
        // Wave indicator
        if (currentRoute != null) {
            val selectedIndex = items.indexOfFirst { it.route == currentRoute }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = (-4).dp)
            ) {
                val containerWidth = remember { mutableStateOf(0f) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged { containerWidth.value = it.width.toFloat() }
                ) {
                    if (containerWidth.value > 0) {
                        val animatedOffset by animateOffsetAsState(
                            targetValue = density.run {
                                val itemWidth = containerWidth.value / items.size
                                Offset(
                                    x = itemWidth * selectedIndex + itemWidth / 2 - 24.dp.toPx(),
                                    y = 0f
                                )
                            },
                            animationSpec = spring(dampingRatio = 0.6f)
                        )

                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(8.dp)
                                .offset { IntOffset(animatedOffset.x.roundToInt(), 0) }
                                .clip(WaveIndicatorShape(waveHeight = 16f))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }

        // Navigation surface
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            shape = CurvedBottomNavShape(),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // In your Row items.forEach
                items.forEach { screen ->
                    CurvedBottomNavItem(
                        icon = screen.icon,
                        label = "",//screen.title
                        selected = currentRoute == screen.route,
                        isAddButton = screen == Screen.Add, // Identify Add button
                        onClick = {
                            if (screen == Screen.Add) {
                                context.startActivity(
                                    Intent(
                                        context,
                                        BuildingFormActivity::class.java
                                    )
                                )
                            } else {
                                navController.navigate(screen.route)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CurvedBottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    isAddButton: Boolean = false // New parameter
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .then(if (isAddButton) Modifier.offset(y = (-16).dp) else Modifier)
                .background(
                    color = when {
                        isAddButton -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else -> Color.Transparent
                    },
                    shape = if (isAddButton) RoundedCornerShape(6.dp) else CircleShape
                )
                .border(
                    width = if (isAddButton) 1.dp else 0.dp,
                    color = if (isAddButton) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = if (isAddButton) RoundedCornerShape(6.dp) else CircleShape
                )
                .clickable(onClick = onClick)
                .padding(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isAddButton) MaterialTheme.colorScheme.primary
                else if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (selected) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun Dp.toPx() = with(LocalDensity.current) { this@toPx.toPx() }

@Composable
fun Float.toDp() = with(LocalDensity.current) { this@toDp.toDp() }


// For wave animation positioning
fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}

@Composable
fun UploadFile(
    context: Context,
    modifier: Modifier = Modifier
) {
    // State for tracking upload progress and result
    var isUploading by remember { mutableStateOf(false) }
    var fileUrl by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Image picker launcher
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isUploading = true
            val client = UploadcareClient("YOUR_PUBLIC_KEY", "YOUR_SECRET_KEY")
            val fileUploader = FileUploader(client, uri, context).store(true)

            fileUploader.uploadAsync(object : UploadFileCallback {
                override fun onSuccess(result: UploadcareFile) {
                    fileUrl = result.originalFileUrl.toString()
                    errorMessage = null
                    isUploading = false
                }

                override fun onFailure(e: UploadcareApiException) {
                    errorMessage = "Upload failed: ${e.message}"
                    isUploading = false
                    Log.e("Upload", errorMessage!!)
                }

                override fun onProgressUpdate(
                    bytesWritten: Long,
                    contentLength: Long,
                    progress: Double
                ) {
                    // Handle progress updates if needed
                }
            })
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
//        Text(
//            text = context.getString(R.string.upload),
//            style = MaterialTheme.typography.bodyLarge
//        )

        Box {
            // Upload button
            Button(
                onClick = { imagePicker.launch("image/*") },
                enabled = !isUploading
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(context.getString(R.string.upload))
                }
            }

            // Show error message if exists
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }
        }

        // Display uploaded image thumbnail
//        fileUrl?.let { url ->
//            Image(
//                painter = rememberAsyncImagePainter(url),
//                contentDescription = "Uploaded building image",
//                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
//                contentScale = ContentScale.Crop
//            )
//        }
    }
}

@Composable
fun YearMonthSelector(
    selectedYear: Int,
    onYearChange: (Int) -> Unit,
    selectedMonth: Int,
    onMonthChange: (Int) -> Unit
) {
    val persianMonths = listOf(
        LocalContext.current.getString(R.string.farvardin),
        LocalContext.current.getString(R.string.ordibehesht),
        LocalContext.current.getString(R.string.khordad),
        LocalContext.current.getString(R.string.tir),
        LocalContext.current.getString(R.string.mordad),
        LocalContext.current.getString(R.string.shahrivar),
        LocalContext.current.getString(R.string.mehr),
        LocalContext.current.getString(R.string.aban),
        LocalContext.current.getString(R.string.azar),
        LocalContext.current.getString(R.string.dey),
        LocalContext.current.getString(R.string.bahman),
        LocalContext.current.getString(R.string.esfand)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Year selector with border
        Row(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = Color.Gray,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onYearChange(selectedYear - 1) }) {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Previous Year"
                )
            }
            Text(
                text = selectedYear.toString(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            IconButton(onClick = { onYearChange(selectedYear + 1) }) {
                Icon(
                    imageVector = Icons.Default.ArrowDropUp,
                    contentDescription = "Next Year"
                )
            }
        }

        // Month selector with border
        Row(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = Color.Gray,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                val prevMonth = if (selectedMonth > 1) selectedMonth - 1 else 12
                onMonthChange(prevMonth)
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Previous Month"
                )
            }
            Text(
                text = persianMonths.getOrNull(selectedMonth - 1) ?: "",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            IconButton(onClick = {
                val nextMonth = if (selectedMonth < 12) selectedMonth + 1 else 1
                onMonthChange(nextMonth)
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowDropUp,
                    contentDescription = "Next Month"
                )
            }
        }
    }
}


// Convert to user-friendly strings
fun PermissionLevel.toDisplayName(context: Context): String {
    return when (this) {
        PermissionLevel.READ -> context.getString(R.string.view)
        PermissionLevel.WRITE -> context.getString(R.string.edit)
        PermissionLevel.DELETE -> context.getString(R.string.delete)
        PermissionLevel.FULL -> context.getString(R.string.full)
    }
}

// For permission selection dialog
@Composable
fun PermissionLevelSelector(
    currentLevel: PermissionLevel,
    context: Context,
    onLevelSelected: (PermissionLevel) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.wrapContentSize()) {
        Button(onClick = { expanded = true }) {
            Text(currentLevel.toDisplayName(context))
            Icon(Icons.Default.ArrowDropDown, null)
        }

        DropdownMenu(expanded, { expanded = false }) {
            PermissionLevel.entries.forEach { level ->
                DropdownMenuItem(
                    text = { Text(level.toDisplayName(context)) },
                    onClick = {
                        onLevelSelected(level)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun PermissionedOutlinedTextField(
    fieldName: Int,
    objectId: Long,
    sharedViewModel: SharedViewModel,
    authDao: AuthorizationDao,
    roleId: Long,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var canEdit by remember { mutableStateOf(false) }

    LaunchedEffect(fieldName, objectId, roleId) {
        canEdit = hasFieldPermission(
            dao = authDao,
            roleId = roleId,
            objectId = objectId,
            fieldName = fieldName,
            required = PermissionLevel.WRITE
        )
    }

    OutlinedTextField(
        value = value,
        onValueChange = { if (canEdit) onValueChange(it) },
        label = { Text(label) },
        modifier = modifier,
        enabled = canEdit
    )
}

suspend fun hasFieldPermission(
    dao: AuthorizationDao,
    roleId: Long,
    objectId: Long,
    fieldName: Int,
    required: PermissionLevel = PermissionLevel.READ
): Boolean {
    val field = dao.getFieldByName(objectId, fieldName) ?: return false
    val fieldPerm = dao.getFieldPermission(roleId, field.fieldId)
    val objectPerm = dao.getObjectPermission(roleId, objectId)

    // Prefer field-level, fallback to object-level
    val effectivePerm = fieldPerm ?: objectPerm
    return effectivePerm != null && effectivePerm >= required.value
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AnimatedLoginScreen(
    onLoginClick: (username: String, password: String) -> Unit,
    modifier: Modifier = Modifier
) {
    // States for inputs and UI
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var loginSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()


    // Animate button width and color on loading
    val buttonWidth by animateDpAsState(targetValue = if (isLoading) 60.dp else 280.dp)
    val buttonColor by animateColorAsState(
        targetValue = if (loginSuccess) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
    )

    // Animate login success message visibility
    AnimatedVisibility(
        visible = loginSuccess,
        enter = fadeIn(animationSpec = tween(700)) + slideInVertically(),
        exit = fadeOut(animationSpec = tween(700)) + slideOutVertically()
    ) {
        Text(
            text = "ورود موفقیت‌آمیز بود!",
            color = Color(0xFF4CAF50),
            fontSize = 20.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = TextAlign.Center
        )
    }

    // Main Column
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF2196F3), Color(0xFF21CBF3))
                )
            )
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = "خوش آمدید",
            style = MaterialTheme.typography.headlineLarge.copy(color = Color.White),
            modifier = Modifier.padding(bottom = 40.dp)
        )

        // Username TextField
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("نام کاربری") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "User Icon") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.White,
                unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f),
                cursorColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
            ),

            )

        Spacer(modifier = Modifier.height(16.dp))

        // Password TextField
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("رمز عبور") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password Icon") },
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.Person else Icons.Filled.Lock
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Person else Icons.Default.Lock,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = Color.White
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.White,
                unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f),
                cursorColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
            ),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Error message
        errorMessage?.let {
            Text(
                text = it,
                color = Color(0xFFFF5252),
                modifier = Modifier.padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )
        }

        // Login Button with animation
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(buttonWidth)
                .height(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(buttonColor)
                .clickable(enabled = !isLoading) {
                    focusManager.clearFocus()
                    errorMessage = null
                    if (username.isBlank() || password.isBlank()) {
                        errorMessage = "لطفا نام کاربری و رمز عبور را وارد کنید"
                        return@clickable
                    }
                    isLoading = true
                    coroutineScope.launch {
                        delay(2000)
                        if (username == "admin" && password == "1234") {
                            loginSuccess = true
                            errorMessage = null
                        } else {
                            errorMessage = "نام کاربری یا رمز عبور اشتباه است"
                            loginSuccess = false
                        }
                        isLoading = false
                    }
                }

        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(28.dp)
                )
            } else {
                Text(
                    text = "ورود",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
fun FundInfoBox(formattedFund: String, context: Context) {
    Surface(
        tonalElevation = 4.dp,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${context.getString(R.string.fund_lbl)}: ${formattedFund.englishToPersianDigits()} ${
                    context.getString(
                        R.string.toman
                    )
                }",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }


    }
}

// Add to your utilities
fun String.englishToPersianDigits(): String {
    return this.map { char ->
        when (char) {
            '0' -> '۰'
            '1' -> '۱'
            '2' -> '۲'
            '3' -> '۳'
            '4' -> '۴'
            '5' -> '۵'
            '6' -> '۶'
            '7' -> '۷'
            '8' -> '۸'
            '9' -> '۹'
            else -> char
        }
    }.joinToString("")
}








