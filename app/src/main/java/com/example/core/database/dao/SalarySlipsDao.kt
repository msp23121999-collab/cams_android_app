package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.SalarySlipsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SalarySlipsDao {
    @Query("SELECT * FROM salary_slips")
    fun getAll(): Flow<List<SalarySlipsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SalarySlipsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<SalarySlipsEntity>)

    @Delete
    suspend fun delete(entity: SalarySlipsEntity)

    @Query("DELETE FROM salary_slips")
    suspend fun deleteAll()
}
