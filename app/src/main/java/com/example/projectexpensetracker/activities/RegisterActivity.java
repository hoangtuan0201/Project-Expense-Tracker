package com.example.projectexpensetracker.activities;

import com.example.projectexpensetracker.R;
import com.example.projectexpensetracker.models.*;
import com.example.projectexpensetracker.database.*;
import com.example.projectexpensetracker.adapters.*;
import com.example.projectexpensetracker.utils.*;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private Button buttonRegister;
    private LinearLayout goToLoginLayout;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        databaseHelper = new DatabaseHelper(this);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        goToLoginLayout = findViewById(R.id.goToLoginLayout);

        buttonRegister.setOnClickListener(v -> handleRegister());
        goToLoginLayout.setOnClickListener(v -> {
            // Finish current activity and go back to Login
            finish();
        });
    }

    private void handleRegister() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isRegistered = databaseHelper.register(username, password);

        if (isRegistered) {
            // Sync new account to Cloud
            User newUser = new User(username, databaseHelper.hashPassword(password));
            FirebaseSyncHelper.registerOnCloud(newUser, new FirebaseSyncHelper.AuthCallback() {
                @Override
                public void onSuccess(String message) {
                    Toast.makeText(RegisterActivity.this, "Registered and Synced to Cloud!", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFailure(String error) {
                    // Allow local success even if cloud sync fails (can sync later)
                    Toast.makeText(RegisterActivity.this, "Registered locally. Cloud sync failed.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } else {
            Toast.makeText(this, "Registration failed. Username might already exist.", Toast.LENGTH_LONG).show();
        }
    }
}
