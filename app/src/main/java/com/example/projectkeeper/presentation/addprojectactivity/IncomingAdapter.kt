package com.example.projectkeeper.presentation.addprojectactivity

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import com.google.android.material.textfield.TextInputLayout
import android.view.View.OnFocusChangeListener
import android.text.TextWatcher
import android.text.Editable
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.projectkeeper.databinding.IncomingItemBinding
import com.example.projectkeeper.databinding.IncomingItemLastBinding
import java.lang.Exception

class IncomingAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var incomingInputsValues: MutableList<String?> = mutableListOf()
    var inputEditText = mutableListOf<String?>()

    override fun getItemViewType(position: Int): Int {
        return if (position == inputEditText.size - 1) 0 else 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val binding = IncomingItemLastBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            IncomingLastItemViewHolder(binding)
        } else {
            val binding = IncomingItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            val viewHolderDefault = IncomingViewHolderDefault(binding)
            viewHolderDefault.incoming.editText!!.setText("")
            IncomingViewHolderDefault(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == 1) {
            val viewHolderDefault = holder as IncomingViewHolderDefault
            try {
                if (incomingInputsValues[position] == null) {
                    viewHolderDefault.incoming.editText!!.setText("")
                } else viewHolderDefault.incoming.editText!!.setText(incomingInputsValues[position])
            } catch (ignored: Exception) {
            }
        }
    }

    override fun getItemCount(): Int {
        return if (inputEditText.size == 0) 0 else inputEditText.size
    }

    internal inner class IncomingViewHolderDefault(binding: IncomingItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var incoming: TextInputLayout = binding.incomingText
        var deleteIncoming: ImageView = binding.deleteItem

        init {

            deleteIncoming.setOnClickListener { v: View? ->
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    incomingInputsValues.removeAt(position)
                    inputEditText.removeAt(position)
                    notifyItemRemoved(position)
                }
            }
            incoming.editText!!.onFocusChangeListener =
                OnFocusChangeListener { view: View?, b: Boolean ->
                    if (inputEditText.size > 2 && b) {
                        deleteIncoming.rotation = -45f
                        deleteIncoming.isClickable = true
                        deleteIncoming.animate().alpha(1f).rotation(0f).duration = 100
                    }
                    if (!b) {
                        incoming.clearFocus()
                        deleteIncoming.isClickable = false
                        deleteIncoming.animate().alpha(0f).duration = 100
                    }
                }
            incoming.editText!!.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    charSequence: CharSequence,
                    i: Int,
                    i1: Int,
                    i2: Int
                ) {
                }

                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                override fun afterTextChanged(editable: Editable) {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        incomingInputsValues[position] = editable.toString()
                    }
                }
            })
        }
    }

    internal inner class IncomingLastItemViewHolder(binding: IncomingItemLastBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var constraintLayout: ConstraintLayout

        init {
            var binding = binding
            binding = IncomingItemLastBinding.bind(itemView)
            constraintLayout = binding.addIncoming
            constraintLayout.setOnClickListener { v: View? ->
                inputEditText!!.add(bindingAdapterPosition - 1, null)
                incomingInputsValues.add(bindingAdapterPosition, null)
                notifyItemInserted(bindingAdapterPosition)
            }
        }
    }
}