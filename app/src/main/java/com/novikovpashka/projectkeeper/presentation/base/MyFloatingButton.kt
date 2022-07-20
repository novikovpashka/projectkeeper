package com.novikovpashka.projectkeeper.presentation.base

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.core.view.updateLayoutParams
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.math.roundToInt

class MyFloatingButton(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : FloatingActionButton(
    context,
    attrs,
    defStyleAttr
) {
    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    init {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            this.setOnApplyWindowInsetsListener { view: View?, insets: WindowInsets ->
                val mInsets = insets.getInsets(WindowInsets.Type.systemBars())
                view!!.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    rightMargin = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        16F,
                        resources.displayMetrics
                    ).roundToInt()
                    bottomMargin = mInsets.bottom + TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        16F,
                        resources.displayMetrics
                    ).roundToInt()
                }
                WindowInsets.CONSUMED
            }
        }
    }

}