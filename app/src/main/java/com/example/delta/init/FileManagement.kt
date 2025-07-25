package com.example.delta.init

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.delta.HomePageActivity
import com.example.delta.R
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.Owners
import com.example.delta.data.entity.Tenants
import com.example.delta.data.entity.Units
import com.example.delta.getCellStringValue
import com.example.delta.viewmodel.SharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class FileManagement {

    fun handleExcelFile(
        inputStream: InputStream,
        activity: Activity,
        sharedViewModel: SharedViewModel
    ) {
        try {
            val workbook = XSSFWorkbook(inputStream)
            val buildingSheet = workbook.getSheetAt(0)
            val unitsSheet = workbook.getSheetAt(1)
            val ownersSheet = workbook.getSheetAt(2)
            val tenantSheet = workbook.getSheetAt(3)

            val buildingsList = readBuildingsFromSheet(buildingSheet, activity)
            val unitsList = readUnitsFromSheet(unitsSheet)
            val ownersList = readOwnersFromSheet(ownersSheet)
            val tenantsList = readTenantsFromSheet(tenantSheet)
            Log.d("buildingsList", buildingsList.toString())
            Log.d("unitsList", unitsList.toString())
            workbook.close()
            CoroutineScope(Dispatchers.Main).launch {
                sharedViewModel.saveBuildingsList(
                    buildingsList,
                    unitsList,
                    ownersList,
                    tenantsList
                ) { successCount, errors ->

                    Toast.makeText(
                        activity,
                        "ثبت $successCount ساختمان موفق بود.\nخطاها:\n${errors.joinToString("\n")}",
                        Toast.LENGTH_LONG
                    ).show()

                    val intent = Intent(activity, HomePageActivity::class.java)
                    activity.startActivity(intent)
                    activity.finish()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            activity.runOnUiThread {
                Toast.makeText(activity, "خطا در پردازش فایل اکسل: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun readBuildingsFromSheet(sheet: org.apache.poi.ss.usermodel.Sheet, context: Context): List<Buildings> {
        val list = mutableListOf<Buildings>()
        val userId = Preference().getUserId(context = context)
        for (row in sheet) {
            if(row.rowNum == 0) continue
            val name = row.getCell(0)?.stringCellValue ?: ""
            if (name.isEmpty()) break

            val phone = row.getCell(1)?.stringCellValue ?: ""
            val email = row.getCell(2)?.stringCellValue ?: ""
            val postcode = row.getCell(3)?.stringCellValue ?: ""
            val street = row.getCell(4)?.stringCellValue ?: ""

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
                userId = userId,
                utilities = emptyList()
            )
            list.add(building)
        }
        return list
    }

    private fun readUnitsFromSheet(sheet: org.apache.poi.ss.usermodel.Sheet): List<Units> {
        val list = mutableListOf<Units>()
        for(row in sheet) {
            if(row.rowNum == 0) continue

            val unitNumber = getCellStringValue(row.getCell(0))
            if(unitNumber.isEmpty()) break

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
            list.add(units)
        }
        return list
    }

    private fun readOwnersFromSheet(sheet: org.apache.poi.ss.usermodel.Sheet): List<Owners> {
        val list = mutableListOf<Owners>()
        for(row in sheet) {
            if(row.rowNum == 0) continue
            val firstName = getCellStringValue(row.getCell(0))
            if(firstName.isEmpty()) break

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
                excelDang = dang.toDoubleOrNull() ?: 0.0
            )
            list.add(owner)
        }
        return list
    }

    private fun readTenantsFromSheet(sheet: org.apache.poi.ss.usermodel.Sheet): List<Tenants> {
        val list = mutableListOf<Tenants>()
        for(row in sheet) {
            if(row.rowNum == 0) continue

            val firstName = getCellStringValue(row.getCell(0))
            if(firstName.isEmpty()) break

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
            list.add(tenants)
        }
        return list
    }

    fun openTemplateExcel(
        activity: Activity,
        rawResourceId: Int,
        fileName: String,
        authority: String
    ) {
        try {
            val inputStream: InputStream = activity.resources.openRawResource(rawResourceId)
            val file = File(activity.cacheDir, fileName)

            inputStream.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }

            val fileUri = FileProvider.getUriForFile(
                activity,
                authority,
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(
                    fileUri,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }

            activity.startActivity(intent)

        } catch (e: ActivityNotFoundException) {
            Log.e("FileManagement", "No app found to open Excel file", e)
            activity.runOnUiThread {
                Toast.makeText(
                    activity,
                    "برنامه‌ای برای باز کردن فایل اکسل وجود ندارد",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Log.e("FileManagement", "Error opening Excel template", e)
            activity.runOnUiThread {
                Toast.makeText(
                    activity,
                    "خطا در باز کردن فایل اکسل: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
