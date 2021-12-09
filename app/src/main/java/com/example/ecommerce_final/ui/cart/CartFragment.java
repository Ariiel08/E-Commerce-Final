package com.example.ecommerce_final.ui.cart;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerce_final.R;
import com.example.ecommerce_final.adapters.CartAdapter;
import com.example.ecommerce_final.database.AppDatabase;
import com.example.ecommerce_final.database.AppExecutors;
import com.example.ecommerce_final.databinding.FragmentCartBinding;
import com.example.ecommerce_final.models.CarouselProducts;
import com.example.ecommerce_final.models.CartProducts;
import com.example.ecommerce_final.models.Product;
import com.example.ecommerce_final.services.CartServices;
import com.example.ecommerce_final.services.PrefManager;
import com.example.ecommerce_final.ui.product.ProductFragment;
import com.example.ecommerce_final.ui.product.ProductViewModel;
import com.google.gson.Gson;
import com.shashank.sony.fancytoastlib.FancyToast;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class CartFragment extends Fragment {

    private FragmentCartBinding binding;
    private AppDatabase appDatabase;
    private RecyclerView recyclerView;
    private PrefManager pref;
    private CartAdapter cartAdapter;
    private CartServices cartServices = CartServices.getInstance();
    private View root;
    private ProductViewModel productViewModel;
    private double result = 0;
    private int quantity = 0;
    private boolean bool;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        root = binding.getRoot();
        pref = new PrefManager(root.getContext());
        appDatabase = AppDatabase.getInstance(root.getContext());

        productViewModel = new ProductViewModel(appDatabase);
        cartAdapter = new CartAdapter(root.getContext());

//        System.out.println("size pref: "+pref.getStringSet("products").size());

        return root;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        if(pref.getStringSet("products") != null && cartServices.getCart().getProducts().size() == 0){
            productViewModel.getProductListLiveData().observe(this, new Observer<List<CarouselProducts>>() {
                ArrayList<String> set = new ArrayList<>(pref.getStringSet("products"));
                Gson gson = new Gson();

                @Override
                public void onChanged(List<CarouselProducts> products) {
                    AppExecutors.getInstance().diskIO().execute(() -> {
                        for (CarouselProducts element: products) {
                            for (String aux: set) {
                                System.out.println(aux);
                                CartProducts cartProducts = gson.fromJson(aux, CartProducts.class);
                                if(cartProducts.getUid().equals(element.product.getUid())){
                                    cartServices.setCartProducts(cartProducts);
                                    cartServices.setCarouselProducts(element);
                                }
                            }
                        }
                    });
                    cartAdapter.setProducts(cartServices.getCart().getProducts());
                    cartAdapter.setCarouselProducts(cartServices.getCart().getCarouselProducts());
                    recyclerView.setAdapter(cartAdapter);
                    fillFields();
                }
            });
        }

        cartAdapter.setCarouselProducts(cartServices.getCart().getCarouselProducts());
        recyclerView = binding.recyclerCart;
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        recyclerView.setAdapter(cartAdapter);

        binding.btnCheckout.setOnClickListener(v -> {
            getActivity().runOnUiThread(new Thread(){
                @Override
                public void run(){
                    FancyToast.makeText(root.getContext(), "Thank you for the purchase", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false).show();
                }
            });
            cartServices.getCart().setProducts(new ArrayList<>());
            cartAdapter.setProducts(cartServices.getCart().getProducts());
            cartAdapter.setCarouselProducts(cartServices.getCart().getCarouselProducts());
            updateProductPref();
            fillFields();

        });

        fillFields();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void fillFields(){
        result = 0;
        quantity = 0;
        for (CartProducts cartProduct:cartServices.getCart().getProducts()){
            result += cartProduct.getPrice()*cartProduct.getQuantity();
            quantity += cartProduct.getQuantity();
        }

        binding.tvSubtotalText.setText("Subtotal ("+quantity+" items):");
        binding.tvSubtotal.setText(String.valueOf(result));
    }

    public void updateProductPref(){
        pref.getEditor().remove("products");
        pref.getEditor().commit();
        Gson gson = new Gson();

        for (CartProducts cartProducts: cartServices.getCart().getProducts()) {
            pref.update("products", gson.toJson(cartProducts));
        }
    }
}