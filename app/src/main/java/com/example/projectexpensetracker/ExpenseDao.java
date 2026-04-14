package com.example.projectexpensetracker;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ExpenseDao {

    @Insert
    long insert(Expense expense);

    @Update
    int update(Expense expense);

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    int delete(int expenseId);

    @Query("DELETE FROM expenses WHERE project_id = :projectId")
    int deleteAllByProject(int projectId);

    @Query("SELECT * FROM expenses WHERE project_id = :projectId ORDER BY date DESC")
    List<Expense> getByProject(int projectId);

    @Query("SELECT * FROM expenses WHERE id = :expenseId LIMIT 1")
    Expense getById(int expenseId);

    @Query("SELECT SUM(amount) FROM expenses WHERE project_id = :projectId")
    Double getTotalExpenseByProject(int projectId);

    @Query("SELECT * FROM expenses WHERE project_id = :projectId AND is_synced = 0")
    List<Expense> getUnsyncedExpenses(int projectId);

    @Query("UPDATE expenses SET is_synced = 1, updated_at = :updatedAt WHERE id = :expenseId")
    void markAsSynced(int expenseId, String updatedAt);
}
