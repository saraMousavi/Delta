package com.example.delta

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.core.content.FileProvider
import com.example.delta.data.entity.UserRoleCrossRef
import com.example.delta.enums.Roles
import com.example.delta.init.FileManagement
import com.example.delta.viewmodel.SharedViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class GuestActivity : ComponentActivity() {
    val sharedViewModel: SharedViewModel by viewModels()
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    var context = LocalContext.current
                    Scaffold(
                       topBar = {
                           CenterAlignedTopAppBar(
                               title = {
                                   Text(
                                       text = getString(R.string.guest_display),
                                       style = MaterialTheme.typography.bodyLarge
                                   )
                               },
                               navigationIcon = {
                                   IconButton(onClick = { startActivity(Intent(context, LoginPage::class.java)) }) {
                                       Icon(
                                           Icons.AutoMirrored.Filled.ArrowBack,
                                           contentDescription = "Back"
                                       )
                                   }
                               }
                           )
                       }
                    ) { innerPadding ->
                           GuestScreen(Modifier.padding(innerPadding), sharedViewModel)
                       }
                }
            }
        }
    }
}


@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestScreen(modifier: Modifier, sharedViewModel: SharedViewModel) {
    var showRoleDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val thisActivity = LocalContext.current as Activity

    val user = sharedViewModel.getUserByRoleId(6L).collectAsState(initial = null)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = context.getString(R.string.guest_entrance),
                        style = MaterialTheme.typography.bodyLarge
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
            modifier = modifier
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
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FeatureItem(
                    icon = Icons.Default.Receipt,
                    text = context.getString(R.string.building_charge),
                    onClick = { /* Handle feature if needed */ }
                )
                FeatureItem(
                    icon = Icons.Default.AttachMoney,
                    text = context.getString(R.string.cost_managing),
                    onClick = { /* Handle feature if needed */ }
                )
                FeatureItem(
                    icon = Icons.Default.BarChart,
                    text = context.getString(R.string.reporting),
                    onClick = { /* Handle feature if needed */ }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Guest message
            Text(
                text = context.getString(R.string.guest_message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // دکمه انتخاب نقش کاربر
            Button(
                onClick = { showRoleDialog = true },
                modifier = Modifier.fillMaxWidth(0.7f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = context.getString(R.string.select_role),
                    style = MaterialTheme.typography.bodyLarge)
            }
        }

        if (showRoleDialog) {

            RoleSelectionDialog(
                onDismiss = { showRoleDialog = false },
                onRoleSelected = { selectedRole ->
                    showRoleDialog = false

                    when (selectedRole) {
                        Roles.GUEST_BUILDING_MANAGER -> {
                            //@todo check it to be done once
                            val guestBuilding = sharedViewModel.getBuildingsForUser(user.value!!.userId)
                            saveLoginState(thisActivity, true, userId = user.value!!.userId, mobile = user.value!!.mobileNumber)
                            //Insert Sample Building for guest
                            Log.d("userid", user.value!!.userId.toString())
                            val inputStream: InputStream = thisActivity.resources.openRawResource(R.raw.export_delta_template_guest)
                            val file = File(thisActivity.cacheDir, "export_delta_template_guest.xlsx")
                            inputStream.use { input ->
                                FileOutputStream(file).use { output ->
                                    input.copyTo(output)
                                }
                            }

                            val fileUri = FileProvider.getUriForFile(
                                thisActivity,
                                "${thisActivity.packageName}.fileprovider",
                                file
                            )
                            val selectFileIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                                type = "*/*" // یا "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                addCategory(Intent.CATEGORY_OPENABLE)
                            }
                            val uri = selectFileIntent.data
                            Log.d("fileUri", fileUri.toString())
                            if(fileUri != null) {
                                val inputStream1 = context.contentResolver.openInputStream(fileUri)
                                Log.d("inputStream ", inputStream1.toString())
                                if (inputStream1 != null) {
                                    FileManagement().handleExcelFile(
                                        inputStream1,
                                        thisActivity,
                                        sharedViewModel
                                    )
                                    inputStream1.close()
                                } else {
                                    Toast.makeText(thisActivity, "Unable to open selected file", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                        }
                        Roles.GUEST_COMPLEX_MANAGER -> {
//                            context.startActivity(Intent(context, ComplexManagerHomeActivity::class.java))
                        }
                        Roles.GUEST_INDEPENDENT_USER -> {
//                            context.startActivity(Intent(context, IndependentUserHomeActivity::class.java))
                        }
                        else -> {

                        }
                    }
                }
            )
        }
    }
}


@Composable
fun RoleSelectionDialog(
    onDismiss: () -> Unit,
    onRoleSelected: (Roles) -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(context.getString(R.string.choose_role), style = MaterialTheme.typography.bodyLarge) },
        text = {
            Column {
                RoleOption(text = Roles.GUEST_BUILDING_MANAGER.getDisplayName(context), onClick = { onRoleSelected(Roles.GUEST_BUILDING_MANAGER) })
                RoleOption(text = Roles.GUEST_COMPLEX_MANAGER.getDisplayName(context), onClick = { onRoleSelected(Roles.GUEST_COMPLEX_MANAGER) })
                RoleOption(text = Roles.GUEST_INDEPENDENT_USER.getDisplayName(context), onClick = { onRoleSelected(Roles.GUEST_INDEPENDENT_USER) })
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(context.getString(R.string.cancel), style = MaterialTheme.typography.bodyLarge)
            }
        }
    )
}

@Composable
fun RoleOption(text: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge)
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
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}
