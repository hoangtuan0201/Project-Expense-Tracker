package com.example.projectexpensetracker.activities;

import com.example.projectexpensetracker.R;
import com.example.projectexpensetracker.models.*;
import com.example.projectexpensetracker.database.*;
import com.example.projectexpensetracker.adapters.*;
import com.example.projectexpensetracker.utils.*;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonLogin;
    private LinearLayout goToRegisterLayout;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        databaseHelper = new DatabaseHelper(this);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        goToRegisterLayout = findViewById(R.id.goToRegisterLayout);

        buttonLogin.setOnClickListener(v -> handleLogin());
        goToRegisterLayout.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void handleLogin() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = databaseHelper.login(username, password);

        if (userId != -1) {
            proceedToMain(userId);
        } else {
            // Try cloud login if account is not found locally
            checkCloudLogin(username, password);
        }
    }

    private void checkCloudLogin(String username, String password) {
        android.app.ProgressDialog progress = new android.app.ProgressDialog(this);
        progress.setTitle("Cloud Login");
        progress.setMessage("Checking credentials and restoring data...");
        progress.setCancelable(false);
        progress.show();

        FirebaseSyncHelper.loginFromCloud(username, databaseHelper.hashPassword(password), new FirebaseSyncHelper.AuthCallback() {
            @Override
            public void onSuccess(String message) {
                // Register local user profile
                User cloudUser = new User(username, databaseHelper.hashPassword(password));
                databaseHelper.forceRegisterUser(cloudUser);
                int newUserId = databaseHelper.login(username, password);

                // Restore project and expense data
                FirebaseSyncHelper.syncDownData(LoginActivity.this, username, newUserId, new FirebaseSyncHelper.SyncCallback() {
                    @Override public void onSyncStarted() {}
                    @Override public void onSyncSuccess(String message) {
                        progress.dismiss();
                        Toast.makeText(LoginActivity.this, "Data restored from Cloud!", Toast.LENGTH_SHORT).show();
                        proceedToMain(newUserId);
                    }
                    @Override public void onSyncFailure(String error) {
                        progress.dismiss();
                        Toast.makeText(LoginActivity.this, "Data restoration failed: " + error, Toast.LENGTH_LONG).show();
                        proceedToMain(newUserId); // Allow login even if restore fails
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                progress.dismiss();
                Toast.makeText(LoginActivity.this, "Invalid credentials or Account not found.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void proceedToMain(int userId) {
        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("user_id", userId);
        editor.apply();

        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, ProjectListActivity.class));
        finish();
    }
}
