package com.novikovpashka.projectkeeper.presentation.settingsactivity

import android.content.Intent
import android.graphics.Insets
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.RecyclerView
import com.novikovpashka.projectkeeper.CurrencyList
import com.novikovpashka.projectkeeper.R
import com.novikovpashka.projectkeeper.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity(), AccentColorAdapter.OnColorListener{
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var accentColorAdapter: AccentColorAdapter
    private lateinit var viewModel: SettingsActivityViewModel
    private var accentColor: Int = 0

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        val _viewModel: SettingsActivityViewModel by viewModels()
        viewModel = _viewModel
        accentColor = viewModel.accentColor.value!!
        setAccentColor(viewModel.accentColor.value!!)

        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            binding.appbarlayout.setOnApplyWindowInsetsListener { v, insets ->
                val mInsets: Insets = insets.getInsets(WindowInsets.Type.systemBars())
                v.setPadding(0, mInsets.top, 0, 0)
                insets
            }
        }

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
            MODE_NIGHT_NO -> {
                binding.light.isChecked = true
                binding.themeGroup.jumpDrawablesToCurrentState()
            }
        }

        viewModel.currentTheme.observe(this) {
            when (it) {
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> binding.useSystem.isChecked = true
                AppCompatDelegate.MODE_NIGHT_YES -> binding.dark.isChecked = true
                MODE_NIGHT_NO -> binding.light.isChecked = true
            }
        }

        viewModel.currentCurrency.observe(this) {
            when (it) {
                CurrencyList.RUB -> binding.rub.isChecked = true
                CurrencyList.USD -> binding.usd.isChecked = true
                CurrencyList.EUR -> binding.eur.isChecked = true
            }
        }

        viewModel.accentColor.observe(this) {
            accentColorAdapter.currentAccentColor = it
            accentColorAdapter.notifyDataSetChanged()
            setAccentColor(it)
        }

        binding.currencyGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                binding.rub.id -> viewModel.saveAndApplyCurrency(CurrencyList.RUB)
                binding.usd.id -> viewModel.saveAndApplyCurrency(CurrencyList.USD)
                binding.eur.id -> viewModel.saveAndApplyCurrency(CurrencyList.EUR)
            }
        }

        binding.themeGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                binding.useSystem.id -> AppCompatDelegate
                    .setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                binding.light.id -> AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
                binding.dark.id -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            viewModel.saveCurrentTheme()
        }

        binding.settingsToolbar.setNavigationOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
    }

    override fun onColorPick(color: Int) {
        viewModel.saveAndApplyAccentColor(color)
    }

    fun setAccentColor(color: Int) {
        when (color) {
            ContextCompat.getColor(this, R.color.myOrange) -> {
                theme.applyStyle(R.style.Theme_Default, true)
            }

            ContextCompat.getColor(this, R.color.myRed) -> {
                theme.applyStyle(R.style.Theme_Default_Red, true)
            }

            ContextCompat.getColor(this, R.color.myGreen) -> {
                theme.applyStyle(R.style.Theme_Default_Green, true)
            }

            ContextCompat.getColor(this, R.color.myPurple) -> {
                theme.applyStyle(R.style.Theme_Default_Purple, true)
            }

            ContextCompat.getColor(this, R.color.myBlue) -> {
                theme.applyStyle(R.style.Theme_Default_Blue, true)
            }
        }
        if (color != accentColor) {
            recreate()
        }
    }

    public interface recreateMain {
        fun recreateMain()
    }

}