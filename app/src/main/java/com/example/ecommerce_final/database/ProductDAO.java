package com.example.ecommerce_final.database;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.ecommerce_final.models.Product;

import java.util.List;

@Dao
public interface ProductDAO {

    @Query("SELECT * FROM product ORDER BY id")
    LiveData<List<Product>> findAll();

    @Query("SELECT * FROM product ORDER BY id")
    List<Product> getProducts();

    @Query("SELECT * FROM product WHERE id = :id")
    Product find(int id);

    @Insert
    void insert(Product product);

    @Update
    void update(Product product);

    @Delete
    void delete(Product product);
}
