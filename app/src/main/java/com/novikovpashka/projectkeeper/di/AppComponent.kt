package com.novikovpashka.projectkeeper.di

import android.content.Context
import com.novikovpashka.projectkeeper.presentation.addprojectactivity.AddProjectActivity
import com.novikovpashka.projectkeeper.presentation.editprojectactivity.EditProjectActivity
import com.novikovpashka.projectkeeper.presentation.mainactivity.BottomSortDialog
import com.novikovpashka.projectkeeper.presentation.mainactivity.MainActivity
import com.novikovpashka.projectkeeper.presentation.mainactivity.SettingsFragment
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [NetworkModule::class, DataModule::class])
interface AppComponent {
    fun inject (mainActivity: MainActivity)
    fun inject (settingsFragment: SettingsFragment)
    fun inject(addProjectActivity: AddProjectActivity)
    fun inject(editProjectActivity: EditProjectActivity)
    fun inject(bottomSortDialog: BottomSortDialog)

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun context (context: Context): Builder

        fun build(): AppComponent
    }
}


