package com.dmd.tasky

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.dmd.tasky.ui.navigation.TaskyNavHost
import com.dmd.tasky.ui.theme.TaskyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep splash visible while checking auth status
        // Once isCheckingAuth becomes false, splash dismisses regardless of auth result
        installSplashScreen().setKeepOnScreenCondition {
            viewModel.state.isCheckingAuth
        }

        enableEdgeToEdge()
        setContent {
            // Only render content after auth check completes
            if (!viewModel.state.isCheckingAuth) {
                TaskyTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        TaskyNavHost(
                            modifier = Modifier.padding(innerPadding),
                            isAuthenticated = viewModel.state.isLoggedIn
                        )
                    }
                }
            }
        }
    }
}
