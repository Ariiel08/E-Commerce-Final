package com.example.ecommerce_final.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerce_final.R;
import com.example.ecommerce_final.database.AppDatabase;
import com.example.ecommerce_final.database.AppExecutors;
import com.example.ecommerce_final.database.CategoryDAO;
import com.example.ecommerce_final.database.ProductDAO;
import com.example.ecommerce_final.databinding.ItemCategoryBinding;
import com.example.ecommerce_final.databinding.ItemProductBinding;
import com.example.ecommerce_final.models.*;
import com.example.ecommerce_final.services.CartServices;
import com.example.ecommerce_final.services.FirebaseServices;
import com.example.ecommerce_final.services.NetResponse;
import com.example.ecommerce_final.services.PrefManager;
import com.example.ecommerce_final.ui.product.ProductDetailsFragment;
import com.example.ecommerce_final.ui.product.ProductFragment;
import com.example.ecommerce_final.utils.CommonUtil;
import com.example.ecommerce_final.utils.Constants;
import com.shashank.sony.fancytoastlib.FancyToast;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder>{
    private static final String TAG = "ProductAdapter";
    private List<Product> products;
    private List<CarouselProducts> searchProducts;
    private ItemProductBinding binding;
    private List<CarouselProducts> elements;
    private final Context context;
    private PrefManager session;
    private User user;
    private CartServices cartServices;
    private boolean option = true;

    public ProductAdapter(Context context, User user){
        this.context = context;
        this.user = user;
    }

    public void setProducts(List<CarouselProducts> products){
        this.elements = products;
        this.searchProducts = new ArrayList<>(products);

        notifyDataSetChanged();
    }

    public void setOption(boolean value){
        this.option = value;
    }

    @NonNull
    @NotNull
    @Override
    public ProductAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        binding = ItemProductBinding.inflate(inflater, parent, false);
        cartServices = CartServices.getInstance();
        return new ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ProductAdapter.ViewHolder holder, int position) {
        holder.tvUid.setText(elements.get(position).product.getUid());
        holder.tvPrice.setText("$"+String.valueOf(elements.get(position).product.getPrice()));
        holder.tvDesc.setText(elements.get(position).product.getDescription());

        if (elements.get(position).carousels != null && !elements.get(position).carousels.isEmpty()) {
            Optional<Carousel> optional = elements.get(position).carousels.stream()
                    .sorted((a1, a2) -> Integer.valueOf(a1.getLineNum()).compareTo(a2.getLineNum()))
                    .findFirst();

            if (optional.isPresent()) {
                downloadImage(optional.get().getPhoto(), binding.imgProduct);
            }
        }

        if (!user.getRol().equals(User.ROL.SELLER)) {
            holder.btnOptions.setVisibility(View.INVISIBLE);
        }

        holder.btnOptions.setOnClickListener(v -> {
            //creating a popup menu
            PopupMenu popup = new PopupMenu(context, holder.btnOptions);
            //inflating menu from xml resource
            popup.inflate(R.menu.options_menu);
            //adding click listener
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.edit_product:
                        Bundle bundle = new Bundle();
                        bundle.putInt("idProduct", elements.get(holder.getAdapterPosition()).product.getId());
                        bundle.putSerializable(Constants.PRODUCT_CAROUSEL, elements.get(holder.getAdapterPosition()));

                        Navigation.findNavController(holder.itemView)
                                .navigate(R.id.product_to_registerProduct, bundle);

                        return true;
                    case R.id.delete_product:
                        CommonUtil.alertDialog(context, "Confirm dialog delete!",
                                "You are about to delete a product. Do you really want to proceed?",
                                () -> {
                                    AppExecutors.getInstance().diskIO().execute(() -> {
                                        ProductDAO productDAO = AppDatabase.getInstance(context).productDAO();
                                        Product product = productDAO.getProducts().get(holder.getAdapterPosition());

                                        if(cartServices.getCart().getProducts().size() > 0){
                                            for (CartProducts cartProducts: cartServices.getCart().getProducts()) {
                                                if(cartProducts.getUid().equals(product.getUid())){
                                                    cartServices.getCart().getProducts().remove(cartProducts);
                                                    break;
                                                }
                                            }

                                            productDAO.deleteCarousels(product.getId());
                                            productDAO.delete(product);
                                        }else{
                                            productDAO.deleteCarousels(product.getId());
                                            productDAO.delete(product);
                                        }
                                    });
                                    refreshFragment();
                                });
                        return true;
                    default:
                        return false;
                }
            });
            //displaying the popup
            popup.show();
        });

        holder.productItem.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putInt("idProduct", elements.get(holder.getAdapterPosition()).product.getId());
            bundle.putSerializable(Constants.PRODUCT_CAROUSEL, elements.get(position));
            Navigation.findNavController(holder.itemView)
                    .navigate(R.id.nav_productDetails, bundle);
        });

    }

    @Override
    public int getItemCount() {
        if(elements == null){
            return 0;
        }

        return elements.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDesc, tvPrice, tvUid;
        ImageView imgProduct;
        ImageView btnOptions;
        CardView productItem;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            tvDesc = binding.tvDescription;
            tvPrice = binding.tvPrice;
            tvUid = binding.tvUid;
            imgProduct = binding.imgProduct;
            btnOptions = binding.btnProductConfig;
            productItem = binding.productItem;

        }
    }

    public Bitmap decodeString(String encodedImage){
        byte[] data = android.util.Base64.decode(encodedImage, android.util.Base64.DEFAULT);

        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    public static void downloadImage(@NonNull String photo, @NonNull ImageView imageView) {
        if (photo != null && !photo.isEmpty()) {
            FirebaseServices.obtain().download(photo, new NetResponse<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    imageView.setImageBitmap(response);
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e(TAG, t.getMessage());
                }
            });
        }
    }

    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        elements.clear();
        if (charText.length() == 0) {
            elements.addAll(searchProducts);
        } else {
            for (CarouselProducts carouselProduct : searchProducts) {
                if (carouselProduct.product.getDescription().toLowerCase(Locale.getDefault()).contains(charText)) {
                    elements.add(carouselProduct);
                }
            }
        }
        notifyDataSetChanged();
    }

    private void refreshFragment(){
        Navigation.findNavController(binding.getRoot()).popBackStack();
        Navigation.findNavController(binding.getRoot())
                .navigate(R.id.nav_product);
    }
}
