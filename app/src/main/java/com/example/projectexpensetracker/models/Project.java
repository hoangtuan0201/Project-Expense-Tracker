package com.example.projectexpensetracker.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

@Entity(
    tableName = "projects",
    foreignKeys = @ForeignKey(
        entity = User.class,
        parentColumns = "id",
        childColumns = "user_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {
        @Index(value = "project_code", unique = true),
        @Index(value = "user_id")
    }
)
public class Project {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "project_code")
    private String projectCode;

    @ColumnInfo(name = "project_name")
    private String projectName;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "start_date")
    private String startDate;

    @ColumnInfo(name = "end_date")
    private String endDate;

    @ColumnInfo(name = "manager")
    private String manager;

    @ColumnInfo(name = "status")
    private String status;           // "Active" | "Completed" | "On Hold"

    @ColumnInfo(name = "budget")
    private double budget;

    @ColumnInfo(name = "special_requirements")
    private String specialRequirements; // optional

    @ColumnInfo(name = "client_info")
    private String clientInfo;          // optional

    @ColumnInfo(name = "photo_url")
    private String photoUrl;            // optional

    @ColumnInfo(name = "user_id")
    private int userId;

    @ColumnInfo(name = "is_synced", defaultValue = "0")
    private int isSynced;            // 0 = not synced, 1 = synced

    @ColumnInfo(name = "is_deleted", defaultValue = "0")
    private int isDeleted;           // 0 = active, 1 = deleted (tombstone)

    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    private String createdAt;

    @ColumnInfo(name = "updated_at")
    private String updatedAt;

    // ─── Constructors ────────────────────────────────────────────────────────────

    @Ignore
    public Project() {}

    /** Constructor used when creating a new project (without id, createdAt, updatedAt) */
    @Ignore
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

    /** Full constructor — used when reading from the database (used by Room) */
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

    public int getIsDeleted() { return isDeleted; }
    public void setIsDeleted(int isDeleted) { this.isDeleted = isDeleted; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    // ─── Helper ──────────────────────────────────────────────────────────────────

    /** Returns true if the project has been synced to the cloud */
    public boolean isSyncedToCloud() { return isSynced == 1; }

    @Override
    public String toString() {
        return "[" + projectCode + "] " + projectName + " (" + status + ")";
    }
}