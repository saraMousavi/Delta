package com.example.delta.init

import com.example.delta.data.entity.AuthorizationObject
import com.example.delta.data.model.AppDatabase
import com.example.delta.enums.AuthObject

object AuthDatabaseInitializer {
    suspend fun initialize(db: AppDatabase) {
        val authDao = db.authorizationDao()

        if (authDao.getCount() == 0) {
//            AuthObject.getAll().forEach { authObj ->
//                authDao.insertAuthorizationObject(
//                    AuthorizationObject(
//                        objectId = authObj.id,
//                        name = authObj.displayNameRes,       // Store the Int resource id
//                        description = authObj.descriptionRes // Store the Int resource id
//                    )
//                )
//            }
        }
    }
}