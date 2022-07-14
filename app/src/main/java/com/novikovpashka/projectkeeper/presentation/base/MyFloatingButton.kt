package com.novikovpashka.projectkeeper.presentation.base

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.WindowInsets
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.math.roundToInt

class MyFloatingButton : FloatingActionButton {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {

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

}