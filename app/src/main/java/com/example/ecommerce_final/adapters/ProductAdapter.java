package com.example.ecommerce_final.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerce_final.R;
import com.example.ecommerce_final.database.AppDatabase;
import com.example.ecommerce_final.database.AppExecutors;
import com.example.ecommerce_final.database.CategoryDAO;
import com.example.ecommerce_final.database.ProductDAO;
import com.example.ecommerce_final.databinding.ItemCategoryBinding;
import com.example.ecommerce_final.databinding.ItemProductBinding;
import com.example.ecommerce_final.models.Category;
import com.example.ecommerce_final.models.Product;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder>{
    private List<Product> products;
    private List<Product> searchProducts;
    private ItemProductBinding binding;
    private final Context context;

    public ProductAdapter(Context context){
        this.context = context;
    }

    public void setProducts(List<Product> products){
        this.products = products;
        this.searchProducts = new ArrayList<>(products);

        notifyDataSetChanged();
    }

    @NonNull
    @NotNull
    @Override
    public ProductAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        binding = ItemProductBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ProductAdapter.ViewHolder holder, int position) {
        holder.tvUid.setText(products.get(position).getUid());
        holder.tvPrice.setText(String.valueOf(products.get(position).getPrice()));
        holder.tvDesc.setText(products.get(position).getDescription());

        if(products.get(position).getEncodedImage() != null){
            holder.imgProduct.setImageBitmap(decodeString(products.get(position).getEncodedImage()));
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
                        bundle.putInt("idProduct", products.get(holder.getAdapterPosition()).getId());
                        Navigation.findNavController(holder.itemView)
                                .navigate(R.id.product_to_registerProduct, bundle);
                        return true;
                    case R.id.delete_product:
                        AppExecutors.getInstance().diskIO().execute(() -> {
                            ProductDAO productDAO = AppDatabase.getInstance(context).productDAO();
                            Product product = productDAO.getProducts().get(holder.getAdapterPosition());
                            productDAO.delete(product);
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
            bundle.putInt("idProduct", products.get(holder.getAdapterPosition()).getId());
            Navigation.findNavController(holder.itemView)
                    .navigate(R.id.nav_productDetails, bundle);
        });
    }

    @Override
    public int getItemCount() {
        if(products == null){
            return 0;
        }

        return products.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDesc, tvPrice, tvUid;
        ImageView imgProduct;
        Button btnOptions;
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

    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        products.clear();
        if (charText.length() == 0) {
            products.addAll(searchProducts);
        } else {
            for (Product product : searchProducts) {
                if (product.getDescription().toLowerCase(Locale.getDefault()).contains(charText)) {
                    products.add(product);
                }
            }
        }
        notifyDataSetChanged();
    }
}
