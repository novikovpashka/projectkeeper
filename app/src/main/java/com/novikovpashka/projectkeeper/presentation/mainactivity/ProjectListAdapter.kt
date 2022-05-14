package com.novikovpashka.projectkeeper.presentation.mainactivity

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.novikovpashka.projectkeeper.CurrencyList
import com.novikovpashka.projectkeeper.Helpers
import com.novikovpashka.projectkeeper.data.datafirestore.Project
import com.novikovpashka.projectkeeper.databinding.ItemViewBinding

class ProjectListAdapter(private val listener: OnItemClickListener) : ListAdapter<Project, RecyclerView.ViewHolder>(ProjectDiffCallback()) {

    var selectMode: Boolean = false
    var selectedProject: MutableList<Project> = mutableListOf()
    var selectedId: MutableList<Int> = mutableListOf()
    var currency = CurrencyList.RUB
    var usdRate = 0.0
    var eurRate = 0.0

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val project = getItem(position)
        (holder as ProjectViewHolder).bind(project)
        holder.binding.card.isChecked = selectedProject.contains(project)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ProjectViewHolder(
            ItemViewBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
    }

    inner class ProjectViewHolder(
        val binding: ItemViewBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Project) {
            binding.apply {
                project = item
                executePendingBindings()
                binding.itemTotalprice.text = Helpers.convert(item.price, currency, usdRate, eurRate)
//                binding.itemIncomings.text = Helpers.convert(item.incomings.sum(), currency, usdRate, eurRate)
//                binding.itemLeft.text = Helpers.convert(item.price - item.incomings.sum(), currency, usdRate, eurRate)

                binding.root.setOnLongClickListener {
                    if (!selectMode && selectedProject.isEmpty()) {
                        listener.addProjectToDelete(item, bindingAdapterPosition)
                        listener.showActionMenu()
                        binding.card.isChecked = true
                    }
                    else if (selectMode && selectedProject.contains(project)){
                        listener.removeProjectToDelete(item, bindingAdapterPosition)
                        binding.card.isChecked = false
                    }
                    else if (selectMode && !selectedProject.contains(project)) {
                        listener.addProjectToDelete(item, bindingAdapterPosition)
                        binding.card.isChecked = true
                    }
                    if (selectedProject.isEmpty()) {
                        listener.closeActionMenu()
                    }
                    return@setOnLongClickListener true
                }

                binding.root.setOnClickListener {
                    if (!selectMode) {
                        listener.onItemClick(item)
                    }
                    else {
                        if (selectedProject.contains(item)) {
                            listener.removeProjectToDelete(item, bindingAdapterPosition)
                            binding.card.isChecked = false
                        }
                        else {
                            listener.addProjectToDelete(item, bindingAdapterPosition)
                            binding.card.isChecked = true
                        }
                    }

                    if (selectedProject.isEmpty()) {
                        listener.closeActionMenu()
                    }
                }
            }
        }
    }

    private class ProjectDiffCallback : DiffUtil.ItemCallback<Project>() {
        override fun areItemsTheSame(oldItem: Project, newItem: Project): Boolean {
            return oldItem.dateAdded == newItem.dateAdded
        }
        override fun areContentsTheSame(oldItem: Project, newItem: Project): Boolean {
            return oldItem == newItem
        }
    }

    interface OnItemClickListener {
        fun onItemClick(project: Project)
        fun showActionMenu()
        fun closeActionMenu()
        fun addProjectToDelete(project: Project, position: Int)
        fun removeProjectToDelete(project: Project, position: Int)
    }

}