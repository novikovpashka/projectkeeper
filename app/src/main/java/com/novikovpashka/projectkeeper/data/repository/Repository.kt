package com.novikovpashka.projectkeeper.data.repository

import com.novikovpashka.projectkeeper.data.model.Project
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class Repository @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val roomRepository: RoomRepository
) {

    suspend fun getProjects(): List<Project> {
        val projectsRoom = roomRepository.getProjects()
        return projectsRoom.ifEmpty {
            val projectsFirestore = firestoreRepository.getProjects()
            roomRepository.addMultipleProjects(projectsFirestore)
            projectsFirestore
        }
    }

    suspend fun addProject(project: Project) {
        firestoreRepository.addProject(project)
        roomRepository.addProject(project)
    }

    suspend fun addMultipleProjects(projects: List<Project>) {
        firestoreRepository.addMultipleProjects(projects)
        roomRepository.addMultipleProjects(projects)
    }

    suspend fun deleteProject(project: Project) {
        firestoreRepository.deleteProject(project)
        roomRepository.deleteProject(project)
    }

    suspend fun deleteMultipleProjects(projects: List<Project>) {
        for (project in projects) {
            firestoreRepository.deleteProject(project)
            roomRepository.deleteProject(project)
        }
    }


}