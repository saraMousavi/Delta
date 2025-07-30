package com.example.delta

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.delta.data.entity.User
import com.example.delta.enums.Roles
import com.example.delta.init.Preference
import com.example.delta.viewmodel.SharedViewModel

class UserProfileActivity : ComponentActivity() {
    val sharedViewModel: SharedViewModel by viewModels()
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val context = LocalContext.current
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
                    )
                    { innerPadding ->
                          Column (modifier = Modifier.padding(innerPadding)){
                              val userId = Preference().getUserId(context = context)
                              val user by sharedViewModel.getUserById(userId).collectAsState(initial = null)
                              user?.let {
                                  UserProfileScreen(initialUser = user!!, onSave = {
                                      sharedViewModel.updateUser(user!!,
                                          onError = {
                                              Toast.makeText(context, context.getString(R.string.failed), Toast.LENGTH_SHORT).show()
                                          })
                                  })
                              }

                          }
                       }
                }
            }
        }
    }
}


@Composable
fun UserProfileScreen(
    initialUser: User,
    onSave: (User) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var userState by remember { mutableStateOf(initialUser) }
    val context = LocalContext.current
    val profilePhoto = userState.profilePhoto
    // For scrolling if content is large
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile photo with circle shape and border
                if (userState.profilePhoto != null && profilePhoto!!.isNotBlank()) {
                    // Use Coil/Glide or your preferred image loader composable
                    AsyncImage(
                        model = userState.profilePhoto,
                        contentDescription = stringResource(id = R.string.profile_image),
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = stringResource(id = R.string.profile_image),
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!isEditing) {
                    Text(
                        text = "${userState.firstName} ${userState.lastName}",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(userState.mobileNumber, style = MaterialTheme.typography.bodyMedium)
                    userState.email?.takeIf { it.isNotBlank() }?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(roleDisplayFromId(userState.roleId, context = context), style = MaterialTheme.typography.bodyMedium)
                    userState.nationalCode?.takeIf { it.isNotBlank() }?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${context.getString(R.string.national_code)}: $it", style = MaterialTheme.typography.bodyMedium)
                    }
                    userState.address?.takeIf { it.isNotBlank() }?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${context.getString(R.string.address)}: $it", style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { isEditing = true },
                        modifier = Modifier.fillMaxWidth(0.6f)
                    ) {
                        Text(text = stringResource(id = R.string.edit_profile), style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    // Editable fields with labels and some vertical spacing
                    UserProfileEditableField(
                        label = stringResource(id = R.string.first_name),
                        value = userState.firstName,
                        onValueChange = { userState = userState.copy(firstName = it) }
                    )
                    UserProfileEditableField(
                        label = stringResource(id = R.string.last_name),
                        value = userState.lastName,
                        onValueChange = { userState = userState.copy(lastName = it) }
                    )
                    UserProfileEditableField(
                        label = stringResource(id = R.string.email),
                        value = userState.email ?: "",
                        onValueChange = { userState = userState.copy(email = it) }
                    )
                    UserProfileEditableField(
                        label = stringResource(id = R.string.mobile_number),
                        value = userState.mobileNumber,
                        onValueChange = { userState = userState.copy(mobileNumber = it) }
                    )
                    UserProfileEditableField(
                        label = stringResource(id = R.string.national_code),
                        value = userState.nationalCode ?: "",
                        onValueChange = { userState = userState.copy(nationalCode = it) }
                    )
                    UserProfileEditableField(
                        label = stringResource(id = R.string.address),
                        value = userState.address ?: "",
                        onValueChange = { userState = userState.copy(address = it) }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(
                            onClick = {
                                isEditing = false
                                userState = initialUser // revert changes
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = stringResource(id = R.string.cancel))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = {
                                isEditing = false
                                onSave(userState)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = stringResource(id = R.string.insert), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserProfileEditableField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.bodyLarge) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    )
}

fun roleDisplayFromId(roleId: Long, context: Context): String = when (roleId) {
    1L -> Roles.ADMIN.getDisplayName(context = context)
    2L -> Roles.PROPERTY_OWNER.getDisplayName(context = context)
    3L -> Roles.PROPERTY_TENANT.getDisplayName(context = context)
    4L -> Roles.INDEPENDENT_USER.getDisplayName(context = context)
    5L -> Roles.GUEST_BUILDING_MANAGER.getDisplayName(context = context)
    6L -> Roles.GUEST_PROPERTY_OWNER.getDisplayName(context = context)
    7L -> Roles.GUEST_PROPERTY_TENANT.getDisplayName(context = context)
    else -> Roles.GUEST_INDEPENDENT_USER.getDisplayName(context = context)
}



