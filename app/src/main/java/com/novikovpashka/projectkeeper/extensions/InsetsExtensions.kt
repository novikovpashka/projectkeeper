package com.novikovpashka.projectkeeper.extensions

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.WindowInsets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.math.roundToInt

fun AppBarLayout.setInsets() {
    val window = (this.context as Activity).window
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        this.setOnApplyWindowInsetsListener { v: View, insets: WindowInsets ->
            val mInsets = insets.getInsets(WindowInsets.Type.systemBars())
            v.setPadding(0, mInsets.top, 0, 0)
            insets
        }
    } else {
        ViewCompat.getWindowInsetsController(window.decorView)?.let {
            it.isAppearanceLightStatusBars = false
        }
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.BLACK
    }
}

fun FloatingActionButton.setInsets() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        this.setOnApplyWindowInsetsListener { _: View?, insets: WindowInsets ->
            val mInsets = insets.getInsets(WindowInsets.Type.systemBars())
            val params = androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.bottomMargin = mInsets.bottom + TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                16F,
                resources.getDisplayMetrics()
            ).roundToInt()
            params.rightMargin = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                16F,
                resources.getDisplayMetrics()
            ).roundToInt()
            params.gravity = android.view.Gravity.END or android.view.Gravity.BOTTOM
            this.layoutParams = params
            insets
        }
    }
}