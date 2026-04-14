package com.example.projectexpensetracker;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "expenses",
    foreignKeys = @ForeignKey(
        entity = Project.class,
        parentColumns = "id",
        childColumns = "project_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {
        @Index(value = "expense_code", unique = true),
        @Index(value = "project_id")
    }
)
public class Expense {

    // ─── Constants — giá trị hợp lệ cho các enum field ───────────────────────────

    @Ignore
    public static final String TYPE_TRAVEL            = "Travel";
    @Ignore
    public static final String TYPE_EQUIPMENT         = "Equipment";
    @Ignore
    public static final String TYPE_MATERIALS         = "Materials";
    @Ignore
    public static final String TYPE_SERVICES          = "Services";
    @Ignore
    public static final String TYPE_SOFTWARE          = "Software/Licenses";
    @Ignore
    public static final String TYPE_LABOUR            = "Labour costs";
    @Ignore
    public static final String TYPE_UTILITIES         = "Utilities";
    @Ignore
    public static final String TYPE_MISCELLANEOUS     = "Miscellaneous";

    @Ignore
    public static final String PAYMENT_CASH           = "Cash";
    @Ignore
    public static final String PAYMENT_CREDIT_CARD    = "Credit Card";
    @Ignore
    public static final String PAYMENT_BANK_TRANSFER  = "Bank Transfer";
    @Ignore
    public static final String PAYMENT_CHEQUE         = "Cheque";

    @Ignore
    public static final String STATUS_PAID            = "Paid";
    @Ignore
    public static final String STATUS_PENDING         = "Pending";
    @Ignore
    public static final String STATUS_REIMBURSED      = "Reimbursed";

    /** Danh sách hiển thị trong Spinner cho Type */
    @Ignore
    public static final String[] EXPENSE_TYPES = {
            TYPE_TRAVEL, TYPE_EQUIPMENT, TYPE_MATERIALS, TYPE_SERVICES,
            TYPE_SOFTWARE, TYPE_LABOUR, TYPE_UTILITIES, TYPE_MISCELLANEOUS
    };

    /** Danh sách hiển thị trong Spinner cho Payment Method */
    @Ignore
    public static final String[] PAYMENT_METHODS = {
            PAYMENT_CASH, PAYMENT_CREDIT_CARD, PAYMENT_BANK_TRANSFER, PAYMENT_CHEQUE
    };

    /** Danh sách hiển thị trong Spinner cho Payment Status */
    @Ignore
    public static final String[] PAYMENT_STATUSES = {
            STATUS_PAID, STATUS_PENDING, STATUS_REIMBURSED
    };

    // ─── Fields ──────────────────────────────────────────────────────────────────

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "expense_code")
    private String expenseCode;

    @ColumnInfo(name = "project_id")
    private int projectId;

    @ColumnInfo(name = "date")
    private String date;

    @ColumnInfo(name = "amount")
    private double amount;

    @ColumnInfo(name = "currency")
    private String currency;

    @ColumnInfo(name = "type")
    private String type;            // one of EXPENSE_TYPES

    @ColumnInfo(name = "payment_method")
    private String paymentMethod;   // one of PAYMENT_METHODS

    @ColumnInfo(name = "claimant")
    private String claimant;

    @ColumnInfo(name = "payment_status")
    private String paymentStatus;   // one of PAYMENT_STATUSES

    @ColumnInfo(name = "description")
    private String description;     // optional

    @ColumnInfo(name = "location")
    private String location;        // optional

    @ColumnInfo(name = "is_synced", defaultValue = "0")
    private int isSynced;           // 0 = not synced, 1 = synced

    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    private String createdAt;

    @ColumnInfo(name = "updated_at")
    private String updatedAt;

    // ─── Constructors ────────────────────────────────────────────────────────────

    @Ignore
    public Expense() {}

    /** Constructor dùng khi tạo expense mới (chưa có id, createdAt, updatedAt) */
    @Ignore
    public Expense(String expenseCode, int projectId, String date,
                   double amount, String currency, String type,
                   String paymentMethod, String claimant, String paymentStatus,
                   String description, String location) {
        this.expenseCode   = expenseCode;
        this.projectId     = projectId;
        this.date          = date;
        this.amount        = amount;
        this.currency      = currency;
        this.type          = type;
        this.paymentMethod = paymentMethod;
        this.claimant      = claimant;
        this.paymentStatus = paymentStatus;
        this.description   = description;
        this.location      = location;
        this.isSynced      = 0;
    }

    /** Constructor đầy đủ — dùng khi đọc từ database */
    public Expense(int id, String expenseCode, int projectId, String date,
                   double amount, String currency, String type,
                   String paymentMethod, String claimant, String paymentStatus,
                   String description, String location,
                   int isSynced, String createdAt, String updatedAt) {
        this.id            = id;
        this.expenseCode   = expenseCode;
        this.projectId     = projectId;
        this.date          = date;
        this.amount        = amount;
        this.currency      = currency;
        this.type          = type;
        this.paymentMethod = paymentMethod;
        this.claimant      = claimant;
        this.paymentStatus = paymentStatus;
        this.description   = description;
        this.location      = location;
        this.isSynced      = isSynced;
        this.createdAt     = createdAt;
        this.updatedAt     = updatedAt;
    }

    // ─── Getters & Setters ───────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getExpenseCode() { return expenseCode; }
    public void setExpenseCode(String expenseCode) { this.expenseCode = expenseCode; }

    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getClaimant() { return claimant; }
    public void setClaimant(String claimant) { this.claimant = claimant; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getIsSynced() { return isSynced; }
    public void setIsSynced(int isSynced) { this.isSynced = isSynced; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    // ─── Helper ──────────────────────────────────────────────────────────────────

    /** Trả về true nếu expense đã được sync lên cloud */
    public boolean isSyncedToCloud() { return isSynced == 1; }

    /** Format amount kèm currency để hiển thị, ví dụ: "1,500.00 USD" */
    public String getFormattedAmount() {
        return String.format("%,.2f %s", amount, currency);
    }

    @Override
    public String toString() {
        return "[" + expenseCode + "] " + type + " - " + getFormattedAmount() + " (" + paymentStatus + ")";
    }
}