package com.novikovpashka.projectkeeper.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import com.novikovpashka.projectkeeper.data.model.Project
import javax.inject.Inject

class FirestoreRepository @Inject constructor() {

    private val db = FirebaseFirestore.getInstance()
    private val userEmail = FirebaseAuth.getInstance().currentUser?.email.toString()

    fun getAllProjects(): CollectionReference {
        return db.collection("Users").document(userEmail).collection("Projects")
    }

    fun addProject(project: Project) {
        db.collection("Users").document(userEmail).collection("Projects")
            .add(project)
    }

    fun addSeveralProjects(projects: List<Project>) {
        val writeBatch: WriteBatch = db.batch()
        for (project in projects) {
            writeBatch.set(
                db.collection("Users").document(userEmail).collection("Projects").document(),
                project
            )
        }
        writeBatch.commit()
    }

    fun updateProject(project: Project) {
        db.collection("Users").document(userEmail).collection("Projects")
            .whereEqualTo("dateStamp", project.dateStamp).get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                for (documentSnapshot: DocumentSnapshot in queryDocumentSnapshots.documents) {
                    documentSnapshot.reference.set(project)
                }
            }
    }

    fun deleteProject(project: Project) {
        db.collection("Users").document(userEmail).collection("Projects")
            .whereEqualTo("dateStamp", project.dateStamp).get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                for (documentSnapshot: DocumentSnapshot in queryDocumentSnapshots.documents) {
                    documentSnapshot.reference.delete()
                }
            }
    }

    fun deleteSeveralProjects(projects: List<Project>) {
        val writeBatch: WriteBatch = db.batch()
        db.collection("Users").document(userEmail).collection("Projects")
            .get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                for (documentSnapshot: DocumentSnapshot in queryDocumentSnapshots.documents) {
                    if (projects.contains(documentSnapshot.toObject(Project::class.java))) {
                        writeBatch.delete(documentSnapshot.reference)
                    }
                }
                writeBatch.commit()
            }
    }

    companion object {
        private var firestoreRepository: FirestoreRepository? = null

        val instance: FirestoreRepository?
            get() {
                if (firestoreRepository == null) {
                    firestoreRepository = FirestoreRepository()
                }
                return firestoreRepository
            }
    }
}