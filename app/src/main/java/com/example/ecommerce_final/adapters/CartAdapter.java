package com.example.ecommerce_final.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerce_final.databinding.ItemCartBinding;
import com.example.ecommerce_final.models.CartProducts;
import com.example.ecommerce_final.services.CartServices;
import com.example.ecommerce_final.services.PrefManager;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder>{
    private ItemCartBinding binding;
    private final Context context;
    private CartServices cartServices = CartServices.getInstance();
    private PrefManager pref;
    private List<CartProducts> cartProducts;

    public CartAdapter(Context context){
        this.context = context;
        this.cartProducts = cartServices.getCart().getProducts();
    }

    public void setProducts(List<CartProducts> cartProducts){
        this.cartProducts = cartProducts;

        notifyDataSetChanged();
    }

    @NonNull
    @NotNull
    @Override
    public CartAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        binding = ItemCartBinding.inflate(inflater, parent, false);
        pref = new PrefManager(binding.getRoot().getContext());
        return new ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull CartAdapter.ViewHolder holder, int position) {
        holder.tvPriceProdCart.setText(String.valueOf(cartProducts.get(position).getPrice()));
        holder.tvDescProdCart.setText(cartProducts.get(position).getDescription());

        if(cartServices.getCart().getProducts().get(position).getEncodedImage() != null){
            holder.imgProductCart.setImageBitmap(decodeString(cartProducts.get(position).getEncodedImage()));
        }

        holder.btnDelete.setOnClickListener(v -> {
            cartServices.getCart().getProducts().remove(position);
            cartProducts = cartServices.getCart().getProducts();
            updateProductPref();
            notifyDataSetChanged();
        });

        holder.tvQuantity.setText(String.valueOf(cartProducts.get(position).getQuantity()));

        holder.btnPlusCart.setOnClickListener(v -> {
            int quantity = cartProducts.get(position).getQuantity();
            cartServices.getCart().getProducts().get(position).setQuantity(quantity + 1);
            cartProducts = cartServices.getCart().getProducts();
            holder.tvQuantity.setText(String.valueOf(cartProducts.get(position).getQuantity()));
        });

        holder.btnMinusCart.setOnClickListener(v -> {
            int quantity = cartProducts.get(position).getQuantity();
            if (quantity > 1){
                quantity -= 1;
            }
            cartServices.getCart().getProducts().get(position).setQuantity(quantity);
            cartProducts = cartServices.getCart().getProducts();
            holder.tvQuantity.setText(String.valueOf(quantity));
        });
    }

    @Override
    public int getItemCount() {
        if(cartProducts == null){
            return 0;
        }

        return cartProducts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescProdCart, tvPriceProdCart, tvQuantity;
        ImageView imgProductCart;
        Button btnPlusCart, btnMinusCart, btnDelete;
        CardView cartProductItem;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            tvDescProdCart = binding.tvDescProdCart;
            tvPriceProdCart = binding.tvPriceProdCart;
            tvQuantity = binding.tvQuantity;
            imgProductCart = binding.imgProductCart;
            btnPlusCart = binding.btnPlusCart;
            btnMinusCart = binding.btnMinusCart;
            btnDelete = binding.btnDelete;
            cartProductItem = binding.cartProductItem;

        }
    }

    public Bitmap decodeString(String encodedImage){
        byte[] data = android.util.Base64.decode(encodedImage, android.util.Base64.DEFAULT);

        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    public void updateProductPref(){
        pref.getEditor().remove("products");
        pref.getEditor().commit();
        Gson gson = new Gson();

        for (CartProducts cartProducts: cartProducts) {
            pref.update("products", gson.toJson(cartProducts));
        }
    }
}
