package net.lag129.vrcfriend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import net.lag129.vrcfriend.ui.screens.AuthSuccessScreen
import net.lag129.vrcfriend.ui.screens.EmailOtpScreen
import net.lag129.vrcfriend.ui.screens.LoginScreen
import net.lag129.vrcfriend.ui.screens.TwoFactorAuthScreen
import net.lag129.vrcfriend.ui.theme.VRCFriendTheme
import net.lag129.vrcfriend.viewmodel.AuthViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            VRCFriendTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()
                val authState by authViewModel.authState.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    NavHost(
                        navController = navController,
                        startDestination = "login"
                    ) {
                        composable("login") {
                            LoginScreen(
                                authViewModel = authViewModel,
                                onNavigateToEmailOtp = {
                                    navController.navigate("emailOtp")
                                },
                                onNavigateToTwoFactorAuth = {
                                    navController.navigate("twoFactorAuth")
                                },
                                onNavigateToSuccess = {
                                    navController.navigate("success") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("emailOtp") {
                            EmailOtpScreen(
                                authViewModel = authViewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateToSuccess = {
                                    navController.navigate("success") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("twoFactorAuth") {
                            TwoFactorAuthScreen(
                                authViewModel = authViewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateToSuccess = {
                                    navController.navigate("success") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("success") {
                            when (authState) {
                                is AuthState.Success -> {
                                    AuthSuccessScreen(
                                        authViewModel = authViewModel
                                    )
                                }

                                else -> {
                                    navController.navigate("login") {
                                        popUpTo("success") { inclusive = true }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

