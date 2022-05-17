package com.novikovpashka.projectkeeper.presentation.addprojectactivity

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.novikovpashka.projectkeeper.databinding.IncomingItemBinding
import com.novikovpashka.projectkeeper.databinding.IncomingItemLastBinding

class IncomingListAdapter(private val listener: OnItemClickListener) : ListAdapter<AddProjectViewModel.ItemIncoming, RecyclerView.ViewHolder>(IncomingDiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return if (position == super.getItemCount()) {
            1
        } else 0
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + 1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (position != super.getItemCount()) {
            val incoming = getItem(position)
            (holder as IncomingViewHolder).bind(incoming)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            IncomingViewHolder(
                IncomingItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        } else
            ButtonViewHolder(
                IncomingItemLastBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
    }

    inner class IncomingViewHolder(
        val binding: IncomingItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AddProjectViewModel.ItemIncoming) {
            binding.apply {
                incoming = item
                executePendingBindings()
            }
        }
        init {
            binding.incomingDesc.addTextChangedListener(object: CustomTextWatcher() {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    listener.onEditDescription(s.toString(), absoluteAdapterPosition)
                }
            })

            binding.incomingValue.addTextChangedListener(object: CustomTextWatcher() {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    listener.onEditValue(s.toString(), absoluteAdapterPosition)
                }
            })

            binding.incomingDate.setOnClickListener {
                listener.onEditDate(currentList[bindingAdapterPosition].incomingDate, bindingAdapterPosition)
            }

            binding.deleteItem.setOnClickListener {
                listener.deleteIncoming(absoluteAdapterPosition)
            }
        }
    }

    inner class ButtonViewHolder(
        binding: IncomingItemLastBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        val addButton: MaterialButton = binding.add
        init {
            addButton.setOnClickListener {
                listener.addIncoming()
            }
        }
    }

    private class IncomingDiffCallback : DiffUtil.ItemCallback<AddProjectViewModel.ItemIncoming>() {
        override fun areItemsTheSame(oldItem: AddProjectViewModel.ItemIncoming, newItem: AddProjectViewModel.ItemIncoming): Boolean {
            return oldItem.dateStamp == newItem.dateStamp
        }
        override fun areContentsTheSame(oldItem: AddProjectViewModel.ItemIncoming, newItem: AddProjectViewModel.ItemIncoming): Boolean {
            return oldItem == newItem
        }
    }

    interface OnItemClickListener {
        fun addIncoming()
        fun deleteIncoming(index: Int)
        fun onEditValue(value: String, index: Int)
        fun onEditDescription(description: String, index: Int)
        fun onEditDate(date: Long, index: Int)
    }

    open class CustomTextWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
        }

    }

}