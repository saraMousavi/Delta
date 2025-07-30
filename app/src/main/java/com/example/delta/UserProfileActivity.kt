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

class UserProfileActivity : ComponentActivity() {
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
                    )
                    { innerPadding ->
                          Column (modifier = Modifier.padding(innerPadding)){

                          }
                       }
                }
            }
        }
    }
}
