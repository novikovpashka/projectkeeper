package com.novikovpashka.projectkeeper.presentation.settingsactivity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import com.novikovpashka.projectkeeper.R
import com.novikovpashka.projectkeeper.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    lateinit var binding: ActivitySettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel: SettingsActivityViewModel by viewModels()

        //set default currency value without animation
        when (viewModel.currentCurrency.value) {
            com.novikovpashka.projectkeeper.CurrencyList.RUB -> {
                binding.rub.isChecked = true
                binding.currencyGroup.jumpDrawablesToCurrentState()
            }
            com.novikovpashka.projectkeeper.CurrencyList.USD -> {
                binding.usd.isChecked = true
                binding.currencyGroup.jumpDrawablesToCurrentState()
            }
            com.novikovpashka.projectkeeper.CurrencyList.EUR -> {
                binding.eur.isChecked = true
                binding.currencyGroup.jumpDrawablesToCurrentState()
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
                com.novikovpashka.projectkeeper.CurrencyList.RUB -> binding.rub.isChecked = true
                com.novikovpashka.projectkeeper.CurrencyList.USD -> binding.usd.isChecked = true
                com.novikovpashka.projectkeeper.CurrencyList.EUR -> binding.eur.isChecked = true
            }
        }

        binding.currencyGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                binding.rub.id -> viewModel.saveAndApplyCurrency(com.novikovpashka.projectkeeper.CurrencyList.RUB)
                binding.usd.id -> viewModel.saveAndApplyCurrency(com.novikovpashka.projectkeeper.CurrencyList.USD)
                binding.eur.id -> viewModel.saveAndApplyCurrency(com.novikovpashka.projectkeeper.CurrencyList.EUR)
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

}