package com.example.projectexpensetracker.adapters;

import com.example.projectexpensetracker.R;
import com.example.projectexpensetracker.models.*;
import com.example.projectexpensetracker.database.*;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    public interface OnExpenseClickListener {
        void onExpenseClick(Expense expense);
    }

    private List<Expense> list;
    private final Context context;
    private final OnExpenseClickListener listener;

    public ExpenseAdapter(Context context, List<Expense> list, OnExpenseClickListener listener) {
        this.context  = context;
        this.list     = list;
        this.listener = listener;
    }

    public void updateList(List<Expense> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_expense, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Expense e = list.get(pos);

        // Icon by type — uses vector drawables
        int iconRes;
        switch (e.getType()) {
            case Expense.TYPE_TRAVEL:    iconRes = R.drawable.ic_calendar;  break; // closest available
            case Expense.TYPE_EQUIPMENT: iconRes = R.drawable.ic_receipt;   break;
            case Expense.TYPE_MATERIALS: iconRes = R.drawable.ic_receipt;   break;
            case Expense.TYPE_SERVICES:  iconRes = R.drawable.ic_person;    break;
            case Expense.TYPE_SOFTWARE:  iconRes = R.drawable.ic_receipt;   break;
            case Expense.TYPE_LABOUR:    iconRes = R.drawable.ic_person;    break;
            case Expense.TYPE_UTILITIES: iconRes = R.drawable.ic_budget;    break;
            default:                     iconRes = R.drawable.ic_receipt;   break;
        }
        h.ivIcon.setImageResource(iconRes);

        h.tvType.setText(e.getType());
        h.tvClaimant.setText(e.getClaimant() + "  •  " + e.getDate());
        h.tvAmount.setText(String.format("%s %,.2f", e.getCurrency(), e.getAmount()));
        h.tvStatus.setText(e.getPaymentStatus());

        // Status color
        switch (e.getPaymentStatus()) {
            case Expense.STATUS_PAID:    h.tvStatus.setTextColor(Color.parseColor("#4CAF50")); break;
            case Expense.STATUS_PENDING: h.tvStatus.setTextColor(Color.parseColor("#FF9800")); break;
            default:                     h.tvStatus.setTextColor(Color.parseColor("#1976D2")); break;
        }

        h.itemView.setOnClickListener(v -> listener.onExpenseClick(e));
    }

    @Override public int getItemCount() { return list == null ? 0 : list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        android.widget.ImageView ivIcon;
        TextView tvType, tvClaimant, tvAmount, tvStatus;
        ViewHolder(@NonNull View v) {
            super(v);
            ivIcon     = v.findViewById(R.id.ivExpenseIcon);
            tvType     = v.findViewById(R.id.tvExpenseType);
            tvClaimant = v.findViewById(R.id.tvExpenseClaimant);
            tvAmount   = v.findViewById(R.id.tvExpenseAmount);
            tvStatus   = v.findViewById(R.id.tvExpenseStatus);
        }
    }
}
