package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.DepartmentsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DepartmentsDao {
    @Query("SELECT * FROM departments")
    fun getAll(): Flow<List<DepartmentsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DepartmentsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<DepartmentsEntity>)

    @Delete
    suspend fun delete(entity: DepartmentsEntity)

    @Query("DELETE FROM departments")
    suspend fun deleteAll()
}
