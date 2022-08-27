package com.novikovpashka.projectkeeper.extensions

import androidx.fragment.app.Fragment

fun Fragment.setTheme(themeId: Int) {
    this.requireContext().theme.applyStyle(themeId, true)
}