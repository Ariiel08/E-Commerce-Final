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
import com.example.ecommerce_final.database.AppExecutors;
import com.example.ecommerce_final.database.CategoryDAO;
import com.example.ecommerce_final.database.ProductDAO;
import com.example.ecommerce_final.databinding.FragmentProductBinding;
import com.example.ecommerce_final.models.CarouselProducts;
import com.example.ecommerce_final.models.Category;
import com.example.ecommerce_final.models.Product;
import com.example.ecommerce_final.models.User;
import com.example.ecommerce_final.services.PrefManager;
import com.example.ecommerce_final.utils.Constants;
import com.shashank.sony.fancytoastlib.FancyToast;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ProductFragment extends Fragment {

    private FragmentProductBinding binding;
    private ProductDAO productDAO;
    private ProductAdapter productAdapter;
    private AppDatabase appDatabase;
    private RecyclerView recyclerView;
    private User user;
    private PrefManager session;
    private int categoryId;
    private int size = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        retrieveSession();

        appDatabase = AppDatabase.getInstance(getContext());
        productDAO = appDatabase.productDAO();

        AppExecutors.getInstance().diskIO().execute(() -> {
            size = appDatabase.categoryDAO().getCategories().size();
        });
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentProductBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        appDatabase = AppDatabase.getInstance(root.getContext());
        productDAO = appDatabase.productDAO();

        productAdapter = new ProductAdapter(root.getContext(), user);

        ProductViewModel productViewModel = new ProductViewModel(appDatabase);
        recyclerView = binding.recyclerProduct;
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        recyclerView.setAdapter(productAdapter);

        try{
            if(this.getArguments().getInt("id") != -1){
                categoryId = this.getArguments().getInt("id");
                productViewModel.getProductListLiveData().observe(this, new Observer<List<CarouselProducts>>() {
                    @Override
                    public void onChanged(List<CarouselProducts> products) {
                        List<CarouselProducts> newProducts = new ArrayList<>();
                        for (CarouselProducts element : products) {
                            if(element.product.getCategoryId() == categoryId){
                                newProducts.add(element);
                            }
                        }

                        productAdapter.setProducts(newProducts);
                    }
                });
            }
        }catch (NullPointerException ex){
            productViewModel.getProductListLiveData().observe(this, new Observer<List<CarouselProducts>>() {
                @Override
                public void onChanged(List<CarouselProducts> products) {
                    productAdapter.setProducts(products);
                }
            });
        }



        Bundle bundle = new Bundle();
        bundle.putInt("idProduct", -1);
        bundle.putSerializable(Constants.PRODUCT_CAROUSEL, null);
        bundle.putSerializable(Constants.USER, user);
        binding.floatBtnAddProduct.setOnClickListener(v -> {
                if(size > 0){
                    Navigation.findNavController(root)
                            .navigate(R.id.product_to_registerProduct, bundle);
                }else{
                    FancyToast.makeText(getContext(), "You need at least one category to register a product.", FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                }
            }
        );

        if (!this.user.getRol().equals(User.ROL.SELLER)) {
            binding.floatBtnAddProduct.setVisibility(View.INVISIBLE);
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void retrieveSession() {
        //create new session object by passing application context
        session = new PrefManager(getContext());
        //get User details if logged in
        user = session.getUserSession();
    }
}