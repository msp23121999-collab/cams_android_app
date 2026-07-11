package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.LeaveApprovalsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LeaveApprovalsDao {
    @Query("SELECT * FROM leave_approvals")
    fun getAll(): Flow<List<LeaveApprovalsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LeaveApprovalsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<LeaveApprovalsEntity>)

    @Delete
    suspend fun delete(entity: LeaveApprovalsEntity)

    @Query("DELETE FROM leave_approvals")
    suspend fun deleteAll()
}
