package com.seniorjuniorconnect.app.ui.senior

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.seniorjuniorconnect.app.MainActivity
import com.seniorjuniorconnect.app.databinding.ActivitySeniorProfileSetupBinding

class SeniorProfileSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeniorProfileSetupBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeniorProfileSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnSaveProfile.setOnClickListener {
            saveProfile()
        }
    }

    private fun getSelectedDomains(): List<String> {
        val domains = mutableListOf<String>()
        if (binding.chipWebDev.isChecked) domains.add("Web Dev")
        if (binding.chipDSA.isChecked) domains.add("DSA")
        if (binding.chipML.isChecked) domains.add("ML/AI")
        if (binding.chipAndroid.isChecked) domains.add("Android")
        if (binding.chipCorCS.isChecked) domains.add("Core CS")
        if (binding.chipGATE.isChecked) domains.add("GATE")
        if (binding.chipMBA.isChecked) domains.add("MBA Prep")
        return domains
    }

    private fun saveProfile() {
        val bio = binding.etBio.text.toString().trim()
        val linkedIn = binding.etLinkedIn.text.toString().trim()
        val subjects = binding.etSubjects.text.toString().trim()
        val placedAt = binding.etPlacedAt.text.toString().trim()
        val finalRound = binding.etFinalRound.text.toString().trim()
        val interviewRound = binding.etInterviewRound.text.toString().trim()
        val domains = getSelectedDomains()

        if (bio.isEmpty()) {
            binding.tilBio.error = "Please add a bio"
            return
        }
        if (domains.isEmpty()) {
            Toast.makeText(this, "Please select at least one domain", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSaveProfile.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        val uid = auth.currentUser?.uid ?: return

        val profileData = hashMapOf(
            "bio" to bio,
            "linkedIn" to linkedIn,
            "subjects" to subjects,
            "domains" to domains,
            "placedAt" to placedAt,
            "finalRound" to finalRound,
            "interviewRound" to interviewRound,
            "profileComplete" to true
        )

        db.collection("users").document(uid)
            .update(profileData as Map<String, Any>)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Profile saved! 🎉", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                binding.btnSaveProfile.isEnabled = true
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}