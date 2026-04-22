package com.example.projectexpensetracker.database;
import com.example.projectexpensetracker.models.*;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ExpenseDao {

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    long insert(Expense expense);

    @Update
    int update(Expense expense);

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    int hardDelete(int expenseId);

    @Query("UPDATE expenses SET is_deleted = 1, is_synced = 0 WHERE id = :expenseId")
    int softDelete(int expenseId);

    @Query("DELETE FROM expenses WHERE project_id = :projectId")
    int hardDeleteAllByProject(int projectId);

    @Query("SELECT * FROM expenses WHERE project_id = :projectId AND is_deleted = 0 ORDER BY date DESC")
    List<Expense> getByProject(int projectId);

    @Query("SELECT * FROM expenses WHERE id = :expenseId LIMIT 1")
    Expense getById(int expenseId);

    @Query("SELECT * FROM expenses WHERE expense_code = :expenseCode LIMIT 1")
    Expense getByCode(String expenseCode);

    @Query("SELECT SUM(amount) FROM expenses WHERE project_id = :projectId AND is_deleted = 0")
    Double getTotalExpenseByProject(int projectId);

    @Query("SELECT * FROM expenses WHERE project_id = :projectId AND is_synced = 0")
    List<Expense> getUnsyncedExpenses(int projectId);

    @Query("SELECT * FROM expenses WHERE is_deleted = 1")
    List<Expense> getAllDeletedExpenses();

    @Query("UPDATE expenses SET is_synced = 1, updated_at = :updatedAt WHERE id = :expenseId")
    void markAsSynced(int expenseId, String updatedAt);
}
