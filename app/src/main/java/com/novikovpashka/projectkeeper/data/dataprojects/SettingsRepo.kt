package com.novikovpashka.projectkeeper.data.dataprojects

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.novikovpashka.projectkeeper.AccentColors
import com.novikovpashka.projectkeeper.CurrencyList
import com.novikovpashka.projectkeeper.data.apicurrency.RetrofitInstance
import retrofit2.Response

class SettingsRepo {

    suspend fun getRateUSDRUB(context: Context): Response<String> {
        return RetrofitInstance.invoke(context).getUSD()
    }

    suspend fun getRateEURRUB(context: Context): Response<String> {
        return RetrofitInstance.invoke(context).getEUR()
    }

    fun saveUSDRateToStorage(context: Context, USD: String) {
        val sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("USDRUB", USD)
        editor.apply()
    }

    fun saveEURRateToStorage(context: Context, EUR: String) {
        val sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("EURRUB", EUR)
        editor.apply()
    }

    fun loadUSDRateFromStorage(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("USDRUB", "No data")!!
    }

    fun loadEURRateFromStorage(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("EURRUB", "No data")!!
    }

    fun saveCurrentCurrencyToStorage (context: Context, currency: CurrencyList) {
        val sharedPreferences = context.getSharedPreferences("sharedPrefs",
            AppCompatActivity.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        val currentCurrency: String = when (currency) {
            CurrencyList.RUB -> "RUB"
            CurrencyList.USD -> "USD"
            CurrencyList.EUR -> "EUR"
        }
        editor.putString("currency", currentCurrency)
        editor.apply()
    }

    fun loadCurrentCurrencyFromStorage(context: Context): CurrencyList {
        val sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val currency = sharedPreferences.getString("currency", "RUB")
        if (currency == "USD") {
            return CurrencyList.USD
        } else if (currency == "EUR") {
            return CurrencyList.EUR
        } else return CurrencyList.RUB
    }

    fun saveAccentColorToStorage (context: Context, color: Int) {
        val sharedPreferences = context.getSharedPreferences("sharedPrefs",
            AppCompatActivity.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        editor.putInt("accentcolor", color)
        editor.apply()
    }

    fun loadAccentColorFromStorage (context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val accentColor = sharedPreferences.getInt("accentcolor",
            ContextCompat.getColor(context, AccentColors.MYORANGE.color))
        return accentColor
    }

    fun saveCurrentThemeToStorage(context: Context) {
        val sharedPreferences = context.getSharedPreferences("sharedPrefs",
            AppCompatActivity.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        editor.putInt("theme", AppCompatDelegate.getDefaultNightMode())
        editor.apply()
    }

    companion object {
        private var settingsRepo: SettingsRepo? = null

        val instance: SettingsRepo?
            get() {
                if (settingsRepo == null) {
                    settingsRepo = SettingsRepo()
                }
                return settingsRepo
            }
    }

}