package com.example.ecommerce_final.ui.cart;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerce_final.adapters.CartAdapter;
import com.example.ecommerce_final.database.AppDatabase;
import com.example.ecommerce_final.database.AppExecutors;
import com.example.ecommerce_final.databinding.FragmentCartBinding;
import com.example.ecommerce_final.models.CartProducts;
import com.example.ecommerce_final.models.Product;
import com.example.ecommerce_final.services.CartServices;
import com.example.ecommerce_final.services.PrefManager;
import com.example.ecommerce_final.ui.product.ProductViewModel;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;


public class CartFragment extends Fragment {

    private FragmentCartBinding binding;
    private AppDatabase appDatabase;
    private RecyclerView recyclerView;
    private PrefManager pref;
    private CartAdapter cartAdapter;
    private CartServices cartServices = CartServices.getInstance();

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public CartFragment() {

    }

    public static CartFragment newInstance(String param1, String param2) {
        CartFragment fragment = new CartFragment();
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
        binding = FragmentCartBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        pref = new PrefManager(root.getContext());
        appDatabase = AppDatabase.getInstance(root.getContext());

        ProductViewModel productViewModel = new ProductViewModel(appDatabase);
        cartAdapter = new CartAdapter(root.getContext());

//        System.out.println("size pref: "+pref.getStringSet("products").size());

        if(pref.getStringSet("products") != null && cartServices.getCart().getProducts().size() == 0){
            productViewModel.getProductListLiveData().observe(this, new Observer<List<Product>>() {
                ArrayList<String> set = new ArrayList<>(pref.getStringSet("products"));
                Gson gson = new Gson();

                @Override
                public void onChanged(List<Product> products) {
                    AppExecutors.getInstance().diskIO().execute(() -> {
                        for (Product product: products) {
                            for (String aux: set) {
                                System.out.println(aux);
                                CartProducts cartProducts = gson.fromJson(aux, CartProducts.class);
                                if(cartProducts.getUid().equals(product.getUid())){
                                    cartServices.setCartProducts(cartProducts);

                                    double result = 0;

                                    for (CartProducts cartProduct:cartServices.getCart().getProducts()){
                                        result += cartProduct.getPrice()*cartProduct.getQuantity();
                                    }
                                }
                            }
                        }
                    });
                    cartAdapter.setProducts(cartServices.getCart().getProducts());
                    recyclerView.setAdapter(cartAdapter);
                }
            });
        }

        recyclerView = binding.recyclerCart;
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        recyclerView.setAdapter(cartAdapter);
        
        double result = 0;
        int quantity = 0;

        for (CartProducts cartProduct:cartServices.getCart().getProducts()){
            result += cartProduct.getPrice()*cartProduct.getQuantity();
            quantity += cartProduct.getQuantity();
        }

        binding.tvSubtotalText.setText("Subtotal ("+quantity+" items):");
        binding.tvSubtotal.setText(String.valueOf(result));

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}