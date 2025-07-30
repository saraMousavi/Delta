package com.example.delta

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.delta.init.FileManagement
import com.example.delta.screens.OnboardingScreen
import com.example.delta.viewmodel.SharedViewModel

class ImportOrManualActivity : ComponentActivity() {

    val sharedViewModel: SharedViewModel by viewModels()
    var buildingTypeName: String = ""
    var buildingUsageName: String = ""

    private val REQUEST_CODE_PICK_EXCEL = 1001

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_EXCEL && resultCode == RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    if (inputStream != null) {
                        FileManagement().handleExcelFile(inputStream, this, sharedViewModel)
                        inputStream.close()
                    } else {
                        Toast.makeText(this, "Unable to open selected file", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(this, "Error reading file: ${e.message}", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
        }
    }




    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MaterialTheme {
                        if (isFirstLoggedIn(this)) {
                            OnboardingScreen {
                                saveFirstLoginState(context = this, isFirstLoggedIn = false)
                                val intent = Intent(this, ImportOrManualActivity::class.java)
                                startActivity(intent)
                            }
                        } else {
                            MainScreen()
                        }
                    }
                }
            }
        }

    }

    @Composable
    fun MainScreen() {
        val context = LocalContext.current
        val thisActivity = this

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                // هدایت به BuildingFormActivity
                val intent = Intent(context, BuildingFormActivity::class.java)
                context.startActivity(intent)
            }, modifier = Modifier.fillMaxWidth(0.7f)) {
                Text(text = "فرم ثبت ساختمان", style = MaterialTheme.typography.bodyLarge)
            }

            Button(onClick = {
                val selectFileIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "*/*" // یا "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
                (context as? Activity)?.startActivityForResult(
                    selectFileIntent,
                    REQUEST_CODE_PICK_EXCEL
                )
            }, modifier = Modifier.fillMaxWidth(0.7f)) {
                Text(text = "بارگذاری فایل اکسل", style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "دانلود فایل تمپلیت اکسل وارد کردن اطلاعات",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    val fileManager = FileManagement()

//                    fileManager.openTemplateExcel(
//                        activity = thisActivity,
//                        rawResourceId = R.raw.export_delta_template,
//                        fileName = "export_delta_template.xlsx",
//                        authority = "${packageName}.fileprovider"
//                    )

                },
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }

}

//fun getCellStringValue(cell: Cell?): String {
//    if (cell == null) return ""
//    //@todo manage dates
//    return when(cell.cellType) {
//        CellType.STRING -> cell.stringCellValue
//        CellType.NUMERIC -> {
//            if (DateUtil.isCellDateFormatted(cell)) {
//                val date = cell.dateCellValue
//                java.text.SimpleDateFormat("yyyy-MM-dd").format(date)
//            } else {
//                // عدد را به رشته تبدیل می‌کنیم
//                if (cell.numericCellValue % 1 == 0.0) // عدد صحیح است؟
//                    cell.numericCellValue.toLong().toString()
//                else
//                    cell.numericCellValue.toString()
//            }
//        }
//        CellType.BOOLEAN -> cell.booleanCellValue.toString()
//        CellType.FORMULA -> {
//            try {
//                cell.stringCellValue
//            } catch(e: Exception) {
//                try {
//                    cell.numericCellValue.toString()
//                } catch (_: Exception) {
//                    ""
//                }
//            }
//        }
//        else -> ""
//    }
//}
