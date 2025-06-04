package com.example.delta.init

import android.util.Log
import com.example.delta.data.entity.AuthorizationField
import com.example.delta.data.model.AppDatabase
import com.example.delta.enums.BuildingFormFields
import com.example.delta.enums.BuildingProfileFields
import com.example.delta.enums.HomePageFields

object AuthFieldDatabaseInitializer {
    suspend fun initializeFields(db: AppDatabase) {
        val authDao = db.authorizationDao()
        if (authDao.getFieldCount() == 0) {
            BuildingProfileFields.entries.forEach { field ->
                authDao.insertAuthorizationField(
                    AuthorizationField(
                        objectId = field.objectId,
                        name = field.fieldNameRes,
                        fieldType = field.fieldType
                    )
                )
            }

            HomePageFields.entries.forEach { field ->
                authDao.insertAuthorizationField(
                    AuthorizationField(
                        objectId = field.objectId,
                        name = field.fieldNameRes,
                        fieldType = field.fieldType
                    )
                )
            }

            BuildingFormFields.entries.forEach { field ->
                authDao.insertAuthorizationField(
                    AuthorizationField(
                        objectId = field.objectId,
                        name = field.fieldNameRes,
                        fieldType = field.fieldType
                    )
                )
            }
            Log.d("authDao.getAllAuthorizationObjects()", authDao.getAllAuthorizationObjects().toString())
            Log.d("---", authDao.getAllAuthorizationFields().toString())
            // Repeat for other screen enums if needed
        }
    }
}