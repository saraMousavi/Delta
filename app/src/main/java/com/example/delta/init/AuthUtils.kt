package com.example.delta.init




import android.content.Context
import android.util.Log
import com.example.delta.data.dao.AuthorizationDao.FieldWithPermission
import com.example.delta.enums.PermissionLevel
import com.example.delta.volley.AuthObjectFieldCross
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class AuthUtils {


    fun getAuthorizationDetailsForRole(
        context: Context,
        roleId: Long
    ): Flow<List<FieldWithPermission>> = flow {
        val api = AuthObjectFieldCross()
        val list = api.fetchFieldsWithPermissionsForRoleSuspend(context, roleId)
        emit(list)
    }.catch { e ->
        Log.e("AuthRepo", "getAuthorizationDetailsForRole failed", e)
        emit(emptyList())
    }

    object AuthorizationObjects {
        const val HOME = "صفحه اصلی"
        const val BUILDING_PROFILE = "پروفایل ساختمان"
    }

    object AuthorizationFieldsHome {
        const val SETTINGS_BUTTON = "دکمه تنظیمات"
        const val CREATE_BUILDING_BUTTON = "دکمه ایجاد ساختمان"
        const val DELETE_BUILDING_BUTTON = "حذف ساختمان"
        const val DASHBOARD_BUTTON = "داشبورد"
        const val CONTACT_MANAGEMENT_BUTTON = "ارتباط با مدیریت"
        const val NOTIFICATION_BUTTON = "نوتیفیکیشن"
    }

    object AuthorizationFieldsBuildingProfile {
        const val EDIT_BUILDING_BUTTON = "ویرایش ساختمان"

        const val FUND_TAB = "صندوق"
        const val CREATE_EARNING_BUTTON = "ایجاد درآمد"
        const val CREATE_CAPITAL_COST_BUTTON = "ایجاد سند عمرانی"
        const val CREATE_OPERATIONAL_COST_BUTTON = "ایجاد سند جاری"

        const val OWNERS_TAB = "مالکین"
        const val CREATE_OWNER_BUTTON = "ایجاد مالک"

        const val UNITS_TAB = "واحدها"
        const val CREATE_UNIT_BUTTON = "ایجاد واحد"

        const val TENANTS_TAB = "ساکنین"
        const val CREATE_TENANT_BUTTON = "ایجاد ساکن"

        const val TRANSACTIONS_TAB = "تراکنش ها"

        const val PHONEBOOK_TAB = "دفترچه تلفن"
        const val CREATE_PHONE_ENTRY_BUTTON = "ایجاد تلفن"
    }


    object AuthUtils {

        fun List<FieldWithPermission>.permissionFor(
            objectName: String,
            fieldName: String
        ): PermissionLevel {
            return firstOrNull { it.objectName == objectName && it.field.name == fieldName }
                ?.crossRef
                ?.permissionLevel
                ?: PermissionLevel.READ
        }

        fun List<FieldWithPermission>.canRead(
            objectName: String,
            fieldName: String
        ): Boolean {
            val p = permissionFor(objectName, fieldName)
            return p.value >= PermissionLevel.READ.value
        }

        fun List<FieldWithPermission>.canWrite(
            objectName: String,
            fieldName: String
        ): Boolean {
            val p = permissionFor(objectName, fieldName)
            return p.value >= PermissionLevel.WRITE.value
        }

        fun List<FieldWithPermission>.isFull(
            objectName: String,
            fieldName: String
        ): Boolean {
            val p = permissionFor(objectName, fieldName)
            return p == PermissionLevel.FULL
        }
    }

}