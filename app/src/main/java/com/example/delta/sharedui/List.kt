package com.example.delta.sharedui

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.delta.R
import com.example.delta.data.entity.Debts
import com.example.delta.formatNumberWithCommas


@Composable
fun DebtItem(debt: Debts, onPayment: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${LocalContext.current.getString(R.string.title)}: ${debt.description}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "${LocalContext.current.getString(R.string.amount)}: ${
                            formatNumberWithCommas(
                                debt.amount
                            )
                        }",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${LocalContext.current.getString(R.string.due)}: ${debt.dueDate}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            Log.d("debt.paymentFlag", debt.paymentFlag.toString())
            if (debt.paymentFlag) {
                Text(
                    text = LocalContext.current.getString(R.string.payment_done),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Button(onClick = onPayment) {
                    Text(
                        text = LocalContext.current.getString(R.string.payment),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
