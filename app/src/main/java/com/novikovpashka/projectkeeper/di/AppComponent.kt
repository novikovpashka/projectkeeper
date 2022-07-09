package com.novikovpashka.projectkeeper.di

import android.content.Context
import com.novikovpashka.projectkeeper.presentation.mainactivity.MainActivity
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [NetworkModule::class, DataModule::class])
interface AppComponent {
    fun inject (mainActivity: MainActivity)

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun context (context: Context): Builder

        fun build(): AppComponent
    }
}


