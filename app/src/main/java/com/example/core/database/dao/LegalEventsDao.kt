package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.LegalEventsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LegalEventsDao {
    @Query("SELECT * FROM legal_events")
    fun getAll(): Flow<List<LegalEventsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LegalEventsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<LegalEventsEntity>)

    @Delete
    suspend fun delete(entity: LegalEventsEntity)

    @Query("DELETE FROM legal_events")
    suspend fun deleteAll()
}
