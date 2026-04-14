package com.example.projectexpensetracker;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        private final TextView tvSyncStatus;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProjectName = itemView.findViewById(R.id.tvProjectName);
            tvProjectCode = itemView.findViewById(R.id.tvProjectCode);
            tvStatus      = itemView.findViewById(R.id.tvStatus);
            tvManager     = itemView.findViewById(R.id.tvManager);
            tvDateRange   = itemView.findViewById(R.id.tvDateRange);
            tvBudget      = itemView.findViewById(R.id.tvBudget);
            tvSyncStatus  = itemView.findViewById(R.id.tvSyncStatus);
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

            // Sync indicator
            if (project.isSyncedToCloud()) {
                tvSyncStatus.setText("⬤ Synced");
                tvSyncStatus.setTextColor(Color.parseColor("#4CAF50")); // xanh lá
            } else {
                tvSyncStatus.setText("⬤ Local");
                tvSyncStatus.setTextColor(Color.parseColor("#9E9E9E")); // xám
            }

            // Click listener — toàn bộ card
            itemView.setOnClickListener(v -> listener.onProjectClick(project));
        }

        /** Đổi màu nền + chữ của status badge theo trạng thái. */
        private void applyStatusBadgeColor(String status) {
            int bgColor;
            int textColor;

            switch (status) {
                case "Active":
                    bgColor   = Color.parseColor("#E8F5E9"); // xanh lá nhạt
                    textColor = Color.parseColor("#2E7D32"); // xanh lá đậm
                    break;
                case "Completed":
                    bgColor   = Color.parseColor("#E3F2FD"); // xanh dương nhạt
                    textColor = Color.parseColor("#1565C0"); // xanh dương đậm
                    break;
                case "On Hold":
                default:
                    bgColor   = Color.parseColor("#FFF8E1"); // vàng nhạt
                    textColor = Color.parseColor("#F57F17"); // cam đậm
                    break;
            }

            // Tạo background shape động (rounded rectangle)
            android.graphics.drawable.GradientDrawable shape =
                    new android.graphics.drawable.GradientDrawable();
            shape.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            shape.setCornerRadius(40f);
            shape.setColor(bgColor);

            tvStatus.setBackground(shape);
            tvStatus.setTextColor(textColor);
        }
    }
}