package com.example.delta.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.delta.data.entity.Role

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

    @Query("SELECT * FROM role where role_name = :name")
    suspend fun getRoleByName(name: String): Role
}