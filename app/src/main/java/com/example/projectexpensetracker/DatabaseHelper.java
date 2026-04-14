package com.example.projectexpensetracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Info
    private static final String DATABASE_NAME = "ProjectExpenseTracker.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    public static final String TABLE_USERS    = "users";
    public static final String TABLE_PROJECTS = "projects";
    public static final String TABLE_EXPENSES = "expenses";

    // Users Table Columns
    public static final String KEY_USER_ID         = "id";
    public static final String KEY_USER_USERNAME   = "username";
    public static final String KEY_USER_PASSWORD   = "password";
    public static final String KEY_USER_CREATED_AT = "created_at";

    // Projects Table Columns
    public static final String KEY_PROJECT_ID                   = "id";
    public static final String KEY_PROJECT_CODE                 = "project_code";
    public static final String KEY_PROJECT_NAME                 = "project_name";
    public static final String KEY_PROJECT_DESCRIPTION          = "description";
    public static final String KEY_PROJECT_START_DATE           = "start_date";
    public static final String KEY_PROJECT_END_DATE             = "end_date";
    public static final String KEY_PROJECT_MANAGER              = "manager";
    public static final String KEY_PROJECT_STATUS               = "status";
    public static final String KEY_PROJECT_BUDGET               = "budget";
    public static final String KEY_PROJECT_SPECIAL_REQUIREMENTS = "special_requirements";
    public static final String KEY_PROJECT_CLIENT_INFO          = "client_info";
    public static final String KEY_PROJECT_PHOTO_URL            = "photo_url";
    public static final String KEY_PROJECT_USER_ID              = "user_id";
    public static final String KEY_PROJECT_IS_SYNCED            = "is_synced";
    public static final String KEY_PROJECT_CREATED_AT           = "created_at";
    public static final String KEY_PROJECT_UPDATED_AT           = "updated_at";

    // Expenses Table Columns
    public static final String KEY_EXPENSE_ID             = "id";
    public static final String KEY_EXPENSE_CODE           = "expense_code";
    public static final String KEY_EXPENSE_PROJECT_ID     = "project_id";
    public static final String KEY_EXPENSE_DATE           = "date";
    public static final String KEY_EXPENSE_AMOUNT         = "amount";
    public static final String KEY_EXPENSE_CURRENCY       = "currency";
    public static final String KEY_EXPENSE_TYPE           = "type";
    public static final String KEY_EXPENSE_PAYMENT_METHOD = "payment_method";
    public static final String KEY_EXPENSE_CLAIMANT       = "claimant";
    public static final String KEY_EXPENSE_PAYMENT_STATUS = "payment_status";
    public static final String KEY_EXPENSE_DESCRIPTION    = "description";
    public static final String KEY_EXPENSE_LOCATION       = "location";
    public static final String KEY_EXPENSE_IS_SYNCED      = "is_synced";
    public static final String KEY_EXPENSE_CREATED_AT     = "created_at";
    public static final String KEY_EXPENSE_UPDATED_AT     = "updated_at";

    // CREATE TABLE statements
    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + "("
                    + KEY_USER_ID         + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + KEY_USER_USERNAME   + " TEXT NOT NULL UNIQUE,"
                    + KEY_USER_PASSWORD   + " TEXT NOT NULL,"
                    + KEY_USER_CREATED_AT + " TEXT DEFAULT (datetime('now'))"
                    + ")";

    private static final String CREATE_TABLE_PROJECTS =
            "CREATE TABLE " + TABLE_PROJECTS + "("
                    + KEY_PROJECT_ID                   + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + KEY_PROJECT_CODE                 + " TEXT NOT NULL UNIQUE,"
                    + KEY_PROJECT_NAME                 + " TEXT NOT NULL,"
                    + KEY_PROJECT_DESCRIPTION          + " TEXT NOT NULL,"
                    + KEY_PROJECT_START_DATE           + " TEXT NOT NULL,"
                    + KEY_PROJECT_END_DATE             + " TEXT NOT NULL,"
                    + KEY_PROJECT_MANAGER              + " TEXT NOT NULL,"
                    + KEY_PROJECT_STATUS               + " TEXT NOT NULL CHECK(" + KEY_PROJECT_STATUS + " IN ('Active','Completed','On Hold')),"
                    + KEY_PROJECT_BUDGET               + " REAL NOT NULL,"
                    + KEY_PROJECT_SPECIAL_REQUIREMENTS + " TEXT,"
                    + KEY_PROJECT_CLIENT_INFO          + " TEXT,"
                    + KEY_PROJECT_PHOTO_URL            + " TEXT,"
                    + KEY_PROJECT_USER_ID              + " INTEGER NOT NULL,"
                    + KEY_PROJECT_IS_SYNCED            + " INTEGER DEFAULT 0,"
                    + KEY_PROJECT_CREATED_AT           + " TEXT DEFAULT (datetime('now')),"
                    + KEY_PROJECT_UPDATED_AT           + " TEXT,"
                    + "FOREIGN KEY (" + KEY_PROJECT_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + KEY_USER_ID + ") ON DELETE CASCADE"
                    + ")";

    private static final String CREATE_TABLE_EXPENSES =
            "CREATE TABLE " + TABLE_EXPENSES + "("
                    + KEY_EXPENSE_ID             + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + KEY_EXPENSE_CODE           + " TEXT NOT NULL UNIQUE,"
                    + KEY_EXPENSE_PROJECT_ID     + " INTEGER NOT NULL,"
                    + KEY_EXPENSE_DATE           + " TEXT NOT NULL,"
                    + KEY_EXPENSE_AMOUNT         + " REAL NOT NULL,"
                    + KEY_EXPENSE_CURRENCY       + " TEXT NOT NULL,"
                    + KEY_EXPENSE_TYPE           + " TEXT NOT NULL CHECK(" + KEY_EXPENSE_TYPE + " IN ("
                    + "'Travel','Equipment','Materials','Services',"
                    + "'Software/Licenses','Labour costs','Utilities','Miscellaneous')),"
                    + KEY_EXPENSE_PAYMENT_METHOD + " TEXT NOT NULL CHECK(" + KEY_EXPENSE_PAYMENT_METHOD + " IN ("
                    + "'Cash','Credit Card','Bank Transfer','Cheque')),"
                    + KEY_EXPENSE_CLAIMANT       + " TEXT NOT NULL,"
                    + KEY_EXPENSE_PAYMENT_STATUS + " TEXT NOT NULL CHECK(" + KEY_EXPENSE_PAYMENT_STATUS + " IN ("
                    + "'Paid','Pending','Reimbursed')),"
                    + KEY_EXPENSE_DESCRIPTION    + " TEXT,"
                    + KEY_EXPENSE_LOCATION       + " TEXT,"
                    + KEY_EXPENSE_IS_SYNCED      + " INTEGER DEFAULT 0,"
                    + KEY_EXPENSE_CREATED_AT     + " TEXT DEFAULT (datetime('now')),"
                    + KEY_EXPENSE_UPDATED_AT     + " TEXT,"
                    + "FOREIGN KEY (" + KEY_EXPENSE_PROJECT_ID + ") REFERENCES " + TABLE_PROJECTS + "(" + KEY_PROJECT_ID + ") ON DELETE CASCADE"
                    + ")";

    // ─── Constructor ─────────────────────────────────────────────────────────────

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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROJECTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // Bật hỗ trợ Foreign Key mỗi khi mở database
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  AUTHENTICATION
    // ═══════════════════════════════════════════════════════════════════════════

    /** Đăng ký user mới. Trả về true nếu thành công. */
    public boolean register(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_USERNAME, username);
        values.put(KEY_USER_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    /** Đăng nhập. Trả về userId nếu thành công, -1 nếu thất bại. */
    public int login(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + KEY_USER_ID + " FROM " + TABLE_USERS
                        + " WHERE " + KEY_USER_USERNAME + "=? AND " + KEY_USER_PASSWORD + "=?",
                new String[]{username, password}
        );

        if (cursor.moveToFirst()) {
            int userId = cursor.getInt(0);
            cursor.close();
            return userId;
        }
        cursor.close();
        return -1;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  PROJECT — CREATE
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Thêm project mới vào database.
     * @return id của row vừa insert, -1 nếu thất bại.
     */
    public long addProject(Project project) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = buildProjectContentValues(project);
        long id = db.insert(TABLE_PROJECTS, null, values);
        return id;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  PROJECT — READ
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Lấy tất cả projects của một user, sắp xếp theo thời gian tạo mới nhất.
     */
    public List<Project> getAllProjectsByUser(int userId) {
        List<Project> projects = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_PROJECTS,
                null,
                KEY_PROJECT_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null,
                KEY_PROJECT_CREATED_AT + " DESC"
        );

        if (cursor.moveToFirst()) {
            do {
                projects.add(cursorToProject(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return projects;
    }

    /**
     * Lấy một project theo id.
     * @return Project nếu tìm thấy, null nếu không.
     */
    public Project getProjectById(int projectId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_PROJECTS,
                null,
                KEY_PROJECT_ID + "=?",
                new String[]{String.valueOf(projectId)},
                null, null, null
        );

        Project project = null;
        if (cursor.moveToFirst()) {
            project = cursorToProject(cursor);
        }
        cursor.close();
        return project;
    }

    /**
     * Tìm kiếm project theo tên hoặc mô tả (feature d).
     * @param keyword  từ khoá tìm kiếm
     * @param userId   chỉ tìm trong projects của user này
     */
    public List<Project> searchProjects(String keyword, int userId) {
        List<Project> projects = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String likeQuery = "%" + keyword + "%";
        Cursor cursor = db.query(
                TABLE_PROJECTS,
                null,
                KEY_PROJECT_USER_ID + "=? AND ("
                        + KEY_PROJECT_NAME + " LIKE ? OR "
                        + KEY_PROJECT_DESCRIPTION + " LIKE ? OR "
                        + KEY_PROJECT_CODE + " LIKE ?)",
                new String[]{String.valueOf(userId), likeQuery, likeQuery, likeQuery},
                null, null,
                KEY_PROJECT_NAME + " ASC"
        );

        if (cursor.moveToFirst()) {
            do {
                projects.add(cursorToProject(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return projects;
    }

    /**
     * Tìm kiếm nâng cao theo nhiều tiêu chí (feature d — advanced search).
     * Bất kỳ tham số nào truyền null sẽ bị bỏ qua.
     *
     * @param userId   bắt buộc
     * @param status   "Active" | "Completed" | "On Hold" | null
     * @param manager  tên manager | null
     * @param fromDate ngày bắt đầu dạng "yyyy-MM-dd" | null
     * @param toDate   ngày kết thúc dạng "yyyy-MM-dd" | null
     */
    public List<Project> advancedSearchProjects(int userId, String status,
                                                String manager,
                                                String fromDate, String toDate) {
        List<Project> projects = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder where = new StringBuilder(KEY_PROJECT_USER_ID + "=?");
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(userId));

        if (status != null && !status.isEmpty()) {
            where.append(" AND ").append(KEY_PROJECT_STATUS).append("=?");
            args.add(status);
        }
        if (manager != null && !manager.isEmpty()) {
            where.append(" AND ").append(KEY_PROJECT_MANAGER).append(" LIKE ?");
            args.add("%" + manager + "%");
        }
        if (fromDate != null && !fromDate.isEmpty()) {
            where.append(" AND ").append(KEY_PROJECT_START_DATE).append(">=?");
            args.add(fromDate);
        }
        if (toDate != null && !toDate.isEmpty()) {
            where.append(" AND ").append(KEY_PROJECT_END_DATE).append("<=?");
            args.add(toDate);
        }

        Cursor cursor = db.query(
                TABLE_PROJECTS,
                null,
                where.toString(),
                args.toArray(new String[0]),
                null, null,
                KEY_PROJECT_START_DATE + " DESC"
        );

        if (cursor.moveToFirst()) {
            do {
                projects.add(cursorToProject(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return projects;
    }

    /**
     * Lấy tất cả projects chưa được sync (dùng cho feature e — upload).
     */
    public List<Project> getUnsyncedProjects(int userId) {
        List<Project> projects = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_PROJECTS,
                null,
                KEY_PROJECT_USER_ID + "=? AND " + KEY_PROJECT_IS_SYNCED + "=0",
                new String[]{String.valueOf(userId)},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            do {
                projects.add(cursorToProject(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return projects;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  PROJECT — UPDATE
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Cập nhật thông tin project.
     * @return số rows bị ảnh hưởng (1 = thành công, 0 = thất bại).
     */
    public int updateProject(Project project) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = buildProjectContentValues(project);
        // Ghi lại thời gian cập nhật
        values.put(KEY_PROJECT_UPDATED_AT, getCurrentDateTime());

        return db.update(
                TABLE_PROJECTS,
                values,
                KEY_PROJECT_ID + "=?",
                new String[]{String.valueOf(project.getId())}
        );
    }

    /**
     * Đánh dấu project đã được sync lên cloud.
     */
    public void markProjectSynced(int projectId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_PROJECT_IS_SYNCED, 1);
        values.put(KEY_PROJECT_UPDATED_AT, getCurrentDateTime());

        db.update(TABLE_PROJECTS, values,
                KEY_PROJECT_ID + "=?",
                new String[]{String.valueOf(projectId)});
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  PROJECT — DELETE
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Xoá một project (và tất cả expenses liên quan nhờ ON DELETE CASCADE).
     * @return số rows bị xoá.
     */
    public int deleteProject(int projectId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_PROJECTS,
                KEY_PROJECT_ID + "=?",
                new String[]{String.valueOf(projectId)});
    }

    /**
     * Xoá toàn bộ projects của một user và reset database về trạng thái ban đầu.
     */
    public void resetDatabase(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PROJECTS,
                KEY_PROJECT_USER_ID + "=?",
                new String[]{String.valueOf(userId)});
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  EXPENSE — CREATE
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Thêm expense mới vào database.
     * @return id của row vừa insert, -1 nếu thất bại.
     */
    public long addExpense(Expense expense) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = buildExpenseContentValues(expense);
        return db.insert(TABLE_EXPENSES, null, values);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  EXPENSE — READ
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Lấy tất cả expenses của một project, sắp xếp theo ngày mới nhất.
     */
    public List<Expense> getExpensesByProject(int projectId) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_EXPENSES,
                null,
                KEY_EXPENSE_PROJECT_ID + "=?",
                new String[]{String.valueOf(projectId)},
                null, null,
                KEY_EXPENSE_DATE + " DESC"
        );

        if (cursor.moveToFirst()) {
            do {
                expenses.add(cursorToExpense(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return expenses;
    }

    /**
     * Lấy một expense theo id.
     * @return Expense nếu tìm thấy, null nếu không.
     */
    public Expense getExpenseById(int expenseId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_EXPENSES,
                null,
                KEY_EXPENSE_ID + "=?",
                new String[]{String.valueOf(expenseId)},
                null, null, null
        );

        Expense expense = null;
        if (cursor.moveToFirst()) {
            expense = cursorToExpense(cursor);
        }
        cursor.close();
        return expense;
    }

    /**
     * Tính tổng chi phí của một project.
     * @return tổng amount (double), 0.0 nếu chưa có expense nào.
     */
    public double getTotalExpenseByProject(int projectId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + KEY_EXPENSE_AMOUNT + ") FROM " + TABLE_EXPENSES
                        + " WHERE " + KEY_EXPENSE_PROJECT_ID + "=?",
                new String[]{String.valueOf(projectId)}
        );

        double total = 0.0;
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    /**
     * Lấy tất cả expenses chưa sync của một project.
     */
    public List<Expense> getUnsyncedExpenses(int projectId) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_EXPENSES,
                null,
                KEY_EXPENSE_PROJECT_ID + "=? AND " + KEY_EXPENSE_IS_SYNCED + "=0",
                new String[]{String.valueOf(projectId)},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            do {
                expenses.add(cursorToExpense(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return expenses;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  EXPENSE — UPDATE
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Cập nhật thông tin expense.
     * @return số rows bị ảnh hưởng.
     */
    public int updateExpense(Expense expense) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = buildExpenseContentValues(expense);
        values.put(KEY_EXPENSE_UPDATED_AT, getCurrentDateTime());

        return db.update(
                TABLE_EXPENSES,
                values,
                KEY_EXPENSE_ID + "=?",
                new String[]{String.valueOf(expense.getId())}
        );
    }

    /**
     * Đánh dấu expense đã được sync lên cloud.
     */
    public void markExpenseSynced(int expenseId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_EXPENSE_IS_SYNCED, 1);
        values.put(KEY_EXPENSE_UPDATED_AT, getCurrentDateTime());

        db.update(TABLE_EXPENSES, values,
                KEY_EXPENSE_ID + "=?",
                new String[]{String.valueOf(expenseId)});
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  EXPENSE — DELETE
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Xoá một expense theo id.
     * @return số rows bị xoá.
     */
    public int deleteExpense(int expenseId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_EXPENSES,
                KEY_EXPENSE_ID + "=?",
                new String[]{String.valueOf(expenseId)});
    }

    /**
     * Xoá toàn bộ expenses của một project.
     */
    public int deleteAllExpensesByProject(int projectId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_EXPENSES,
                KEY_EXPENSE_PROJECT_ID + "=?",
                new String[]{String.valueOf(projectId)});
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    /** Map Project object → ContentValues để dùng cho insert/update. */
    private ContentValues buildProjectContentValues(Project p) {
        ContentValues values = new ContentValues();
        values.put(KEY_PROJECT_CODE,                 p.getProjectCode());
        values.put(KEY_PROJECT_NAME,                 p.getProjectName());
        values.put(KEY_PROJECT_DESCRIPTION,          p.getDescription());
        values.put(KEY_PROJECT_START_DATE,           p.getStartDate());
        values.put(KEY_PROJECT_END_DATE,             p.getEndDate());
        values.put(KEY_PROJECT_MANAGER,              p.getManager());
        values.put(KEY_PROJECT_STATUS,               p.getStatus());
        values.put(KEY_PROJECT_BUDGET,               p.getBudget());
        values.put(KEY_PROJECT_SPECIAL_REQUIREMENTS, p.getSpecialRequirements());
        values.put(KEY_PROJECT_CLIENT_INFO,          p.getClientInfo());
        values.put(KEY_PROJECT_PHOTO_URL,            p.getPhotoUrl());
        values.put(KEY_PROJECT_USER_ID,              p.getUserId());
        values.put(KEY_PROJECT_IS_SYNCED,            p.getIsSynced());
        return values;
    }

    /** Map Expense object → ContentValues để dùng cho insert/update. */
    private ContentValues buildExpenseContentValues(Expense e) {
        ContentValues values = new ContentValues();
        values.put(KEY_EXPENSE_CODE,           e.getExpenseCode());
        values.put(KEY_EXPENSE_PROJECT_ID,     e.getProjectId());
        values.put(KEY_EXPENSE_DATE,           e.getDate());
        values.put(KEY_EXPENSE_AMOUNT,         e.getAmount());
        values.put(KEY_EXPENSE_CURRENCY,       e.getCurrency());
        values.put(KEY_EXPENSE_TYPE,           e.getType());
        values.put(KEY_EXPENSE_PAYMENT_METHOD, e.getPaymentMethod());
        values.put(KEY_EXPENSE_CLAIMANT,       e.getClaimant());
        values.put(KEY_EXPENSE_PAYMENT_STATUS, e.getPaymentStatus());
        values.put(KEY_EXPENSE_DESCRIPTION,    e.getDescription());
        values.put(KEY_EXPENSE_LOCATION,       e.getLocation());
        values.put(KEY_EXPENSE_IS_SYNCED,      e.getIsSynced());
        return values;
    }

    /** Đọc một Project từ vị trí hiện tại của Cursor. */
    private Project cursorToProject(Cursor cursor) {
        return new Project(
                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PROJECT_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROJECT_CODE)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROJECT_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROJECT_DESCRIPTION)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROJECT_START_DATE)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROJECT_END_DATE)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROJECT_MANAGER)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROJECT_STATUS)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_PROJECT_BUDGET)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROJECT_SPECIAL_REQUIREMENTS)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROJECT_CLIENT_INFO)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROJECT_PHOTO_URL)),
                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PROJECT_USER_ID)),
                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PROJECT_IS_SYNCED)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROJECT_CREATED_AT)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROJECT_UPDATED_AT))
        );
    }

    /** Đọc một Expense từ vị trí hiện tại của Cursor. */
    private Expense cursorToExpense(Cursor cursor) {
        return new Expense(
                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_EXPENSE_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_EXPENSE_CODE)),
                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_EXPENSE_PROJECT_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_EXPENSE_DATE)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_EXPENSE_AMOUNT)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_EXPENSE_CURRENCY)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_EXPENSE_TYPE)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_EXPENSE_PAYMENT_METHOD)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_EXPENSE_CLAIMANT)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_EXPENSE_PAYMENT_STATUS)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_EXPENSE_DESCRIPTION)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_EXPENSE_LOCATION)),
                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_EXPENSE_IS_SYNCED)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_EXPENSE_CREATED_AT)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_EXPENSE_UPDATED_AT))
        );
    }

    /** Trả về datetime hiện tại theo format SQLite. */
    private String getCurrentDateTime() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                java.util.Locale.getDefault())
                .format(new java.util.Date());
    }
}