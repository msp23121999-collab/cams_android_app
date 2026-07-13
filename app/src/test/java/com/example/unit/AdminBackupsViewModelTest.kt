package com.example.unit

import com.example.core.repository.AdminRepository
import com.example.features.admin.providers.AdminBackupsViewModel
import com.example.features.admin.models.AdminBackup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class AdminBackupsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepository: AdminRepository
    private lateinit var viewModel: AdminBackupsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mock(AdminRepository::class.java)
        viewModel = AdminBackupsViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadBackups_success_updatesUiState() = runTest {
        val mockData = listOf(
            AdminBackup("1", "backup_01.sql", 1024L, "COMPLETED", "2023-10-01")
        )
        when(mockRepository.getBackupsHistory()).thenReturn(mockData)

        viewModel.loadBackups()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(1, state.backups.size)
        assertEquals("backup_01.sql", state.backups[0].filename)
    }
}
