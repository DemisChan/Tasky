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
    fun `when token is valid then isAuthenticated is true`() = runTest {
        coEvery { tokenManager.isTokenValid() } returns true
        viewModel = MainViewModel(tokenManager)

        advanceUntilIdle()

        assertEquals(true, viewModel.isAuthenticated)
    }

    @Test
    fun `when token is invalid then isAuthenticated is false`() = runTest {
        coEvery { tokenManager.isTokenValid() } returns false
        viewModel = MainViewModel(tokenManager)

        advanceUntilIdle()

        assertEquals(false, viewModel.isAuthenticated)
    }

    @Test
    fun `checks token validity on initialization`() = runTest {
        coEvery { tokenManager.isTokenValid() } returns true
        viewModel = MainViewModel(tokenManager)

        advanceUntilIdle()

        coVerify(exactly = 1) { tokenManager.isTokenValid() }
    }
}