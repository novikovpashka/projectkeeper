package com.novikovpashka.projectkeeper.presentation.mainactivity

import androidx.appcompat.app.AppCompatActivity
import com.novikovpashka.projectkeeper.presentation.mainactivity.BottomSortDialog.RadioListener
import com.novikovpashka.projectkeeper.presentation.mainactivity.SettingsFragment.SettingsListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.MaterialToolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import android.widget.TextView
import android.widget.EditText
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import android.os.Build
import androidx.core.view.WindowCompat
import androidx.core.view.ViewCompat
import com.novikovpashka.projectkeeper.R.id
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import com.novikovpashka.projectkeeper.R.anim
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.content.DialogInterface
import android.text.TextWatcher
import android.text.Editable
import androidx.recyclerview.widget.LinearLayoutManager
import com.novikovpashka.projectkeeper.data.dataprojects.Project
import com.novikovpashka.projectkeeper.CurrencyList
import com.google.android.material.snackbar.Snackbar
import android.content.Intent
import android.graphics.Color
import android.os.Parcelable
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import com.novikovpashka.projectkeeper.R.menu
import com.github.javafaker.Faker
import com.novikovpashka.projectkeeper.data.dataprojects.Incoming
import com.novikovpashka.projectkeeper.presentation.addprojectactivity.AddProjectActivity
import com.novikovpashka.projectkeeper.presentation.startactivity.StartActivity
import com.novikovpashka.projectkeeper.presentation.projectactivity.ProjectActivity
import com.novikovpashka.projectkeeper.R
import androidx.core.content.ContextCompat
import com.novikovpashka.projectkeeper.databinding.ActivityMainBinding
import java.lang.Exception
import java.text.DecimalFormat
import java.util.*

class MainActivity : AppCompatActivity(), RadioListener, ProjectListAdapter.OnItemClickListener,
    SettingsListener {
    private lateinit var addButton: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var shimmerProjects: ShimmerFrameLayout
    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var materialToolbar: MaterialToolbar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var projectAdapter: ProjectListAdapter
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var usdrubRate: TextView
    private lateinit var eurrubRate: TextView
    private lateinit var lastupdate: TextView
    private lateinit var searchText: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
        setAccentColor(sharedViewModel.loadAccentColor())
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            binding.appbarlayout.setOnApplyWindowInsetsListener { v: View, insets: WindowInsets ->
                val mInsets = insets.getInsets(WindowInsets.Type.systemBars())
                v.setPadding(0, mInsets.top, 0, 0)
                insets
            }
            binding.addButton.setOnApplyWindowInsetsListener { _: View?, insets: WindowInsets ->
                val mInsets = insets.getInsets(WindowInsets.Type.systemBars())
                val params = CoordinatorLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                params.bottomMargin = mInsets.bottom + dpToPx(16)
                params.rightMargin = dpToPx(16)
                params.gravity = Gravity.END or Gravity.BOTTOM
                binding.addButton.layoutParams = params
                insets
            }
        } else {
            val windowInsetsController = ViewCompat.getWindowInsetsController(window.decorView)
            if (windowInsetsController != null) {
                windowInsetsController.isAppearanceLightStatusBars = false
            }
            window.statusBarColor = Color.BLACK
            window.navigationBarColor = Color.BLACK
        }

        addButton = binding.addButton
        addButton.setOnClickListener { v: View -> showAddPopupMenu(v) }

        coordinatorLayout = binding.coordinator
        materialToolbar = binding.materialToolbar
        drawerLayout = binding.drawerLayout
        shimmerProjects = binding.shimmerProjects
        shimmerProjects.visibility = View.VISIBLE
        navigationView = binding.navigationView
        mAuth = FirebaseAuth.getInstance()
        recyclerView = binding.recyclerView
        projectAdapter = ProjectListAdapter(this)
        searchText = binding.searchEditText

        usdrubRate = navigationView.getHeaderView(0)
            .findViewById(id.usdrub_value)
        eurrubRate = navigationView.getHeaderView(0)
            .findViewById(id.eurrub_value)
        lastupdate = navigationView.getHeaderView(0)
            .findViewById(id.last_updated)

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_UNSPECIFIED) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

        initRecyclerAndObservers()
        setToolbarListeners()

        sharedViewModel.loadRates()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        activateDrawer()
    }

    private fun setToolbarListeners() {
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
                        startStartActivity()
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

    private fun initRecyclerAndObservers() {
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = projectAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        sharedViewModel.projects.observe(this) { projects: List<Project?> ->
            projectAdapter.submitList(
                projects
            )
        }
        sharedViewModel.shimmerActive.observe(this) { aBoolean: Boolean ->
            shimmerProjects.visibility = if (aBoolean) View.VISIBLE else View.GONE
        }
        sharedViewModel.usdrubRate.observe(this) { s: String ->
            val result = s + "₽"
            usdrubRate.text = result
            try {
                projectAdapter.usdRate = s.toDouble()
            } catch (ignored: Exception) {
            }
        }
        sharedViewModel.currency.observe(this) { currency: CurrencyList? ->
            projectAdapter.currency = currency!!
            projectAdapter.notifyDataSetChanged()
        }
        sharedViewModel.eurrubRate.observe(this) { s: String ->
            val result = s + "₽"
            eurrubRate.text = result
            try {
                projectAdapter.eurRate = s.toDouble()
            } catch (ignored: Exception) {
            }
        }
        sharedViewModel.updated.observe(this) { s: String? -> lastupdate.text = s }
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
            if (s != null) {
                Snackbar.make(coordinatorLayout, s, 5000)
                    .setAction("UNDO") { sharedViewModel.restoreDeletedProjects() }
                    .show()
            }
        }
        sharedViewModel.snackbarInfo.observe(this) { s: String? ->
            if (s != null) {
                Snackbar.make(coordinatorLayout, s, 5000).show()
            }
        }
        sharedViewModel.title.observe(this) { s: String? ->
            if (s != null) {
                materialToolbar.title = s
            } else materialToolbar.title = "Projects"
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getParcelableExtra<Parcelable?>("projectToAdd") != null) {
            val project = intent.getParcelableExtra<Project>("projectToAdd")
            sharedViewModel.addProject(project!!)
        } else if (intent.getParcelableExtra<Parcelable?>("projectToRemove") != null) {
            sharedViewModel.deleteProject(intent.getParcelableExtra("projectToRemove")!!)
        } else if (intent.getParcelableExtra<Parcelable?>("projectToUpdate") != null) {
            sharedViewModel.updateProject(intent.getParcelableExtra("projectToUpdate")!!)
        }
    }

    override fun onStop() {
        super.onStop()
        drawerLayout.closeDrawer(Gravity.LEFT, false)
        if (searchText.text.toString().isEmpty()) {
            stopSearchMode()
        }
    }

    private fun showAddPopupMenu(v: View) {
        val popupMenu = PopupMenu(v.context, v)
        popupMenu.menuInflater.inflate(menu.main_add_menu, popupMenu.menu)
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            if (item.itemId == id.add_project) {
                startAddProjectActivity()
                return@setOnMenuItemClickListener true
            } else if (item.itemId == id.add_random_1) {
                addOneRandomProject()
                return@setOnMenuItemClickListener true
            }
            false
        }
    }

    /**Add 1 random */
    private fun addOneRandomProject() {
        val faker = Faker()
        val name = faker.country().capital()
        val description = faker.harryPotter().quote()
        val price =
            DecimalFormat("####").format(((Math.random() * 200000).toInt() / 1000 * 1000).toLong())
                .toDouble()
        val incomings: MutableList<Incoming> = ArrayList()
        for (i in 0..19) {
            val incomingDescription = faker.harryPotter().quote()
            val incomingValue = DecimalFormat("####")
                .format(((Math.random() * price).toInt() / 20 / 1000 * 1000).toLong()).toDouble()
            val incoming = Incoming(
                incomingDescription,
                incomingValue,
                Date().time
            )
            incomings.add(incoming)
        }
        val project = Project(name, price, description, incomings)
        sharedViewModel.addProject(project)
    }

    private fun startAddProjectActivity() {
        val intent = Intent(this, AddProjectActivity::class.java)
        startActivity(intent)
    }

    private fun startStartActivity() {
        val intent = Intent(this, StartActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun startProjectActivity(project: Project) {
        val intent = Intent(this, ProjectActivity::class.java)
        intent.putExtra("Project", project)
        startActivity(intent)
        overridePendingTransition(anim.slide_from_right, anim.slide_to_left_slow)
    }

    override fun applySortClicked(sortParam: SortParam, orderParam: OrderParam) {
        sharedViewModel.sortParamLiveData.value = sortParam
        sharedViewModel.orderParamLiveData.value = orderParam
        sharedViewModel.saveSortAndOrderParamsToStorage()
    }

    override fun onItemClick(project: Project) {
        startProjectActivity(project)
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

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun setSelectedMode() {
        addButton.hide()
        searchText.visibility = View.GONE
        materialToolbar.menu.clear()
        materialToolbar.inflateMenu(menu.topappbar_menu_delete)
        materialToolbar.setNavigationIcon(R.drawable.ic_baseline_close_24)
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
        materialToolbar.menu.clear()
        if (searchText.text.toString().isNotEmpty()) {
            setSearchMode()
        } else {
            materialToolbar.inflateMenu(menu.topappbar_menu)
            materialToolbar.setNavigationIcon(R.drawable.ic_baseline_menu_24)
            materialToolbar.setNavigationOnClickListener {
                drawerLayout.openDrawer(
                    GravityCompat.START
                )
            }
            addButton.show()
        }
    }

    private fun setSearchMode() {
        addButton.hide()
        materialToolbar.menu.clear()
        materialToolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        searchText.visibility = View.VISIBLE
        materialToolbar.setNavigationOnClickListener { stopSearchMode() }
        searchText.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(searchText, 0)
    }

    private fun stopSearchMode() {
        materialToolbar.menu.clear()
        searchText.setText("")
        searchText.visibility = View.GONE
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(
            this@MainActivity.window.decorView.windowToken, 0
        )
        materialToolbar.inflateMenu(menu.topappbar_menu)
        materialToolbar.setNavigationIcon(R.drawable.ic_baseline_menu_24)
        materialToolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(
                GravityCompat.START
            )
        }
        addButton.show()
    }

    private fun setAccentColor(color: Int) {
        when (color) {
            ContextCompat.getColor(this, R.color.myOrange) -> {
                theme.applyStyle(R.style.Theme_Default, true)
            }
            ContextCompat.getColor(this, R.color.myRed) -> {
                theme.applyStyle(R.style.Theme_Default_Red, true)
            }
            ContextCompat.getColor(this, R.color.myGreen) -> {
                theme.applyStyle(R.style.Theme_Default_Green, true)
            }
            ContextCompat.getColor(this, R.color.myPurple) -> {
                theme.applyStyle(R.style.Theme_Default_Purple, true)
            }
            ContextCompat.getColor(this, R.color.myBlue) -> {
                theme.applyStyle(R.style.Theme_Default_Blue, true)
            }
        }
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
}