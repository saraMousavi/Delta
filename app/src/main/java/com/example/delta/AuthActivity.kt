package com.example.delta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Column {
                Greeting(
                    name = getString(R.string.auth),
                    modifier = Modifier.
                    padding(top= 150.dp, start = 90.dp),
                    fontSize = 48.sp,
                )
                Row {
                    Spacer(Modifier.padding(start = 40.dp))
                    SimpleOutlinedTextFieldSample(name= "",
                        modifier = Modifier
                            .fillMaxWidth(0.1f)
                            .padding(vertical = 16.dp, horizontal = 5.dp))
                    SimpleOutlinedTextFieldSample(name= "",
                        modifier = Modifier
                            .fillMaxWidth(0.2f)
                            .padding(vertical = 16.dp, horizontal = 5.dp))
                    SimpleOutlinedTextFieldSample(name= "",
                        modifier = Modifier
                            .fillMaxWidth(0.3f)
                            .padding(vertical = 16.dp, horizontal = 5.dp))
                    SimpleOutlinedTextFieldSample(name= "",
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .padding(vertical = 16.dp, horizontal = 5.dp))
                }
                FilledButtonExample (name = getString(R.string.submit),
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .padding(vertical = 16.dp, horizontal =  20.dp),
                    onClick = {  })
            }
        }
    }
}