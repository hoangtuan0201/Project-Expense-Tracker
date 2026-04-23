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
 * FirebaseSyncHelper — Data synchronization logic and account management on the Cloud.
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
     * Register account on the Cloud for cross-device synchronization.
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
                    accountInfo.put("password", user.getPassword()); // Already hashed from DatabaseHelper
                    
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
     * Login from the Cloud if the account is not available on the local device.
     */
    public static void loginFromCloud(String username, String password, AuthCallback callback) {
        FirebaseDatabase database = FirebaseDatabase.getInstance(FirebaseConfig.DATABASE_URL);
        DatabaseReference accountRef = database.getReference(FirebaseConfig.getAccountNode(username));

        accountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String cloudPassword = snapshot.child("password").getValue(String.class);
                    // DatabaseHelper.hashPassword(input) should match cloudPassword
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
     * Synchronize all data (Upload) based on username.
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
                // 1. Pull the latest Expenses from Firebase to SQLite first
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
                                Expense existing = dbHelper.getExpenseByCode(e.getExpenseCode());
                                if (existing != null && existing.getIsDeleted() == 1) {
                                    // Skip if deleted locally (do not pull back)
                                    continue;
                                }
                                if (existing == null) {
                                    e.setProjectId(localProjId);
                                    e.setIsSynced(1);
                                    dbHelper.addExpense(e);
                                }
                            }
                        }
                    }
                }

                // 2. Push all updated SQLite data back to Firebase
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
                // Only push expenses that have not been deleted
                if (expense.getIsDeleted() == 0) {
                    expensesMap.put(expense.getExpenseCode(), convertExpenseToMap(expense));
                }
            }
            projectMap.put("expenses", expensesMap);
            backupData.put(project.getProjectCode(), projectMap);
        }

        userRef.child("projects").setValue(backupData)
            .addOnSuccessListener(aVoid -> {
                for (Project p : projects) {
                    dbHelper.markProjectSynced(p.getId());
                }
                // Cleanup: Permanently delete items marked for deletion after successful sync
                cleanupDeletedItems(dbHelper);
                callback.onSyncSuccess("Data synchronized to Cloud successfully!");
            })
            .addOnFailureListener(e -> callback.onSyncFailure("Sync failed: " + e.getMessage()));
    }

    private static void cleanupDeletedItems(DatabaseHelper dbHelper) {
        List<Expense> deletedExpenses = dbHelper.getAllDeletedExpenses();
        for (Expense e : deletedExpenses) {
            dbHelper.hardDeleteExpense(e.getId());
        }
        List<Project> deletedProjects = dbHelper.getAllDeletedProjects();
        for (Project p : deletedProjects) {
            dbHelper.hardDeleteProject(p.getId());
        }
    }

    /**
     * Download all data from the Cloud to the device (Restore).
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
                        Project existingProj = dbHelper.getProjectByCode(p.getProjectCode());
                        int projId;
                        if (existingProj != null) {
                            projId = existingProj.getId();
                        } else {
                            p.setUserId(localUserId);
                            p.setIsSynced(1); 
                            projId = (int) dbHelper.addProject(p);
                        }
                        
                        // Restore Expenses (Deduplicate by Code)
                        DataSnapshot expSnapRoot = projSnap.child("expenses");
                        for (DataSnapshot expSnap : expSnapRoot.getChildren()) {
                            Expense e = expSnap.getValue(Expense.class);
                            if (e != null && projId != -1) {
                                if (dbHelper.getExpenseByCode(e.getExpenseCode()) == null) {
                                    e.setProjectId(projId);
                                    e.setIsSynced(1);
                                    dbHelper.addExpense(e);
                                }
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
