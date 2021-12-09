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
import com.example.ecommerce_final.MainActivity;
import com.example.ecommerce_final.adapters.ProductAdapter;
import com.example.ecommerce_final.database.AppDatabase;
import com.example.ecommerce_final.database.AppExecutors;
import com.example.ecommerce_final.database.ProductDAO;
import com.example.ecommerce_final.databinding.FragmentSearchBinding;
import com.example.ecommerce_final.models.CarouselProducts;
import com.example.ecommerce_final.models.Product;
import com.example.ecommerce_final.models.User;
import com.example.ecommerce_final.services.PrefManager;
import com.example.ecommerce_final.ui.product.ProductViewModel;

import java.util.List;


public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private ProductAdapter productAdapter;
    private ProductDAO productDAO;
    private RecyclerView recyclerView;
    private User user;
    private PrefManager session;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        retrieveSession();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        productDAO = AppDatabase.getInstance(root.getContext()).productDAO();
        productAdapter = new ProductAdapter(root.getContext(), user);

        AppExecutors.getInstance().diskIO().execute(() -> {
            productAdapter.setProducts(productDAO.getCarouselProducts());
        });
        productAdapter.setOption(false);

        ProductViewModel productViewModel = new ProductViewModel(AppDatabase.getInstance(root.getContext()));
        recyclerView = binding.recyclerSearch;
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));

        getActivity().runOnUiThread(new Thread(){
            @Override
            public void run(){
                recyclerView.setAdapter(productAdapter);
            }
        });

        binding.search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                productViewModel.getProductListLiveData().observe( SearchFragment.this, new Observer<List<CarouselProducts>>() {
                    @Override
                    public void onChanged(List<CarouselProducts> products) {
                        productAdapter.filter(newText);
                    }
                });
                return false;
            }
        });


        return root;
    }

    private void retrieveSession() {
        session = new PrefManager(getContext());
        user = session.getUserSession();
    }
}