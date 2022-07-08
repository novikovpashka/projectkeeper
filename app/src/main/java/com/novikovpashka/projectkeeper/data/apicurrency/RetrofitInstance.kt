package com.novikovpashka.projectkeeper.data.apicurrency

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    operator fun invoke (context: Context): CurrencyApi {

        val networkConnectionInterceptor =
            NetworkConnectionInterceptor(context)

        val okHttpClient by lazy {
            OkHttpClient.Builder()
                .addInterceptor(networkConnectionInterceptor)
                .build()
        }

        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://api.coingate.com/v2/rates/merchant/")
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(CurrencyApi::class.java)
    }
}