package com.tech.expencetraker.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.tech.expencetraker.R

class AboutSupportActivity : AppCompatActivity() {

    private lateinit var tvAppVersion: TextView
    private lateinit var tvDeveloperInfo: TextView
    private lateinit var btnContactSupport: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_support)

        // Initialize Views
        tvAppVersion = findViewById(R.id.tvAppVersion)
        tvDeveloperInfo = findViewById(R.id.tvDeveloperInfo)
        btnContactSupport = findViewById(R.id.btnContactSupport)

        // Set App Version Manually (if needed)
        tvAppVersion.text = "App Version: 1.0.0"  // Replace with actual version if necessary

        // Set up Contact Support button
        btnContactSupport.setOnClickListener {
            sendSupportEmail()
        }
    }

    private fun sendSupportEmail() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:support@example.com")
            putExtra(Intent.EXTRA_SUBJECT, "Support Request - Expense Tracker App")
        }
        startActivity(Intent.createChooser(emailIntent, "Send Email"))
    }
}
