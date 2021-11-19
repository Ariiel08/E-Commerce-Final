package com.example.ecommerce_final.ui.product;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.ecommerce_final.database.AppDatabase;
import com.example.ecommerce_final.models.Category;
import com.example.ecommerce_final.models.Product;

import java.util.List;

public class ProductViewModel extends ViewModel {

    private LiveData<List<Product>> productListLiveData;

    public ProductViewModel(@NonNull AppDatabase appDatabase) {
        productListLiveData = appDatabase.productDAO().findAll();
    }

    public LiveData<List<Product>> getProductListLiveData() {
        return productListLiveData;
    }
}