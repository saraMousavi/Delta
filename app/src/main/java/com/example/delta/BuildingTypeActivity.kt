package com.example.delta

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.delta.viewmodel.BuildingTypeViewModel
import com.example.delta.viewmodel.SharedViewModel
import com.example.delta.volley.BuildingType
import kotlinx.coroutines.launch

class BuildingTypeActivity : ComponentActivity() {

    private val viewModel: BuildingTypeViewModel by viewModels()
    val sharedViewModel: SharedViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            val buildingTypes by viewModel.getAllBuildingType()
                .collectAsState(initial = emptyList())

            LaunchedEffect(Unit) {
                try {
                    val remoteList = BuildingType().fetchAllSuspend(context)
                    remoteList.forEach { t ->
                        viewModel.insertBuildingType(t)
                    }
                } catch (_: Exception) {}
            }

            AppTheme(useDarkTheme = sharedViewModel.isDarkModeEnabled) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {

                    Scaffold(
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Text(
                                        text = getString(R.string.building_type_list),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                navigationIcon = {
                                    IconButton(onClick = { finish() }) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back"
                                        )
                                    }
                                }
                            )
                        }
                    ) { innerPadding ->
                        CostForm(
                            sharedViewModel = sharedViewModel,
                            viewModel = viewModel,
                            insertItem = { name ->

                                scope.launch {
                                    try {
                                        val created = BuildingType()
                                            .createBuildingTypeSuspend(context, name)

                                        if (created != null) {
                                            viewModel.insertBuildingType(created)
                                        }
                                    } catch (_: Exception) {}
                                }
                            },

                            listContent = { vm ->
                                GenericList(
                                    sharedViewModel = sharedViewModel,
                                    items = buildingTypes,
                                    itemContent = { item ->
                                        GenericItem(
                                            sharedViewModel = sharedViewModel,
                                            item = item,
                                            itemName = { it.buildingTypeName }
                                        )
                                    },
                                    onDeleteItem = { item ->
                                        vm.deleteBuildingType(item)
                                        BuildingType().deleteBuildingType(context, item.buildingTypeId, onError = {}, onSuccess = {})
                                    }
                                )
                            },

                            contextString = R.string.building_type_list
                        )
                    }
                }
            }
        }
    }
}
