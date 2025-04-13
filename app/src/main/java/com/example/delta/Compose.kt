@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.delta

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import com.example.delta.data.entity.Units
import com.example.delta.init.IranianLocations
import com.example.delta.viewmodel.BuildingsViewModel
import androidx.compose.material3.InputChip
import com.google.android.material.chip.Chip


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier, fontSize: TextUnit) {
    Text(
        text = "$name",
        modifier = modifier,
        fontFamily = firaSansFamily,
        fontSize = fontSize
    )
}


@Composable
fun FilledButtonExample(name: String, modifier: Modifier =  Modifier, onClick: () -> Unit) {
    Button(onClick = { onClick() }, modifier = modifier, colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.btn))) {
        Text( text = " $name", fontFamily = firaSansFamily)
    }
}

@Composable
fun CheckboxMinimalExample(modifier: Modifier = Modifier) {
    var checked by remember { mutableStateOf(true) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            "Minimal checkbox"
        )
        Checkbox(
            checked = checked,
            onCheckedChange = { checked = it }
        )
    }

    Text(
        if (checked) "Checkbox is checked" else "Checkbox is unchecked"
    )
}

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
            label = { Text(text = context.getString(R.string.type), style = MaterialTheme.typography.bodyMedium) }
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
                    style = MaterialTheme.typography.titleLarge
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
    itemLabel: (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }
    AppTheme {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                readOnly = true,
                value = selectedItem?.let { itemLabel(it) } ?: "",
                onValueChange = { },
                label = { Text(label) },
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
                            style = MaterialTheme.typography.bodyLarge) },
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
    viewModel: BuildingsViewModel,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val selectedProvince by viewModel.province.collectAsState()
        val selectedState by viewModel.state.collectAsState()
        val availableStates by viewModel.availableStates.collectAsState()

        Column(modifier = modifier) {
            // Province Selector
            ExposedDropdownMenuBoxExample(
                items = IranianLocations.provinces.keys.toList(),
                selectedItem = selectedProvince,
                onItemSelected = { province ->
                    viewModel.onProvinceSelected(province)
                },
                label = LocalContext.current.getString(R.string.province), // Persian for "Province"
                itemLabel = { it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // State Selector
            ExposedDropdownMenuBoxExample(
                items = availableStates,
                selectedItem = selectedState,
                onItemSelected = { state ->
                    viewModel.onStateSelected(state)
                },
                label = LocalContext.current.getString(R.string.state), // Persian for "State"
                itemLabel = { it }
            )
        }
    }
}

//@TODO merge this function with ChipgroupShared
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChipGroupUnits(
    selectedUnits: List<Units>,
    onSelectionChange: (List<Units>) -> Unit,
    units: List<Units>
) {
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
    items: List<String>
) {
    Row(modifier = modifier) {
        Text(
            text = LocalContext.current.getString(R.string.shared_things),
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

