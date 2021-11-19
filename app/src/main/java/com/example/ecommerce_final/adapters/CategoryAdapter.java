package com.example.ecommerce_final.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecommerce_final.R;
import com.example.ecommerce_final.database.AppDatabase;
import com.example.ecommerce_final.database.AppExecutors;
import com.example.ecommerce_final.database.CategoryDAO;
import com.example.ecommerce_final.database.ProductDAO;
import com.example.ecommerce_final.databinding.ItemCategoryBinding;
import com.example.ecommerce_final.models.Category;
import com.example.ecommerce_final.models.CategoryProducts;
import com.example.ecommerce_final.models.Product;
import com.example.ecommerce_final.ui.category.CategoryFragment;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder>{
    private List<CategoryProducts> categories;
    private ItemCategoryBinding binding;
    private final Context context;

    public CategoryAdapter(Context context){
        this.context = context;
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
        if(categories.get(position).category.getEncodedImage() != null){
            holder.imgCategory.setImageBitmap(decodeString(categories.get(position).category.getEncodedImage()));
        }

        holder.btnEditCategory.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.btnEditCategory);

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
                            Category category = categoryDAO.getCategories().get(holder.getAdapterPosition()).category;
                            categoryDAO.delete(category);
                        });
                        return true;
                    default:
                        return false;
                }
            });

            popup.show();
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
        Button btnEditCategory;

        public ViewHolder(View v) {
            super(v);
            imgCategory = binding.imgCategory;
            tvNameCategory = binding.tvNameCategory;
            btnEditCategory = binding.btnEditCategory;

        }
    }

    public Bitmap decodeString(String encodedImage){
        byte[] data = android.util.Base64.decode(encodedImage, android.util.Base64.DEFAULT);

        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }
}
