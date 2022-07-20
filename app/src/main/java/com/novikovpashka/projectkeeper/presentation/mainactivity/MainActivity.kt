package com.novikovpashka.projectkeeper.presentation.mainactivity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.novikovpashka.projectkeeper.CurrencyList
import com.novikovpashka.projectkeeper.MainApp
import com.novikovpashka.projectkeeper.R.anim
import com.novikovpashka.projectkeeper.R.id
import com.novikovpashka.projectkeeper.data.model.Project
import com.novikovpashka.projectkeeper.databinding.ActivityMainBinding
import com.novikovpashka.projectkeeper.extensions.logOutDialog
import com.novikovpashka.projectkeeper.extensions.startProjectActivity
import com.novikovpashka.projectkeeper.extensions.startSettingsFragment
import com.novikovpashka.projectkeeper.presentation.addprojectactivity.AddProjectActivity
import com.novikovpashka.projectkeeper.presentation.base.MyFloatingButton
import com.novikovpashka.projectkeeper.presentation.base.MyToolbar
import javax.inject.Inject


class MainActivity : AppCompatActivity(), ProjectListAdapter.OnItemClickListener {
    private lateinit var addButton: MyFloatingButton
    private lateinit var shimmerProjects: ShimmerFrameLayout
    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var materialToolbar: MyToolbar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var projectAdapter: ProjectListAdapter
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var usdrubRate: TextView
    private lateinit var eurrubRate: TextView
    private lateinit var lastupdate: TextView
    private lateinit var searchText: EditText

    @Inject
    lateinit var factory: SharedViewModel.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as MainApp).appComponent.inject(this)
        sharedViewModel = ViewModelProvider(this, factory)[SharedViewModel::class.java]
        setTheme(sharedViewModel.loadThemeIdFromStorage())

        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        addButton = binding.addButton
        coordinatorLayout = binding.coordinator
        materialToolbar = binding.materialToolbar
        searchText = materialToolbar.searchText
        searchText.setText(sharedViewModel.searchTextLiveData.value)
        drawerLayout = binding.drawerLayout
        shimmerProjects = binding.shimmerProjects
        navigationView = binding.navigationView

        usdrubRate = navigationView.getHeaderView(0)
            .findViewById(id.usdrub_value)
        eurrubRate = navigationView.getHeaderView(0)
            .findViewById(id.eurrub_value)
        lastupdate = navigationView.getHeaderView(0)
            .findViewById(id.last_updated)

        val recyclerView = binding.recyclerView
        projectAdapter = ProjectListAdapter(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = projectAdapter

        setClickListeners()
        initViewModelObservers()

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.getParcelableExtra<Project>("projectToAdd")?.let {
            sharedViewModel.addProject(it)
        }
        intent.getParcelableExtra<Project>("projectToRemove")?.let {
            sharedViewModel.deleteProject(it)
        }
        intent.getParcelableExtra<Project>("projectToUpdate")?.let {
            sharedViewModel.updateProject(it)
        }
    }

    override fun onBackPressed() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        super.onBackPressed()
    }

    override fun onProjectClick(project: Project) {
        startProjectActivity(project)
    }

    override fun addProjectToDelete(project: Project, position: Int) {
        sharedViewModel.addProjectToDelete(project, position)
    }

    override fun removeProjectToDelete(project: Project, position: Int) {
        sharedViewModel.removeProjectToDelete(project, position)
    }

    private fun setClickListeners() {
        addButton.setOnClickListener {
            val intent = Intent(this, AddProjectActivity::class.java)
            startActivity(intent)
            overridePendingTransition(anim.slide_from_right, anim.slide_to_left_slow)
        }

        materialToolbar.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                id.sort ->
                    BottomSortDialog().show(supportFragmentManager, "filter_dialog")
                id.delete ->
                    sharedViewModel.deleteSelectedProjects()
                id.search ->
                    sharedViewModel.toolbarMode.value = SharedViewModel.ToolbarMode.SEARCH
                id.clear_text ->
                    searchText.setText("")
            }
            false
        }

        materialToolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navigationView.setNavigationItemSelectedListener { item: MenuItem ->
            if (item.itemId == id.settings) {
                drawerLayout.closeDrawer(Gravity.LEFT, true)
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                startSettingsFragment().commit()
                return@setNavigationItemSelectedListener true
            } else if (item.itemId == id.logout) {
                MaterialAlertDialogBuilder(this).logOutDialog().show()
            } else if (item.itemId == id.add_random_5) {
                sharedViewModel.addFiveRandomProject()
            }
            false
        }

        searchText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                sharedViewModel.searchTextLiveData.value =
                    editable.toString().trim { it <= ' ' }.lowercase()
            }
        })
    }

    private fun initViewModelObservers() {

        sharedViewModel.projects.observe(this) { projects: List<Project> ->
            projectAdapter.submitList(projects)
        }

        sharedViewModel.shimmerActive.observe(this) { aBoolean: Boolean ->
            shimmerProjects.visibility = if (aBoolean) View.VISIBLE else View.GONE
        }

        sharedViewModel.currencyLiveData.observe(this) { currency: CurrencyList? ->
            projectAdapter.currency = currency!!
            projectAdapter.notifyDataSetChanged()
        }

        sharedViewModel.usdrubRate.observe(this) { s: String ->
            try {
                projectAdapter.usdRate = s.toDouble()
                usdrubRate.text = s + "₽"
            } catch (ignored: Exception) {
                usdrubRate.text = "No data"
            }
        }

        sharedViewModel.eurrubRate.observe(this) { s: String ->
            try {
                projectAdapter.eurRate = s.toDouble()
                eurrubRate.text = s + "₽"
            } catch (ignored: Exception) {
                eurrubRate.text = "No data"
            }
        }

        sharedViewModel.ratesUpdatedDate.observe(this) { lastupdate.text = it }

        sharedViewModel.selectMode.observe(this) {
            val prevValue = projectAdapter.selectMode

            projectAdapter.selectMode = it

            if (it) {
                sharedViewModel.toolbarMode.value = SharedViewModel.ToolbarMode.SELECT
            } else {
                if (prevValue) {
                    if (sharedViewModel.searchTextLiveData.value!!.isNotEmpty()) {
                        sharedViewModel.toolbarMode.value = SharedViewModel.ToolbarMode.SEARCH
                    } else sharedViewModel.toolbarMode.value = SharedViewModel.ToolbarMode.DEFAULT
                }
            }
        }

        sharedViewModel.projectsToDelete.observe(this) {
            projectAdapter.selectedProject = it
        }

        sharedViewModel.projectsIdToDelete.observe(this) {
            projectAdapter.selectedId = it
        }

        sharedViewModel.snackbarWithAction.observe(this) {
            it?.let {
                val snackbar = Snackbar.make(coordinatorLayout, it, 5000)
                    .setAction("UNDO") { sharedViewModel.restoreDeletedProjects() }
                snackbar.animationMode = BaseTransientBottomBar.ANIMATION_MODE_SLIDE
                snackbar.show()
            }
        }

        sharedViewModel.snackbarInfo.observe(this) {
            it?.let {
                val snackbar = Snackbar.make(coordinatorLayout, it, 5000)
                snackbar.animationMode = BaseTransientBottomBar.ANIMATION_MODE_SLIDE
                snackbar.show()
            }
        }

        sharedViewModel.title.observe(this) {
            materialToolbar.title = it ?: "Projects"
        }

        sharedViewModel.toolbarMode.observe(this) {
            when (it) {
                SharedViewModel.ToolbarMode.SEARCH -> setSearchMode()
                SharedViewModel.ToolbarMode.SELECT -> setSelectedMode()
                else -> setDefaultMode()
            }
        }
    }

    private fun setSelectedMode() {
        addButton.hide()
        materialToolbar.setSelectMode()
        materialToolbar.setNavigationOnClickListener {
            for (x in sharedViewModel.clearSelectedProjects()) {
                projectAdapter.notifyItemChanged(x)
            }
            addButton.show()
        }
    }

    private fun setSearchMode() {
        addButton.hide()
        materialToolbar.setSearchMode()
        materialToolbar.setNavigationOnClickListener {
            sharedViewModel.toolbarMode.value = SharedViewModel.ToolbarMode.DEFAULT
        }
    }

    private fun setDefaultMode() {
        for (x in sharedViewModel.clearSelectedProjects()) {
            projectAdapter.notifyItemChanged(x)
        }
        materialToolbar.setDefaultMode()
        materialToolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        addButton.show()
    }

}