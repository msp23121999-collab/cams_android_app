package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.FacultyProfilesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FacultyProfilesDao {
    @Query("SELECT * FROM faculty_profiles")
    fun getAll(): Flow<List<FacultyProfilesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FacultyProfilesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<FacultyProfilesEntity>)

    @Delete
    suspend fun delete(entity: FacultyProfilesEntity)

    @Query("DELETE FROM faculty_profiles")
    suspend fun deleteAll()
}
