package com.example.projectexpensetracker.activities;

import com.example.projectexpensetracker.R;
import com.example.projectexpensetracker.models.*;
import com.example.projectexpensetracker.database.*;
import com.example.projectexpensetracker.adapters.*;
import com.example.projectexpensetracker.utils.*;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * ProjectDetailActivity — Req (b) + (c)
 * - Displays complete project information (View, Edit, Delete)
 * - List and manage project expenses (Add, Edit)
 */
public class ProjectDetailActivity extends AppCompatActivity {

    private static final int REQ_EDIT_PROJECT = 201;
    private static final int REQ_ADD_EXPENSE  = 202;
    private static final int REQ_EDIT_EXPENSE = 203;

    // ─── Views ───────────────────────────────────────────────────────────────────
    private TextView tvDetailName, tvDetailCode, tvDetailDescription;
    private TextView tvDetailStatus, tvDetailManager, tvDetailDateRange;
    private TextView tvDetailBudget, tvDetailTotalSpent;
    private View     cardOptional;
    private View     rowSpecial, rowClient;
    private TextView tvDetailSpecial, tvDetailClient;
    private TextView tvExpenseCount;
    private View     layoutNoExpense;
    private RecyclerView recyclerExpenses;
    private FloatingActionButton fabAddExpense;

    // ─── Data ────────────────────────────────────────────────────────────────────
    private DatabaseHelper databaseHelper;
    private ExpenseAdapter expenseAdapter;
    private int projectId;
    private Project project;

    // ─── Lifecycle ───────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail);

        projectId = getIntent().getIntExtra("project_id", -1);
        if (projectId == -1) { finish(); return; }

        databaseHelper = new DatabaseHelper(this);

        initViews();
        setupToolbar();
        loadProjectData();
        setupExpenseList();
    }

    // ─── Init ────────────────────────────────────────────────────────────────────

    private void initViews() {
        tvDetailName        = findViewById(R.id.tvDetailName);
        tvDetailCode        = findViewById(R.id.tvDetailCode);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);
        tvDetailStatus      = findViewById(R.id.tvDetailStatus);
        tvDetailManager     = findViewById(R.id.tvDetailManager);
        tvDetailDateRange   = findViewById(R.id.tvDetailDateRange);
        tvDetailBudget      = findViewById(R.id.tvDetailBudget);
        tvDetailTotalSpent  = findViewById(R.id.tvDetailTotalSpent);
        tvDetailSpecial     = findViewById(R.id.tvDetailSpecial);
        tvDetailClient      = findViewById(R.id.tvDetailClient);
        rowSpecial          = findViewById(R.id.rowSpecial);
        rowClient           = findViewById(R.id.rowClient);
        cardOptional        = findViewById(R.id.cardOptional);
        tvExpenseCount      = findViewById(R.id.tvExpenseCount);
        layoutNoExpense     = findViewById(R.id.layoutNoExpense);
        recyclerExpenses    = findViewById(R.id.recyclerExpenses);
        fabAddExpense       = findViewById(R.id.fabAddExpense);

        fabAddExpense.setOnClickListener(v -> {
            Intent i = new Intent(this, AddEditExpenseActivity.class);
            i.putExtra("project_id", projectId);
            startActivityForResult(i, REQ_ADD_EXPENSE);
        });
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit_project) {
                openEditProject();
                return true;
            }
            if (item.getItemId() == R.id.action_delete_project) {
                confirmDeleteProject();
                return true;
            }
            return false;
        });
    }

    // ─── Data Loading ─────────────────────────────────────────────────────────────

    /** Req (b): Load and display complete project information. */
    private void loadProjectData() {
        project = databaseHelper.getProjectById(projectId);
        if (project == null) { finish(); return; }

        tvDetailName.setText(project.getProjectName());
        tvDetailCode.setText(project.getProjectCode());
        tvDetailDescription.setText(project.getDescription());
        tvDetailManager.setText(project.getManager());
        tvDetailDateRange.setText(project.getStartDate() + " → " + project.getEndDate());
        tvDetailBudget.setText(String.format("$%,.2f", project.getBudget()));

        // Status badge color
        applyStatusColor(project.getStatus());

        // Total spent
        double spent = databaseHelper.getTotalExpenseByProject(projectId);
        tvDetailTotalSpent.setText(String.format("$%,.2f", spent));

        // Optional fields — use row visibility, plain text (icon is in XML)
        boolean hasOptional = false;
        if (project.getSpecialRequirements() != null && !project.getSpecialRequirements().isEmpty()) {
            tvDetailSpecial.setText(project.getSpecialRequirements());
            rowSpecial.setVisibility(View.VISIBLE);
            hasOptional = true;
        } else {
            rowSpecial.setVisibility(View.GONE);
        }
        if (project.getClientInfo() != null && !project.getClientInfo().isEmpty()) {
            tvDetailClient.setText(project.getClientInfo());
            rowClient.setVisibility(View.VISIBLE);
            hasOptional = true;
        } else {
            rowClient.setVisibility(View.GONE);
        }
        cardOptional.setVisibility(hasOptional ? View.VISIBLE : View.GONE);
        
        ImageView ivDetailPhoto = findViewById(R.id.ivDetailPhoto);
        if (project.getPhotoUrl() != null && !project.getPhotoUrl().isEmpty()) {
            ivDetailPhoto.setVisibility(View.VISIBLE);
            com.bumptech.glide.Glide.with(this)
                .load(project.getPhotoUrl())
                .into(ivDetailPhoto);
        } else {
            ivDetailPhoto.setVisibility(View.GONE);
        }
    }

    private void applyStatusColor(String status) {
        int bg, fg;
        switch (status) {
            case "Active":
                bg = Color.parseColor("#E8F5E9"); fg = Color.parseColor("#2E7D32"); break;
            case "Completed":
                bg = Color.parseColor("#E3F2FD"); fg = Color.parseColor("#1565C0"); break;
            default: // On Hold
                bg = Color.parseColor("#FFF8E1"); fg = Color.parseColor("#F57F17"); break;
        }
        android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
        shape.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        shape.setCornerRadius(40f);
        shape.setColor(bg);
        tvDetailStatus.setBackground(shape);
        tvDetailStatus.setTextColor(fg);
        tvDetailStatus.setText(status);
    }

    /** Req (c): Set up RecyclerView to display the expense list. */
    private void setupExpenseList() {
        recyclerExpenses.setLayoutManager(new LinearLayoutManager(this));
        refreshExpenseList();
    }

    private void refreshExpenseList() {
        List<Expense> expenses = databaseHelper.getExpensesByProject(projectId);

        if (expenseAdapter == null) {
            expenseAdapter = new ExpenseAdapter(this, expenses, expense -> {
                // Long-press or click → edit expense
                new MaterialAlertDialogBuilder(this)
                        .setTitle(expense.getExpenseCode())
                        .setMessage("Amount: " + expense.getCurrency() + " " + String.format("%,.2f", expense.getAmount()) +
                                "\nType: " + expense.getType() +
                                "\nClaimant: " + expense.getClaimant() +
                                "\nStatus: " + expense.getPaymentStatus())
                        .setPositiveButton("Edit", (d, w) -> {
                            Intent i = new Intent(this, AddEditExpenseActivity.class);
                            i.putExtra("expense_id", expense.getId());
                            startActivityForResult(i, REQ_EDIT_EXPENSE);
                        })
                        .setNegativeButton("Delete", (d, w) -> confirmDeleteExpense(expense))
                        .setNeutralButton("Cancel", null)
                        .show();
            });
            recyclerExpenses.setAdapter(expenseAdapter);
        } else {
            expenseAdapter.updateList(expenses);
        }

        int count = expenses.size();
        tvExpenseCount.setText(count + " item" + (count == 1 ? "" : "s"));
        layoutNoExpense.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
        recyclerExpenses.setVisibility(count == 0 ? View.GONE : View.VISIBLE);

        // Update total spent after any change
        double spent = databaseHelper.getTotalExpenseByProject(projectId);
        tvDetailTotalSpent.setText(String.format("$%,.2f", spent));
    }

    // ─── Actions ─────────────────────────────────────────────────────────────────

    private void openEditProject() {
        Intent i = new Intent(this, AddEditProjectActivity.class);
        i.putExtra("project_id", projectId);
        startActivityForResult(i, REQ_EDIT_PROJECT);
    }

    private void confirmDeleteProject() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Project?")
                .setMessage("\"" + project.getProjectName() + "\" and all its expenses will be permanently deleted.")
                .setPositiveButton("Delete", (d, w) -> {
                    databaseHelper.deleteProject(projectId);
                    Toast.makeText(this, "Project deleted", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDeleteExpense(Expense expense) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Expense?")
                .setMessage("Delete expense " + expense.getExpenseCode() + "?")
                .setPositiveButton("Delete", (d, w) -> {
                    databaseHelper.deleteExpense(expense.getId());
                    Toast.makeText(this, "Expense deleted", Toast.LENGTH_SHORT).show();
                    refreshExpenseList();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ─── Activity Results ─────────────────────────────────────────────────────────

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQ_EDIT_PROJECT) {
                loadProjectData();        // Refresh project info
                setResult(RESULT_OK);     // Propagate up to ProjectListActivity
            } else if (requestCode == REQ_ADD_EXPENSE || requestCode == REQ_EDIT_EXPENSE) {
                refreshExpenseList();     // Refresh expense list + total spent
            }
        }
    }
}
