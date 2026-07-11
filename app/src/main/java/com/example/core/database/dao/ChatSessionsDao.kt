package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.ChatSessionsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatSessionsDao {
    @Query("SELECT * FROM chat_sessions")
    fun getAll(): Flow<List<ChatSessionsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ChatSessionsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ChatSessionsEntity>)

    @Delete
    suspend fun delete(entity: ChatSessionsEntity)

    @Query("DELETE FROM chat_sessions")
    suspend fun deleteAll()
}
