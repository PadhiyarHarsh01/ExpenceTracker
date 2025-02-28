package com.tech.expencetraker.ui.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.button.MaterialButton
import com.tech.expencetraker.R
import com.tech.expencetraker.ui.auth.LoginActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var switchTheme: androidx.appcompat.widget.SwitchCompat
    private lateinit var btnChangePassword: MaterialButton
    private lateinit var btnDeleteAccount: MaterialButton
    private lateinit var btnNotificationSettings: MaterialButton
    private lateinit var btnAboutSupport: MaterialButton
    private lateinit var btnLogout: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize UI elements
        switchTheme = findViewById(R.id.switchTheme)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount)
        btnNotificationSettings = findViewById(R.id.btnNotificationSettings)
        btnAboutSupport = findViewById(R.id.btnAboutSupport)
        btnLogout = findViewById(R.id.btnLogout)

        // Shared Preferences for Theme
        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("DarkMode", false)
        switchTheme.isChecked = isDarkMode
        updateTheme(isDarkMode)

        // Toggle Dark Mode
        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("DarkMode", isChecked).apply()
            updateTheme(isChecked)
        }

        // Change Password Click
        btnChangePassword.setOnClickListener {
            Toast.makeText(this, "Change Password Clicked", Toast.LENGTH_SHORT).show()
            // TODO: Implement Change Password functionality
        }

        // Delete Account Click
        btnDeleteAccount.setOnClickListener {
            Toast.makeText(this, "Delete Account Clicked", Toast.LENGTH_SHORT).show()
            // TODO: Implement Delete Account functionality
        }

        // ✅ Connect Notification Settings Screen ✅
        btnNotificationSettings.setOnClickListener {
            startActivity(Intent(this, NotificationSettingsActivity::class.java))
        }

        // About & Support Click
        btnAboutSupport.setOnClickListener {
            Toast.makeText(this, "About & Support Clicked", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to About & Support Screen
        }

        // Logout Click
        btnLogout.setOnClickListener {
            logoutUser()
        }
    }

    private fun updateTheme(isDarkMode: Boolean) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun logoutUser() {
        sharedPreferences.edit().clear().apply()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        // Redirect to Login Screen
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
