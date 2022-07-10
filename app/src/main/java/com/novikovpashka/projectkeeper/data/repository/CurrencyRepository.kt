package com.novikovpashka.projectkeeper.data.repository

import com.novikovpashka.projectkeeper.data.apicurrency.CurrencyApi
import retrofit2.Response
import javax.inject.Inject

class CurrencyRepository @Inject constructor(private val currencyApi: CurrencyApi) {

    suspend fun getRateUSDRUB(): Response<String> {
        return currencyApi.getUSD()
    }

    suspend fun getRateEURRUB(): Response<String> {
        return currencyApi.getEUR()
    }
}