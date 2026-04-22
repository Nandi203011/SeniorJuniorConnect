package com.seniorjuniorconnect.app.ui.senior

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.seniorjuniorconnect.app.databinding.FragmentProfileBinding
import com.seniorjuniorconnect.app.ui.auth.LoginActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        loadProfile()

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun loadProfile() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                binding.tvName.text = doc.getString("name") ?: "Name"
                binding.tvEmail.text = doc.getString("email") ?: "email"
                val college = doc.getString("college") ?: ""
                val branch = doc.getString("branch") ?: ""
                val year = doc.getString("year") ?: ""
                binding.tvCollegeBranch.text = "$college • $branch • $year"
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}