package com.seniorjuniorconnect.app.ui.mentorship

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.seniorjuniorconnect.app.databinding.ItemMessageReceivedBinding
import com.seniorjuniorconnect.app.databinding.ItemMessageSentBinding
import com.seniorjuniorconnect.app.model.ChatMessage
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private var messages: List<ChatMessage>,
    private val currentUid: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_SENT = 1
        private const val VIEW_RECEIVED = 2
    }

    inner class SentViewHolder(val binding: ItemMessageSentBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class ReceivedViewHolder(val binding: ItemMessageReceivedBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderUid == currentUid) VIEW_SENT else VIEW_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_SENT) {
            SentViewHolder(
                ItemMessageSentBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        } else {
            ReceivedViewHolder(
                ItemMessageReceivedBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault())
            .format(Date(message.timestamp))

        when (holder) {
            is SentViewHolder -> {
                with(holder.binding) {
                    tvMessage.text = message.text
                    tvTime.text = timeStr
                    tvSeen.visibility = if (message.seen) View.VISIBLE else View.GONE
                    bindAttachment(tvAttachment, message)
                }
            }
            is ReceivedViewHolder -> {
                with(holder.binding) {
                    tvMessage.text = message.text
                    tvTime.text = timeStr
                    bindAttachment(tvAttachment, message)
                }
            }
        }
    }

    private fun bindAttachment(tvAttachment: android.widget.TextView, message: ChatMessage) {
        when (message.attachmentType) {
            "pdf" -> {
                tvAttachment.visibility = View.VISIBLE
                tvAttachment.text = "📄 View PDF"
                tvAttachment.setOnClickListener {
                    // Google Docs viewer can open any public PDF URL
                    val googleDocsUrl = "https://docs.google.com/viewer?url=${message.attachmentUrl}"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(googleDocsUrl))
                    it.context.startActivity(intent)
                }
            }
            "link" -> {
                tvAttachment.visibility = View.VISIBLE
                tvAttachment.text = "🔗 ${message.attachmentUrl}"
                tvAttachment.setOnClickListener {
                    val url = if (message.attachmentUrl.startsWith("http"))
                        message.attachmentUrl else "https://${message.attachmentUrl}"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    it.context.startActivity(intent)
                }
            }
            else -> tvAttachment.visibility = View.GONE
        }
    }
    override fun getItemCount() = messages.size

    fun updateList(newMessages: List<ChatMessage>) {
        messages = newMessages
        notifyDataSetChanged()
    }
}