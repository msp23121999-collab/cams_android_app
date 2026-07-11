package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.AssignmentsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssignmentsDao {
    @Query("SELECT * FROM assignments")
    fun getAll(): Flow<List<AssignmentsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AssignmentsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<AssignmentsEntity>)

    @Delete
    suspend fun delete(entity: AssignmentsEntity)

    @Query("DELETE FROM assignments")
    suspend fun deleteAll()
}
