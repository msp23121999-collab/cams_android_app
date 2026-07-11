package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fee_structure")
data class FeeStructureEntity(
    @PrimaryKey
    val id: String,
    val dummyData: String = ""
)
