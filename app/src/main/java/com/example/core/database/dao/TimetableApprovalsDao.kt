package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.TimetableApprovalsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimetableApprovalsDao {
    @Query("SELECT * FROM timetable_approvals")
    fun getAll(): Flow<List<TimetableApprovalsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TimetableApprovalsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<TimetableApprovalsEntity>)

    @Delete
    suspend fun delete(entity: TimetableApprovalsEntity)

    @Query("DELETE FROM timetable_approvals")
    suspend fun deleteAll()
}
