package com.novikovpashka.projectkeeper.data.repository

import com.novikovpashka.projectkeeper.data.model.Project
import com.novikovpashka.projectkeeper.data.room.ProjectsDAO
import com.novikovpashka.projectkeeper.extensions.toProject
import com.novikovpashka.projectkeeper.extensions.toProjectWithIncomingsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RoomRepository @Inject constructor(private val projectsDAO: ProjectsDAO) {

    suspend fun getProjects(): List<Project> {

        val projects = mutableListOf<Project>()
        withContext(Dispatchers.IO) {
            val projectsRoom = projectsDAO.getAll()
            projectsRoom.forEach {
                projects.add(it.toProject())
            }
        }
        return projects
    }

    suspend fun addProject(project: Project) {

        val projectWithIncomings = project.toProjectWithIncomingsEntity()
        projectsDAO.addProject(projectWithIncomings.projectEntity)
        projectWithIncomings.incomingsEntity.forEach {
            projectsDAO.addIncoming(it)
        }
    }

    suspend fun addMultipleProjects(projects: List<Project>) {

        projects.forEach { project ->
            val projectWithIncomings = project.toProjectWithIncomingsEntity()
            projectsDAO.addProject(projectWithIncomings.projectEntity)
            projectWithIncomings.incomingsEntity.forEach {
                projectsDAO.addIncoming(it)
            }
        }
    }

    suspend fun deleteProject(project: Project) {

        val projectWithIncomings = project.toProjectWithIncomingsEntity()
        projectsDAO.deleteProject(projectWithIncomings.projectEntity)

        projectWithIncomings.incomingsEntity.forEach {
            projectsDAO.deleteIncoming(it)
        }
    }
}