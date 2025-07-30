@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.delta

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import android.webkit.MimeTypeMap
import android.provider.OpenableColumns
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
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.delta.init.NavItem
import androidx.compose.foundation.lazy.items
import kotlin.math.roundToInt
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.People
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.delta.data.entity.Owners
import com.example.delta.enums.HomePageFields
import com.example.delta.enums.PermissionLevel
import com.example.delta.init.AuthUtils
import com.example.delta.init.Preference
import ir.hamsaa.persiandatepicker.util.PersianCalendar
import java.io.File
import java.io.FileOutputStream


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
    contextString: Int
) {
    AppTheme {
        val context = LocalContext.current
        var showDialog by remember { mutableStateOf(false) }

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    modifier = Modifier.padding(16.dp),
//                    containerColor = Color(context.getColor(R.color.secondary_color))
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
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
//                )
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
fun <T> GenericList(
    items: List<T>,
    itemContent: @Composable (T) -> Unit,
    onDeleteItem: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    AppTheme {
        LazyColumn(
            modifier = modifier.fillMaxSize()
        ) {
            items(items) { item ->
                SwipeToDeleteItem(
                    onDelete = {
                        onDeleteItem(item)
                    },
                    modifier = Modifier,
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
                    style = MaterialTheme.typography.bodyLarge,
                    text = itemName(item) // Use the lambda to get the name
                )
            }
        }
    }
}


@Composable
fun SwipeToDeleteItem(
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
                    val itemName by remember { mutableStateOf("") }
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
                label = { Text(text = label, style = MaterialTheme.typography.bodyLarge) },
                textStyle = MaterialTheme.typography.bodyLarge,
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
                                style = MaterialTheme.typography.bodyLarge
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
                label = { Text(text = unit.unitNumber, style = MaterialTheme.typography.bodyLarge) }
            )
        }
    }
}

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
                label = { Text(text = "${owner.firstName} ${owner.lastName}", style = MaterialTheme.typography.bodyLarge) }
            )
        }
    }
}


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
                    label = { Text(item, style = MaterialTheme.typography.bodyLarge) }
                )
            }
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
                            val monthStr = persianPickerDate.persianMonth.toString().padStart(2, '0')
                            val dayStr = persianPickerDate.persianDay.toString().padStart(2, '0')
                            val dateStr = "${persianPickerDate.persianYear}/$monthStr/$dayStr"

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


@Composable
fun AuthScreen() {
    val context = LocalContext.current
//    val userRole = permissionsManager.getUserRole()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
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
//
//@Composable
//fun RoleManagementSection(permissionsManager: RolePermissionsManager) {
//    // State to hold all permissions
//    val allPermissions = remember { mutableStateListOf<RoleAuthorization>() }
//
//    // Load permissions on initial composition
//    LaunchedEffect(Unit) {
//        allPermissions.addAll(permissionsManager.getAllPermissions())
//    }
//
//    Column {
//        Text("Manage Roles and Permissions", style = MaterialTheme.typography.titleMedium)
//        Spacer(modifier = Modifier.height(8.dp))
//
//        // List all roles
//        allPermissions.forEach { permission ->
//            Text("Role: ${permission.role}")
//            // Add options to edit/delete the role
//        }
//    }
//}

@Composable
fun SettingsScreen(
    context: Context
) {
    val showAuthScreen by remember { mutableStateOf(false) }
    val items = listOf(
        NavItem(
            title = R.string.charges_calculation,
            icon = Icons.Outlined.Calculate,
            onClick = { context.startActivity(Intent(context, ChargeCalculationActivity::class.java)) }
        ),

         NavItem(
            title = R.string.supporting,
            icon = Icons.Outlined.Support,
            onClick = { context.startActivity(Intent(context, ChargeCalculationActivity::class.java)) }
        ),
//        NavItem(
//            title = R.string.owners_list,
//            icon = Icons.Outlined.Badge,
//            onClick = { context.startActivity(Intent(context, OwnersActivity::class.java)) }
//        ),
//        NavItem(
//            title = R.string.tenant_list,
//            icon = Icons.Outlined.People,
//            onClick = { context.startActivity(Intent(context, TenantsActivity::class.java)) }
//        ),
//        NavItem(
//            title = R.string.cost_list,
//            icon = Icons.Outlined.MoneyOff,
//            onClick = { context.startActivity(Intent(context, CostActivity::class.java)) }
//        ),
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
        AuthScreen()
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
    modifier: Modifier = Modifier,
    sharedViewModel: SharedViewModel
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current
    val density = LocalDensity.current
    val userId = Preference().getUserId(context = context)
    val permissionLevelSetting = AuthUtils.checkFieldPermission(
        userId,
        HomePageFields.SETTING.fieldNameRes,
        sharedViewModel
    )

    val permissionLevelAddBuilding = AuthUtils.checkFieldPermission(
        userId,
        HomePageFields.ADD_BUILDING.fieldNameRes,
        sharedViewModel
    )

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
                val containerWidth = remember { mutableFloatStateOf(0f) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged { containerWidth.floatValue = it.width.toFloat() }
                ) {
                    if (containerWidth.floatValue > 0) {
                        val animatedOffset by animateOffsetAsState(
                            targetValue = density.run {
                                val itemWidth = containerWidth.floatValue / items.size
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
                                if(permissionLevelAddBuilding == PermissionLevel.FULL || permissionLevelAddBuilding == PermissionLevel.WRITE){
                                    context.startActivity(
                                        Intent(
                                            context,
                                            BuildingFormActivity::class.java
                                        )
                                    )
                                } else {
                                    Toast.makeText(context, context.getString(R.string.auth_cancel), Toast.LENGTH_LONG).show()
                                }

                            } else if (screen == Screen.Settings){
                                if(permissionLevelSetting == PermissionLevel.FULL || permissionLevelSetting == PermissionLevel.WRITE){
                                    navController.navigate(screen.route)
                                } else {
                                    Toast.makeText(context, context.getString(R.string.auth_cancel), Toast.LENGTH_LONG).show()
                                }
                            }
                            else {
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

fun copyUriToInternalStorage(context: Context, uri: Uri, filename: String): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val file = File(context.filesDir, filename)
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun UploadFile(
    sharedViewModel: SharedViewModel,
    context: Context,
    modifier: Modifier = Modifier,
    onFileSaved: (String) -> Unit
) {
    var isSaving by remember { mutableStateOf(false) }
    val savedFilePaths = sharedViewModel.savedFilePaths
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isSaving = true
            val filename = queryFileName(context, it) ?: "uploaded_${System.currentTimeMillis()}"
            val savedPath = copyUriToInternalStorage(context, it, filename)
            if (savedPath != null) {
                savedFilePaths.add(savedPath)
                errorMessage = null
                onFileSaved(savedPath)
            } else {
                errorMessage = "Failed to save file"
            }
            isSaving = false
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box {
                Button(
                    onClick = { filePicker.launch("*/*") }, // Allow all file types
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = context.getString(R.string.upload),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = savedFilePaths) { path ->
                FileItem(
                    filePath = path,
                    onClick = { openFile(context, path) },
                    onDelete = {
                        sharedViewModel.deleteFile(path)
                    },
                    modifier = Modifier.size(64.dp)
                )
            }
        }
    }
}

@Composable
fun FileItem(
    filePath: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val file = remember(filePath) { File(filePath) }
    val extension = remember(filePath) { file.extension.lowercase() }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() },
        contentAlignment = Alignment.TopEnd
    ) {
        when (extension) {
            "jpg", "jpeg", "png", "gif", "bmp", "webp" -> {
                Image(
                    painter = rememberAsyncImagePainter(file),
                    contentDescription = "Image file",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            "pdf" -> Icon(
                imageVector = Icons.Default.PictureAsPdf,
                contentDescription = "PDF file",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            // Add other file type icons as needed
            else -> Icon(
                imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                contentDescription = "File",
                modifier = Modifier.size(32.dp)
            )
        }

        IconButton(
            onClick = {
                onDelete()
            },
            modifier = Modifier
                .size(24.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = CircleShape
                )
                .align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete file",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}


fun openFile(context: Context, filePath: String) {
    val file = File(filePath)
    val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
    val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension.lowercase()) ?: "*/*"

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mime)
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
    }

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No app found to open this file type", Toast.LENGTH_SHORT).show()
    }
}

fun queryFileName(context: Context, uri: Uri): String? {
    var name: String? = null
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0) {
                name = it.getString(index)
            }
        }
    }
    return name
}

@Composable
fun YearMonthSelector(
    selectedYear: Int?, // nullable Int, null means no year selected
    onYearChange: (Int) -> Unit,
    selectedMonth: Int,
    onMonthChange: (Int) -> Unit
) {
    val context = LocalContext.current
    val persianMonths = listOf(
        context.getString(R.string.farvardin),
        context.getString(R.string.ordibehesht),
        context.getString(R.string.khordad),
        context.getString(R.string.tir),
        context.getString(R.string.mordad),
        context.getString(R.string.shahrivar),
        context.getString(R.string.mehr),
        context.getString(R.string.aban),
        context.getString(R.string.azar),
        context.getString(R.string.dey),
        context.getString(R.string.bahman),
        context.getString(R.string.esfand)
    )

    val currentYear = remember { PersianCalendar().persianYear }

    BoxWithConstraints {
        val maxWidth = maxWidth
        // Allocate roughly 30% for year and 40% for month text widths, adjust as needed
        val yearTextWidth = maxWidth * 0.1f
        val monthTextWidth = maxWidth * 0.5f

        Row(
            modifier = Modifier.wrapContentWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Year selector with arrows and text
            Row(
                modifier = Modifier
                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = {
                        val year = selectedYear ?: currentYear
                        onYearChange(year - 1)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = context.getString(R.string.previous_year)
                    )
                }

                Text(
                    text = selectedYear?.toString() ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .width(yearTextWidth),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                IconButton(
                    onClick = {
                        val year = selectedYear ?: currentYear
                        onYearChange(year + 1)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropUp,
                        contentDescription = context.getString(R.string.next_year)
                    )
                }
            }

            // Month selector with arrows and month name
            Row(
                modifier = Modifier
                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = {
                        val prevMonth = if (selectedMonth > 1) selectedMonth - 1 else 12
                        onMonthChange(prevMonth)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = context.getString(R.string.previous_month),
                        modifier = Modifier.size(24.dp), // explicit size
                        tint = MaterialTheme.colorScheme.onSurface // ensure contrast
                    )
                }

                Text(
                    text = persianMonths.getOrNull(selectedMonth - 1) ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .width(monthTextWidth),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                IconButton(
                    onClick = {
                        val nextMonth = if (selectedMonth < 12) selectedMonth + 1 else 1
                        onMonthChange(nextMonth)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropUp,
                        contentDescription = context.getString(R.string.next_month)
                    )
                }
            }
        }
    }
}


@Composable
fun FundInfoBox(formattedFund: String, context: Context, title: String) {
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
                text = "${title}: ${formattedFund.englishToPersianDigits()} ${
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












