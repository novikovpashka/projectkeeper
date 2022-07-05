package com.novikovpashka.projectkeeper.presentation.mainactivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.github.javafaker.Faker;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.novikovpashka.projectkeeper.CurrencyList;
import com.novikovpashka.projectkeeper.R;
import com.novikovpashka.projectkeeper.R.anim;
import com.novikovpashka.projectkeeper.R.id;
import com.novikovpashka.projectkeeper.R.menu;
import com.novikovpashka.projectkeeper.data.dataprojects.Incoming;
import com.novikovpashka.projectkeeper.data.dataprojects.Project;
import com.novikovpashka.projectkeeper.databinding.ActivityMainBinding;
import com.novikovpashka.projectkeeper.presentation.addprojectactivity.AddProjectActivity;
import com.novikovpashka.projectkeeper.presentation.projectactivity.ProjectActivity;
import com.novikovpashka.projectkeeper.presentation.mainactivity.BottomSortDialog.RadioListener;
import com.novikovpashka.projectkeeper.presentation.mainactivity.SharedViewModel.OrderParam;
import com.novikovpashka.projectkeeper.presentation.mainactivity.SharedViewModel.SortParam;
import com.novikovpashka.projectkeeper.presentation.mainactivity.ProjectListAdapter.OnItemClickListener;
import com.novikovpashka.projectkeeper.presentation.startactivity.StartActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements RadioListener, OnItemClickListener, SettingsFragment.SettingsListener {
    private FloatingActionButton addButton;
    private RecyclerView recyclerView;
    private ShimmerFrameLayout shimmerProjects;
    private CoordinatorLayout coordinatorLayout;
    private MaterialToolbar materialToolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private AppBarLayout appBarLayout;

    private FirebaseAuth mAuth;
    private ProjectListAdapter projectAdapter;
    private SharedViewModel sharedViewModel;

    private TextView usdrub;
    private TextView eurrub;
    private TextView lastupdate;

    private EditText searchText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("mytag", "main oncreate");
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        setAccentColor(sharedViewModel.loadAccentColor());


        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        appBarLayout = binding.appbarlayout;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
            binding.appbarlayout.setOnApplyWindowInsetsListener((v, insets) -> {
                Insets mInsets = insets.getInsets(WindowInsets.Type.systemBars());
                v.setPadding(0, mInsets.top, 0, 0);
                return insets;
            });

            binding.addButton.setOnApplyWindowInsetsListener((v, insets) -> {
                Insets mInsets = insets.getInsets(WindowInsets.Type.systemBars());
                CoordinatorLayout.LayoutParams params =
                        new CoordinatorLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );
                params.bottomMargin = mInsets.bottom + dpToPx(16);
                params.rightMargin = dpToPx(16);
                params.gravity = Gravity.END | Gravity.BOTTOM;
                binding.addButton.setLayoutParams(params);
                return insets;
            });
        }
        else {
            WindowInsetsControllerCompat windowInsetsController =
                    ViewCompat.getWindowInsetsController(getWindow().getDecorView());
            windowInsetsController.setAppearanceLightStatusBars(false);
            getWindow().setStatusBarColor(Color.BLACK);
            getWindow().setNavigationBarColor(Color.BLACK);
        }

//        searchText = binding.searchMain.searchInput;
//        sortButton = binding.searchMain.filter;
//        cancelSearch = binding.searchMain.cancelSearch;
        addButton = binding.addButton;
        coordinatorLayout = binding.coordinator;
        materialToolbar = binding.materialToolbar;
        drawerLayout = binding.drawerLayout;
        shimmerProjects = binding.shimmerProjects;
        shimmerProjects.setVisibility(View.VISIBLE);
//        linearLayout = binding.searchMain.searchBar;
        navigationView = binding.navigationView;
        mAuth = FirebaseAuth.getInstance();
        recyclerView = binding.recyclerView;
        projectAdapter = new ProjectListAdapter(this);
        searchText = binding.searchEditText;

        usdrub = navigationView.getHeaderView(0)
                .findViewById(id.usdrub_value);
        eurrub = navigationView.getHeaderView(0)
                .findViewById(id.eurrub_value);
        lastupdate = navigationView.getHeaderView(0)
                .findViewById(id.last_updated);

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_UNSPECIFIED) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }

        initRecyclerAndObservers();
        setToolbarListeners();

        addButton.setOnClickListener(this::showAddPopupMenu);
        sharedViewModel.loadRateUSDRUB();
        sharedViewModel.loadRateEURRUB();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        activateDrawer();
    }


    private void setToolbarListeners() {
        materialToolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == id.sort) {
                BottomSortDialog bottomSortDialog = new BottomSortDialog();
                Bundle bundle = new Bundle();
                bundle.putSerializable("currentSortParam", sharedViewModel.
                        getSortParamLiveData().getValue());
                bundle.putSerializable("currentOrderParam", sharedViewModel.
                        getOrderParamLiveData().getValue());
                bottomSortDialog.setArguments(bundle);
                bottomSortDialog.show(getSupportFragmentManager(), "filter_dialog");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
                }
            }
            else if (item.getItemId() == id.delete) {
                sharedViewModel.deleteSelectedProjects();
            }
            else if (item.getItemId() == id.search) {
                setSearchMode();
            }
            return false;
        });

        materialToolbar.setNavigationOnClickListener(view ->
                drawerLayout.openDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == id.settings) {

                getSupportFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .setCustomAnimations(
                                anim.slide_from_right_settings,
                                anim.slide_to_left,
                                anim.slide_to_right,
                                anim.slide_to_right)
                        .add(id.fragmentContainer, SettingsFragment.class, null)
                        .addToBackStack(null)
                        .commit();
                drawerLayout.closeDrawer(Gravity.LEFT, true);
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                return true;
            }
            else if (item.getItemId() == id.logout) {
                new MaterialAlertDialogBuilder(this)
                        .setMessage("Log out?")
                        .setNegativeButton("Stay", (dialog, which) -> dialog.cancel())
                        .setPositiveButton("Log out", (dialog, which) -> {
                            mAuth.signOut();
                            startStartActivity();
                        })
                        .show();
            }

            return false;
        });

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                sharedViewModel.getSearchTextLiveData().setValue(editable.toString().trim().
                        toLowerCase(Locale.ROOT));
            }
        });

//        cancelSearch.setOnClickListener(view -> {
//            searchText.setText("");
//            searchText.clearFocus();
//            sortExpand();
//        });
    }



    private void initRecyclerAndObservers() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(projectAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        sharedViewModel.getProjects().observe(this, projects ->
                projectAdapter.submitList(projects));

        sharedViewModel.getShimmerActive().observe(this, aBoolean ->
                shimmerProjects.setVisibility(aBoolean ? View.VISIBLE : View.GONE ));

        sharedViewModel.getUSDRUB().observe(this, s -> {
            String result = s + "₽";
            usdrub.setText(result);
            try {
                projectAdapter.setUsdRate(Double.parseDouble(s));
            }
            catch (Exception ignored) {

            }
        });

        sharedViewModel.getCurrency().observe(this, currency -> {
            projectAdapter.setCurrency(currency);
            projectAdapter.notifyDataSetChanged();
        });

        sharedViewModel.getEURRUB().observe(this, s -> {
            String result = s + "₽";
            eurrub.setText(result);
            try {
                projectAdapter.setEurRate(Double.parseDouble(s));
            }
            catch (Exception ignored) {

            }
        });

        sharedViewModel.getUpdated().observe(this, s -> lastupdate.setText(s));

        sharedViewModel.getSelectMode().observe(this, aBoolean -> {
            boolean prevValue = projectAdapter.getSelectMode();
            projectAdapter.setSelectMode(aBoolean);
            if(aBoolean) {
                setSelectedMode();
            }
            else {
                if (prevValue) stopSelectMode();
            }
        });

        sharedViewModel.getProjectsToDelete().observe(this, projects -> {
            projectAdapter.getSelectedProject().clear();
            if (!projects.isEmpty()) {
                projectAdapter.getSelectedProject().addAll(projects);
            }
        });

        sharedViewModel.getProjectsIdToDelete().observe(this, integers -> {
            projectAdapter.getSelectedId().clear();
            if (!integers.isEmpty()) {
                projectAdapter.getSelectedId().addAll(integers);
            }
        });

        sharedViewModel.getSnackbar().observe(this, s ->  {
            if (s != null) {
                Snackbar.make(coordinatorLayout, s, 5000).
                        setAction("UNDO", view -> sharedViewModel.
                                restoreDeletedProjects()).show();
            }
        });

        sharedViewModel.getTitle().observe(this, s ->  {
            if (s != null) {
                materialToolbar.setTitle(s);
            }
            else materialToolbar.setTitle("Projects");
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getParcelableExtra("projectToAdd") != null) {
            Project project = intent.getParcelableExtra("projectToAdd");
            sharedViewModel.addProject(project);
        }

        else if (intent.getParcelableExtra("projectToRemove") != null) {
            sharedViewModel.deleteProject(intent.getParcelableExtra("projectToRemove"));
        }

        else if (intent.getParcelableExtra("projectToUpdate") != null) {
            sharedViewModel.updateProject(intent.getParcelableExtra("projectToUpdate"));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        drawerLayout.closeDrawer(Gravity.LEFT, false);
        if (searchText.getText().toString().isEmpty()) {
            stopSearchMode();
        }
    }

    public void showAddPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
        popupMenu.getMenuInflater().inflate(menu.main_add_menu, popupMenu.getMenu());
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == id.add_project) {
                startAddProjectActivity();
                return true;
            }
            else if (item.getItemId() == id.add_random_1) {
                addOneRandomProject();
                return true;
            }
            return false;
        });
    }

    /**Add 1 random**/
    private void addOneRandomProject() {
        Faker faker = new Faker();
        String name = faker.country().capital();
        String description = faker.harryPotter().quote();
        double price = Double.parseDouble(new DecimalFormat("####").
                format((int) (Math.random() * 200000)/1000*1000));

        List<Incoming> incomings = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            String incomingDescription = faker.harryPotter().quote();
            double incomingValue = Double.parseDouble(new DecimalFormat("####")
                    .format((int) (Math.random() * price)/20/1000*1000));
            Incoming incoming = new Incoming(
                    incomingDescription,
                    incomingValue,
                    new Date().getTime()
            );
            incomings.add(incoming);
        }

        Project project = new Project(name, price, description, incomings);
        sharedViewModel.addProject(project);
    }

    private void startAddProjectActivity() {
        Intent intent = new Intent(this, AddProjectActivity.class);
        startActivity(intent);
    }

    private void startStartActivity() {
        Intent intent = new Intent(this, StartActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void startProjectActivity(Project project) {
        Intent intent = new Intent(this, ProjectActivity.class);
        intent.putExtra("Project", project);
        startActivity(intent);
        overridePendingTransition(anim.slide_from_right, anim.slide_to_left_slow);
    }

    @Override
    public void applySortClicked(SortParam sortParam, OrderParam orderParam) {
        sharedViewModel.getSortParamLiveData().setValue(sortParam);
        sharedViewModel.getOrderParamLiveData().setValue(orderParam);
    }

    @Override
    public void onItemClick(@NonNull Project project) {
        startProjectActivity(project);
    }

    @Override
    public void showActionMenu() {
        sharedViewModel.getSelectMode().setValue(true);
    }

    @Override
    public void closeActionMenu() {
        sharedViewModel.getSelectMode().setValue(false);
        sharedViewModel.clearSelectedProjects();
    }

    @Override
    public void addProjectToDelete(@NonNull Project project, int position) {
        sharedViewModel.addProjectToDelete(project, position);
    }

    @Override
    public void removeProjectToDelete(@NonNull Project project, int position) {
        sharedViewModel.removeProjectToDelete(project, position);
    }


    public int dpToPx(int dp){
        return (int)(dp * getResources().getDisplayMetrics().density);
    }

    public void setSelectedMode() {
        addButton.hide();
        searchText.setVisibility(View.GONE);
        materialToolbar.getMenu().clear();
        materialToolbar.inflateMenu(menu.topappbar_menu_delete);
        materialToolbar.setNavigationIcon(R.drawable.ic_baseline_close_24);
        materialToolbar.setNavigationOnClickListener(v -> {
            for (int x : sharedViewModel.clearSelectedProjects()) {
                projectAdapter.notifyItemChanged(x);
            }
            addButton.show();
        });
    }

    public void stopSelectMode() {
        for (int x : sharedViewModel.clearSelectedProjects()) {
            projectAdapter.notifyItemChanged(x);
        }
        materialToolbar.getMenu().clear();
        if (!searchText.getText().toString().isEmpty()) {
            setSearchMode();
        }
        else {
            materialToolbar.inflateMenu(menu.topappbar_menu);
            materialToolbar.setNavigationIcon(R.drawable.ic_baseline_menu_24);
            materialToolbar.setNavigationOnClickListener(view ->
                    drawerLayout.openDrawer(GravityCompat.START));
            addButton.show();
        }
    }

    public void setSearchMode() {
        addButton.hide();
        materialToolbar.getMenu().clear();
        materialToolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        searchText.setVisibility(View.VISIBLE);
        materialToolbar.setNavigationOnClickListener(v -> stopSearchMode());
        searchText.requestFocus();
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchText, 0);
    }

    public void stopSearchMode() {
        materialToolbar.getMenu().clear();
        searchText.setText("");
        searchText.setVisibility(View.GONE);
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(
                MainActivity.this.getWindow().getDecorView().getWindowToken(),0
        );
        materialToolbar.inflateMenu(menu.topappbar_menu);
        materialToolbar.setNavigationIcon(R.drawable.ic_baseline_menu_24);
        materialToolbar.setNavigationOnClickListener(view ->
                drawerLayout.openDrawer(GravityCompat.START));
        addButton.show();
    }

    public void setAccentColor(int color) {
        if (color == ContextCompat.getColor(this, R.color.myOrange)) {
            getTheme().applyStyle(R.style.Theme_Default, true);
        } else if (color == ContextCompat.getColor(this, R.color.myRed)) {
            getTheme().applyStyle(R.style.Theme_Default_Red, true);
        } else if (color == ContextCompat.getColor(this, R.color.myGreen)) {
            getTheme().applyStyle(R.style.Theme_Default_Green, true);
        } else if (color == ContextCompat.getColor(this, R.color.myPurple)) {
            getTheme().applyStyle(R.style.Theme_Default_Purple, true);
        } else if (color == ContextCompat.getColor(this, R.color.myBlue)) {
            getTheme().applyStyle(R.style.Theme_Default_Blue, true);
        }
    }

    @Override
    public void activateDrawer() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    @Override
    public void recreateActivity() {
        recreate();
    }

    @Override
    public void currencyChanged(CurrencyList currency) {
        projectAdapter.setCurrency(currency);
        projectAdapter.notifyDataSetChanged();
    }

}