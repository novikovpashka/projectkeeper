package com.novikovpashka.projectkeeper.presentation.settingsactivity

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.novikovpashka.projectkeeper.CurrencyList
import com.novikovpashka.projectkeeper.data.datafirestore.ProjectFirestoreRepo

class SettingsActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val projectsRepository = ProjectFirestoreRepo.instance!!

    val currentCurrency = MutableLiveData(projectsRepository
        .loadCurrentCurrencyFromStorage(getApplication<Application>().applicationContext))
    val currentTheme = MutableLiveData(AppCompatDelegate.getDefaultNightMode())

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

}