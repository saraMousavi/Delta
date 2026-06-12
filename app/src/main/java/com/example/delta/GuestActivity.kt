package com.example.delta

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.delta.data.dao.AuthorizationDao
import com.example.delta.data.dao.RoleDao
import com.example.delta.data.entity.Role
import com.example.delta.data.model.AppDatabase
import com.example.delta.enums.Roles
import com.example.delta.init.Preference
import com.example.delta.viewmodel.SharedViewModel
import kotlinx.coroutines.launch

private lateinit var roleDao: RoleDao
private lateinit var authorizationDao: AuthorizationDao

class GuestActivity : ComponentActivity() {
    val sharedViewModel: SharedViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = AppDatabase.getDatabase(this)

        roleDao = database.roleDao()
        authorizationDao = database.authorizationDao()

        setContent {
            AppTheme(useDarkTheme = sharedViewModel.isDarkModeEnabled) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val context = LocalContext.current

                    BackHandler {
                        context.startActivity(Intent(context, LoginPage::class.java))
                        (context as? Activity)?.finish()
                    }

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
                                    IconButton(onClick = {
                                        context.startActivity(Intent(context, LoginPage::class.java))
                                        (context as? Activity)?.finish()
                                    }) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back"
                                        )
                                    }
                                }
                            )
                        }
                    ) { innerPadding ->
                        GuestScreen(
                            modifier = Modifier.padding(innerPadding),
                            sharedViewModel = sharedViewModel
                        )
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
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showRoleDialog by remember { mutableStateOf(false) }
    var confirmedRole by remember { mutableStateOf<Roles?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val roles = listOf(
        Roles.GUEST_BUILDING_MANAGER,
        Roles.GUEST_PROPERTY_OWNER,
        Roles.GUEST_PROPERTY_TENANT
    )


    var selectedRole by remember { mutableStateOf<Roles?>(null) }

    val sliderImages = remember {
        listOf(
            R.drawable.banner_guest,
            R.drawable.banner_first,
            R.drawable.banner_second
        )
    }

    var sliderIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(confirmedRole) {
        confirmedRole?.let { role ->
            try {
                isLoading = true
                insertUserBasedOnRole(role, context)

                val activity = context as? Activity
                activity?.let { act ->
                    val intent = Intent(act, LoginPage::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    act.startActivity(intent)
                    act.finish()
                }
            } catch (e: Exception) {
                Log.e("GuestScreen", "Error inserting guest: $e")
            } finally {
                isLoading = false
            }
            confirmedRole = null
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GuestHeroCard(
                title = context.getString(R.string.guest_entrance),
                subtitle = context.getString(R.string.guest_message),
                onPrimaryClick = {
                    showRoleDialog = true },
                onSecondaryClick = {
                    context.startActivity(Intent(context, LoginPage::class.java))
                    (context as? Activity)?.finish()
                }
            )

            Spacer(Modifier.height(16.dp))

            ImageSlider(
                images = sliderImages,
                index = sliderIndex,
                onPrev = { sliderIndex = (sliderIndex - 1).coerceAtLeast(0) },
                onNext = { sliderIndex = (sliderIndex + 1).coerceAtMost(sliderImages.lastIndex) }
            )

            Spacer(Modifier.height(18.dp))

            SectionHeader(
                title = context.getString(R.string.features),
                subtitle = ""
            )

            Spacer(Modifier.height(10.dp))

            FeatureGrid(
                items = listOf(
                    FeatureUi(Icons.Default.HomeWork, context.getString(R.string.building_management)),
                    FeatureUi(Icons.Default.Groups, context.getString(R.string.members)),
                    FeatureUi(Icons.Default.AttachMoney, context.getString(R.string.cost_managing)),
                    FeatureUi(Icons.Default.ReceiptLong, context.getString(R.string.debts)),
                    FeatureUi(Icons.Default.Notifications, context.getString(R.string.title_notifications)),
                    FeatureUi(Icons.Default.BarChart, context.getString(R.string.reporting)),
                    FeatureUi(Icons.Default.Phone, context.getString(R.string.phone_book)),
                    FeatureUi(Icons.Default.Security, context.getString(R.string.permissions_management))
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(18.dp))

            HighlightRow(
                items = listOf(
                    HighlightUi(
                        icon = Icons.Default.Bolt,
                        title = context.getString(R.string.fast),
                        subtitle = context.getString(R.string.simple_and_fast_usage)
                    ),
                    HighlightUi(
                        icon = Icons.Default.Lock,
                        title = context.getString(R.string.secure),
                        subtitle = context.getString(R.string.safe_data)
                    ),
                    HighlightUi(
                        icon = Icons.Default.SupportAgent,
                        title = context.getString(R.string.support),
                        subtitle = context.getString(R.string.in_app_support)
                    )
                )
            )

            Spacer(Modifier.height(18.dp))

            CalloutCard(
                title = context.getString(R.string.try_guest),
                body = context.getString(R.string.guest_message),
                onClick = {
                    context.startActivity(Intent(context, LoginPage::class.java))
                    (context as? Activity)?.finish()
                }
            )

            Spacer(Modifier.height(18.dp))

            Button(
                onClick = { showRoleDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = context.getString(R.string.login_as_guest),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    context.startActivity(Intent(context, LoginPage::class.java))
                    (context as? Activity)?.finish()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = context.getString(R.string.back_to_login),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(Modifier.height(18.dp))
        }

        if (isLoading) {
            Dialog(onDismissRequest = {}) {
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        if (showRoleDialog) {
            RoleSelectionBottomSheet(
                sharedViewModel = sharedViewModel,
                roles = roles,
                initialSelectedRole = selectedRole,
                onDismissRequest = { showRoleDialog = false },
                onConfirm = { confirmedRoleSelected ->
                    confirmedRole = confirmedRoleSelected
                    selectedRole = confirmedRoleSelected
                    showRoleDialog = false
                }
            )
        }
    }
}

@Composable
private fun GuestHeroCard(
    title: String,
    subtitle: String,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onPrimaryClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(text = context.getString(R.string.guest_start), style = MaterialTheme.typography.bodyLarge)
                }
                OutlinedButton(
                    onClick = onSecondaryClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(text = context.getString(R.string.login_signup), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun ImageSlider(
    images: List<Int>,
    index: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
        ) {
            Image(
                painter = painterResource(images[index]),
                contentDescription = "Slider",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPrev,
                    enabled = index > 0,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Prev")
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(images.size) { i ->
                        Box(
                            modifier = Modifier
                                .size(if (i == index) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (i == index) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                )
                        )
                    }
                }

                IconButton(
                    onClick = onNext,
                    enabled = index < images.lastIndex,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Next")
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private data class FeatureUi(val icon: ImageVector, val title: String)

@Composable
private fun FeatureGrid(items: List<FeatureUi>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        val chunked = items.chunked(2)
        chunked.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { item ->
                    FeatureCard(
                        icon = item.icon,
                        title = item.title,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun FeatureCard(icon: ImageVector, title: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private data class HighlightUi(val icon: ImageVector, val title: String, val subtitle: String)

@Composable
private fun HighlightRow(items: List<HighlightUi>) {
    val s = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(s),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.forEach { item ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.width(240.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(text = item.title, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalloutCard(title: String, body: String, onClick: () -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f)
            )
            Spacer(Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = context.getString(R.string.login_signup)  ,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

private fun insertUserBasedOnRole(role: Roles, context: Context) {
    when (role) {
        Roles.GUEST_BUILDING_MANAGER -> {
            Preference().saveLoginState(
                context,
                true,
                userId = 3,
                mobile = "09111111111",
                roles = listOf(Role(roleId = 7L, roleName = "مدیر ساختمان(مهمان)", roleDescription = "مدیر ساختمان(مهمان)"))
            )
            saveFirstLoginState(context, true)
            Preference().setRoleId(context, 7L)
        }
        Roles.GUEST_PROPERTY_OWNER -> {
            Preference().saveLoginState(
                context,
                true,
                userId = 7,
                mobile = "09000000000",
                roles = listOf(Role(roleId = 9L, roleName = "مالک(مهمان)", roleDescription = "مالک(مهمان)"))
            )
            saveFirstLoginState(context, true)
            Preference().setRoleId(context, 9L)
        }
        Roles.GUEST_PROPERTY_TENANT -> {
            Preference().saveLoginState(
                context,
                true,
                userId = 5,
                mobile = "09888888888",
                roles = listOf(Role(roleId = 10L, roleName = "مستأجر(مهمان)", roleDescription = "مستأجر(مهمان)"))
            )
            saveFirstLoginState(context, true)
            Preference().setRoleId(context, 10L)
        }
        else -> Unit
    }
}

@Composable
fun FeatureItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(110.dp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSelectionBottomSheet(
    roles: List<Roles>,
    initialSelectedRole: Roles?,
    onDismissRequest: () -> Unit,
    onConfirm: (Roles) -> Unit,
    sharedViewModel: SharedViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedRole by remember { mutableStateOf(initialSelectedRole) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = context.getString(R.string.select_role_for_different_feature),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            ExposedDropdownMenuBoxExample(
                sharedViewModel = sharedViewModel,
                items = roles,
                selectedItem = selectedRole,
                onItemSelected = { selectedRole = it },
                label = context.getString(R.string.select_role),
                modifier = Modifier.fillMaxWidth(1f),
                itemLabel = { it.getDisplayName(context) }
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = {
                    coroutineScope.launch {
                        sheetState.hide()
                        onDismissRequest()
                    }
                }) {
                    Text(
                        text = context.getString(R.string.cancel),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                TextButton(
                    onClick = {
                        selectedRole?.let {
                            coroutineScope.launch {
                                sheetState.hide()
                                onConfirm(it)
                            }
                        }
                    },
                    enabled = selectedRole != null
                ) {
                    Text(
                        text = context.getString(R.string.confirm),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
