package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "club_memberships")
data class ClubMembershipsEntity(
    @PrimaryKey
    val id: String,
    val dummyData: String = ""
)
