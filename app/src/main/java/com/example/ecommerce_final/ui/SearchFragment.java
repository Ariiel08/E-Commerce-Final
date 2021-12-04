package com.example.ecommerce_final.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerce_final.adapters.ProductAdapter;
import com.example.ecommerce_final.database.AppDatabase;
import com.example.ecommerce_final.database.AppExecutors;
import com.example.ecommerce_final.database.ProductDAO;
import com.example.ecommerce_final.databinding.FragmentSearchBinding;
import com.example.ecommerce_final.models.Product;
import com.example.ecommerce_final.ui.product.ProductViewModel;

import java.util.List;


public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private ProductAdapter productAdapter;
    private ProductDAO productDAO;
    private RecyclerView recyclerView;


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public SearchFragment() {

    }

    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        productDAO = AppDatabase.getInstance(root.getContext()).productDAO();
        productAdapter = new ProductAdapter(root.getContext());

        AppExecutors.getInstance().diskIO().execute(() -> {
            productAdapter.setProducts(productDAO.getProducts());
        });

        ProductViewModel productViewModel = new ProductViewModel(AppDatabase.getInstance(root.getContext()));
        recyclerView = binding.recyclerSearch;
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        recyclerView.setAdapter(productAdapter);

        binding.search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                productViewModel.getProductListLiveData().observe( SearchFragment.this, new Observer<List<Product>>() {
                    @Override
                    public void onChanged(List<Product> products) {
                        productAdapter.filter(newText);
                    }
                });
                return false;
            }
        });


        return root;
    }
}