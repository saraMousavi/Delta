package com.example.delta.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.delta.data.entity.User
import com.example.delta.data.entity.UserWithRole

@Dao
interface UsersDao {    
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertUser(user: User): Long

        @Update
        suspend fun updateUser(user: User)

        @Delete
        suspend fun deleteUser(user: User)

        @Query("SELECT * FROM user")
        suspend fun getUsers(): List<User>

        @Transaction
        @Query("SELECT * FROM user")
        fun getUsersWithRoles(): List<UserWithRole>

}