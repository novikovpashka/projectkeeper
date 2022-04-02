package com.example.projectkeeper.presentation.loginactivity

import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.progressindicator.LinearProgressIndicator
import android.os.Bundle
import android.content.Intent
import android.view.View
import com.example.projectkeeper.presentation.mainactivity.MainActivity
import android.view.WindowManager
import android.widget.Button
import com.example.projectkeeper.databinding.ActivityLoginBinding
import com.example.projectkeeper.domain.models.EmailAndPasswordParam
import com.example.projectkeeper.domain.usecases.LoginByEmailUseCase
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

class LoginActivity : AppCompatActivity() {

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var progressBar: LinearProgressIndicator
    private val LoginByEmailUseCase: LoginByEmailUseCase = LoginByEmailUseCase()

    override fun onCreate(savedInstanceState: Bundle?) {
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
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val param = EmailAndPasswordParam(email = email, password = password)

            if (LoginByEmailUseCase.execute(EmailAndPasswordParam = param)) {
                progressOn()
                createOrLoginUserByEmail(param.email.trim(), param.password.trim())
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
        val intent = Intent(this, MainActivity::class.java)
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
}