package com.example.delta.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "building_uploaded_files_cross_ref",
    primaryKeys = ["buildingId", "fileId"],
    foreignKeys = [
        ForeignKey(
            entity = Buildings::class,
            parentColumns = ["buildingId"],
            childColumns = ["buildingId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UploadedFileEntity::class,
            parentColumns = ["fileId"],
            childColumns = ["fileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("buildingId"), Index("fileId")]
)
data class BuildingUploadedFileCrossRef(
    val buildingId: Long,
    val fileId: Long
)
