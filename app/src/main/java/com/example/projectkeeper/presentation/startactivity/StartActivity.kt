package com.example.projectkeeper.presentation.startactivity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.example.projectkeeper.databinding.ActivityStartBinding
import com.example.projectkeeper.presentation.loginactivity.LoginActivity
import com.example.projectkeeper.presentation.mainactivity.MainActivity
import com.google.firebase.auth.FirebaseAuth

class StartActivity : AppCompatActivity() {
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firebaseUser = mAuth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        loadSavedThemeFromStorage()
        val binding: ActivityStartBinding = ActivityStartBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (firebaseUser != null) {
            startMainActivity()
        } else startLoginActivity()
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    fun loadSavedThemeFromStorage() {
        val context = applicationContext
        val sharedPreferences = context.getSharedPreferences("sharedPrefs", MODE_PRIVATE)
        AppCompatDelegate.setDefaultNightMode(sharedPreferences
            .getInt("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM))
    }
}