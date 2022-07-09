package com.novikovpashka.projectkeeper.di

import android.content.Context
import android.content.SharedPreferences
import com.novikovpashka.projectkeeper.data.apicurrency.CurrencyApi
import com.novikovpashka.projectkeeper.data.dataprojects.SettingsRepo
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DataModule {

    @Singleton
    @Provides
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
    }


}