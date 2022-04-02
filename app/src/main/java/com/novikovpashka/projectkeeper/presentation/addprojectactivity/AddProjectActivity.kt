package com.novikovpashka.projectkeeper.presentation.addprojectactivity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputLayout
import com.novikovpashka.projectkeeper.R
import com.novikovpashka.projectkeeper.data.datafirestore.Project
import com.novikovpashka.projectkeeper.databinding.ActivityAddProjectBinding
import java.io.Serializable

class AddProjectActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var incomingAdapter: IncomingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        val binding: ActivityAddProjectBinding = ActivityAddProjectBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val projectName: TextInputLayout = binding.projectName
        val projectPrice: TextInputLayout = binding.projectPrice
        val toolbar: MaterialToolbar = binding.addActivityToolbar
        recyclerView = binding.recycler

        initRecycler()

        toolbar.setNavigationOnClickListener { v: View? -> finish() }
        toolbar.setOnMenuItemClickListener { item: MenuItem ->
            if (item.itemId == R.id.add_project) {
                val project: Project
                val incomings: MutableList<Double> = mutableListOf()
                val incomingValues = incomingAdapter.incomingInputsValues
                for (x in incomingValues) {
                    try {
                        if (x != null) {
                            incomings.add(x.toDouble())
                        }
                    } catch (ignored: Exception) {
                    }
                }
                val name = projectName.editText!!.text.toString().trim { it <= ' ' }
                val price = projectPrice.editText!!.text.toString().trim { it <= ' ' }.toDouble()
                project = Project(name, price, incomings)
                startMainActivity(project)
                return@setOnMenuItemClickListener true
            }
            false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val values = incomingAdapter.incomingInputsValues
        val fields = incomingAdapter.inputEditText
        outState.putSerializable("values", values as Serializable)
        outState.putSerializable("fields", fields as Serializable)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        try {
            incomingAdapter.inputEditText =
                savedInstanceState.getSerializable("fields") as MutableList<String?>
        } catch (ignored: Exception) {
        }
        try {
            incomingAdapter.incomingInputsValues =
                savedInstanceState.getSerializable("values") as MutableList<String?>
        } catch (ignored: Exception) {
        }
    }

    private fun startMainActivity(project: Project) {
        val intent = Intent(this@AddProjectActivity, com.novikovpashka.projectkeeper.presentation.mainactivity.MainActivity::class.java)
        intent.putExtra("projectToAdd", project)
        startActivity(intent)
    }

    private fun initRecycler() {
        incomingAdapter = IncomingAdapter()
        incomingAdapter.incomingInputsValues.add(null)
        incomingAdapter.incomingInputsValues.add(null)
        val linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(false)
        recyclerView.adapter = incomingAdapter
        val editTextLines = mutableListOf<String?>()
        editTextLines.add(null)
        editTextLines.add(null)
        val editTextValues = mutableListOf<String?>()
        editTextValues.add(null)
        incomingAdapter.inputEditText = editTextLines
        incomingAdapter.incomingInputsValues = editTextValues
        incomingAdapter.notifyDataSetChanged()
    }
}