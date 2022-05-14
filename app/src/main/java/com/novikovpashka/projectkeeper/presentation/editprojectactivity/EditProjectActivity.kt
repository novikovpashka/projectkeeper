package com.novikovpashka.projectkeeper.presentation.editprojectactivity

import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.appbar.MaterialToolbar
import android.os.Bundle
import android.view.View
import com.novikovpashka.projectkeeper.data.datafirestore.Project
import com.novikovpashka.projectkeeper.databinding.ActivityEditProjectBinding

class EditProjectActivity : AppCompatActivity() {

    private lateinit var projectName: EditText
    private lateinit var projectPrice: EditText
    private lateinit var projectIncoming: TextInputLayout
    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityEditProjectBinding = ActivityEditProjectBinding.inflate(layoutInflater)
        setContentView(binding.root)
        projectName = binding.projectName
        projectPrice = binding.projectPrice
//        projectIncoming = binding.incoming.incomingText
        toolbar = binding.toolbar

        setInputFieldsDisable()

        val project: Project = intent.getParcelableExtra("Project")!!
        projectName.setText(project.name)
        projectPrice.setText(project.price.toString())
        projectIncoming.editText!!.setText(project.incomings[0].toString())
        toolbar.setNavigationOnClickListener { view: View? -> finish() }
    }

    private fun setInputFieldsDisable() {
        projectName.isEnabled = false
        projectPrice.isEnabled = false
        projectIncoming.isEnabled = false
    }

    private fun setInputFieldsEnable() {
        projectName.isEnabled = true
        projectPrice.isEnabled = true
        projectIncoming.isEnabled = true
    }
}