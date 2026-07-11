package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.UsersEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsersDao {
    @Query("SELECT * FROM users")
    fun getAll(): Flow<List<UsersEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: UsersEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<UsersEntity>)

    @Delete
    suspend fun delete(entity: UsersEntity)

    @Query("DELETE FROM users")
    suspend fun deleteAll()
}
