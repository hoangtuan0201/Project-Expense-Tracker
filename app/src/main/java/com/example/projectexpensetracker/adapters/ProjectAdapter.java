package com.example.projectexpensetracker.adapters;

import com.example.projectexpensetracker.R;
import com.example.projectexpensetracker.models.*;
import com.example.projectexpensetracker.database.*;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * ProjectAdapter — displays a list of Projects in a RecyclerView.
 *
 * Usage:
 *   ProjectAdapter adapter = new ProjectAdapter(context, projectList, project -> {
 *       // Handle when a user clicks on a project
 *   });
 *   recyclerView.setAdapter(adapter);
 */
public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    // Interface callback — returns the clicked project to the Activity/Fragment
    public interface OnProjectClickListener {
        void onProjectClick(Project project);
    }

    private final Context context;
    private List<Project> projectList;
    private final OnProjectClickListener listener;

    public ProjectAdapter(Context context, List<Project> projectList,
                          OnProjectClickListener listener) {
        this.context     = context;
        this.projectList = projectList;
        this.listener    = listener;
    }

    // ─── Lifecycle ───────────────────────────────────────────────────────────────

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projectList.get(position);
        holder.bind(project);
    }

    @Override
    public int getItemCount() {
        return projectList.size();
    }

    // ─── Public helpers ──────────────────────────────────────────────────────────

    /** Updates the entire list (used during search or reload). */
    public void updateList(List<Project> newList) {
        this.projectList = newList;
        notifyDataSetChanged();
    }

    // ─── ViewHolder ──────────────────────────────────────────────────────────────

    class ProjectViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvProjectName;
        private final TextView tvProjectCode;
        private final TextView tvStatus;
        private final TextView tvManager;
        private final TextView tvDateRange;
        private final TextView tvBudget;
        private final ImageView ivSyncIcon;
        private final TextView tvSyncStatus;
        private final View viewAccentBar;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProjectName = itemView.findViewById(R.id.tvProjectName);
            tvProjectCode = itemView.findViewById(R.id.tvProjectCode);
            tvStatus      = itemView.findViewById(R.id.tvStatus);
            tvManager     = itemView.findViewById(R.id.tvManager);
            tvDateRange   = itemView.findViewById(R.id.tvDateRange);
            tvBudget      = itemView.findViewById(R.id.tvBudget);
            ivSyncIcon    = itemView.findViewById(R.id.ivSyncIcon);
            tvSyncStatus  = itemView.findViewById(R.id.tvSyncStatus);
            viewAccentBar = itemView.findViewById(R.id.viewAccentBar);
        }

        void bind(Project project) {
            // Basic information
            tvProjectName.setText(project.getProjectName());
            tvProjectCode.setText(project.getProjectCode());
            tvManager.setText(project.getManager());
            tvBudget.setText(String.format("$%,.2f", project.getBudget()));

            // Date range: "01 Jan - 31 Dec"
            tvDateRange.setText(project.getStartDate() + " → " + project.getEndDate());

            // Calculate total spending to check the budget
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            double totalExpenses = dbHelper.getTotalExpenseByProject(project.getId());
            boolean isOverBudget = totalExpenses > project.getBudget();

            // Status badge — different colors for each status
            tvStatus.setText(project.getStatus());
            applyStatusBadgeColor(project.getStatus(), isOverBudget);

            // Sync indicator — icon tint + label
            if (project.isSyncedToCloud()) {
                ivSyncIcon.setImageResource(R.drawable.ic_cloud_done);
                ivSyncIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                tvSyncStatus.setText("Synced");
                tvSyncStatus.setTextColor(Color.parseColor("#4CAF50"));
            } else {
                ivSyncIcon.setImageResource(R.drawable.ic_sync);
                ivSyncIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#9E9E9E")));
                tvSyncStatus.setText("Local");
                tvSyncStatus.setTextColor(Color.parseColor("#9E9E9E"));
            }

            // Click listener — entire card
            itemView.setOnClickListener(v -> listener.onProjectClick(project));
        }

        /** Changes the status badge and left accent bar colors based on status and budget. */
        private void applyStatusBadgeColor(String status, boolean isOverBudget) {
            int bgColor, textColor, accentColor;

            if (isOverBudget) {
                // Red theme for Overbudget (Priority)
                bgColor     = Color.parseColor("#FEE2E2");
                textColor   = Color.parseColor("#991B1B");
                accentColor = Color.parseColor("#EF4444");
            } else {
                switch (status) {
                    case "Active":
                        bgColor     = Color.parseColor("#DCFCE7");
                        textColor   = Color.parseColor("#166534");
                        accentColor = Color.parseColor("#22C55E");
                        break;
                    case "Completed":
                        bgColor     = Color.parseColor("#DBEAFE");
                        textColor   = Color.parseColor("#1E40AF");
                        accentColor = Color.parseColor("#6366F1");
                        break;
                    case "On Hold":
                    default:
                        bgColor     = Color.parseColor("#F3F4F6"); // Light Grey
                        textColor   = Color.parseColor("#374151"); // Dark Grey
                        accentColor = Color.parseColor("#9E9E9E"); // Medium Grey
                        break;
                }
            }

            // Status pill badge
            android.graphics.drawable.GradientDrawable shape =
                    new android.graphics.drawable.GradientDrawable();
            shape.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            shape.setCornerRadius(40f);
            shape.setColor(bgColor);
            tvStatus.setBackground(shape);
            tvStatus.setTextColor(textColor);

            // Left accent bar
            viewAccentBar.setBackgroundColor(accentColor);
        }
    }
}
