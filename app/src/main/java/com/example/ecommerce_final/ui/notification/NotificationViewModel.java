package com.example.ecommerce_final.ui.notification;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import com.example.ecommerce_final.database.AppDatabase;
import com.example.ecommerce_final.models.NotificationModel;

import java.util.List;

public class NotificationViewModel {
    private LiveData<List<NotificationModel>> notificationListLiveData;
    private AppDatabase appDatabase;

    public NotificationViewModel(@NonNull AppDatabase appDatabase) {
        this.appDatabase = appDatabase;
        notificationListLiveData = appDatabase.notificationDAO().findAll();
    }

    public LiveData<List<NotificationModel>> getNotificationListLiveData() {
        return notificationListLiveData;
    }
}
