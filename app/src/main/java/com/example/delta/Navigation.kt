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
import androidx.core.content.edit

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
                        style = MaterialTheme.typography.bodyLarge
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
                        label = { Text(context.getString(R.string.logout)) },
                        selected = false,
                        onClick = { val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                            prefs.edit() { putBoolean("is_logged_in", false) }
                            context.startActivity(Intent(context, LoginPage::class.java))
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                    NavigationDrawerItem(
                        label = { Text(context.getString(R.string.app_version)) },
                        selected = false,
                        onClick = {
                        }
                    )
                    Spacer(Modifier.height(12.dp))

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

