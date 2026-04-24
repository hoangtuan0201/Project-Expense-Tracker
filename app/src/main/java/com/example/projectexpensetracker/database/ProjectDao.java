package com.example.projectexpensetracker.database;
import com.example.projectexpensetracker.models.*;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;
import androidx.room.RawQuery;

import java.util.List;

@Dao
public interface ProjectDao {

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    long insert(Project project);

    @Query("SELECT * FROM projects WHERE project_code = :projectCode LIMIT 1")
    Project getByCode(String projectCode);

    @Update
    int update(Project project);

    @Query("DELETE FROM projects WHERE id = :projectId")
    int hardDelete(int projectId);

    @Query("UPDATE projects SET is_deleted = 1, is_synced = 0 WHERE id = :projectId")
    int softDelete(int projectId);

    @Query("SELECT * FROM projects WHERE user_id = :userId AND is_deleted = 0 ORDER BY created_at DESC")
    List<Project> getAllByUser(int userId);

    @Query("SELECT * FROM projects WHERE id = :projectId LIMIT 1")
    Project getById(int projectId);

    @Query("SELECT * FROM projects WHERE user_id = :userId AND is_deleted = 0 AND (project_name LIKE :keyword OR description LIKE :keyword OR project_code LIKE :keyword) ORDER BY project_name ASC")
    List<Project> searchProjects(int userId, String keyword);

    @Query("SELECT * FROM projects WHERE user_id = :userId AND is_synced = 0")
    List<Project> getUnsyncedProjects(int userId);

    @Query("SELECT * FROM projects WHERE is_deleted = 1")
    List<Project> getAllDeletedProjects();

    @Query("UPDATE projects SET is_synced = 1, updated_at = :updatedAt WHERE id = :projectId")
    void markAsSynced(int projectId, String updatedAt);

    // Use RawQuery to support advanced searching with optional criteria
    @RawQuery
    List<Project> advancedSearch(SupportSQLiteQuery query);
}
