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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.delta.data.entity.Income
import com.example.delta.factory.IncomeViewModelFactory
import java.time.LocalDate

class IncomeActivity : ComponentActivity() {
    private val viewModel: IncomeViewModel by viewModels {
        IncomeViewModelFactory(application = this.application)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppTheme {
                val incomes by viewModel.getAllIncome().collectAsState(initial = emptyList())
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text( text = getString(R.string.income_list) ,
                                style = MaterialTheme.typography.bodyLarge) },
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
                            viewModel.insertIncome(Income(buildingId = 0 , incomeName = name, amount = 0.0, currency = name))
                        },
                        listContent = { vm ->
                            GenericList(
                                viewModel = vm,
                                items = incomes,
                                itemContent = { item ->
                                    GenericItem(
                                        item = item,
                                        itemName = { (it as Income).incomeName })
                                },
                                onDeleteItem = { item ->
                                    vm.deleteIncome(item)
                                }
                            )
                        },
                        contextString = R.string.income_list,
                        onFabClick = {}
                    )
                }
            }

        }
    }

}




