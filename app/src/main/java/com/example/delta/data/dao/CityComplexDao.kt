package com.example.delta.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.delta.data.entity.CityComplexes

@Dao
interface CityComplexDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCityComplex(cityComplexes: CityComplexes): Long

    //@todo filter by userid
    @Query("SELECT * FROM city_complex ORDER BY name")
    suspend fun getAllCityComplexesFlow(): List<CityComplexes>
}
