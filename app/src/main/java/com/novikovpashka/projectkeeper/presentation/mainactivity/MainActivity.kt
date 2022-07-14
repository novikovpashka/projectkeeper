package com.novikovpashka.projectkeeper.presentation.mainactivity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.novikovpashka.projectkeeper.CurrencyList
import com.novikovpashka.projectkeeper.MainApp
import com.novikovpashka.projectkeeper.R.*
import com.novikovpashka.projectkeeper.data.model.Project
import com.novikovpashka.projectkeeper.databinding.ActivityMainBinding
import com.novikovpashka.projectkeeper.extensions.logOutDialog
import com.novikovpashka.projectkeeper.extensions.startSettingsFragment
import com.novikovpashka.projectkeeper.presentation.addprojectactivity.AddProjectActivity
import com.novikovpashka.projectkeeper.presentation.base.MyAppBarLayout
import com.novikovpashka.projectkeeper.presentation.base.MyFloatingButton
import com.novikovpashka.projectkeeper.presentation.base.MyToolbar
import com.novikovpashka.projectkeeper.presentation.projectactivity.ProjectActivity
import javax.inject.Inject

class MainActivity : AppCompatActivity(), ProjectListAdapter.OnItemClickListener {
    private lateinit var appBarLayout: MyAppBarLayout
    private lateinit var addButton: MyFloatingButton
    private lateinit var recyclerView: RecyclerView
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

        appBarLayout = binding.appbarlayout
        addButton = binding.addButton
        coordinatorLayout = binding.coordinator
        materialToolbar = binding.materialToolbar
        drawerLayout = binding.drawerLayout
        shimmerProjects = binding.shimmerProjects
        navigationView = binding.navigationView
        recyclerView = binding.recyclerView
        projectAdapter = ProjectListAdapter(this)

        searchText = materialToolbar.searchText
        searchText.setText(sharedViewModel.searchTextLiveData.value)

        usdrubRate = navigationView.getHeaderView(0)
            .findViewById(id.usdrub_value)
        eurrubRate = navigationView.getHeaderView(0)
            .findViewById(id.eurrub_value)
        lastupdate = navigationView.getHeaderView(0)
            .findViewById(id.last_updated)

        setClickListeners()
        initViewModelObservers()

        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = projectAdapter
    }

    override fun onStop() {
        super.onStop()
        drawerLayout.closeDrawer(Gravity.LEFT, false)
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

    override fun onItemClick(project: Project) {
        val intent = Intent(this, ProjectActivity::class.java)
        intent.putExtra("Project", project)
        startActivity(intent)
        overridePendingTransition(anim.slide_from_right, anim.slide_to_left_slow)
    }

    override fun showActionMenu() {
        sharedViewModel.selectMode.value = true
    }

    override fun closeActionMenu() {
        sharedViewModel.selectMode.value = false
        sharedViewModel.clearSelectedProjects()
    }

    override fun addProjectToDelete(project: Project, position: Int) {
        sharedViewModel.addProjectToDelete(project, position)
    }

    override fun removeProjectToDelete(project: Project, position: Int) {
        sharedViewModel.removeProjectToDelete(project, position)
    }

    private fun setClickListeners() {

        addButton.setOnClickListener { v: View -> showAddPopupMenu(v) }

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
                startSettingsFragment().commit()
                drawerLayout.closeDrawer(Gravity.LEFT, true)
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                return@setNavigationItemSelectedListener true
            } else if (item.itemId == id.logout) {
                MaterialAlertDialogBuilder(this).logOutDialog().show()
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

        sharedViewModel.ratesUpdatedDate.observe(this) { s: String? -> lastupdate.text = s }

        sharedViewModel.selectMode.observe(this) { aBoolean: Boolean ->
            val prevValue = projectAdapter.selectMode
            projectAdapter.selectMode = aBoolean
            if (aBoolean) {
                sharedViewModel.toolbarMode.value = SharedViewModel.ToolbarMode.SELECT
            } else {
                if (prevValue) {
                    if (sharedViewModel.searchTextLiveData.value!!.isNotEmpty()) {
                        sharedViewModel.toolbarMode.value = SharedViewModel.ToolbarMode.SEARCH
                    } else sharedViewModel.toolbarMode.value = SharedViewModel.ToolbarMode.DEFAULT
                }
            }
        }

        sharedViewModel.projectsToDelete.observe(this) { projects: List<Project> ->
            projectAdapter.selectedProject.clear()
            if (projects.isNotEmpty()) {
                projectAdapter.selectedProject.addAll(projects)
            }
        }

        sharedViewModel.projectsIdToDelete.observe(this) { integers: List<Int> ->
            projectAdapter.selectedId.clear()
            if (integers.isNotEmpty()) {
                projectAdapter.selectedId.addAll(integers)
            }
        }

        sharedViewModel.snackbarWithAction.observe(this) {
            it?.let {
                Snackbar.make(coordinatorLayout, it, 5000)
                    .setAction("UNDO") { sharedViewModel.restoreDeletedProjects() }
                    .show()
            }
        }

        sharedViewModel.snackbarInfo.observe(this) {
            it?.let {
                Snackbar.make(coordinatorLayout, it, 5000).show()
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

    private fun showAddPopupMenu(v: View) {
        val popupMenu = PopupMenu(v.context, v)
        popupMenu.menuInflater.inflate(menu.main_add_menu, popupMenu.menu)
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            if (item.itemId == id.add_project) {
                val intent = Intent(this, AddProjectActivity::class.java)
                startActivity(intent)
                return@setOnMenuItemClickListener true
            } else if (item.itemId == id.add_random_1) {
                sharedViewModel.addRandomProject()
                return@setOnMenuItemClickListener true
            }
            false
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