package com.example.projectexpensetracker;

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
 * ProjectAdapter — hiển thị danh sách Project trong RecyclerView.
 *
 * Cách dùng:
 *   ProjectAdapter adapter = new ProjectAdapter(context, projectList, project -> {
 *       // xử lý khi user click vào 1 project
 *   });
 *   recyclerView.setAdapter(adapter);
 */
public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    // Interface callback — trả về project được click lên Activity/Fragment
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

    /** Cập nhật toàn bộ danh sách (dùng khi search hoặc reload). */
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
            // Thông tin cơ bản
            tvProjectName.setText(project.getProjectName());
            tvProjectCode.setText(project.getProjectCode());
            tvManager.setText(project.getManager());
            tvBudget.setText(String.format("$%,.2f", project.getBudget()));

            // Date range: "01 Jan - 31 Dec"
            tvDateRange.setText(project.getStartDate() + " → " + project.getEndDate());

            // Status badge — màu khác nhau cho từng trạng thái
            tvStatus.setText(project.getStatus());
            applyStatusBadgeColor(project.getStatus());

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

            // Click listener — toàn bộ card
            itemView.setOnClickListener(v -> listener.onProjectClick(project));
        }

        /** Đổi màu status badge + accent bar bên trái theo trạng thái. */
        private void applyStatusBadgeColor(String status) {
            int bgColor, textColor, accentColor;

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
                    bgColor     = Color.parseColor("#FEF9C3");
                    textColor   = Color.parseColor("#854D0E");
                    accentColor = Color.parseColor("#F59E0B");
                    break;
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