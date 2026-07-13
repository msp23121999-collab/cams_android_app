package com.example.unit

import com.example.core.repository.PrincipalRepository
import com.example.features.principal.providers.PrincipalApprovalsViewModel
import com.example.features.principal.models.PrincipalLeaveDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class PrincipalViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepository: PrincipalRepository
    private lateinit var viewModel: PrincipalApprovalsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mock(PrincipalRepository::class.java)
        viewModel = PrincipalApprovalsViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadLeaves_updatesStateCorrectly() = runTest {
        val mockLeaves = listOf(
            PrincipalLeaveDto("1", "EMP001", "John Doe", "Sick Leave", "2023-10-01", "2023-10-02", "Pending")
        )
        when(mockRepository.getLeaves()).thenReturn(mockLeaves)

        viewModel.loadLeaves()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(1, state.leaves.size)
        assertEquals("John Doe", state.leaves[0].facultyName)
    }
}
