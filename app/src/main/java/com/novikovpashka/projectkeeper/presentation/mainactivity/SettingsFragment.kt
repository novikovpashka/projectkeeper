package com.novikovpashka.projectkeeper.presentation.mainactivity

import android.content.Context
import android.graphics.Insets
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.novikovpashka.projectkeeper.CurrencyList
import com.novikovpashka.projectkeeper.R
import com.novikovpashka.projectkeeper.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment(), AccentColorAdapter.OnColorListener {

    private lateinit var viewModel: SharedViewModel
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var settingsListener: SettingsListener
    private lateinit var recyclerView: RecyclerView
    private lateinit var accentColorAdapter: AccentColorAdapter
    private var accentColor: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            binding.appbarlayout.setOnApplyWindowInsetsListener { v, insets ->
                val mInsets: Insets = insets.getInsets(WindowInsets.Type.systemBars())
                v.setPadding(0, mInsets.top, 0, 0)
                insets
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        accentColor = viewModel.accentColor.value!!
        recyclerView = binding.recycler
        accentColorAdapter = AccentColorAdapter(viewModel.getAccentColors(), this)
        recyclerView.adapter = accentColorAdapter
        recyclerView.setHasFixedSize(false)

        //set default values without animation
        when (viewModel.currentCurrency.value) {
            CurrencyList.RUB -> {
                binding.rub.isChecked = true
                binding.currencyGroup.jumpDrawablesToCurrentState()
            }
            CurrencyList.USD -> {
                binding.usd.isChecked = true
                binding.currencyGroup.jumpDrawablesToCurrentState()
            }
            CurrencyList.EUR -> {
                binding.eur.isChecked = true
                binding.currencyGroup.jumpDrawablesToCurrentState()
            }
        }

        when (viewModel.currentTheme.value) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> {
                binding.useSystem.isChecked = true
                binding.themeGroup.jumpDrawablesToCurrentState()
            }
            AppCompatDelegate.MODE_NIGHT_YES -> {
                binding.dark.isChecked = true
                binding.themeGroup.jumpDrawablesToCurrentState()
            }
            AppCompatDelegate.MODE_NIGHT_NO -> {
                binding.light.isChecked = true
                binding.themeGroup.jumpDrawablesToCurrentState()
            }
        }

        viewModel.currentTheme.observe(viewLifecycleOwner) {
            when (it) {
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> binding.useSystem.isChecked = true
                AppCompatDelegate.MODE_NIGHT_YES -> binding.dark.isChecked = true
                AppCompatDelegate.MODE_NIGHT_NO -> binding.light.isChecked = true
            }
        }

        viewModel.currentCurrency.observe(viewLifecycleOwner) {
            when (it) {
                CurrencyList.RUB -> binding.rub.isChecked = true
                CurrencyList.USD -> binding.usd.isChecked = true
                CurrencyList.EUR -> binding.eur.isChecked = true
            }
        }

        viewModel.accentColor.observe(viewLifecycleOwner) {
            accentColorAdapter.currentAccentColor = it
            accentColorAdapter.notifyDataSetChanged()
            setAccentColor(it)
        }

        binding.currencyGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                binding.rub.id -> {
                    viewModel.saveAndApplyCurrency(CurrencyList.RUB)
                    settingsListener.currencyChanged(CurrencyList.RUB)
                }
                binding.usd.id -> {
                    viewModel.saveAndApplyCurrency(CurrencyList.USD)
                    settingsListener.currencyChanged(CurrencyList.USD)
                }
                binding.eur.id -> {
                    viewModel.saveAndApplyCurrency(CurrencyList.EUR)
                    settingsListener.currencyChanged(CurrencyList.EUR)
                }
            }
        }

        binding.themeGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                binding.useSystem.id -> AppCompatDelegate
                    .setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                binding.light.id -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                binding.dark.id -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            viewModel.saveCurrentTheme()
        }

        binding.settingsToolbar.setNavigationOnClickListener {
            settingsListener.activateDrawer()
//            requireActivity().onBackPressed()
            requireActivity().supportFragmentManager.popBackStack()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        settingsListener = context as SettingsListener
    }

    interface SettingsListener {
        fun activateDrawer()
        fun recreateActivity()
        fun currencyChanged(currency: CurrencyList)
    }

    override fun onColorPick(color: Int) {
        viewModel.saveAndApplyAccentColor(color)
    }

    fun setAccentColor(color: Int) {
        when (color) {
            ContextCompat.getColor(activity!!.applicationContext, R.color.myOrange) -> {
                context!!.theme.applyStyle(R.style.Theme_Default, true)
            }

            ContextCompat.getColor(activity!!.applicationContext, R.color.myRed) -> {
                context!!.theme.applyStyle(R.style.Theme_Default_Red, true)
            }

            ContextCompat.getColor(activity!!.applicationContext, R.color.myGreen) -> {
                context!!.theme.applyStyle(R.style.Theme_Default_Green, true)
            }

            ContextCompat.getColor(activity!!.applicationContext, R.color.myPurple) -> {
                context!!.theme.applyStyle(R.style.Theme_Default_Purple, true)
            }

            ContextCompat.getColor(activity!!.applicationContext, R.color.myBlue) -> {
                context!!.theme.applyStyle(R.style.Theme_Default_Blue, true)
            }
        }
        if (color != accentColor) {
            activity!!.recreate()
            Log.v("mytag", "color applied")
        }
    }

}