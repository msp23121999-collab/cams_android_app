package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.InternshipDrivesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InternshipDrivesDao {
    @Query("SELECT * FROM internship_drives")
    fun getAll(): Flow<List<InternshipDrivesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: InternshipDrivesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<InternshipDrivesEntity>)

    @Delete
    suspend fun delete(entity: InternshipDrivesEntity)

    @Query("DELETE FROM internship_drives")
    suspend fun deleteAll()
}
