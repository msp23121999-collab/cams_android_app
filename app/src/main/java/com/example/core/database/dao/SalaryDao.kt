package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entities.SalaryEntity

@Dao
interface SalaryDao {
    @Query("SELECT * FROM salary")
    suspend fun getAll(): List<SalaryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SalaryEntity)
}
