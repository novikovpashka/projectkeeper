package com.novikovpashka.projectkeeper.presentation.loginactivity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.novikovpashka.projectkeeper.R
import com.novikovpashka.projectkeeper.data.datafirestore.ProjectFirestoreRepo
import com.novikovpashka.projectkeeper.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var progressBar: LinearProgressIndicator

    override fun onCreate(savedInstanceState: Bundle?) {
        val projectsRepository = ProjectFirestoreRepo.instance!!
        setAccentColor(projectsRepository.loadAccentColorFromStorage(this))
        super.onCreate(savedInstanceState)
        val binding: ActivityLoginBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val loginButton: Button = binding.loginButton
        val title: TextView = binding.title
        val emailEditText: EditText = binding.editTextTextEmailAddress
        val passwordEditText: EditText = binding.editTextTextPassword
        progressBar  = binding.progressbarLogin

        title.animate().duration = 1000
        title.animate().alpha(1f)

        if (mAuth.currentUser != null) {
            startMainActivity()
        }
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty() ) {
                progressOn()
                createOrLoginUserByEmail(email, password)
            }
            else Snackbar.make(it, "Fields can't be empty", BaseTransientBottomBar.LENGTH_LONG).show()
        }
    }

    private fun createOrLoginUserByEmail(email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                startMainActivity()
            } else {
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        startMainActivity()
                    } else {
                        progressOff()
                        Snackbar.make(window.decorView.rootView,
                            task.exception?.message.toString(), BaseTransientBottomBar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, com.novikovpashka.projectkeeper.presentation.mainactivity.MainActivity::class.java)
        startActivity(intent)
    }

    private fun progressOn() {
        progressBar.visibility = View.VISIBLE
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun progressOff() {
        progressBar.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    fun setAccentColor(color: Int) {
        when (color) {
            ContextCompat.getColor(this, R.color.myOrange) -> {
                theme.applyStyle(R.style.Theme_Default, true)
            }

            ContextCompat.getColor(this, R.color.myRed) -> {
                theme.applyStyle(R.style.Theme_Default_Red, true)
            }

            ContextCompat.getColor(this, R.color.myGreen) -> {
                theme.applyStyle(R.style.Theme_Default_Green, true)
            }

            ContextCompat.getColor(this, R.color.myPurple) -> {
                theme.applyStyle(R.style.Theme_Default_Purple, true)
            }

            ContextCompat.getColor(this, R.color.myBlue) -> {
                theme.applyStyle(R.style.Theme_Default_Blue, true)
            }
        }
    }
}