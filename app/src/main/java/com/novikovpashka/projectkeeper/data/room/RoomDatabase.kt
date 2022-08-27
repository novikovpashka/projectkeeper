package com.novikovpashka.projectkeeper.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.novikovpashka.projectkeeper.data.model.IncomingEntity
import com.novikovpashka.projectkeeper.data.model.ProjectEntity

@Database (entities = [ProjectEntity::class, IncomingEntity::class], version = 1)
abstract class RoomDatabase: RoomDatabase() {
    abstract fun projectsDao(): ProjectsDAO
}