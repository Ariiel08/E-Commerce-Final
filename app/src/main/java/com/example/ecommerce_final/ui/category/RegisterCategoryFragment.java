package com.example.ecommerce_final.ui.category;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.example.ecommerce_final.MainActivity;
import com.example.ecommerce_final.R;
import com.example.ecommerce_final.database.AppDatabase;
import com.example.ecommerce_final.database.AppExecutors;
import com.example.ecommerce_final.database.CategoryDAO;
import com.example.ecommerce_final.databinding.FragmentRegisterCategoryBinding;
import com.example.ecommerce_final.models.Category;
import com.example.ecommerce_final.models.Product;
import com.example.ecommerce_final.models.User;
import com.example.ecommerce_final.services.FirebaseServices;
import com.example.ecommerce_final.services.NetResponse;
import com.example.ecommerce_final.services.UserServices;
import com.example.ecommerce_final.ui.login.RegisterFragment;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.shashank.sony.fancytoastlib.FancyToast;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

public class RegisterCategoryFragment extends Fragment {

    private static final String TAG = "Register Category";
    private static final int RESULT_OK = -1;
    private FragmentRegisterCategoryBinding binding;
    private static final int REQUEST_CODE_STORAGE = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;
    private static Bitmap bitmapImage;
    private CategoryDAO categoryDAO;
    private String encodedImage = null;
    private Uri uri = null;
    private View root;
    private int uid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentRegisterCategoryBinding.inflate(inflater, container, false);
        root = binding.getRoot();
        categoryDAO = AppDatabase.getInstance(root.getContext()).categoryDAO();

        if(this.getArguments().getInt("id") != -1){
            binding.btnAddCategory.setText("Update Category");
            AppExecutors.getInstance().diskIO().execute(() -> {
                Category category = categoryDAO.find(this.getArguments().getInt("id"));
                fillFields(category);
            });
        }

        binding.btnSelectCategoryImage.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            imageResultLauncher.launch(intent);
        });

        binding.btnAddCategory.setOnClickListener(v -> {

            Category category = new Category(binding.txtName.getText().toString().trim(), encodedImage);

            AppExecutors.getInstance().diskIO().execute(() -> {
                if(this.getArguments().getInt("id") != -1){
                    uid = this.getArguments().getInt("id");
                    uploadImage(category);
                }else{
                    uid = (int) categoryDAO.insert(category);
                    uploadImage(category);
                }
            });

            NavHostFragment.findNavController(RegisterCategoryFragment.this)
                    .navigate(R.id.registerCategory_to_category);
        });

        return root;
    }

    private ActivityResultLauncher<Intent> imageResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        try {
                            uri = result.getData().getData();
                            InputStream inputStream = root.getContext().getContentResolver().openInputStream(uri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                            binding.imgSelectCategory.setImageBitmap(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });


    private void fillFields(Category category) {
        if(category != null){
            binding.txtName.setText(category.getName());
            if(category.getEncodedImage() != null){
                FirebaseServices.obtain().download(category.getEncodedImage(), new NetResponse<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        binding.imgSelectCategory.setImageBitmap(response);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.e(TAG, t.getMessage());
                    }
                });
                encodedImage = category.getEncodedImage();
            }
        }
    }

    public void uploadImage(Category category){
        if(uri != null){

            FirebaseServices.obtain().upload(uri, String.format("category/%s.jpg", uid),
                    new NetResponse<String>() {
                        @Override
                        public void onResponse(String response) {
                            FancyToast.makeText(root.getContext(), "Successfully upload image", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false).show();
                            System.out.println("uid upload: "+uid);
                            category.setId(uid);
                            category.setEncodedImage(response);

                            AppExecutors.getInstance().diskIO().execute(() -> {
                                categoryDAO.update(category);
                            });

                        }

                        @Override
                        public void onFailure(Throwable t) {
                            FancyToast.makeText(root.getContext(), t.getMessage(), FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                        }
                    });

        }else{
            AppExecutors.getInstance().diskIO().execute(() -> {
                category.setId(uid);
                category.setEncodedImage(encodedImage);
                categoryDAO.update(category);
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}