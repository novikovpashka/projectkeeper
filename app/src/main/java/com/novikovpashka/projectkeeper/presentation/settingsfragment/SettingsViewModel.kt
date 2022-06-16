package com.novikovpashka.projectkeeper.presentation.settingsfragment

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.novikovpashka.projectkeeper.AccentColors
import com.novikovpashka.projectkeeper.CurrencyList
import com.novikovpashka.projectkeeper.data.datafirestore.ProjectFirestoreRepo

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val projectsRepository = ProjectFirestoreRepo.instance!!

    val currentCurrency = MutableLiveData(projectsRepository
        .loadCurrentCurrencyFromStorage(application.applicationContext))
    val currentTheme = MutableLiveData(AppCompatDelegate.getDefaultNightMode())
    val accentColor = MutableLiveData(projectsRepository.loadAccentColorFromStorage(application.applicationContext))

    fun saveAndApplyCurrency (currency: CurrencyList) {
        projectsRepository.saveCurrentCurrencyToStorage(getApplication<Application>()
            .applicationContext, currency)
        currentCurrency.value = currency
    }

    fun saveCurrentTheme () {
        projectsRepository.saveCurrentThemeToStorage(getApplication<Application>()
            .applicationContext)
        currentTheme.value = AppCompatDelegate.getDefaultNightMode()
    }

    fun saveAndApplyAccentColor(color: Int) {
        projectsRepository.saveAccentColorToStorage(getApplication<Application>().applicationContext, color)
        accentColor.value = color
    }

    fun getAccentColors(): MutableList<Int> {
        val colorList: MutableList<Int> = mutableListOf()
        for (x in AccentColors.values()) {
            colorList.add(ContextCompat.getColor(getApplication<Application>().applicationContext, x.color))
        }
        return colorList
    }
}