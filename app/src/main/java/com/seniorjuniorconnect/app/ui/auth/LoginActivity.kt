package com.seniorjuniorconnect.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.seniorjuniorconnect.app.MainActivity
import com.seniorjuniorconnect.app.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInputs(email, password)) {
                loginUser(email, password)
            }
        }

        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your email first", Toast.LENGTH_SHORT).show()
            } else {
                auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Reset email sent!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            return false
        }
        if (!email.endsWith(".edu") && !email.contains("@")) {
            binding.tilEmail.error = "Enter a valid college email"
            return false
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            return false
        }
        if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            return false
        }
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        return true
    }

    private fun loginUser(email: String, password: String) {
        binding.btnLogin.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
                Toast.makeText(this, "Login failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}