package com.example.projectexpensetracker.database;
import com.example.projectexpensetracker.models.*;

import android.content.Context;
import androidx.sqlite.db.SimpleSQLiteQuery;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {

    private final AppDatabase db;

    public DatabaseHelper(Context context) {
        db = AppDatabase.getInstance(context);
    }

    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  AUTHENTICATION
    // ═══════════════════════════════════════════════════════════════════════════

    public boolean register(String username, String password) {
        try {
            long result = db.userDao().insert(new User(username, hashPassword(password)));
            return result != -1;
        } catch (Exception e) {
            return false; // Usually caught constraint exception
        }
    }

    public int login(String username, String password) {
        Integer id = db.userDao().login(username, hashPassword(password));
        return id != null ? id : -1;
    }

    public String getUsernameById(int userId) {
        return db.userDao().getUsernameById(userId);
    }

    public int getUserIdByUsername(String username) {
        Integer id = db.userDao().getUserIdByUsername(username);
        return id != null ? id : -1;
    }

    public boolean forceRegisterUser(User user) {
        try {
            long result = db.userDao().insert(user);
            return result != -1;
        } catch (Exception e) {
            return false;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  PROJECT
    // ═══════════════════════════════════════════════════════════════════════════

    public long addProject(Project project) {
        try {
            return db.projectDao().insert(project);
        } catch (Exception e) {
            return -1;
        }
    }

    public List<Project> getAllProjectsByUser(int userId) {
        return db.projectDao().getAllByUser(userId);
    }

    public Project getProjectById(int projectId) {
        return db.projectDao().getById(projectId);
    }

    public List<Project> searchProjects(String keyword, int userId) {
        return db.projectDao().searchProjects(userId, "%" + keyword + "%");
    }

    public List<Project> advancedSearchProjects(int userId, String status, String manager, String fromDate, String toDate) {
        StringBuilder query = new StringBuilder("SELECT * FROM projects WHERE user_id = ?");
        List<Object> args = new ArrayList<>();
        args.add(userId);

        if (status != null && !status.isEmpty()) {
            query.append(" AND status = ?");
            args.add(status);
        }
        if (manager != null && !manager.isEmpty()) {
            query.append(" AND manager LIKE ?");
            args.add("%" + manager + "%");
        }
        if (fromDate != null && !fromDate.isEmpty()) {
            query.append(" AND start_date >= ?");
            args.add(fromDate);
        }
        if (toDate != null && !toDate.isEmpty()) {
            query.append(" AND end_date <= ?");
            args.add(toDate);
        }
        query.append(" ORDER BY start_date DESC");

        SimpleSQLiteQuery sqLiteQuery = new SimpleSQLiteQuery(query.toString(), args.toArray());
        return db.projectDao().advancedSearch(sqLiteQuery);
    }

    public List<Project> getUnsyncedProjects(int userId) {
        return db.projectDao().getUnsyncedProjects(userId);
    }

    public int updateProject(Project project) {
        project.setUpdatedAt(getCurrentDateTime());
        return db.projectDao().update(project);
    }

    public void markProjectSynced(int projectId) {
        db.projectDao().markAsSynced(projectId, getCurrentDateTime());
    }

    public int deleteProject(int projectId) {
        return db.projectDao().delete(projectId);
    }

    public void resetDatabase() {
        db.clearAllTables();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  EXPENSES
    // ═══════════════════════════════════════════════════════════════════════════

    public long addExpense(Expense expense) {
        try {
            return db.expenseDao().insert(expense);
        } catch (Exception e) {
            return -1;
        }
    }

    public List<Expense> getExpensesByProject(int projectId) {
        return db.expenseDao().getByProject(projectId);
    }

    public Expense getExpenseById(int expenseId) {
        return db.expenseDao().getById(expenseId);
    }

    public double getTotalExpenseByProject(int projectId) {
        Double total = db.expenseDao().getTotalExpenseByProject(projectId);
        return total != null ? total : 0.0;
    }

    public List<Expense> getUnsyncedExpenses(int projectId) {
        return db.expenseDao().getUnsyncedExpenses(projectId);
    }

    public int updateExpense(Expense expense) {
        expense.setUpdatedAt(getCurrentDateTime());
        return db.expenseDao().update(expense);
    }

    public void markExpenseSynced(int expenseId) {
        db.expenseDao().markAsSynced(expenseId, getCurrentDateTime());
    }

    public int deleteExpense(int expenseId) {
        return db.expenseDao().delete(expenseId);
    }

    public int deleteAllExpensesByProject(int projectId) {
        return db.expenseDao().deleteAllByProject(projectId);
    }

    private String getCurrentDateTime() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                java.util.Locale.getDefault())
                .format(new java.util.Date());
    }
}