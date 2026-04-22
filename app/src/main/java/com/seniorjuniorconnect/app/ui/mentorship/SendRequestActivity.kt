package com.seniorjuniorconnect.app.ui.mentorship

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.seniorjuniorconnect.app.databinding.ActivitySendRequestBinding

class SendRequestActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySendRequestBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var seniorUid = ""
    private var seniorName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySendRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Get senior details passed from HomeFragment
        seniorUid = intent.getStringExtra("seniorUid") ?: ""
        seniorName = intent.getStringExtra("seniorName") ?: ""

        binding.tvSeniorName.text = "Sending request to: $seniorName"

        binding.btnSendRequest.setOnClickListener {
            sendRequest()
        }
    }

    private fun sendRequest() {
        val message = binding.etMessage.text.toString().trim()

        if (message.isEmpty()) {
            binding.tilMessage.error = "Please write a message"
            return
        }
        if (message.length < 20) {
            binding.tilMessage.error = "Please write a bit more"
            return
        }

        binding.tilMessage.error = null
        binding.btnSendRequest.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val juniorName = doc.getString("name") ?: "Junior"

                val request = hashMapOf(
                    "juniorUid" to uid,
                    "juniorName" to juniorName,
                    "seniorUid" to seniorUid,
                    "seniorName" to seniorName,
                    "message" to message,
                    "status" to "pending",
                    "timestamp" to System.currentTimeMillis()
                )

                db.collection("mentorship_requests").add(request)
                    .addOnSuccessListener {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this,
                            "Request sent! 🎉", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSendRequest.isEnabled = true
                        Toast.makeText(this,
                            "Error: ${it.message}", Toast.LENGTH_LONG).show()
                    }
            }
    }
}