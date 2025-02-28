package com.tech.expencetraker.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.tech.expencetraker.R
import com.tech.expencetraker.ui.home.HomeActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var tvForgotPassword: TextView
    private lateinit var tvSignup: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("users")

        // Initialize UI components
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        tvSignup = findViewById(R.id.tvSignup)
        progressBar = findViewById(R.id.progressBar)

        // Check if user is already logged in
        if (auth.currentUser != null) {
            navigateToHome()
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (validateInput(email, password)) {
                loginUser(email, password)
            }
        }

        tvForgotPassword.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isEmpty()) {
                etEmail.error = "Enter your email first"
            } else {
                resetPassword(email)
            }
        }

        tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                etEmail.error = "Email required"
                false
            }
            password.isEmpty() -> {
                etPassword.error = "Password required"
                false
            }
            password.length < 6 -> {
                etPassword.error = "Password must be at least 6 characters"
                false
            }
            else -> true
        }
    }

    private fun loginUser(email: String, password: String) {
        progressBar.visibility = View.VISIBLE
        btnLogin.isEnabled = false

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    fetchUserData(userId)
                } else {
                    showError("User ID not found!")
                }
            } else {
                showError("Login Failed: ${task.exception?.message}")
            }
        }
    }

    private fun fetchUserData(userId: String) {
        database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                progressBar.visibility = View.GONE
                btnLogin.isEnabled = true

                if (snapshot.exists()) {
                    val name = snapshot.child("name").getValue(String::class.java) ?: "User"
                    Toast.makeText(this@LoginActivity, "Welcome, $name!", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                } else {
                    showError("User data not found!")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showError("Database Error: ${error.message}")
            }
        })
    }

    private fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show()
            } else {
                showError("Failed: ${task.exception?.message}")
            }
        }
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        btnLogin.isEnabled = true
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
