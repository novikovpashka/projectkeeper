package com.novikovpashka.projectkeeper.presentation.mainactivity;

import android.content.Intent;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.view.ActionMode.Callback;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.github.javafaker.Faker;
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

@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity implements RadioListener, OnItemClickListener {
    private FloatingActionButton addButton;
    private RecyclerView recyclerView;
    private ImageView sortButton;
    private ImageView cancelSearch;
    private EditText searchText;
    private ShimmerFrameLayout shimmerProjects;
    private CoordinatorLayout coordinatorLayout;
    private MaterialToolbar materialToolbar;
    private DrawerLayout drawerLayout;
    private LinearLayout linearLayout;
    private NavigationView navigationView;

    private FirebaseAuth mAuth;
    private ProjectListAdapter projectAdapter;
    private MainActivityViewModel mainActivityViewModel;

    private ActionMode actionMode;

    private TextView usdrub;
    private TextView eurrub;
    private TextView lastupdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        searchText = binding.searchMain.searchInput;
        sortButton = binding.searchMain.filter;
        cancelSearch = binding.searchMain.cancelSearch;
        addButton = binding.addButton;
        coordinatorLayout = binding.coordinator;
        materialToolbar = binding.materialToolbar;
        drawerLayout = binding.drawerLayout;
        shimmerProjects = binding.shimmerProjects;
        shimmerProjects.setVisibility(View.VISIBLE);
        linearLayout = binding.searchMain.searchBar;
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
            return false;
        });

        materialToolbar.setNavigationOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case id.settings:
                    startSettingsActivity();
                    overridePendingTransition(anim.slide_from_right, anim.slide_to_left);
                case id.logout:
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
                mainActivityViewModel.getSearchTextLiveData().setValue(editable.toString().trim().
                        toLowerCase(Locale.ROOT));
                if (!editable.toString().equals("")) {
                    sortCollapse();
                }
                else sortExpand();
            }
        });

        sortButton.setOnClickListener(view -> {
            BottomSortDialog bottomSortDialog = new BottomSortDialog();
            Bundle bundle = new Bundle();
            bundle.putSerializable("currentSortParam", mainActivityViewModel.
                    getSortParamLiveData().getValue());
            bundle.putSerializable("currentOrderParam", mainActivityViewModel.
                    getOrderParamLiveData().getValue());
            bottomSortDialog.setArguments(bundle);
            bottomSortDialog.show(getSupportFragmentManager(), "filter_dialog");
        });

        cancelSearch.setOnClickListener(view -> {
            searchText.setText("");
            searchText.clearFocus();
            sortExpand();
        });

    }

    private void initRecyclerAndObservers() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(projectAdapter);

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
            projectAdapter.setSelectMode(aBoolean);
            if(aBoolean) {
                actionMode = startSupportActionMode(new ActionBarCallback());
            }
            else if (actionMode !=null) {
                actionMode.finish();
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

        mainActivityViewModel.getSnackbar().observe(this, s -> Snackbar.
                make(coordinatorLayout, s, BaseTransientBottomBar.LENGTH_LONG).
                setAction("UNDO", view -> mainActivityViewModel.
                        restoreDeletedProjects()).show());

        recyclerView.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    /* Scroll Down */
                    if (addButton.isShown()) {
                        addButton.hide();
                    }
                    if (!recyclerView.canScrollVertically(1) && !projectAdapter.getSelectMode()) {
                        addButton.show();
                    }
                } else if (dy < 0) {
                    /* Scroll Up */
                    if (!addButton.isShown() && !projectAdapter.getSelectMode()) {
                        addButton.show();
                    }
                }
            }
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
                addTenRandomProjects();
                return true;
            }
            else if (item.getItemId() == id.add_random_1) {
                addOneRandomProject();
                return true;
            }
            return false;
        });
    }

    /**Add 10 random**/
    private void addTenRandomProjects() {
        for (int i = 0; i < 10; i++) {
            Faker faker = new Faker();
            String name = faker.country().capital();
            double price = Double.parseDouble(new DecimalFormat("####").format((int) (Math.random() * 200000)/1000*1000));
            double incoming = Double.parseDouble(new DecimalFormat("####").format((int) (Math.random() * price)/1000*1000));
            ArrayList<Double> incomings = new ArrayList<>();
            incomings.add(incoming);
            Project project = new Project(name, price, incomings);
            mainActivityViewModel.addProject(project);
        }
    }

    /**Add 1 random**/
    private void addOneRandomProject() {
        Faker faker = new Faker();
        String name = faker.country().capital();

        double price = Double.parseDouble(new DecimalFormat("####").
                format((int) (Math.random() * 200000)/1000*1000));
        double incoming = Double.parseDouble(new DecimalFormat("####")
                .format((int) (Math.random() * price)/1000*1000));
        ArrayList<Double> incomings = new ArrayList<>();
        incomings.add(incoming);

        Project project = new Project(name, price, incomings);
        mainActivityViewModel.addProject(project);

    }

    private void sortCollapse() {
        cancelSearch.setVisibility(View.VISIBLE);
        sortButton.setClickable(false);
        sortButton.animate().alpha(0).rotation(45).setDuration(100);
        cancelSearch.setClickable(true);
        cancelSearch.animate().alpha(1).rotation(0).setDuration(100);
    }

    private void sortExpand() {
        sortButton.setClickable(true);
        sortButton.animate().alpha(1).rotation(0).setDuration(100);
        cancelSearch.setClickable(false);
        cancelSearch.animate().alpha(0).rotation(-45).setDuration(100);
    }

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

    class ActionBarCallback implements Callback {
        int statusBarColor;
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            actionMode = mode;
            actionMode.getMenuInflater().inflate(R.menu.topappbar_menu_contextual, menu);
            statusBarColor = getWindow().getStatusBarColor();

            TypedValue typedValue = new TypedValue();
            Theme theme = getTheme();
            theme.resolveAttribute(attr.backgroundColor, typedValue, true);
            @ColorInt int color = typedValue.data;
            getWindow().setStatusBarColor(color);
            addButton.hide();
            linearLayout.setVisibility(View.GONE);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == id.delete) {
                mainActivityViewModel.deleteProjects();
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            for (int x : mainActivityViewModel.clearSelectedProjects()) {
                projectAdapter.notifyItemChanged(x);
            }
            addButton.show();
            linearLayout.setVisibility(View.VISIBLE);

            //Crutch to avoid status bar blinking
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getWindow().setStatusBarColor(statusBarColor);
                }
            }, 500);
        }
    }

}