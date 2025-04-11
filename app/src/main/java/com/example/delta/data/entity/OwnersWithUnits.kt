package com.example.delta.data.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation


//data class OwnerWithUnits(
//    @Embedded val owner: Owners,
//    @Relation(
//        parentColumn = "ownerId",
//        entityColumn = "ownerId", // This should match the parentColumn in the junction table
//        associateBy = Junction(
//            value = OwnersUnitsCrossRef::class,
//            parentColumn = "ownerId",
//            entityColumn = "unitId"
//        )
//    )
//    val units: List<Units>
//)



