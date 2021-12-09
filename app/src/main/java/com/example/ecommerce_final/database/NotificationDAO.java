package com.example.ecommerce_final.database;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.ecommerce_final.models.NotificationModel;

import java.util.List;

@Dao
public interface NotificationDAO {
    @Query("SELECT * FROM NotificationModel ORDER BY id")
    LiveData<List<NotificationModel>> findAll();

    @Query("SELECT * FROM NotificationModel WHERE id = :id")
    NotificationModel find(int id);

    @Insert
    void insert(NotificationModel notificationModel);

    @Update
    void update(NotificationModel notificationModel);

    @Transaction
    @Delete
    void delete(NotificationModel notificationModel);

    @Transaction
    @Query("DELETE FROM notificationmodel")
    void deleteAll();
}
