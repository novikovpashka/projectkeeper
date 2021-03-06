package com.novikovpashka.projectkeeper.presentation.addprojectactivity

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Insets
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowInsets
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.novikovpashka.projectkeeper.MainApp
import com.novikovpashka.projectkeeper.R
import com.novikovpashka.projectkeeper.data.model.Project
import com.novikovpashka.projectkeeper.databinding.ActivityAddProjectBinding
import com.novikovpashka.projectkeeper.presentation.mainactivity.MainActivity
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class AddProjectActivity : AppCompatActivity(), IncomingListAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var incomingAdapter: IncomingListAdapter
    private lateinit var viewModel: AddProjectViewModel

    @Inject
    lateinit var factory: AddProjectViewModel.Factory

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        (application as MainApp).appComponent.inject(this)
        viewModel = ViewModelProvider(this, factory)[AddProjectViewModel::class.java]
        setTheme(viewModel.loadThemeIdFromStorage())

        val binding: ActivityAddProjectBinding = ActivityAddProjectBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        recyclerView = binding.recycler
        incomingAdapter = IncomingListAdapter(this)

        initRecycler()

        viewModel.snackbar.observe(this) { s: String? ->
            Snackbar.make(binding.root, s!!, BaseTransientBottomBar.LENGTH_LONG).show()
        }

        binding.addActivityToolbar.setNavigationOnClickListener {
            if (binding.projectNameEditText.text.isNotEmpty()
                || binding.projectPriceEditText.text.isNotEmpty()
                || binding.projectDescriptionEditText.text.isNotEmpty()
                || incomingAdapter.itemCount > 1
            ) {
                MaterialAlertDialogBuilder(this)
                    .setMessage("Discard changes?")
                    .setPositiveButton("Discard") { dialog, which ->
                        finish()
                    }
                    .setNegativeButton("Keep editing") { dialog, which ->
                        dialog.cancel()
                    }
                    .show()
            }
            else finish()
        }

        binding.addActivityToolbar.setOnMenuItemClickListener { item: MenuItem ->
            if (item.itemId == R.id.add_project) {
                val project = viewModel.parseProject(
                    name = binding.projectNameEditText.text.toString(),
                    price = binding.projectPriceEditText.text.toString(),
                    description = binding.projectDescriptionEditText.text.toString())
                if (project != null) {
                    startMainActivity(project)
                    return@setOnMenuItemClickListener true
                }
                return@setOnMenuItemClickListener false
            }
            false
        }

        binding.projectDescriptionEditText.setOnTouchListener { v, _ ->
            if (binding.projectDescriptionEditText.lineCount > 6) {
                v.parent.requestDisallowInterceptTouchEvent(true)
            }
            false
        }
    }

    private fun startMainActivity(project: Project) {
        val intent = Intent(this@AddProjectActivity, MainActivity::class.java)
        intent.putExtra("projectToAdd", project)
        startActivity(intent)
    }

    private fun initRecycler() {
        recyclerView.adapter = incomingAdapter
        recyclerView.setHasFixedSize(false)

        viewModel.incomingsLiveData.observe(this) {
            incomingAdapter.submitList(it.toMutableList())
        }
    }

    override fun addIncoming() {
        viewModel.addIncoming()
    }

    override fun deleteIncoming(position: Int) {
        viewModel.deleteIncoming(position)
    }

    override fun onEditValue(value: String, index: Int) {
        viewModel.saveIncomingValue(value, index)
    }

    override fun onEditDescription(description: String, index: Int) {
        viewModel.saveIncomingDescription(description, index)
    }

    override fun onEditDate(date: Long, index: Int) {
        showDatePicker(date, index)
    }

    private fun showDatePicker(date: Long, index: Int) {
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        val datePickerDialog = DatePickerDialog(
            this,
            mOnDataSetListener(index),
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
        )
        datePickerDialog.show()
    }

    inner class mOnDataSetListener(index: Int) : DatePickerDialog.OnDateSetListener {
        var _index: Int = 0
        init {
            _index = index
        }
        override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
            val calendar: Calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            val simpleDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH)
            val date = simpleDateFormat.format(calendar.timeInMillis)
            viewModel.saveIncomingDateText(date, index = _index)
            incomingAdapter.notifyItemChanged(_index)
        }
    }

}