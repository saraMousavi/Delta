package com.example.delta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class GuestActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    GuestScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestScreen() {
    var mobileNumber by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = context.getString(R.string.guest_entrance),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Banner
            Image(
                painter = painterResource(R.drawable.banner),
                contentDescription = "Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.height(24.dp))


            Spacer(modifier = Modifier.height(32.dp))

            // Features section
            Text(
                text = context.getString(R.string.features),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FeatureItem(
                    icon = Icons.Default.Receipt,  // Changed to more appropriate icon
                    text = context.getString(R.string.building_charge),
                    onClick = { /* Handle feature */ }
                )
                FeatureItem(
                    icon = Icons.Default.AttachMoney,  // Changed to money icon
                    text = context.getString(R.string.cost_managing),
                    onClick = { /* Handle feature */ }
                )
                FeatureItem(
                    icon = Icons.Default.BarChart,  // Changed to chart icon
                    text = context.getString(R.string.reporting),
                    onClick = { /* Handle feature */ }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Guest message
            Text(
                text = context.getString(R.string.guest_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun FeatureItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Add to your strings.xml:
// <string name="guest_message">Sign in to access all features and manage your properties</string>
