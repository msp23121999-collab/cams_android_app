package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.NoticeAcknowledgementsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoticeAcknowledgementsDao {
    @Query("SELECT * FROM notice_acknowledgements")
    fun getAll(): Flow<List<NoticeAcknowledgementsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: NoticeAcknowledgementsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<NoticeAcknowledgementsEntity>)

    @Delete
    suspend fun delete(entity: NoticeAcknowledgementsEntity)

    @Query("DELETE FROM notice_acknowledgements")
    suspend fun deleteAll()
}
