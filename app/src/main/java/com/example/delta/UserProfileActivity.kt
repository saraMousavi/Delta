package com.example.delta

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.delta.enums.Gender
import com.example.delta.enums.Roles
import com.example.delta.init.Preference
import com.example.delta.viewmodel.SharedViewModel

class UserProfileActivity : ComponentActivity() {
    val sharedViewModel: SharedViewModel by viewModels()
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme (useDarkTheme = sharedViewModel.isDarkModeEnabled){
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
                                  UserProfileScreen(sharedViewModel = sharedViewModel, initialUser = user!!, onSave = {
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
    sharedViewModel: SharedViewModel,
    initialUser: User,
    onSave: (User) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var userState by remember { mutableStateOf(initialUser) }
    val context = LocalContext.current
    val profilePhoto = userState.profilePhoto

    // Helper to show text or placeholder
    fun displayValue(value: String?): String = value?.takeIf { it.isNotBlank() } ?: "--"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Image Section
                if (!profilePhoto.isNullOrBlank()) {
                    AsyncImage(
                        model = profilePhoto,
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
                    // Display all fields with dividers and consistent spacing
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Full Name
                        UserProfileDisplayRow(
                            label = stringResource(R.string.full_name),
                            value = "${displayValue(userState.firstName)} ${displayValue(userState.lastName)}"
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        UserProfileDisplayRow(
                            label = stringResource(R.string.mobile_number),
                            value = displayValue(userState.mobileNumber)
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        UserProfileDisplayRow(
                            label = stringResource(R.string.email),
                            value = displayValue(userState.email)
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        UserProfileDisplayRow(
                            label = stringResource(R.string.gender),
                            value = displayValue(
                                userState.gender?.getDisplayName(context)
                                    ?: Gender.FEMALE.getDisplayName(context)
                            )
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        UserProfileDisplayRow(
                            label = stringResource(R.string.role),
                            value = roleDisplayFromId(userState.roleId, context = context)
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        UserProfileDisplayRow(
                            label = stringResource(R.string.national_code),
                            value = displayValue(userState.nationalCode)
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        UserProfileDisplayRow(
                            label = stringResource(R.string.address),
                            value = displayValue(userState.address)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Edit button at the bottom
                    Button(
                        onClick = { isEditing = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                    ) {
                        Text(text = stringResource(id = R.string.edit_profile), style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    // Editable fields (your existing implementation unchanged)
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
                    GenderDropdown(
                        sharedViewModel = sharedViewModel,
                        selectedGender = userState.gender?.let {
                            Gender.fromDisplayName(
                                context,
                                it.getDisplayName(context)
                            )
                        },
                        onGenderSelected = { gender ->
                            userState = userState.copy(gender = gender)
                        },
                        label = stringResource(id = R.string.gender),
                        modifier = Modifier.fillMaxWidth()
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
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.cancel),
                                style = MaterialTheme.typography.bodyLarge
                            )
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

@Composable
private fun UserProfileDisplayRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenderDropdown(
    sharedViewModel: SharedViewModel,
    selectedGender: Gender?,
    onGenderSelected: (Gender) -> Unit,
    modifier: Modifier = Modifier,
    label: String
) {
    val context = LocalContext.current

    ExposedDropdownMenuBoxExample(
        sharedViewModel = sharedViewModel,
        items = Gender.entries.toList(),
        selectedItem = selectedGender,
        onItemSelected = { selected ->
            onGenderSelected(selected)
        },
        label = label,
        modifier = modifier,
        itemLabel = { it.getDisplayName(context) }
    )
}





