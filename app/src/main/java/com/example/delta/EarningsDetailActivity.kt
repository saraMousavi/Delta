package com.example.delta

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.delta.data.entity.Credits
import com.example.delta.viewmodel.SharedViewModel
import kotlinx.coroutines.launch

class EarningDetailActivity : ComponentActivity() {
    private val sharedViewModel: SharedViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val earningId = intent.getLongExtra(EXTRA_EARNING_ID, 0L)
        val buildingId = intent.getLongExtra(EXTRA_BUILDING_ID, 0L)

        setContent {
            AppTheme (useDarkTheme = sharedViewModel.isDarkModeEnabled){
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MaterialTheme {
                        EarningDetailScreen(
                            viewModel = sharedViewModel,
                            earningId = earningId,
                            buildingId = buildingId,
                            onBack = { finish() })
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_EARNING_ID = "extra_earning_id"
        const val EXTRA_BUILDING_ID = "extra_building_id"

        fun start(context: Context, earningId: Long, buildingId : Long) {
            val intent = Intent(context, EarningDetailActivity::class.java)
            intent.putExtra(EXTRA_EARNING_ID, earningId)
            Log.d("putbuildingId", buildingId.toString())
            intent.putExtra(EXTRA_BUILDING_ID, buildingId)
            context.startActivity(intent)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarningDetailScreen(
    viewModel: SharedViewModel,
    earningId: Long,
    buildingId: Long,
    onBack: () -> Unit
) {
    val earning by viewModel.earningDetail.collectAsState()
    val credits by viewModel.earningCredits.collectAsState()
    val selectedCredits by viewModel.selectedCredits.collectAsState()
    val sumSelectedAmount by viewModel.sumSelectedAmount.collectAsState()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(earningId) {
        viewModel.loadEarningDetail(context, earningId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${context.getString(R.string.earning_title)} : ${earning?.earningsName}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${context.getString(R.string.amount)}: ${
                            formatNumberWithCommas(sumSelectedAmount)
                        } ${context.getString(R.string.toman)}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Button(
                        enabled = selectedCredits.isNotEmpty(),
                        onClick = {
                            coroutineScope.launch {
                                viewModel.markSelectedAsReceived(
                                    context,
                                    earningId,
                                    buildingId,
                                    onSuccess = {
                                        coroutineScope.launch {
                                            viewModel.loadFundBalances(context, buildingId)
                                            snackbarHostState.showSnackbar(
                                                context.getString(R.string.transfer_to_capital)
                                            )
                                        }
                                    },
                                    onError = {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                context.getString(R.string.failed)
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    ) {
                        Text(text = context.getString(R.string.receipt_money), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        modifier = Modifier.padding(8.dp)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            item {
                val selectableCredits = credits.filter { it.receiptFlag != true }
                val allSelected = selectedCredits.size == selectableCredits.size && selectableCredits.isNotEmpty()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = allSelected,
                        onCheckedChange = { checked ->
                            if (checked) {
                                viewModel.setSelectedCredits(selectableCredits.map { it.creditsId }.toSet())
                            } else {
                                viewModel.setSelectedCredits(emptySet())
                            }
                        }
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = LocalContext.current.getString(R.string.select_all),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            items(credits) { credit ->
                CreditListItem(
                    credit = credit,
                    checked = selectedCredits.contains(credit.creditsId),
                    enabled = credit.receiptFlag != true,
                    onCheckedChange = { viewModel.toggleCreditSelection(credit.creditsId) }
                )
            }
        }

    }
}

@Composable
fun CreditListItem(
    credit: Credits,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val context = LocalContext.current

    val cardColor =
        if (credit.receiptFlag == true)
            Color(0xFF4CAF50)
        else
            MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = if (enabled) onCheckedChange else null,
                enabled = enabled
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = credit.description, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "${context.getString(R.string.due)}: ${credit.dueDate}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = "${formatNumberWithCommas(credit.amount)} ${context.getString(R.string.toman)}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(end = 8.dp)
            )

            Text(
                text = if (credit.receiptFlag == true)
                    context.getString(R.string.receipt)
                else
                    context.getString(R.string.not_receipt),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

