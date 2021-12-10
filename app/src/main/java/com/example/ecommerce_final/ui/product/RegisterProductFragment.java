package com.example.ecommerce_final.ui.product;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.navigation.fragment.NavHostFragment;
import com.example.ecommerce_final.R;
import com.example.ecommerce_final.database.AppDatabase;
import com.example.ecommerce_final.database.AppExecutors;
import com.example.ecommerce_final.database.ProductDAO;
import com.example.ecommerce_final.databinding.FragmentRegisterProductBinding;
import com.example.ecommerce_final.models.*;
import com.example.ecommerce_final.services.CarouselUpload;
import com.example.ecommerce_final.services.FirebaseServices;
import com.example.ecommerce_final.services.NetResponse;
import com.example.ecommerce_final.utils.Constants;
import com.example.ecommerce_final.utils.KProgressHUDUtils;
import com.example.ecommerce_final.utils.ValidUtil;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class RegisterProductFragment extends Fragment {

    private FragmentRegisterProductBinding binding;
    private static final int NOTIFICATION_ID = 45612;
    private static final String CHANNEL_ID = "123456";
    private ProductDAO productDAO;
    private Category category;
    private User user;
    private CarouselProducts element;
    private ArrayList<Drawable> drawables;
    private ArrayList<Uri> uris;
    private int position = 0;
    private int uid = -1;
    private boolean access = false;
    private NotificationCompat.Builder notification;
    private View root;

    public RegisterProductFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        productDAO = AppDatabase.getInstance(getContext()).productDAO();
        uid = this.getArguments().getInt("idProduct");
        notification = new NotificationCompat.Builder(this.getContext(), CHANNEL_ID);
        notification.setAutoCancel(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentRegisterProductBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        element = (CarouselProducts) getArguments().getSerializable(Constants.PRODUCT_CAROUSEL);
        User user = (User) getArguments().getSerializable(Constants.USER);

        drawables = new ArrayList<>();
        uris = new ArrayList<>();

        if(this.getArguments().getInt("idProduct") != -1){
            binding.btnAddProduct.setText("Update Product");
            AppExecutors.getInstance().diskIO().execute(() -> {
                Product product = productDAO.find(this.getArguments().getInt("idProduct"));
                fillFields(product);
            });

            if (element != null && !access) {
                if (element.carousels != null && !element.carousels.isEmpty()) {
                    final KProgressHUD progressDialog = new KProgressHUDUtils(getActivity()).showDownload();
                    FirebaseServices.obtain().downloadsCarousel(element.carousels, new NetResponse<List<Bitmap>>() {
                        @Override
                        public void onResponse(List<Bitmap> response) {
                            for (Bitmap bitmap : response) {
                                drawables.add(new BitmapDrawable(getContext().getResources(), bitmap));
                            }
                            binding.imgSelectProduct.setImageDrawable(drawables.get(0));
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            FancyToast.makeText(getContext(), t.getMessage(), FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                            progressDialog.dismiss();
                        }
                    });
                }
            }
        }

        binding.imgSelectProduct.setFactory(() -> new ImageView(getContext()));

        binding.btnNext.setOnClickListener(v -> {
            if (position < drawables.size() - 1) {
                binding.imgSelectProduct.setImageDrawable(drawables.get(++position));
            } else {
                FancyToast.makeText(getContext(), "Last Image Already Shown", FancyToast.LENGTH_SHORT, FancyToast.WARNING, false).show();
            }
        });

        binding.btnPrevious.setOnClickListener(v -> {
            if (position > 0) {
                binding.imgSelectProduct.setImageDrawable(drawables.get(--position));
            } else {
                FancyToast.makeText(getContext(), "First Image Already Shown", FancyToast.LENGTH_SHORT, FancyToast.WARNING, false).show();
            }
        });

        binding.btnSelectProductImage.setOnClickListener(v -> photoOptions());

        binding.btnDeleteImage.setOnClickListener(v -> {

            if(drawables.size() > 0){
                drawables.remove(position);

                if(position > 0){
                    position--;
                }

                if(drawables.size() == 0){
                    binding.imgSelectProduct.setImageDrawable(null);
                }else{
                    binding.imgSelectProduct.setImageDrawable(drawables.get(position));
                }
            }
        });

        binding.btnAddProduct.setOnClickListener(v -> {
            if (ValidUtil.isEmpty(getContext(), binding.txtDescription)) {
                return;
            }

            if (ValidUtil.isEmpty(getContext(), binding.txtPrice)){
                return;
            }

            if (element == null) {
                element = new CarouselProducts();
            }
//            element.product.setActive(binding.active.isChecked());

            final KProgressHUD progressDialog = new KProgressHUDUtils(getActivity()).showConnecting();

            AppExecutors.getInstance().diskIO().execute(() -> {
                category = AppDatabase.getInstance(root.getContext()).categoryDAO().findByName(binding.spnCategory.getSelectedItem().toString());

                element.product.setDescription(binding.txtDescription.getText().toString().trim());
                element.product.setCategoryId(category.getId());
                element.product.setPrice(Double.valueOf(binding.txtPrice.getText().toString().trim()));
                element.product.setUid(UUID.randomUUID().toString());

//                Product product = new Product(UUID.randomUUID().toString(), Double.parseDouble(binding.txtPrice.getText().toString().trim()),
//                        binding.txtDescription.getText().toString().trim(), encodedImage, category.getId());
                if(this.getArguments().getInt("idProduct") != -1){
                    element.product.setId(this.getArguments().getInt("idProduct"));
                    productDAO.update(element.product);
                }else{
                    long id = productDAO.insert(element.product);
                    element.product.setId((int) id);
                    notifyProduct();
                }

                if(access) {
                    List<CarouselUpload> uploads = new ArrayList<>();
                    productDAO.deleteCarousels(element.product.getId());
                    final List<Carousel> carousels = new ArrayList<>();

                    for (int index = 0; index < drawables.size(); index++) {
                        Carousel carousel = new Carousel(element.product.getId(), index, String.format("products/%s/%s.jpg", element.product.getId(), index));
                        carousels.add(carousel);
                        uploads.add(new CarouselUpload(uris.get(index), carousel));
                    }

                    productDAO.insertCarousels(carousels);

                    if (drawables != null && !drawables.isEmpty() && element.product.getId() != 0) {
                        function.apply(uploads).accept(progressDialog);
                    } else {
                        progressDialog.dismiss();
                    }
                }else{
                    progressDialog.dismiss();
                }
            });

            if(!access){
                nav();
            }
        });

        AppExecutors.getInstance().diskIO().execute(() -> {
            List<CategoryProducts> categoryProducts = AppDatabase.getInstance(this.getContext()).categoryDAO().getCategories();
            List<String> categoriesNames = new ArrayList<>();

            for (int i = 0; i < categoryProducts.size(); i++) {
                categoriesNames.add(categoryProducts.get(i).category.getName());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_spinner_dropdown_item, categoriesNames);

            getActivity().runOnUiThread(() -> {
                binding.spnCategory.setAdapter(adapter);
            });
        });

        return root;
    }

    public void nav(){
        NavHostFragment.findNavController(RegisterProductFragment.this)
                .navigate(R.id.registerProduct_to_product);
    }

    private void fillFields(Product product) {
        if(product != null){
            binding.txtDescription.setText(product.getDescription());
            binding.txtPrice.setText(String.valueOf(product.getPrice()));
        }
    }

    private final Function<List<CarouselUpload>, Consumer<KProgressHUD>> function = uploads -> progress -> {
        FirebaseServices.obtain().uploadsCarousel(uploads, new NetResponse<Void>() {
            @Override
            public void onResponse(Void response) {
                FancyToast.makeText(getContext(), "Successfully upload images", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false).show();
                progress.dismiss();
                nav();
            }

            @Override
            public void onFailure(Throwable t) {
                progress.dismiss();
                FancyToast.makeText(getContext(), t.getMessage(), FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
            }
        });

    };

    private void photoOptions() {
        // initialising intent
        Intent intent = new Intent();
        // setting type to select to be image
        intent.setType("image/*");
        // allowing multiple image to be selected
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);

        pickAndChoosePictureResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> pickAndChoosePictureResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        try {
                            final ClipData clipData = result.getData().getClipData();

                            if(uid != -1){
                                drawables = new ArrayList<>();
                                binding.imgSelectProduct.setImageDrawable(null);
                            }

                            if (clipData != null) {
                                for (int i = 0; i < clipData.getItemCount(); i++) {
                                    // adding imageuri in array
                                    final Uri uri = clipData.getItemAt(i).getUri();
                                    final InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
                                    final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                    drawables.add(new BitmapDrawable(getContext().getResources(), bitmap));
                                    access = true;

                                    uris.add(uri);
                                }
                            } else {
                                final Uri uri = result.getData().getData();
                                final InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
                                final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                drawables.add(new BitmapDrawable(getContext().getResources(), bitmap));
                                access = true;
                                uris.add(uri);
                            }
                            binding.imgSelectProduct.setImageDrawable(drawables.get(0));
                            position = 0;
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    public void notifyProduct(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "notification_product", importance);


            NotificationManager notificationManager = root.getContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        notification.setSmallIcon(R.drawable.ic__info_icon);
        notification.setTicker("New product added");
        notification.setWhen(System.currentTimeMillis());
        notification.setContentTitle("New product added");
        notification.setContentText("Product "+"'"+binding.txtDescription.getText().toString().trim()+"'"+" has been added.");

        NotificationManager notificationManager = (NotificationManager) root.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification.build());

        NotificationModel notificationModel = new NotificationModel("New product added", "Product "+"'"+binding.txtDescription.getText().toString().trim()+"'"+" has been added.");

        AppExecutors.getInstance().diskIO().execute(() -> {
            AppDatabase.getInstance(root.getContext()).notificationDAO().insert(notificationModel);
        });


    }
}