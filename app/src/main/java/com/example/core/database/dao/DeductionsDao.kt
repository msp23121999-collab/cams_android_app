package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.DeductionsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeductionsDao {
    @Query("SELECT * FROM deductions")
    fun getAll(): Flow<List<DeductionsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DeductionsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<DeductionsEntity>)

    @Delete
    suspend fun delete(entity: DeductionsEntity)

    @Query("DELETE FROM deductions")
    suspend fun deleteAll()
}
