package com.example.cookmate.ui.view.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cookmate.MainActivity
import com.example.cookmate.R
import com.example.cookmate.databinding.ActivitySignUpBinding
import com.example.cookmate.ui.viewmodel.SignUpViewModel
import com.example.cookmate.data.repository.AuthRepository
import com.example.cookmate.data.repository.UserRepository
import com.example.cookmate.data.model.User

/**
 * Activity handling user registration
 */
class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    
    private val viewModel: SignUpViewModel by viewModels { 
        SignUpViewModelFactory(AuthRepository(), UserRepository()) 
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTextChangeListeners()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupTextChangeListeners() {
        val afterTextChangedListener = { 
            viewModel.signUpDataChanged(
                binding.usernameField.text.toString(),
                binding.emailField.text.toString(),
                binding.passwordField.text.toString()
            )
        }

        binding.usernameField.doAfterTextChanged { afterTextChangedListener() }
        binding.emailField.doAfterTextChanged { afterTextChangedListener() }
        binding.passwordField.doAfterTextChanged { afterTextChangedListener() }
    }

    private fun setupClickListeners() {
        binding.signupButton.setOnClickListener {
            viewModel.signUp(
                binding.emailField.text.toString(),
                binding.passwordField.text.toString(),
                binding.usernameField.text.toString()
            )
        }

        binding.loginLink.setOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.formState.observe(this) { formState ->
            binding.signupButton.isEnabled = formState.isDataValid

            // Clear previous errors
            binding.usernameInputLayout.error = null
            binding.emailInputLayout.error = null
            binding.passwordInputLayout.error = null

            // Set new errors if any
            formState.usernameError?.let {
                binding.usernameInputLayout.error = it
            }
            formState.emailError?.let {
                binding.emailInputLayout.error = it
            }
            formState.passwordError?.let {
                binding.passwordInputLayout.error = it
            }
            formState.error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.signUpResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                // Navigate to MainActivity
                startActivity(Intent(this, MainActivity::class.java))
                // Finish all previous activities
                finishAffinity()
            }.onFailure { exception ->
                Toast.makeText(
                    this,
                    "Sign up failed: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Factory for creating SignUpViewModel instances
     * @property authRepository Repository for authentication operations
     * @property userRepository Repository for user operations
     */
    class SignUpViewModelFactory(
        private val authRepository: AuthRepository,
        private val userRepository: UserRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SignUpViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SignUpViewModel(authRepository, userRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
