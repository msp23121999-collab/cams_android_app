package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entities.ApiCacheEntity

@Dao
interface ApiCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(cache: ApiCacheEntity)

    @Query("SELECT * FROM api_cache WHERE url = :url")
    fun get(url: String): ApiCacheEntity?

    @Query("DELETE FROM api_cache")
    fun clearAll()
}
