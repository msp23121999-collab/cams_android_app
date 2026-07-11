package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.InternshipApplicationsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InternshipApplicationsDao {
    @Query("SELECT * FROM internship_applications")
    fun getAll(): Flow<List<InternshipApplicationsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: InternshipApplicationsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<InternshipApplicationsEntity>)

    @Delete
    suspend fun delete(entity: InternshipApplicationsEntity)

    @Query("DELETE FROM internship_applications")
    suspend fun deleteAll()
}
