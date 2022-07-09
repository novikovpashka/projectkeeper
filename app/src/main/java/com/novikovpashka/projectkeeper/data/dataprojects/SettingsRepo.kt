package com.novikovpashka.projectkeeper.data.dataprojects

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.novikovpashka.projectkeeper.AccentColors
import com.novikovpashka.projectkeeper.CurrencyList
import com.novikovpashka.projectkeeper.data.apicurrency.CurrencyApi
import com.novikovpashka.projectkeeper.data.apicurrency.RetrofitInstance
import com.novikovpashka.projectkeeper.presentation.mainactivity.OrderParam
import com.novikovpashka.projectkeeper.presentation.mainactivity.SortParam
import retrofit2.Response
import javax.inject.Inject

class SettingsRepo @Inject constructor(private val currencyApi: CurrencyApi, private val sharedPreferences: SharedPreferences){

    suspend fun getRateUSDRUB(): Response<String> {
        return currencyApi.getUSD()
    }

    suspend fun getRateEURRUB(): Response<String> {
        return currencyApi.getEUR()
    }

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

    fun saveCurrentCurrencyToStorage (currency: CurrencyList) {
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

    fun saveAccentColorToStorage (color: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt("accentcolor", color)
        editor.apply()
    }

    fun loadAccentColorFromStorage(): Int {
//        return sharedPreferences.getInt(
//            "accentcolor",
//            ContextCompat.getColor(context, AccentColors.MYORANGE.color)
//        )
        return 0
    }

    fun saveCurrentThemeToStorage() {
        val editor = sharedPreferences.edit()
        editor.putInt("theme", AppCompatDelegate.getDefaultNightMode())
        editor.apply()
    }

    fun saveSortAndOrderParamsToStorage (sortParam: SortParam,
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
                SortParam.BY_DATE_ADDED}
        }
    }

    fun loadOrderParam(): OrderParam {
        val orderParam = sharedPreferences.getString(
            "orderparam",
            OrderParam.ASCENDING.name
        )
        return when (orderParam) {
            OrderParam.ASCENDING.name -> OrderParam.ASCENDING
            else -> OrderParam.ASCENDING
        }
    }

//    companion object {
//        private var settingsRepo: SettingsRepo? = null
//
//        val instance: SettingsRepo?
//            get() {
//                if (settingsRepo == null) {
//                    settingsRepo = SettingsRepo()
//                }
//                return settingsRepo
//            }
//    }

}