package com.example.delta

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.delta.data.entity.Credits
import com.example.delta.viewmodel.SharedViewModel

class EarningDetailActivity : ComponentActivity() {
    private val sharedViewModel: SharedViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val earningId = intent.getLongExtra(EXTRA_EARNING_ID, 0L)

        setContent {
            AppTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MaterialTheme {
                        EarningDetailScreen(
                            viewModel = sharedViewModel,
                            earningId = earningId,
                            onBack = { finish() })
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_EARNING_ID = "extra_earning_id"

        fun start(context: Context, earningId: Long) {
            val intent = Intent(context, EarningDetailActivity::class.java)
            intent.putExtra(EXTRA_EARNING_ID, earningId)
            context.startActivity(intent)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarningDetailScreen(
    viewModel: SharedViewModel,
    earningId: Long,
    onBack: () -> Unit
) {
    Log.d("earningId", earningId.toString())
    val earning by viewModel.getEarning(earningId).collectAsState(initial = null)
    val credits by viewModel.getCreditFromEarning(earningId).collectAsState(initial = emptyList())
    LaunchedEffect(credits) {
        viewModel.updateCredits(credits)
    }



    if (earning == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    val selectedCredits by viewModel.selectedCredits.collectAsState()
    val sumSelectedAmount by viewModel.sumSelectedAmount.collectAsState()
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${context.getString(R.string.earning_title)} : ${earning!!.earningsName}",
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
                            formatNumberWithCommas(
                                sumSelectedAmount
                            )
                        } ${LocalContext.current.getString(R.string.toman)}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Button(
                        enabled = selectedCredits.isNotEmpty(),
                        onClick = {
                            try {
                                viewModel.markSelectedAsReceived(earningId)
                                Toast.makeText(context,context.getString(R.string.transfer_to_operational),
                                    Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                Log.e("markError", e.message.toString())
                                Toast.makeText(context,context.getString(R.string.failed),
                                    Toast.LENGTH_LONG).show()
                            }
                        }
                    ) {
                        Text(
                            text = context.getString(R.string.receipt_money),
                            style = MaterialTheme.typography.bodyLarge
                        )
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(0.dp)
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
                Text(
                    text = credit.description,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${context.getString(R.string.due)}: ${credit.dueDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${formatNumberWithCommas(credit.amount)} ${context.getString(R.string.toman)}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = if (credit.receiptFlag == false) context.getString(R.string.not_receipt) else context.getString(
                    R.string.receipt
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = if (credit.receiptFlag == false) Color(context.getColor(R.color.Red)) else Color(
                    context.getColor(R.color.Green)
                )
            )
        }
    }
}

