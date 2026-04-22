package com.seniorjuniorconnect.app.ui.mentorship

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.seniorjuniorconnect.app.databinding.ItemRequestCardBinding
import com.seniorjuniorconnect.app.model.MentorshipRequest

class MentorshipAdapter(
    private var requests: List<MentorshipRequest>,
    private val currentUserRole: String,
    private val onAccept: (MentorshipRequest) -> Unit,
    private val onReject: (MentorshipRequest) -> Unit
) : RecyclerView.Adapter<MentorshipAdapter.RequestViewHolder>() {

    inner class RequestViewHolder(val binding: ItemRequestCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val binding = ItemRequestCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requests[position]
        with(holder.binding) {

            // Show junior name if senior is viewing, else show senior name
            tvName.text = if (currentUserRole == "senior")
                "From: ${request.juniorName}"
            else
                "To: ${request.seniorName}"

            tvMessage.text = request.message

            // Status color
            when (request.status) {
                "accepted" -> {
                    tvStatus.text = "✅ Accepted"
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                    btnAccept.visibility = View.GONE
                    btnReject.visibility = View.GONE
                }
                "rejected" -> {
                    tvStatus.text = "❌ Rejected"
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#FF4444"))
                    btnAccept.visibility = View.GONE
                    btnReject.visibility = View.GONE
                }
                else -> {
                    tvStatus.text = "⏳ Pending"
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#FFA500"))

                    // Only seniors see accept/reject buttons
                    if (currentUserRole == "senior") {
                        btnAccept.visibility = View.VISIBLE
                        btnReject.visibility = View.VISIBLE
                        btnAccept.setOnClickListener { onAccept(request) }
                        btnReject.setOnClickListener { onReject(request) }
                    }
                }
            }
        }
    }

    override fun getItemCount() = requests.size

    fun updateList(newList: List<MentorshipRequest>) {
        requests = newList
        notifyDataSetChanged()
    }
}