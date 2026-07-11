package com.example.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.database.CamsDatabase
import com.example.core.database.dao.UserDao
import com.example.core.database.entities.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Example Integration Test.
 * Tests components working together, e.g., Room database IO.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DatabaseIntegrationTest {

    private lateinit var db: CamsDatabase
    private lateinit var userDao: UserDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, CamsDatabase::class.java
        ).allowMainThreadQueries().build()
        userDao = db.userDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `insert user and retrieve it`() = runTest {
        // Given
        val user = UserEntity(
            id = "1",
            email = "test@test.com",
            phone = null,
            fullName = "John Doe",
            role = "STUDENT",
            isActive = true,
            departmentId = "CS",
            createdAt = "now",
            updatedAt = "now",
            isDeleted = false,
            deletedAt = null
        )
        
        // When
        userDao.insert(user)
        val loadedUsers = userDao.getAll().first()
        
        // Then
        assertEquals(1, loadedUsers.size)
        assertEquals("John Doe", loadedUsers[0].fullName)
    }
}
