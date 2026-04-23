package com.seniorjuniorconnect.app.ui.questions

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.seniorjuniorconnect.app.databinding.ItemQuestionCardBinding
import com.seniorjuniorconnect.app.model.Question

class QuestionsAdapter(
    private var questions: List<Question>,
    private val currentUserRole: String,
    private val onAnswerClick: (Question) -> Unit
) : RecyclerView.Adapter<QuestionsAdapter.QuestionViewHolder>() {

    inner class QuestionViewHolder(val binding: ItemQuestionCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val binding = ItemQuestionCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return QuestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        val question = questions[position]
        with(holder.binding) {

            tvTopic.text = question.topic
            tvQuestion.text = question.question
            tvAskedBy.text = "Asked by: ${question.askedBy}"

            if (question.isAnswered) {
                tvAnsweredBadge.visibility = View.VISIBLE
                divider.visibility = View.VISIBLE
                tvAnswer.visibility = View.VISIBLE
                tvAnswer.text = "💬 ${question.answeredBy}: ${question.answer}"
                btnAnswer.visibility = View.GONE

                // Show attachment if exists
                if (question.attachmentUrl.isNotEmpty()) {
                    val label = when (question.attachmentType) {
                        "image" -> "🖼️ View Image"
                        "pdf" -> "📄 View PDF"
                        "link" -> "🔗 Open Link"
                        else -> "📎 View Attachment"
                    }
                    tvAttachmentLink.text = label
                    tvAttachmentLink.visibility = View.VISIBLE
                    tvAttachmentLink.setOnClickListener {
                        val intent = Intent(Intent.ACTION_VIEW,
                            Uri.parse(question.attachmentUrl))
                        holder.itemView.context.startActivity(intent)
                    }
                } else {
                    tvAttachmentLink.visibility = View.GONE
                }

            } else {
                tvAnsweredBadge.visibility = View.GONE
                divider.visibility = View.GONE
                tvAnswer.visibility = View.GONE
                tvAttachmentLink.visibility = View.GONE

                if (currentUserRole == "senior") {
                    btnAnswer.visibility = View.VISIBLE
                    btnAnswer.setOnClickListener { onAnswerClick(question) }
                } else {
                    btnAnswer.visibility = View.GONE
                }
            }
        }
    }

    override fun getItemCount() = questions.size

    fun updateList(newList: List<Question>) {
        questions = newList
        notifyDataSetChanged()
    }
}