package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.LeaveBalancesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LeaveBalancesDao {
    @Query("SELECT * FROM leave_balances")
    fun getAll(): Flow<List<LeaveBalancesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LeaveBalancesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<LeaveBalancesEntity>)

    @Delete
    suspend fun delete(entity: LeaveBalancesEntity)

    @Query("DELETE FROM leave_balances")
    suspend fun deleteAll()
}
