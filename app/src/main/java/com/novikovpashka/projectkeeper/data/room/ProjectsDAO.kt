package com.novikovpashka.projectkeeper.data.room

import androidx.room.*
import com.novikovpashka.projectkeeper.data.model.IncomingEntity
import com.novikovpashka.projectkeeper.data.model.ProjectEntity
import com.novikovpashka.projectkeeper.data.model.ProjectWithIncomings

@Dao
interface ProjectsDAO {
    @Transaction
    @Query("SELECT * FROM ProjectEntity")
    suspend fun getAll(): List<ProjectWithIncomings>

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    suspend fun addProject(projectEntity: ProjectEntity)

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    suspend fun addIncoming(incomingEntity: IncomingEntity)

    @Query("DELETE FROM ProjectEntity")
    suspend fun deleteAllProjects()

    @Query("DELETE FROM IncomingEntity")
    suspend fun deleteAllIncomings()

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    @Delete
    suspend fun deleteIncoming(incoming: IncomingEntity)

}