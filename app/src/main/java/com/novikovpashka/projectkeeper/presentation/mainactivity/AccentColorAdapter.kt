package com.novikovpashka.projectkeeper.presentation.mainactivity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.novikovpashka.projectkeeper.databinding.ColorItemBinding

class AccentColorAdapter(
    private val colorList: MutableList<Int>,
    private val currentAccentColor: Int,
    onColorListener: OnColorListener
) : RecyclerView.Adapter<AccentColorAdapter.ColorHolder>() {

    private var mOnColorListener: OnColorListener

    init {
        mOnColorListener = onColorListener
    }

    inner class ColorHolder(binding: ColorItemBinding, onColorListener: OnColorListener) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        val colorCircle: ImageView = binding.colorItem
        val colorCircleSelect: ImageView = binding.colorItemSelect

        private var mOnColorListener: OnColorListener

        init {
            colorCircle.setOnClickListener(this)
            mOnColorListener = onColorListener
        }

        override fun onClick(v: View?) {
            mOnColorListener.onColorPick(colorList[bindingAdapterPosition])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorHolder {
        return ColorHolder(
            ColorItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), mOnColorListener
        )
    }

    override fun onBindViewHolder(holder: ColorHolder, position: Int) {
        holder.colorCircle.setColorFilter(
            ContextCompat.getColor(
                holder.colorCircle.context,
                colorList[position]
            )
        )
        holder.colorCircleSelect.visibility =
            if (colorList[position] == currentAccentColor) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int {
        return colorList.size
    }

    interface OnColorListener {
        fun onColorPick(color: Int)
    }
}