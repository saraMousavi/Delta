package com.example.delta

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.LayoutDirection
import com.example.delta.data.entity.User
import com.example.delta.viewmodel.BuildingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginPage : ComponentActivity() {
    val viewModel: BuildingsViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    LoginPageForm(viewModel)
                }
            }
        }
    }
}

// Example of a simple login function
fun convertToPersianDigits(input: String): String {
    val persianDigits = listOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    val builder = StringBuilder()
    for (char in input) {
        if (char.isDigit()) {
            val digit = char.toString().toInt()
            builder.append(persianDigits[digit])
        } else {
            builder.append(char) // keep non-digit characters as is
        }
    }
    return builder.toString()
}

fun login(username: String, password: String): User? {
    // Simulate backend authentication
    val phoneNumber = "09103009458"
    val persianPhoneNumber = convertToPersianDigits(phoneNumber)

    val pass = "1234"
    val persianPassword = convertToPersianDigits(pass)
    val users = listOf(
        User(1, persianPhoneNumber, persianPassword, "owner"),
        User(2, "tenant", "password", "tenant"),
        User(3, "manager", "password", "manager"),
        User(4, "guest", "password", "guest")
    )

    return users.find { it.mobileNumber == username && it.password == password }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginPageForm(buildingsViewModel: BuildingsViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = context.getString(R.string.login_user),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = {
                    Text(
                        text = context.getString(R.string.mobile_number),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            PasswordTextField(
                password = password,
                onPasswordChange = { password = it },
                context = context
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch(Dispatchers.IO) { // Launch in IO dispatcher
                        handleLogin(context, username, password, buildingsViewModel)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    context.getString(R.string.login),
                    modifier = Modifier.padding(2.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                buildAnnotatedString {
                    append(context.getString(R.string.not_account))
                    withLink(
                        LinkAnnotation.Url(
                            url = "guest",
                            styles = TextLinkStyles(
                                style = SpanStyle(color = Color.Blue)
                            ),
                            linkInteractionListener = {
                                // Navigate to guest page
                                val intent = Intent(context, GuestActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                context.startActivity(intent)
                            }
                        )
                    ) {
                        append(context.getString(R.string.guest_entrance))
                    }
                }
            )
        }
    }
}

// Example of handling login and role assignment
fun handleLogin(
    context: Context,
    username: String,
    password: String,
    buildingsViewModel: BuildingsViewModel
) {
    val user = login(username, password)


    if (user != null) {
        // Save user role locally
        val sharedPref = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        with((sharedPref.edit())) {
            putString("role", user.role)
            apply()
        }

        // Navigate to appropriate activity based on role
        when (user.role) {
            "owner" -> {
                checkBuildingList(context, buildingsViewModel)
            }
            "tenant" -> {
//                navigateToActivity(context, TenantActivity::class.java)
            }
            "manager" -> {
//                navigateToActivity(context, ManagerActivity::class.java)
            }
            "guest" -> {
                navigateToActivity(context, GuestActivity::class.java)
            }
        }
    } else {
        Log.i("LoginDebug", "username111: $username")
        Log.i("LoginDebug", "password111: $password")
        Log.i("LoginDebug", "user111: ${user?.mobileNumber}")
        CoroutineScope(Dispatchers.Main).launch {
            // Handle invalid login
            Toast.makeText(
                context,
                context.getString(R.string.invalid_username),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

fun checkBuildingList(context: Context, buildingsViewModel: BuildingsViewModel) {
    CoroutineScope(Dispatchers.IO).launch {
        // Simulate checking if there are buildings for the user
        val buildings = buildingsViewModel.getAllBuildingsList()
        withContext(Dispatchers.Main) {
            Log.d("buildings.isEmpty()", buildings.isEmpty().toString())
            if (buildings.isEmpty()) {
                navigateToActivity(context, BuildingFormActivity::class.java)
            } else {
                navigateToActivity(context, HomePageActivity::class.java)
            }
        }
    }
}

fun navigateToActivity(context: Context, activityClass: Class<*>) {
    val intent = Intent(context, activityClass)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    context.startActivity(intent)
    if (context is Activity) {
        context.finish()
    }
}
