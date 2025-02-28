package com.tech.expencetraker.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.tech.expencetraker.R
import com.tech.expencetraker.ui.auth.LoginActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var ivProfileImage: ImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var btnEditProfile: Button
    private lateinit var btnLogout: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Bind views
        ivProfileImage = findViewById(R.id.ivProfileImage)
        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        btnLogout = findViewById(R.id.btnLogout)

        // Load user details
        loadUserProfile()

        // Logout button click listener
        btnLogout.setOnClickListener {
            logoutUser()
        }

        // Edit profile click listener (Placeholder for future implementation)
        btnEditProfile.setOnClickListener {
            // You can implement an Edit Profile feature later
            // startActivity(Intent(this, EditProfileActivity::class.java))
        }
    }

    private fun loadUserProfile() {
        val user: FirebaseUser? = auth.currentUser
        if (user != null) {
            tvUserName.text = user.displayName ?: "No Name Available"
            tvUserEmail.text = user.email ?: "No Email Available"
        }
    }

    private fun logoutUser() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}