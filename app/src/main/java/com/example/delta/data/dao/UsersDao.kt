package com.example.delta.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.delta.data.entity.Role
import com.example.delta.data.entity.TenantsUnitsCrossRef
import com.example.delta.data.entity.User
import com.example.delta.data.entity.UserRoleCrossRef
import com.example.delta.data.entity.UsersBuildingsCrossRef

@Dao
interface UsersDao {    
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertUser(user: User): Long

        @Insert
        suspend fun insertUserRoleCrossRef(crossRef: UserRoleCrossRef)

        @Insert
        suspend fun insertUserBuildingCrossRef(crossRef: UsersBuildingsCrossRef)

        @Update
        suspend fun updateUser(user: User)

        @Delete
        suspend fun deleteUser(user: User)

        @Query("SELECT * FROM user")
        suspend fun getUsers(): List<User>

        @Query("SELECT * FROM user WHERE mobile_number = :mobile LIMIT 1")
        fun getUserByMobile(mobile: String): User?

        @Query("SELECT * FROM user WHERE userId = :userId")
        fun getUserById(userId: Long): User?


        @Query("""
    SELECT role.* FROM user_role_cross_ref as ur 
                INNER JOIN role on role.roleId = ur.roleId where userId = :userId
""")
        suspend fun getRoleByUserId(userId:Long): Role

        @Transaction
        @Query("SELECT * FROM user_role_cross_ref")
        fun getUsersWithRoles(): List<UserRoleCrossRef>

}