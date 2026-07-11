package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.ClubsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClubsDao {
    @Query("SELECT * FROM clubs")
    fun getAll(): Flow<List<ClubsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ClubsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ClubsEntity>)

    @Delete
    suspend fun delete(entity: ClubsEntity)

    @Query("DELETE FROM clubs")
    suspend fun deleteAll()
}
