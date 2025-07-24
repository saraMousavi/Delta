package com.example.delta

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import androidx.core.content.FileProvider
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.Owners
import com.example.delta.data.entity.Tenants
import com.example.delta.data.entity.Units
import com.example.delta.init.Preference
import com.example.delta.viewmodel.SharedViewModel
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


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
                        handleExcelFile(inputStream)
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

    private fun handleExcelFile(inputStream: InputStream) {
        try {
            val workbook = XSSFWorkbook(inputStream)
            val buildingSheet = workbook.getSheetAt(0)
            val unitsSheet = workbook.getSheetAt(1)
            val ownersSheet = workbook.getSheetAt(2)
            val tenantSheet = workbook.getSheetAt(3)
            val buildingsList = mutableListOf<Buildings>()
            val unitsList = mutableListOf<Units>()
            val ownersList = mutableListOf<Owners>()
            val tenantsList = mutableListOf<Tenants>()

            for (row in buildingSheet) {
                if (row.rowNum == 0) continue

                val name = row.getCell(0)?.stringCellValue ?: ""
                val phone = row.getCell(1)?.stringCellValue ?: ""
                val email = row.getCell(2)?.stringCellValue ?: ""
                val postcode = row.getCell(3)?.stringCellValue ?: ""
                val street = row.getCell(4)?.stringCellValue ?: ""
                if (name.isEmpty()){
                    break
                } else {
                    val building = Buildings(
                        name = name,
                        phone = phone,
                        email = email,
                        postCode = postcode,
                        street = street,
                        province = "Tehran",
                        state = "Central",
                        buildingTypeId = 1,
                        buildingUsageId = 1,
                        fund = 0.0,
                        userId = Preference().getUserId(context = this),
                        utilities = emptyList()
                    )
                    buildingsList.add(building)
                }
            }
            for (row in unitsSheet) {
                if (row.rowNum == 0) continue
                val unitNumber = getCellStringValue(row.getCell(0))
                if (unitNumber.isEmpty()) {
                    break
                } else {

                    val area = getCellStringValue(row.getCell(1))
                    val numberOfRoom = getCellStringValue(row.getCell(2))
                    val numberOfParking = getCellStringValue(row.getCell(3))
                    val buildingName = getCellStringValue(row.getCell(4))

                    val units = Units(
                        unitNumber = unitNumber,
                        area = area,
                        numberOfRooms = numberOfRoom,
                        numberOfParking = numberOfParking,
                        excelBuildingName = buildingName
                    )
                    unitsList.add(units)
                }

            }

            for (row in ownersSheet) {
                if (row.rowNum == 0) continue
                val firstName = getCellStringValue(row.getCell(0))
                if (firstName.isEmpty()) {
                    break
                } else {
                    val lastName = getCellStringValue(row.getCell(1))
                    val phoneNumber = getCellStringValue(row.getCell(2))
                    val mobileNumber = getCellStringValue(row.getCell(3))
                    val address = getCellStringValue(row.getCell(4))
                    val email = getCellStringValue(row.getCell(5))
                    val unitsNumber = getCellStringValue(row.getCell(6))
                    val buildingName = getCellStringValue(row.getCell(7))
                    val isManager = getCellStringValue(row.getCell(8))
                    val dang = getCellStringValue(row.getCell(9))
                    val owner = Owners(
                        firstName = firstName,
                        lastName = lastName,
                        phoneNumber = phoneNumber,
                        mobileNumber = mobileNumber,
                        address = address,
                        birthday = "",
                        email = email,
                        excelUnitsNumber = unitsNumber,
                        excelBuildingName = buildingName,
                        excelIsManager = isManager.toBoolean(),
                        excelDang = dang.toDouble()
                    )
                    ownersList.add(owner)
                }
            }
            for (row in tenantSheet) {
                if (row.rowNum == 0) continue
                val firstName = getCellStringValue(row.getCell(0))
                if (firstName.isEmpty()) {
                    break
                } else {
                    val lastName = getCellStringValue(row.getCell(1))
                    val phoneNumber = getCellStringValue(row.getCell(2))
                    val mobileNumber = getCellStringValue(row.getCell(3))
                    val email = getCellStringValue(row.getCell(4))
                    val numberOfTenants = getCellStringValue(row.getCell(5))
                    val startDate = getCellStringValue(row.getCell(6))
                    val endDate = getCellStringValue(row.getCell(7))
                    val status = getCellStringValue(row.getCell(8))
                    val unitsNumber = getCellStringValue(row.getCell(9))
                    val buildingName = getCellStringValue(row.getCell(10))
                    val tenants = Tenants(
                        firstName = firstName,
                        lastName = lastName,
                        phoneNumber = phoneNumber,
                        mobileNumber = mobileNumber,
                        birthday = "",
                        email = email,
                        startDate = startDate,
                        endDate = endDate,
                        status = status,
                        numberOfTenants = numberOfTenants,
                        excelUnitsNumber = unitsNumber,
                        excelBuildingName = buildingName
                    )
                    tenantsList.add(tenants)
                }
            }
            workbook.close()

            sharedViewModel.saveBuildingsList(buildingsList, unitsList, ownersList, tenantsList) { successCount, errors ->
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "ثبت $successCount ساختمان موفق بود.\nخطاها:\n${errors.joinToString("\n")}",
                        Toast.LENGTH_LONG
                    ).show()
                    val intent = Intent(this, HomePageActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                Toast.makeText(this, "خطا در پردازش فایل اکسل: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun openTemplateExcel() {
        val inputStream = resources.openRawResource(R.raw.export_delta_template)
        val fileName = "export_delta_template.xlsx"
        val file = File(cacheDir, fileName)

        inputStream.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }

        val fileUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.e("e", e.toString())
            runOnUiThread {
                Toast.makeText(
                    this,
                    "برنامه‌ای برای باز کردن فایل اکسل وجود ندارد",
                    Toast.LENGTH_LONG
                ).show()
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
                        MainScreen()
                    }
                }
            }
        }

    }

    @Composable
    fun MainScreen() {
        val context = LocalContext.current


        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
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
                    openTemplateExcel()
                },
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }

}

fun getCellStringValue(cell: Cell?): String {
    if (cell == null) return ""
    //@todo manage dates
    return when(cell.cellType) {
        CellType.STRING -> cell.stringCellValue
        CellType.NUMERIC -> {
            if (DateUtil.isCellDateFormatted(cell)) {
                val date = cell.dateCellValue
                java.text.SimpleDateFormat("yyyy-MM-dd").format(date)
            } else {
                // عدد را به رشته تبدیل می‌کنیم
                if (cell.numericCellValue % 1 == 0.0) // عدد صحیح است؟
                    cell.numericCellValue.toLong().toString()
                else
                    cell.numericCellValue.toString()
            }
        }
        CellType.BOOLEAN -> cell.booleanCellValue.toString()
        CellType.FORMULA -> {
            try {
                cell.stringCellValue
            } catch(e: Exception) {
                try {
                    cell.numericCellValue.toString()
                } catch (_: Exception) {
                    ""
                }
            }
        }
        else -> ""
    }
}
