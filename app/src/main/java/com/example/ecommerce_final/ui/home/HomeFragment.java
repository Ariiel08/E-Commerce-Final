package com.example.ecommerce_final.ui.home;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerce_final.R;
import com.example.ecommerce_final.adapters.ProductAdapter;
import com.example.ecommerce_final.database.AppDatabase;
import com.example.ecommerce_final.database.AppExecutors;
import com.example.ecommerce_final.database.ProductDAO;
import com.example.ecommerce_final.databinding.FragmentHomeBinding;
import com.example.ecommerce_final.models.CarouselProducts;
import com.example.ecommerce_final.models.User;
import com.example.ecommerce_final.services.PrefManager;
import com.example.ecommerce_final.ui.product.ProductViewModel;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    private RecyclerView recyclerView;
    private ProductDAO productDAO;
    private ProductAdapter productAdapter;
    private AppDatabase appDatabase;
    private User user;
    private PrefManager session;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        retrieveSession();
        appDatabase = AppDatabase.getInstance(getContext());
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        productDAO = AppDatabase.getInstance(root.getContext()).productDAO();
        productAdapter = new ProductAdapter(root.getContext(), user);
        ProductViewModel productViewModel = new ProductViewModel(appDatabase);


        recyclerView = binding.recyclerNewProducts;
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        recyclerView.setAdapter(productAdapter);

        productViewModel.getProductListLiveData().observe(this, new Observer<List<CarouselProducts>>() {
            @Override
            public void onChanged(List<CarouselProducts> products) {
                List<CarouselProducts> newCarouselProd = new ArrayList<>();


                AppExecutors.getInstance().diskIO().execute(() -> {
                    int size = productDAO.getCarouselProducts().size();


                    if(size == 1){
                        newCarouselProd.add(productDAO.getCarouselProducts().get(size-1));

                        getActivity().runOnUiThread(new Thread(){
                            @Override
                            public void run(){
                                productAdapter.setProducts(newCarouselProd);
                            }
                        });
                    }else if(size == 2){
                        newCarouselProd.add(productDAO.getCarouselProducts().get(size-2));
                        newCarouselProd.add(productDAO.getCarouselProducts().get(size-1));

                        getActivity().runOnUiThread(new Thread(){
                            @Override
                            public void run(){
                                productAdapter.setProducts(newCarouselProd);
                            }
                        });
                    }else if(size >= 3){
                        newCarouselProd.add(productDAO.getCarouselProducts().get(size-3));
                        newCarouselProd.add(productDAO.getCarouselProducts().get(size-2));
                        newCarouselProd.add(productDAO.getCarouselProducts().get(size-1));

                        getActivity().runOnUiThread(new Thread(){
                            @Override
                            public void run(){
                                productAdapter.setProducts(newCarouselProd);
                            }
                        });
                    }
                });
            }
        });
        return root;
    }

    private void retrieveSession() {
        session = new PrefManager(getContext());
        user = session.getUserSession();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}