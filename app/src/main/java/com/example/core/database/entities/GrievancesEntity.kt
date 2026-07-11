package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grievances")
data class GrievancesEntity(
    @PrimaryKey
    val id: String,
    val dummyData: String = ""
)
