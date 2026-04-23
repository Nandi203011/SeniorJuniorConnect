package com.seniorjuniorconnect.app.ui.mentorship

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.seniorjuniorconnect.app.databinding.ItemRequestCardBinding
import com.seniorjuniorconnect.app.model.MentorshipRequest
import com.seniorjuniorconnect.app.ui.mentorship.ChatActivity
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
            tvName.text = if (currentUserRole == "senior")
                "From: ${request.juniorName}"
            else
                "To: ${request.seniorName}"

            tvMessage.text = request.message

            when (request.status) {
                "accepted" -> {
                    tvStatus.text = "✅ Accepted"
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                    btnAccept.visibility = View.GONE
                    btnReject.visibility = View.GONE

                    // Show chat button when accepted
                    btnChat.visibility = View.VISIBLE
                    btnChat.setOnClickListener {
                        val context = holder.itemView.context
                        val intent = Intent(context, ChatActivity::class.java).apply {
                            putExtra("juniorUid", request.juniorUid)
                            putExtra("juniorName", request.juniorName)
                            putExtra("seniorUid", request.seniorUid)
                            putExtra("seniorName", request.seniorName)
                        }
                        context.startActivity(intent)
                    }
                }

                "rejected" -> {
                    tvStatus.text = "❌ Rejected"
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#FF4444"))
                    btnAccept.visibility = View.GONE
                    btnReject.visibility = View.GONE
                    btnChat.visibility = View.GONE
                }

                else -> {
                    tvStatus.text = "⏳ Pending"
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#FFA500"))
                    btnChat.visibility = View.GONE

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