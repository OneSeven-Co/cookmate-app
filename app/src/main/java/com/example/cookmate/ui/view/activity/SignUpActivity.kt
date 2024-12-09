package com.example.cookmate.ui.view.activity

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cookmate.R
import com.example.cookmate.data.firebase.FirebaseMethods.createUser
import com.example.cookmate.ui.view.fragment.HomeFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var usernameField: TextInputEditText
    private lateinit var emailField: TextInputEditText
    private lateinit var passwordField: TextInputEditText
    private lateinit var signupButton: MaterialButton
    private lateinit var errorTextView: TextView
    private lateinit var loginText: TextView
    private lateinit var termsAndServiceText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        usernameField = findViewById(R.id.usernameField)
        emailField = findViewById(R.id.emailField)
        passwordField = findViewById(R.id.passwordField)
        signupButton = findViewById(R.id.signupButton)
        errorTextView = findViewById(R.id.loginLink)
        loginText = findViewById(R.id.loginText)
        termsAndServiceText = findViewById(R.id.termsAndServiceText)

        signupButton.setOnClickListener {
            val username = usernameField.text.toString().trim()
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, R.string.error_invalid_email, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            checkIfUsernameExists(username, email, password)
        }
    }

    private fun checkIfUsernameExists(username: String, email: String, password: String) {
        firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    Toast.makeText(this, R.string.error_name_taken, Toast.LENGTH_LONG).show()
                } else {
                    createUser(
                        email, password, username,
                        onComplete = { navigateToHomeScreen() }
                    )
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "${R.string.check_error}${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun fireStoreUserInit(userId: String, username: String) {
        val userMap = hashMapOf(
            "username" to username,
            "favouriteRecipes" to emptyList<String>(),
            "createdRecipes" to emptyList<String>(),
            "authLevel" to "User"
        )

        firestore.collection("users").document(userId)
            .set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                navigateToHomeScreen()
            }
            .addOnFailureListener { exception: Exception ->
                Toast.makeText(this, "Failed to save username: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun handleSignUpError(exception: Exception?) {
        errorTextView.visibility = View.VISIBLE
        when (exception) {
            is FirebaseAuthUserCollisionException -> {
                Toast.makeText(this, R.string.error_user_exists, Toast.LENGTH_LONG).show()
            }
            is FirebaseAuthInvalidCredentialsException -> {
                Toast.makeText(this, R.string.error_invalid_email, Toast.LENGTH_LONG).show()
            }
            else -> {
                Toast.makeText(this, R.string.error_unknown, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun navigateToHomeScreen() {
        val homeIntent = Intent(this, HomeFragment::class.java)
        startActivity(homeIntent)
        finish()
    }
}
