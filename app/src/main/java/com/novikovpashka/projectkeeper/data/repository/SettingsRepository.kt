package com.novikovpashka.projectkeeper.data.repository

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.novikovpashka.projectkeeper.CurrencyList
import com.novikovpashka.projectkeeper.R
import com.novikovpashka.projectkeeper.presentation.mainactivity.OrderParam
import com.novikovpashka.projectkeeper.presentation.mainactivity.SharedViewModel
import com.novikovpashka.projectkeeper.presentation.mainactivity.SortParam
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    ) {

    fun saveRatesToStorage(USD: String, EUR: String) {
        val editor = sharedPreferences.edit()
        editor.putString("usdrubRate", USD)
        editor.putString("eurrubRate", EUR)
        editor.apply()
    }

    fun loadUSDRateFromStorage(): String {
        return sharedPreferences.getString("usdrubRate", "No data")!!
    }

    fun loadEURRateFromStorage(): String {
        return sharedPreferences.getString("eurrubRate", "No data")!!
    }

    fun saveCurrentCurrencyToStorage(currency: CurrencyList) {
        val editor = sharedPreferences.edit()
        val currentCurrency: String = when (currency) {
            CurrencyList.RUB -> CurrencyList.RUB.name
            CurrencyList.USD -> CurrencyList.USD.name
            CurrencyList.EUR -> CurrencyList.EUR.name
        }
        editor.putString("currency", currentCurrency)
        editor.apply()
    }

    fun loadCurrentCurrencyFromStorage(): CurrencyList {
        return when (sharedPreferences.getString("currency", "RUB")) {
            CurrencyList.USD.name -> CurrencyList.USD
            CurrencyList.EUR.name -> CurrencyList.EUR
            else -> CurrencyList.RUB
        }
    }

    fun saveAccentColorToStorage(color: Int) {
        val editor = sharedPreferences.edit()
        when (color) {
            R.color.myOrange -> {
                editor.putInt("themeid", R.style.Theme_Default)
            }
            R.color.myRed -> {
                editor.putInt("themeid", R.style.Theme_Default_Red)
            }
            R.color.myGreen -> {
                editor.putInt("themeid", R.style.Theme_Default_Green)
            }
            R.color.myPurple -> {
                editor.putInt("themeid", R.style.Theme_Default_Purple)
            }
            R.color.myBlue -> {
                editor.putInt("themeid", R.style.Theme_Default_Blue)
            }
        }
        editor.putInt("accentcolor", color)
        editor.apply()
    }

    fun loadAccentColorFromStorage(): Int {
        return sharedPreferences.getInt(
            "accentcolor",
            R.color.myOrange
        )
    }

    fun loadThemeIdFromStorage(): Int {
        return sharedPreferences.getInt(
            "themeid",
            R.style.Theme_Default
        )
    }

    fun saveNightModeToStorage() {
        val editor = sharedPreferences.edit()
        editor.putInt("nightmode", AppCompatDelegate.getDefaultNightMode())
        editor.apply()
    }

    fun loadNightModeFromStorage(): Int {
        return sharedPreferences.getInt(
            "nightmode",
            SharedViewModel.NightMode.AS_SYSTEM.value
        )
    }

    fun saveSortAndOrderParamsToStorage(
        sortParam: SortParam,
        orderParam: OrderParam
    ) {
        val editor = sharedPreferences.edit()
        editor.putString("sortparam", sortParam.name)
        editor.putString("orderparam", orderParam.name)
        editor.apply()
    }

    fun loadSortParam(): SortParam {
        val sortParam = sharedPreferences.getString(
            "sortparam",
            SortParam.BY_DATE_ADDED.name
        )
        return when (sortParam) {
            SortParam.BY_DATE_ADDED.name -> SortParam.BY_DATE_ADDED
            SortParam.BY_NAME.name -> SortParam.BY_NAME
            else -> {
                SortParam.BY_DATE_ADDED
            }
        }
    }

    fun loadOrderParam(): OrderParam {
        val orderParam = sharedPreferences.getString(
            "orderparam",
            OrderParam.ASCENDING.name
        )
        return when (orderParam) {
            OrderParam.ASCENDING.name -> OrderParam.ASCENDING
            else -> OrderParam.DESCENDING
        }
    }

}