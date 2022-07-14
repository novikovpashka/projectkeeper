package com.novikovpashka.projectkeeper.extensions

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import com.novikovpashka.projectkeeper.R
import com.novikovpashka.projectkeeper.presentation.mainactivity.SettingsFragment

fun Fragment.setTheme(themeId: Int) {
    this.requireContext().theme.applyStyle(themeId, true)
}