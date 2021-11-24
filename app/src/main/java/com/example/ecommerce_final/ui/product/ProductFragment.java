package com.example.ecommerce_final.ui.product;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerce_final.R;
import com.example.ecommerce_final.adapters.CategoryAdapter;
import com.example.ecommerce_final.adapters.ProductAdapter;
import com.example.ecommerce_final.database.AppDatabase;
import com.example.ecommerce_final.database.CategoryDAO;
import com.example.ecommerce_final.database.ProductDAO;
import com.example.ecommerce_final.databinding.FragmentProductBinding;
import com.example.ecommerce_final.models.Category;
import com.example.ecommerce_final.models.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductFragment extends Fragment {

    private FragmentProductBinding binding;
    private ProductDAO productDAO;
    private ProductAdapter productAdapter;
    private AppDatabase appDatabase;
    private RecyclerView recyclerView;
    private int categoryId;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentProductBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        appDatabase = AppDatabase.getInstance(root.getContext());
        productDAO = appDatabase.productDAO();

        productAdapter = new ProductAdapter(root.getContext());

        ProductViewModel productViewModel = new ProductViewModel(appDatabase);
        recyclerView = binding.recyclerProduct;
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        recyclerView.setAdapter(productAdapter);

        try{
            if(this.getArguments().getInt("id") != -1){
                categoryId = this.getArguments().getInt("id");
                productViewModel.getProductListLiveData().observe(this, new Observer<List<Product>>() {
                    @Override
                    public void onChanged(List<Product> products) {
                        List<Product> newProducts = new ArrayList<>();
                        for (Product product : products) {
                            if(product.getCategoryId() == categoryId){
                                newProducts.add(product);
                            }
                        }

                        productAdapter.setProducts(newProducts);
                    }
                });
            }
        }catch (NullPointerException ex){
            productViewModel.getProductListLiveData().observe(this, new Observer<List<Product>>() {
                @Override
                public void onChanged(List<Product> products) {
                    productAdapter.setProducts(products);
                }
            });
        }



        Bundle bundle = new Bundle();
        bundle.putInt("idProduct", -1);
        binding.floatBtnAddProduct.setOnClickListener(v ->
                Navigation.findNavController(root)
                        .navigate(R.id.product_to_registerProduct, bundle));


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}