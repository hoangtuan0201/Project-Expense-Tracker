package com.example.projectexpensetracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Info
    private static final String DATABASE_NAME = "ProjectExpenseTracker.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_PROJECTS = "projects";
    public static final String TABLE_EXPENSES = "expenses";

    // Users Table Columns
    public static final String KEY_USER_ID = "id";
    public static final String KEY_USER_USERNAME = "username";
    public static final String KEY_USER_PASSWORD = "password";
    public static final String KEY_USER_CREATED_AT = "created_at";

    // Projects Table Columns
    public static final String KEY_PROJECT_ID = "id";
    public static final String KEY_PROJECT_CODE = "project_code";
    public static final String KEY_PROJECT_NAME = "project_name";
    public static final String KEY_PROJECT_DESCRIPTION = "description";
    public static final String KEY_PROJECT_START_DATE = "start_date";
    public static final String KEY_PROJECT_END_DATE = "end_date";
    public static final String KEY_PROJECT_MANAGER = "manager";
    public static final String KEY_PROJECT_STATUS = "status";
    public static final String KEY_PROJECT_BUDGET = "budget";
    public static final String KEY_PROJECT_SPECIAL_REQUIREMENTS = "special_requirements";
    public static final String KEY_PROJECT_CLIENT_INFO = "client_info";
    public static final String KEY_PROJECT_PHOTO_URL = "photo_url";
    public static final String KEY_PROJECT_USER_ID = "user_id";
    public static final String KEY_PROJECT_IS_SYNCED = "is_synced";
    public static final String KEY_PROJECT_CREATED_AT = "created_at";
    public static final String KEY_PROJECT_UPDATED_AT = "updated_at";

    // Expenses Table Columns
    public static final String KEY_EXPENSE_ID = "id";
    public static final String KEY_EXPENSE_CODE = "expense_code";
    public static final String KEY_EXPENSE_PROJECT_ID = "project_id";
    public static final String KEY_EXPENSE_DATE = "date";
    public static final String KEY_EXPENSE_AMOUNT = "amount";
    public static final String KEY_EXPENSE_CURRENCY = "currency";
    public static final String KEY_EXPENSE_TYPE = "type";
    public static final String KEY_EXPENSE_PAYMENT_METHOD = "payment_method";
    public static final String KEY_EXPENSE_CLAIMANT = "claimant";
    public static final String KEY_EXPENSE_PAYMENT_STATUS = "payment_status";
    public static final String KEY_EXPENSE_DESCRIPTION = "description";
    public static final String KEY_EXPENSE_LOCATION = "location";
    public static final String KEY_EXPENSE_IS_SYNCED = "is_synced";
    public static final String KEY_EXPENSE_CREATED_AT = "created_at";
    public static final String KEY_EXPENSE_UPDATED_AT = "updated_at";

    // CREATE TABLE statements
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + KEY_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_USER_USERNAME + " TEXT NOT NULL UNIQUE,"
            + KEY_USER_PASSWORD + " TEXT NOT NULL,"
            + KEY_USER_CREATED_AT + " TEXT DEFAULT (datetime('now'))" + ")";

    private static final String CREATE_TABLE_PROJECTS = "CREATE TABLE " + TABLE_PROJECTS + "("
            + KEY_PROJECT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_PROJECT_CODE + " TEXT NOT NULL UNIQUE,"
            + KEY_PROJECT_NAME + " TEXT NOT NULL,"
            + KEY_PROJECT_DESCRIPTION + " TEXT NOT NULL,"
            + KEY_PROJECT_START_DATE + " TEXT NOT NULL,"
            + KEY_PROJECT_END_DATE + " TEXT NOT NULL,"
            + KEY_PROJECT_MANAGER + " TEXT NOT NULL,"
            + KEY_PROJECT_STATUS + " TEXT NOT NULL CHECK(" + KEY_PROJECT_STATUS + " IN ('Active','Completed','On Hold')),"
            + KEY_PROJECT_BUDGET + " REAL NOT NULL,"
            + KEY_PROJECT_SPECIAL_REQUIREMENTS + " TEXT,"
            + KEY_PROJECT_CLIENT_INFO + " TEXT,"
            + KEY_PROJECT_PHOTO_URL + " TEXT,"
            + KEY_PROJECT_USER_ID + " INTEGER NOT NULL,"
            + KEY_PROJECT_IS_SYNCED + " INTEGER DEFAULT 0,"
            + KEY_PROJECT_CREATED_AT + " TEXT DEFAULT (datetime('now')),"
            + KEY_PROJECT_UPDATED_AT + " TEXT,"
            + "FOREIGN KEY (" + KEY_PROJECT_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + KEY_USER_ID + ") ON DELETE CASCADE"
            + ")";

    private static final String CREATE_TABLE_EXPENSES = "CREATE TABLE " + TABLE_EXPENSES + "("
            + KEY_EXPENSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_EXPENSE_CODE + " TEXT NOT NULL UNIQUE,"
            + KEY_EXPENSE_PROJECT_ID + " INTEGER NOT NULL,"
            + KEY_EXPENSE_DATE + " TEXT NOT NULL,"
            + KEY_EXPENSE_AMOUNT + " REAL NOT NULL,"
            + KEY_EXPENSE_CURRENCY + " TEXT NOT NULL,"
            + KEY_EXPENSE_TYPE + " TEXT NOT NULL CHECK(" + KEY_EXPENSE_TYPE + " IN ("
            + "'Travel','Equipment','Materials','Services',"
            + "'Software/Licenses','Labour costs','Utilities','Miscellaneous'"
            + ")),"
            + KEY_EXPENSE_PAYMENT_METHOD + " TEXT NOT NULL CHECK(" + KEY_EXPENSE_PAYMENT_METHOD + " IN ("
            + "'Cash','Credit Card','Bank Transfer','Cheque'"
            + ")),"
            + KEY_EXPENSE_CLAIMANT + " TEXT NOT NULL,"
            + KEY_EXPENSE_PAYMENT_STATUS + " TEXT NOT NULL CHECK(" + KEY_EXPENSE_PAYMENT_STATUS + " IN ("
            + "'Paid','Pending','Reimbursed'"
            + ")),"
            + KEY_EXPENSE_DESCRIPTION + " TEXT,"
            + KEY_EXPENSE_LOCATION + " TEXT,"
            + KEY_EXPENSE_IS_SYNCED + " INTEGER DEFAULT 0,"
            + KEY_EXPENSE_CREATED_AT + " TEXT DEFAULT (datetime('now')),"
            + KEY_EXPENSE_UPDATED_AT + " TEXT,"
            + "FOREIGN KEY (" + KEY_EXPENSE_PROJECT_ID + ") REFERENCES " + TABLE_PROJECTS + "(" + KEY_PROJECT_ID + ") ON DELETE CASCADE"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_PROJECTS);
        db.execSQL(CREATE_TABLE_EXPENSES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROJECTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        // Create tables again
        onCreate(db);
    }

    /** AUTHENTICATION REPOSITORY METHODS */
    public boolean register(String username, String password){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);

        long result = db.insert("users", null, values);
        return result != -1;
    }
    public int login(String username, String password){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT id FROM users WHERE username=? AND password=?",
                new String[]{username, password}
        );

        if(cursor.moveToFirst()){
            return cursor.getInt(0);
        }
        return -1;
    }


}
