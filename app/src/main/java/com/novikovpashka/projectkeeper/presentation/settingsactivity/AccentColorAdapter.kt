package com.novikovpashka.projectkeeper.presentation.settingsactivity

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.novikovpashka.projectkeeper.AccentColors
import com.novikovpashka.projectkeeper.R
import com.novikovpashka.projectkeeper.databinding.ColorItemBinding
import com.novikovpashka.projectkeeper.databinding.ItemViewBinding

class AccentColorAdapter (private val colorList: MutableList<Int>, onColorListener: OnColorListener) : RecyclerView.Adapter<AccentColorAdapter.ColorHolder>() {

    var mOnColorListener: OnColorListener
    var currentAccentColor: Int = 999

    init{
        mOnColorListener = onColorListener
    }

    inner class ColorHolder(binding: ColorItemBinding, onColorListener: OnColorListener) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        val colorCircle: ImageView = binding.colorItem
        val colorCircleSelect: ImageView = binding.colorItemSelect

        var mOnColorListener: OnColorListener

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
                LayoutInflater.from(parent.context), parent, false), mOnColorListener)
    }

    override fun onBindViewHolder(holder: ColorHolder, position: Int) {
        holder.colorCircle.setColorFilter(colorList[position])
        if (colorList[position] == currentAccentColor) {
            holder.colorCircleSelect.visibility = View.VISIBLE
        }
        else holder.colorCircleSelect.visibility = View.GONE

    }

    override fun getItemCount(): Int {
        return colorList.size
    }

    interface OnColorListener {
        fun onColorPick(color: Int)
    }

}