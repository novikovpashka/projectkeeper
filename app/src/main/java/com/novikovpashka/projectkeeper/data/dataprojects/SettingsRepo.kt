package com.novikovpashka.projectkeeper.data.dataprojects

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.novikovpashka.projectkeeper.AccentColors
import com.novikovpashka.projectkeeper.CurrencyList
import com.novikovpashka.projectkeeper.data.apicurrency.RetrofitInstance
import com.novikovpashka.projectkeeper.presentation.mainactivity.OrderParam
import com.novikovpashka.projectkeeper.presentation.mainactivity.SortParam
import retrofit2.Response

class SettingsRepo {

    suspend fun getRateUSDRUB(context: Context): Response<String> {
        return RetrofitInstance.invoke(context).getUSD()
    }

    suspend fun getRateEURRUB(context: Context): Response<String> {
        return RetrofitInstance.invoke(context).getEUR()
    }

    fun saveRatesToStorage(context: Context, USD: String, EUR: String) {
        val sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("usdrubRate", USD)
        editor.putString("eurrubRate", EUR)
        editor.apply()
    }

    fun loadUSDRateFromStorage(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("usdrubRate", "No data")!!
    }

    fun loadEURRateFromStorage(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("eurrubRate", "No data")!!
    }

    fun saveCurrentCurrencyToStorage (context: Context, currency: CurrencyList) {
        val sharedPreferences = context.getSharedPreferences("sharedPrefs",
            AppCompatActivity.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        val currentCurrency: String = when (currency) {
            CurrencyList.RUB -> CurrencyList.RUB.name
            CurrencyList.USD -> CurrencyList.USD.name
            CurrencyList.EUR -> CurrencyList.EUR.name
        }
        editor.putString("currency", currentCurrency)
        editor.apply()
    }

    fun loadCurrentCurrencyFromStorage(context: Context): CurrencyList {
        val sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        return when (sharedPreferences.getString("currency", "RUB")) {
            CurrencyList.USD.name -> CurrencyList.USD
            CurrencyList.EUR.name -> CurrencyList.EUR
            else -> CurrencyList.RUB
        }
    }

    fun saveAccentColorToStorage (context: Context, color: Int) {
        val sharedPreferences = context.getSharedPreferences("sharedPrefs",
            AppCompatActivity.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        editor.putInt("accentcolor", color)
        editor.apply()
    }

    fun loadAccentColorFromStorage(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt(
            "accentcolor",
            ContextCompat.getColor(context, AccentColors.MYORANGE.color)
        )
    }

    fun saveCurrentThemeToStorage(context: Context) {
        val sharedPreferences = context.getSharedPreferences("sharedPrefs",
            AppCompatActivity.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        editor.putInt("theme", AppCompatDelegate.getDefaultNightMode())
        editor.apply()
    }

    fun saveSortAndOrderParamsToStorage (context: Context,
                                         sortParam: SortParam,
                                         orderParam: OrderParam
    ) {
        val sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("sortparam", sortParam.name)
        editor.putString("orderparam", orderParam.name)
        editor.apply()
    }

    fun loadSortParam(context: Context): SortParam {
        val sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val sortParam = sharedPreferences.getString(
            "sortparam",
            SortParam.BY_DATE_ADDED.name
        )
        return when (sortParam) {
            SortParam.BY_DATE_ADDED.name -> SortParam.BY_DATE_ADDED
            SortParam.BY_NAME.name -> SortParam.BY_NAME
            else -> {
                SortParam.BY_DATE_ADDED}
        }
    }

    fun loadOrderParam(context: Context): OrderParam {
        val sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val orderParam = sharedPreferences.getString(
            "orderparam",
            OrderParam.ASCENDING.name
        )
        return when (orderParam) {
            OrderParam.ASCENDING.name -> OrderParam.ASCENDING
            else -> OrderParam.ASCENDING
        }
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