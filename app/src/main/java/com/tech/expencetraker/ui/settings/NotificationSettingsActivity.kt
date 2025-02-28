package com.tech.expencetraker.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import com.tech.expencetraker.R

class NotificationSettingsActivity : AppCompatActivity() {

    private lateinit var switchEnableNotifications: SwitchMaterial
    private lateinit var switchEmailNotifications: SwitchMaterial
    private lateinit var switchPushNotifications: SwitchMaterial
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_settings)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("NotificationSettings", Context.MODE_PRIVATE)

        // Initialize UI elements
        switchEnableNotifications = findViewById(R.id.switchEnableNotifications)
        switchEmailNotifications = findViewById(R.id.switchEmailNotifications)
        switchPushNotifications = findViewById(R.id.switchPushNotifications)

        // Load saved preferences
        switchEnableNotifications.isChecked = sharedPreferences.getBoolean("EnableNotifications", true)
        switchEmailNotifications.isChecked = sharedPreferences.getBoolean("EmailNotifications", true)
        switchPushNotifications.isChecked = sharedPreferences.getBoolean("PushNotifications", true)

        // Set listeners
        switchEnableNotifications.setOnCheckedChangeListener { _, isChecked ->
            savePreference("EnableNotifications", isChecked)
        }

        switchEmailNotifications.setOnCheckedChangeListener { _, isChecked ->
            savePreference("EmailNotifications", isChecked)
        }

        switchPushNotifications.setOnCheckedChangeListener { _, isChecked ->
            savePreference("PushNotifications", isChecked)
        }
    }

    private fun savePreference(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
        Toast.makeText(this, "$key updated", Toast.LENGTH_SHORT).show()
    }
}
