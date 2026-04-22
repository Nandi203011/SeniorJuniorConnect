package com.seniorjuniorconnect.app.ui.junior

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.seniorjuniorconnect.app.databinding.FragmentHomeBinding
import com.seniorjuniorconnect.app.model.Senior
import android.content.Intent
import com.seniorjuniorconnect.app.ui.mentorship.SendRequestActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: SeniorAdapter
    private var allSeniors = listOf<Senior>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        // Setup RecyclerView
        adapter = SeniorAdapter(emptyList()) { senior ->
            val intent = Intent(requireContext(), SendRequestActivity::class.java)
            intent.putExtra("seniorUid", senior.uid)
            intent.putExtra("seniorName", senior.name)
            startActivity(intent)
        }
        binding.rvSeniors.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSeniors.adapter = adapter

        // Load seniors from Firestore
        loadSeniors()

        // Search functionality
        binding.etSearch.addTextChangedListener { text ->
            filterSeniors(text.toString())
        }
    }

    private fun loadSeniors() {
        db.collection("users")
            .whereEqualTo("role", "senior")
            .get()
            .addOnSuccessListener { documents ->
                allSeniors = documents.map { doc ->
                    Senior(
                        uid = doc.id,
                        name = doc.getString("name") ?: "",
                        email = doc.getString("email") ?: "",
                        college = doc.getString("college") ?: "",
                        branch = doc.getString("branch") ?: "",
                        year = doc.getString("year") ?: "",
                        bio = doc.getString("bio") ?: "",
                        linkedIn = doc.getString("linkedIn") ?: "",
                        domains = (doc.get("domains") as? List<String>) ?: emptyList(),
                        subjects = doc.getString("subjects") ?: "",
                        placedAt = doc.getString("placedAt") ?: "",
                        finalRound = doc.getString("finalRound") ?: "",
                        interviewRound = doc.getString("interviewRound") ?: ""
                    )
                }
                adapter.updateList(allSeniors)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(),
                    "Failed to load seniors", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterSeniors(query: String) {
        if (query.isEmpty()) {
            adapter.updateList(allSeniors)
            return
        }
        val filtered = allSeniors.filter { senior ->
            senior.name.contains(query, ignoreCase = true) ||
                    senior.domains.any { it.contains(query, ignoreCase = true) } ||
                    senior.placedAt.contains(query, ignoreCase = true) ||
                    senior.finalRound.contains(query, ignoreCase = true) ||
                    senior.branch.contains(query, ignoreCase = true)
        }
        adapter.updateList(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}