package com.example.ecommerce_final.ui.product;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.ecommerce_final.R;
import com.example.ecommerce_final.database.AppDatabase;
import com.example.ecommerce_final.database.AppExecutors;
import com.example.ecommerce_final.database.ProductDAO;
import com.example.ecommerce_final.databinding.FragmentProductDetailsBinding;
import com.example.ecommerce_final.models.Carousel;
import com.example.ecommerce_final.models.CarouselProducts;
import com.example.ecommerce_final.models.CartProducts;
import com.example.ecommerce_final.models.Product;
import com.example.ecommerce_final.services.CartServices;
import com.example.ecommerce_final.services.FirebaseServices;
import com.example.ecommerce_final.services.NetResponse;
import com.example.ecommerce_final.services.PrefManager;
import com.example.ecommerce_final.ui.login.RegisterFragment;
import com.example.ecommerce_final.utils.Constants;
import com.example.ecommerce_final.utils.KProgressHUDUtils;
import com.google.gson.Gson;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public class ProductDetailsFragment extends Fragment {

    private FragmentProductDetailsBinding binding;
    private ProductDAO productDAO;
    private final CartServices cartServices = CartServices.getInstance();
    private PrefManager pref;
    private Product product;
    private CarouselProducts element;



    public ProductDetailsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentProductDetailsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        element = (CarouselProducts) getArguments().getSerializable(Constants.PRODUCT_CAROUSEL);
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
            productToCartProduct(Integer.parseInt(binding.tvQuantity.getText().toString().trim()), element.carousels);

            Navigation.findNavController(root).popBackStack();
            Navigation.findNavController(root)
                    .navigate(R.id.nav_cart);
        });

        if (element.carousels != null && !element.carousels.isEmpty()) {
            final KProgressHUD progressDialog = new KProgressHUDUtils(getActivity()).showDownload();
            FirebaseServices.obtain().downloadsCarousel(element.carousels, new NetResponse<List<Bitmap>>() {
                @Override
                public void onResponse(List<Bitmap> response) {
                    ArrayList<Drawable> drawables = new ArrayList<>();
                    for (Bitmap bitmap : response) {
                        drawables.add(new BitmapDrawable(getContext().getResources(), bitmap));
                    }
                    carouselView.accept(drawables);
                    progressDialog.dismiss();
                }

                @Override
                public void onFailure(Throwable t) {
                    FancyToast.makeText(getContext(), t.getMessage(), FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                    progressDialog.dismiss();
                }
            });
        }

        return root;
    }

    private void fillFields(Product product) {
        if(product != null){
            binding.tvProductDesc.setText(product.getDescription());
            binding.tvProductPrice.setText(String.valueOf(product.getPrice()));
        }
    }

    private final Consumer<ArrayList<Drawable>> carouselView = new Consumer<ArrayList<Drawable>>() {
        @Override
        public void accept(ArrayList<Drawable> drawables) {
            binding.imgProductDetail.setSize(drawables.size());
            binding.imgProductDetail.setCarouselViewListener((view1, position) -> {
                ImageView imageView = view1.findViewById(R.id.imageView);
                imageView.setImageDrawable(drawables.get(position));
            });
            binding.imgProductDetail.show();
        }
    };

    private void productToCartProduct(int quantity, List<Carousel> carousels){
        CartProducts cartProduct = new CartProducts(product.getUid(), product.getPrice(), quantity, product.getDescription(), product.getEncodedImage());
        cartProduct.setCarousels(carousels);
        Gson gson = new Gson();
        String json = gson.toJson(cartProduct);
        pref.putStringSet("products", json);
    }
}