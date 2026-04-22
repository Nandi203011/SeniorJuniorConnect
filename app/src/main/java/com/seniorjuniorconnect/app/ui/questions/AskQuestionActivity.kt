package com.seniorjuniorconnect.app.ui.questions

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.seniorjuniorconnect.app.databinding.ActivityAskQuestionBinding

class AskQuestionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAskQuestionBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAskQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        binding.btnPostQuestion.setOnClickListener {
            postQuestion()
        }
    }

    private fun getSelectedTopic(): String {
        return when {
            binding.chipPlacement.isChecked -> "Placement"
            binding.chipProjects.isChecked -> "Projects"
            binding.chipSubjects.isChecked -> "Subjects"
            binding.chipCollegeLife.isChecked -> "College Life"
            binding.chipGeneral.isChecked -> "General"
            else -> "General"
        }
    }

    private fun postQuestion() {
        val questionText = binding.etQuestion.text.toString().trim()
        val topic = getSelectedTopic()

        if (questionText.isEmpty()) {
            binding.tilQuestion.error = "Please write your question"
            return
        }
        if (questionText.length < 10) {
            binding.tilQuestion.error = "Question is too short"
            return
        }

        binding.tilQuestion.error = null
        binding.btnPostQuestion.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        val uid = auth.currentUser?.uid ?: return
        val userDoc = db.collection("users").document(uid)

        userDoc.get().addOnSuccessListener { doc ->
            val name = doc.getString("name") ?: "Anonymous"

            val question = hashMapOf(
                "question" to questionText,
                "topic" to topic,
                "askedBy" to name,
                "askedByUid" to uid,
                "answeredBy" to "",
                "answer" to "",
                "isAnswered" to false,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("questions").add(question)
                .addOnSuccessListener {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this,
                        "Question posted! 🎉", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    binding.progressBar.visibility = View.GONE
                    binding.btnPostQuestion.isEnabled = true
                    Toast.makeText(this,
                        "Error: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}