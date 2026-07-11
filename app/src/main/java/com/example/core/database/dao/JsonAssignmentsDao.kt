package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.JsonAssignmentsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JsonAssignmentsDao {
    @Query("SELECT * FROM json_assignments")
    fun getAll(): Flow<List<JsonAssignmentsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: JsonAssignmentsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<JsonAssignmentsEntity>)

    @Delete
    suspend fun delete(entity: JsonAssignmentsEntity)

    @Query("DELETE FROM json_assignments")
    suspend fun deleteAll()
}
