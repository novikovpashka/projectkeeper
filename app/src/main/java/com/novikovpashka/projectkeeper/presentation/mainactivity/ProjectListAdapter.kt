package com.novikovpashka.projectkeeper.presentation.mainactivity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.novikovpashka.projectkeeper.CurrencyList
import com.novikovpashka.projectkeeper.Helpers
import com.novikovpashka.projectkeeper.data.model.Project
import com.novikovpashka.projectkeeper.databinding.ItemViewBinding

class ProjectListAdapter(private val listener: OnItemClickListener) :
    ListAdapter<Project, RecyclerView.ViewHolder>(ProjectDiffCallback()) {

    var selectMode: Boolean = false
    var selectedProject: List<Project> = mutableListOf()
    var selectedId: List<Int> = mutableListOf()

    var currency = CurrencyList.RUB
    var usdRate = 0.0
    var eurRate = 0.0

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val project = getItem(position)
        (holder as ProjectViewHolder).bind(project)

        with(holder.binding) {

            card.isChecked = selectedProject.contains(project)

            itemTotalprice.text =
                Helpers.convertPrice(project.price, currency, usdRate, eurRate)

            itemIncomings.text =
                Helpers.convertPrice(project.incomingsSum, currency, usdRate, eurRate)

            itemLeft.text =
                Helpers.convertPrice(project.price - project.incomingsSum, currency, usdRate, eurRate)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ProjectViewHolder(
            ItemViewBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    inner class ProjectViewHolder(
        val binding: ItemViewBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Project) {
            binding.apply {
                project = item
                executePendingBindings()

                binding.root.setOnLongClickListener {
                    if (selectedProject.contains(item)) {
                        listener.removeProjectToDelete(item, bindingAdapterPosition)
                        binding.card.isChecked = false
                    } else {
                        listener.addProjectToDelete(item, bindingAdapterPosition)
                        binding.card.isChecked = true
                    }
                    return@setOnLongClickListener true
                }

                binding.root.setOnClickListener {
                    if (!selectMode) {
                        listener.onProjectClick(item)
                    } else {
                        if (selectedProject.contains(item)) {
                            listener.removeProjectToDelete(item, bindingAdapterPosition)
                            binding.card.isChecked = false
                        } else {
                            listener.addProjectToDelete(item, bindingAdapterPosition)
                            binding.card.isChecked = true
                        }
                    }
                }
            }
        }
    }

    private class ProjectDiffCallback : DiffUtil.ItemCallback<Project>() {
        override fun areItemsTheSame(oldItem: Project, newItem: Project): Boolean {
            return oldItem.dateStamp == newItem.dateStamp
        }

        override fun areContentsTheSame(oldItem: Project, newItem: Project): Boolean {
            return oldItem == newItem
        }
    }

    interface OnItemClickListener {
        fun onProjectClick(project: Project)
        fun addProjectToDelete(project: Project, position: Int)
        fun removeProjectToDelete(project: Project, position: Int)
    }

}