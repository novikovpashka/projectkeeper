package com.novikovpashka.projectkeeper.presentation.mainactivity;

import android.content.Intent;
import android.content.res.Resources.Theme;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.WrapperListAdapter;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.view.ActionMode.Callback;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.github.javafaker.Faker;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.novikovpashka.projectkeeper.R;
import com.novikovpashka.projectkeeper.R.anim;
import com.novikovpashka.projectkeeper.R.attr;
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
import com.novikovpashka.projectkeeper.presentation.settingsactivity.SettingsActivity;
import com.novikovpashka.projectkeeper.presentation.startactivity.StartActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements RadioListener, OnItemClickListener {
    private FloatingActionButton addButton;
    private RecyclerView recyclerView;
//    private ImageView sortButton;
//    private ImageView cancelSearch;
//    private EditText searchText;
    private ShimmerFrameLayout shimmerProjects;
    private CoordinatorLayout coordinatorLayout;
    private MaterialToolbar materialToolbar;
    private DrawerLayout drawerLayout;
//    private LinearLayout linearLayout;
    private NavigationView navigationView;

    private FirebaseAuth mAuth;
    private ProjectListAdapter projectAdapter;
    private MainActivityViewModel mainActivityViewModel;
    private AppBarLayout appBarLayout;

    private TextView usdrub;
    private TextView eurrub;
    private TextView lastupdate;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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


//        searchText = binding.searchMain.searchInput;
//        sortButton = binding.searchMain.filter;
//        cancelSearch = binding.searchMain.cancelSearch;
        appBarLayout = binding.appbarlayout;
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

        mainActivityViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

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
    }

    private void setToolbarListeners() {
        materialToolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == id.logout) {
                mAuth.signOut();
                startStartActivity();
                return true;
            }
            if (item.getItemId() == id.delete) {
                mainActivityViewModel.deleteProjects();
            }
            return false;
        });

        materialToolbar.setNavigationOnClickListener(view ->
                drawerLayout.openDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case id.settings:
                    startSettingsActivity();
                    overridePendingTransition(anim.slide_from_right, anim.slide_to_left);
                case id.logout:
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

//        sortButton.setOnClickListener(view -> {
//            BottomSortDialog bottomSortDialog = new BottomSortDialog();
//            Bundle bundle = new Bundle();
//            bundle.putSerializable("currentSortParam", mainActivityViewModel.
//                    getSortParamLiveData().getValue());
//            bundle.putSerializable("currentOrderParam", mainActivityViewModel.
//                    getOrderParamLiveData().getValue());
//            bottomSortDialog.setArguments(bundle);
//            bottomSortDialog.show(getSupportFragmentManager(), "filter_dialog");
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

    private void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
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




}