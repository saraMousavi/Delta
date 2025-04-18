@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.delta

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlin.math.roundToInt


@Composable
fun SimpleOutlinedTextFieldSample(name: String, modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf("") }

    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text(" $name")},
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
fun InputAndButton(insertItem: (String) -> Unit, itemNameState: String, onDismiss: () -> Unit,) {
    var itemName by remember { mutableStateOf(itemNameState) }
    val context = LocalContext.current
    AppTheme {
        OutlinedTextField(
            value = itemName,
            modifier = Modifier.fillMaxWidth(),
            onValueChange = { itemName = it },
            keyboardOptions = KeyboardOptions.Default,
            label = { Text(text = context.getString(R.string.type), style = MaterialTheme.typography.bodyLarge) }
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
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                modifier = modifier.fillMaxWidth().menuAnchor(),
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
                        text = { Text(
                            text = itemLabel(item),
                            style = MaterialTheme.typography.bodyMedium) },
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


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChipGroupShared(
    modifier: Modifier = Modifier,
    selectedItems: List<String>,
    onSelectionChange: (List<String>) -> Unit,
    items: List<String>,
    label: String
) {
    Row(modifier = modifier) {
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
                        val mutableSelectedItems = selectedItems.toMutableList()
                        if (isSelected) {
                            mutableSelectedItems.remove(item)
                        } else {
                            mutableSelectedItems.add(item)
                        }
                        onSelectionChange(mutableSelectedItems.toList())
                    },
                    label = { Text(text = item, style = MaterialTheme.typography.bodyLarge) }
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
        label = { Text(text = context.getString(R.string.prompt_password),
            style = MaterialTheme.typography.bodyLarge) },
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


// HomeScreen.kt
@Composable
fun HomeScreen(
    onNavigateToForm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Building Management",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(32.dp))

        FilledTonalButton(
            onClick = onNavigateToForm,
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Add New Building")
        }
    }
}

// SettingsScreen.kt
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium
        )
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
                                    context.startActivity(Intent(context, BuildingFormActivity::class.java))
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
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .then(if(isAddButton) Modifier.offset(y = (-16).dp) else Modifier)
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
                .padding(8.dp)
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

