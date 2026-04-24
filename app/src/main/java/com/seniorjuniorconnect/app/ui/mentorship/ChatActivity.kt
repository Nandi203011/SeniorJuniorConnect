package com.seniorjuniorconnect.app.ui.mentorship

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.seniorjuniorconnect.app.BuildConfig
import com.seniorjuniorconnect.app.databinding.ActivityChatBinding
import com.seniorjuniorconnect.app.model.ChatMessage
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: ChatAdapter

    private lateinit var chatId: String
    private lateinit var currentUid: String
    private lateinit var juniorUid: String
    private lateinit var juniorName: String
    private lateinit var seniorUid: String
    private lateinit var seniorName: String

    private var attachedPdfUrl: String = ""
    private var attachedLink: String = ""
    private var attachmentType: String = ""

    companion object {
        private const val PDF_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUid = auth.currentUser?.uid ?: return

        // Get extras from MentorshipAdapter
        juniorUid = intent.getStringExtra("juniorUid") ?: return
        juniorName = intent.getStringExtra("juniorName") ?: return
        seniorUid = intent.getStringExtra("seniorUid") ?: return
        seniorName = intent.getStringExtra("seniorName") ?: return

        // Chat document ID is always junior_senior (consistent for both sides)
        chatId = "${juniorUid}_${seniorUid}"

        // Toolbar title shows the OTHER person's name
        val otherName = if (currentUid == juniorUid) seniorName else juniorName
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = otherName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        // RecyclerView
        adapter = ChatAdapter(emptyList(), currentUid)
        binding.rvMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true // newest messages at bottom
        }
        binding.rvMessages.adapter = adapter

        listenForMessages()
        markMessagesAsSeen()

        // Send button
        binding.btnSend.setOnClickListener {
            sendMessage()
        }

        // Attach PDF
        binding.btnAttachPdf.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/pdf"
            startActivityForResult(intent, PDF_REQUEST_CODE)
        }

        // Attach Link
        binding.btnAttachLink.setOnClickListener {
            val input = EditText(this)
            input.hint = "https://..."
            AlertDialog.Builder(this)
                .setTitle("Attach a link")
                .setView(input)
                .setPositiveButton("Attach") { _, _ ->
                    val link = input.text.toString().trim()
                    if (link.isNotEmpty()) {
                        attachedLink = link
                        attachmentType = "link"
                        attachedPdfUrl = ""
                        showAttachmentPreview("🔗 $link")
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Remove attachment
        binding.btnRemoveAttachment.setOnClickListener {
            clearAttachment()
        }
    }

    // Handle PDF picked from file picker
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PDF_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val pdfUri = data?.data ?: return
            val fileName = pdfUri.lastPathSegment ?: "attachment.pdf"

            showAttachmentPreview("📄 Uploading $fileName...")
            binding.btnSend.isEnabled = false

            uploadToCloudinary(pdfUri, fileName)
        }
    }
    private fun uploadToCloudinary(pdfUri: android.net.Uri, fileName: String) {
        val CLOUD_NAME = BuildConfig.CLOUD_NAME
        val UPLOAD_PRESET = BuildConfig.UPLOAD_PRESET

        Thread {
            try {
                val inputStream = contentResolver.openInputStream(pdfUri)
                val bytes = inputStream?.readBytes() ?: return@Thread
                inputStream.close()

                val client = okhttp3.OkHttpClient()

                val requestBody = okhttp3.MultipartBody.Builder()
                    .setType(okhttp3.MultipartBody.FORM)
                    .addFormDataPart(
                        "file", fileName,
                        bytes.toRequestBody("application/pdf".toMediaType())
                    )
                    .addFormDataPart("upload_preset", UPLOAD_PRESET)
                    .addFormDataPart("resource_type", "raw") // required for PDFs
                    .build()

                val request = okhttp3.Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/$CLOUD_NAME/raw/upload")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                val json = org.json.JSONObject(responseBody ?: "")
                val rawUrl = json.getString("secure_url")
// Force PDF content type by adding fl_attachment flag
                val url = rawUrl.replace("/upload/", "/upload/fl_attachment/")

                runOnUiThread {
                    attachedPdfUrl = url
                    attachedLink = ""
                    attachmentType = "pdf"
                    showAttachmentPreview("📄 $fileName ready to send")
                    binding.btnSend.isEnabled = true
                }

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "PDF upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    clearAttachment()
                    binding.btnSend.isEnabled = true
                }
            }
        }.start()
    }

    private fun showAttachmentPreview(text: String) {
        binding.layoutAttachmentPreview.visibility = View.VISIBLE
        binding.tvAttachmentName.text = text
    }

    private fun clearAttachment() {
        attachedPdfUrl = ""
        attachedLink = ""
        attachmentType = ""
        binding.layoutAttachmentPreview.visibility = View.GONE
        binding.tvAttachmentName.text = ""
    }

    private fun sendMessage() {
        val text = binding.etMessage.text.toString().trim()

        // Must have either text or an attachment
        if (text.isEmpty() && attachmentType.isEmpty()) {
            Toast.makeText(this, "Type a message or attach something", Toast.LENGTH_SHORT).show()
            return
        }

        val attachUrl = if (attachmentType == "pdf") attachedPdfUrl else attachedLink

        val message = hashMapOf(
            "senderUid" to currentUid,
            "text" to text,
            "timestamp" to System.currentTimeMillis(),
            "seen" to false,
            "attachmentUrl" to attachUrl,
            "attachmentType" to attachmentType
        )

        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(message)
            .addOnSuccessListener {
                binding.etMessage.setText("")
                clearAttachment()
                // Scroll to bottom
                binding.rvMessages.scrollToPosition(adapter.itemCount - 1)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to send: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun listenForMessages() {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val messages = snapshot.documents.map { doc ->
                        ChatMessage(
                            id = doc.id,
                            senderUid = doc.getString("senderUid") ?: "",
                            text = doc.getString("text") ?: "",
                            timestamp = doc.getLong("timestamp") ?: 0L,
                            seen = doc.getBoolean("seen") ?: false,
                            attachmentUrl = doc.getString("attachmentUrl") ?: "",
                            attachmentType = doc.getString("attachmentType") ?: ""
                        )
                    }
                    adapter.updateList(messages)
                    // Scroll to latest
                    if (messages.isNotEmpty()) {
                        binding.rvMessages.scrollToPosition(messages.size - 1)
                    }
                    // Mark incoming messages as seen
                    markMessagesAsSeen()
                }
            }
    }

    // Mark all messages NOT sent by current user as seen
    private fun markMessagesAsSeen() {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .whereEqualTo("seen", false)
            .get()
            .addOnSuccessListener { docs ->
                val batch = db.batch()
                docs.documents.forEach { doc ->
                    val senderUid = doc.getString("senderUid") ?: ""
                    if (senderUid != currentUid) {
                        batch.update(doc.reference, "seen", true)
                    }
                }
                batch.commit()
            }
    }
}