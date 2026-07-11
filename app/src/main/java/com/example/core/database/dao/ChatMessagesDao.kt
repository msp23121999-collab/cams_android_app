package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.ChatMessagesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessagesDao {
    @Query("SELECT * FROM chat_messages")
    fun getAll(): Flow<List<ChatMessagesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ChatMessagesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ChatMessagesEntity>)

    @Delete
    suspend fun delete(entity: ChatMessagesEntity)

    @Query("DELETE FROM chat_messages")
    suspend fun deleteAll()
}
