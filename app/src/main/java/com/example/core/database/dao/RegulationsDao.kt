package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.RegulationsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RegulationsDao {
    @Query("SELECT * FROM regulations")
    fun getAll(): Flow<List<RegulationsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RegulationsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<RegulationsEntity>)

    @Delete
    suspend fun delete(entity: RegulationsEntity)

    @Query("DELETE FROM regulations")
    suspend fun deleteAll()
}
