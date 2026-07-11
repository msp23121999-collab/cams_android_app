package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.ClassAdvisorsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassAdvisorsDao {
    @Query("SELECT * FROM class_advisors")
    fun getAll(): Flow<List<ClassAdvisorsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ClassAdvisorsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ClassAdvisorsEntity>)

    @Delete
    suspend fun delete(entity: ClassAdvisorsEntity)

    @Query("DELETE FROM class_advisors")
    suspend fun deleteAll()
}
