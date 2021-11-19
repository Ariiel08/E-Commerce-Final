package com.example.ecommerce_final.models;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class CategoryProducts {
    @Embedded
    public Category category;
    @Relation(parentColumn = "id", entityColumn = "categoryId")
    public List<Product> product;
}
