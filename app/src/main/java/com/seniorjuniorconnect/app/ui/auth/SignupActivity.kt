package com.seniorjuniorconnect.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.seniorjuniorconnect.app.databinding.ActivitySignupBinding
import com.seniorjuniorconnect.app.ui.senior.SeniorProfileSetupActivity

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnSignup.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val college = binding.etCollege.text.toString().trim()
            val branch = binding.etBranch.text.toString().trim()
            val year = binding.etYear.text.toString().trim()
            val role = if (binding.rbSenior.isChecked) "senior" else "junior"

            if (validateInputs(name, email, password, college, branch, year)) {
                registerUser(name, email, password, college, branch, year, role)
            }
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun validateInputs(
        name: String, email: String, password: String,
        college: String, branch: String, year: String
    ): Boolean {
        if (name.isEmpty()) { binding.tilName.error = "Name is required"; return false }
        if (email.isEmpty()) { binding.tilEmail.error = "Email is required"; return false }
        if (password.isEmpty()) { binding.tilPassword.error = "Password is required"; return false }
        if (password.length < 6) { binding.tilPassword.error = "Min 6 characters"; return false }
        if (college.isEmpty()) { binding.tilCollege.error = "College is required"; return false }
        if (branch.isEmpty()) { binding.tilBranch.error = "Branch is required"; return false }
        if (year.isEmpty()) { binding.tilYear.error = "Year is required"; return false }

        binding.tilName.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilCollege.error = null
        binding.tilBranch.error = null
        binding.tilYear.error = null
        return true
    }

    private fun registerUser(
        name: String, email: String, password: String,
        college: String, branch: String, year: String, role: String
    ) {
        binding.btnSignup.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener

                // Save user data to Firestore
                val user = hashMapOf(
                    "uid" to uid,
                    "name" to name,
                    "email" to email,
                    "college" to college,
                    "branch" to branch,
                    "year" to year,
                    "role" to role,
                    "createdAt" to System.currentTimeMillis()
                )

                db.collection("users").document(uid).set(user)
                    .addOnSuccessListener {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this, "Account created! 🎉", Toast.LENGTH_SHORT).show()

                        // Navigate based on role
                        if (role == "senior") {
                            startActivity(Intent(this, SeniorProfileSetupActivity::class.java))
                        } else {
                            startActivity(Intent(this, com.seniorjuniorconnect.app.MainActivity::class.java))
                        }
                        finish()
                    }
                    .addOnFailureListener {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSignup.isEnabled = true
                        Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                binding.btnSignup.isEnabled = true
                Toast.makeText(this, "Signup failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}