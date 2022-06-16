package com.novikovpashka.projectkeeper.presentation.mainactivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
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
import com.novikovpashka.projectkeeper.data.datafirestore.Project;
import com.novikovpashka.projectkeeper.databinding.ActivityMainBinding;
import com.novikovpashka.projectkeeper.presentation.addprojectactivity.AddProjectActivity;
import com.novikovpashka.projectkeeper.presentation.editprojectactivity.EditProjectActivity;
import com.novikovpashka.projectkeeper.presentation.mainactivity.BottomSortDialog.RadioListener;
import com.novikovpashka.projectkeeper.presentation.mainactivity.MainActivityViewModel.OrderParam;
import com.novikovpashka.projectkeeper.presentation.mainactivity.MainActivityViewModel.SortParam;
import com.novikovpashka.projectkeeper.presentation.mainactivity.ProjectListAdapter.OnItemClickListener;
import com.novikovpashka.projectkeeper.presentation.settingsfragment.SettingsFragment;
import com.novikovpashka.projectkeeper.presentation.startactivity.StartActivity;

public class MainActivity extends AppCompatActivity implements RadioListener, OnItemClickListener, SettingsFragment.SettingsListener {
    private FloatingActionButton addButton;
    private RecyclerView recyclerView;
    private ShimmerFrameLayout shimmerProjects;
    private CoordinatorLayout coordinatorLayout;
    private MaterialToolbar materialToolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private FirebaseAuth mAuth;
    private ProjectListAdapter projectAdapter;
    private MainActivityViewModel mainActivityViewModel;

    private TextView usdrub;
    private TextView eurrub;
    private TextView lastupdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("mytag", "main oncreate");
        mainActivityViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        setAccentColor(mainActivityViewModel.loadAccentColor());

        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
            binding.appbarlayout.setOnApplyWindowInsetsListener((v, insets) -> {
                Insets mInsets = insets.getInsets(WindowInsets.Type.systemBars());
                v.setPadding(0, mInsets.top, 0, 0);
                return insets;
            });

            binding.addButton.setOnApplyWindowInsetsListener((v, insets) -> {
                Insets mInsets = insets.getInsets(WindowInsets.Type.systemBars());
                CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
        mainActivityViewModel.getValueUSDRUB();
        mainActivityViewModel.getValueEURRUB();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mainActivityViewModel.loadCurrentCurrency();

        if (mainActivityViewModel.getCurrentAccentColor() != mainActivityViewModel.loadAccentColor()) {
            mainActivityViewModel.setCurrentAccentColor(mainActivityViewModel.loadAccentColor());
            recreate();
        }

    }


    private void setToolbarListeners() {
        materialToolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == id.sort) {
                BottomSortDialog bottomSortDialog = new BottomSortDialog();
                Bundle bundle = new Bundle();
                bundle.putSerializable("currentSortParam", mainActivityViewModel.
                        getSortParamLiveData().getValue());
                bundle.putSerializable("currentOrderParam", mainActivityViewModel.
                        getOrderParamLiveData().getValue());
                bottomSortDialog.setArguments(bundle);
                bottomSortDialog.show(getSupportFragmentManager(), "filter_dialog");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

                }

            }
            else if (item.getItemId() == id.delete) {
                mainActivityViewModel.deleteProjects();
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
                                anim.slide_from_right,
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

//        searchText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//                mainActivityViewModel.getSearchTextLiveData().setValue(editable.toString().trim().
//                        toLowerCase(Locale.ROOT));
//                if (!editable.toString().equals("")) {
//                    sortCollapse();
//                }
//                else sortExpand();
//            }
//        });

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

        mainActivityViewModel.getProjects().observe(this, projects ->
                projectAdapter.submitList(projects));

        mainActivityViewModel.getShimmerActive().observe(this, aBoolean ->
                shimmerProjects.setVisibility(aBoolean ? View.VISIBLE : View.GONE ));

        mainActivityViewModel.getUSDRUB().observe(this, s -> {
            String result = s + "₽";
            usdrub.setText(result);
            try {
                projectAdapter.setUsdRate(Double.parseDouble(s));
            }
            catch (Exception ignored) {

            }
        });

        mainActivityViewModel.getCurrency().observe(this, currency -> {
            projectAdapter.setCurrency(currency);
            projectAdapter.notifyDataSetChanged();
        });

        mainActivityViewModel.getEURRUB().observe(this, s -> {
            String result = s + "₽";
            eurrub.setText(result);
            try {
                projectAdapter.setEurRate(Double.parseDouble(s));
            }
            catch (Exception ignored) {

            }
        });

        mainActivityViewModel.getUpdated().observe(this, s -> lastupdate.setText(s));

        mainActivityViewModel.getSelectMode().observe(this, aBoolean -> {
            boolean prevValue = projectAdapter.getSelectMode();
            projectAdapter.setSelectMode(aBoolean);
            if(aBoolean) {
                setSelectedMode();
            }
            else {
                if (prevValue) stopSelectMode();
            }
        });

        mainActivityViewModel.getProjectsToDelete().observe(this, projects -> {
            projectAdapter.getSelectedProject().clear();
            if (!projects.isEmpty()) {
                projectAdapter.getSelectedProject().addAll(projects);
            }
        });

        mainActivityViewModel.getProjectsIdToDelete().observe(this, integers -> {
            projectAdapter.getSelectedId().clear();
            if (!integers.isEmpty()) {
                projectAdapter.getSelectedId().addAll(integers);
            }
        });

        mainActivityViewModel.getSnackbar().observe(this, s ->  {
            if (s != null) {
                Snackbar.make(coordinatorLayout, s, 5000).
                    setAction("UNDO", view -> mainActivityViewModel.
                            restoreDeletedProjects()).show();
            }
        });

        mainActivityViewModel.getTitle().observe(this, s ->  {
            if (s != null) {
                materialToolbar.setTitle(s);
            }
            else materialToolbar.setTitle("Projects");
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Project project = intent.getParcelableExtra("projectToAdd");
        mainActivityViewModel.addProject(project);
    }


    @Override
    protected void onStop() {
        super.onStop();
        drawerLayout.closeDrawer(Gravity.LEFT, false);
    }


    /**Add projects menu**/
    public void showAddPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
        popupMenu.getMenuInflater().inflate(menu.main_add_menu, popupMenu.getMenu());
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == id.add_project) {
                startAddProjectActivity();
                return true;
            }
            else if (item.getItemId() == id.add_random_10) {
//                addTenRandomProjects();
                return true;
            }
            else if (item.getItemId() == id.add_random_1) {
//                addOneRandomProject();
                return true;
            }
            return false;
        });
    }

    /**Add 10 random**/
//    private void addTenRandomProjects() {
//        for (int i = 0; i < 10; i++) {
//            Faker faker = new Faker();
//            String name = faker.country().capital();
//            double price = Double.parseDouble(new DecimalFormat("####").format((int) (Math.random() * 200000)/1000*1000));
//            double incoming = Double.parseDouble(new DecimalFormat("####").format((int) (Math.random() * price)/1000*1000));
//            ArrayList<Double> incomings = new ArrayList<>();
//            incomings.add(incoming);
//            Project project = new Project(name, price, incomings);
//            mainActivityViewModel.addProject(project);
//        }
//    }

    /**Add 1 random**/
//    private void addOneRandomProject() {
//        Faker faker = new Faker();
//        String name = faker.country().capital();
//
//        double price = Double.parseDouble(new DecimalFormat("####").
//                format((int) (Math.random() * 200000)/1000*1000));
//        double incoming = Double.parseDouble(new DecimalFormat("####")
//                .format((int) (Math.random() * price)/1000*1000));
//        ArrayList<Double> incomings = new ArrayList<>();
//        incomings.add(incoming);
//
//        Project project = new Project(name, price, incomings);
//        mainActivityViewModel.addProject(project);
//    }

//    private void sortCollapse() {
//        cancelSearch.setVisibility(View.VISIBLE);
//        sortButton.setClickable(false);
//        sortButton.animate().alpha(0).rotation(45).setDuration(100);
//        cancelSearch.setClickable(true);
//        cancelSearch.animate().alpha(1).rotation(0).setDuration(100);
//    }
//
//    private void sortExpand() {
//        sortButton.setClickable(true);
//        sortButton.animate().alpha(1).rotation(0).setDuration(100);
//        cancelSearch.setClickable(false);
//        cancelSearch.animate().alpha(0).rotation(-45).setDuration(100);
//    }

    private void startAddProjectActivity() {
        Intent intent = new Intent(this, AddProjectActivity.class);
        startActivity(intent);
    }


    private void startStartActivity() {
        Intent intent = new Intent(this, StartActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void startEditProjectActivity(Project project) {
        Intent intent = new Intent(this, EditProjectActivity.class);
        intent.putExtra("Project", project);
        startActivity(intent);
    }

    @Override
    public void applySortClicked(SortParam sortParam, OrderParam orderParam) {
        mainActivityViewModel.getSortParamLiveData().setValue(sortParam);
        mainActivityViewModel.getOrderParamLiveData().setValue(orderParam);
    }

    @Override
    public void onItemClick(@NonNull Project project) {
        startEditProjectActivity(project);
    }

    @Override
    public void showActionMenu() {
        mainActivityViewModel.getSelectMode().setValue(true);
    }

    @Override
    public void closeActionMenu() {
        mainActivityViewModel.getSelectMode().setValue(false);
        mainActivityViewModel.clearSelectedProjects();
    }

    @Override
    public void addProjectToDelete(@NonNull Project project, int position) {
        mainActivityViewModel.addProjectToDelete(project, position);
    }

    @Override
    public void removeProjectToDelete(@NonNull Project project, int position) {
        mainActivityViewModel.removeProjectToDelete(project, position);
    }


    public int dpToPx(int dp){
        return (int)(dp * getResources().getDisplayMetrics().density);
    }

    public void setSelectedMode() {
        addButton.hide();
        materialToolbar.getMenu().clear();
        materialToolbar.inflateMenu(menu.topappbar_menu_contextual);
        materialToolbar.setNavigationIcon(R.drawable.ic_baseline_close_24);
        materialToolbar.setNavigationOnClickListener(v -> {
            for (int x : mainActivityViewModel.clearSelectedProjects()) {
                projectAdapter.notifyItemChanged(x);
            }
            addButton.show();
        });
    }

    public void stopSelectMode() {
        for (int x : mainActivityViewModel.clearSelectedProjects()) {
            projectAdapter.notifyItemChanged(x);
        }
        materialToolbar.getMenu().clear();
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