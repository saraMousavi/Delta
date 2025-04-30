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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
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
        User(userId = 1L, mobileNumber = persianPhoneNumber, password = persianPassword, roleId = 1L), // Admin
        User(userId = 2L, mobileNumber = convertToPersianDigits("09123456789"), password = "password", roleId = 2L), // owner
        User(userId = 3L, mobileNumber = convertToPersianDigits("09134567890"), password = "password", roleId = 3L), // manager
        User(userId = 4L, mobileNumber = convertToPersianDigits("09145678901"), password = "password", roleId = 4L)  // tenant
    )


    return users.find { it.mobileNumber == username && it.password == password }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun LoginPageForm(buildingsViewModel: BuildingsViewModel) {
//    var username by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    val context = LocalContext.current
//    val scope = rememberCoroutineScope()
//
//    Scaffold(
//        topBar = {
//            CenterAlignedTopAppBar(
//                title = {
//                    Text(
//                        text = context.getString(R.string.login_user),
//                        style = MaterialTheme.typography.bodyLarge
//                    )
//                }
//            )
//        }
//    ) { innerPadding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(innerPadding)
//                .padding(16.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            OutlinedTextField(
//                value = username,
//                onValueChange = { username = it },
//                label = {
//                    Text(
//                        text = context.getString(R.string.mobile_number),
//                        style = MaterialTheme.typography.bodyLarge
//                    )
//                },
//                modifier = Modifier.fillMaxWidth()
//            )
//            Spacer(modifier = Modifier.height(8.dp))
//            PasswordTextField(
//                password = password,
//                onPasswordChange = { password = it },
//                context = context
//            )
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Button(
//                onClick = {
//                    scope.launch(Dispatchers.IO) { // Launch in IO dispatcher
//                        handleLogin(context, username, password, buildingsViewModel)
//                    }
//                },
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(context.getColor(R.color.secondary_color)) // Change button text color
//                ),
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text(
//                    context.getString(R.string.login),
//                    modifier = Modifier.padding(2.dp),
//                    style = MaterialTheme.typography.bodyLarge
//                )
//            }
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(
//                buildAnnotatedString {
//                    append(context.getString(R.string.not_account))
//                    withLink(
//                        LinkAnnotation.Url(
//                            url = "guest",
//                            styles = TextLinkStyles(
//                                style = SpanStyle(color = Color.Blue)
//                            ),
//                            linkInteractionListener = {
//                                // Navigate to guest page
//                                val intent = Intent(context, GuestActivity::class.java)
//                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                                context.startActivity(intent)
//                            }
//                        )
//                    ) {
//                        append(context.getString(R.string.guest_entrance))
//                    }
//                }
//            )
//        }
//    }
//}

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
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.shadow(4.dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(text = context.getString(R.string.mobile_number)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            Spacer(modifier = Modifier.height(16.dp))
            PasswordTextField(
                password = password,
                onPasswordChange = { password = it },
                context = context
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        handleLogin(context, username, password, buildingsViewModel)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = context.getString(R.string.login),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = buildAnnotatedString {
                    append(context.getString(R.string.not_account) + " ")
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append(context.getString(R.string.guest_entrance))
                    }
                },
                modifier = Modifier.clickable {
                    val intent = Intent(context, GuestActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    context.startActivity(intent)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun handleLogin(
    context: Context,
    username: String,
    password: String,
    buildingsViewModel: BuildingsViewModel
) {
    val user = login(username, password)

    if (user != null) {
        // Save userId and roleId locally
        val sharedPref = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().apply {
            putLong("user_id", user.userId)
            putLong("role_id", user.roleId)
            apply()
        }

        // Navigate to HomePageActivity after login
        Log.d("buildingsViewModel.getAllBuildingsList()", buildingsViewModel.getAllBuildingsList().toString())
        if(buildingsViewModel.getAllBuildingsList().isEmpty()){
            val intent = Intent(context, BuildingFormActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("user_id", user.userId)  // optional, if you want to pass userId explicitly
            context.startActivity(intent)
        } else {
            val intent = Intent(context, HomePageActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("user_id", user.userId)  // optional, if you want to pass userId explicitly
            context.startActivity(intent)
        }


        // If context is an Activity, finish it to prevent back navigation
        if (context is Activity) {
            context.finish()
        }
    } else {
        Log.i("LoginDebug", "Invalid login attempt: username=$username")
        CoroutineScope(Dispatchers.Main).launch {
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
