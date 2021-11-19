package com.example.ecommerce_final.ui.product;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.navigation.fragment.NavHostFragment;
import com.example.ecommerce_final.R;
import com.example.ecommerce_final.database.AppDatabase;
import com.example.ecommerce_final.database.AppExecutors;
import com.example.ecommerce_final.database.ProductDAO;
import com.example.ecommerce_final.databinding.FragmentRegisterCategoryBinding;
import com.example.ecommerce_final.databinding.FragmentRegisterProductBinding;
import com.example.ecommerce_final.models.Category;
import com.example.ecommerce_final.models.CategoryProducts;
import com.example.ecommerce_final.models.Product;
import com.example.ecommerce_final.ui.category.RegisterCategoryFragment;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RegisterProductFragment extends Fragment {

    private FragmentRegisterProductBinding binding;
    private static final int RESULT_OK = -1;
    private static final int REQUEST_CODE_STORAGE = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;
    private ProductDAO productDAO;
    private String encodedImage = null;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public RegisterProductFragment() {

    }

    public static RegisterProductFragment newInstance(String param1, String param2) {
        RegisterProductFragment fragment = new RegisterProductFragment();
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

        binding = FragmentRegisterProductBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        productDAO = AppDatabase.getInstance(root.getContext()).productDAO();

        if(this.getArguments().getInt("idProduct") != -1){
            binding.btnAddProduct.setText("Update Product");
            AppExecutors.getInstance().diskIO().execute(() -> {
                Product product = productDAO.find(this.getArguments().getInt("idProduct"));
                fillFields(product);
            });
        }

        binding.btnAddProduct.setOnClickListener(v -> {
            Product product = new Product(UUID.randomUUID().toString(), binding.txtPrice.getText().toString().trim(),
                    binding.txtDescription.getText().toString().trim(), encodedImage);

            AppExecutors.getInstance().diskIO().execute(() -> {
                if(this.getArguments().getInt("idProduct") != -1){
                    product.setId(this.getArguments().getInt("idProduct"));
                    productDAO.update(product);
                }else{
                    productDAO.insert(product);
                }
            });
            NavHostFragment.findNavController(RegisterProductFragment.this)
                    .navigate(R.id.registerProduct_to_product);
        });

        binding.btnSelectProductImage.setOnClickListener(v -> {
            if(ContextCompat.checkSelfPermission(
                    root.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE
            )!= PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE
                );
            } else{
                selectImage();
            }
        });

        AppExecutors.getInstance().diskIO().execute(() -> {
            List<CategoryProducts> categoryProducts = AppDatabase.getInstance(this.getContext()).categoryDAO().getCategories();
            List<String> categoriesNames = new ArrayList<>();

            for (int i = 0; i < categoryProducts.size(); i++) {
                categoriesNames.add(categoryProducts.get(i).category.getName());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_spinner_dropdown_item, categoriesNames);
            binding.spnCategory.setAdapter(adapter);
        });

        return root;
    }

    private void selectImage(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(binding.getRoot().getContext().getPackageManager()) != null){
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_CODE_STORAGE && grantResults.length > 0){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectImage();
            }else{
                Toast.makeText(binding.getRoot().getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if(data != null){
                Uri selectedImageUri = data.getData();
                if(selectedImageUri != null){
                    try {
                        InputStream inputStream = binding.getRoot().getContext().getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmapImage = BitmapFactory.decodeStream(inputStream);
                        binding.imgSelectProduct.setImageBitmap(bitmapImage);

                        encodedImage = getStringImage(bitmapImage);
                    }catch (Exception e){
                        Toast.makeText(binding.getRoot().getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    public Bitmap decodeString(String encodedImage){
        byte[] data = android.util.Base64.decode(encodedImage, android.util.Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    private void fillFields(Product product) {
        if(product != null){
            binding.txtDescription.setText(product.getDescription());
            binding.txtPrice.setText(product.getPrice());

            if(product.getEncodedImage() != null){
                binding.imgSelectProduct.setImageBitmap(decodeString(product.getEncodedImage()));
                encodedImage = product.getEncodedImage();
            }
        }
    }
}