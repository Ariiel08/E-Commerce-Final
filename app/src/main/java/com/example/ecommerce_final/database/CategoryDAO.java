package com.example.ecommerce_final.database;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.ecommerce_final.models.Category;
import com.example.ecommerce_final.models.CategoryProducts;

import java.util.List;

@Dao
public interface CategoryDAO {
//    @Query("SELECT * FROM category ORDER BY id")
//    LiveData<List<Category>> findAll();

    @Transaction
    @Query("SELECT * FROM category ORDER BY id")
    List<CategoryProducts> getCategories();

    @Query("SELECT * FROM category WHERE id = :id")
    Category find(int id);

    @Query("SELECT * FROM category WHERE name = :name")
    Category findByName(String name);

    @Query("SELECT * FROM category WHERE id = :id")
    LiveData<CategoryProducts> findProductCategory(int id);

    @Transaction
    @Query("SELECT * FROM category ORDER BY id")
    LiveData<List<CategoryProducts>> findAll();

    @Insert
    void insert(Category category);

    @Update
    void update(Category category);

    @Delete
    void delete(Category category);

}
