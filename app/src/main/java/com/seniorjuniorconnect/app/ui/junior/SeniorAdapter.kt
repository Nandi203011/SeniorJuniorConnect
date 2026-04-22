package com.seniorjuniorconnect.app.ui.junior

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.seniorjuniorconnect.app.databinding.ItemSeniorCardBinding
import com.seniorjuniorconnect.app.model.Senior

class SeniorAdapter(
    private var seniors: List<Senior>,
    private val onConnectClick: (Senior) -> Unit
) : RecyclerView.Adapter<SeniorAdapter.SeniorViewHolder>() {

    inner class SeniorViewHolder(val binding: ItemSeniorCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeniorViewHolder {
        val binding = ItemSeniorCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SeniorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SeniorViewHolder, position: Int) {
        val senior = seniors[position]
        with(holder.binding) {

            tvSeniorName.text = senior.name
            tvYear.text = senior.year
            tvCollegeBranch.text = "${senior.college} • ${senior.branch}"
            tvBio.text = senior.bio.ifEmpty { "No bio added yet" }

            // Placement status
            when {
                senior.placedAt.isNotEmpty() ->
                    tvPlacement.text = "🟢 Placed at ${senior.placedAt}"
                senior.finalRound.isNotEmpty() ->
                    tvPlacement.text = "🔵 Final Round at ${senior.finalRound}"
                senior.interviewRound.isNotEmpty() ->
                    tvPlacement.text = "🟡 Interview Round at ${senior.interviewRound}"
                else -> tvPlacement.text = ""
            }

            // Domain chips
            chipGroupDomains.removeAllViews()
            senior.domains.forEach { domain ->
                val chip = Chip(holder.itemView.context)
                chip.text = domain
                chip.setTextColor(android.graphics.Color.WHITE)
                chip.chipBackgroundColor =
                    android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#2D2D44")
                    )
                chip.isClickable = false
                chipGroupDomains.addView(chip)
            }

            btnConnect.setOnClickListener {
                onConnectClick(senior)
            }
        }
    }

    override fun getItemCount() = seniors.size

    fun updateList(newList: List<Senior>) {
        seniors = newList
        notifyDataSetChanged()
    }
}