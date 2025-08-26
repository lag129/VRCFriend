package net.lag129.vrcfriend

import io.github.vrchatapi.model.CurrentUser

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object RequiresEmailOtp : AuthState()
    object RequiresTwoFactorAuth : AuthState()
    data class Success(val user: CurrentUser) : AuthState()
    data class Error(val message: String) : AuthState()
}
