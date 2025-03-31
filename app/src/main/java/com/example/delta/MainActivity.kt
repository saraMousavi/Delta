package com.example.delta

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.delta.ui.theme.DeltaTheme
import androidx.compose.runtime.remember

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.modifier.modifierLocalOf
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Typeface
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            DeltaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Sara",
                        modifier = Modifier.padding(innerPadding),
                        fontSize = 24.sp
                    )


//                    CheckboxMinimalExample(Modifier.padding(32.dp))
                    SimpleOutlinedTextFieldSample(name= getString(R.string.create_building ),
                        modifier = Modifier.padding(16.dp, 125.dp))
                    SimpleOutlinedTextFieldSample(name="Last Name",
                        modifier = Modifier.padding(16.dp, 210.dp))

                    FilledButtonExample (name = "Submit",
                        modifier = Modifier.padding(64.dp, 300.dp),
                        onClick = { Log.d("Filled tonal button", "Filled tonal button clicked.") })
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DeltaTheme {
    }
}


val firaSansFamily = FontFamily(
    Font(R.font.yekan, FontWeight.Light)
)

