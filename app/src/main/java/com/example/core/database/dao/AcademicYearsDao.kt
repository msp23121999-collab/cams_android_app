package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.AcademicYearsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AcademicYearsDao {
    @Query("SELECT * FROM academic_years")
    fun getAll(): Flow<List<AcademicYearsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AcademicYearsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<AcademicYearsEntity>)

    @Delete
    suspend fun delete(entity: AcademicYearsEntity)

    @Query("DELETE FROM academic_years")
    suspend fun deleteAll()
}
