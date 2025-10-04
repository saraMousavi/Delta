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
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Lock
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
import androidx.compose.foundation.lazy.grid.items
import kotlin.math.roundToInt
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material3.InputChipDefaults
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import coil.compose.rememberAsyncImagePainter
import com.example.delta.data.entity.Owners
import com.example.delta.enums.HomePageFields
import com.example.delta.enums.PermissionLevel
import com.example.delta.init.AuthUtils
import com.example.delta.init.Preference
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
    sharedViewModel: SharedViewModel,
    viewModel: VM,
    insertItem: (String) -> Unit,
    listContent: @Composable (VM) -> Unit,
    contextString: Int
) {
    AppTheme (useDarkTheme = sharedViewModel.isDarkModeEnabled){
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
                sharedViewModel = sharedViewModel,
                onDismiss = { showDialog = false },
                onInsert = insertItem
            )
        }
    }
}


@Composable
fun InputAndButton(sharedViewModel: SharedViewModel, insertItem: (String) -> Unit, itemNameState: String, onDismiss: () -> Unit) {
    var itemName by remember { mutableStateOf(itemNameState) }
    val context = LocalContext.current
    AppTheme (useDarkTheme = sharedViewModel.isDarkModeEnabled){
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
    modifier: Modifier = Modifier,
    sharedViewModel: SharedViewModel
) {
    AppTheme (useDarkTheme = sharedViewModel.isDarkModeEnabled){
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
    sharedViewModel: SharedViewModel,
    item: T,
    itemName: (T) -> String, // Lambda to extract item name
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    AppTheme (useDarkTheme = sharedViewModel.isDarkModeEnabled){
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
    sharedViewModel: SharedViewModel,
    onDismiss: () -> Unit,
    onInsert: (String) -> Unit
) {
    AppTheme (useDarkTheme = sharedViewModel.isDarkModeEnabled){
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
                        sharedViewModel = sharedViewModel,
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
    sharedViewModel: SharedViewModel,
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    label: String,
    itemLabel: (T) -> String,
    modifier: Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    AppTheme (useDarkTheme = sharedViewModel.isDarkModeEnabled){
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
            sharedViewModel = sharedViewModel,
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
            sharedViewModel = sharedViewModel,
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
    label: String,
    context: Context
) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    FlowRow(
        modifier = Modifier.padding(8.dp)
    ) {
        val allSelected = units.isNotEmpty() && selectedUnits.size == units.size
        InputChip(
            selected = allSelected,
            onClick = {
                if (allSelected) {
                    // Deselect all
                    onSelectionChange(emptyList())
                } else {
                    // Select all
                    onSelectionChange(units)
                }
            },
            label = { Text(text = context.getString(R.string.all), style = MaterialTheme.typography.bodyLarge) },
            colors = InputChipDefaults.inputChipColors(
                selectedContainerColor = Color.Gray,
                selectedLabelColor = Color.White,
                containerColor = if (allSelected) Color.Gray else Color.LightGray,
                labelColor = if (allSelected) Color.White else Color.Black
            )
        )

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
    sharedViewModel: SharedViewModel,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    context: Context
) {
    val activity = context as? Activity
    AppTheme (useDarkTheme = sharedViewModel.isDarkModeEnabled){
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

    val chargeItem = NavItem(
        title = R.string.charges_calculation,
        icon = Icons.Outlined.Calculate,
        onClick = { context.startActivity(Intent(context, ChargeCalculationActivity::class.java)) }
    )

    val capitalInfoItem = NavItem(
        title = R.string.capital_info,
        icon = Icons.Outlined.AttachMoney,
        onClick = { context.startActivity(Intent(context, CapitalActivity::class.java)) }
    )

    val firstGroup = listOf(
        NavItem(
            title = R.string.supporting,
            icon = Icons.Outlined.Support,
            onClick = { context.startActivity(Intent(context, ChargeCalculationActivity::class.java)) }
        ),
        NavItem(
            title = R.string.income_list,
            icon = Icons.Outlined.AttachMoney,
            onClick = { context.startActivity(Intent(context, EarningsActivity::class.java)) }
        )
    )

    val secondGroup = listOf(
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
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Charge calculation full width row
            item {
                ClickableSettingItem(
                    title = context.getString(chargeItem.title),
                    icon = chargeItem.icon,
                    onClick = chargeItem.onClick,
                    modifier = Modifier.fillMaxWidth(),
                    iconOnTop = false
                )
            }
            // Capital calculation full width row
            item {
                ClickableSettingItem(
                    title = context.getString(capitalInfoItem.title),
                    icon = capitalInfoItem.icon,
                    onClick = capitalInfoItem.onClick,
                    modifier = Modifier.fillMaxWidth(),
                    iconOnTop = false
                )
            }

            // First group grid wrapped in Box to enforce max height
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp) // adjust based on your content
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(4.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        userScrollEnabled = false
                    ) {
                        items(firstGroup) { item ->
                            ClickableSettingItem(
                                title = context.getString(item.title),
                                icon = item.icon,
                                onClick = item.onClick,
                                modifier = Modifier.height(120.dp),
                                iconOnTop = true
                            )
                        }
                    }
                }
            }

            // Second group grid wrapped in Box to enforce max height
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp) // adjust as needed to fit all rows without scrolling
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(4.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        userScrollEnabled = false
                    ) {
                        items(secondGroup) { item ->
                            ClickableSettingItem(
                                title = context.getString(item.title),
                                icon = item.icon,
                                onClick = item.onClick,
                                modifier = Modifier.height(120.dp),
                                iconOnTop = true
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAuthScreen) {
        AuthScreen()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClickableSettingItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconOnTop: Boolean = true
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        if (iconOnTop) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier
                        .size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
            }
        } else {
            // Icon left, title right row for full width charge row
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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
    maxFileSizeBytes: Long = 5 * 1024 * 1024, // for example 5MB max size limit
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
            // Before saving, check size
            val fileSize = getFileSize(context, it)
            if (fileSize != null && fileSize > maxFileSizeBytes) {
                errorMessage = "${context.getString(R.string.file_exeed)}: ${maxFileSizeBytes / (1024 * 1024)} MB"
                return@let
            }
            isSaving = true
            val filename = queryFileName(context, it) ?: "uploaded_${System.currentTimeMillis()}"
            val savedPath = copyUriToInternalStorage(context, it, filename)
            if (savedPath != null) {
                savedFilePaths.add(savedPath)
                errorMessage = null
                onFileSaved(savedPath)
            } else {
                errorMessage = context.getString(R.string.failed)
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
                Row (horizontalArrangement = Arrangement.SpaceBetween){
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
                            color = MaterialTheme.colorScheme.error
                        )
                    }
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

// Helper function to get file size from Uri
fun getFileSize(context: Context, uri: Uri): Long? {
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (sizeIndex != -1) {
            cursor.moveToFirst()
            return cursor.getLong(sizeIndex)
        }
    }
    // Fallback: try open inputStream and get available size (less efficient)
    return try {
        context.contentResolver.openInputStream(uri)?.available()?.toLong()
    } catch (e: Exception) {
        null
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
fun FundInfoBox(formattedFund: String, context: Context, title: String) {
    Surface(
        tonalElevation = 4.dp,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 8.dp),
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












