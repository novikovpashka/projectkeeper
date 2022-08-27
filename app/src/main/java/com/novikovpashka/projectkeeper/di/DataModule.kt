package com.novikovpashka.projectkeeper.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.novikovpashka.projectkeeper.data.room.ProjectsDAO
import com.novikovpashka.projectkeeper.data.room.RoomDatabase
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

    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Singleton
    @Provides
    fun provideRoomDataBase(context: Context): RoomDatabase {
        return Room.databaseBuilder(
            context,
            RoomDatabase::class.java,
            "projects"
        ).build()
    }

    @Singleton
    @Provides
    fun provideRoomDAO(roomDatabase: RoomDatabase): ProjectsDAO {
        return roomDatabase.projectsDao()
    }

}