package com.example.cookmate.ui.view.activity

import android.content.Intent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import com.example.cookmate.MainActivity
import com.example.cookmate.databinding.ActivityLoginBinding
import com.example.cookmate.ui.viewmodel.LoginViewModel
import com.example.cookmate.ui.login.LoginViewModelFactory

import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        val email = binding.emailField
        val password = binding.passwordField
        val loginButton = binding.signInButton
        val signUpLink = binding.signUpLink

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())[LoginViewModel::class.java]

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // Disable login button unless both email/password are valid
            loginButton.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                email.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        email.afterTextChanged {
            loginViewModel.loginDataChanged(
                email.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    email.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            email.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }
        }

        loginButton.setOnClickListener {
            val emailText = email.text.toString()
            val passwordText = password.text.toString()

            if (emailText.isEmpty() || passwordText.isEmpty()) {
                // Show error if fields are empty
                if (emailText.isEmpty()) email.error = "Email cannot be empty"
                if (passwordText.isEmpty()) password.error = "Password cannot be empty"
                return@setOnClickListener
            }

            firebaseAuth.signInWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign-in success, navigate to MainActivity
                        val user = firebaseAuth.currentUser
                        if (user != null) {
                            // User is signed in
                            Toast.makeText(this, "Welcome, ${user.email}", Toast.LENGTH_LONG).show()

                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        // Sign-in failed, show error message
                        val errorMessage = task.exception?.message ?: "Login failed"
                        // Display the error to the user
                        password.error = errorMessage
                    }
                }
        }


        // Navigate to SignUpActivity when "Sign Up" is clicked
        signUpLink.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        // Unused methods
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        // Unused methods
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}
