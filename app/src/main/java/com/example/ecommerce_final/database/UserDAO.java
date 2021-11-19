package com.example.ecommerce_final.database;

import androidx.room.*;
import com.example.ecommerce_final.models.User;

import java.util.List;

@Dao
public interface UserDAO {
    @Query("SELECT * FROM user ORDER BY id")
    List<User> findAll();

    @Insert
    void insert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);
}
