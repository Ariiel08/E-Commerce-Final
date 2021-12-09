package com.example.ecommerce_final.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class NotificationModel {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String content;

    public NotificationModel(){

    }

    public NotificationModel(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
