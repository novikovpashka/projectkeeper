package com.novikovpashka.projectkeeper.extensions

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import com.novikovpashka.projectkeeper.R
import com.novikovpashka.projectkeeper.data.model.Project
import com.novikovpashka.projectkeeper.presentation.mainactivity.SettingsFragment
import com.novikovpashka.projectkeeper.presentation.projectactivity.ProjectActivity

fun Activity.setTheme(themeId: Int) {
    applicationContext.theme.applyStyle(themeId, true)
}

fun FragmentActivity.startSettingsFragment(): FragmentTransaction {
    return this.supportFragmentManager.beginTransaction()
        .addToBackStack(null)
        .setCustomAnimations(
            R.anim.slide_from_right_settings,
            R.anim.slide_to_left,
            R.anim.slide_to_right,
            R.anim.slide_to_right
        )
        .replace(R.id.fragmentContainer, SettingsFragment::class.java, null)
}

fun Activity.startProjectActivity(project: Project) {
    val intent = Intent(this, ProjectActivity::class.java)
    intent.putExtra("Project", project)
    startActivity(intent)
    overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left_slow)
}