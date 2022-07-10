package com.novikovpashka.projectkeeper

import android.app.Application
import com.novikovpashka.projectkeeper.di.AppComponent
import com.novikovpashka.projectkeeper.di.DaggerAppComponent

class MainApp: Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder()
            .context(this)
            .build()
    }
}

//val Context.appComponent: AppComponent
//    get() = appComponent