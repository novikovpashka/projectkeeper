package com.novikovpashka.projectkeeper.data.apicurrency

import retrofit2.Response
import retrofit2.http.GET

interface CurrencyApi {
    @GET("USD/RUB")
    suspend fun getUSD(): Response<String>

    @GET("EUR/RUB")
    suspend fun getEUR(): Response<String>
}