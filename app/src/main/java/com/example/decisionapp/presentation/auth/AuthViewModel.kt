package com.example.decisionapp.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.decisionapp.domain.model.User
import com.example.decisionapp.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val currentUser: StateFlow<User?> = getCurrentUserUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = loginUseCase(username, password)
            _uiState.value = result.fold(
                onSuccess = { AuthUiState(success = true) },
                onFailure = { AuthUiState(error = it.message) }
            )
        }
    }

    fun register(username: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = registerUseCase(username, password, confirmPassword)
            _uiState.value = result.fold(
                onSuccess = { AuthUiState(success = true) },
                onFailure = { AuthUiState(error = it.message) }
            )
        }
    }

    fun logout() {
        viewModelScope.launch { logoutUseCase() }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}