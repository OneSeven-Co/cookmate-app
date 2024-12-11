package com.example.cookmate.ui.view.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.cookmate.databinding.ActivityLoginBinding
import com.example.cookmate.ui.viewmodel.LoginViewModel
import com.example.cookmate.data.repository.AuthRepository
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import com.example.cookmate.MainActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    
    private val viewModel: LoginViewModel by viewModels { 
        LoginViewModelFactory(AuthRepository()) 
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTextChangeListeners()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupTextChangeListeners() {
        binding.emailField.doAfterTextChanged { text ->
            viewModel.loginDataChanged(
                text.toString(),
                binding.passwordField.text.toString()
            )
        }

        binding.passwordField.doAfterTextChanged { text ->
            viewModel.loginDataChanged(
                binding.emailField.text.toString(),
                text.toString()
            )
        }
    }

    private fun setupClickListeners() {
        binding.signInButton.setOnClickListener {
            viewModel.login(
                binding.emailField.text.toString(),
                binding.passwordField.text.toString()
            )
        }

        binding.signUpLink.setOnClickListener {
            // Navigate to SignUp activity
            startActivity(Intent(this, SignUpActivity::class.java))
            // Don't finish this activity so user can come back
        }
    }

    private fun observeViewModel() {
        viewModel.loginFormState.observe(this) { formState ->
            binding.signInButton.isEnabled = formState.isDataValid
            
            // Clear previous errors
            binding.emailInputLayout.error = null
            binding.passwordInputLayout.error = null
            
            // Set new errors if any
            formState.emailError?.let {
                binding.emailInputLayout.error = it
            }
            
            formState.passwordError?.let {
                binding.passwordInputLayout.error = it
            }
        }

        viewModel.loginResult.observe(this) { result ->
            result.onSuccess { user ->
                // Navigate to MainActivity
                startActivity(Intent(this, MainActivity::class.java))
                // Finish both LoginActivity and WelcomeActivity
                finishAffinity()
            }.onFailure { exception ->
                Toast.makeText(
                    this,
                    "Login failed: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

/**
 * Factory for creating LoginViewModel instances
 */
class LoginViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}