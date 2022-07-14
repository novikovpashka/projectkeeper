package com.novikovpashka.projectkeeper.extensions

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import com.novikovpashka.projectkeeper.R
import com.novikovpashka.projectkeeper.presentation.mainactivity.SettingsFragment

fun Activity.setTheme(themeId: Int) {
    applicationContext.theme.applyStyle(themeId, true)
}

fun FragmentActivity.startSettingsFragment() : FragmentTransaction {
    return this.supportFragmentManager.beginTransaction()
        .setReorderingAllowed(true)
        .setCustomAnimations(
            R.anim.slide_from_right_settings,
            R.anim.slide_to_left,
            R.anim.slide_to_right,
            R.anim.slide_to_right
        )
        .add(R.id.fragmentContainer, SettingsFragment::class.java, null)
        .addToBackStack(null)
}