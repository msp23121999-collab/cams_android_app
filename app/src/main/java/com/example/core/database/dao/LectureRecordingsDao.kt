package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.LectureRecordingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LectureRecordingsDao {
    @Query("SELECT * FROM lecture_recordings")
    fun getAll(): Flow<List<LectureRecordingsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LectureRecordingsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<LectureRecordingsEntity>)

    @Delete
    suspend fun delete(entity: LectureRecordingsEntity)

    @Query("DELETE FROM lecture_recordings")
    suspend fun deleteAll()
}
