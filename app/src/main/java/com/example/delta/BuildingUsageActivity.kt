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
import com.example.delta.viewmodel.BuildingUsageViewModel
import com.example.delta.viewmodel.SharedViewModel
import com.example.delta.volley.BuildingUsage
import kotlinx.coroutines.launch

class BuildingUsageActivity : ComponentActivity() {

    private val viewModel: BuildingUsageViewModel by viewModels()
    val sharedViewModel: SharedViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {

            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            val usages by viewModel.getAllBuildingUsage()
                .collectAsState(initial = emptyList())

            LaunchedEffect(Unit) {
                try {
                    val remoteList = BuildingUsage().fetchAllSuspend(context)
                    remoteList.forEach { u ->
                        viewModel.insertBuildingUsage(u)
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
                                        text = getString(R.string.building_usage_list),
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
                                        val created =
                                            BuildingUsage().createBuildingUsageSuspend(context, name)
                                        if (created != null) {
                                            viewModel.insertBuildingUsage(created)
                                        }
                                    } catch (_: Exception) {}
                                }
                            },

                            listContent = { vm ->
                                GenericList(
                                    sharedViewModel = sharedViewModel,
                                    items = usages,
                                    itemContent = { item ->
                                        GenericItem(
                                            sharedViewModel = sharedViewModel,
                                            item = item,
                                            itemName = { it.buildingUsageName }
                                        )
                                    },
                                    onDeleteItem = { item ->
                                        vm.deleteBuildingUsage(item)
                                        BuildingUsage().deleteBuildingUsage(context = context,
                                            buildingUsageId = item.buildingUsageId,
                                            onError = {

                                            },
                                            onSuccess = {

                                            })
                                    }
                                )
                            },

                            contextString = R.string.building_usage_list
                        )
                    }
                }
            }
        }
    }
}
