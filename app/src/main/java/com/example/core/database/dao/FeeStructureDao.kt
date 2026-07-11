package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.FeeStructureEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeeStructureDao {
    @Query("SELECT * FROM fee_structure")
    fun getAll(): Flow<List<FeeStructureEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FeeStructureEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<FeeStructureEntity>)

    @Delete
    suspend fun delete(entity: FeeStructureEntity)

    @Query("DELETE FROM fee_structure")
    suspend fun deleteAll()
}
