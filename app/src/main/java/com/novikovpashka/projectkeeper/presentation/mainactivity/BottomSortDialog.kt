package com.novikovpashka.projectkeeper.presentation.mainactivity

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.novikovpashka.projectkeeper.MainApp
import com.novikovpashka.projectkeeper.databinding.BottomSortMainBinding
import javax.inject.Inject

class BottomSortDialog : BottomSheetDialogFragment() {
    private lateinit var bottomSortMainBinding: BottomSortMainBinding
    private lateinit var sharedViewModel: SharedViewModel


    @Inject
    lateinit var factory: SharedViewModel.Factory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        (requireActivity().application as MainApp).appComponent.inject(this)
        sharedViewModel = ViewModelProvider(requireActivity(), factory)[SharedViewModel::class.java]

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
        }

        bottomSortMainBinding = BottomSortMainBinding.inflate(layoutInflater)

        bottomSortMainBinding.apply {
            viewModel = sharedViewModel
        }

        bottomSortMainBinding.applySort.setOnClickListener {
            sharedViewModel.saveSortAndOrderParamsToStorage()
            dismiss()
        }

        return bottomSortMainBinding.root
    }

}