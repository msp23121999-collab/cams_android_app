package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.SectionsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SectionsDao {
    @Query("SELECT * FROM sections")
    fun getAll(): Flow<List<SectionsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SectionsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<SectionsEntity>)

    @Delete
    suspend fun delete(entity: SectionsEntity)

    @Query("DELETE FROM sections")
    suspend fun deleteAll()
}
