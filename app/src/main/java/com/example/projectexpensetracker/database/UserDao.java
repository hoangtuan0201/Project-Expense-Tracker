package com.example.projectexpensetracker.database;
import com.example.projectexpensetracker.models.*;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface UserDao {

    @Insert
    long insert(User user);

    @Query("SELECT id FROM users WHERE username = :username AND password = :password LIMIT 1")
    Integer login(String username, String password);

    @Query("SELECT username FROM users WHERE id = :userId LIMIT 1")
    String getUsernameById(int userId);

    @Query("SELECT id FROM users WHERE username = :username LIMIT 1")
    Integer getUserIdByUsername(String username);
}
