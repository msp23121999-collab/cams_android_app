package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leave_balances")
data class LeaveBalancesEntity(
    @PrimaryKey
    val id: String,
    val dummyData: String = ""
)
