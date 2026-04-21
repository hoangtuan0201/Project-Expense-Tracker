package com.example.projectexpensetracker.utils;
import com.example.projectexpensetracker.models.*;
import com.example.projectexpensetracker.database.*;

/**
 * FirebaseConfig — Lớp cấu hình tập trung cho Firebase.
 * Bạn nên dán URL Database của mình vào đây.
 */
public class FirebaseConfig {

    // Đây là "Environment Variable" của bạn. 
    // Hãy thay thế bằng URL Realtime Database của bạn từ Firebase Console.
    public static final String DATABASE_URL = "https://expenseprojecttracker-default-rtdb.asia-southeast1.firebasedatabase.app/";

    // Logic này giúp xác định node lưu trữ dữ liệu của user
    public static String getUserNode(String username) {
        return "users/" + username.replace(".", "_");
    }

    // Logic xác định node lưu trữ thông tin đăng nhập
    public static String getAccountNode(String username) {
        return "accounts/" + username.replace(".", "_");
    }
}
