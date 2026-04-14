package com.example.projectexpensetracker;

public class Expense {

    // ─── Constants — giá trị hợp lệ cho các enum field ───────────────────────────

    public static final String TYPE_TRAVEL            = "Travel";
    public static final String TYPE_EQUIPMENT         = "Equipment";
    public static final String TYPE_MATERIALS         = "Materials";
    public static final String TYPE_SERVICES          = "Services";
    public static final String TYPE_SOFTWARE          = "Software/Licenses";
    public static final String TYPE_LABOUR            = "Labour costs";
    public static final String TYPE_UTILITIES         = "Utilities";
    public static final String TYPE_MISCELLANEOUS     = "Miscellaneous";

    public static final String PAYMENT_CASH           = "Cash";
    public static final String PAYMENT_CREDIT_CARD    = "Credit Card";
    public static final String PAYMENT_BANK_TRANSFER  = "Bank Transfer";
    public static final String PAYMENT_CHEQUE         = "Cheque";

    public static final String STATUS_PAID            = "Paid";
    public static final String STATUS_PENDING         = "Pending";
    public static final String STATUS_REIMBURSED      = "Reimbursed";

    /** Danh sách hiển thị trong Spinner cho Type */
    public static final String[] EXPENSE_TYPES = {
            TYPE_TRAVEL, TYPE_EQUIPMENT, TYPE_MATERIALS, TYPE_SERVICES,
            TYPE_SOFTWARE, TYPE_LABOUR, TYPE_UTILITIES, TYPE_MISCELLANEOUS
    };

    /** Danh sách hiển thị trong Spinner cho Payment Method */
    public static final String[] PAYMENT_METHODS = {
            PAYMENT_CASH, PAYMENT_CREDIT_CARD, PAYMENT_BANK_TRANSFER, PAYMENT_CHEQUE
    };

    /** Danh sách hiển thị trong Spinner cho Payment Status */
    public static final String[] PAYMENT_STATUSES = {
            STATUS_PAID, STATUS_PENDING, STATUS_REIMBURSED
    };

    // ─── Fields ──────────────────────────────────────────────────────────────────

    private int id;
    private String expenseCode;
    private int projectId;
    private String date;
    private double amount;
    private String currency;
    private String type;            // one of EXPENSE_TYPES
    private String paymentMethod;   // one of PAYMENT_METHODS
    private String claimant;
    private String paymentStatus;   // one of PAYMENT_STATUSES
    private String description;     // optional
    private String location;        // optional
    private int isSynced;           // 0 = not synced, 1 = synced
    private String createdAt;
    private String updatedAt;

    // ─── Constructors ────────────────────────────────────────────────────────────

    public Expense() {}

    /** Constructor dùng khi tạo expense mới (chưa có id, createdAt, updatedAt) */
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