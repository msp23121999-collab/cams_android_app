package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.FacultyResearchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FacultyResearchDao {
    @Query("SELECT * FROM faculty_research")
    fun getAll(): Flow<List<FacultyResearchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FacultyResearchEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<FacultyResearchEntity>)

    @Delete
    suspend fun delete(entity: FacultyResearchEntity)

    @Query("DELETE FROM faculty_research")
    suspend fun deleteAll()
}
