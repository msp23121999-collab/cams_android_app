package com.example.features.admin.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.core.database.dao.UsersDao

class AdminUserViewModelFactory(private val usersDao: UsersDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminUserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminUserViewModel(usersDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
