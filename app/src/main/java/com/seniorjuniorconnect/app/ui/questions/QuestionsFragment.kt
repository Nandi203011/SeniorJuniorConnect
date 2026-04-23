package com.seniorjuniorconnect.app.ui.questions

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
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog

class QuestionsFragment : Fragment() {

    private var _binding: FragmentQuestionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: QuestionsAdapter
    private var currentUserRole = "junior"
    private var allQuestions = listOf<Question>()
    private var selectedFilter = "All"

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
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                currentUserRole = doc.getString("role") ?: "junior"
                setupRecyclerView()
                loadQuestions()

                if (currentUserRole == "junior") {
                    binding.fabAskQuestion.visibility = View.VISIBLE
                } else {
                    binding.fabAskQuestion.visibility = View.GONE
                }

                // Move chip listener INSIDE here so adapter is ready
                binding.chipGroupFilter.setOnCheckedChangeListener { _, checkedId ->
                    selectedFilter = when (checkedId) {
                        binding.chipFilterPlacement.id -> "Placement"
                        binding.chipFilterProjects.id -> "Projects"
                        binding.chipFilterSubjects.id -> "Subjects"
                        binding.chipFilterCollegeLife.id -> "College Life"
                        binding.chipFilterGeneral.id -> "General"
                        else -> "All"
                    }
                    filterQuestions()

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
                    allQuestions = snapshot.documents.map { doc ->
                        Question(
                            id = doc.id,
                            question = doc.getString("question") ?: "",
                            topic = doc.getString("topic") ?: "",
                            askedBy = doc.getString("askedBy") ?: "",
                            askedByUid = doc.getString("askedByUid") ?: "",
                            answeredBy = doc.getString("answeredBy") ?: "",
                            answer = doc.getString("answer") ?: "",
                            attachmentUrl = doc.getString("attachmentUrl") ?: "",
                            attachmentType = doc.getString("attachmentType") ?: "",
                            isAnswered = doc.getBoolean("isAnswered") ?: false,
                            timestamp = doc.getLong("timestamp") ?: 0L
                        )
                    }
                    filterQuestions()
                }
            }
    }

    private fun filterQuestions() {
        if (!::adapter.isInitialized) return  // Add this line
        val filtered = if (selectedFilter == "All") allQuestions
        else allQuestions.filter { it.topic == selectedFilter }
        adapter.updateList(filtered)
    }
    private var selectedFileUri: Uri? = null
    private var selectedFileType: String = ""
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedFileUri = it
            val mimeType = requireContext().contentResolver.getType(it) ?: ""
            selectedFileType = when {
                mimeType.contains("pdf") -> "pdf"
                mimeType.contains("image") -> "image"
                else -> "file"
            }
            // Update the status text inside the dialog
            fileStatusTextView?.text = "✅ ${selectedFileType.uppercase()} selected!"
            fileStatusTextView?.setTextColor(
                android.graphics.Color.parseColor("#4CAF50")
            )
            Toast.makeText(requireContext(),
                "✅ ${selectedFileType.uppercase()} selected!", Toast.LENGTH_SHORT).show()
        }
    }

    private var currentAnswerDialog: AlertDialog? = null
    private var fileStatusTextView: android.widget.TextView? = null

    private fun showAnswerDialog(question: Question) {
        selectedFileUri = null
        selectedFileType = ""

        val context = requireContext()

        val answerInput = EditText(context).apply {
            hint = "Type your answer..."
            setPadding(48, 32, 48, 16)
            minLines = 3
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
        }

        val linkInput = EditText(context).apply {
            hint = "Attach a link (optional) e.g. https://..."
            setPadding(48, 8, 48, 16)
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_URI
        }

        val uploadFileBtn = com.google.android.material.button.MaterialButton(context).apply {
            text = "📄 Upload PDF"
            setBackgroundColor(android.graphics.Color.parseColor("#2D2D44"))
            setTextColor(android.graphics.Color.WHITE)
            setPadding(48, 16, 48, 16)
            val lp = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.setMargins(0, 8, 0, 8)
            layoutParams = lp
            setOnClickListener {
                filePickerLauncher.launch("application/pdf")
            }
        }

        val uploadImageBtn = com.google.android.material.button.MaterialButton(context).apply {
            text = "🖼️ Upload Image"
            setBackgroundColor(android.graphics.Color.parseColor("#2D2D44"))
            setTextColor(android.graphics.Color.WHITE)
            setPadding(48, 16, 48, 16)
            val lp = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.setMargins(0, 0, 0, 8)
            layoutParams = lp
            setOnClickListener {
                filePickerLauncher.launch("image/*")
            }
        }

        val fileStatusText = android.widget.TextView(context).apply {
            text = "No file selected"
            setTextColor(android.graphics.Color.parseColor("#B0B0CC"))
            textSize = 12f
            setPadding(48, 0, 48, 16)
        }
        fileStatusTextView = fileStatusText

        val dividerLabel = android.widget.TextView(context).apply {
            text = "── OR attach a link ──"
            setTextColor(android.graphics.Color.parseColor("#6C63FF"))
            textSize = 12f
            setPadding(48, 8, 48, 4)
            gravity = android.view.Gravity.CENTER
        }

        val container = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 16, 48, 0)
            addView(answerInput)
            addView(uploadFileBtn)
            addView(uploadImageBtn)
            addView(fileStatusText)
            addView(dividerLabel)
            addView(linkInput)
        }

        // Update file status dynamically
        filePickerLauncher
        // already registered above

        val dialog = AlertDialog.Builder(context)
            .setTitle("Answer: ${question.topic}")
            .setMessage(question.question)
            .setView(container)
            .setPositiveButton("Post Answer") { _, _ ->
                val answer = answerInput.text.toString().trim()
                val link = linkInput.text.toString().trim()
                if (answer.isEmpty()) {
                    Toast.makeText(requireContext(),
                        "Answer cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                postAnswerWithAttachment(question, answer, link)
            }
            .setNegativeButton("Cancel", null)
            .create()

        currentAnswerDialog = dialog
        dialog.show()
    }

    private fun postAnswerWithAttachment(
        question: Question, answer: String, link: String
    ) {
        val uid = auth.currentUser?.uid ?: return
        val storage = com.google.firebase.storage.FirebaseStorage.getInstance()

        binding.root.post {
            Toast.makeText(requireContext(), "Posting answer...", Toast.LENGTH_SHORT).show()
        }

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: "Senior"

                if (selectedFileUri != null) {
                    // Upload file to Firebase Storage first
                    val fileRef = storage.reference
                        .child("answers/${question.id}/$selectedFileType-${System.currentTimeMillis()}")

                    fileRef.putFile(selectedFileUri!!)
                        .addOnSuccessListener {
                            fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                                saveAnswer(question, answer, name,
                                    downloadUri.toString(), selectedFileType)
                            }
                        }
                        .addOnFailureListener {
                            if (isAdded) Toast.makeText(requireContext(),
                                "File upload failed: ${it.message}",
                                Toast.LENGTH_LONG).show()
                        }
                } else {
                    // No file, just save answer with optional link
                    saveAnswer(question, answer, name, link,
                        if (link.isNotEmpty()) "link" else "")
                }
            }
    }

    private fun saveAnswer(
        question: Question, answer: String, name: String,
        attachmentUrl: String, attachmentType: String
    ) {
        val updateMap = mutableMapOf<String, Any>(
            "answer" to answer,
            "answeredBy" to name,
            "isAnswered" to true,
            "attachmentUrl" to attachmentUrl,
            "attachmentType" to attachmentType
        )
        db.collection("questions").document(question.id)
            .update(updateMap)
            .addOnSuccessListener {
                if (isAdded) Toast.makeText(requireContext(),
                    "Answer posted! ✅", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                if (isAdded) Toast.makeText(requireContext(),
                    "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}