package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.ExamSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamSettingsDao {
    @Query("SELECT * FROM exam_settings")
    fun getAll(): Flow<List<ExamSettingsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ExamSettingsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ExamSettingsEntity>)

    @Delete
    suspend fun delete(entity: ExamSettingsEntity)

    @Query("DELETE FROM exam_settings")
    suspend fun deleteAll()
}
