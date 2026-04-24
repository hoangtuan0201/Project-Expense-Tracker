package com.example.projectexpensetracker.utils;
import com.example.projectexpensetracker.models.*;
import com.example.projectexpensetracker.database.*;

/**
 * FirebaseConfig — Centralized configuration class for Firebase.
 * You should paste your Database URL here.
 */
public class FirebaseConfig {

    // This is your "Environment Variable". 
    // Please replace this with your Realtime Database URL from the Firebase Console.
    public static final String DATABASE_URL = "https://expenseprojecttracker-default-rtdb.asia-southeast1.firebasedatabase.app/";

    // This logic helps determine the data storage node for the user.
    public static String getUserNode(String username) {
        return "users/" + username.replace(".", "_");
    }

    // Logic for determining the account storage node.
    public static String getAccountNode(String username) {
        return "accounts/" + username.replace(".", "_");
    }
}
