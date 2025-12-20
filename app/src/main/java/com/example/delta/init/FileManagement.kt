package com.example.delta.init

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.delta.HomePageActivity
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.Units
import com.example.delta.volley.Building
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat

class FileManagement {

    fun handleExcelFile(
        inputStream: InputStream,
        activity: Activity,
        context: Context
    ) {
        try {
            val workbook = XSSFWorkbook(inputStream)
            val buildingSheet = workbook.getSheetAt(0)
            val unitsSheet = workbook.getSheetAt(1)
            val ownersSheet = workbook.getSheetAt(2)
            val tenantSheet = workbook.getSheetAt(3)

            val buildings = readBuildingsFromSheet(buildingSheet, activity)
            val units = readUnitsFromSheet(unitsSheet)
            val owners = readOwnersFromSheet(ownersSheet)
            val tenants = readTenantsFromSheet(tenantSheet)

            val payload = ExcelPayloadBuilder.build(buildings, units, owners, tenants)

            workbook.close()

            CoroutineScope(Dispatchers.IO).launch {
                Building().sendBulk(context = context , payload,
                    onSuccess = {
                        activity.runOnUiThread {
                            val intent = Intent(activity, HomePageActivity::class.java)
                            activity.startActivity(intent)
                            activity.finish()
                        }
                    },
                    onError = {
                        activity.runOnUiThread {
                            Toast.makeText(activity, it.message, Toast.LENGTH_LONG).show()
                        }
                    }
                )
            }

        } catch (e: Exception) {
            activity.runOnUiThread {
                Toast.makeText(activity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun readBuildingsFromSheet(
        sheet: org.apache.poi.ss.usermodel.Sheet,
        context: Context
    ): List<Buildings> {
        val list = mutableListOf<Buildings>()
        val userId = Preference().getUserId(context)
        for (row in sheet) {
            if (row.rowNum == 0) continue
            val name = getCellStringValue(row.getCell(0))
            if (name.isEmpty()) break

            val phone = getCellStringValue(row.getCell(1))
            val mobileNumber = getCellStringValue(row.getCell(2))
            val postCode = getCellStringValue(row.getCell(3))
            val street = getCellStringValue(row.getCell(4))
            val floorCount = getCellStringValue(row.getCell(5))
            val unitCount = getCellStringValue(row.getCell(6))
            val parkingCount = getCellStringValue(row.getCell(7))

            val building = Buildings(
                buildingId = 0,
                name = name,
                serialNumber = "",
                postCode = postCode,
                street = street,
                province = "Tehran",
                state = "Central",
                buildingTypeId = 1,
                buildingUsageId = 1,
                fund = 0.0,
                userId = userId,
                floorCount = floorCount.toInt(),
                unitCount = unitCount.toInt(),
                parkingCount = parkingCount.toInt(),
                phone = phone,
                mobileNumber = mobileNumber

            )
            list.add(building)
        }
        return list
    }

    private fun readUnitsFromSheet(sheet: org.apache.poi.ss.usermodel.Sheet): List<Units> {
        val list = mutableListOf<Units>()
        for (row in sheet) {
            if (row.rowNum == 0) continue
            val unitNumber = getCellStringValue(row.getCell(0))
            if (unitNumber.isEmpty()) break
            val area = getCellStringValue(row.getCell(1))
            val rooms = getCellStringValue(row.getCell(2))
            val parking = getCellStringValue(row.getCell(3))
            val warehouse = getCellStringValue(row.getCell(4))
            val buildingName = getCellStringValue(row.getCell(5))
            val postCode = getCellStringValue(row.getCell(6))

            list.add(
                Units(
                    unitId = 0,
                    buildingId = null,
                    unitNumber = unitNumber,
                    floorNumber = 0,
                    area = area,
                    postCode = postCode,
                    numberOfRooms = rooms,
                    numberOfWarehouse = warehouse,
                    numberOfParking = parking,
                    excelBuildingName = buildingName
                )
            )
        }
        return list
    }

    private fun readOwnersFromSheet(sheet: org.apache.poi.ss.usermodel.Sheet): List<ExcelOwner> {
        val list = mutableListOf<ExcelOwner>()
        for (row in sheet) {
            if (row.rowNum == 0) continue
            val firstName = getCellStringValue(row.getCell(0))
            if (firstName.isEmpty()) break

            list.add(
                ExcelOwner(
                    firstName = firstName,
                    lastName = getCellStringValue(row.getCell(1)),
                    phoneNumber = getCellStringValue(row.getCell(2)),
                    mobileNumber = getCellStringValue(row.getCell(3)),
                    address = getCellStringValue(row.getCell(4)),
                    email = getCellStringValue(row.getCell(5)),
                    excelUnitsNumber = getCellStringValue(row.getCell(6)),
                    excelBuildingName = getCellStringValue(row.getCell(7)),
                    excelIsManager = getCellStringValue(row.getCell(8)).toBoolean(),
                    excelDang = getCellStringValue(row.getCell(9)).toDoubleOrNull() ?: 0.0,
                    excelIsResident = getCellStringValue(row.getCell(10)).toBoolean(),
                )
            )
        }
        return list
    }

    private fun readTenantsFromSheet(sheet: org.apache.poi.ss.usermodel.Sheet): List<ExcelTenant> {
        val list = mutableListOf<ExcelTenant>()
        for (row in sheet) {
            if (row.rowNum == 0) continue

            val firstName = getCellStringValue(row.getCell(0))
            if (firstName.isEmpty()) break

            list.add(
                ExcelTenant(
                    firstName = firstName,
                    lastName = getCellStringValue(row.getCell(1)),
                    phoneNumber = getCellStringValue(row.getCell(2)),
                    mobileNumber = getCellStringValue(row.getCell(3)),
                    email = getCellStringValue(row.getCell(4)),
                    numberOfTenants = getCellStringValue(row.getCell(5)),
                    startDate = getCellStringValue(row.getCell(6)),
                    endDate = getCellStringValue(row.getCell(7)),
                    status = getCellStringValue(row.getCell(8)),
                    excelUnitsNumber = getCellStringValue(row.getCell(9)),
                    excelBuildingName = getCellStringValue(row.getCell(10)),
                    excelPostCode = getCellStringValue(row.getCell(11)),
                )
            )
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

            val uri = FileProvider.getUriForFile(activity, authority, file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(
                    uri,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            activity.startActivity(intent)

        } catch (e: Exception) {
            activity.runOnUiThread {
                Toast.makeText(activity, e.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getCellStringValue(cell: Cell?): String {
        if (cell == null) return ""
        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue.trim()
            CellType.NUMERIC ->
                if (DateUtil.isCellDateFormatted(cell)) {
                    val d = cell.dateCellValue
                    SimpleDateFormat("yyyy-MM-dd").format(d)
                } else {
                    val v = cell.numericCellValue
                    if (v % 1.0 == 0.0) v.toLong().toString() else v.toString()
                }
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            CellType.FORMULA ->
                try {
                    cell.stringCellValue.trim()
                } catch (e1: Exception) {
                    try { cell.numericCellValue.toString() } catch (e2: Exception) { "" }
                }
            else -> ""
        }
    }
}
