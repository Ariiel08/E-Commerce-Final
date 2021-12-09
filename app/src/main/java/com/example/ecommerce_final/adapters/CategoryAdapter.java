package com.example.ecommerce_final.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.ecommerce_final.databinding.ItemCategoryBinding;
import com.example.ecommerce_final.models.Category;
import com.example.ecommerce_final.models.CategoryProducts;
import com.example.ecommerce_final.models.User;
import com.example.ecommerce_final.services.FirebaseServices;
import com.example.ecommerce_final.services.NetResponse;
import com.example.ecommerce_final.ui.login.LoginActivity;
import com.shashank.sony.fancytoastlib.FancyToast;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder>{
    private static final String TAG = "CategoryAdapter";
    private List<CategoryProducts> categories;
    private ItemCategoryBinding binding;
    private final Context context;
    private User user;

    public CategoryAdapter(Context context, User user){
        this.context = context;
        this.user = user;
    }

    public void setCategories(List<CategoryProducts> categories){
        this.categories = categories;
        notifyDataSetChanged();
    }

    @NonNull
    @NotNull
    @Override
    public CategoryAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        binding = ItemCategoryBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull CategoryAdapter.ViewHolder holder, int position) {
        holder.tvNameCategory.setText(categories.get(position).category.getName());

        if (categories.get(position).category.getEncodedImage() != null && !categories.get(position).category.getEncodedImage().isEmpty()) {
            FirebaseServices.obtain().download(categories.get(position).category.getEncodedImage(), new NetResponse<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    System.out.println(response.toString());
                    holder.imgCategory.setImageBitmap(response);
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e(TAG, t.getMessage());
                }
            });
        }


        if (!user.getRol().equals(User.ROL.SELLER)) {
            holder.btnOptionsCategory.setVisibility(View.INVISIBLE);
        }

        holder.btnOptionsCategory.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.btnOptionsCategory);

            popup.inflate(R.menu.options_menu);

            popup.getMenu().getItem(0).setTitle("Edit Category");
            popup.getMenu().getItem(1).setTitle("Delete Category");
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.edit_product:
                        Bundle bundle = new Bundle();
                        bundle.putInt("id", categories.get(holder.getAdapterPosition()).category.getId());
                        Navigation.findNavController(holder.itemView)
                                .navigate(R.id.category_to_registerCategory, bundle);
                        return true;
                    case R.id.delete_product:
                        AppExecutors.getInstance().diskIO().execute(() -> {
                            CategoryDAO categoryDAO = AppDatabase.getInstance(context).categoryDAO();
                            if(categoryDAO.getCategories().get(holder.getAdapterPosition()).product.size() > 0){
                                ((Activity) context).runOnUiThread(new Thread(){
                                    @Override
                                    public void run(){
                                        FancyToast.makeText(context, "This category can't be deleted because it has products associated.", FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                                    }
                                });
                            }else{
                                Category category = categoryDAO.getCategories().get(holder.getAdapterPosition()).category;
                                categoryDAO.delete(category);
                            }
                        });
                        return true;
                    default:
                        return false;
                }
            });

            popup.show();
        });

        holder.categoryItem.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putInt("id", categories.get(holder.getAdapterPosition()).category.getId());
            Navigation.findNavController(holder.itemView)
                    .navigate(R.id.category_to_product, bundle);
        });

    }

    @Override
    public int getItemCount() {
        if(categories == null){
            return 0;
        }

        return categories.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCategory;
        TextView tvNameCategory;
        ImageView btnOptionsCategory;
        CardView categoryItem;

        public ViewHolder(@NonNull @NotNull View v) {
            super(v);
            imgCategory = binding.imgCategory;
            tvNameCategory = binding.tvNameCategory;
            btnOptionsCategory = (ImageView) binding.btnOptionsCategory;
            categoryItem = binding.categoryItem;

        }
    }

    public Bitmap decodeString(String encodedImage){
        byte[] data = android.util.Base64.decode(encodedImage, android.util.Base64.DEFAULT);

        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }
}
