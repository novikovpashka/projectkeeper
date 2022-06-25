package com.novikovpashka.projectkeeper.presentation.projectactivity

import android.graphics.Insets
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.widget.TextView
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.novikovpashka.projectkeeper.Helpers
import com.novikovpashka.projectkeeper.data.datafirestore.Project
import com.novikovpashka.projectkeeper.databinding.ActivityProjectBinding

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

        toolbar.setNavigationOnClickListener { view: View? -> finish() }
    }

}