package com.example.projectexpensetracker;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface UserDao {

    @Insert
    long insert(User user);

    @Query("SELECT id FROM users WHERE username = :username AND password = :password LIMIT 1")
    Integer login(String username, String password);
}
