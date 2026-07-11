package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "salary_slips")
data class SalarySlipsEntity(
    @PrimaryKey
    val id: String,
    val dummyData: String = ""
)
