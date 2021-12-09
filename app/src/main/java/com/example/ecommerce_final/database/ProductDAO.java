package com.example.ecommerce_final.database;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.ecommerce_final.models.Carousel;
import com.example.ecommerce_final.models.CarouselProducts;
import com.example.ecommerce_final.models.Product;

import java.util.List;

@Dao
public interface ProductDAO {

    @Query("SELECT * FROM product ORDER BY id")
    LiveData<List<CarouselProducts>> findAll();

    @Query("SELECT * FROM product ORDER BY id")
    List<CarouselProducts> getCarouselProducts();

    @Query("SELECT * FROM product ORDER BY id")
    List<Product> getProducts();

    @Query("SELECT * FROM product WHERE id = :id")
    Product find(int id);

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Product product);

    @Update
    void update(Product product);

    @Delete
    void delete(Product product);

    @Query("DELETE FROM carousel WHERE product = :uid")
    void deleteCarousels(int uid);

    @Insert
    void insertCarousels(List<Carousel> carousels);
}
