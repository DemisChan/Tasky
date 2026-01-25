package com.dmd.tasky

import com.dmd.tasky.core.data.token.TokenManager
import com.dmd.tasky.features.auth.presentation.MainCoroutineRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var tokenManager: TokenManager
    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        tokenManager = mockk()
    }

    @Test
    fun `MainState defaults have correct values`() {
        // Test the data class defaults directly - no coroutines involved
        val defaultState = MainState()

        assertEquals(true, defaultState.isCheckingAuth)
        assertEquals(false, defaultState.isLoggedIn)
    }

    @Test
    fun `when token is valid then isLoggedIn is true and isCheckingAuth is false`() = runTest {
        coEvery { tokenManager.isTokenValid() } returns true
        viewModel = MainViewModel(tokenManager)

        advanceUntilIdle()

        assertEquals(false, viewModel.state.isCheckingAuth)
        assertEquals(true, viewModel.state.isLoggedIn)
    }

    @Test
    fun `when token is invalid then isLoggedIn is false and isCheckingAuth is false`() = runTest {
        coEvery { tokenManager.isTokenValid() } returns false
        viewModel = MainViewModel(tokenManager)

        advanceUntilIdle()

        assertEquals(false, viewModel.state.isCheckingAuth)
        assertEquals(false, viewModel.state.isLoggedIn)
    }

    @Test
    fun `checks token validity on initialization`() = runTest {
        coEvery { tokenManager.isTokenValid() } returns true
        viewModel = MainViewModel(tokenManager)

        advanceUntilIdle()

        coVerify(exactly = 1) { tokenManager.isTokenValid() }
    }
}
