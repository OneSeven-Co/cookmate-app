package com.example.cookmate.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cookmate.data.model.User
import com.example.cookmate.data.repository.AuthRepository
import com.example.cookmate.data.repository.UserRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for handling sign-up related business logic
 */
class SignUpViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _signUpResult = MutableLiveData<Result<User>>()
    val signUpResult: LiveData<Result<User>> = _signUpResult

    private val _formState = MutableLiveData<SignUpFormState>()
    val formState: LiveData<SignUpFormState> = _formState

    /**
     * Attempts to create a new user account
     */
    fun signUp(email: String, password: String, username: String) {
        // Only proceed if form is valid
        if (_formState.value?.isDataValid != true) {
            return
        }
        
        viewModelScope.launch {
            authRepository.createUser(email, password, username).collect { result ->
                _signUpResult.value = result
            }
        }
    }

    /**
     * Checks if a username is already taken
     */
    fun checkUsername(username: String) {
        viewModelScope.launch {
            userRepository.checkUsernameExists(username).collect { result ->
                result.onSuccess { exists ->
                    if (exists) {
                        _formState.value = SignUpFormState(usernameError = "Username already taken")
                    } else {
                        _formState.value = SignUpFormState(isDataValid = true)
                    }
                }.onFailure {
                    _formState.value = SignUpFormState(error = "Error checking username")
                }
            }
        }
    }

    /**
     * Validates the sign-up form data
     * @param username User's username
     * @param email User's email
     * @param password User's password
     */
    fun signUpDataChanged(username: String, email: String, password: String) {
        val currentState = SignUpFormState(isDataValid = false)

        // Skip validation if all fields are empty
        if (areAllFieldsEmpty(username, email, password)) {
            _formState.value = currentState
            return
        }

        // Validate individual fields
        validateUsername(username, currentState)?.let { 
            _formState.value = it
            return 
        }

        validateEmail(email, currentState)?.let { 
            _formState.value = it
            return 
        }

        validatePassword(password, currentState)?.let { 
            _formState.value = it
            return 
        }

        // Check if all fields are valid and complete
        if (areAllFieldsValid(username, email, password)) {
            checkUsernameAvailability(username, currentState)
        } else {
            _formState.value = currentState
        }
    }

    private fun areAllFieldsEmpty(username: String, email: String, password: String): Boolean {
        return username.isEmpty() && email.isEmpty() && password.isEmpty()
    }

    private fun validateUsername(username: String, currentState: SignUpFormState): SignUpFormState? {
        return if (username.isNotEmpty() && username.isBlank()) {
            currentState.copy(usernameError = "Username cannot be empty")
        } else null
    }

    private fun validateEmail(email: String, currentState: SignUpFormState): SignUpFormState? {
        return if (email.isNotEmpty() && !isEmailValid(email)) {
            currentState.copy(emailError = "Invalid email address")
        } else null
    }

    private fun validatePassword(password: String, currentState: SignUpFormState): SignUpFormState? {
        return if (password.isNotEmpty() && !isPasswordValid(password)) {
            currentState.copy(passwordError = "Password must be at least 6 characters")
        } else null
    }

    private fun areAllFieldsValid(username: String, email: String, password: String): Boolean {
        return username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() &&
               !username.isBlank() && isEmailValid(email) && isPasswordValid(password)
    }

    private fun checkUsernameAvailability(username: String, currentState: SignUpFormState) {
        viewModelScope.launch {
            userRepository.checkUsernameExists(username).collect { result ->
                result.onSuccess { exists ->
                    _formState.value = if (exists) {
                        currentState.copy(usernameError = "Username already taken")
                    } else {
                        currentState.copy(isDataValid = true)
                    }
                }.onFailure {
                    _formState.value = currentState.copy(error = "Error checking username")
                }
            }
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }
}

/**
 * Data class representing the state of the sign-up form
 */
data class SignUpFormState(
    val usernameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val error: String? = null,
    val isDataValid: Boolean = false
) 