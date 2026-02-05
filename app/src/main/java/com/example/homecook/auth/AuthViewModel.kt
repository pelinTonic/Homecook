package com.example.homecook.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(
        AuthUiState(isLoggedIn = auth.currentUser != null)
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        val trimmed = email.trim()
        if (trimmed.isEmpty() || password.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Email and password are required.")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        auth.signInWithEmailAndPassword(trimmed, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _uiState.value = AuthUiState(isLoggedIn = true)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = task.exception?.localizedMessage ?: "Login failed."
                    )
                }
            }
    }

    fun register(email: String, password: String, confirm: String) {
        val trimmed = email.trim()
        if (trimmed.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "All fields are required.")
            return
        }
        if (password != confirm) {
            _uiState.value = _uiState.value.copy(errorMessage = "Passwords do not match.")
            return
        }
        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(errorMessage = "Password must be at least 6 characters.")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        auth.createUserWithEmailAndPassword(trimmed, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _uiState.value = AuthUiState(isLoggedIn = true)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = task.exception?.localizedMessage ?: "Registration failed."
                    )
                }
            }
    }

    fun logout() {
        auth.signOut()
        _uiState.value = AuthUiState(isLoggedIn = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
