package com.example.delta.data.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.delta.data.dao.AuthorizationDao
import com.example.delta.data.dao.BuildingTypeDao
import com.example.delta.data.dao.BuildingUsageDao
import com.example.delta.data.dao.BuildingsDao
import com.example.delta.data.dao.CityComplexDao
import com.example.delta.data.dao.CostDao
import com.example.delta.data.dao.CreditsDao
import com.example.delta.data.dao.DebtsDao
import com.example.delta.data.dao.EarningsDao
import com.example.delta.data.dao.FundsDao
import com.example.delta.data.dao.NotificationDao
import com.example.delta.data.dao.PhonebookDao
import com.example.delta.data.dao.RoleDao
import com.example.delta.data.dao.UnitsDao
import com.example.delta.data.dao.UploadedFileDao
import com.example.delta.data.dao.UsersDao
import com.example.delta.data.entity.AuthorizationField
import com.example.delta.data.entity.AuthorizationObject
import com.example.delta.data.entity.BuildingTypes
import com.example.delta.data.entity.BuildingUploadedFileCrossRef
import com.example.delta.data.entity.BuildingUsages
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.CityComplexes
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Credits
import com.example.delta.data.entity.Debts
import com.example.delta.data.entity.Earnings
import com.example.delta.data.entity.EmergencyNumber
import com.example.delta.data.entity.Funds
import com.example.delta.data.entity.Notification
import com.example.delta.data.entity.OwnersUnitsCrossRef
import com.example.delta.data.entity.PhonebookEntry
import com.example.delta.data.entity.Role
import com.example.delta.data.entity.RoleAuthorizationObjectFieldCrossRef
import com.example.delta.data.entity.TenantsUnitsCrossRef
import com.example.delta.data.entity.Units
import com.example.delta.data.entity.UploadedFileEntity
import com.example.delta.data.entity.User
import com.example.delta.data.entity.UserRoleBuildingUnitCrossRef
import com.example.delta.data.entity.UsersBuildingsCrossRef
import com.example.delta.data.entity.UsersNotificationCrossRef
import com.example.delta.init.Converter

@Database(
    entities = [BuildingTypes::class,
        BuildingUsages::class, Buildings::class,
        Costs::class, Earnings::class, Units::class,
        Debts::class,
        OwnersUnitsCrossRef::class, TenantsUnitsCrossRef::class,
        AuthorizationObject::class,
        AuthorizationField::class,
        RoleAuthorizationObjectFieldCrossRef::class,
        User::class, Role::class, UserRoleBuildingUnitCrossRef::class,
        UploadedFileEntity::class, BuildingUploadedFileCrossRef::class,
        PhonebookEntry::class, EmergencyNumber::class,
        Notification::class, UsersNotificationCrossRef::class,
        CityComplexes::class,
        Funds::class,
        Credits::class,
        UsersBuildingsCrossRef::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun buildingTypeDao(): BuildingTypeDao
    abstract fun buildingUsageDao(): BuildingUsageDao
    abstract fun buildingsDao(): BuildingsDao
    abstract fun costDao(): CostDao
    abstract fun earningsDao(): EarningsDao
    abstract fun unitsDao(): UnitsDao
    abstract fun debtsDao(): DebtsDao
    abstract fun usersDao(): UsersDao
    abstract fun authorizationDao(): AuthorizationDao
    abstract fun roleDao(): RoleDao
    abstract fun uploadedFileDao() : UploadedFileDao
    abstract fun phonebookDao() : PhonebookDao
    abstract fun notificationDao() : NotificationDao
    abstract fun cityComplexDao() : CityComplexDao
    abstract fun fundsDao() : FundsDao
    abstract fun creditsDao() : CreditsDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null


        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

