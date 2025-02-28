package com.tech.expencetraker.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.tech.expencetraker.R
import com.tech.expencetraker.ui.auth.LoginActivity
import com.tech.expencetraker.ui.home.HomeActivity

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // Load fade-in animation
        val logo = findViewById<ImageView>(R.id.logo)
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        logo.startAnimation(fadeIn)

        // Navigate after delay
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, 2000) // Reduced delay to 2 seconds for a smoother experience
    }

    private fun navigateToNextScreen() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val nextActivity = if (currentUser != null) HomeActivity::class.java else LoginActivity::class.java
        startActivity(Intent(this, nextActivity))
        finish()
    }
}
