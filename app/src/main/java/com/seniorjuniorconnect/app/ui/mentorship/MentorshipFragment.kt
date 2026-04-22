package com.seniorjuniorconnect.app.ui.mentorship

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.seniorjuniorconnect.app.databinding.FragmentMentorshipBinding
import com.seniorjuniorconnect.app.model.MentorshipRequest

class MentorshipFragment : Fragment() {

    private var _binding: FragmentMentorshipBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: MentorshipAdapter
    private var currentUserRole = "junior"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMentorshipBinding.inflate(inflater, container, false)
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
                loadRequests(uid)
            }
    }

    private fun setupRecyclerView() {
        adapter = MentorshipAdapter(
            emptyList(),
            currentUserRole,
            onAccept = { request -> updateRequestStatus(request, "accepted") },
            onReject = { request -> updateRequestStatus(request, "rejected") }
        )
        binding.rvRequests.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRequests.adapter = adapter
    }

    private fun loadRequests(uid: String) {
        val field = if (currentUserRole == "senior") "seniorUid" else "juniorUid"

        db.collection("mentorship_requests")
            .whereEqualTo(field, uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val requests = snapshot.documents.map { doc ->
                        MentorshipRequest(
                            id = doc.id,
                            juniorUid = doc.getString("juniorUid") ?: "",
                            juniorName = doc.getString("juniorName") ?: "",
                            seniorUid = doc.getString("seniorUid") ?: "",
                            seniorName = doc.getString("seniorName") ?: "",
                            message = doc.getString("message") ?: "",
                            status = doc.getString("status") ?: "pending",
                            timestamp = doc.getLong("timestamp") ?: 0L
                        )
                    }
                    adapter.updateList(requests)
                }
            }
    }

    private fun updateRequestStatus(request: MentorshipRequest, status: String) {
        db.collection("mentorship_requests").document(request.id)
            .update("status", status)
            .addOnSuccessListener {
                val msg = if (status == "accepted") "Request accepted! ✅" else "Request rejected"
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(),
                    "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}