package com.novikovpashka.projectkeeper.presentation.projectactivity

import android.content.Intent
import android.graphics.Insets
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.novikovpashka.projectkeeper.Helpers
import com.novikovpashka.projectkeeper.R
import com.novikovpashka.projectkeeper.data.datafirestore.Project
import com.novikovpashka.projectkeeper.databinding.ActivityProjectBinding
import com.novikovpashka.projectkeeper.presentation.editprojectactivity.EditProjectActivity
import com.novikovpashka.projectkeeper.presentation.mainactivity.MainActivity

class ProjectActivity : AppCompatActivity() {

    private lateinit var projectName: TextView
    private lateinit var projectPrice: TextView
    private lateinit var projectDescription: TextView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: IncomingListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityProjectBinding = ActivityProjectBinding.inflate(layoutInflater)
        setContentView(binding.root)
        projectName = binding.projectName
        projectPrice = binding.projectPrice
        projectDescription = binding.projectDescription
        toolbar = binding.toolbar
        appBarLayout = binding.appbarlayout
        recyclerView = binding.recycler
        adapter = IncomingListAdapter()
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            binding.appbarlayout.setOnApplyWindowInsetsListener { v, insets ->
                val mInsets: Insets = insets.getInsets(WindowInsets.Type.systemBars())
                v.setPadding(0, mInsets.top, 0, 0)
                insets
            }
        }

        val project: Project = intent.getParcelableExtra("Project")!!
        projectName.text = project.name
        projectPrice.text = Helpers.convertPriceProject(project.price)
        projectDescription.text = project.description
        adapter.submitList(project.incomings)

        toolbar.setNavigationOnClickListener { view: View? ->
            finish()
            overridePendingTransition(0, R.anim.slide_to_right)
        }

        toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.remove_project) {
                MaterialAlertDialogBuilder(this)
                    .setMessage("Remove ${project.name}?")
                    .setPositiveButton("Remove") { dialog, which ->
                        startMainActivityAndRemoveProject(project)
                    }
                    .setNegativeButton("Cancel") { dialog, which ->
                        dialog.cancel()
                    }
                    .show()
                return@setOnMenuItemClickListener true
            }
            else if (it.itemId == R.id.edit_project) {
                startEditProjectActivity(project)
                return@setOnMenuItemClickListener true
            }
            return@setOnMenuItemClickListener false
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0, R.anim.slide_to_right)
    }

    private fun startMainActivityAndRemoveProject(project: Project) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("projectToRemove", project)
        startActivity(intent)
        overridePendingTransition(0, R.anim.slide_to_right)
    }

    private fun startEditProjectActivity (project: Project) {
        val intent = Intent(this, EditProjectActivity::class.java)
        intent.putExtra("projectToEdit", project)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left_slow)
    }

}