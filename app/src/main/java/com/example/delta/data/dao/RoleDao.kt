package com.example.delta.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.delta.data.entity.Role
import com.example.delta.data.entity.UserWithRole
import kotlinx.coroutines.flow.Flow

@Dao
interface RoleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRole(role: Role): Long

    @Update
    suspend fun updateRole(role: Role)

    @Delete
    suspend fun deleteRole(role: Role)

    @Query("SELECT * FROM role")
    suspend fun getRoles(): List<Role>

    @Transaction
    @Query("""
        SELECT r.* FROM Role r
        INNER JOIN user_role_cross_ref urc ON r.roleId = urc.roleId
        INNER JOIN User u ON urc.userId = u.userId
        WHERE u.mobile_number = :mobileNumber
    """)
    fun getRoleNameByMobileNumber(mobileNumber: String): Role?


    @Query("SELECT * FROM role where roleName = :name")
    suspend fun getRoleByName(name: String): Role
}