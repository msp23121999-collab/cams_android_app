package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_materials")
data class StudyMaterialsEntity(
    @PrimaryKey
    val id: String,
    val dummyData: String = ""
)
