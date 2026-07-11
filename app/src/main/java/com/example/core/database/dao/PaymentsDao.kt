package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.PaymentsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentsDao {
    @Query("SELECT * FROM payments")
    fun getAll(): Flow<List<PaymentsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PaymentsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<PaymentsEntity>)

    @Delete
    suspend fun delete(entity: PaymentsEntity)

    @Query("DELETE FROM payments")
    suspend fun deleteAll()
}
