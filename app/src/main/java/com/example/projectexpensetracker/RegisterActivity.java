package com.example.projectexpensetracker;

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
            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
            // Go to login after successful registration
            finish();
        } else {
            Toast.makeText(this, "Registration failed. Username might already exist.", Toast.LENGTH_LONG).show();
        }
    }
}
