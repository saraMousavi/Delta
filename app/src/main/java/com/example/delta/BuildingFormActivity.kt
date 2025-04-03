package com.example.delta


import com.example.delta.data.entity.BuildingTypes
import com.example.delta.data.entity.BuildingUsages
import com.example.delta.data.entity.Buildings
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.delta.viewmodel.BuildingTypeViewModel
import com.example.delta.viewmodel.BuildingUsageViewModel
import com.example.delta.viewmodel.BuildingsViewModel

class BuildingFormActivity : ComponentActivity() {
    private val viewModel: BuildingsViewModel by viewModels()
    private val buildingTypeViewModel: BuildingTypeViewModel by viewModels()
    private val buildingUsageViewModel: BuildingUsageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                // Fetch Building Types and Usages from your ViewModels
                val buildingTypes by buildingTypeViewModel.getAllBuildingType().collectAsState(initial = emptyList())
                val buildingUsages by buildingUsageViewModel.getAllBuildingUsage().collectAsState(initial = emptyList())

                BuildingFormScreen(
                    viewModel = viewModel,
                    buildingTypes = buildingTypes,
                    buildingUsages = buildingUsages
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildingFormScreen(
    viewModel: BuildingsViewModel,
    buildingTypes: List<BuildingTypes>,
    buildingUsages: List<BuildingUsages>
) {
    var name by remember { mutableStateOf("") }
    var selectedBuildingTypes by remember { mutableStateOf<BuildingTypes?>(null) }
    var selectedBuildingUsages by remember { mutableStateOf<BuildingUsages?>(null) }
    var ownerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var nationalCode by remember { mutableStateOf("") }
    var postCode by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item{CenterAlignedTopAppBar(
            title = { Text( text = context.getString(R.string.buildings_form) , style = MaterialTheme.typography.bodyLarge) },
            navigationIcon = {
                IconButton(onClick = {
                    (context as? BuildingFormActivity)?.finish()
                     }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )}
        item{Spacer(modifier = Modifier.height(16.dp))}

        item {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = {
                    Text(
                        text = context.getString(R.string.building_name),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            OutlinedTextField(
                value = ownerName,
                onValueChange = { ownerName = it },
                label = {
                    Text(
                        text = context.getString(R.string.name_family),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = {
                    Text(
                        text = context.getString(R.string.email),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            OutlinedTextField(
                value = nationalCode,
                onValueChange = { nationalCode = it },
                label = {
                    Text(
                        text = context.getString(R.string.national_code),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item{Spacer(modifier = Modifier.height(8.dp))}

        item {
            OutlinedTextField(
                value = postCode,
                onValueChange = { postCode = it },
                label = {
                    Text(
                        text = context.getString(R.string.post_code),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }



        item {
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = {
                    Text(
                        text = context.getString(R.string.address),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Building Type Dropdown
        item {
            ExposedDropdownMenuBoxExample(
                items = buildingTypes,
                selectedItem = selectedBuildingTypes,
                onItemSelected = { selectedBuildingTypes = it },
                label = context.getString(R.string.building_type),
                itemLabel = { it.buildingTypeName }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Building Usage Dropdown
        item {
            ExposedDropdownMenuBoxExample(
                items = buildingUsages,
                selectedItem = selectedBuildingUsages,
                onItemSelected = { selectedBuildingUsages = it },
                label = context.getString(R.string.building_usage),
                itemLabel = { it.buildingUsageName }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item{Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                // Handle form submission
                if (selectedBuildingTypes?.id != null && selectedBuildingUsages?.id != null) {
                    Log.d("BuildingForm", "Building Type ID: ${selectedBuildingTypes?.id}, Building Usage ID: ${selectedBuildingUsages?.id}")
                    viewModel.insertBuildings(Buildings(
                        name = name,
                        ownerName = ownerName,
                        phone = phone,
                        email = email,
                        nationalCode = nationalCode,
                        postCode = postCode,
                        address = address,
                        fundNumber = address,
                        currentBalance = address,
                        buildingTypeId = selectedBuildingTypes?.id!!,
                        buildingUsageId = selectedBuildingUsages?.id!!
                    ))
                    // Optionally, navigate back or clear the form
                    (context as? BuildingFormActivity)?.finish()
                } else {
                    Log.e("BuildingForm", "Building Type or Building Usage is null")
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
            )) {
            Text(
                text = context.getString(R.string.insert),
                modifier = Modifier.padding(2.dp),
                style = MaterialTheme.typography.titleLarge
            )
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
                    text = { Text(itemLabel(item)) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}