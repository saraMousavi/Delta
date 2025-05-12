package com.example.delta.data.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class BuildingWithUploadedFiles(
    @Embedded val building: Buildings,
    @Relation(
        parentColumn = "buildingId",
        entityColumn = "fileId",
        associateBy = Junction(BuildingUploadedFileCrossRef::class)
    )
    val uploadedFiles: List<UploadedFileEntity>
)
