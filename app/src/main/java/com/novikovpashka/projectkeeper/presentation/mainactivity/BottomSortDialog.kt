package com.novikovpashka.projectkeeper.presentation.mainactivity

import android.content.Context
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.widget.RadioButton
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import com.novikovpashka.projectkeeper.databinding.BottomSortMainBinding

class BottomSortDialog : BottomSheetDialogFragment() {
    private var radioListener: RadioListener? = null
    private lateinit var buttonDate: RadioButton
    private lateinit var buttonName: RadioButton
    private lateinit var buttonAscending: RadioButton
    private lateinit var buttonDescending: RadioButton
    private lateinit var bottomSortMainBinding: BottomSortMainBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bottomSortMainBinding = BottomSortMainBinding.inflate(layoutInflater)

        buttonDate = bottomSortMainBinding.byDate
        buttonName = bottomSortMainBinding.byName
        buttonAscending = bottomSortMainBinding.ascending
        buttonDescending = bottomSortMainBinding.descending

        val bundle = arguments
        val currentSortParam =
            bundle!!.getSerializable("currentSortParam") as SortParam?
        if (currentSortParam == SortParam.BY_NAME) buttonName.isChecked = true
        else buttonDate.isChecked = true

        val currentOrderParam =
            bundle.getSerializable("currentOrderParam") as OrderParam?
        if (currentOrderParam == OrderParam.ASCENDING)
            buttonAscending.isChecked = true
        else buttonDescending.isChecked = true

        bottomSortMainBinding.applySort.setOnClickListener {
            val sortParam = if (buttonDate.isChecked) SortParam.BY_DATE_ADDED
            else SortParam.BY_NAME
            val orderParam = if (buttonAscending.isChecked) OrderParam.ASCENDING
            else OrderParam.DESCENDING
            radioListener!!.applySortClicked(sortParam, orderParam)
            dismiss()
        }

        return bottomSortMainBinding.root
    }

    interface RadioListener {
        fun applySortClicked(sortParam: SortParam, orderParam: OrderParam)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        radioListener = context as RadioListener
    }
}