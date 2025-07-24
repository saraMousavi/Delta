package com.example.delta.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.delta.data.entity.PhonebookEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface PhonebookDao {
    @Query("SELECT * FROM phonebook_entries WHERE buildingId = :buildingId AND type = 'resident'")
    fun getResidents(buildingId: Long): Flow<List<PhonebookEntry>>

    @Query("SELECT * FROM phonebook_entries WHERE buildingId = :buildingId AND type = 'emergency'")
    fun getEmergencyNumbers(buildingId: Long): Flow<List<PhonebookEntry>>

    @Insert
    suspend fun insertEntry(entry: PhonebookEntry)

    @Delete
    suspend fun deletePhonebookEntry(entry: PhonebookEntry)
}