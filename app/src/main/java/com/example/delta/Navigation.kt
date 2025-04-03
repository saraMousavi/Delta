package com.example.delta

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailDrawer(
    title: String,
    imageId : Int,
    content: @Composable (PaddingValues) -> Unit
) {
    AppTheme {
    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center // Center the content within the Box
                    ) {
                        Image(
                            painter = painterResource(id = imageId),
                            contentDescription = "My Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(120.dp)
                                .clip(CircleShape) // Adjust size as needed
                        )
                    }
                    Spacer(Modifier.height(12.dp))

                    HorizontalDivider()
                    Text(
                        text = context.getString(R.string.first_sect),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                    NavigationDrawerItem(
                        label = { Text(context.getString(R.string.account)) },
                        selected = false,
                        onClick = {}
                    )
                    Spacer(Modifier.height(12.dp))
                    NavigationDrawerItem(
                        label = { Text(context.getString(R.string.customizing)) },
                        selected = false,
                        onClick = {}
                    )
                    Spacer(Modifier.height(12.dp))
                    NavigationDrawerItem(
                        label = { Text(context.getString(R.string.change_language)) },
                        selected = false,
                        onClick = {}
                    )
                    Spacer(Modifier.height(12.dp))
                    NavigationDrawerItem(
                        label = { Text(context.getString(R.string.dark_mode)) },
                        selected = false,
                        onClick = {}
                    )
                    Spacer(Modifier.height(12.dp))
                    NavigationDrawerItem(
                        label = { Text(context.getString(R.string.sharing)) },
                        selected = false,
                        onClick = {}
                    )
                    Spacer(Modifier.height(12.dp))
                    NavigationDrawerItem(
                        label = { Text(context.getString(R.string.app_info)) },
                        selected = false,
                        onClick = {}
                    )
                    Spacer(Modifier.height(12.dp))
                    NavigationDrawerItem(
                        label = { Text(context.getString(R.string.guide_active)) },
                        selected = false,
                        onClick = {}
                    )
                    Spacer(Modifier.height(12.dp))
                    NavigationDrawerItem(
                        label = { Text(context.getString(R.string.supporting)) },
                        selected = false,
                        onClick = {}
                    )
                    Spacer(Modifier.height(12.dp))
                    NavigationDrawerItem(
                        label = { Text(context.getString(R.string.app_version)) },
                        selected = false,
                        onClick = {}
                    )
                    Spacer(Modifier.height(12.dp))

                    HorizontalDivider()
                    Text(
                        text = context.getString(R.string.second_sect),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    NavigationDrawerItem(
                        label = { Text(context.getString(R.string.cost_list)) },
                        selected = false,
                        onClick = {
                            context.startActivity(Intent(context, CostActivity::class.java))
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                    NavigationDrawerItem(
                        label = { Text(context.getString(R.string.income_list)) },
                        selected = false,
                        onClick = {
                            context.startActivity(Intent(context, EarningsActivity::class.java))
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                    NavigationDrawerItem(
                        label = { Text(context.getString(R.string.building_type_list)) },
                        selected = false,
                        onClick = {
                            context.startActivity(Intent(context, BuildingTypeActivity::class.java))
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                    NavigationDrawerItem(
                        label = { Text(context.getString(R.string.building_usage_list)) },
                        selected = false,
                        onClick = {
                            context.startActivity(Intent(context, BuildingUsageActivity::class.java))
                        }
                    )
                }
            }
        },
        drawerState = drawerState
    ) {

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) {
                                    drawerState.open()
                                } else {
                                    drawerState.close()
                                }
                            }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { innerPadding ->
            content(innerPadding)
        }
    }
}
}
}

fun switchCostActivity(context: Context){
    val navigate = Intent(context, CostActivity::class.java)
    context.startActivity(navigate)
}

