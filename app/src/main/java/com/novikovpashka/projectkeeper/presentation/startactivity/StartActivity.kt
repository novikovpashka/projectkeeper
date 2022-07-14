package com.novikovpashka.projectkeeper.presentation.startactivity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.novikovpashka.projectkeeper.AccentColors
import com.novikovpashka.projectkeeper.R
import com.novikovpashka.projectkeeper.databinding.ActivityStartBinding
import com.novikovpashka.projectkeeper.presentation.loginactivity.LoginActivity

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
        val intent = Intent(this, com.novikovpashka.projectkeeper.presentation.mainactivity.MainActivity::class.java)
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
            .getInt("nightmode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM))

        val accentColor = sharedPreferences.getInt("accentcolor",
            ContextCompat.getColor(context, AccentColors.MYORANGE.color))

        when (accentColor) {
            ContextCompat.getColor(this, R.color.myOrange) ->
                theme.applyStyle(R.style.Theme_Default, true)

            ContextCompat.getColor(this, R.color.myRed) ->
                theme.applyStyle(R.style.Theme_Default_Red, true)

            ContextCompat.getColor(this, R.color.myGreen) ->
                theme.applyStyle(R.style.Theme_Default_Green, true)

            ContextCompat.getColor(this, R.color.myPurple) ->
                theme.applyStyle(R.style.Theme_Default_Purple, true)
            ContextCompat.getColor(this, R.color.myBlue) -> {
                theme.applyStyle(R.style.Theme_Default_Blue, true)
            }

        }

    }
}