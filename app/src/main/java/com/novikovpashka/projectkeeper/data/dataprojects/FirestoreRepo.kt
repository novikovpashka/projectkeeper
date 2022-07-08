package com.novikovpashka.projectkeeper.data.dataprojects

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.novikovpashka.projectkeeper.AccentColors
import com.novikovpashka.projectkeeper.CurrencyList
import com.novikovpashka.projectkeeper.data.apicurrency.RetrofitInstance
import retrofit2.Response

class FirestoreRepo {

    private val db = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser?.email.toString()

    fun getAllProjects(): CollectionReference {
        return db.collection("Users").
            document(user).collection("Projects")
    }

    fun addProject(project: Project) {
        db.collection("Users").document(user).collection("Projects")
            .add(project)
    }

    fun updateProject(project: Project) {
        db.collection("Users").document(user).collection("Projects")
            .whereEqualTo("dateStamp", project.dateStamp).get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                for (documentSnapshot: DocumentSnapshot in queryDocumentSnapshots.documents) {
                    documentSnapshot.reference.set(project)
                }
            }
    }

    fun deleteProject(project: Project) {
        Log.v("mytag", project.dateStamp.toString())
        db.collection("Users").document(user).collection("Projects")
            .whereEqualTo("dateStamp", project.dateStamp).get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                for (documentSnapshot: DocumentSnapshot in queryDocumentSnapshots.documents) {
                    documentSnapshot.reference.delete()
                }
            }
    }

    fun deleteSeveralProjects(projects: List<Project>) {
        db.collection("Users").document(user).collection("Projects")
            .get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                for (documentSnapshot: DocumentSnapshot in queryDocumentSnapshots.documents) {
                    if (projects.contains(documentSnapshot.toObject(Project::class.java))) {
                        documentSnapshot.reference.delete()
                    }
                }
            }
    }

    companion object {
        private var firestoreRepo: FirestoreRepo? = null

        val instance: FirestoreRepo?
            get() {
                if (firestoreRepo == null) {
                    firestoreRepo = FirestoreRepo()
                }
                return firestoreRepo
            }
    }
}