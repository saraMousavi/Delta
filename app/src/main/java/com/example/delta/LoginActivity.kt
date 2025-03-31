package com.example.delta

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.delta.ui.theme.DeltaTheme

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent{
            Column {
                    Greeting(
                        name = getString(R.string.enter),
                        modifier = Modifier.padding(top = 150.dp, start = 90.dp),
                        fontSize = 48.sp
                    )
                SimpleOutlinedTextFieldSample(name= getString(R.string.phone_number ),
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .padding(vertical = 16.dp, horizontal = 20.dp))

                FilledButtonExample (name = getString(R.string.submit),
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .padding(vertical = 16.dp, horizontal =  20.dp),
                    onClick = { switchActivity() })
            }
        }
    }

    fun switchActivity(){
        val navigate = Intent(this, AuthActivity::class.java)
        startActivity(navigate)
    }
}

