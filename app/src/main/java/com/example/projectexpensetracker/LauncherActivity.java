package com.example.projectexpensetracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        if (userId != -1) {
            // User is logged in, go to ProjectListActivity
            startActivity(new Intent(this, ProjectListActivity.class));
        } else {
            // User is not logged in, go to LoginActivity
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish(); // Finish LauncherActivity so user can't navigate back to it
    }
}
