package com.example.unit

import com.example.core.repository.AuthRepository
import com.example.features.auth.providers.AuthViewModel
import com.example.features.auth.models.LoginResponse
import com.example.features.auth.models.UserDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.junit.Assert.*
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepository: AuthRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mock(AuthRepository::class.java)
        viewModel = AuthViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun login_success_updatesUiState() = runTest {
        val mockUser = UserDto(
            id = "1", email = "test@student.com", role = "STUDENT", 
            fullName = "Test Student", isMfaEnabled = false
        )
        val mockResponse = LoginResponse("fake_token", mockUser)
        
        when(mockRepository.login("test@student.com", "password", "STUDENT"))
            .thenReturn(Response.success(mockResponse))

        viewModel.login("test@student.com", "password", "STUDENT")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertTrue(state.isSuccess)
    }
}
