package com.seniorjuniorconnect.app.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.seniorjuniorconnect.app.databinding.ActivitySplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        lifecycleScope.launch {
            delay(2000) // 2 second splash
            checkAuthAndNavigate()
        }
    }

    private fun checkAuthAndNavigate() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User already logged in → go to MainActivity
            startActivity(Intent(this, com.seniorjuniorconnect.app.MainActivity::class.java))
        } else {
            // Not logged in → go to Login screen
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}