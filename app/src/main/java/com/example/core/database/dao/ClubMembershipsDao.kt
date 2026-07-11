package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entities.ClubMembershipsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClubMembershipsDao {
    @Query("SELECT * FROM club_memberships")
    fun getAll(): Flow<List<ClubMembershipsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ClubMembershipsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ClubMembershipsEntity>)

    @Delete
    suspend fun delete(entity: ClubMembershipsEntity)

    @Query("DELETE FROM club_memberships")
    suspend fun deleteAll()
}
