package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entities.StudyMaterialsEntity

@Dao
interface StudyMaterialsDao {
    @Query("SELECT * FROM study_materials")
    suspend fun getAll(): List<StudyMaterialsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: StudyMaterialsEntity)
}
