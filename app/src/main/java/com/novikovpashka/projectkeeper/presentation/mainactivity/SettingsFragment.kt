package com.novikovpashka.projectkeeper.presentation.mainactivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.novikovpashka.projectkeeper.MainApp
import com.novikovpashka.projectkeeper.databinding.FragmentSettingsBinding
import javax.inject.Inject

class SettingsFragment : Fragment(), AccentColorAdapter.OnColorListener {

    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var accentColorAdapter: AccentColorAdapter

    @Inject
    lateinit var factory: SharedViewModel.Factory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity().application as MainApp).appComponent.inject(this)
        sharedViewModel = ViewModelProvider(requireActivity(), factory)[SharedViewModel::class.java]

        binding.apply {
            viewmodel = sharedViewModel
        }

        recyclerView = binding.recycler

        accentColorAdapter = AccentColorAdapter(
            sharedViewModel.getAccentColorsList(),
            sharedViewModel.loadAccentColorFromStorage(),
            this
        )

        recyclerView.adapter = accentColorAdapter
        recyclerView.setHasFixedSize(false)

        binding.settingsToolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    override fun onResume() {
        binding.currencyGroup.jumpDrawablesToCurrentState()
        binding.nightModeGroup.jumpDrawablesToCurrentState()
        super.onResume()
    }

    override fun onColorPick(color: Int) {
        if (color != sharedViewModel.loadAccentColorFromStorage()) {
            sharedViewModel.setAccentColor(color)
        }
        requireActivity().recreate()
    }

}