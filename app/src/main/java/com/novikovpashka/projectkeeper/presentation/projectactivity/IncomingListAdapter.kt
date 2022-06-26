package com.novikovpashka.projectkeeper.presentation.projectactivity

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.novikovpashka.projectkeeper.Helpers
import com.novikovpashka.projectkeeper.data.datafirestore.Incoming
import com.novikovpashka.projectkeeper.databinding.IncomingItemBinding

class IncomingListAdapter() : ListAdapter<Incoming, RecyclerView.ViewHolder>(IncomingDiffCallback()) {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val incoming = getItem(position)
        (holder as IncomingViewHolder).bind(incoming)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return IncomingViewHolder(IncomingItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false))
    }

    inner class IncomingViewHolder(
        val binding: IncomingItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Incoming) {
            binding.incomingDate.text = Helpers.convertDate(item.incomingDate)
            binding.incomingValue.text = Helpers.convertPriceProject(item.incomingValue)
            binding.incomingDesc.text = item.incomingDescription
        }
    }

    private class IncomingDiffCallback : DiffUtil.ItemCallback<Incoming>() {
        override fun areItemsTheSame(
            oldItem: Incoming,
            newItem: Incoming
        ): Boolean {
            return oldItem.dateStamp == newItem.dateStamp
        }
        override fun areContentsTheSame(oldItem: Incoming, newItem: Incoming): Boolean {
            return oldItem == newItem
        }
    }

}