package com.example.projectexpensetracker;

public class Project {

    private int id;
    private String projectCode;
    private String projectName;
    private String description;
    private String startDate;
    private String endDate;
    private String manager;
    private String status;           // "Active" | "Completed" | "On Hold"
    private double budget;
    private String specialRequirements; // optional
    private String clientInfo;          // optional
    private String photoUrl;            // optional
    private int userId;
    private int isSynced;            // 0 = not synced, 1 = synced
    private String createdAt;
    private String updatedAt;

    // ─── Constructors ────────────────────────────────────────────────────────────

    public Project() {}

    /** Constructor dùng khi tạo project mới (chưa có id, createdAt, updatedAt) */
    public Project(String projectCode, String projectName, String description,
                   String startDate, String endDate, String manager,
                   String status, double budget,
                   String specialRequirements, String clientInfo,
                   String photoUrl, int userId) {
        this.projectCode          = projectCode;
        this.projectName          = projectName;
        this.description          = description;
        this.startDate            = startDate;
        this.endDate              = endDate;
        this.manager              = manager;
        this.status               = status;
        this.budget               = budget;
        this.specialRequirements  = specialRequirements;
        this.clientInfo           = clientInfo;
        this.photoUrl             = photoUrl;
        this.userId               = userId;
        this.isSynced             = 0;
    }

    /** Constructor đầy đủ — dùng khi đọc từ database */
    public Project(int id, String projectCode, String projectName, String description,
                   String startDate, String endDate, String manager,
                   String status, double budget,
                   String specialRequirements, String clientInfo,
                   String photoUrl, int userId,
                   int isSynced, String createdAt, String updatedAt) {
        this.id                   = id;
        this.projectCode          = projectCode;
        this.projectName          = projectName;
        this.description          = description;
        this.startDate            = startDate;
        this.endDate              = endDate;
        this.manager              = manager;
        this.status               = status;
        this.budget               = budget;
        this.specialRequirements  = specialRequirements;
        this.clientInfo           = clientInfo;
        this.photoUrl             = photoUrl;
        this.userId               = userId;
        this.isSynced             = isSynced;
        this.createdAt            = createdAt;
        this.updatedAt            = updatedAt;
    }

    // ─── Getters & Setters ───────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getProjectCode() { return projectCode; }
    public void setProjectCode(String projectCode) { this.projectCode = projectCode; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getManager() { return manager; }
    public void setManager(String manager) { this.manager = manager; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getBudget() { return budget; }
    public void setBudget(double budget) { this.budget = budget; }

    public String getSpecialRequirements() { return specialRequirements; }
    public void setSpecialRequirements(String specialRequirements) {
        this.specialRequirements = specialRequirements;
    }

    public String getClientInfo() { return clientInfo; }
    public void setClientInfo(String clientInfo) { this.clientInfo = clientInfo; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getIsSynced() { return isSynced; }
    public void setIsSynced(int isSynced) { this.isSynced = isSynced; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    // ─── Helper ──────────────────────────────────────────────────────────────────

    /** Trả về true nếu project đã được sync lên cloud */
    public boolean isSyncedToCloud() { return isSynced == 1; }

    @Override
    public String toString() {
        return "[" + projectCode + "] " + projectName + " (" + status + ")";
    }
}