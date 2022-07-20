package com.novikovpashka.projectkeeper.presentation.base

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.WindowInsets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import com.google.android.material.appbar.AppBarLayout

//class MyAppBarLayout : AppBarLayout {
//    constructor(context: Context) : super(context)
//    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
//    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
//        context,
//        attrs,
//        defStyleAttr
//    )

class MyAppBarLayout(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : AppBarLayout(
    context,
    attrs,
    defStyleAttr
) {
    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    init {
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
}