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
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tech.expencetraker.R
import com.tech.expencetraker.ui.home.HomeActivity
import java.security.MessageDigest

class SignupActivity : AppCompatActivity() {

    private lateinit var etFullNameO: TextInputLayout
    private lateinit var etFullName: TextInputEditText
    private lateinit var etEmailO: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPasswordO: TextInputLayout
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPasswordO: TextInputLayout
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnSignup: MaterialButton
    private lateinit var tvLoginRedirect: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize Views
        etFullNameO = findViewById(R.id.etFullNameO)
        etFullName = findViewById(R.id.etFullName)
        etEmailO = findViewById(R.id.etEmailO)
        etEmail = findViewById(R.id.etEmail)
        etPasswordO = findViewById(R.id.etPasswordO)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPasswordO = findViewById(R.id.etConfirmPasswordO)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnSignup = findViewById(R.id.btnSignup)
        tvLoginRedirect = findViewById(R.id.tvLoginRedirect)
        progressBar = findViewById(R.id.progressBar)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("users")

        Log.d("SignupActivity", "Views initialized successfully")

        btnSignup.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (validateInput(fullName, email, password, confirmPassword)) {
                registerUser(fullName, email, password)
            }
        }

        tvLoginRedirect.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun validateInput(fullName: String, email: String, password: String, confirmPassword: String): Boolean {
        var isValid = true

        if (fullName.isEmpty()) {
            etFullNameO.error = "Full Name is required"
            isValid = false
        } else {
            etFullNameO.error = null
        }

        if (email.isEmpty()) {
            etEmailO.error = "Email is required"
            isValid = false
        } else {
            etEmailO.error = null
        }

        if (password.isEmpty()) {
            etPasswordO.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            etPasswordO.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            etPasswordO.error = null
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPasswordO.error = "Confirm your password"
            isValid = false
        } else if (password != confirmPassword) {
            etConfirmPasswordO.error = "Passwords do not match"
            isValid = false
        } else {
            etConfirmPasswordO.error = null
        }

        return isValid
    }

    private fun registerUser(fullName: String, email: String, password: String) {
        progressBar.visibility = View.VISIBLE

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                    // Hash the password before storing it
                    val hashedPassword = hashPassword(password)

                    val userMap = mapOf(
                        "fullName" to fullName,
                        "email" to email,
                        "userId" to userId,
                        "password" to hashedPassword // Store hashed password instead of plain text
                    )

                    database.child(userId).setValue(userMap)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Signup successful!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to save user data!", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Signup failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Function to hash password using SHA-256
    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }
}
