package com.example.ecommerce_final.ui.login;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import com.example.ecommerce_final.MainActivity;
import com.example.ecommerce_final.R;
import com.example.ecommerce_final.databinding.FragmentRegisterBinding;
import com.example.ecommerce_final.models.User;
import com.example.ecommerce_final.services.FirebaseServices;
import com.example.ecommerce_final.services.NetResponse;
import com.example.ecommerce_final.services.PrefManager;
import com.example.ecommerce_final.services.UserServices;
import com.example.ecommerce_final.utils.ValidUtil;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.shashank.sony.fancytoastlib.FancyToast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private View root;
    private Uri uri = null;
    private int uid;
    private PrefManager session;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        root = binding.getRoot();
        session = new PrefManager(root.getContext());

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(root.getContext(), R.layout.auto_complete_item, Arrays.asList("SELLER", "CUSTOMER"));
        binding.spnRol.setAdapter(adapter);

        binding.btnRegister.setOnClickListener(v -> {

            attemptRegister();
        });

        binding.txtName.setText("test");
        binding.txtLastName.setText("testing");
        binding.txtEmail.setText("test@hotmail.com");
        binding.txtPassword.setText("123456");
        binding.txtConfirmPass.setText("123456");
        binding.txtContact.setText("849-123-4567");

        binding.tvLoginRegister.setOnClickListener(view -> {
            Intent intent = new Intent(binding.getRoot().getContext(), LoginActivity.class);
            startActivity(intent);
        });

        binding.txtBirthday.setOnClickListener(v -> {
            final Calendar cldr = Calendar.getInstance();
            int day = cldr.get(Calendar.DAY_OF_MONTH);
            int month = cldr.get(Calendar.MONTH);
            int year = cldr.get(Calendar.YEAR);
            final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            DatePickerDialog picker = new DatePickerDialog(root.getContext(),
                    (view, year1, monthOfYear, dayOfMonth) -> binding.txtBirthday.setText(dateFormat.format(cldr.getTime())), year, month, day);
            picker.show();
        });

        binding.imgProfile.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            imageResultLauncher.launch(intent);
        });

        return root;
    }

    private void attemptRegister() {
        if (ValidUtil.isEmpty(root.getContext(), this.binding.txtName, this.binding.txtLastName, this.binding.txtEmail, this.binding.txtPassword, this.binding.txtConfirmPass)) {
            return;
        }

        // Store values at the time of the login attempt.
        final String email = this.binding.txtEmail.getText().toString();
        final String password = this.binding.txtPassword.getText().toString();
        final String repeatPassword = this.binding.txtConfirmPass.getText().toString();

        if (!password.equals(repeatPassword)) {
            Toast.makeText(root.getContext(), "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ValidUtil.isEmailValid(this.binding.txtEmail, email) && ValidUtil.isPasswordValid(this.binding.txtPassword, password)) {
            final KProgressHUD progressDialog = KProgressHUD.create(root.getContext())
                    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                    .setLabel(getString(R.string.please_wait))
                    .setDetailsLabel(getString(R.string.connecting))
                    .setCancellable(false)
                    .setAnimationSpeed(2)
                    .setDimAmount(0.5f)
                    .show();

            final User user = new User()
                    .setFirstName(binding.txtName.getText().toString().trim())
                    .setLastName(binding.txtLastName.getText().toString().trim())
                    .setEmail(binding.txtEmail.getText().toString().trim())
                    .setPassword(binding.txtPassword.getText().toString().trim())
                    .setRol(User.ROL.valueOf(binding.spnRol.getSelectedItem().toString().trim()))
                    .setContact(binding.txtContact.getText().toString().trim())
                    .setBirthday(binding.txtBirthday.getText().toString().trim());

            final Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://137.184.110.89:7002")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            final Call<User> userCreateCall = retrofit.create(UserServices.class).create(user);


            call(userCreateCall, error -> {
                if (error) {
                    progressDialog.dismiss();
                    return;
                }

                if(uri != null){
                    FirebaseServices.obtain().upload(uri, String.format("profile/%s.jpg", uid),
                            new NetResponse<String>() {
                                @Override
                                public void onResponse(String response) {
                                    FancyToast.makeText(root.getContext(), "Successfully upload image", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false).show();

                                    user.setPhoto(response);
                                    user.setUid(uid);
                                    final Call<User> userUpdateCall = retrofit.create(UserServices.class).update(user);

                                    call(userUpdateCall, res1 -> progressDialog.dismiss());
                                    session.createLoginSession(user);
                                    Intent intent = new Intent(binding.getRoot().getContext(), MainActivity.class);
                                    intent.putExtra("name",user.getFirstName() +" "+ user.getLastName());
                                    intent.putExtra("email", user.getEmail());
                                    startActivity(intent);
                                    getParentFragmentManager().beginTransaction().remove(RegisterFragment.this).commitAllowingStateLoss();
                                }

                                @Override
                                public void onFailure(Throwable t) {
                                    progressDialog.dismiss();
                                    FancyToast.makeText(root.getContext(), t.getMessage(), FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                                }
                            });
                }else{
                    progressDialog.dismiss();
                    session.createLoginSession(user);
                    Intent intent = new Intent(binding.getRoot().getContext(), MainActivity.class);
                    intent.putExtra("name",user.getFirstName() +" "+ user.getLastName());
                    intent.putExtra("email", user.getEmail());
                    startActivity(intent);
                }
            });


        }
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

                            binding.imgProfile.setImageBitmap(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    private void call(Call<User> call, Consumer<Boolean> error) {
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                switch (response.code()) {
                    case 201:
                        FancyToast.makeText(root.getContext(), "Successfully registered", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false).show();
                        uid = response.body().getUid();
                        error.accept(false);
                        break;
                    case 204:
                        FancyToast.makeText(root.getContext(), "Successfully updated", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false).show();
                        error.accept(false);
                        break;
                    default:
                        try {
                            error.accept(true);
                            FancyToast.makeText(root.getContext(), response.errorBody().string(), FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                error.accept(true);
                FancyToast.makeText(root.getContext(), t.getMessage(), FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
            }
        });

    }
}