package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.FeeRecordsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeeRecordsDao {
    @Query("SELECT * FROM fee_records")
    fun getAll(): Flow<List<FeeRecordsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FeeRecordsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<FeeRecordsEntity>)

    @Delete
    suspend fun delete(entity: FeeRecordsEntity)

    @Query("DELETE FROM fee_records")
    suspend fun deleteAll()
}
