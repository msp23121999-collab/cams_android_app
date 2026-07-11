package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entities.GrievancesEntity

@Dao
interface GrievancesDao {
    @Query("SELECT * FROM grievances")
    suspend fun getAll(): List<GrievancesEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: GrievancesEntity)
}
