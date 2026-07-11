package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.MessagesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessagesDao {
    @Query("SELECT * FROM messages")
    fun getAll(): Flow<List<MessagesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MessagesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<MessagesEntity>)

    @Delete
    suspend fun delete(entity: MessagesEntity)

    @Query("DELETE FROM messages")
    suspend fun deleteAll()
}
