package com.example.ecommerce_final.ui.category;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.example.ecommerce_final.R;
import com.example.ecommerce_final.database.AppDatabase;
import com.example.ecommerce_final.database.AppExecutors;
import com.example.ecommerce_final.database.CategoryDAO;
import com.example.ecommerce_final.databinding.FragmentRegisterCategoryBinding;
import com.example.ecommerce_final.models.Category;
import com.example.ecommerce_final.models.Product;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegisterCategoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterCategoryFragment extends Fragment {

    private static final int RESULT_OK = -1;
    private FragmentRegisterCategoryBinding binding;
    private static final int REQUEST_CODE_STORAGE = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;
    private static Bitmap bitmapImage;
    private CategoryDAO categoryDAO;
    private String encodedImage = null;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public RegisterCategoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RegisterCategoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RegisterCategoryFragment newInstance(String param1, String param2) {
        RegisterCategoryFragment fragment = new RegisterCategoryFragment();
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

        binding = FragmentRegisterCategoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        categoryDAO = AppDatabase.getInstance(root.getContext()).categoryDAO();

        if(this.getArguments().getInt("id") != -1){
            binding.btnAddCategory.setText("Update Category");
            AppExecutors.getInstance().diskIO().execute(() -> {
                Category category = categoryDAO.find(this.getArguments().getInt("id"));
                fillFields(category);
            });
        }

        binding.btnSelectCategoryImage.setOnClickListener(view -> {
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

        binding.btnAddCategory.setOnClickListener(v -> {
            Category category = new Category(binding.txtName.getText().toString().trim(), encodedImage);

            AppExecutors.getInstance().diskIO().execute(() -> {
                if(this.getArguments().getInt("id") != -1){
                    category.setId(this.getArguments().getInt("id"));
                    categoryDAO.update(category);
                }else{
                    categoryDAO.insert(category);
                }
            });
            NavHostFragment.findNavController(RegisterCategoryFragment.this)
                    .navigate(R.id.registerCategory_to_category);
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
                        bitmapImage = BitmapFactory.decodeStream(inputStream);
                        binding.imgSelectCategory.setImageBitmap(bitmapImage);

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

    private void fillFields(Category category) {
        if(category != null){
            binding.txtName.setText(category.getName());
            if(category.getEncodedImage() != null){
                binding.imgSelectCategory.setImageBitmap(decodeString(category.getEncodedImage()));
                encodedImage = category.getEncodedImage();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}