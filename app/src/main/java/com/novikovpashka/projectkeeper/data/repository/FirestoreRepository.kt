package com.novikovpashka.projectkeeper.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.WriteBatch
import com.novikovpashka.projectkeeper.data.model.Project
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirestoreRepository @Inject constructor() {

    private val db = FirebaseFirestore.getInstance()
    private val userEmail = FirebaseAuth.getInstance().currentUser?.email.toString()

    suspend fun getProjects(): List<Project> {

        return suspendCoroutine { continuation ->
            val projectList = mutableListOf<Project>()
            db.collection("Users").document(userEmail)
                .collection("Projects").get()
                .addOnSuccessListener { querySnapshot ->
                    querySnapshot.forEach {
                        projectList.add(it.toObject(Project::class.java))
                    }
                    continuation.resume(projectList)
                }
        }
    }


    fun addProject(project: Project) {

        db.collection("Users").document(userEmail).collection("Projects")
            .add(project)
    }

    fun addMultipleProjects(projects: List<Project>) {

        val writeBatch: WriteBatch = db.batch()
        for (project in projects) {
            writeBatch.set(
                db.collection("Users").document(userEmail).collection("Projects").document(),
                project
            )
        }
        writeBatch.commit()
    }

    fun updateProject(project: Project): Task<QuerySnapshot> {

        return db.collection("Users").document(userEmail).collection("Projects")
            .whereEqualTo("dateStamp", project.dateStamp).get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                for (documentSnapshot: DocumentSnapshot in queryDocumentSnapshots.documents) {
                    documentSnapshot.reference.set(project)
                }
            }
    }

    fun deleteProject(project: Project): Task<QuerySnapshot> {

        return db.collection("Users").document(userEmail).collection("Projects")
            .whereEqualTo("dateStamp", project.dateStamp).get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                for (documentSnapshot: DocumentSnapshot in queryDocumentSnapshots.documents) {
                    documentSnapshot.reference.delete()
                }
            }
    }
}