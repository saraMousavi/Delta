package com.example.delta

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.delta.viewmodel.CostViewModel
import com.example.delta.factory.CostViewModelFactory
import com.example.delta.viewmodel.SharedViewModel

class CostActivity : ComponentActivity() {
    private val viewModel: CostViewModel by viewModels {
        CostViewModelFactory(application = this.application)
    }
    val sharedViewModel: SharedViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val costs by viewModel.getAllMenuCost().collectAsState(initial = emptyList())
            AppTheme (useDarkTheme = sharedViewModel.isDarkModeEnabled){
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = getString(R.string.cost_list),
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
//                            viewModel.insertCost(Costs(buildingId = 0 , costName = name, amount = 0.0  , period = "0", amountUnit = "1", currency = name))
                        },
                        listContent = { vm ->
                            GenericList(
                                sharedViewModel = sharedViewModel,
                                items = costs,
                                itemContent = { item ->
                                    GenericItem(sharedViewModel = sharedViewModel, item = item, itemName = { (it).costName })
                                },
                                onDeleteItem = { item ->
                                    vm.deleteCost(item)
                                }
                            )
                        },
                        contextString = R.string.cost_list
                    )
                }
            }

            }
        }
    }

}