package com.example.ecommerce_final.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity
public class Category {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String encodedImage;

    public Category(String name, String encodedImage) {
        this.name = name;
        this.encodedImage = encodedImage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEncodedImage() {
        return encodedImage;
    }

    public void setEncodedImage(String image) {
        this.encodedImage = image;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
