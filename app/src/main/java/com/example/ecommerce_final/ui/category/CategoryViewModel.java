package com.example.ecommerce_final.ui.category;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.ecommerce_final.database.AppDatabase;
import com.example.ecommerce_final.models.CategoryProducts;

import java.util.List;

public class CategoryViewModel extends ViewModel {

    private final LiveData<List<CategoryProducts>> categoryListLiveData;

    public CategoryViewModel(@NonNull AppDatabase appDatabase) {
        categoryListLiveData = appDatabase.categoryDAO().findAll();
    }

    public LiveData<List<CategoryProducts>> getCategoryListLiveData() {
        return categoryListLiveData;
    }
}