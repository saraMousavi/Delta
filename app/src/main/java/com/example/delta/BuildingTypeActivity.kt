package com.example.delta

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.delta.data.entity.BuildingType

class BuildingTypeActivity : ComponentActivity() {

    private val viewModel: BuildingTypeViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val buildingTypes by viewModel.getAllBuildingType()
                .collectAsState(initial = emptyList())
            AppTheme {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text( text = getString(R.string.building_type_list) , style = MaterialTheme.typography.bodyLarge) },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    CostForm(
                        viewModel = viewModel,
                        insertItem = { name ->
                            viewModel.insertBuildingType(BuildingType(buildingTypeName = name))
                        },
                        listContent = { vm ->
                            GenericList(
                                viewModel = vm,
                                items = buildingTypes,
                                itemContent = { item ->
                                    GenericItem(
                                        item = item,
                                        itemName = { (it as BuildingType).buildingTypeName })
                                },
                                onDeleteItem = { item ->
                                    vm.deleteBuildingType(item)
                                }
                            )
                        },
                        contextString = R.string.building_type_list,
                        onFabClick = {}
                    )
                }

            }
        }
    }

}