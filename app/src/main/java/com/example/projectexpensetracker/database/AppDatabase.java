package com.example.projectexpensetracker.database;
import com.example.projectexpensetracker.models.*;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {User.class, Project.class, Expense.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract UserDao userDao();
    public abstract ProjectDao projectDao();
    public abstract ExpenseDao expenseDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "ProjectExpenseTracker.db")
                            // Bật allowMainThreadQueries() để giữ độ tương thích với code cũ
                            // không cần cấu trúc lại toàn bộ ứng dụng sang async / rx.
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
