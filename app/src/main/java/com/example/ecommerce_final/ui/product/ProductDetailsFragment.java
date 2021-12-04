package com.example.ecommerce_final.ui.product;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.ecommerce_final.R;
import com.example.ecommerce_final.database.AppDatabase;
import com.example.ecommerce_final.database.AppExecutors;
import com.example.ecommerce_final.database.ProductDAO;
import com.example.ecommerce_final.databinding.FragmentProductDetailsBinding;
import com.example.ecommerce_final.models.CartProducts;
import com.example.ecommerce_final.models.Product;
import com.example.ecommerce_final.services.CartServices;
import com.example.ecommerce_final.services.PrefManager;
import com.google.gson.Gson;


public class ProductDetailsFragment extends Fragment {

    private FragmentProductDetailsBinding binding;
    private ProductDAO productDAO;
    private final CartServices cartServices = CartServices.getInstance();
    private PrefManager pref;
    private Product product;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public ProductDetailsFragment() {

    }

    public static ProductDetailsFragment newInstance(String param1, String param2) {
        ProductDetailsFragment fragment = new ProductDetailsFragment();
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

        binding = FragmentProductDetailsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        pref = new PrefManager(root.getContext());
        productDAO = AppDatabase.getInstance(root.getContext()).productDAO();

        if(this.getArguments().getInt("idProduct") != -1){
            AppExecutors.getInstance().diskIO().execute(() -> {
                product = productDAO.find(this.getArguments().getInt("idProduct"));
                fillFields(product);
            });
        }

        binding.btnPlus.setOnClickListener(v -> {
            int quantity = Integer.parseInt(binding.tvQuantity.getText().toString());
            quantity += 1;
            binding.tvQuantity.setText(String.valueOf(quantity));
        });

        binding.btnMinus.setOnClickListener(v -> {
            int quantity = Integer.parseInt(binding.tvQuantity.getText().toString());
            if (quantity > 1){
                quantity -= 1;
            }
            binding.tvQuantity.setText(String.valueOf(quantity));
        });

        binding.btnAddCart.setOnClickListener(v -> {
            productToCartProduct(Integer.parseInt(binding.tvQuantity.getText().toString().trim()));
            Navigation.findNavController(root)
                    .navigate(R.id.productDetails_to_cart);
        });

        return root;
    }

    public Bitmap decodeString(String encodedImage){
        byte[] data = android.util.Base64.decode(encodedImage, android.util.Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    private void fillFields(Product product) {
        if(product != null){
            binding.tvProductDesc.setText(product.getDescription());
            binding.tvProductPrice.setText(String.valueOf(product.getPrice()));

            if(product.getEncodedImage() != null){
                binding.imgProductDetail.setImageBitmap(decodeString(product.getEncodedImage()));
            }
        }
    }

    private void productToCartProduct(int quantity){
        CartProducts cartProduct = new CartProducts(product.getUid(), product.getPrice(), quantity, product.getDescription(), product.getEncodedImage());
        Gson gson = new Gson();
        String json = gson.toJson(cartProduct);
        pref.putStringSet("products", json);
    }
}