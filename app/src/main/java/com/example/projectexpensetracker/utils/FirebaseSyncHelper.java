package com.example.projectexpensetracker.utils;
import com.example.projectexpensetracker.models.*;
import com.example.projectexpensetracker.database.*;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FirebaseSyncHelper — Logic đồng bộ hóa dữ liệu và quản lý tài khoản trên Cloud.
 */
public class FirebaseSyncHelper {

    private static final String TAG = "FirebaseSyncHelper";

    public interface SyncCallback {
        void onSyncStarted();
        void onSyncSuccess(String message);
        void onSyncFailure(String error);
    }

    public interface AuthCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }

    /**
     * Đăng ký tài khoản lên Cloud để có thể đồng bộ xuyên thiết bị.
     */
    public static void registerOnCloud(User user, AuthCallback callback) {
        FirebaseDatabase database = FirebaseDatabase.getInstance(FirebaseConfig.DATABASE_URL);
        DatabaseReference accountRef = database.getReference(FirebaseConfig.getAccountNode(user.getUsername()));

        accountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    callback.onFailure("Username already exists on Cloud.");
                } else {
                    Map<String, String> accountInfo = new HashMap<>();
                    accountInfo.put("username", user.getUsername());
                    accountInfo.put("password", user.getPassword()); // Đã được hash từ DatabaseHelper
                    
                    accountRef.setValue(accountInfo)
                        .addOnSuccessListener(aVoid -> callback.onSuccess("Account synced to Cloud."))
                        .addOnFailureListener(e -> callback.onFailure("Cloud sync failed: " + e.getMessage()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }

    /**
     * Đăng nhập từ Cloud nếu tài khoản chưa có ở máy cục bộ.
     */
    public static void loginFromCloud(String username, String password, AuthCallback callback) {
        FirebaseDatabase database = FirebaseDatabase.getInstance(FirebaseConfig.DATABASE_URL);
        DatabaseReference accountRef = database.getReference(FirebaseConfig.getAccountNode(username));

        accountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String cloudPassword = snapshot.child("password").getValue(String.class);
                    // DatabaseHelper.hashPassword(input) nên khớp với cloudPassword
                    if (cloudPassword != null && cloudPassword.equals(password)) {
                        callback.onSuccess("Cloud login successful.");
                    } else {
                        callback.onFailure("Invalid cloud credentials.");
                    }
                } else {
                    callback.onFailure("Account not found on Cloud.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }

    /**
     * Đồng bộ toàn bộ dữ liệu (Upload) dựa trên username.
     */
    public static void syncAllData(Context context, String username, SyncCallback callback) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            callback.onSyncFailure("No internet connection available.");
            return;
        }

        callback.onSyncStarted();

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        int userId = dbHelper.getUserIdByUsername(username);
        
        if (userId == -1) {
            callback.onSyncFailure("Local user not found.");
            return;
        }

        List<Project> projects = dbHelper.getAllProjectsByUser(userId);

        if (projects.isEmpty()) {
            callback.onSyncSuccess("No data to upload.");
            return;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance(FirebaseConfig.DATABASE_URL);
        DatabaseReference projectsRef = database.getReference(FirebaseConfig.getUserNode(username)).child("projects");

        projectsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // 1. Kéo Expenses mới nhất từ Firebase về SQLite trước
                for (DataSnapshot projSnap : snapshot.getChildren()) {
                    Project p = projSnap.getValue(Project.class);
                    if (p != null) {
                        Project localProj = dbHelper.getProjectByCode(p.getProjectCode());
                        int localProjId = -1;
                        if (localProj != null) {
                            localProjId = localProj.getId();
                        } else {
                            p.setUserId(userId);
                            p.setIsSynced(1);
                            localProjId = (int) dbHelper.addProject(p);
                        }

                        DataSnapshot expSnapRoot = projSnap.child("expenses");
                        for (DataSnapshot expSnap : expSnapRoot.getChildren()) {
                            Expense e = expSnap.getValue(Expense.class);
                            if (e != null && localProjId != -1) {
                                if (dbHelper.getExpenseByCode(e.getExpenseCode()) == null) {
                                    e.setProjectId(localProjId);
                                    e.setIsSynced(1);
                                    dbHelper.addExpense(e);
                                }
                            }
                        }
                    }
                }

                // 2. Đẩy ngược toàn bộ dữ liệu SQLite đã cập nhật lên lại Firebase
                pushLocalDataToFirebase(context, username, userId, dbHelper, callback);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onSyncFailure("Pull from Firebase failed: " + error.getMessage());
            }
        });
    }

    private static void pushLocalDataToFirebase(Context context, String username, int userId, DatabaseHelper dbHelper, SyncCallback callback) {
        List<Project> projects = dbHelper.getAllProjectsByUser(userId);

        if (projects.isEmpty()) {
            callback.onSyncSuccess("No data to upload.");
            return;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance(FirebaseConfig.DATABASE_URL);
        DatabaseReference userRef = database.getReference(FirebaseConfig.getUserNode(username));

        Map<String, Object> backupData = new HashMap<>();
        for (Project project : projects) {
            Map<String, Object> projectMap = convertProjectToMap(project);
            List<Expense> expenses = dbHelper.getExpensesByProject(project.getId());
            Map<String, Object> expensesMap = new HashMap<>();
            for (Expense expense : expenses) {
                expensesMap.put(expense.getExpenseCode(), convertExpenseToMap(expense));
            }
            projectMap.put("expenses", expensesMap);
            backupData.put(project.getProjectCode(), projectMap);
        }

        userRef.child("projects").setValue(backupData)
            .addOnSuccessListener(aVoid -> {
                for (Project p : projects) {
                    dbHelper.markProjectSynced(p.getId());
                }
                callback.onSyncSuccess("Data synchronized to Cloud successfully!");
            })
            .addOnFailureListener(e -> callback.onSyncFailure("Sync failed: " + e.getMessage()));
    }

    /**
     * Tải toàn bộ dữ liệu từ Cloud về máy (Restore).
     */
    public static void syncDownData(Context context, String username, int localUserId, SyncCallback callback) {
        callback.onSyncStarted();
        
        FirebaseDatabase database = FirebaseDatabase.getInstance(FirebaseConfig.DATABASE_URL);
        DatabaseReference projectsRef = database.getReference(FirebaseConfig.getUserNode(username)).child("projects");

        projectsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DatabaseHelper dbHelper = new DatabaseHelper(context);
                for (DataSnapshot projSnap : snapshot.getChildren()) {
                    Project p = projSnap.getValue(Project.class);
                    if (p != null) {
                        p.setUserId(localUserId);
                        p.setIsSynced(1); // Dữ liệu từ cloud mặc định là đã sync
                        long projId = dbHelper.addProject(p);
                        
                        // Khôi phục Expenses
                        DataSnapshot expSnapRoot = projSnap.child("expenses");
                        for (DataSnapshot expSnap : expSnapRoot.getChildren()) {
                            Expense e = expSnap.getValue(Expense.class);
                            if (e != null) {
                                e.setProjectId((int) projId);
                                e.setIsSynced(1);
                                dbHelper.addExpense(e);
                            }
                        }
                    }
                }
                callback.onSyncSuccess("Data restored from Cloud successfully!");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onSyncFailure("Restore failed: " + error.getMessage());
            }
        });
    }

    private static Map<String, Object> convertProjectToMap(Project p) {
        Map<String, Object> map = new HashMap<>();
        map.put("projectCode", p.getProjectCode());
        map.put("projectName", p.getProjectName());
        map.put("description", p.getDescription());
        map.put("startDate", p.getStartDate());
        map.put("endDate", p.getEndDate());
        map.put("manager", p.getManager());
        map.put("status", p.getStatus());
        map.put("budget", p.getBudget());
        map.put("specialRequirements", p.getSpecialRequirements());
        map.put("clientInfo", p.getClientInfo());
        map.put("photoUrl", p.getPhotoUrl());
        map.put("updatedAt", p.getUpdatedAt());
        return map;
    }

    private static Map<String, Object> convertExpenseToMap(Expense e) {
        Map<String, Object> map = new HashMap<>();
        map.put("expenseCode", e.getExpenseCode());
        map.put("amount", e.getAmount());
        map.put("currency", e.getCurrency());
        map.put("date", e.getDate());
        map.put("type", e.getType());
        map.put("paymentMethod", e.getPaymentMethod());
        map.put("claimant", e.getClaimant());
        map.put("paymentStatus", e.getPaymentStatus());
        map.put("description", e.getDescription());
        map.put("location", e.getLocation());
        map.put("updatedAt", e.getUpdatedAt());
        return map;
    }
}
