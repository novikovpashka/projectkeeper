package com.novikovpashka.projectkeeper.presentation.mainactivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.novikovpashka.projectkeeper.MainApp
import com.novikovpashka.projectkeeper.databinding.FragmentSettingsBinding
import javax.inject.Inject

class SettingsFragment : Fragment(), AccentColorAdapter.OnColorListener {

    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var binding: FragmentSettingsBinding

    @Inject
    lateinit var factory: SharedViewModel.Factory


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        (requireActivity().application as MainApp).appComponent.inject(this)
        sharedViewModel = ViewModelProvider(requireActivity(), factory)[SharedViewModel::class.java]

        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        val accentColorAdapter = AccentColorAdapter(
            sharedViewModel.getAccentColorsList(),
            sharedViewModel.loadAccentColorFromStorage(),
            this
        )

        with(binding) {
            apply {
                viewmodel = sharedViewModel
            }

            recycler.adapter = accentColorAdapter
            recycler.setHasFixedSize(false)

            settingsToolbar.setNavigationOnClickListener {
                requireActivity().onBackPressed()
            }

            return root
        }
    }

    override fun onStart() {
        binding.currencyGroup.jumpDrawablesToCurrentState()
        binding.nightModeGroup.jumpDrawablesToCurrentState()
        super.onStart()
    }

    override fun onColorPick(color: Int) {
        if (color != sharedViewModel.loadAccentColorFromStorage()) {
            sharedViewModel.setAccentColor(color)
        }
        requireActivity().recreate()
    }

}