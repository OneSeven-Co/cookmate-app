package com.example.cookmate.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cookmate.data.model.User
import com.example.cookmate.data.repository.AuthRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for handling login-related business logic
 */
class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
    
    private val _loginResult = MutableLiveData<Result<User>>()
    val loginResult: LiveData<Result<User>> = _loginResult

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    /**
     * Attempts to log in the user
     * @param email User's email
     * @param password User's password
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            authRepository.login(email, password).collect { result ->
                _loginResult.value = result
            }
        }
    }

    /**
     * Validates the login form data
     * @param email User's email
     * @param password User's password
     */
    fun loginDataChanged(email: String, password: String) {
        // Clear previous errors and start with invalid state
        val currentState = LoginFormState(isDataValid = false)

        // Only validate completed fields
        if (email.isEmpty() && password.isEmpty()) {
            _loginForm.value = currentState
            return
        }

        // Validate email only if user has finished typing
        if (email.isNotEmpty() && !isEmailValid(email)) {
            _loginForm.value = currentState.copy(emailError = "Invalid email address")
            return
        }

        // Validate password only if user has finished typing
        if (password.isNotEmpty() && !isPasswordValid(password)) {
            _loginForm.value = currentState.copy(passwordError = "Password must be at least 6 characters")
            return
        }

        // Both fields are valid and not empty
        if (email.isNotEmpty() && password.isNotEmpty() && 
            isEmailValid(email) && isPasswordValid(password)) {
            _loginForm.value = currentState.copy(isDataValid = true)
        } else {
            _loginForm.value = currentState
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return if (email.isBlank()) false else {
            android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        return if (password.isBlank()) false else {
            password.length >= 6
        }
    }
}

/**
 * Data class representing the state of the login form
 */
data class LoginFormState(
    val emailError: String? = null,
    val passwordError: String? = null,
    val isDataValid: Boolean = false
)