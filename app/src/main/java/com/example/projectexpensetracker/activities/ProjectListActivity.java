package com.example.projectexpensetracker.activities;
import com.example.projectexpensetracker.models.*;
import com.example.projectexpensetracker.database.*;
import com.example.projectexpensetracker.adapters.*;
import com.example.projectexpensetracker.utils.*;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProjectListActivity extends AppCompatActivity {

    // ─── UI Views ────────────────────────────────────────────────────────────────
    private RecyclerView recyclerProjects;
    private LinearLayout layoutEmptyState;
    private LinearLayout layoutAdvancedSearch;
    private TextView tvProjectCount;
    private TextView tvEmptyMessage;
    private TextInputEditText etSearch;
    private TextInputEditText etManagerFilter;
    private TextInputEditText etFromDate;
    private TextInputEditText etToDate;
    private AutoCompleteTextView spinnerStatusFilter;
    private MaterialButton btnToggleAdvancedSearch;
    private MaterialButton btnApplyFilters;
    private MaterialButton btnResetFilters;
    private ExtendedFloatingActionButton fabAddProject;

    // ─── Data ────────────────────────────────────────────────────────────────────
    private DatabaseHelper databaseHelper;
    private ProjectAdapter projectAdapter;
    private List<Project> projectList;
    private int currentUserId;

    private static final int REQUEST_ADD_PROJECT  = 1001;
    private static final int REQUEST_EDIT_PROJECT = 1002;

    // ─── Lifecycle ───────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);

        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);

        if (currentUserId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        databaseHelper = new DatabaseHelper(this);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSearchBar();
        setupAdvancedSearch();
        setupFab();
        loadProjects();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProjects();
    }

    // ─── Init ────────────────────────────────────────────────────────────────────

    private void initViews() {
        recyclerProjects        = findViewById(R.id.recyclerProjects);
        layoutEmptyState        = findViewById(R.id.layoutEmptyState);
        layoutAdvancedSearch    = findViewById(R.id.layoutAdvancedSearch);
        tvProjectCount          = findViewById(R.id.tvProjectCount);
        tvEmptyMessage          = findViewById(R.id.tvEmptyMessage);
        etSearch                = findViewById(R.id.etSearch);
        etManagerFilter         = findViewById(R.id.etManagerFilter);
        etFromDate              = findViewById(R.id.etFromDate);
        etToDate                = findViewById(R.id.etToDate);
        spinnerStatusFilter     = findViewById(R.id.spinnerStatusFilter);
        btnToggleAdvancedSearch = findViewById(R.id.btnToggleAdvancedSearch);
        btnApplyFilters         = findViewById(R.id.btnApplyFilters);
        btnResetFilters         = findViewById(R.id.btnResetFilters);
        fabAddProject           = findViewById(R.id.fabAddProject);
    }

    // ─── Toolbar ─────────────────────────────────────────────────────────────────

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_sync) {
                performCloudSync();
                return true;
            }
            if (item.getItemId() == R.id.action_logout) {
                confirmLogout();
                return true;
            }
            if (item.getItemId() == R.id.action_reset_db) {
                confirmResetDatabase();
                return true;
            }
            return false;
        });
    }

    // ─── RecyclerView ────────────────────────────────────────────────────────────

    private void setupRecyclerView() {
        projectList = databaseHelper.getAllProjectsByUser(currentUserId);

        projectAdapter = new ProjectAdapter(this, projectList, project -> {
            Intent intent = new Intent(this, ProjectDetailActivity.class);
            intent.putExtra("project_id", project.getId());
            startActivityForResult(intent, REQUEST_EDIT_PROJECT);
        });

        recyclerProjects.setLayoutManager(new LinearLayoutManager(this));
        recyclerProjects.setAdapter(projectAdapter);
    }

    // ─── FAB ─────────────────────────────────────────────────────────────────────

    private void setupFab() {
        fabAddProject.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditProjectActivity.class);
            intent.putExtra("user_id", currentUserId);
            startActivityForResult(intent, REQUEST_ADD_PROJECT);
        });

        recyclerProjects.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (dy > 0) fabAddProject.shrink();
                else if (dy < 0) fabAddProject.extend();
            }
        });
    }

    // ─── Search ──────────────────────────────────────────────────────────────────

    private void setupSearchBar() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                if (keyword.isEmpty()) {
                    loadProjects();
                } else {
                    List<Project> results = databaseHelper.searchProjects(keyword, currentUserId);
                    updateProjectList(results, "No projects match \"" + keyword + "\"");
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupAdvancedSearch() {
        String[] statusOptions = {"All Status", "Active", "Completed", "On Hold"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, statusOptions);
        spinnerStatusFilter.setAdapter(statusAdapter);
        spinnerStatusFilter.setText(statusOptions[0], false);

        btnToggleAdvancedSearch.setOnClickListener(v -> {
            if (layoutAdvancedSearch.getVisibility() == View.GONE) {
                layoutAdvancedSearch.setVisibility(View.VISIBLE);
                btnToggleAdvancedSearch.setText("▲  Advanced Filters");
            } else {
                layoutAdvancedSearch.setVisibility(View.GONE);
                btnToggleAdvancedSearch.setText("▼  Advanced Filters");
            }
        });

        etFromDate.setOnClickListener(v -> showDatePicker(etFromDate));
        etToDate.setOnClickListener(v -> showDatePicker(etToDate));

        btnApplyFilters.setOnClickListener(v -> {
            String rawStatus = spinnerStatusFilter.getText().toString().trim();
            String status  = (rawStatus.equals("All Status") || rawStatus.isEmpty()) ? "" : rawStatus;
            String manager = etManagerFilter.getText() != null ? etManagerFilter.getText().toString().trim() : "";
            String from    = etFromDate.getText() != null ? etFromDate.getText().toString().trim() : "";
            String to      = etToDate.getText() != null ? etToDate.getText().toString().trim() : "";

            List<Project> results = databaseHelper.advancedSearchProjects(
                    currentUserId,
                    status.isEmpty()  ? null : status,
                    manager.isEmpty() ? null : manager,
                    from.isEmpty()    ? null : from,
                    to.isEmpty()      ? null : to
            );
            updateProjectList(results, "No projects match the selected filters.");
        });

        btnResetFilters.setOnClickListener(v -> {
            spinnerStatusFilter.setText(statusOptions[0], false);
            etManagerFilter.setText("");
            etFromDate.setText("");
            etToDate.setText("");
            etSearch.setText("");
            loadProjects();
        });
    }

    private void showDatePicker(TextInputEditText target) {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder
                .datePicker()
                .setTitleText("Select date")
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            String formatted = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(new Date(selection));
            target.setText(formatted);
        });

        picker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    // ─── Data loading ────────────────────────────────────────────────────────────

    private void loadProjects() {
        projectList = databaseHelper.getAllProjectsByUser(currentUserId);
        updateProjectList(projectList, "No projects yet.\nTap + to add your first project.");
    }

    private void updateProjectList(List<Project> list, String emptyMessage) {
        projectAdapter.updateList(list);

        int count = list.size();
        tvProjectCount.setText(count + (count == 1 ? " project" : " projects"));

        if (count == 0) {
            recyclerProjects.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
            tvEmptyMessage.setText(emptyMessage);
        } else {
            recyclerProjects.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }

    // ─── Actions ─────────────────────────────────────────────────────────────────

    /** Confirmation dialog before logging out. */
    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    getSharedPreferences("user", MODE_PRIVATE)
                            .edit().remove("user_id").apply();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /** Confirmation dialog before full factory reset. */
    private void confirmResetDatabase() {
        new AlertDialog.Builder(this)
                .setTitle("Factory Reset")
                .setMessage("This will permanently delete ALL user accounts, projects, and expenses from this device. You will be logged out. Proceed?")
                .setPositiveButton("Delete Everything", (dialog, which) -> {
                    databaseHelper.resetDatabase();
                    
                    // Clear session and return to login
                    getSharedPreferences("user", MODE_PRIVATE)
                            .edit().clear().apply();
                    
                    Toast.makeText(this, "All data wiped successfully.", Toast.LENGTH_LONG).show();
                    
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /** Perform a full data synchronization to Firebase. */
    private void performCloudSync() {
        String username = databaseHelper.getUsernameById(currentUserId);
        FirebaseSyncHelper.syncAllData(this, username, new FirebaseSyncHelper.SyncCallback() {
            private android.app.ProgressDialog progressDialog;

            @Override
            public void onSyncStarted() {
                progressDialog = new android.app.ProgressDialog(ProjectListActivity.this);
                progressDialog.setTitle("Synchronizing Cloud");
                progressDialog.setMessage("Please wait while we upload your projects to Firebase...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            @Override
            public void onSyncSuccess(String message) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Toast.makeText(ProjectListActivity.this, message, Toast.LENGTH_LONG).show();
                loadProjects();
            }

            @Override
            public void onSyncFailure(String error) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                new AlertDialog.Builder(ProjectListActivity.this)
                        .setTitle("Sync Failed")
                        .setMessage(error)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }
}