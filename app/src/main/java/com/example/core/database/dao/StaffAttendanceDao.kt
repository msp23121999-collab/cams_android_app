package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entities.StaffAttendanceEntity

@Dao
interface StaffAttendanceDao {
    @Query("SELECT * FROM staff_attendance")
    suspend fun getAll(): List<StaffAttendanceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: StaffAttendanceEntity)
}
