package com.example.ecommerce_final.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.ecommerce_final.models.*;

@Database(entities = {Category.class, Product.class, Carousel.class, NotificationModel.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "ecommerce";
    private static final Object LOCK = new Object();
    private static AppDatabase sIntance;

    public static AppDatabase getInstance(Context context) {
        if (sIntance == null) {
            synchronized (LOCK) {
                sIntance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DATABASE_NAME).build();
            }
        }
        return sIntance;
    }

    public abstract CategoryDAO categoryDAO();
    public abstract ProductDAO productDAO();
    public abstract NotificationDAO notificationDAO();
}
