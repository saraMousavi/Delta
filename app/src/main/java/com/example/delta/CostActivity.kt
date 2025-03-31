package com.example.delta

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
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
import com.example.delta.CostForm
import com.example.delta.GenericItem
import com.example.delta.GenericList
import com.example.delta.CostViewModel
import com.example.delta.R
import com.example.delta.data.entity.Cost
import com.example.delta.factory.CostViewModelFactory
import java.time.LocalDate
import java.util.Date

class CostActivity : ComponentActivity() {
    private val viewModel: CostViewModel by viewModels {
        CostViewModelFactory(application = this.application)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val costs by viewModel.getAllCost().collectAsState(initial = emptyList())
            AppTheme {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text( text = getString(R.string.cost_list) , style = MaterialTheme.typography.bodyLarge) },
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
                            viewModel.insertCost(Cost(buildingId = 0 , costName = name, amount = 0.0  , currency = name))
                        },
                        listContent = { vm ->
                            GenericList(
                                viewModel = vm,
                                items = costs,
                                itemContent = { item ->
                                    GenericItem(item = item, itemName = { (it as Cost).costName })
                                },
                                onDeleteItem = { item ->
                                    vm.deleteCost(item)
                                }
                            )
                        },
                        contextString = R.string.cost_list,
                        onFabClick = {}
                    )
                }

            }
        }
    }

}