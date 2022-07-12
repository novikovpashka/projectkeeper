package com.novikovpashka.projectkeeper.presentation.mainactivity

import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.novikovpashka.projectkeeper.CurrencyList
import com.novikovpashka.projectkeeper.MainApp
import com.novikovpashka.projectkeeper.R
import com.novikovpashka.projectkeeper.R.*
import com.novikovpashka.projectkeeper.data.model.Project
import com.novikovpashka.projectkeeper.databinding.ActivityMainBinding
import com.novikovpashka.projectkeeper.extensions.setInsets
import com.novikovpashka.projectkeeper.presentation.addprojectactivity.AddProjectActivity
import com.novikovpashka.projectkeeper.presentation.base.MyToolbar
import com.novikovpashka.projectkeeper.presentation.mainactivity.BottomSortDialog.RadioListener
import com.novikovpashka.projectkeeper.presentation.mainactivity.SettingsFragment.SettingsListener
import com.novikovpashka.projectkeeper.presentation.projectactivity.ProjectActivity
import com.novikovpashka.projectkeeper.presentation.startactivity.StartActivity
import javax.inject.Inject

class MainActivity : AppCompatActivity(), RadioListener, ProjectListAdapter.OnItemClickListener,
    SettingsListener {
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var addButton: FloatingActionButton
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

    @Inject
    lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as MainApp).appComponent.inject(this)
        sharedViewModel = ViewModelProvider(this, factory)[SharedViewModel::class.java]
        setAccentColorAndNightMode(sharedViewModel.loadAccentColor())

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
        mAuth = FirebaseAuth.getInstance()
        recyclerView = binding.recyclerView
        projectAdapter = ProjectListAdapter(this)
        searchText = materialToolbar.searchText

        usdrubRate = navigationView.getHeaderView(0)
            .findViewById(id.usdrub_value)
        eurrubRate = navigationView.getHeaderView(0)
            .findViewById(id.eurrub_value)
        lastupdate = navigationView.getHeaderView(0)
            .findViewById(id.last_updated)

        setInsets()
        setClickListeners()
        initViewModelObservers()

        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = projectAdapter
    }

    override fun onStop() {
        super.onStop()
        drawerLayout.closeDrawer(Gravity.LEFT, false)
        if (searchText.text.toString().isEmpty()) {
            stopSearchMode()
        }
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
        super.onBackPressed()
        activateDrawer()
    }

    override fun applySortClicked(sortParam: SortParam, orderParam: OrderParam) {
        sharedViewModel.sortParamLiveData.value = sortParam
        sharedViewModel.orderParamLiveData.value = orderParam
        sharedViewModel.saveSortAndOrderParamsToStorage()
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

    override fun activateDrawer() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }

    override fun recreateActivity() {
        recreate()
    }

    override fun currencyChanged(currency: CurrencyList) {
        projectAdapter.currency = currency
        projectAdapter.notifyDataSetChanged()
    }

    private fun setClickListeners() {

        addButton.setOnClickListener { v: View -> showAddPopupMenu(v) }

        materialToolbar.setOnMenuItemClickListener { item: MenuItem ->
            if (item.itemId == id.sort) {
                val bottomSortDialog = BottomSortDialog()
                val bundle = Bundle()
                bundle.putSerializable(
                    "currentSortParam",
                    sharedViewModel.sortParamLiveData.value
                )
                bundle.putSerializable(
                    "currentOrderParam",
                    sharedViewModel.orderParamLiveData.value
                )
                bottomSortDialog.arguments = bundle
                bottomSortDialog.show(supportFragmentManager, "filter_dialog")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                }
            } else if (item.itemId == id.delete) {
                sharedViewModel.deleteSelectedProjects()
            } else if (item.itemId == id.search) {
                setSearchMode()
            } else if (item.itemId == id.clear_text) {
                searchText.setText("")
            }
            false
        }

        materialToolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(
                GravityCompat.START
            )
        }

        navigationView.setNavigationItemSelectedListener { item: MenuItem ->
            if (item.itemId == id.settings) {
                supportFragmentManager.beginTransaction()
                    .setReorderingAllowed(true)
                    .setCustomAnimations(
                        anim.slide_from_right_settings,
                        anim.slide_to_left,
                        anim.slide_to_right,
                        anim.slide_to_right
                    )
                    .add(id.fragmentContainer, SettingsFragment::class.java, null)
                    .addToBackStack(null)
                    .commit()
                drawerLayout.closeDrawer(Gravity.LEFT, true)
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                return@setNavigationItemSelectedListener true
            } else if (item.itemId == id.logout) {
                MaterialAlertDialogBuilder(this)
                    .setMessage("Log out?")
                    .setNegativeButton("Stay") { dialog: DialogInterface, _: Int -> dialog.cancel() }
                    .setPositiveButton("Log out") { _: DialogInterface?, _: Int ->
                        mAuth.signOut()
                        val intent = Intent(this, StartActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                    .show()
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

        sharedViewModel.currency.observe(this) { currency: CurrencyList? ->
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
                setSelectedMode()
            } else {
                if (prevValue) stopSelectMode()
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

        sharedViewModel.snackbarWithAction.observe(this) { s: String? ->
            s?.let {
                Snackbar.make(coordinatorLayout, it, 5000)
                    .setAction("UNDO") { sharedViewModel.restoreDeletedProjects() }
                    .show()
            }
        }

        sharedViewModel.snackbarInfo.observe(this) { s: String? ->
            s?.let {
                Snackbar.make(coordinatorLayout, it, 5000).show()
            }
        }

        sharedViewModel.title.observe(this) { s: String? ->
            if (s != null) {
                materialToolbar.title = s
            } else materialToolbar.title = "Projects"
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

    private fun stopSelectMode() {
        for (x in sharedViewModel.clearSelectedProjects()) {
            projectAdapter.notifyItemChanged(x)
        }
        if (searchText.text.toString().isNotEmpty()) {
            setSearchMode()
        } else {
            materialToolbar.setDefaultMode()
            materialToolbar.setNavigationOnClickListener {
                drawerLayout.openDrawer(GravityCompat.START)
            }
            addButton.show()
        }
    }

    private fun setSearchMode() {
        addButton.hide()
        materialToolbar.setSearchMode()
        materialToolbar.setNavigationOnClickListener { stopSearchMode() }
    }

    private fun stopSearchMode() {
        materialToolbar.setDefaultMode()
        materialToolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        addButton.show()
    }

    private fun setAccentColorAndNightMode(color: Int) {

        when (color) {
            R.color.myOrange -> {
                this.theme.applyStyle(style.Theme_Default, true)
            }
            R.color.myRed -> {
                this.theme.applyStyle(style.Theme_Default_Red, true)
            }
            R.color.myGreen -> {
                this.theme.applyStyle(style.Theme_Default_Green, true)
            }
            R.color.myPurple -> {
                this.theme.applyStyle(style.Theme_Default_Purple, true)
            }
            R.color.myBlue -> {
                this.theme.applyStyle(style.Theme_Default_Blue, true)
            }
        }

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_UNSPECIFIED) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun setInsets() {
        appBarLayout.setInsets()
        addButton.setInsets()
    }
}