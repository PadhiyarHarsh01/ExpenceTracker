package com.tech.expencetraker

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.tech.expencetraker.ui.auth.LoginActivity
import com.tech.expencetraker.ui.home.HomeActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Ensure you have a simple loading UI here

        // Navigate after a short delay
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, 1500) // 1.5 seconds delay for a quick transition
    }

    private fun navigateToNextScreen() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val nextActivity = if (currentUser != null) HomeActivity::class.java else LoginActivity::class.java
        startActivity(Intent(this, nextActivity))
        finish()
    }
}
