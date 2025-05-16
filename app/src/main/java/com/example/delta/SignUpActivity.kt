package com.example.delta

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.delta.data.entity.Role
import com.example.delta.data.entity.User
import com.example.delta.init.Validation
import com.example.delta.viewmodel.SharedViewModel
import kotlin.getValue

class SignUpActivity : ComponentActivity() {
    val sharedViewModel: SharedViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    SignUpScreen(
                        onSignUpSuccess = {
                            // Go to BuildingFormActivity after successful sign up
                            saveLoginState(this, true)
                            val intent = Intent(this, BuildingFormActivity::class.java)
                            startActivity(intent)
                            finish()
                        },
                        sharedViewModel = sharedViewModel
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    sharedViewModel: SharedViewModel
) {
    var mobile by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mobileError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val userRoles = listOf(
        Role(2L, context.getString(R.string.owner), ""),      // Owner
        Role(3L, context.getString(R.string.tenant), ""),    // Tenant
        Role(4L, context.getString(R.string.manager), "")       // Manager
    )

    var selectedRole by remember { mutableStateOf<Role?>(null) }


    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = context.getString(R.string.sign_up),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.shadow(4.dp),
            navigationIcon = {
                IconButton(onClick = {
                    (context as? SignUpActivity)?.finish()
                }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = context.getString(R.string.back)
                    )
                }
            }
        )
        Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
        ){
            OutlinedTextField(
                value = mobile,
                onValueChange = {
                    mobile = it
                    mobileError = !Validation().isValidIranMobile(it)
                },
                label = { Text(stringResource(R.string.mobile_number)) },
                isError = mobileError,
                modifier = Modifier.fillMaxWidth()
            )
            if (mobileError) {
                Text(
                    text = stringResource(R.string.invalid_mobile_number),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Start)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = it.length < 6
                },
                label = { Text(stringResource(R.string.password)) },
                isError = passwordError,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )
            if (passwordError) {
                Text(
                    text = stringResource(R.string.password_too_short),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Start)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBoxExample(
                items = userRoles,
                selectedItem = selectedRole,
                onItemSelected = { role -> selectedRole = role },
                label = context.getString(R.string.role), // Add this to your strings.xml
                modifier = Modifier.fillMaxWidth(),
                itemLabel = { role -> role.roleName }
            )

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    // Save user logic here (e.g., insert into database)
                    // Example:
                    // viewModel.insertUser(User(mobile = mobile, password = password))
                    sharedViewModel.insertUser(
                        User(
                            mobileNumber = mobile,
                            password = password,
                            roleId = selectedRole?.roleId ?: 1L
                        )
                    )
                    onSignUpSuccess()
                },
                enabled = !mobileError && !passwordError && mobile.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.sign_up), style= MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

