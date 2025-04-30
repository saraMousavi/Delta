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
import com.example.delta.data.dao.CostDao
import com.example.delta.data.dao.DebtsDao
import com.example.delta.data.dao.EarningsDao
import com.example.delta.data.dao.OwnersDao
import com.example.delta.data.dao.RoleDao
import com.example.delta.data.dao.TenantDao
import com.example.delta.data.dao.UnitsDao
import com.example.delta.data.dao.UsersDao
import com.example.delta.data.entity.AuthorizationField
import com.example.delta.data.entity.AuthorizationObject
import com.example.delta.data.entity.BuildingOwnerCrossRef
import com.example.delta.data.entity.BuildingTenantCrossRef
import com.example.delta.data.entity.BuildingTypes
import com.example.delta.data.entity.BuildingUsages
import com.example.delta.data.entity.Buildings
import com.example.delta.data.entity.Costs
import com.example.delta.data.entity.Debts
import com.example.delta.data.entity.Earnings
import com.example.delta.data.entity.OwnerWithBuildings
import com.example.delta.data.entity.Owners
import com.example.delta.data.entity.OwnersUnitsCrossRef
import com.example.delta.data.entity.Role
import com.example.delta.data.entity.RoleAuthorizationFieldCrossRef
import com.example.delta.data.entity.RoleAuthorizationObjectCrossRef
import com.example.delta.data.entity.Tenants
import com.example.delta.data.entity.TenantsUnitsCrossRef
import com.example.delta.data.entity.Units
import com.example.delta.data.entity.User
import com.example.delta.init.Converter
import java.security.acl.Owner

@Database(
    entities = [BuildingTypes::class,
        BuildingUsages::class, Buildings::class,
        Costs::class, Earnings::class, Units::class,
        Debts::class, Owners::class, Tenants::class,
        BuildingOwnerCrossRef::class, BuildingTenantCrossRef::class,
        OwnerWithBuildings::class,
        OwnersUnitsCrossRef::class, TenantsUnitsCrossRef::class,
        AuthorizationObject::class,
        AuthorizationField::class,
        RoleAuthorizationObjectCrossRef::class,
        RoleAuthorizationFieldCrossRef::class,
        User::class, Role::class],
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
    abstract fun ownersDao(): OwnersDao
    abstract fun tenantDao(): TenantDao
    abstract fun usersDao(): UsersDao
    abstract fun authorizationDao(): AuthorizationDao
    abstract fun roleDao(): RoleDao


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

