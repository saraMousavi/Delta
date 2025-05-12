package com.example.delta.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.delta.data.entity.UploadedFileEntity

@Dao
interface UploadedFileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUploadedFile(file: UploadedFileEntity): Long


    @Update
    suspend fun updateUploadedFile(file: UploadedFileEntity)


    @Query(
        """
        SELECT uf.*
        FROM uploaded_files uf
        INNER JOIN building_uploaded_files_cross_ref ref ON uf.fileId = ref.fileId
        WHERE ref.buildingId = :buildingId
    """
    )
    suspend fun getFileUrlsForBuilding(buildingId: Long): List<UploadedFileEntity>
}
