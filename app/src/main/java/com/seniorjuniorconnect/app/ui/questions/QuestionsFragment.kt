package com.seniorjuniorconnect.app.ui.questions

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.seniorjuniorconnect.app.databinding.FragmentQuestionsBinding
import com.seniorjuniorconnect.app.model.Question

class QuestionsFragment : Fragment() {

    private var _binding: FragmentQuestionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: QuestionsAdapter
    private var currentUserRole = "junior"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuestionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Get current user role first
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                currentUserRole = doc.getString("role") ?: "junior"
                setupRecyclerView()
                loadQuestions()

                // Only juniors can ask questions
                if (currentUserRole == "junior") {
                    binding.fabAskQuestion.visibility = View.VISIBLE
                } else {
                    binding.fabAskQuestion.visibility = View.GONE
                }
            }

        binding.fabAskQuestion.setOnClickListener {
            startActivity(Intent(requireContext(), AskQuestionActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        adapter = QuestionsAdapter(emptyList(), currentUserRole) { question ->
            showAnswerDialog(question)
        }
        binding.rvQuestions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvQuestions.adapter = adapter
    }

    private fun loadQuestions() {
        db.collection("questions")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (!isAdded) return@addSnapshotListener
                if (snapshot != null) {
                    val questions = snapshot.documents.map { doc ->
                        Question(
                            id = doc.id,
                            question = doc.getString("question") ?: "",
                            topic = doc.getString("topic") ?: "",
                            askedBy = doc.getString("askedBy") ?: "",
                            askedByUid = doc.getString("askedByUid") ?: "",
                            answeredBy = doc.getString("answeredBy") ?: "",
                            answer = doc.getString("answer") ?: "",
                            isAnswered = doc.getBoolean("isAnswered") ?: false,
                            timestamp = doc.getLong("timestamp") ?: 0L
                        )
                    }
                    adapter.updateList(questions)
                }
            }
    }

    private fun showAnswerDialog(question: Question) {
        val editText = EditText(requireContext())
        editText.hint = "Type your answer..."
        editText.setPadding(48, 32, 48, 32)

        AlertDialog.Builder(requireContext())
            .setTitle("Answer this question")
            .setMessage(question.question)
            .setView(editText)
            .setPositiveButton("Post Answer") { _, _ ->
                val answer = editText.text.toString().trim()
                if (answer.isNotEmpty()) {
                    postAnswer(question, answer)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun postAnswer(question: Question, answer: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: "Senior"
                db.collection("questions").document(question.id)
                    .update(
                        mapOf(
                            "answer" to answer,
                            "answeredBy" to name,
                            "isAnswered" to true
                        )
                    )
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(),
                            "Answer posted! ✅", Toast.LENGTH_SHORT).show()
                    }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}