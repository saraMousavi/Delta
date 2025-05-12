package com.example.delta.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "uploaded_files")
data class UploadedFileEntity(
    @PrimaryKey(autoGenerate = true) val fileId: Long = 0,
    val fileUrl: String
)
