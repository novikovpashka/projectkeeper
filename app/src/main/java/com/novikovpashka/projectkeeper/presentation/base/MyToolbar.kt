package com.novikovpashka.projectkeeper.presentation.base

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.novikovpashka.projectkeeper.R

class MyToolbar: MaterialToolbar {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    var searchText: EditText = EditText(this.context)

    init {
        searchText.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        searchText.background = null
        this.addView(searchText)
        searchText.hint = "Search project"
        searchText.isFocusable = true
        searchText.visibility = View.GONE
    }

    fun setSelectMode() = with (this) {
        menu.clear()
        inflateMenu(R.menu.topappbar_menu_select_mode)
        setNavigationIcon(R.drawable.ic_baseline_close_24)
        searchText.visibility = View.GONE
    }

    fun setSearchMode() = with(this) {
        menu.clear()
        inflateMenu(R.menu.topappbar_menu_search_mode)
        setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        searchText.visibility = View.VISIBLE
        searchText.requestFocus()
        val imm = this.context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(searchText, 0)
    }

    fun setDefaultMode() = with(this) {
        menu.clear()
        inflateMenu(R.menu.topappbar_menu_default_mode)
        setNavigationIcon(R.drawable.ic_baseline_menu_24)
        searchText.visibility = View.GONE
        val imm = this.context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(
            this.windowToken, 0
        )
        searchText.setText("")
    }



}